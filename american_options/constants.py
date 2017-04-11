import numpy as np
from numpy.linalg import cholesky
S0 = 100 * np.ones(1)
rho = 0
corr_matrix = cholesky(np.eye( 1 if (type(S0) is int) else len(S0)) * (1 - rho) + rho).T

K = 100
r = 0.05
delta = 0.1
mu = r - delta
sigma = 0.2
T = 3
m = 3
deltat = T / m
discount = np.exp(-r * deltat)
ticks = 0