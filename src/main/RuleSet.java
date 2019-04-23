package main;

import java.util.ArrayList;
import java.util.Arrays;

import jfml.FuzzyInferenceSystem;
import jfml.knowledgebase.KnowledgeBaseType;
import jfml.knowledgebase.variable.FuzzyVariableType;
import jfml.knowledgebase.variable.TskVariableType;
import jfml.rule.AntecedentType;
import jfml.rule.ClauseType;
import jfml.rule.TskConsequentType;
import jfml.rule.TskFuzzyRuleType;
import jfml.rulebase.TskRuleBaseType;
import jfml.term.FuzzyTermType;
import jfml.term.TskTermType;

public class RuleSet {
	//Fields ******************************************************
	FuzzyInferenceSystem fs;
	MersenneTwisterFast uniqueRnd;
	public ArrayList<Rule> rules = new ArrayList<Rule>();
	float[] concList;
	float fitness = 100f;

	int[][] count;
	// ************************************************************

	//Constructor *************************************************
	public RuleSet(SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
		this.concList = new float[setting.ruleNum];
		Arrays.fill(this.concList, 0.5f);
	}

	//Deep Copy
	public RuleSet(RuleSet ruleSet, SettingForGA setting) {
		this.rules.clear();
		this.concList = new float[ruleSet.concList.length];
		for(int i = 0; i < ruleSet.rules.size(); i++) {
			this.rules.add(ruleSet.rules.get(i));
		}
		for(int i = 0; i < ruleSet.concList.length; i++) {
			this.concList[i] = ruleSet.concList[i];
		}
		this.fitness = ruleSet.getFitness();
		this.uniqueRnd = new MersenneTwisterFast(ruleSet.uniqueRnd.nextInt());
		makeFS(setting);
	}

	// ************************************************************

	//Methods *****************************************************

	public void countUsedFuzzySet(SettingForGA setting) {
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		this.count = new int[Ndim][Fdiv];

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			Arrays.fill(this.count[dim_i], 0);
			for(int div_i = 0; div_i < Fdiv; div_i++) {

				for(int rule_i = 0; rule_i < this.rules.size(); rule_i++) {
					if(this.rules.get(rule_i).rule[dim_i] == div_i) {
						this.count[dim_i][div_i]++;
					}
				}
			}
		}
	}

	//ruleIdx番目のルールのdim番目の条件部に突然変異操作を行う
	public void micMutation(int ruleIdx, int dim, SettingForGA setting) {
		rules.get(ruleIdx).mutation(dim, setting);
	}

	public void makeFS(SettingForGA setting) {
		int ruleNum = setting.ruleNum;
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		this.fs = new FuzzyInferenceSystem();

		//KnowledgeBase ***********************************
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

		//output variable
		TskVariableType EBWR = new TskVariableType("EBWR");
		EBWR.setType("output");
		EBWR.setCombination("WA");

		FuzzyTermType[] gaussians = new FuzzyTermType[Ndim];
		for(int div_i = 0; div_i < Fdiv; div_i++) {
			inputVariable[0].addFuzzyTerm(setting.MoveNo[div_i]);
			inputVariable[1].addFuzzyTerm(setting.DBSN[div_i]);
			inputVariable[2].addFuzzyTerm(setting.DWSN[div_i]);
			inputVariable[3].addFuzzyTerm(setting.DBWR[div_i]);
			inputVariable[4].addFuzzyTerm(setting.DWWR[div_i]);
			inputVariable[5].addFuzzyTerm(setting.DBTMR[div_i]);
			inputVariable[6].addFuzzyTerm(setting.DWTMR[div_i]);
		}
		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			kb.addVariable(inputVariable[dim_i]);
		}

		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			EBWR.addTskTerm(new TskTermType("Conclusion" + String.valueOf(rule_i), 0, new float[] {this.concList[rule_i]}));
		}
		kb.addVariable(EBWR);

		this.fs.setKnowledgeBase(kb);
		// ***************************************

		//RuleBase *******************************
		TskRuleBaseType ruleBase = new TskRuleBaseType();
		ruleBase.setActivationMethod("PROD");
		ruleBase.setAndMethod("PROD");
		//rule initialize
		TskFuzzyRuleType[] rules = new TskFuzzyRuleType[ruleNum];
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			rules[rule_i] = new TskFuzzyRuleType("rule" + String.valueOf(rule_i), "and", "PROD", 1.0f);
		}
		//Antecedent part generate
		AntecedentType[] ant = new AntecedentType[ruleNum];
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			ant[rule_i] = new AntecedentType();

			ant[rule_i].addClause(new ClauseType(inputVariable[0],
					setting.MoveNo[this.rules.get(rule_i).rule[0]]));
			ant[rule_i].addClause(new ClauseType(inputVariable[1],
					setting.DBSN[this.rules.get(rule_i).rule[1]]));
			ant[rule_i].addClause(new ClauseType(inputVariable[2],
					setting.DWSN[this.rules.get(rule_i).rule[2]]));
			ant[rule_i].addClause(new ClauseType(inputVariable[3],
					setting.DBWR[this.rules.get(rule_i).rule[3]]));
			ant[rule_i].addClause(new ClauseType(inputVariable[4],
					setting.DWWR[this.rules.get(rule_i).rule[4]]));
			ant[rule_i].addClause(new ClauseType(inputVariable[5],
					setting.DBTMR[this.rules.get(rule_i).rule[5]]));
			ant[rule_i].addClause(new ClauseType(inputVariable[6],
					setting.DWTMR[this.rules.get(rule_i).rule[6]]));

			rules[rule_i].setAntecedent(ant[rule_i]);
		}
		//Consequent part generate
		TskConsequentType[] con = new TskConsequentType[ruleNum];
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			con[rule_i] = new TskConsequentType();
			con[rule_i].addTskThenClause(EBWR, EBWR.getTerms().get(rule_i));
			rules[rule_i].setTskConsequent(con[rule_i]);
		}

		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			ruleBase.addTskRule(rules[rule_i]);
		}

		this.fs.addRuleBase(ruleBase);
		// ***************************************


