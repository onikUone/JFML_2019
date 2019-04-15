# -*- coding: utf-8 -*-
"""
Created on Sun Mar 10 21:53:44 2019

@author: t.urita
"""

#欠損値は-1に設定

import numpy as np
import matplotlib.pyplot as plt

def normalize(data, data_min = 0, data_max = 0, initialize = False, shortage = False):

    if shortage:
        shortage_id = data == -1
    else:
        shortage_id = False
            
    
    if not(initialize):
        data_min = np.min(data)
        data_max = np.max(data)
    
    n_data = (data - data_min)/(data_max - data_min)
    
    n_data[n_data > 1] = 1
    n_data[n_data < 0] = 0
    n_data[shortage_id] = -1
    
    return np.reshape(n_data, [-1, 1])

train_data = np.genfromtxt("TrainingData\\GameDataG1.csv", delimiter = ',', skip_header = 1)

for i in range(2, 46):
    filename = "TrainingData\\GameDataG" + str(i)  + ".csv"
    train_data = np.append(train_data, np.genfromtxt(filename, delimiter = ',', skip_header = 1, filling_values = -1), axis = 0)

test_data = np.genfromtxt("TestingData\\GameDataG46.csv", delimiter = ',', skip_header = 1)

for i in range(47,61):
    filename = "TestingData\\GameDataG" + str(i)  + ".csv"
    test_data = np.append(test_data, np.genfromtxt(filename, delimiter = ',', skip_header = 1, filling_values = -1), axis = 0)

train_data_x1 = np.reshape(train_data[:, 0], [-1,1])
train_data_x2 = normalize(train_data[:, 1], 0, 25000, True, True)
train_data_x3 = normalize(train_data[:, 2], 0, 25000, True, True)
train_data_x4 = np.reshape(train_data[:, 3], [-1, 1])
train_data_x5 = np.reshape(train_data[:, 4], [-1, 1])
train_data_x6 = normalize(train_data[:, 5] - train_data[:, 6], -1, 1, True)
train_data_x7= np.reshape(train_data[:, 7], [-1, 1])

save_train_data = np.hstack((train_data_x1, train_data_x2, train_data_x3, train_data_x4, train_data_x5, train_data_x6, train_data_x7))
np.savetxt("train_data_fixed1.csv", save_train_data, delimiter = ',')

test_data_x1 = np.reshape(test_data[:, 0], [-1,1])
test_data_x2 = normalize(test_data[:, 1], 0, 25000, True, True)
test_data_x3 = normalize(test_data[:, 2], 0, 25000, True, True)
test_data_x4 = np.reshape(test_data[:, 3], [-1, 1])
test_data_x5 = np.reshape(test_data[:, 4], [-1, 1])
test_data_x6 = normalize(test_data[:, 5] - test_data[:, 6], -1, 1, True)
test_data_x7= np.reshape(test_data[:, 7], [-1, 1])

save_test_data = np.hstack((test_data_x1, test_data_x2, test_data_x3, test_data_x4, test_data_x5, test_data_x6, test_data_x7))
np.savetxt("test_data_fixed1.csv", save_test_data, delimiter = ',')

