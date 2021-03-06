from recordclass import recordclass

import numpy as np
from scipy.stats import norm

m = 4
N = 1000
b = 200

S0 = 100
K = 100
r = 0.05
delta = 0.1
mu = r - delta
sigma = 0.2
T = 1

ticks = 0
Vertex = recordclass("Vertex", ["children", "estimate"])

def set_precalculated_constants(S0, T, m, N):
    deltat = np.linspace(0, T, m)[:, None]
    states = np.linspace(1 / (2 * N), 1 - 1 / (2 * N), N, endpoint=True)
    states = np.repeat(states[None, :], m, axis=0)
    states = S0 * np.exp((mu - sigma * sigma / 2) * deltat + sigma * np.sqrt(deltat) * norm.ppf(states))
    discount = np.exp(-r * T / m)
    borders = (states[:, 1:] + states[:, :-1]) / 2
    return deltat, states, borders, discount

def get_probs(level, source):
    """
    :param level: int in 0:(m-1)
    :param source: int in 0:(N-1)
    :return: table of probabilites of transition from `states[level-1, source]` to all `states[level]`
    """
    law = norm(loc=(mu - sigma * sigma / 2) * deltat[level], scale=sigma * np.sqrt(deltat[level]))
    p = law.cdf(np.log(borders[level]) - np.log(states[level - 1, source]))
    return np.diff(np.concatenate([[0], p, [1]]))


def payoff(lvl, i):
    global ticks
    ticks += 1
    return max(states[lvl, i] - K, 0)


def tree():
    def dummy():
        return Vertex([], None)

    levels = [{0: dummy()}]

    # m-1 because last row is generated on the previous iteration and does not need any additional modeling
    for lv in range(m - 1):
        levels.append({})
        for i in levels[lv]:
            """
            i is index of actual value, i.e. X_lv^{j_1, ... j_lv} = states[lv, i]
            v is dummy vertex that we should generate now
            """
            probs = get_probs(lv + 1, i)
            children = np.random.choice(range(len(probs)), size=b, replace=True, p=probs)
            levels[lv][i].children = children
            next_level = levels[lv + 1]
            for child in children:
                if not next_level.get(child, None):
                    next_level[child] = dummy()
    return levels


def evaluate(t):
    prev_lvl = t[-1]
    for i in prev_lvl:
        prev_lvl[i].estimate = payoff(m-1, i)  # estimate at the last time is just payoff

    cur_lvl = prev_lvl
    for lvl in reversed(range(m - 1)):
        prev_lvl = cur_lvl
        cur_lvl = t[lvl]

        for i in cur_lvl:
            v = cur_lvl[i]
            continuation = [prev_lvl[x].estimate for x in v.children]
            v.estimate = max(payoff(lvl, i), discount * np.mean(continuation))

    return t[0][0].estimate


# print(evaluate(tree()))

for s in [130]:
    f = open("results{}.csv".format(s), "w")
    f.write("S0,K,m,b,N,est,ticks\n")
    S0 = s
    for possible_states in [10, 100, 1000, 10000, 100000]:
        N = possible_states
        for branches in [10, 20, 50, 100, 150, 200, 300]:
            b = branches
            deltat, states, borders, discount = set_precalculated_constants(s, T, m, N)
            print("{S0},{K},{m},{b},{N}\n".format(S0=S0, K=K, m=m, b=b, N=N))
            for _ in range(1000):
                print(_)
                ticks = 0
                result = evaluate(tree())
                f.write("{S0},{K},{m},{b},{N},{est},{ticks}\n".format(S0=S0, K=K, m=m, b=b, N=N, est=result, ticks=ticks))

    f.close()