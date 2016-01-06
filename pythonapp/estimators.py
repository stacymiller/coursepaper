import numpy as np
from stochastic_mesh import diffusion_mesh_generator, generate_mesh, lognormal_weights

__author__ = 'stacymiller'


# M = 5  # width of mesh
# n = 10  # length of mesh, # of exercise opportunities


# weight is a function that takes 3 parameters: # of time point, source state and target state
# payoff is a function that takes 2 parameters: time and state
class MeshEstimator(object):
    @classmethod
    def estimate(cls, initial_price, mesh, weight, payoff):
        try:
            n, M, d = mesh.shape
        except ValueError:
            n, M = mesh.shape
            d = 1
        e = np.empty((n, M))
        e[-1] = map(payoff, np.repeat(n, M), mesh[-1])
        for t in np.arange(n-1, 0, -1) - 1:  # for n=10 array([8, 7, 6, 5, 4, 3, 2, 1, 0])
            for state in range(M):
                weights = map(weight, np.repeat(t+1, M), np.repeat(mesh[t, state], M), mesh[t+1])
                e[t, state] = max(payoff(t, mesh[t, state]), np.dot(weights, e[t+1]) / np.sum(weights))
        weights = map(weight, np.zeros((M,)), np.repeat(initial_price, M), mesh[0])
        estimate = max(payoff(0, initial_price), np.dot(weights, e[t+1]) / np.sum(weights))
        return estimate, e

initial_price = 100
n = 10
M = 50
riskless_interest_rate = 0.03
dividend_rate = 0.1
drift = riskless_interest_rate - dividend_rate
volatility = 0.3
strike_price = 100
payoff = lambda time, price: max(price - strike_price, 0)

sample_size = 10
with open("mesh_estimates_ext_corrected_model.csv", "wb") as f:
    f.write("M,n,estimate\n")
    for M in [25, 50, 75, 100]:
        for n in [10, 25, 50, 100]:
            for _ in xrange(sample_size):
                f.write("%d, %d, " % (M, n))
                weight = lognormal_weights(initial_price, drift=drift, volatility=volatility, n=n)
                mesh = generate_mesh(n=n, M=M, 
                                     asset_state_generator=diffusion_mesh_generator(initial_price, drift=drift, volatility=volatility, n=n)
                                    )
                est, trace = MeshEstimator.estimate(initial_price, mesh, weight, payoff)
                f.write("%f\n" % est)

# print trace