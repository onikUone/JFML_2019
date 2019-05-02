package main;

import java.util.ArrayList;
import java.util.Arrays;

import jfml.FuzzyInferenceSystem;
import jfml.knowledgebase.KnowledgeBaseType;
import jfml.knowledgebase.variable.FuzzyVariableType;
import jfml.knowledgebase.variable.KnowledgeBaseVariable;
import jfml.knowledgebase.variable.TskVariableType;
import jfml.rule.AntecedentType;
import jfml.rule.ClauseType;
import jfml.rule.TskConsequentType;
import jfml.rule.TskFuzzyRuleType;
import jfml.rulebase.TskRuleBaseType;
import jfml.term.FuzzyTermType;
import jfml.term.TskTermType;

public class FS {

	//field
	FuzzyInferenceSystem fs;
	int ruleNum = 100;
	float[][][] fuzzyParams;
	public ArrayList<int[]> rules = new ArrayList<int[]>();
	float[] concList;
	int[][] count;

	float fitness = 100f;

	MersenneTwisterFast uniqueRnd;


	//constructor
	public FS() {}

	public FS(SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
	}


	//method

	public void generateRuleIdx(SettingForGA setting) {
		//[ruleMin, ruleMax]のランダム値
		this.ruleNum = this.uniqueRnd.nextInt(setting.ruleMax - setting.ruleMin) + setting.ruleMin;


		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			int[] rule = new int[setting.Ndim];
			//ruleIdx 生成
			for(int dim_i = 0; dim_i < setting.Ndim; dim_i++) {
				rule[dim_i] = this.uniqueRnd.nextInt(setting.Fdiv);
			}
			this.rules.add(rule);
		}

