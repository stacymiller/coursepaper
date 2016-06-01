from recordclass import recordclass

import numpy as np
from scipy.stats import norm

m = 4
N = 1000
b = 200

S0 = 70
K = 100
r = 0.05
delta = 0.1
mu = r - delta
sigma = 0.2
T = 1
# interest = 0.11  # current Russian interest rate

states = np.linspace(1 / (2 * N), 1 - 1 / (2 * N), N, endpoint=True)
states = np.repeat(states[None, :], m, axis=0)
deltat = np.linspace(0, T, m)[:, None]
states = S0 * np.exp((mu - sigma * sigma / 2) * deltat + sigma * np.sqrt(deltat) * norm.ppf(states))

borders = (states[:, 1:] + states[:, :-1]) / 2

Vertex = recordclass("Vertex", ["children", "estimate"])


def get_probs(level, source):
    """
    :param level: int in 0:(m-1)
    :param source: int in 0:(N-1)
    :return: table of probabilites of transition from `states[level-1, source]` to all `states[level]`
    """
    law = norm(loc=(mu - sigma * sigma / 2) * deltat[level], scale=sigma * np.sqrt(deltat[level]))
    p = law.cdf(np.log(borders[level]) - np.log(states[level - 1, source]))
    return np.diff(np.concatenate([[0], p, [1]]))
    # p = law.cdf(np.log(states[level]) - np.log(states[level - 1, source]))  # for the sake of precision!
    # return np.array([0.5 * (p[i] + p[i + 1]) if i == 0 else
    #                  0.5 * (p[i + 1] - p[i - 1]) if i + 1 < N else
    #                  1 - 0.5 * (p[i] + p[i - 1]) for i in range(N)])


def payoff(lvl, i):
    return max(states[lvl, i] - K, 0)  # TODO: discounting (or I do not need it at all?)


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


def evaluate(tree):
    prev_lvl = tree[-1]
    for i in prev_lvl:
        prev_lvl[i].estimate = payoff(m - 1, i)

    cur_lvl = prev_lvl
    for lvl in reversed(range(m - 1)):
        prev_lvl = cur_lvl
        cur_lvl = tree[lvl]

        for i in cur_lvl:
            v = cur_lvl[i]
            continuation = [prev_lvl[x].estimate for x in v.children]
            v.estimate = max(payoff(lvl, i), np.exp(-r * T / m) * np.mean(continuation))

    return tree[0][0].estimate


results = np.array([evaluate(tree()) for _ in range(100)])
np.savetxt("init_res.csv", results, delimiter=",")
print(results.mean())
print(results.std())
