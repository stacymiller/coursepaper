import numpy as np
from numpy.linalg import cholesky

def make_constants(dim_X=5, rho=0, K=100, r=0.05, delta=0.1, sigma=0.2, T=1, m=3):
    S0 = 100 * np.ones(dim_X)
    corr_matrix = cholesky(np.eye( 1 if (type(S0) is int) else len(S0)) * (1 - rho) + rho).T

    mu = r - delta
    deltat = T / m
    discount = np.exp(-r * deltat)

    return S0, rho, corr_matrix, K, r, mu, delta, sigma, T, m, deltat, discount