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


def matplotlib_to_slides():
    font = {'family': 'Arial',
            'weight': 'normal',
            'size': 22}
    matplotlib.rc('font', **font)


def show_stats(k=1, s=1, total=1, filename="", groupby="branches", plottitle=None, true_value=None, to_slides=False):
    lnw = 2 if to_slides else 1
    if to_slides:
        matplotlib_to_slides()
    data = pd.read_table(filename, sep=",", decimal=".")
    if not plottitle:
        plottitle = (filename.split("/")[-1]).split(".")[0].replace("_", " ")
    means = data.groupby(groupby).mean()
    q = norm.ppf(0.975)
    uperr = q * data.groupby(groupby).std().upper_estimator / np.sqrt(len(means))
    lowerr = q * data.groupby(groupby).std().lower_estimator / np.sqrt(len(means))
    # # plt.subplot(total,1,k)
    # plt.ylim(ymin=0, ymax=1)
    plt.title(plottitle)
    plt.xlabel(u"число ветвей")
    plt.ylabel(u"оценки")
    up = plt.errorbar(means.index, means.upper_estimator, yerr=uperr, elinewidth=lnw, linewidth=lnw)
    up.set_label(u"верхняя оценка")
    low = plt.errorbar(means.index, means.lower_estimator, yerr=lowerr, elinewidth=lnw, linewidth=lnw)
    low.set_label(u"нижняя оценка")
    if true_value:
        tvl = plt.axhline(true_value, xmax=means.index[-1], linewidth=lnw, color="red")
        tvl.set_label(u"истинное значение")
    plt.legend()
    plt.tight_layout()


def RMSE_by_nop(filename, true_value, groupby="branches", plottitle=None, to_slides=False):
    if to_slides:
        matplotlib_to_slides()
    data = pd.read_table(filename, sep=",", decimal=".")
    # print grouped.upper_estimator - true_value
    data["upper_dev"] = (data.upper_estimator - true_value) ** 2
    data["lower_dev"] = (data.lower_estimator - true_value) ** 2
    grouped = data.groupby(groupby)
    # print (data.upper_estimator - true_value)**2
    rmse_upper = grouped.upper_dev.mean()
    rmse_lower = grouped.lower_dev.mean()
    plt.xscale("log")
    plt.plot(grouped.elem_comp_upper_est.mean(), rmse_upper)
    plt.plot(grouped.elem_comp_lower_est.mean(), rmse_lower)
    plt.rc('text', usetex=True)
    plt.xlabel(
        r'\# of elementary computations ($\max\left\lbrace h_k\left(X_k\right), '
        r'\mathsf{E}\left(V(X_{k+1})\middle\vert X_k\right)\right\rbrace$)')
    plt.ylabel("RMSE")

# plt.plot(GBM_consecutive(100, 0.2, 0.1, 100))
show_stats(filename="../app/test_convergence_to_true_value_random_subtree.txt", groupby="branches", true_value=5.731)
# RMSE_by_nop(filename="../app/test_convergence_to_true_value_standard_6_branches.txt", groupby="branches", true_value=5.731)
# plottitle=u"оценки по полному дереву", true_value=5.731)
# plt.savefig("../paper/media/rmse_by_nop_standard_3_exec_times.eps")
# plt.savefig("../paper/media/convergence_to_true_value_random_subtree_modified_ev.png")
plt.show()