import unittest

from ghalton import Halton
import numpy as np
from collections import deque

class HaltonNorm:
    def __init__(self, halton_dim, cache=100, randomized=False):
        """
        :type cache: int
        :type halton_dim: int
        :param halton_dim: dimension of underlying Halton sequence
        :param cache: size of data to ask from generator (less questions to generator are better, 
        more cache -- higher memory usage)
        :param randomized: should we randomize sequence?
        """
        self.halton_dim = halton_dim
        self.generator = Halton(self.halton_dim)
        self._data = np.array([])
        self._cursor = 0
        self._cache = cache
        self.randomize(randomized)
        self._get_data()

    def randomize(self, randomized=True):
        if randomized:
            self.random = np.random.random(size=self.halton_dim)
        else:
            self.random = np.zeros(self.halton_dim)

    def gaussian(self, size):
        """
        get next elements of Halton sequence after Box-Muller transform
        :param size: shape of array to return (e.g. (12, 3) if you want 12 points from 3-dimensional space
        :return: `size`-shaped ndarray
        """
        while self._cursor + np.prod(size) >= len(self._data):
            self._get_data()
        res = self._data[self._cursor:self._cursor + np.prod(size)].reshape(size)
        self._cursor = self._cursor + np.prod(size)
        return res

    def _get_data(self):
        n = int(np.ceil(self._cache / self.halton_dim))
        p = (np.array(self.generator.get(n + n % 2)) + self.random) % 1
        self._data = np.concatenate([
            self._data[self._cursor:],
            self.gauss_transform(p.flatten())
        ])
        self._cursor = 0

    @staticmethod
    def gauss_transform(p):
        """
        applies Box-Muller transformation to `p`
        :param p: data to be transformed
        :return: ndarray of the same shape as `p`
        """
        u1 = p.flatten()[::2]
        u2 = p.flatten()[1::2]
        z1 = np.sqrt(-2 * np.log(u1)) * np.cos(2 * np.pi * u2)
        z2 = np.sqrt(-2 * np.log(u1)) * np.sin(2 * np.pi * u2)
        result = np.empty_like(p.flatten())
        result[::2] = z1
        result[1::2] = z2
        return result.reshape(p.shape)


class TestHaltonNorm(unittest.TestCase):
    def test_iterator(self):
        halton_dim = 5
        for n in [
            12,  # 12 * 5 = 60 < 100 in cache
            26,  # 26 * 5 = 130 > 100 need additional space
            100 # 100 * 5 = 500 5x cache size
        ]:
            print(n)
            g = Halton(halton_dim)
            gn = HaltonNorm(halton_dim)
            a = HaltonNorm.gauss_transform(np.array(g.get(n)))
            b = gn.gaussian((n // 2, halton_dim))
            b = gn.gaussian((n - n // 2, halton_dim))
            assert np.allclose(a[n - n // 2:], b), "wrong iteration between chunks for n = {}".format(n)