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
    i = (np.arange(m) + 1).reshape(-1, 1)
    def get_sample():
        return state * np.exp(
            (mu - sigma * sigma / 2) * deltat * i +
            sigma * np.sqrt(deltat) * np.cumsum(norm.rvs(size=size).dot(corr_matrix), axis=0)
        )
    return np.stack([get_sample() for _ in range(n)], axis=1)


def value(state):
    return np.max(state)


def payoff(state):
    return max(value(state) - K, 0)


# def evaluate(mesh):
#     for i in range