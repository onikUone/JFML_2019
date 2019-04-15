# -*- coding: utf-8 -*-
"""
Created on Fri Mar 15 03:59:27 2019

@author: t.urita
"""
import numpy as np


#実行ファイルと同じディレクトリ内にTrainingDataのディレクトリがあるものとしてます．
#もしなければ，directory_pathを適当に変更してください．

def load_train_data(directory_path = "TrainingData"):
    
    train_data = np.genfromtxt(directory_path + "\\GameDataG1.csv", delimiter = ',', skip_header = 1)
    
    for i in range(2, 46):
        file_path = directory_path + "\\GameDataG" + str(i)  + ".csv"
        train_data = np.append(train_data, np.genfromtxt(file_path, delimiter = ',', skip_header = 1, filling_values = -1), axis = 0)

    return train_data

class make_conclusion:
    
    def __init__(self, rule_num, load = True):
        
        self.rule_num = rule_num
        self.train_data = None
        
        if load:
            self.train_data = load_train_data()
            
#    TrainingDataからランダムに選択
    def make_choice_rule(self, seed):
        
        if self.train_data is None:
               self.train_data = load_train_data()         
        
        return np.random.choice(self.train_data[:, 7], self.rule_num)
    
#    [0,1]からランダムに選択
    def make_random_rule(self, seed):
        
        return np.random.rand(self.rule_num)
    
#    平均0.5，分散1/6の正規分布からランダムに選択
    def make_gaussian_rule(self, seed, mean = 0.5, var = 1/6):
        
        gaussian_rule = np.random.normal(loc = mean, scale = var, size = self.rule_num)
        gaussian_rule[gaussian_rule > 1] = 1
        gaussian_rule[gaussian_rule < 0] = 0
        
        return gaussian_rule

rule_num = 6**5
conclusion = make_conclusion(rule_num)

seed = 1

choice_rule1 = conclusion.make_choice_rule(seed)
np.savetxt("choice_rule1.csv", choice_rule1, delimiter = ',')

random_rule1 = conclusion.make_random_rule(seed)
np.savetxt("random_rule1.csv", random_rule1, delimiter = ',')

gaussian_rule1 = conclusion.make_gaussian_rule(seed)
np.savetxt("gaussian_rule1.csv", gaussian_rule1, delimiter = ',')

seed = 2

choice_rule2 = conclusion.make_choice_rule(seed)
np.savetxt("choice_rule2.csv", choice_rule2, delimiter = ',')

random_rule2 = conclusion.make_random_rule(seed)
np.savetxt("random_rule2.csv", random_rule2, delimiter = ',')

gaussian_rule2 = conclusion.make_gaussian_rule(seed)
np.savetxt("gaussian_rule2.csv", gaussian_rule2, delimiter = ',')
