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
matplotlib.rc('font',**{'family':'Arial'})

def show_stats(k=1, s=1, total=1, filename=""):
    data = pd.read_table(filename, sep=",", decimal=".")
    # data = data[data.branches <= 30]
    means = data.groupby("branches").mean()
    q = norm.ppf(0.975)
    uperr = q*data.groupby("branches").std().upper_estimator / np.sqrt(len(means))
    # print means.index
    lowerr = q*data.groupby("branches").std().lower_estimator / np.sqrt(len(means))
    # plt.subplot(total,1,k)
    # plt.tight_layout()
    # plt.ylim(ymin=0.3, ymax=0.9)
    # plt.title(u'%d возможных состояний' % (s*100))
    up = plt.errorbar(means.index, means.upper_estimator, yerr=uperr, color="green")
    up.set_label(u"верхняя оценка")
    low = plt.errorbar(means.index, means.lower_estimator, yerr=lowerr, color="blue")
    low.set_label(u"нижняя оценка")
    plt.legend()
# border = lambda x, T, K: K - np.sqrt(5*(T-np.array(x)))
# plt.plot(GBM_consecutive(100, 0.2, 0.1, 100))
# plt.plot(range(100), border(range(100), 100, 110))
# plt.axhline(110)
# data = pd.read_table("../app/test_convergence_to_true_value_900.txt", sep=",", decimal=".")
# data = data[data.branches >= 30]
# means = data.groupby("branches").mean()
# print means.describe()

show_stats(filename="../app/test_convergence_to_true_value_random_subtree.txt")
plt.savefig("../paper/media/convergence_to_true_value_random_subtree.eps")
plt.show()