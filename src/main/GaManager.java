package main;

import java.util.Date;
import java.util.concurrent.ForkJoinPool;

import jfml.FuzzyInferenceSystem;
import jfml.knowledgebase.variable.KnowledgeBaseVariable;

public class GaManager {

	//Fields ********************************************
	ForkJoinPool forkJoinPool;
	// **************************************************

	//Constructor ***************************************
	public GaManager() {

	}
	// **************************************************

	//Methods *******************************************

	public void gaFrame(SettingForGA setting, PopulationManager popManager, DataSetInfo tra, DataSetInfo tst, DataSetInfo eva) {
		Date start = new Date();
		System.out.println(start);


		//初期個体群生成
		popManager.currentPops.clear();
		popManager.generateInitialPopulation(setting);
		popManager.makeFML(setting);	//各currentPopsのfsを生成

//		//MSEの学習推移見る用
//		popManager.calcConclusion2(setting, tra, tst, setting.calcGeneration);	//結論部学習
//		popManager.outputCurrentFML("calcGene" + String.valueOf(setting.calcGeneration) + "/FML", 0, setting);	//FML出力
//		popManager.outputCurrentCount("calcGene" + String.valueOf(setting.calcGeneration) + "/Count", 0, setting);

		popManager.calcConclusion(setting, tra, setting.calcGeneration);	//結論部学習
		popManager.outputCurrentFML("gene_0_initial/FML", 0, setting);	//FML出力
		popManager.outputCurrentMSE("gene_0_initial/MSE", 0, tst, setting);	//MSE出力
		popManager.outputCurrentCount("gene_0_initial/Count", 0, setting);
		float[] evaMSE = popManager.calcMSE(eva, setting);
		popManager.setEvaAsFitness(evaMSE);

		for(int gene_i = 0; gene_i < setting.gaGeneration; gene_i++) {
			System.out.print(".");
			//1. 子個体生成
			popManager.newPops.clear();
			popManager.crossOverAndMichiganOpe(setting);
			popManager.newPopsMutation(setting);
			popManager.calcConclusionForChild(setting, tra, setting.calcGeneration);

			//2. 個体評価 = evaのMSE計算
			evaMSE = popManager.calcMseForChild(eva, setting);
			popManager.setEvaAsFitnessForChild(evaMSE);

			//3. 世代更新
			popManager.populationUpdate(setting);

			//4. 現世代FML出力
			String dir = "gene_" + String.valueOf(gene_i+1);
			popManager.outputCurrentFML(dir + "/FML", gene_i+1, setting);
			popManager.outputCurrentMSE(dir + "/MSE", gene_i+1, tst, setting);
			popManager.outputCurrentCount(dir + "/Count", gene_i+1, setting);
		}

		Date end = new Date();
		System.out.println(end);
		System.out.println("");
	}


//	public void gaFrame(SettingForFML setting, PopulationManager popManager, int[][] setRule, float[] concList, DataSetInfo tra, DataSetInfo tst, ResultMaster resultMaster) {
////		//初期個体群生成
////		popManager.generateInitialPopulation(setting);
//
//		//初期結論部リスト0.5 FML生成
//		concList = new float[setRule.length];
//		Arrays.fill(concList, 0.5f);
//		popManager.generateAllRuleFS(setRule, concList, setting);
//
//		int ruleNum = setRule.length;
//		float[][] memberships = makeMemberships(tra, popManager, ruleNum);
//
//		float[] newConcList = concList;
//		for(int i = 0; i < 10000; i++) {
//			if(i % 10 == 0) {
//				System.out.println(String.valueOf(i * 10));
//			}
//			System.out.print(".");
//			newConcList = calcConclusion(tra, memberships, newConcList, 10, setting);
//			popManager.generateAllRuleFS(setRule, newConcList, setting);
//
//			float[] yTra = reasoning(tra, popManager, setting);
//			float[] yTst = reasoning(tst, popManager, setting);
//			resultMaster.calcMSE(tra, tst, yTra, yTst);
//			//推論値リスト保持用
//			try {
//				int nowGene = (i+1)*10;
//				resultMaster.writeY(yTra, yTst, nowGene);
//				resultMaster.writeMSE(nowGene);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//
//		int generation = setting.generation;
//		for(int gene_i = 0; gene_i < generation; gene_i++) {
//			if(gene_i % 10 == 0) {
//				System.out.print(".");
//			}
//
//			linearLearning(tra, setRule, popManager, setting);
//
//			//100世代ごとに結果出力
//			if(gene_i % 1 == 0) {
//				float[] yTra = reasoning(tra, popManager, setting);
//				float[] yTst = reasoning(tst, popManager, setting);
//				resultMaster.calcMSE(tra, tst, yTra, yTst);
//				//推論値リスト保持用
//				try {
//					resultMaster.writeY(yTra, yTst, gene_i);
//					resultMaster.writeMSE(gene_i);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//				System.out.println();
//			}
//		}
//	}

