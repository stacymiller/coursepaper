import time
from scipy import stats

from constants import *
import numpy as np


def generate_mesh(width, asset_state_generator):
    """
    Generate (`steps`, `width`) array of mesh nodes
    :param width: number of nodes at each step
    :param asset_state_generator: callable that takes number of current step `M` and desirable number of nodes `n`
    and returns ndarray of shape (`n`,) containing sample nodes for this step
    :return: ndarray of shape (`m`, `width`) containing sample nodes from each step
    """
    i = (np.arange(m)[:,None] + 1)
    xi = np.random.normal(size=(m, width)) * i
    states = S0 * np.exp(sigma * np.sqrt(deltat) * xi + (mu - sigma * sigma / 2) * i * deltat)
    return states


def gbm_state_generator(step, nodes):
    """
    Generate sample states on level `step`
    :param step: number of current step 1 <= step <= m
    :param nodes:
    :return:
    """
    t = step * deltat
    return S0 * np.exp((mu - sigma * sigma / 2) * t + np.sqrt(t) * sigma * np.random.normal(size=nodes))


def evaluate(mesh, mesh_density, transition_density, payoff):
    """
    evaluates option price using random mesh method
    :param payoff: vectorized callable such that `payoff(x, t)` payoff for the option when state of underlying asset is x
    and time from option start is t; should accept list of values t
    :param mesh_density: vectorized callable such that `mesh_density(x, t)` = density of (X_t | X_0 = S0) at point x;
    should accept list of values t
    :param transition_density: vectorized callable such that
    `transition_density(x, y)` = density of (X_{t + deltat} | X_t = x) at point y; should accept list of values for both x and y
    :param mesh: ndarray of shape (`m`, `width`) containing sample nodes for each (of the uniformly distributed) execution point
    :return: tuple of upper and lower estimates for given option
    """

    def normalized_density(step, x):
        return transition_density(x, mesh[step]) / mesh_density(x, (step + 1) * deltat)

    def upper_bound_on_layer(cur_nodes, prev_estimates):
        cur_estimates = np.zeros_like(cur_nodes, dtype=np.float32)
        # print(cur_nodes)
        for i in range(len(cur_nodes)):
            node = cur_nodes[i]
            rho = normalized_density(step, node)
            Q = np.dot(rho, prev_estimates) / np.sum(rho)
            # print("Q =", Q)
            p = payoff(node)
            # print("h =", p)
            cur_estimates[i] = max(p, discount * Q)  # )
        # print("Y_{i} = {est}".format(i = step, est = cur_estimates))

        return cur_estimates

    prev_layer = payoff(mesh[-1])
    step = 0
    for step in reversed(list(range(m - 1))):
        prev_layer = upper_bound_on_layer(mesh[step], prev_layer)

    return upper_bound_on_layer(np.array([S0]), prev_layer)


def evaluate2(mesh, density):
    Y = []
    for i in reversed(range(len(mesh))):
        # print(i)
        h = payoff(mesh[i])
        if i == len(mesh) - 1:
            Y.append(h)
            continue

        # print(mesh[i+1])
        # print(mesh[i])

        Q = []
        for x in mesh[i]:
            rho = density(x, mesh[i + 1], deltat) / density(S0, mesh[i + 1], (i + 2) * deltat)
            Q.append(np.dot(Y[-1], rho) / np.sum(rho))
        Y.append(np.maximum(h, discount * np.array(Q)))
        # print(Y[-1])
    return max(payoff(S0), discount * np.mean(Y[-1]))
    # return Y[-1][0]


