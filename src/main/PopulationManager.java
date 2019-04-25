package main;

import static java.util.Comparator.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import jfml.FuzzyInferenceSystem;
import jfml.JFML;
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

public class PopulationManager implements Serializable{

	//Fields ******************************************************
	MersenneTwisterFast uniqueRnd;

	//個体群
	int popSize;
	public ArrayList<RuleSet> currentPops = new ArrayList<RuleSet>();
	public ArrayList<RuleSet> newPops = new ArrayList<RuleSet>();
	public ArrayList<RuleSet> margePops = new ArrayList<RuleSet>();
	Rule all;

	//データセット情報
	int Ndim;
	int traDataSize;
	int evaDataSize;
	int tstDataSize;

	int ruleNum;

	int[][] count;

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

	public PopulationManager(DataSetInfo tra, DataSetInfo tst, DataSetInfo eva, SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
		this.popSize = setting.popSize;
		this.Ndim = setting.Ndim;
		this.traDataSize = tra.getDataSize();
		this.evaDataSize = eva.getDataSize();
		this.tstDataSize = tst.getDataSize();
		this.ruleNum = setting.ruleNum;
	}

	// ************************************************************

	//Methods *****************************************************

	//初期個体群生成
	public void generateInitialPopulation(SettingForGA setting) {

		RuleSet ruleSet = null;
		Rule rule = null;
		float[] param = new float[2 * Ndim];

		//ランダムに組み合わせを生成
		int[] ruleIdx = new int[Ndim];
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			ruleSet = new RuleSet(setting);

			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				rule = new Rule(setting);

				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					ruleIdx[dim_i] = (int)(this.uniqueRnd.nextDoubleIE() * setting.Fdiv);
				}
				rule.setRule(ruleIdx);

				param[0] = setting.MoveNo_param[ ruleIdx[0] ][0];
				param[1] = setting.MoveNo_param[ ruleIdx[0] ][1];
				param[2] = setting.DBSN_param[ ruleIdx[1] ][0];
				param[3] = setting.DBSN_param[ ruleIdx[1] ][1];
				param[4] = setting.DWSN_param[ ruleIdx[2] ][0];
				param[5] = setting.DWSN_param[ ruleIdx[2] ][1];
				param[6] = setting.DBWR_param[ ruleIdx[3] ][0];
				param[7] = setting.DBWR_param[ ruleIdx[3] ][1];
				param[8] = setting.DWWR_param[ ruleIdx[4] ][0];
				param[9] = setting.DWWR_param[ ruleIdx[4] ][1];
				param[10] = setting.DBTMR_param[ ruleIdx[5] ][0];
				param[11] = setting.DBTMR_param[ ruleIdx[5] ][1];
				param[12] = setting.DWTMR_param[ ruleIdx[6] ][0];
				param[13] = setting.DWTMR_param[ ruleIdx[6] ][1];
				rule.setParams(param);

