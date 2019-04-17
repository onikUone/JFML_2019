package main;

import java.util.ArrayList;

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

public class PopulationManager {

	//Fields ******************************************************
	MersenneTwisterFast uniqueRnd;

	//個体群
	int popSize;
	public ArrayList<Population> currentPops = new ArrayList<Population>();
	public ArrayList<Population> newPops = new ArrayList<Population>();
	public ArrayList<Population> margePops = new ArrayList<Population>();
	Population all;

	//データセット情報
	int Ndim;
	int traDataSize;
	int tstDataSize;

	int ruleNum;

	// ************************************************************

	//Constructor *************************************************
	public PopulationManager(MersenneTwisterFast rnd, DataSetInfo tra, DataSetInfo tst, SettingForFML setting) {
		this.uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		this.popSize = setting.popSize;
		this.Ndim = tra.getNdim();
		this.traDataSize = tra.getDataSize();
		this.tstDataSize = tst.getDataSize();
		this.ruleNum = setting.ruleNum;
	}

	// ************************************************************

	//Methods *****************************************************

	//初期個体群生成
	public void generateInitialPopulation(SettingForFML setting) {

		this.currentPops.clear();

		//popSizeの個体インスタンス生成
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			this.currentPops.add(new Population(this.uniqueRnd, this.Ndim, setting));
			this.currentPops.get(pop_i).getFS().setKnowledgeBase(setting.getKnowledgeBase());
		}




	}

	//全ルール探索識別器生成
	public void generateAllRuleFS(int[][] setRule, float[] concList, SettingForFML setting) {

		this.currentPops.clear();
		int ruleNum = setRule.length;
//		for(int pop_i = 0; pop_i < ruleNum; pop_i++) {
//			currentPops.add(new Population(this.uniqueRnd, this.Ndim, setting) );
//		}

		this.all = new Population(this.uniqueRnd, setting.Ndim, setting);

		//FMLのFuzzyInferenceSystem 生成
		FuzzyInferenceSystem fs = new FuzzyInferenceSystem();

		KnowledgeBaseType kb = new KnowledgeBaseType();
		FuzzyVariableType[] inputVariable = new FuzzyVariableType[setting.Ndim];
		inputVariable[0] = new FuzzyVariableType("Move", 0, 1);
		inputVariable[1] = new FuzzyVariableType("DBSN", 0, 1);
		inputVariable[2] = new FuzzyVariableType("DWSN", 0, 1);
		inputVariable[3] = new FuzzyVariableType("DBWR", 0, 1);
		inputVariable[4] = new FuzzyVariableType("DWWR", 0, 1);
		inputVariable[5] = new FuzzyVariableType("DBTMR", 0, 1);
		inputVariable[6] = new FuzzyVariableType("DWTMR", 0, 1);
		FuzzyTermType[] gaussians = new FuzzyTermType[5];
		gaussians[0] = new FuzzyTermType("SS_G", FuzzyTermType.TYPE_rightGaussianShape, new float[] {0f, 0.105f});
		gaussians[1] = new FuzzyTermType("S_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.25f, 0.105f});
//		gaussians[1] = new FuzzyTermType("S_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.25f, 0.0625f});
		gaussians[2] = new FuzzyTermType("M_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.5f, 0.105f});
		gaussians[3] = new FuzzyTermType("L_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.75f, 0.105f});
//		gaissoams[3] = new FuzzyTermType("L_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.75f, 0.0625f});
		gaussians[4] = new FuzzyTermType("LL_G", FuzzyTermType.TYPE_leftGaussianShape, new float[] {1f, 0.105f});

//		FuzzyTermType[] gaussians = new FuzzyTermType[5];
//		gaussians[0] = new FuzzyTermType("don't care", FuzzyTermType.TYPE_rectangularShape, new float[] {0f, 1f});
//		gaussians[1] = new FuzzyTermType("One", FuzzyTermType.TYPE_triangularShape, new float[] {0f, 0f, 0.375f});
//		gaussians[2] = new FuzzyTermType("Two", FuzzyTermType.TYPE_triangularShape, new float[] {0f, 0.375f, 0.625f});
//		gaussians[3] = new FuzzyTermType("Three", FuzzyTermType.TYPE_triangularShape, new float[] {0.375f, 0.625f, 1f});
//		gaussians[4] = new FuzzyTermType("Four", FuzzyTermType.TYPE_triangularShape, new float[] {0.625f, 1f, 1f});

//		FuzzyTermType[] gaussians = new FuzzyTermType[6];
//		gaussians[0] = new FuzzyTermType("don't care", FuzzyTermType.TYPE_rectangularShape, new float[] {0f, 1f});
//		gaussians[1] = new FuzzyTermType("SS", FuzzyTermType.TYPE_triangularShape, new float[] {0f, 0f, 0.25f});
//		gaussians[2] = new FuzzyTermType("S", FuzzyTermType.TYPE_triangularShape, new float[] {0f, 0.25f, 0.5f});
//		gaussians[3] = new FuzzyTermType("M", FuzzyTermType.TYPE_triangularShape, new float[] {0.25f, 0.5f, 0.75f});
//		gaussians[4] = new FuzzyTermType("L", FuzzyTermType.TYPE_triangularShape, new float[] {0.5f, 0.75f, 1f});
//		gaussians[5] = new FuzzyTermType("LL", FuzzyTermType.TYPE_triangularShape, new float[] {0.75f, 1f, 1f});

//		FuzzyTermType[] gaussians = new FuzzyTermType[5];
//		gaussians[0] = new FuzzyTermType("SS", FuzzyTermType.TYPE_triangularShape, new float[] {0f, 0f, 0.25f});
//		gaussians[1] = new FuzzyTermType("S", FuzzyTermType.TYPE_triangularShape, new float[] {0f, 0.25f, 0.5f});
//		gaussians[2] = new FuzzyTermType("M", FuzzyTermType.TYPE_triangularShape, new float[] {0.25f, 0.5f, 0.75f});
//		gaussians[3] = new FuzzyTermType("L", FuzzyTermType.TYPE_triangularShape, new float[] {0.5f, 0.75f, 1f});
//		gaussians[4] = new FuzzyTermType("LL", FuzzyTermType.TYPE_triangularShape, new float[] {0.75f, 1f, 1f});

//		FuzzyTermType[] gaussians = new FuzzyTermType[3];
//		gaussians = new FuzzyTermType[3];
//		gaussians[0] = new FuzzyTermType("small", FuzzyTermType.TYPE_triangularShape, new float[] {0f, 0f, 0.5f});
//		gaussians[1] = new FuzzyTermType("medium", FuzzyTermType.TYPE_triangularShape, new float[] {0f, 0.5f, 1f});
//		gaussians[2] = new FuzzyTermType("large", FuzzyTermType.TYPE_triangularShape, new float[] {0.5f, 1f, 1f});


		for(int i = 0; i < setting.Ndim; i++) {
			for(int j = 0; j < setting.Fdiv; j++) {
				inputVariable[i].addFuzzyTerm(gaussians[j]);
			}
		}
		for(int i = 0; i < setting.Ndim; i++) {
			kb.addVariable(inputVariable[i]);
		}
		TskVariableType EBWR = new TskVariableType("EBWR");
		EBWR.setType("output");
		EBWR.setCombination("WA");
		for(int i = 0; i < concList.length; i++) {
			EBWR.addTskTerm(new TskTermType("Conclusion" + String.valueOf(i), 0, new float[] {concList[i]}));
		}
		kb.addVariable(EBWR);

		fs.setKnowledgeBase(kb);

		//RuleBase Initialize
		TskRuleBaseType ruleBase = new TskRuleBaseType();
		ruleBase.setActivationMethod("PROD");
		ruleBase.setAndMethod("PROD");
		//Rule Initialize
		TskFuzzyRuleType[] rules = new TskFuzzyRuleType[ruleNum];
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			rules[rule_i] = new TskFuzzyRuleType("rule" + String.valueOf(rule_i), "and", "PROD", 1.0f);
		}
		//Antecedent Part Generate
		AntecedentType[] ant = new AntecedentType[ruleNum];
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			ant[rule_i] = new AntecedentType();
			for(int dim_i = 0; dim_i < setting.Ndim; dim_i++) {
				ant[rule_i].addClause( new ClauseType(inputVariable[dim_i], gaussians[setRule[rule_i][dim_i]]) );
			}
			rules[rule_i].setAntecedent(ant[rule_i]);
		}
		//ConsequentPart Generate
		TskConsequentType[] con = new TskConsequentType[ruleNum];
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			con[rule_i] = new TskConsequentType();
			con[rule_i].addTskThenClause(EBWR, EBWR.getTerms().get(rule_i));
			rules[rule_i].setTskConsequent(con[rule_i]);
		}
		//RuleBase add Rules
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			ruleBase.addTskRule(rules[rule_i]);
		}

		fs.addRuleBase(ruleBase);

		this.all.setFS(fs);

	}

	public void renewConclusion(float[] concList, PopulationManager popManager) {
		popManager.all.getFS().getKnowledgeBase().getVariable("EBWR").getTerms().clear();
		for(int rule_i = 0; rule_i < concList.length; rule_i++) {
			((TskVariableType)popManager.all.getFS().getKnowledgeBase().getVariable("EBWR")).addTskTerm(new TskTermType("Conclusion" + String.valueOf(rule_i), 0, new float[] {concList[rule_i]}));
		}
	}


	// ************************************************************
}































