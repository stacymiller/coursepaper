from ghalton import Halton
import numpy as np
from collections import deque

class HaltonNorm:
    def __init__(self, ndim, m, b, randomized=False):
        """

        :param ndim: asset dimension
        :param m: number of execution times (time 0 not included)
        :param b: number of branches
        :param randomized: should we randomize sequence?
        """
        self.ndim = ndim
        self.constructive_dim = int((b ** (np.arange(m) + 1)).sum() * ndim)
        print(self.constructive_dim)
        self.halton_dim = 500
        self.generator = Halton(self.constructive_dim)
        self._data = np.array([])
        self._cursor = 0
        self.randomize(randomized)
        self._get_data()

    def randomize(self, randomized=True):
        if randomized:
            self.random = np.random.random(size=self.constructive_dim)
        else:
            self.random = np.zeros(self.constructive_dim)

    def gaussian(self, n):
        if self._cursor >= len(self._data):
            self._get_data()
        res = self._data[self._cursor:self._cursor + n * self.ndim].reshape(n, self.ndim)
        self._cursor = self._cursor + n * self.ndim
        return res

    def _get_data(self):
        p = (np.array(self.generator.get(1)[0]) + self.random) % 1
        self._data = self._gauss_transform(p)
        self._cursor = 0

    def _gauss_transform(self, p):
        u1 = p.flatten()[::2]
        u2 = p.flatten()[1::2]
        z1 = np.sqrt(-2 * np.log(u1)) * np.cos(2 * np.pi * u2)
        z2 = np.sqrt(-2 * np.log(u1)) * np.sin(2 * np.pi * u2)
        result = np.empty_like(p.flatten())
        result[::2] = z1
        result[1::2] = z2
        return result.reshape(p.shape)