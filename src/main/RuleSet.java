package main;

import java.util.ArrayList;

import jfml.FuzzyInferenceSystem;
import jfml.knowledgebase.KnowledgeBaseType;
import jfml.knowledgebase.variable.FuzzyVariableType;
import jfml.knowledgebase.variable.TskVariableType;
import jfml.term.FuzzyTermType;

public class RuleSet {
	//Fields ******************************************************
	FuzzyInferenceSystem fs;
	MersenneTwisterFast uniqueRnd;
	public ArrayList<Rule> rules = new ArrayList<Rule>();
	// ************************************************************

	//Constructor *************************************************
	public RuleSet(SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
	}

	// ************************************************************

	//Methods *****************************************************

	public void makeFS(SettingForGA setting) {
		int ruleNum = setting.ruleNum;
		int Ndim = setting.Ndim;

		this.fs = new FuzzyInferenceSystem();
		KnowledgeBaseType kb = new KnowledgeBaseType();

		//inputVariable
		FuzzyVariableType[] inputVariable = new FuzzyVariableType[Ndim];
		inputVariable[0] = new FuzzyVariableType("Move", 0, 1);
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
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			gaussians[0] = new FuzzyTermType("MoveNo" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
											new float[] {
											this.rules.get(rule_i).params[0],
											this.rules.get(rule_i).params[1]});

			gaussians[1] = new FuzzyTermType("DBSN" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
											new float[] {
											this.rules.get(rule_i).params[2],
											this.rules.get(rule_i).params[3]});
			gaussians[2] = new FuzzyTermType("DWSN" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
											new float[] {
											this.rules.get(rule_i).params[4],
											this.rules.get(rule_i).params[5]});
			gaussians[3] = new FuzzyTermType("DBWR" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
											new float[] {
											this.rules.get(rule_i).params[6],
											this.rules.get(rule_i).params[7]});
			gaussians[4] = new FuzzyTermType("DWWR" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
											new float[] {
											this.rules.get(rule_i).params[8],
											this.rules.get(rule_i).params[9]});
			gaussians[5] = new FuzzyTermType("DBTMR" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
											new float[] {
											this.rules.get(rule_i).params[10],
											this.rules.get(rule_i).params[11]});
			gaussians[6] = new FuzzyTermType("DWTMR" + String.valueOf(rule_i), FuzzyTermType.TYPE_gaussianShape,
											new float[] {
											this.rules.get(rule_i).params[12],
											this.rules.get(rule_i).params[13]});

			for(int v_i = 0; v_i < inputVariable.length; v_i++) {
				inputVariable[v_i].addFuzzyTerm(gaussians[v_i]);
				kb.addVariable(inputVariable[v_i]);
			}
		}

		//TODO ruleNumだけconclusion clauseをEBWRに追加
		//TODO ruleNumのルールを持つruleBaseを追加

	}

	// ************************************************************

}
