import numpy as np
from scipy.stats import norm

from constants import *
from quazi_mc_seq_gen import HaltonNorm

ticks = 0


def value(state):
    return np.max(state)


def payoff(state):
    return max(value(state) - K, 0)


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

type = "quazirandom"
with open("results_continious_constructive_dimension.csv", "w") as f:
    f.write("S0,rho,K,m,b,est,type\n")
    for branches in [2,4,6,8,10]:#, 150, 200, 300]:
        b = branches
        print("{S0},{rho},{K},{m},{b},{type}\n".format(S0=np.mean(S0), rho=rho, K=K, m=m, b=b, type=type))
        quasirand = HaltonNorm(len(S0), m, b, randomized=True)
        # for s in [70, 100]:
        #     S0 = s * np.ones(5)
        for type in ["quazirandom", "pseudorandom"]:
            for i in range(500):
                print(i)
                # ticks = 0
                if (i % 10 == 0):
                    quasirand.randomize()
                result = evaluate_tree(S0, b, m)
                f.write("{S0},{rho},{K},{m},{b},{est},{type}\n".format(S0=np.mean(S0), rho=rho, K=K, m=m, b=b, est=result, type=type))
