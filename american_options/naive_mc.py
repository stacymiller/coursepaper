import numpy as np
from constants import *
from scipy.stats import norm

def value(state):
    return np.max(state)

def payoff(state):
    return max(value(state) - K, 0)

def get_states(state, n):
    """
    
    :param state: S_0
    :param n: length of sequence
    :return: `(n,) + S_0.shape`-shaped ndarray
    """
    size = (n,) + np.asarray(state).shape
    rand = norm.rvs(size=size)
    return state * np.exp(
        (mu - sigma * sigma / 2) * deltat * (np.arange(n) + 1).reshape(-1, 1) +
        sigma * np.sqrt(deltat) * np.cumsum(rand.dot(corr_matrix), axis=0)
    )

estimates = [np.max((discount ** (np.arange(m) + 1)) * np.fromiter(map(payoff, get_states(S0, m)), np.float)) for _ in range(1000000)]
np.savetxt("naive_estimates.csv", estimates)
print(np.mean(estimates), np.std(estimates))

