# coding=utf-8
import numpy as np
import pandas as pd
from numpy.core.umath import sqrt
from scipy.stats import norm
import pylab as plt
import matplotlib
from matplotlib import axes

__author__ = 'stacymiller'

from scipy.stats.distributions import norm

s = 10
# w = [1. / b for i in xrange(b)]
mu = 0.2
sigma = 0.005
deltat = 0.05

markers = iter(['.', 'o', 'v', 's', '*', '+', 'x', '^', '<', '>'])

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

matplotlib.rc('font', **{'family': 'Times New Roman'})


def show_stats(filename="", groupby="branches", plottitle=None, true_value=None, to_slides=False):
    lnw = 2 if to_slides else 1
    if to_slides:
        matplotlib_to_slides()
    data = pd.read_table(filename, sep=",", decimal=".")
    print data.columns
    if not plottitle and plottitle is not None:
        plottitle = (filename.split("/")[-1]).split(".")[0].replace("_", " ")
    means = data.groupby(groupby).mean()
    q = norm.ppf(0.975)
    # print data.groupby(groupby).std()
    uperr = q * data.groupby(groupby).std().upper_estimator / np.sqrt(len(means))
    lowerr = q * data.groupby(groupby).std().lower_estimator / np.sqrt(len(means))
    # # plt.subplot(total,1,k)
    # plt.ylim(ymin=0, ymax=1)
    # plt.xlim(xmin=3, xmax=min(50, means.index[-1]))
    if plottitle:
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


def RMSE_by_nop(filenames, true_value, estimator, groupby="branches", plottitle=None, to_slides=False,):
    lnw = 2 if to_slides else 1
    if to_slides:
        matplotlib_to_slides()
    plt.xscale("log")
    plt.yscale("log")
    for filename in filenames:
         # dtype={'branches': np.int32,
         #                                                            "upper_estimator": np.float64,
         #                                                            "lower_estimator": np.float64,
         #                                                            "elem_comp_upper_est": np.int32,
         #                                                            "elem_comp_lower_est": np.int32}
        data = pd.read_table(filename, sep=",", decimal=".", header=0)
        if "upper" in estimator:
            est_value = "upper_estimator"
            est_comp = "elem_comp_upper_est"
        else:
            est_value = "lower_estimator"
            est_comp = "elem_comp_lower_est"
        # if filename == "../test_convergence_to_true_value_random_subtree_6_steps_linear_ev.txt":
        #     for (index, row) in data.iterrows():
        #         print index
        #         print row[est_value]
        #         print (row[est_value] - true_value) ** 2
        print filename
        data["deviance"] = (data[est_value] - true_value) ** 2
        grouped = data.groupby(groupby)
        rmse = grouped.deviance.mean()
        comp = grouped[est_comp].mean()
        plt.xlim(xmin=comp[comp.index[0]],
                 xmax=comp[comp.index[-1]])
        plot, = plt.plot(comp, rmse, linewidth=lnw, marker=markers.next())
        plot.set_label(filename.split("_")[-2])

    # plt.rc('text', usetex=True)
    plt.xlabel(u"число простейших операций")
        # r'\# elementary computations ($\max\left\lbrace h_k\left(X_k\right), '
        # r'\mathsf{E}\left(V(X_{k+1})\middle\vert X_k\right)\right\rbrace$)')
    plt.ylabel("RMSE")
    # plt.legend()
    plt.legend(mode="expand", ncol=2, framealpha=0.5)
    plt.tight_layout()

def true_value(filename, groupby="branches"):
    data = pd.read_table(filename, sep=",", decimal=".")
    data["true_value"] = (data.upper_estimator + data.lower_estimator) / 2.
    true_values = data.groupby(groupby).true_value.mean()
    plt.plot(true_values)

def nop_by_steps(filenames, groupby="branches"):
    for filename in filenames:
        data = pd.read_table(filename, sep=",", decimal=".")
        nops = data.groupby(groupby).elem_comp_upper_est.mean()
        print nops
        plt.plot(nops.index, nops)

def draw_gbm_sample():
    gbm_sample = GBM_consecutive(100, 0.2, 0.1, 100)
    root = 110 - 15 * np.sqrt(1 - np.arange(100) / 100.)
    execution_point = None
    for (n, (g, r)) in enumerate(zip(gbm_sample, root)):
        if (g < r):
            execution_point = (n, g)
            break
    plt.plot(np.arange(100) / 100., gbm_sample)
    plt.plot(np.arange(100) / 100., root)
    plt.axhline(110, xmax=0.99, color="red")
    if execution_point:
        n, g = execution_point
        plt.plot([n / 100., n / 100.], [g, plt.ylim()[0]], ls="--", color="green")

# show_stats(filename="../test_convergence_to_true_value_random_subtree_12_steps.txt", groupby="branches", plottitle=None, to_slides=False)#, true_value=5.731)
RMSE_by_nop(filenames=["../test_convergence_to_true_value_random_subtree_6_steps_quadratic_ev.txt",
                       "../test_convergence_to_true_value_random_subtree_6_steps_linear_ev.txt",
                       "../test_convergence_to_true_value_random_subtree_6_steps_hyperbolic_ev.txt",
                       "../test_convergence_to_true_value_standard_6.txt"],
            estimator="lower",
            groupby="branches", true_value=5.95, to_slides=False)
# RMSE_by_nop(filename="../test_stats/test_convergence_to_true_value_standard_6_branches.txt",
#             groupby="branches", true_value=6.01858184667124, to_slides=False)
# plottitle=u"оценки по полному дереву", true_value=5.731)
plt.savefig("../paper/media/rmse_over_nop_lower.eps")
# plt.savefig("../paper/media/convergence_to_true_value_standard_slides.eps")
# true_value(filename="../test_stats/test_convergence_to_true_value_standard_6_branches.txt")
# nop_by_steps(filenames=["../test_convergence_to_american_option_random_subtree.txt",
#                         "../test_convergence_to_american_option_standard.txt"], groupby="execution_times")
plt.show()