def evaluate3(n):
    """
    evaulate American option using global constants: S0, K, lambda, sigma, discount, m, T
    :param n: number of execution times uniformly distributed from 0 to T
    :return: value of an option
    """
    lmbd = mu - sigma * sigma / 2
    xi = stats.norm.rvs(scale=np.arange(1, m + 1), size=(n, m)).T

    def rho(k, i, j):
        """
        transition weight from xi[k-1, i] to xi[k, j]
        :param k: step number in [0:(m-1)]
        :param i: source state number at step k-1 in [1:n]
        :param j: target state number at step k in [1:n]
        :return: float transition weight
        """
        if k == 0:
            return np.full_like(j, 1)
        # return np.sqrt(k + 1) * np.exp(- 0.5 * ((xi[k, j] - xi[k - 1, i]) ** 2) + 0.5 * (xi[k, j] ** 2) / (k + 1))
        # return np.sqrt(k + 1) * np.exp( - k * (xi[k-1, i] ** 2) / 2 + np.sqrt((k + 1) * k) * xi[k-1, i] * xi[k, j])
        return np.sqrt(k + 1) * np.exp( - 0.5 * ((np.sqrt(k + 1) * xi[k, j] - np.sqrt(k) * xi[k-1, i]) ** 2) + (xi[k, j] ** 2) / 2.)

    def state(xi, k):
        # print(xi, k)
        return S0 * np.exp(sigma * np.sqrt(deltat) * xi + lmbd * (k + 1) * deltat)

    def h(xi, k):
        """
        calculate payoff from underlying random variable
        :param xi: random state
        :param k: step number in [0:(m-1)]
        :return: payoff
        """
        return payoff(state(xi, k))

    prev_row = h(xi[m - 1], m-1)
    cur_row = None
    for k in np.arange(m - 1, -1, -1):
        if k == 0:
            cur_row = np.full_like(prev_row, payoff(S0))
        else:
            cur_row = h(xi[k - 1], k - 1)

        for i in range(n):
            rhos = rho(k, i, np.arange(n))
            hold = discount * rhos.dot(prev_row) / np.sum(rhos)
            cur_row[i] = max(cur_row[i], hold)

        prev_row = cur_row
        # print(cur_row)
    return cur_row[0]

ticks = 0


def payoff(state):
    global ticks
    ticks += 1

    return np.max([state - K, np.zeros_like(state)], axis=0)


def mesh_density(x, t):
    return 1. / 3
    # return 1 / (x * sigma * np.sqrt(2 * np.pi * t)) * S0 * np.exp(
    #     (np.log(S0) + (mu - sigma * sigma / 2) * t - np.log(x)) / (2 * sigma * sigma * t)
    # )


def transition_density(x, y, dt):
    # return np.full_like(y, 1. / len(y), dtype=np.float32)
    return 1 / (y * sigma * np.sqrt(2 * np.pi * dt)) * np.exp(
        - ((np.log(y) - np.log(x) - (mu - sigma * sigma / 2) * dt) ** 2) / (2 * sigma * sigma * dt)
    )

np.random.seed(13)
print(m)
with open("results_mesh.csv", "w") as f:
    f.write("value,n,S0\n")
    for n in np.power(10, [1, 2, 3, 4, 5]):
        for S0 in [70, 100]:
            for _ in range(100):
                print("{}, {}, {}".format(evaluate3(n), n, S0))
                f.write("{}, {}, {}\n".format(evaluate3(n), n, S0))
            # mesh = generate_mesh(n, gbm_state_generator)
    # print(mesh)
    #     print("{}, {}, 2, {}".format(evaluate2(mesh, transition_density), n, S0))
        # print()
    # # m = 3
    # # S0 = 1
    # # K = 4
    # # mesh = np.array([[1,2,5],[2,3,6],[3,4,7]])
    # # print(mesh)
    # # mesh.shape = (3,3)
    # print(evaluate(mesh, mesh_density, transition_density, payoff))
    # print(n, np.mean([evaluate(mesh, mesh_density, transition_density, payoff) for _ in range(100)]))
# print(mesh[-1])
# print(payoff(mesh[-1]))
# print(transition_density(mesh[1,1], mesh[2]) / mesh_density(mesh[2], 2 * deltat))
# print(transition_density(mesh[1,1], mesh[2]))
