package main;

import java.io.IOException;

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

public class test {

	public static void main(String[] args) {
		float[] x = new float[100];
		for(int i = 0; i < 100; i++) {
			x[i] = (float)i / 100.0f;
		}

		FuzzyVariableType[] inputVariable = new FuzzyVariableType[6];
		inputVariable[0] = new FuzzyVariableType("Left_v", 0, 1);
		inputVariable[1] = new FuzzyVariableType("Right_v", 0, 1);
		inputVariable[2] = new FuzzyVariableType("One_v", 0, 1);
		inputVariable[3] = new FuzzyVariableType("Two_v", 0, 1);
		inputVariable[4] = new FuzzyVariableType("Three_v", 0, 1);
		inputVariable[5] = new FuzzyVariableType("Four_v", 0, 1);

		FuzzyTermType[] gaussians = new FuzzyTermType[6];
		gaussians[0] = new FuzzyTermType("LEFT", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.21f});
		gaussians[1] = new FuzzyTermType("RIGHT", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.21f});
		gaussians[2] = new FuzzyTermType("One", FuzzyTermType.TYPE_gaussianShape, new float[] {0.66f, 0.1f});
		gaussians[3] = new FuzzyTermType("Two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.72f, 0.1f});
		gaussians[4] = new FuzzyTermType("Three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.78f, 0.1f});
		gaussians[5] = new FuzzyTermType("Four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.84f, 0.1f});

		KnowledgeBaseType kb = new KnowledgeBaseType();

		for(int i = 0; i < 6; i++) {
			inputVariable[i].addFuzzyTerm(gaussians[i]);
			kb.addVariable(inputVariable[i]);
		}

		//RuleBase Initialize
		TskRuleBaseType ruleBase = new TskRuleBaseType();
		ruleBase.setActivationMethod("PROD");
		ruleBase.setAndMethod("PROD");
		//Rule Initialize
		TskFuzzyRuleType[] rules = new TskFuzzyRuleType[6];
		rules[0] = new TskFuzzyRuleType("left", "and", "PROD", 1.0f);
		rules[1] = new TskFuzzyRuleType("right", "and", "PROD", 1.0f);
		rules[2] = new TskFuzzyRuleType("one", "and", "PROD", 1.0f);
		rules[3] = new TskFuzzyRuleType("two", "and", "PROD", 1.0f);
		rules[4] = new TskFuzzyRuleType("three", "and", "PROD", 1.0f);
		rules[5] = new TskFuzzyRuleType("four", "and", "PROD", 1.0f);

		//Antecedent Part Generate
		AntecedentType[] ant = new AntecedentType[6];
		for(int i = 0; i < 6; i++) {
			ant[i] = new AntecedentType();
			ant[i].addClause(new ClauseType(inputVariable[i], gaussians[i]));
			rules[i].setAntecedent(ant[i]);
		}

		//ConsequentPart Generate
		TskConsequentType[] con = new TskConsequentType[6];
		TskVariableType outputVariable = new TskVariableType("output");
		outputVariable.setType("output");
		outputVariable.setCombination("WA");
		kb.addVariable(outputVariable);
		outputVariable.addTskTerm(new TskTermType("Conclusion", 0, new float[] {0.5f}));
		for(int rule_i = 0; rule_i < 6; rule_i++) {
			con[rule_i] = new TskConsequentType();
			con[rule_i].addTskThenClause(outputVariable, (TskTermType)outputVariable.getTerm("Conclusion"));
			rules[rule_i].setTskConsequent(con[rule_i]);
			ruleBase.addTskRule(rules[rule_i]);
		}

		FuzzyInferenceSystem fs = new FuzzyInferenceSystem();
		fs.setKnowledgeBase(kb);
		fs.addRuleBase(ruleBase);




		float[][] memberships = new float[100][6];
		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[6];
		input[0] = fs.getVariable("Left_v");
		input[1] = fs.getVariable("Right_v");
		input[2] = fs.getVariable("One_v");
		input[3] = fs.getVariable("Two_v");
		input[4] = fs.getVariable("Three_v");
		input[5] = fs.getVariable("Four_v");

		for(int i = 0; i < 100; i++) {
			for(int j = 0; j < 6; j++) {
				input[j].setValue(x[i]);
			}

			fs.evaluate();

			for(int j = 0; j < 6; j++) {
				memberships[i][j] = ((TskVariableType) fs.getKnowledgeBase().getVariable("output")).getWZ().get(j).getW();
			}
		}

		try {
			Output.writeGaussian(memberships, "results/test/a.csv");
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}





		System.out.println();
	}

}
