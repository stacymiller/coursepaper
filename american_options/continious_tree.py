import numpy as np
from scipy.stats import norm

from constants import *
from quazi_mc_seq_gen import QuasiNorm


def value(state):
    return np.max(state)


def payoff(state):
    return max(value(state) - K, 0)


def get_states(state, n):
    size = (n,) + np.asarray(state).shape
    if type == "MC":
        rand = norm.rvs(size=size)
    elif type.find("QMC") >= 0:
        rand = quasirand.gaussian(size=size)
    else:
        raise ValueError("type \"{}\" is wrong!".format(type))
    return state * np.exp((mu - sigma * sigma / 2) * deltat +
                          sigma * np.sqrt(deltat) * rand.dot(corr_matrix))


def evaluate_tree(state, branches, steps_left):
    if steps_left == 0:
        # print(" " * (m - steps_left), payoff(state))
        return payoff(state), payoff(state)
    children = get_states(state, branches)
    contin_values = []
    for child in children:
        contin_values.append(evaluate_tree(child, branches, steps_left - 1))
    contin_values = np.array(contin_values)
    upper_contin_values = contin_values[:, 0]
    lower_contin_values = contin_values[:, 1]

    p = payoff(state)
    est_upper = max(p, discount * np.mean(upper_contin_values))
    est_lower = np.where(lower_contin_values.mean() - lower_contin_values < p, p, lower_contin_values).mean()
    # print(" " * (m - steps_left), max(payoff(state), discount * est))
    return est_upper, est_lower

np.random.seed(13)

samples=500
quasirand = QuasiNorm(10)
fmt = "{asset_dim},{rho},{T},{m},{b},{est_upper},{est_lower},{type},{qdim},{group}\n"

# filename = "variance_estimation.csv"
filename = "results_random_tree_sobol.csv"
with open(filename, "w") as f:
    f.write(fmt.format(asset_dim="asset_dim", rho="rho", T="T", m="m", b="b",
                       est_upper="est_upper", est_lower="est_lower", type="type",
                       qdim="qdim", group="group"))


samples = 15000
types = ["MC", "QMC", "RQMC"]

for b in [2, 5, 10, 20, 50]:
    for t, dim_X, rho in [(1, 2, 0.3), (1, 5, 0.3), (3, 5, 0)]:
        S0, rho, corr_matrix, K, r, mu, delta, sigma, T, m, deltat, discount = make_constants(dim_X=dim_X, T=t, rho=rho)

        for type in types:
            randomized = (type.find("QMC") >= 0) and (type.find("R") >= 0)
            for qdim in [dim_X, (b**np.arange(1, m+1)).sum()*dim_X] if type.find("QMC") >= 0 else [None]:
                if qdim is not None:
                    if qdim > 40:
                        continue
                    quasirand = QuasiNorm(int(qdim), cache=int(1e6), randomized=randomized, type="sobol")
                strata = samples / 25  # 25 estimates in one stratum
                current_group = lambda i: i // (samples / strata)
                with open(filename, "a") as f:
                    for i in range(samples):
                        print(i)
                        if randomized and (current_group(i) != current_group(i - 1)):
                            quasirand.randomize()
                        upper, lower = evaluate_tree(S0, b, m)
                        f.write(fmt.format(
                            asset_dim=dim_X, rho=rho, T=T, m=m, b=b, est_upper=upper, est_lower=lower,
                            type=type, qdim=qdim, group=current_group(i)
                        ))
