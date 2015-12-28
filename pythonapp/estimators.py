import numpy as np
from stochastic_mesh import diffusion_mesh_generator, generate_mesh, lognormal_weights

__author__ = 'stacymiller'


# M = 5  # width of mesh
# n = 10  # length of mesh, # of exercise opportunities


# weight is a function that takes 3 parameters: # of time point, source state and target state
class MeshEstimator(object):
    @classmethod
    def estimate(cls, initial_price, mesh, weight, payoff=lambda S: max(20 - np.dot(S, [0.01, 1]), 0)):
        try:
            n, M, d = mesh.shape
        except ValueError:
            n, M = mesh.shape
            d = 1
        e = np.empty((n, M))
        e[-1] = map(payoff, mesh[-1])
        for t in np.arange(n-1, 0, -1) - 1:  # for n=10 array([8, 7, 6, 5, 4, 3, 2, 1, 0])
            for state in range(M):
                weights = map(weight, np.repeat(t+1, M), np.repeat(mesh[t, state], M), mesh[t+1])
                e[t, state] = max(payoff(mesh[t, state]), np.mean(np.dot(weights, e[t+1])))
        weights = map(weight, np.zeros((M,)), np.repeat(initial_price, M), mesh[0])
        estimate = max(payoff(initial_price), np.mean(np.dot(weights, e[0])))
        return estimate, e

initial_price = 100
n = 10
M = 5
drift=-0.05
volatility=0.5
mesh = generate_mesh(n=n, M=M, asset_state_generator=diffusion_mesh_generator(initial_price, drift=drift, volatility=volatility, M=M))
weight = lognormal_weights(initial_price, drift=drift, volatility=volatility, M=M)

print MeshEstimator.estimate(initial_price, mesh, weight, payoff=lambda S: max(110 - S, 0))