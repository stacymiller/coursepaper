# coding=utf-8
import numpy as np
import pandas as pd
from numpy.core.umath import sqrt
from scipy.stats import norm
import pylab as plt
import matplotlib

__author__ = 'stacymiller'

from scipy.stats.distributions import norm

s = 10
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


# font = {'family': 'normal',
#         'weight': 'normal',
#         'size': 22}
# matplotlib.rc('font',**{'family':'Droid Sans Mono'})

def show_stats(k, s, total):
    data = pd.read_table("../app/test_convergence_to_true_value_%d00.txt" % s, sep=",", decimal=".")
    data = data[data.branches <= 30]
    means = data.groupby("branches").mean()
    q = norm.ppf(0.975)
    uperr = q*data.groupby("branches").std().upper_estimator / np.sqrt(len(means))

    lowerr = q*data.groupby("branches").std().lower_estimator / np.sqrt(len(means))
    plt.subplot(total,1,k)
    plt.tight_layout()
    plt.ylim(ymin=0.3, ymax=0.9)
    plt.title(u'%d возможных состояний' % (s*100))
    plt.errorbar(range(3, len(means)+3), means.upper_estimator, yerr=uperr)
    plt.errorbar(range(3, len(means)+3), means.lower_estimator, yerr=lowerr)

t = [1,3,5,7,9]
for (i, s) in enumerate(t):
    show_stats(i+1, s, len(t))

data = pd.read_table("../app/test_convergence_to_true_value_900.txt", sep=",", decimal=".")
data = data[data.branches >= 30]
means = data.groupby("branches").mean()
print means.describe()


plt.savefig("../paper/media/true_value_test_finite_grid.eps")
plt.show()