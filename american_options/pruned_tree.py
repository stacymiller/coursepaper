import logging
import numpy as np
import time

from constants import make_constants
from quazi_mc_seq_gen import QuasiNorm
from scipy.stats import norm

from joblib import Parallel, delayed


def value(state):
    return np.max(state)


def payoff(state):
    return max(value(state) - K, 0)


def payoffs(states):
    if len(states.shape) == 1:
        states = states.reshape(1, -1)
    a = np.max(states, axis=1) - K
    a[a < 0] = 0
    return a


def get_states(state, n, generator):
    return get_states_for_time(state, 1, n, generator)


def get_states_for_time(state, i, n, generator):
    size = (n,) + np.asarray(state).shape
    rand = generator(size=size)
    return state * np.exp((mu - sigma * sigma / 2) * i * deltat +
                          sigma * np.sqrt(i * deltat) * rand.dot(corr_matrix))


def evaluate_tree(state, branches, steps_left, continuation, generator):
    children = get_states(state, branches, generator)
    if steps_left == 1:
        contin_values = continuation(children)
    else:
        contin_values = [evaluate_tree(child, branches, steps_left - 1, continuation, generator)[0] for child in children]
        # contin_values = [evaluate_tree(child, branches, steps_left - 1, continuation) for child in children]

    contin_value = discount * np.mean(contin_values)
    est_upper = max(payoff(state), contin_value)
    return est_upper, contin_value


def smooth(states, continuations):
    def make_predictors(states):
        return np.c_[states, states ** 2]

    predictors = make_predictors(states)

    immediate_payoff = payoffs(states)
    examples = immediate_payoff > 0
    if not examples.any():
        examples = ~ examples
    coef, resid, rank, s = np.linalg.lstsq(predictors[examples], continuations[examples])
    # coef, resid, rank, s = np.linalg.lstsq(predictors, continuations)

    def regression(new_states):
        if len(new_states.shape) == 1:
            new_states = new_states.reshape(1, -1)
        continuations = make_predictors(new_states) @ coef
        return np.max([payoffs(new_states), continuations], axis=0)

    return regression


def evaluate(h, n, m, b, seed=np.random.randint(int(1e8)), type="MC"):
    np.random.seed(seed)
    assert (m - 1) % (h - 1) == 0
    generator_grid = generator_qr if type.find("grid") >= 0 else generator_pr
    generator_tree = generator_qr if type.find("tree") >= 0 else generator_pr
    continuation = payoffs
    pruning_moments = (np.arange((m - h) / (h - 1)).astype(np.int) + 1) * (h - 1)
    for p in reversed(pruning_moments):
        clock = time.time()
        grow_points = get_states_for_time(S0, p, n, generator_grid)
        continuations = np.array([evaluate_tree(state, b, h-1, continuation, generator_tree)[1] for state in grow_points])#.flatten()
        continuation = smooth(grow_points, continuations)
        logging.debug("done {} in {}".format(p, time.time() - clock))
    clock = time.time()
    # res = np.mean([evaluate_tree(S0, b, h - 1, continuation)[0] for _ in range(n)])
    res = evaluate_tree(S0, b, h - 1, continuation, generator_tree)[0]
    # res = np.mean([evaluate_tree(S0, b, h - 1, continuation) for _ in range(n)])
    logging.debug("done final in {}".format(time.time() - clock))
    return res


if __name__ == '__main__':
    dim_X = 2
    rho = 0.3
    t = 1
    types = ["MC", "QMC grid", "RQMC grid", "QMC tree", "RQMC tree"]
    # type = "MC"

    fmt = "{b},{h},{m},{n},{type},{est},{group}\n"
    filename = "results_pruned_qmc.csv"
    with open(filename, "w") as f:
        f.write(fmt.format(b="b",h="h",m="m",n="n",type="type",est="est",group="group"))

    logging.basicConfig(level=logging.INFO)
    # logging.basicConfig(level=logging.DEBUG)
    # for n in [50, 100, 200]:
    #     for h in range(2, 6):
    #         for b in range(2, 15):
    #             if (b ** np.arange(1, h)).sum() <= 40:
    samples = 1000
    strata = samples / 25
    current_group = lambda i: i // (samples / strata)
    for b, h, m, n in [(14, 2, 22, 200), (4, 3, 22, 100), (14, 2, 22, 100)]:
        for type in types:
            generator_pr = norm.rvs
            qdim = dim_X if type.find("grid") >= 0 else dim_X * (b ** np.arange(1, h)).sum()
            randomized = type[0] == "R"
            quasirand = QuasiNorm(qdim, randomized=randomized, type="sobol")
            generator_qr = quasirand.gaussian

            m = ((21 // (h - 1)) + 1) * (h - 1)
            S0, rho, corr_matrix, K, r, mu, delta, sigma, T, m, deltat, discount = make_constants(
                dim_X=dim_X, T=t, rho=rho, m=m
            )
            print("b = {}, h = {}, m = {}, n = {}, discount = {}, deltat = {}".format(b, h, m, n, discount, deltat))
            clock = time.time()

            if not randomized:
                results = Parallel(n_jobs=8)(
                    delayed(evaluate)(h=h, n=n, m=m+1, b=b, seed=np.random.randint(int(1e8))+s, type=type) for s in range(samples)
                )
            else:
                results = []
                for s in range(samples):
                    if current_group(s) != current_group(s - 1):
                        quasirand.randomize()
                    results.append(evaluate(h=h, n=n, m=m+1, b=b, seed=np.random.randint(int(1e8))+s, type=type))

            # results = [evaluate(h=13, n=200, m=m+1, b=3) for _ in range(10)]
            logging.info("done all in {}".format(time.time() - clock))
            print(np.mean(results))
            print(np.std(results))
            with open(filename, "a") as f:
                for i, res in enumerate(results):
                    f.write(fmt.format(b=b, h=h, m=m, n=n, type=type, est=res, group=current_group(i)))
