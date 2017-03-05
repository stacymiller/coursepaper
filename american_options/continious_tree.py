import numpy as np
from scipy.stats import norm

from constants import *
from quazi_mc_seq_gen import HaltonNorm

ticks = 0


def value(state):
    return np.max(state)


def payoff(state):
    return max(value(state) - K, 0)


quasirand = HaltonNorm(len(S0))
def get_states(state, n):
    if type == "pseudorandom":
        rand = norm.rvs(size=(n,) + np.asarray(state).shape)
    elif type == "quazirandom":
        rand = quasirand.gaussian(n)
    else:
        raise ValueError("type \"{}\" is wrong!".format(type))
    return state * np.exp((mu - sigma * sigma / 2) * deltat +
                          sigma * np.sqrt(deltat) * rand.dot(corr_matrix))


def evaluate_tree(state, branches, steps_left):
    if steps_left == 0:
        # print(" " * (m - steps_left), payoff(state))
        return payoff(state)
    children = get_states(state, branches)
    est = 0
    for child in children:
        est += evaluate_tree(child, branches, steps_left - 1)
    est /= len(children)
    # print(" " * (m - steps_left), max(payoff(state), discount * est))
    return max(payoff(state), discount * est)

np.random.seed(13)
# print(evaluate_tree(S0, 4, m))
# values = [evaluate_tree(S0, 50, m) for _ in range(100)]
# print(np.mean(values))
# print(np.std(values))
f = open("results_continious.csv", "w")
f.write("S0,K,m,b,est,type\n")

for branches in [10, 20, 50, 100, 150, 200, 300]:
    b = branches
    print("{S0},{K},{m},{b}\n".format(S0=S0, K=K, m=m, b=b))
    for s in [70, 100]:
        S0 = s * np.ones(5)
        for type in ["quazirandom", "pseudorandom"]:
            for _ in range(100):
                print(_)
                # ticks = 0
                result = evaluate_tree(S0, b, m)
                f.write("{S0},{K},{m},{b},{est},{type}\n".format(S0=np.mean(S0), K=K, m=m, b=b, est=result, type=type))
f.close()