//		//全ルールで違うKnowledgeBaseを作成してしまう
//		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
//			gaussians[0] = new FuzzyTermType("MoveNo" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
//											new float[] {
//											this.rules.get(rule_i).params[0],
//											this.rules.get(rule_i).params[1]});
//
//			gaussians[1] = new FuzzyTermType("DBSN" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
//											new float[] {
//											this.rules.get(rule_i).params[2],
//											this.rules.get(rule_i).params[3]});
//			gaussians[2] = new FuzzyTermType("DWSN" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
//											new float[] {
//											this.rules.get(rule_i).params[4],
//											this.rules.get(rule_i).params[5]});
//			gaussians[3] = new FuzzyTermType("DBWR" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
//											new float[] {
//											this.rules.get(rule_i).params[6],
//											this.rules.get(rule_i).params[7]});
//			gaussians[4] = new FuzzyTermType("DWWR" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
//											new float[] {
//											this.rules.get(rule_i).params[8],
//											this.rules.get(rule_i).params[9]});
//			gaussians[5] = new FuzzyTermType("DBTMR" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
//											new float[] {
//											this.rules.get(rule_i).params[10],
//											this.rules.get(rule_i).params[11]});
//			gaussians[6] = new FuzzyTermType("DWTMR" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
//											new float[] {
//											this.rules.get(rule_i).params[12],
//											this.rules.get(rule_i).params[13]});
//
//			for(int v_i = 0; v_i < inputVariable.length; v_i++) {
//				inputVariable[v_i].addFuzzyTerm(gaussians[v_i]);
//				kb.addVariable(inputVariable[v_i]);
//			}
//		}


	}

	public void setConcList(float[] newConcList) {
		for(int i = 0; i < newConcList.length; i++) {
			this.concList[i] = newConcList[i];
		}
	}

	public void setFitness(float _fitness) {
		this.fitness = _fitness;
	}

	public float getFitness() {
		return this.fitness;
	}


	// ************************************************************

}
