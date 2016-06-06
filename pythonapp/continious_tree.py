import numpy as np
from scipy.stats import norm

m = 4
# N = 1000
b = 3

S0 = 70
K = 100
r = 0.05
delta = 0.1
mu = r - delta
sigma = 0.2
T = 1
deltat = T / m
discount = np.exp(-r * deltat)
ticks = 0
# law = norm(loc=(mu - sigma * sigma / 2) * (T / m), scale=sigma * np.sqrt(T / m))


def value(state):
    return np.max(np.array(state))


def payoff(state):
    global ticks
    ticks += 1
    return max(state - K, 0)


def get_states(state, n):
    return state * np.exp((mu - sigma * sigma / 2) * deltat + sigma * np.sqrt(deltat) * norm.rvs(size=n))


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

# print(evaluate_tree(S0, b, m))
# print("Setting: S_0 = {}, K = {}, m = {}, b = {}".format(S0, K, m, b))
# results = np.array([evaluate_tree(S0, b, m) for _ in range(1000)])
# print("{:.4f} +- {:.4f}".format(results.mean(), results.std()))

f = open("results_continious.csv", "w")
f.write("S0,K,m,b,est,ticks\n")
for s in [130]:
    S0 = s
    for branches in [10, 20, 50, 100, 150, 200, 300]:
            b = branches
        # for possible_states in [10, 100, 1000, 10000, 100000]:
        #     N = possible_states
            # deltat, states, borders, discount = set_precalculated_constants(s, T, m, N)
            print("{S0},{K},{m},{b}\n".format(S0=S0, K=K, m=m, b=b))
            for _ in range(1000):
                print(_)
                ticks = 0
                result = evaluate_tree(S0, b, m)
                f.write("{S0},{K},{m},{b},{est},{ticks}\n".format(S0=S0, K=K, m=m, b=b, est=result, ticks=ticks))
f.close()
