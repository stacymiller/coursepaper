import time

import logging
from joblib import Parallel, delayed
from scipy import stats

from constants import *
import numpy as np
from scipy.stats import norm

from quazi_mc_seq_gen import QuasiNorm


def get_states(state, n):
    """
    :param state: S_0
    :param n: number of samples
    :return: ndarray of shape (`m`, `n`, `len(state)`)
    """
    size = (m,) + np.asarray(state).shape
    i = (np.arange(m+1)).reshape(-1, 1)
    if type == "MC":
        rand = norm.rvs
    elif type.find("QMC") >= 0:
        rand = quasirand.gaussian
    else:
        raise ValueError("type \"{}\" is wrong!".format(type))
    def get_sample():
        return state * np.exp(
            (mu - sigma * sigma / 2) * deltat * i +
            sigma * np.sqrt(deltat) * np.r_[np.zeros((1, len(state))), np.cumsum(rand(size=size).dot(corr_matrix), axis=0)]
        )
    return np.stack([get_sample() for _ in range(n)], axis=1)


def value(state):
    return np.max(state)


def payoff(state):
    return max(value(state) - K, 0)


def evaluate(b=10):
    clock = time.time()
    mesh = get_states(S0, b)
    cash_flow = np.fromiter(map(payoff, mesh[-1]), dtype=np.float32)#.reshape(-1, 1)

    for i in reversed(range(len(mesh)-1)):
        cash_flow = discount * cash_flow
        X = np.fromiter(map(value, mesh[i]), dtype=np.float32)
        predictors = np.c_[X, X**2]
        # predictors = np.c_[np.exp(-X/2), np.exp(-X/2) * (1 - X), np.exp(-X/2) * (1 - 2 * X + 0.5 * X**2)]
        immediate_payoff = np.fromiter(map(payoff, mesh[i]), dtype=np.float32)
        examples = immediate_payoff > 0
        if examples.any():
            coef, resid, rank, s = np.linalg.lstsq(predictors[examples], cash_flow[examples])
        else:
            coef, resid, rank, s = np.linalg.lstsq(predictors, cash_flow)
        continuation = predictors @ coef
        cash_flow[immediate_payoff > continuation] = immediate_payoff[immediate_payoff > continuation]

    logging.info("done in", time.time() - clock)
    return cash_flow.mean()


fmt = "{asset_dim},{rho},{T},{m},{b},{est},{type},{qdim},{group}\n"
filename = "results_lsm_halton.csv"
with open(filename, "w") as f:
    f.write(fmt.format(asset_dim="asset_dim", rho="rho", T="T", m="m", b="b",
                       est="est", type="type",
                       qdim="qdim", group="group"))

samples = 15000
types = ["MC", "QMC", "RQMC"]

# filename = "results_lsm_to_pruned.csv"
# with open(filename, "w") as f:
#     fmt = "{n},{est}\n"
#     f.write(fmt.format(n="n", est="est"))
#
# type="MC"
# S0, rho, corr_matrix, K, r, mu, delta, sigma, T, m, deltat, discount = make_constants(dim_X=2, T=1, rho=0.3, m=22)
# for n in [200*14, 1250]:
#     print(n)
#     res = Parallel(n_jobs=8)(delayed(evaluate)(200*14) for _ in range(samples))
#     with open(filename, "a") as f:
#         for est in res:
#             f.write(fmt.format(n=n, est=est))
for t, dim_X, rho in [(1, 2, 0.3), (1, 5, 0.3), (3, 5, 0)]:
    S0, rho, corr_matrix, K, r, mu, delta, sigma, T, m, deltat, discount = make_constants(dim_X=dim_X, T=t, rho=rho)

    for b in [2, 5, 10, 20, 50]:
        for type in types:
            randomized = (type.find("QMC") >= 0) and (type.find("R") >= 0)
            for qdim in [dim_X, b*m*dim_X] if type.find("QMC") >= 0 else [None]:
                if qdim is not None:
                    if qdim > 40:
                        continue
                    quasirand = QuasiNorm(int(qdim), cache=int(1e6), randomized=randomized, type="halton")
                strata = samples / 25  # 25 estimates in one stratum
                current_group = lambda i: i // (samples / strata)
                with open(filename, "a") as f:
                    for i in range(samples):
                        print(i)
                        if randomized and (current_group(i) != current_group(i - 1)):
                            quasirand.randomize()
                        est = evaluate(b)
                        f.write(fmt.format(
                            asset_dim=dim_X, rho=rho, T=T, m=m, b=b, est=est,
                            type=type, qdim=qdim, group=current_group(i)
                        ))
