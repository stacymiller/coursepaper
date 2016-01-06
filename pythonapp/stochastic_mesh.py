from __future__ import division
import numpy as np

__author__ = 'stacymiller'


def generate_mesh(n, M, asset_state_generator):
    return np.array(map(lambda n: [asset_state_generator(n) for _ in range(M)], range(n)))


# all parameters can be multidimensional
def diffusion_mesh_generator(initial_price, drift, volatility, n, T=1):
    dt = T / n
    zeros = np.zeros_like(drift)
    try:
        sigma_sq = np.diag(volatility)
    except ValueError:
        assert volatility >= 0, "volatility is dispersion of norma distribution and therefore must be non-negative"
        sigma_sq = volatility

    if zeros.shape:
        from numpy.random import multivariate_normal as normal
    else:
        from numpy.random import normal as normal

    return lambda t: initial_price * np.exp((drift - sigma_sq / 2) * t * dt + np.sqrt(t * dt) * normal(zeros, volatility))


def jump_diffusion_mesh_generator():
    pass


def lognormal_weights(initial_price, drift, volatility, n, T=1):
    dt = T / n
    transitional_density = lambda x, y, s, t: np.exp(
        -(np.log(y) - np.log(x) - (drift - volatility / 2) * (t - s)) ** 2 / (2 * volatility * (t - s))
    ) / (2 * np.pi * y * np.sqrt(volatility * (t - s)))

    marginal_density = lambda state, time: transitional_density(initial_price, state, 0, time)

    return lambda time, source, target: transitional_density(source, target, time * dt, (time + 1) * dt) / marginal_density(target, (time + 1) * dt)
