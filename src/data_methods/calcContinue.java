package data_methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import jfml.FuzzyInferenceSystem;
import jfml.JFML;
import jfml.knowledgebase.KnowledgeBaseType;
import jfml.knowledgebase.variable.FuzzyVariableType;
import jfml.knowledgebase.variable.KnowledgeBaseVariable;
import jfml.knowledgebase.variable.TskVariableType;
import jfml.rule.AntecedentType;
import jfml.rule.TskConsequentType;
import jfml.rule.TskFuzzyRuleType;
import jfml.rulebase.TskRuleBaseType;
import jfml.term.FuzzyTermType;
import jfml.term.TskTermType;
import main.DataSetInfo;
import main.Pattern;
import main.SettingForGA;

public class calcContinue {

	public static void main(String[] args) {
		String inputFML = args[0];
		System.out.println("input: " + inputFML + ".xml");

		int continueGeneration = Integer.parseInt(args[1]);

		File xml = new File("XML/" + inputFML + ".xml");
		FuzzyInferenceSystem fs = JFML.load(xml);

		String folderName = "XML/" + inputFML;
		File newdir = new File(folderName);
		newdir.mkdirs();
		newdir = new File(folderName + "/MSE");
		newdir.mkdirs();

		int Ndim = fs.getKnowledgeBase().getVariables().size() - 1;	//EBWR分マイナス

		String traFileName = null;
		if(Ndim == 7) {
			traFileName = "DataSets/traData_raw_2.csv";	//2は45Game分全て含む学習データ
		} else if(Ndim == 6) {
			traFileName = "DataSets/traData_raw_Ndim6.csv";	//2は45Game分全て含む学習データ
		}
		String tstFileName = "DataSets/tstData_raw_2.csv";
		DataSetInfo tra = new DataSetInfo(traFileName);
		DataSetInfo tst = new DataSetInfo(tstFileName);

		SettingForGA setting = new SettingForGA(tra);

		float[] newConcList = calcConclusion(setting, fs, continueGeneration, tra, tst, folderName, inputFML);

//		FuzzyInferenceSystem newFS = makeFS(setting, fs, newConcList);


//		//FML出力
//		String fileName = folderName + "/after" + String.valueOf(continueGeneration) + "_" + inputFML;
//		File outputXML = new File(fileName);
//		JFML.writeFSTtoXML(newFS, outputXML);

	}

	public static void outputXML(String fileName, FuzzyInferenceSystem fs) {
		File outputXML = new File(fileName);
		JFML.writeFSTtoXML(fs, outputXML);
	}

	public static FuzzyInferenceSystem makeFS(SettingForGA setting, FuzzyInferenceSystem oldFS, float[] newConcList) {

		KnowledgeBaseType kb = oldFS.getKnowledgeBase();	//knowledgeBaseの保存
		TskRuleBaseType ruleBase = (TskRuleBaseType) oldFS.getRuleBase(0);

		FuzzyInferenceSystem newFS = new FuzzyInferenceSystem();

		int Ndim = kb.getVariables().size() - 1;	//EBWRがマイナス1
		int Fdiv = kb.getVariable("DBSN").getTerms().size() - 1;	//don't careがマイナス1
		int ruleNum = kb.getVariable("EBWR").getTerms().size();

		//KnowledgeBase ***********************************************
		KnowledgeBaseType newKB = new KnowledgeBaseType();

		//inputVariable
		FuzzyVariableType[] inputVariable = new FuzzyVariableType[Ndim];

		String[] dimName = new String[Ndim];
		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			dimName[dim_i] = ((FuzzyVariableType)kb.getKnowledgeBaseVariables().get(dim_i)).getName();
			inputVariable[dim_i] = new FuzzyVariableType(dimName[dim_i], 0, 1);
		}

		//outputVariable
		TskVariableType EBWR = new TskVariableType("EBWR");
		EBWR.setType("output");
		EBWR.setCombination("WA");

		//Fuzzy Set for Input Variable (= FuzzyTerm)
		FuzzyTermType[][] gaussians = new FuzzyTermType[Ndim][Fdiv];
		String[] name = new String[Fdiv];