	public float[] calcConclusion(DataSetInfo tra, float[][] memberships, float[] preConcList, int generation, SettingForFML setting) {
		int dataSize = memberships.length;
		int ruleNum = memberships[0].length;
		Pattern[] line = new Pattern[dataSize];
		float memberSum;
		float y;
		float diff;
		float[] newConcList = preConcList;

		for(int data_i = 0; data_i < dataSize; data_i++) {
			line[data_i] = tra.getPattern(data_i);
		}

		for(int gene_i = 0; gene_i < generation; gene_i++) {
			for(int data_i = 0; data_i < dataSize; data_i++) {
				memberSum = 0f;
				y = 0f;
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					memberSum += memberships[data_i][rule_i];
					y += memberships[data_i][rule_i] * newConcList[rule_i];
				}
				y /= memberSum;
				diff = line[data_i].getY() - y;
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					newConcList[rule_i] = newConcList[rule_i] + setting.eta * diff * memberships[data_i][rule_i] / memberSum;
				}
			}
		}
		return newConcList;
	}

//	public float[][] makeMemberships(DataSetInfo tra, PopulationManager popManager, int ruleNum) {
//		Pattern line;
//		float[][] memberships = new float[tra.getDataSize()][ruleNum];
//		KnowledgeBaseVariable[] inputVariables = new KnowledgeBaseVariable[tra.getNdim()];
//		for(int data_i = 0; data_i < tra.getDataSize(); data_i++) {
//			line = tra.getPattern(data_i);
//
//			inputVariables[0] = popManager.all.fs.getVariable("Move");
//			inputVariables[1] = popManager.all.fs.getVariable("DBSN");
//			inputVariables[2] = popManager.all.fs.getVariable("DWSN");
//			inputVariables[3] = popManager.all.fs.getVariable("DBWR");
//			inputVariables[4] = popManager.all.fs.getVariable("DWWR");
//			inputVariables[5] = popManager.all.fs.getVariable("DBTMR");
//			inputVariables[6] = popManager.all.fs.getVariable("DWTMR");
//
//			if(line.getDimValue(2) >= 0) {
//				inputVariables[0].setValue((float)line.getDimValue(0));
//				inputVariables[1].setValue((float)line.getDimValue(1));
//				inputVariables[2].setValue((float)line.getDimValue(2));
//				inputVariables[3].setValue((float)line.getDimValue(3));
//				inputVariables[4].setValue((float)line.getDimValue(4));
//				inputVariables[5].setValue((float)line.getDimValue(5));
//				inputVariables[6].setValue((float)line.getDimValue(6));
//			} else {
//				inputVariables[0].setValue((float)line.getDimValue(0));
//				inputVariables[1].setValue((float)line.getDimValue(1));
//				inputVariables[2].setValue((float)line.getDimValue(1));
//				inputVariables[3].setValue((float)line.getDimValue(3));
//				inputVariables[4].setValue(1f - (float)line.getDimValue(3));
//				inputVariables[5].setValue((float)line.getDimValue(5));
//				inputVariables[6].setValue((float)line.getDimValue(5));
//			}
//			popManager.all.fs.evaluate();
//			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
//				memberships[data_i][rule_i] = ((TskVariableType) popManager.all.getFS().getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
//			}
//		}
//		return memberships;
//	}
//
//	//毎回学習用
//	public void linearLearning(DataSetInfo tra, int[][] setRule, PopulationManager popManager, SettingForFML setting) {
//		Pattern line;
//		int ruleNum = setRule.length;
//		float[] memberships = new float[ruleNum];
//		float memberSum;
//		float y;
//		float diff;
//		float[] newConcList = new float[ruleNum];
//
//		KnowledgeBaseVariable[] inputVariables = new KnowledgeBaseVariable[setting.Ndim];
//		KnowledgeBaseVariable out;
//
//		//全パターンに対して学習
//		for(int data_i = 0; data_i < tra.getPatterns().size(); data_i++) {
//			memberSum = 0;
//			line = tra.getPattern(data_i);
//
//			inputVariables[0] = popManager.all.fs.getVariable("Move");
//			inputVariables[1] = popManager.all.fs.getVariable("DBSN");
//			inputVariables[2] = popManager.all.fs.getVariable("DWSN");
//			inputVariables[3] = popManager.all.fs.getVariable("DBWR");
//			inputVariables[4] = popManager.all.fs.getVariable("DWWR");
//			inputVariables[5] = popManager.all.fs.getVariable("DBTMR");
//			inputVariables[6] = popManager.all.fs.getVariable("DWTMR");
//
//			if(line.getDimValue(2) >= 0) {
//				inputVariables[0].setValue((float)line.getDimValue(0));
//				inputVariables[1].setValue((float)line.getDimValue(1));
//				inputVariables[2].setValue((float)line.getDimValue(2));
//				inputVariables[3].setValue((float)line.getDimValue(3));
//				inputVariables[4].setValue((float)line.getDimValue(4));
//				inputVariables[5].setValue((float)line.getDimValue(5));
//				inputVariables[6].setValue((float)line.getDimValue(6));
//			} else {
//				inputVariables[0].setValue((float)line.getDimValue(0));
//				inputVariables[1].setValue((float)line.getDimValue(1));
//				inputVariables[2].setValue((float)line.getDimValue(1));
//				inputVariables[3].setValue((float)line.getDimValue(3));
//				inputVariables[4].setValue(1f - (float)line.getDimValue(3));
//				inputVariables[5].setValue((float)line.getDimValue(5));
//				inputVariables[6].setValue((float)line.getDimValue(5));
//			}
//
//			popManager.all.fs.evaluate();
//			out = popManager.all.fs.getVariable("EBWR");
//			y = out.getValue();
//
//			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
//				memberships[rule_i] = ((TskVariableType) popManager.all.getFS().getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
//				memberSum += memberships[rule_i];
//			}
//
//			//修正値計算
//			diff = (float)line.getY() - y;
//			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
//				TskTermType term = (TskTermType) popManager.all.getFS().getKnowledgeBase()
//						.getVariable("EBWR").getTerm("Conclusion" + String.valueOf(rule_i));
//				float conc = term.getTskValue().get(0);
//				newConcList[rule_i] = conc + setting.eta * diff * memberships[rule_i] / memberSum;
//			}
//
//			popManager.generateAllRuleFS(setRule, newConcList, setting);	//結論部更新（新しいFS生成）
//		}
//
//	}
//
	public static float[] reasoning(DataSetInfo dataset, FuzzyInferenceSystem fs, SettingForGA setting) {
		Pattern line;
		float[] y = new float[dataset.getDataSize()];

		KnowledgeBaseVariable[] inputVariables = new KnowledgeBaseVariable[setting.Ndim];
		KnowledgeBaseVariable out;

		for(int data_i = 0; data_i < dataset.getPatterns().size(); data_i++) {
			line = dataset.getPattern(data_i);

			inputVariables[0] = fs.getVariable("MoveNo");
			inputVariables[1] = fs.getVariable("DBSN");
			inputVariables[2] = fs.getVariable("DWSN");
			inputVariables[3] = fs.getVariable("DBWR");
			inputVariables[4] = fs.getVariable("DWWR");
			inputVariables[5] = fs.getVariable("DBTMR");
			inputVariables[6] = fs.getVariable("DWTMR");

			if(line.getDimValue(2) >= 0) {
				inputVariables[0].setValue((float)line.getDimValue(0));
				inputVariables[1].setValue((float)line.getDimValue(1));
				inputVariables[2].setValue((float)line.getDimValue(2));
				inputVariables[3].setValue((float)line.getDimValue(3));
				inputVariables[4].setValue((float)line.getDimValue(4));
				inputVariables[5].setValue((float)line.getDimValue(5));
				inputVariables[6].setValue((float)line.getDimValue(6));
			} else {	//欠損値
				inputVariables[0].setValue((float)line.getDimValue(0));
				inputVariables[1].setValue((float)line.getDimValue(1));
				inputVariables[2].setValue((float)line.getDimValue(1));
				inputVariables[3].setValue((float)line.getDimValue(3));
				inputVariables[4].setValue(1f - (float)line.getDimValue(3));
				inputVariables[5].setValue((float)line.getDimValue(5));
				inputVariables[6].setValue((float)line.getDimValue(5));
			}

			fs.evaluate();
			out = fs.getVariable("EBWR");
			y[data_i] = out.getValue();
		}

		return y;
	}

	// **************************************************
}
