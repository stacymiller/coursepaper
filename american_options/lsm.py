from scipy import stats

from constants import *
import numpy as np
from scipy.stats import norm


def get_states(state, n):
    """
    :param state: S_0
    :param n: number of samples
    :return: ndarray of shape (`m`, `n`, `len(state)`)
    """
    size = (m,) + np.asarray(state).shape
    i = (np.arange(m+1)).reshape(-1, 1)
    def get_sample():
        return state * np.exp(
            (mu - sigma * sigma / 2) * deltat * i +
            sigma * np.sqrt(deltat) * np.r_[np.zeros((1, len(state))), np.cumsum(norm.rvs(size=size).dot(corr_matrix), axis=0)]
        )
    return np.stack([get_sample() for _ in range(n)], axis=1)


def value(state):
    return np.max(state)


def payoff(state):
    return max(value(state) - K, 0)


def evaluate(b=10):
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

    return cash_flow.mean()

filename = "results_lsm.csv"
fmt = "{S0},{rho},{K},{m},{b},{est}\n"
samples = 500
with open(filename, "w") as f:
    f.write(fmt.format(S0="S0", rho="rho", K="K", m="m", b="b", est="est"))
for b in [10, 20, 50, 100, 200, 500, 1000]:
    with open(filename, "a") as f:
        for i in range(samples):
            est = evaluate(b)
            f.write(fmt.format(S0=S0[0], rho=rho, K=K, m=m, b=b, est=est))
# est = [evaluate(100) for _ in range(500)]
# print(np.mean(est), np.std(est))
