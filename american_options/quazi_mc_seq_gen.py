from ghalton import Halton
import numpy as np


class HaltonNorm:
    def __init__(self, ndim):
        self.generator = Halton(ndim)
        # self._tail = []

    def gaussian(self, n):
        p = np.array(self.generator.get(n))
        # self._tail = p[n:len(p)]
        # print(len(p))
        u1 = p.flatten()[::2]
        u2 = p.flatten()[1::2]
        z1 = np.sqrt(-2*np.log(u1))*np.cos(2*np.pi*u2)
        z2 = np.sqrt(-2*np.log(u1))*np.sin(2*np.pi*u2)
        result = np.empty_like(p.flatten())
        result[::2] = z1
        result[1::2] = z2
        assert len(result.reshape(p.shape)) == n
        # TODO: indexing
        return result.reshape(p.shape)