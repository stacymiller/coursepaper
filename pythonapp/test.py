import numpy
from numpy.core.umath import sqrt
import pylab

__author__ = 'stacymiller'

from scipy.stats.distributions import norm
b = 10
w = [1./b for i in xrange(b)]
mu = 0.3
sigma = 0.1
deltat = 0.05
xnext = lambda xprev: [x*(1 + mu*deltat + sigma*sqrt(deltat)*norm.rvs()) for i in xrange(b) for x in xprev]
def generator(branches, parents):
    x = []
    for p in parents:
        x.extend(p*(1.+mu*deltat) + sigma*sqrt(deltat)*p*norm.rvs(size=branches))
    return numpy.asarray(x)


pylab.hist(generator(300, xnext((100, ))), bins=100)
# print xnext((100, 200))
x = xnext(xnext((100, )))
param = dict(loc=x[0]*(1.+mu*deltat), scale=sigma*sqrt(deltat)*x[0])
def F(r):
    F_value = lambda a: sum([norm(loc=p*(1.+mu*deltat), scale=sigma*sqrt(deltat)*p).cdf(a) for p in x]) / (b*b)
    # F_value = lambda a: norm(loc=x[0]*(1.+mu*deltat), scale=sigma*sqrt(deltat)*x[0]).cdf(a)
    try:
        return [F_value(arg) for arg in r]
    except TypeError as te:
        return F_value(r)
# F = lambda r: [sum([norm(loc=p*(1.+mu*deltat), scale=sigma*sqrt(deltat)*p).cdf(arg) for p in x]) / b for arg in r] if len(r)
dots = numpy.linspace(50,150,200)
# pylab.plot(dots,F(dots),'ro')
# pylab.grid(b=1)
pylab.show()