		for(int div_i = 0; div_i < Fdiv; div_i++) {
			name[div_i] = ((FuzzyTermType)kb.getVariable("DBSN").getTerms().get(div_i)).getName();
		}

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				gaussians[dim_i][div_i] = (FuzzyTermType) kb.getVariable(dimName[dim_i]).getTerm(name[div_i]);
				inputVariable[dim_i].addFuzzyTerm(gaussians[dim_i][div_i]);
			}
			newKB.addVariable(inputVariable[dim_i]);
		}

		//Fuzzy Set for Output Variable
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			EBWR.addTskTerm( new TskTermType("Conclusion" + String.valueOf(rule_i), 0, new float[] {newConcList[rule_i]} ) );
		}
		newKB.addVariable(EBWR);

		newFS.setKnowledgeBase(newKB);
		// ************************************************************

		//RuleBase ****************************************************
		TskRuleBaseType newRuleBase = new TskRuleBaseType();
		newRuleBase.setActivationMethod("PROD");
		newRuleBase.setAndMethod("PROD");
		//rule initialize
		TskFuzzyRuleType rule;
		AntecedentType ant;
		TskConsequentType con;
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			ant = ruleBase.getTskRules().get(rule_i).getAntecedent();
			con = new TskConsequentType();
			con.addTskThenClause(EBWR, EBWR.getTerms().get(rule_i));

			rule = new TskFuzzyRuleType("rule" + String.valueOf(rule_i), "and", "PROD", 1.0f);
			rule.setAntecedent(ant);
			rule.setTskConsequent(con);

			newRuleBase.addTskRule(rule);
		}
		newFS.addRuleBase(newRuleBase);
		// ************************************************************

		return newFS;
	}

	public static float[] calcConclusion(SettingForGA setting, FuzzyInferenceSystem fs, int continueGeneration, DataSetInfo tra, DataSetInfo tst, String folderName, String inputFML) {
		int dataSize = tra.getDataSize();
		int Ndim = fs.getKnowledgeBase().getVariables().size() - 1;	//EBWR分マイナス
		int ruleNum = fs.getKnowledgeBase().getVariable("EBWR").getTerms().size();

		Pattern[] lines = new Pattern[dataSize];
		float[] y = new float[dataSize];
		float diff;
		float memberSum;
		float[][] memberships = new float[dataSize][ruleNum];
		float[] newConcList = new float[ruleNum];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable output;

		if(Ndim == 7) {
			input[0] = fs.getVariable("MoveNo");
			input[1] = fs.getVariable("DBSN");
			input[2] = fs.getVariable("DWSN");
			input[3] = fs.getVariable("DBWR");
			input[4] = fs.getVariable("DWWR");
			input[5] = fs.getVariable("DBTMR");
			input[6] = fs.getVariable("DWTMR");
		} else if(Ndim == 6) {
			//Ndim = 6 , MoveNo無しversion
			input[0] = fs.getVariable("DBSN");
			input[1] = fs.getVariable("DWSN");
			input[2] = fs.getVariable("DBWR");
			input[3] = fs.getVariable("DWWR");
			input[4] = fs.getVariable("DBTMR");
			input[5] = fs.getVariable("DWTMR");
		}

		for(int data_i = 0; data_i < dataSize; data_i++) {
			lines[data_i] = tra.getPattern(data_i);

			if(Ndim == 7) {
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
			} else if(Ndim == 6) {
				if(lines[data_i].getDimValue(1) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue(lines[data_i].getDimValue(dim_i));
					}
				} else {
					//Ndim = 6 , MoveNo無しversion
					input[0].setValue(lines[data_i].getDimValue(0));
					input[1].setValue(lines[data_i].getDimValue(0));
					input[2].setValue(lines[data_i].getDimValue(2));
					input[3].setValue(1f - lines[data_i].getDimValue(2));
					input[4].setValue(lines[data_i].getDimValue(4));
					input[5].setValue(lines[data_i].getDimValue(4));
				}
			}

			fs.evaluate();
			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				//読み込んだデータに対してのメンバシップ値を保持
				memberships[data_i][rule_i] = ((TskVariableType) fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
				//現在の結論部の値を保持
				newConcList[rule_i] = ((TskVariableType) fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getZ();
			}
		}

		//学習計算開始
		for(int gene_i = 0; gene_i < continueGeneration; gene_i++) {
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

			if(gene_i % 100 == 0) {
				FuzzyInferenceSystem newFS = makeFS(setting, fs, newConcList);
				String fileName = folderName + "/after" + String.valueOf(gene_i) + "_" + inputFML + ".xml";
				outputXML(fileName, newFS);
				outputMSE(newFS, folderName + "/MSE/gene" + String.valueOf(gene_i) + ".csv", tra, tst);
			}

		}

		FuzzyInferenceSystem newFS = makeFS(setting, fs, newConcList);
		String fileName = folderName + "/after" + String.valueOf(continueGeneration) + "_" + inputFML + ".xml";
		outputXML(fileName, newFS);
		outputMSE(newFS, folderName + "/MSE/gene" + String.valueOf(continueGeneration) + ".csv", tra, tst);

		return newConcList;
	}

	public static float[] reasoning(FuzzyInferenceSystem fs, DataSetInfo dataset) {
		int dataSize = dataset.getDataSize();
		float[] y = new float[dataSize];
		int Ndim = fs.getKnowledgeBase().getVariables().size() - 1;	//EBWR分マイナス
		int ruleNum = fs.getKnowledgeBase().getVariable("EBWR").getTerms().size();

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable output;
		Pattern line;


		String[] dimName = new String[Ndim];
		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			dimName[dim_i] = ((FuzzyVariableType)fs.getKnowledgeBase().getKnowledgeBaseVariables().get(dim_i)).getName();
			input[dim_i] = fs.getVariable(dimName[dim_i]);
		}

		for(int data_i = 0; data_i < dataSize; data_i++) {
			line = dataset.getPattern(data_i);

			if(Ndim == 7) {
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
			} else if(Ndim == 6) {
				if(line.getDimValue(1) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue( line.getDimValue(dim_i) );
					}
				} else {
					//Ndim = 6 , MoveNo無しversion
					input[0].setValue(line.getDimValue(0));
					input[1].setValue(line.getDimValue(0));
					input[2].setValue(line.getDimValue(2));
					input[3].setValue(1f - line.getDimValue(2));
					input[4].setValue(line.getDimValue(4));
					input[5].setValue(line.getDimValue(4));
				}
			}

			fs.evaluate();

			output = fs.getVariable("EBWR");
			y[data_i] = output.getValue();
		}

		return y;
	}

	public static void outputMSE(FuzzyInferenceSystem fs, String fileName, DataSetInfo tra, DataSetInfo tst) {

		float[] yTra = reasoning(fs, tra);
		float[] yTst = reasoning(fs, tst);

		float traMSE = main.FmlGaManager.calcMSE(yTra, tra);
		float tstMSE = main.FmlGaManager.calcMSE(yTst, tst);

		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			pw.println(traMSE + "," + tstMSE);

			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}