		this.concList = new float[ruleNum];
		Arrays.fill(this.concList, 0.5f);
	}


	public void setFuzzyParams(float[][][] _fuzzyParams) {
		this.fuzzyParams = _fuzzyParams;
	}

	//this.rulesからFMLを生成するメソッド
	public void makeFS(SettingForGA setting) {
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		this.fs = new FuzzyInferenceSystem();

		//KnowledgeBase ***********************************************
		KnowledgeBaseType kb = new KnowledgeBaseType();

		//inputVariable
		FuzzyVariableType[] inputVariable = new FuzzyVariableType[Ndim];
		inputVariable[0] = new FuzzyVariableType("MoveNo", 0, 1);
		inputVariable[1] = new FuzzyVariableType("DBSN", 0, 1);
		inputVariable[2] = new FuzzyVariableType("DWSN", 0, 1);
		inputVariable[3] = new FuzzyVariableType("DBWR", 0, 1);
		inputVariable[4] = new FuzzyVariableType("DWWR", 0, 1);
		inputVariable[5] = new FuzzyVariableType("DBTMR", 0, 1);
		inputVariable[6] = new FuzzyVariableType("DWTMR", 0, 1);

		//outputVariable
		TskVariableType EBWR = new TskVariableType("EBWR");
		EBWR.setType("output");
		EBWR.setCombination("WA");


		//Fuzzy Set for Input Variable (= FuzzyTerm)
		FuzzyTermType[][] gaussians = new FuzzyTermType[Ndim][Fdiv];
		String[] name = {"VerySmall", "Small", "S-Medium", "L-Medium", "Large", "VeryLarge"};

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				gaussians[dim_i][div_i] = new FuzzyTermType(name[div_i], FuzzyTermType.TYPE_gaussianShape, this.fuzzyParams[dim_i][div_i]);
				inputVariable[dim_i].addFuzzyTerm(gaussians[dim_i][div_i]);
			}
			kb.addVariable(inputVariable[dim_i]);
		}

		//Fuzzy Set for Output Variable
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			EBWR.addTskTerm( new TskTermType("Conclusion" + String.valueOf(rule_i), 0, new float[] {this.concList[rule_i]} ) );
		}
		kb.addVariable(EBWR);

		this.fs.setKnowledgeBase(kb);
		// ************************************************************

		//RuleBase ****************************************************
		TskRuleBaseType ruleBase = new TskRuleBaseType();
		ruleBase.setActivationMethod("PROD");
		ruleBase.setAndMethod("PROD");
		//rule initialize
		TskFuzzyRuleType rule;
		AntecedentType ant;
		TskConsequentType con;
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {

			//前件部 生成
			ant = new AntecedentType();
			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				ant.addClause(new ClauseType(inputVariable[dim_i], gaussians[dim_i][this.rules.get(rule_i)[dim_i]]));
			}

			//後件部 生成
			con = new TskConsequentType();
			con.addTskThenClause(EBWR, EBWR.getTerms().get(rule_i));

			rule = new TskFuzzyRuleType("rule" + String.valueOf(rule_i), "and", "PROD", 1.0f);
			rule.setAntecedent(ant);
			rule.setTskConsequent(con);

			ruleBase.addTskRule(rule);

		}

		this.fs.addRuleBase(ruleBase);
		// ************************************************************
	}



	//結論部の学習メソッド
	public void calcConclusion(SettingForGA setting, DataSetInfo tra) {
		int dataSize = tra.getDataSize();
		int Ndim = setting.Ndim;
		int ruleNum = this.rules.size();

		Pattern[] lines = new Pattern[dataSize];
		float[] y = new float[dataSize];
		float diff;
		float memberSum;
		float[][] memberships = new float[dataSize][ruleNum];
		float[] newConcList = new float[ruleNum];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable output;

		input[0] = this.fs.getVariable("MoveNo");
		input[1] = this.fs.getVariable("DBSN");
		input[2] = this.fs.getVariable("DWSN");
		input[3] = this.fs.getVariable("DBWR");
		input[4] = this.fs.getVariable("DWWR");
		input[5] = this.fs.getVariable("DBTMR");
		input[6] = this.fs.getVariable("DWTMR");

		for(int data_i = 0; data_i < dataSize; data_i++) {
			lines[data_i] = tra.getPattern(data_i);

			if(lines[data_i].getDimValue(2) >= 0) {
				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					input[dim_i].setValue(lines[data_i].getDimValue(dim_i));
				}
			} else {
				input[0].setValue(lines[data_i].getDimValue(0));
				input[1].setValue(lines[data_i].getDimValue(1));
				input[2].setValue(lines[data_i].getDimValue(1));
				input[3].setValue(lines[data_i].getDimValue(3));
				input[4].setValue(1f - lines[data_i].getDimValue(3));
				input[5].setValue(lines[data_i].getDimValue(5));
				input[6].setValue(lines[data_i].getDimValue(5));
			}

			this.fs.evaluate();
			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				//読み込んだデータに対してのメンバシップ値を保持
				memberships[data_i][rule_i] = ((TskVariableType) this.fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
				//現在の結論部の値を保持
				newConcList[rule_i] = ((TskVariableType) this.fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getZ();
			}
		}

		//学習計算開始
		for(int gene_i = 0; gene_i < setting.calcGeneration; gene_i++) {
			for(int data_i = 0; data_i < dataSize; data_i++) {
				memberSum = 0f;
				y[data_i] = 0f;
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					memberSum += memberships[data_i][rule_i];
					y[data_i] += memberships[data_i][rule_i] * newConcList[rule_i];
				}
				y[data_i] /= memberSum;

				//修正量計算
				diff = lines[data_i].getY() - y[data_i];
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					newConcList[rule_i] += setting.eta * diff * memberships[data_i][rule_i] / memberSum;
				}
			}
		}

		//concList更新
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			this.concList[rule_i] = newConcList[rule_i];
		}

		this.makeFS(setting);
	}

	//与えられたdatasetの推論値y[dataset.dataSize]を返すメソッド
	public float[] reasoning(SettingForGA setting, DataSetInfo dataset) {
		int dataSize = dataset.DataSize;
		int Ndim = setting.Ndim;
		float[] y = new float[dataSize];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable output;
		Pattern line;

		input[0] = this.fs.getVariable("MoveNo");
		input[1] = this.fs.getVariable("DBSN");
		input[2] = this.fs.getVariable("DWSN");
		input[3] = this.fs.getVariable("DBWR");
		input[4] = this.fs.getVariable("DWWR");
		input[5] = this.fs.getVariable("DBTMR");
		input[6] = this.fs.getVariable("DWTMR");

		for(int data_i = 0; data_i < dataSize; data_i++) {
			line = dataset.getPattern(data_i);
			if(line.getDimValue(2) >= 0) {
				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					input[dim_i].setValue( line.getDimValue(dim_i) );
				}
			} else {
				input[0].setValue(line.getDimValue(0));
				input[1].setValue(line.getDimValue(1));
				input[2].setValue(line.getDimValue(1));
				input[3].setValue(line.getDimValue(3));
				input[4].setValue(1f - line.getDimValue(3));
				input[5].setValue(line.getDimValue(5));
				input[6].setValue(line.getDimValue(5));
			}

			this.fs.evaluate();

			output = this.fs.getVariable("EBWR");
			y[data_i] = output.getValue();
		}

		return y;
	}

	public void setFitness(float _fitness) {
		this.fitness = _fitness;
	}

	public float getFitness() {
		return this.fitness;
	}

}

