				ruleSet.rules.add(rule);
			}

			this.currentPops.add(ruleSet);
		}

	}

	//ruleIdx → FMLシステム
	//ルールインデックスからFMLを作ることでXML(KnowledgeBase)のサイズを小さくできる
	public void makeFML(SettingForGA setting) {
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			this.currentPops.get(pop_i).makeFS(setting);
		}
	}

	//param → FMLシステム生成
	public void param2fml() {
	}

	//毎回FMLインスタンスを作り直す
	public void calcConclusion2(SettingForGA setting, DataSetInfo tra, int generation) {
		int dataSize = tra.getDataSize();
		int Ndim = tra.getNdim();

		Pattern line;
		float y;
		float diff;
		float memberSum;
		float[] memberships = new float[ruleNum];
		float[] newConcList = new float[ruleNum];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable out;

		for(int pop_i = 0; pop_i < popSize; pop_i++) {

			for(int gene_i = 0; gene_i < generation; gene_i++) {

				for(int data_i = 0; data_i < dataSize; data_i++) {
					line = tra.getPattern(data_i);
					memberSum = 0;

					input[0] = currentPops.get(pop_i).fs.getVariable("MoveNo");
					input[1] = currentPops.get(pop_i).fs.getVariable("DBSN");
					input[2] = currentPops.get(pop_i).fs.getVariable("DWSN");
					input[3] = currentPops.get(pop_i).fs.getVariable("DBWR");
					input[4] = currentPops.get(pop_i).fs.getVariable("DWWR");
					input[5] = currentPops.get(pop_i).fs.getVariable("DBTMR");
					input[6] = currentPops.get(pop_i).fs.getVariable("DWTMR");
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

					currentPops.get(pop_i).fs.evaluate();

					for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
						//読み込んだデータに対してのメンバシップ値を保持
						memberships[rule_i] = ((TskVariableType) currentPops.get(pop_i).fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
						//現在の結論部の値を保持
						newConcList[rule_i] = ((TskVariableType) currentPops.get(pop_i).fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getZ();
						memberSum += memberships[rule_i];
					}

					//修正値計算
					out = currentPops.get(pop_i).fs.getVariable("EBWR");
					y =  out.getValue();
					diff = (float) line.getY() - y;
					for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
						newConcList[rule_i] += setting.eta * diff * memberships[rule_i] / memberSum;
					}

					currentPops.get(pop_i).setConcList(newConcList);
					currentPops.get(pop_i).makeFS(setting);

				}
			}
		}
	}

	//メンバシップ値を先に保存しといて学習を行う
	//こちらの方が早い
	public void calcConclusion(SettingForGA setting, DataSetInfo tra, int generation) {
		int dataSize = tra.getDataSize();
		int Ndim = tra.getNdim();

		Pattern[] line = new Pattern[dataSize];
		float y;
		float diff;
		float memberSum;
		float[][] memberships = new float[dataSize][ruleNum];
		float[] newConcList = new float[ruleNum];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable out;

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			for(int data_i = 0; data_i < dataSize; data_i++) {
				line[data_i] = tra.getPattern(data_i);


				input[0] = currentPops.get(pop_i).fs.getVariable("MoveNo");
				input[1] = currentPops.get(pop_i).fs.getVariable("DBSN");
				input[2] = currentPops.get(pop_i).fs.getVariable("DWSN");
				input[3] = currentPops.get(pop_i).fs.getVariable("DBWR");
				input[4] = currentPops.get(pop_i).fs.getVariable("DWWR");
				input[5] = currentPops.get(pop_i).fs.getVariable("DBTMR");
				input[6] = currentPops.get(pop_i).fs.getVariable("DWTMR");
				if(line[data_i].getDimValue(2) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue( line[data_i].getDimValue(dim_i) );
					}
				} else {
					input[0].setValue(line[data_i].getDimValue(0));
					input[1].setValue(line[data_i].getDimValue(1));
					input[2].setValue(line[data_i].getDimValue(1));
					input[3].setValue(line[data_i].getDimValue(3));
					input[4].setValue(1f - line[data_i].getDimValue(3));
					input[5].setValue(line[data_i].getDimValue(5));
					input[6].setValue(line[data_i].getDimValue(5));
				}

				currentPops.get(pop_i).fs.evaluate();
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					//読み込んだデータに対してのメンバシップ値を保持
					memberships[data_i][rule_i] = ((TskVariableType) currentPops.get(pop_i).fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
					//現在の結論部の値を保持
					newConcList[rule_i] = ((TskVariableType) currentPops.get(pop_i).fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getZ();
				}
			}

			//修正値計算
			for(int gene_i = 0; gene_i < generation; gene_i++) {
				for(int data_i = 0; data_i < dataSize; data_i++) {
					memberSum = 0;
					y = 0f;
					for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
						memberSum += memberships[data_i][rule_i];
						y += memberships[data_i][rule_i] * newConcList[rule_i];
					}
					y /= memberSum;
					diff = line[data_i].getY() - y;
					for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
						newConcList[rule_i] += setting.eta * diff * memberships[data_i][rule_i] / memberSum;
					}
				}
			}
			currentPops.get(pop_i).setConcList(newConcList);
			currentPops.get(pop_i).makeFS(setting);
		}
	}

	public void calcConclusion2(SettingForGA setting, DataSetInfo tra, DataSetInfo tst, int generation) {
		int dataSize = tra.getDataSize();
		int Ndim = tra.getNdim();

		Pattern[] line = new Pattern[dataSize];
		float y;
		float diff;
		float memberSum;
		float[][] memberships = new float[dataSize][ruleNum];
		float[] newConcList = new float[ruleNum];

//		float[] mseTra = new float[setting.popSize];
//		float[] mseTst = new float[setting.popSize];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable out;

		ResultMaster resultMaster = new ResultMaster();

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			resultMaster.addMSE();
			for(int data_i = 0; data_i < dataSize; data_i++) {
				line[data_i] = tra.getPattern(data_i);


				input[0] = currentPops.get(pop_i).fs.getVariable("MoveNo");
				input[1] = currentPops.get(pop_i).fs.getVariable("DBSN");
				input[2] = currentPops.get(pop_i).fs.getVariable("DWSN");
				input[3] = currentPops.get(pop_i).fs.getVariable("DBWR");
				input[4] = currentPops.get(pop_i).fs.getVariable("DWWR");
				input[5] = currentPops.get(pop_i).fs.getVariable("DBTMR");
				input[6] = currentPops.get(pop_i).fs.getVariable("DWTMR");
				if(line[data_i].getDimValue(2) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue( line[data_i].getDimValue(dim_i) );
					}
				} else {
					input[0].setValue(line[data_i].getDimValue(0));
					input[1].setValue(line[data_i].getDimValue(1));
					input[2].setValue(line[data_i].getDimValue(1));
					input[3].setValue(line[data_i].getDimValue(3));
					input[4].setValue(1f - line[data_i].getDimValue(3));
					input[5].setValue(line[data_i].getDimValue(5));
					input[6].setValue(line[data_i].getDimValue(5));
				}

				currentPops.get(pop_i).fs.evaluate();
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					//読み込んだデータに対してのメンバシップ値を保持
					memberships[data_i][rule_i] = ((TskVariableType) currentPops.get(pop_i).fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
					//現在の結論部の値を保持
					newConcList[rule_i] = ((TskVariableType) currentPops.get(pop_i).fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getZ();
				}
			}

			//修正値計算
			for(int gene_i = 0; gene_i < generation; gene_i++) {
				for(int data_i = 0; data_i < dataSize; data_i++) {
					memberSum = 0;
					y = 0f;
					for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
						memberSum += memberships[data_i][rule_i];
						y += memberships[data_i][rule_i] * newConcList[rule_i];
					}
					y /= memberSum;
					diff = line[data_i].getY() - y;
					for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
						newConcList[rule_i] += setting.eta * diff * memberships[data_i][rule_i] / memberSum;
					}
				}

				currentPops.get(pop_i).setConcList(newConcList);
				currentPops.get(pop_i).makeFS(setting);

				float[] yTra = GaManager.reasoning(tra, currentPops.get(pop_i).fs, setting);
				float[] yTst = GaManager.reasoning(tst, currentPops.get(pop_i).fs, setting);
				float mseTra = calcMSE(tra, yTra, setting);
				float mseTst = calcMSE(tst, yTst, setting);
				resultMaster.setMSE(mseTra, mseTst);
			}
		}
		resultMaster.writeMSE("calcGene" + String.valueOf(setting.calcGeneration) + "/writeMSE", setting);
	}

	//メンバシップ値を先に保存しといて学習を行う
	//こちらの方が早い
	public void calcConclusionForChild(SettingForGA setting, DataSetInfo tra, int generation) {
		int dataSize = tra.getDataSize();
		int Ndim = tra.getNdim();

		Pattern[] line = new Pattern[dataSize];
		float y;
		float diff;
		float memberSum;
		float[][] memberships = new float[dataSize][ruleNum];
		float[] newConcList = new float[ruleNum];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable out;

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			for(int data_i = 0; data_i < dataSize; data_i++) {
				line[data_i] = tra.getPattern(data_i);


				input[0] = newPops.get(pop_i).fs.getVariable("MoveNo");
				input[1] = newPops.get(pop_i).fs.getVariable("DBSN");
				input[2] = newPops.get(pop_i).fs.getVariable("DWSN");
				input[3] = newPops.get(pop_i).fs.getVariable("DBWR");
				input[4] = newPops.get(pop_i).fs.getVariable("DWWR");
				input[5] = newPops.get(pop_i).fs.getVariable("DBTMR");
				input[6] = newPops.get(pop_i).fs.getVariable("DWTMR");
				if(line[data_i].getDimValue(2) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue( line[data_i].getDimValue(dim_i) );
					}
				} else {
					input[0].setValue(line[data_i].getDimValue(0));
					input[1].setValue(line[data_i].getDimValue(1));
					input[2].setValue(line[data_i].getDimValue(1));
					input[3].setValue(line[data_i].getDimValue(3));
					input[4].setValue(1f - line[data_i].getDimValue(3));
					input[5].setValue(line[data_i].getDimValue(5));
					input[6].setValue(line[data_i].getDimValue(5));
				}

				newPops.get(pop_i).fs.evaluate();
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					//読み込んだデータに対してのメンバシップ値を保持
					memberships[data_i][rule_i] = ((TskVariableType) newPops.get(pop_i).fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
					//現在の結論部の値を保持
					newConcList[rule_i] = ((TskVariableType) newPops.get(pop_i).fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getZ();
				}
			}

			//修正値計算
			for(int gene_i = 0; gene_i < generation; gene_i++) {
				for(int data_i = 0; data_i < dataSize; data_i++) {
					memberSum = 0;
					y = 0f;
					for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
						memberSum += memberships[data_i][rule_i];
						y += memberships[data_i][rule_i] * newConcList[rule_i];
					}
					y /= memberSum;
					diff = line[data_i].getY() - y;
					for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
						newConcList[rule_i] += setting.eta * diff * memberships[data_i][rule_i] / memberSum;
					}
				}
			}
			newPops.get(pop_i).setConcList(newConcList);
			newPops.get(pop_i).makeFS(setting);
		}
	}

	//現世代個体FML出力
	public void outputCurrentFML(String folderName, int nowGene, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName =  setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			String fileName = dirName + sep +
							"gene" + String.valueOf(nowGene) +
							"_pop" + String.valueOf(pop_i) +
							".xml";
			File xml = new File(fileName);
			JFML.writeFSTtoXML(this.currentPops.get(pop_i).fs, xml);
		}
	}

	//現世代個体MSE出力
	public void outputCurrentMSE(String folderName, int nowGene, DataSetInfo dataset, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		float[] mse = calcMSE(dataset, setting);

		String fileName = dirName + sep +
						"gene" + String.valueOf(nowGene) +
						"_MSE.csv";
		try {
			Output.writeArray(mse, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	//現世代使用ファジィ集合出力
	public void outputCurrentCount(String folderName, int nowGene, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		String fileName = dirName + sep +
						"gene" + String.valueOf(nowGene) +
						"_count.csv";

		countUsedFuzzySet(setting);
		Output.writeArray(this.count, fileName);
	}

	//現世代個体MSE計算
	public float[] calcMSE(DataSetInfo dataset, SettingForGA setting) {
		float[][] y = reasoning(dataset, setting);
		float[] mse = new float[setting.popSize];
		Arrays.fill(mse, 0f);
		float diff;
		for(int pop_i = 0; pop_i < setting.popSize; pop_i++) {
			for(int data_i = 0; data_i < dataset.getDataSize(); data_i++) {
				diff = y[pop_i][data_i] - dataset.getPattern(data_i).getY();
				mse[pop_i] += diff * diff;
			}
			mse[pop_i] /= dataset.getDataSize();
		}
		return mse;
	}

	//与えられたy[]からMSE計算
	public float calcMSE(DataSetInfo dataset, float[] y, SettingForGA setting) {
		float mse = 0f;
		float diff;

		for(int data_i = 0; data_i < y.length; data_i++) {
			diff = y[data_i] - dataset.getPattern(data_i).getY();
			mse += diff * diff;
		}
		mse /= dataset.getDataSize();

		return mse;
	}

	//marge個体群MSE計算
	public float[] calcMseForChild(DataSetInfo dataset, SettingForGA setting) {
		float[][] y = reasoningForChild(dataset, setting);
		float[] mse = new float[setting.popSize];
		Arrays.fill(mse, 0f);
		float diff;
		for(int pop_i = 0; pop_i < setting.popSize; pop_i++) {
			for(int data_i = 0; data_i < dataset.getDataSize(); data_i++) {
				diff = y[pop_i][data_i] - dataset.getPattern(data_i).getY();
				mse[pop_i] += diff * diff;
			}
			mse[pop_i] /= dataset.getDataSize();
		}
		return mse;
	}

	//現世代個体推論値計算
	public float[][] reasoning(DataSetInfo dataset, SettingForGA setting) {
		int popSize = setting.popSize;
		int dataSize = dataset.getDataSize();
		int Ndim = setting.Ndim;

		float[][] y = new float[popSize][dataSize];
		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable out;
		Pattern[] line = new Pattern[dataSize];
		for(int data_i = 0; data_i < dataSize; data_i++) {
			line[data_i] = dataset.getPattern(data_i);
		}

		for(int pop_i = 0; pop_i < popSize; pop_i++	) {
			input[0] = currentPops.get(pop_i).fs.getVariable("MoveNo");
			input[1] = currentPops.get(pop_i).fs.getVariable("DBSN");
			input[2] = currentPops.get(pop_i).fs.getVariable("DWSN");
			input[3] = currentPops.get(pop_i).fs.getVariable("DBWR");
			input[4] = currentPops.get(pop_i).fs.getVariable("DWWR");
			input[5] = currentPops.get(pop_i).fs.getVariable("DBTMR");
			input[6] = currentPops.get(pop_i).fs.getVariable("DWTMR");

			for(int data_i = 0; data_i < dataSize; data_i++) {
				if(line[data_i].getDimValue(2) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue( line[data_i].getDimValue(dim_i) );
					}
				} else {
					input[0].setValue(line[data_i].getDimValue(0));
					input[1].setValue(line[data_i].getDimValue(1));
					input[2].setValue(line[data_i].getDimValue(1));
					input[3].setValue(line[data_i].getDimValue(3));
					input[4].setValue(1f - line[data_i].getDimValue(3));
					input[5].setValue(line[data_i].getDimValue(5));
					input[6].setValue(line[data_i].getDimValue(5));
				}

				currentPops.get(pop_i).fs.evaluate();

				out = currentPops.get(pop_i).fs.getVariable("EBWR");
				y[pop_i][data_i] = out.getValue();
			}
		}

		return y;
	}

	//marge個体群推論値計算
	public float[][] reasoningForChild(DataSetInfo dataset, SettingForGA setting) {
		int popSize = setting.popSize;
		int dataSize = dataset.getDataSize();
		int Ndim = setting.Ndim;

		float[][] y = new float[popSize][dataSize];
		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable out;
		Pattern[] line = new Pattern[dataSize];
		for(int data_i = 0; data_i < dataSize; data_i++) {
			line[data_i] = dataset.getPattern(data_i);
		}

		for(int pop_i = 0; pop_i < popSize; pop_i++	) {
			input[0] = newPops.get(pop_i).fs.getVariable("MoveNo");
			input[1] = newPops.get(pop_i).fs.getVariable("DBSN");
			input[2] = newPops.get(pop_i).fs.getVariable("DWSN");
			input[3] = newPops.get(pop_i).fs.getVariable("DBWR");
			input[4] = newPops.get(pop_i).fs.getVariable("DWWR");
			input[5] = newPops.get(pop_i).fs.getVariable("DBTMR");
			input[6] = newPops.get(pop_i).fs.getVariable("DWTMR");

			for(int data_i = 0; data_i < dataSize; data_i++) {
				if(line[data_i].getDimValue(2) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue( line[data_i].getDimValue(dim_i) );
					}
				} else {
					input[0].setValue(line[data_i].getDimValue(0));
					input[1].setValue(line[data_i].getDimValue(1));
					input[2].setValue(line[data_i].getDimValue(1));
					input[3].setValue(line[data_i].getDimValue(3));
					input[4].setValue(1f - line[data_i].getDimValue(3));
					input[5].setValue(line[data_i].getDimValue(5));
					input[6].setValue(line[data_i].getDimValue(5));
				}

				newPops.get(pop_i).fs.evaluate();

				out = newPops.get(pop_i).fs.getVariable("EBWR");
				y[pop_i][data_i] = out.getValue();
			}
		}

		return y;
	}

	//MSEを適合度としてセット
	public void setEvaAsFitness(float[] mse) {
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			this.currentPops.get(pop_i).setFitness(mse[pop_i]);
		}
	}

	//MSEを適合度としてセット
	public void setEvaAsFitnessForChild(float[] mse) {
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			this.newPops.get(pop_i).setFitness(mse[pop_i]);
		}
	}


	public void crossOverAndMichiganOpe(SettingForGA setting) {
		int mom, dad;
		int Nmom, Ndad;

		this.newPops.clear();

		for(int child_i = 0; child_i < setting.popSize; child_i++) {
			//親選択 バイナリトーナメント
			mom = binaryT4(setting);
			dad = binaryT4(setting);

			if(uniqueRnd.nextDoubleIE() < setting.rateCrossOver) {
				if(uniqueRnd.nextBoolean()) {
					this.newPops.add(new RuleSet(currentPops.get(mom), setting));
				} else {
					this.newPops.add(new RuleSet(currentPops.get(dad), setting));
				}
				continue;
			}

			Nmom = uniqueRnd.nextInt(setting.ruleNum) + 1;	//momから取り出すルールの個数を選択
			Ndad = setting.ruleNum - Nmom;					//dadから取り出すルールの個数を選択

			int[] pmom = sampringWithout(Nmom, setting.ruleNum);
			int[] pdad = sampringWithout(Ndad, setting.ruleNum);

			RuleSet ruleSet = new RuleSet(setting);
			Rule rule = new Rule(setting);

			ruleSet.rules.clear();
			for(int i = 0; i < Nmom; i++) {
				rule.setRule(currentPops.get(mom).rules.get(pmom[i]).rule);
				rule.setParams(currentPops.get(mom).rules.get(pmom[i]).params);
				ruleSet.rules.add(rule);
			}
			for(int i = 0; i < Ndad; i++) {
				rule.setRule(currentPops.get(dad).rules.get(pdad[i]).rule);
				rule.setParams(currentPops.get(dad).rules.get(pdad[i]).params);
				ruleSet.rules.add(rule);
			}

			this.newPops.add(ruleSet);
			this.newPops.get(child_i).makeFS(setting);
		}
	}

	//突然変異
	public void newPopsMutation(SettingForGA setting) {
		for(int pop_i = 0; pop_i < setting.popSize; pop_i++) {
			for(int rule_i = 0; rule_i < setting.ruleNum; rule_i++) {
				if(uniqueRnd.nextInt(setting.ruleNum) == 0) {
					int mutDim = uniqueRnd.nextInt(setting.Ndim);
					newPops.get(pop_i).micMutation(rule_i, mutDim, setting);
				}

			}
		}
	}

	//使用しているファジィ集合カウント
	public void countUsedFuzzySet(SettingForGA setting) {
		for(int pop_i = 0; pop_i < currentPops.size(); pop_i++) {
			currentPops.get(pop_i).countUsedFuzzySet(setting);
		}

		this.count = new int[setting.Ndim][setting.Fdiv];

		for(int pop_i = 0; pop_i < setting.popSize; pop_i++) {
			for(int dim_i = 0; dim_i < setting.Ndim; dim_i++) {
				for(int div_i = 0; div_i < setting.Fdiv; div_i++) {
					this.count[dim_i][div_i] += currentPops.get(pop_i).count[dim_i][div_i];
				}
			}
		}
	}

	public void populationMarge() {
		this.margePops.clear();
		for(int pop_i = 0; pop_i < this.currentPops.size(); pop_i++) {
			this.margePops.add(this.currentPops.get(pop_i));
		}
		for(int pop_i = 0; pop_i < this.newPops.size(); pop_i++) {
			this.margePops.add(this.newPops.get(pop_i));
		}
	}

	public void populationUpdate(SettingForGA setting) {
		//現世代個体群 + 子個体群 を marge
		populationMarge();
		currentPops.clear();
		newPops.clear();
		//fitnessの低い順にソート
		margePops.sort(comparing(RuleSet::getFitness));

		//fitnessの値が低い順にpopSizeだけ次世代に個体を格納
		for(int pop_i = 0; pop_i < setting.popSize; pop_i++) {
			currentPops.add(margePops.get(pop_i));
		}
	}

	public class PopulationComparator implements Comparator<RuleSet> {
		@Override
		public int compare(RuleSet a, RuleSet b) {
			float no1 = a.getFitness();
			float no2 = b.getFitness();

			//昇順でソート
			if(no1 > no2) {
				return 1;
			} else if(no1 == no2) {
				return 0;
			} else {
				return -1;
			}
		}
	}


	//[0, ruleNum-1]の範囲からnum個のインデックスを非復元抽出
	public int[] sampringWithout(int num, int ruleNum) {
		int ans[] = new int[num];

		for(int i = 0; i < num; i++) {
			boolean isSame = false;
			ans[i] = uniqueRnd.nextInt(ruleNum);
			for(int j = 0; j < i; j++) {
				if(ans[i] == ans[j]) {
					isSame = true;
				}
			}
			if(isSame) {
				i--;
			}
		}

		return ans;
	}

	public int binaryT4(SettingForGA setting) {
		int winner = 0;
		int select1, select2;

		//トーナメント出場者
		select1 = uniqueRnd.nextInt(setting.popSize);
		select2 = uniqueRnd.nextInt(setting.popSize);

		int optimizer = 1;	//最小化:1, 最大化:-1
		if( (optimizer * currentPops.get(select1).getFitness()) < (optimizer * currentPops.get(select2).getFitness()) ) {
			winner = select1;
		} else {
			winner = select2;
		}

		return winner;
	}



	//全ルール探索識別器生成
	public void generateAllRuleFS(int[][] setRule, float[] concList, SettingForFML setting) {

		this.currentPops.clear();
		int ruleNum = setRule.length;
//		for(int pop_i = 0; pop_i < ruleNum; pop_i++) {
//			currentPops.add(new Population(this.uniqueRnd, this.Ndim, setting) );
//		}

		this.all = new Rule(this.uniqueRnd, setting.Ndim, setting);

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

//		FuzzyTermType[] gaussians = new FuzzyTermType[3];
//		gaussians[0] = new FuzzyTermType("small_G", FuzzyTermType.TYPE_rightGaussianShape, new float[] {0f, 0.21f});
//		gaussians[1] = new FuzzyTermType("medium_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.5f, 0.21f});
//		gaussians[2] = new FuzzyTermType("large_G", FuzzyTermType.TYPE_leftGaussianShape, new float[] {1f, 0.21f});


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

//		this.all.setFS(fs);

	}

//	public void renewConclusion(float[] concList, PopulationManager popManager) {
//		popManager.all.getFS().getKnowledgeBase().getVariable("EBWR").getTerms().clear();
//		for(int rule_i = 0; rule_i < concList.length; rule_i++) {
//			((TskVariableType)popManager.all.getFS().getKnowledgeBase().getVariable("EBWR")).addTskTerm(new TskTermType("Conclusion" + String.valueOf(rule_i), 0, new float[] {concList[rule_i]}));
//		}
//	}


	// ************************************************************
}































