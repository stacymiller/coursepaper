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


def get_states(state, n):
    return get_states_for_time(state, 1, n)


def get_states_for_time(state, i, n):
    size = (n,) + np.asarray(state).shape
    rand = generator(size=size)
    return state * np.exp((mu - sigma * sigma / 2) * i * deltat +
                          sigma * np.sqrt(i * deltat) * rand.dot(corr_matrix))


def evaluate_tree(state, branches, steps_left, continuation):
    children = get_states(state, branches)
    if steps_left == 1:
        contin_values = continuation(children)
    else:
        contin_values = [evaluate_tree(child, branches, steps_left - 1, continuation)[0] for child in children]
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


def evaluate(h, n, m, b):
    assert (m - 1) % (h - 1) == 0
    continuation = payoffs
    pruning_moments = (np.arange((m - h) / (h - 1)).astype(np.int) + 1) * (h - 1)
    for p in reversed(pruning_moments):
        clock = time.time()
        grow_points = get_states_for_time(S0, p, n)
        # continuations = Parallel(n_jobs=4)(delayed(evaluate_tree)(state, b, h-1, continuation) for state in grow_points)
        # continuations = np.array(continuations)[:,1]
        continuations = np.array([evaluate_tree(state, b, h-1, continuation)[1] for state in grow_points])
        # continuations = np.array([evaluate_tree(state, b, h-1, continuation) for state in grow_points])
        continuation = smooth(grow_points, continuations)
        logging.debug("done {} in {}".format(p, time.time() - clock))
    clock = time.time()
    # res = np.mean([evaluate_tree(S0, b, h - 1, continuation)[0] for _ in range(n)])
    res = evaluate_tree(S0, b, h - 1, continuation)[0]
    # res = np.mean([evaluate_tree(S0, b, h - 1, continuation) for _ in range(n)])
    logging.debug("done final in {}".format(time.time() - clock))
    return res

if __name__ == '__main__':
    dim_X = 2
    rho = 0.3
    t = 1
    type = "MC"
    quasirand = QuasiNorm(10)
    if type == "MC":
        generator = norm.rvs
    elif type.find("QMC") >= 0:
        generator = quasirand.gaussian
    else:
        raise ValueError("type \"{}\" is wrong!".format(type))

    # logging.basicConfig(level=logging.INFO)
    logging.basicConfig(level=logging.DEBUG)
    S0, rho, corr_matrix, K, r, mu, delta, sigma, T, m, deltat, discount = make_constants(
        dim_X=dim_X, T=t, rho=rho, m=12
    )
    clock = time.time()
    # results = Parallel(n_jobs=4)(delayed(evaluate)(h=3, n=200, m=11, b=3) for _ in range(100))
    results = [evaluate(h=13, n=200, m=m+1, b=3) for _ in range(10)]
    logging.info("done all in {}".format(time.time() - clock))
    print(np.mean(results))
    print(np.std(results))