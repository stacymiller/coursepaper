__author__ = 'stacymiller'

class ImitatedAsset(object):
    discount_rate = 0.05
    volatility = 0.2
    dividendRate = 0.1
    def __init__(self, price, time, is_last_child=False):
        if price is None:
            raise ValueError("price is None!")
        self.price = price
        self.is_last_child = is_last_child
        self.children = set()
        self.discount_factor = self.discount_rate*time

    def add_child(self, child):
        if self.is_last_child:
            raise ValueError("last child cannot have children")
        if isinstance(child, set):
            self.children.update(child)
        self.children.add(child)

    def __gt__(self, other):
        return other.price<self.price
    def __ge__(self, other):
        return not self.price<other.price
    def __le__(self, other):
        return not other.price<self.price

# class PandomSubtreeEstimator:
#     timedelta = 1
#     steps_total = 0
#
#     @classmethod
#     def estimate(cls, branches, steps, initial_price, strike_price):
#         if cls.timedelta == 1:
#             cls.timedelta = 1. / steps
#             cls.steps_total = steps
#         asset = ImitatedAsset(initial_price, steps==0, cls.timedelta)
#         if steps > 0:
