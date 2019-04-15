# -*- coding: utf-8 -*-
"""
Created on Fri Mar 15 03:59:27 2019

@author: t.urita
"""

import numpy as np

np.random.seed(seed = 1)

choice_rule1 = np.random.choice(train_data[:, 7], 5**6)
np.savetxt("choice_rule1.csv", choice_rule1, delimiter = ',')

random_rule1 = np.random.rand(5**6)
np.savetxt("random_rule1.csv", choice_rule1, delimiter = ',')

gaussian_rule1 = np.random.normal(loc = 0.5, scale = 1/6, size = 5**6)
gaussian_rule1[gaussian_rule1 > 1] = 1
gaussian_rule1[gaussian_rule1 < 0] = 0
np.savetxt("gaussian_rule1.csv", choice_rule1, delimiter = ',')

np.random.seed(seed = 2)
choice_rule2 = np.random.choice(train_data[:, 7], 5**6)
np.savetxt("choice_rule2.csv", choice_rule2, delimiter = ',')

random_rule2 = np.random.rand(5**6)
np.savetxt("random_rule2.csv", choice_rule2, delimiter = ',')

gaussian_rule2 = np.random.normal(loc = 0.5, scale = 1/6, size = 5**6)
gaussian_rule2[gaussian_rule2 > 1] = 1
gaussian_rule2[gaussian_rule2 < 0] = 0
np.savetxt("gaussian_rule2.csv", choice_rule2, delimiter = ',')