import numpy as np
S0 = 100
K = 100
r = 0.05
delta = 0.1
mu = r - delta
sigma = 0.2
T = 1
m = 3
deltat = T / m
discount = np.exp(-r * deltat)
ticks = 0