import numpy as np
from numpy.core.umath import sqrt
from scipy.stats import norm
import pylab
import matplotlib

__author__ = 'stacymiller'

from scipy.stats.distributions import norm

b = 10
# w = [1. / b for i in xrange(b)]
mu = 0.2
sigma = 0.005
deltat = 0.05


def GBM_consecutive(N, volatility, u, S0):
    Wt = norm.rvs(size=N)
    dt = 1. / N
    St = np.empty((N, ))
    St[0] = S0
    for i in xrange(1, len(St)):
        St[i] = St[i - 1] * np.exp((u - 0.5 * volatility * volatility) * dt + volatility * np.sqrt(dt) * Wt[i])
    return St


def GBM(N, volatility, u, S0):
    Wt = np.cumsum(norm.rvs(size=N))
    t = np.array(range(N)) / 365.
    p1 = (u - 0.5 * volatility * volatility) * t
    p2 = volatility * Wt
    return S0 * np.exp(p1 + p2)


font = {'family': 'normal',
        'weight': 'normal',
        'size': 22}
matplotlib.rc('font', **font)

asymptotic = lambda m, k: ((m + k + 1) ** (m + k + 1)) / (sqrt(2 * np.pi * k * (m + 1)) * (k ** k) * ((m + 1) ** (m + 1)))
tr = map(asymptotic, xrange(4, 52), np.full((52-4, ), 10))
pylab.plot(tr, linewidth=2)

pylab.plot([0, 100], [110, 110], ls="-", linewidth=2, color="black")
pylab.savefig("traces.eps", format="eps")
pylab.show()
