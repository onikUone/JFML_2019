package main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import jfml.FuzzyInferenceSystem;
import jfml.JFML;
import jfml.rule.TskFuzzyRuleType;
import jfml.rulebase.TskRuleBaseType;
import jfml.term.FuzzyTermType;

public class MakeResult {
	public static void main(String[] args) {

		String folderName = "XML/$LAST";

		String inputFML = "fitTRA_eta0.1_rule1000";
		String inputFuzzyParams = folderName + "/" + inputFML + ".csv";

		File xml = new File(folderName + "/" + inputFML + ".xml");
		FuzzyInferenceSystem fs = JFML.load(xml);

		File newdir = new File(folderName + "/" + inputFML);
		newdir.mkdirs();

		int Ndim = fs.getKnowledgeBase().getVariables().size() - 1;	//EBWR分マイナス
		int Fdiv = fs.getKnowledgeBase().getVariable("DBSN").getTerms().size() - 1;	//don't careがマイナス1
		int ruleNum = fs.getKnowledgeBase().getVariable("EBWR").getTerms().size();

		//fuzzyParams fileの読み込み
		float[][][] fuzzyParams = new float[Ndim][Fdiv][2];
		List<String[]> lines = new ArrayList<String[]>();
		String line;
		try {
			BufferedReader in = new BufferedReader(new FileReader(inputFuzzyParams));
			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				line = in.readLine();
				for(int div_i = 0; div_i < Fdiv; div_i++) {
					fuzzyParams[dim_i][div_i][0] = Float.parseFloat(line.split(",")[div_i]);
				}
				line = in.readLine();
				for(int div_i = 0; div_i < Fdiv; div_i++) {
					fuzzyParams[dim_i][div_i][1] = Float.parseFloat(line.split(",")[div_i]);
				}
				line = in.readLine();
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		String traFileName = "DataSets/traData_raw_Ndim6.csv";	//2は45Game分全て含む学習データ
		String tstFileName = "DataSets/tstData_raw_Ndim6.csv";
		DataSetInfo tra = new DataSetInfo(traFileName);
		DataSetInfo tst = new DataSetInfo(tstFileName);

		SettingForGA setting = new SettingForGA(tra);

		// **********************************************************************

		FS fsPop = new FS(setting);
		fsPop.readFML(fs, fuzzyParams, setting);


		String xmlFileName = folderName + "/" + inputFML + "/" + "after_" + inputFML + ".xml";
		File outputXML = new File(xmlFileName);
		JFML.writeFSTtoXML(fsPop.fs, outputXML);

		fsPop.resetConcList();
		fsPop.calcConclusion(setting, tra);
		fsPop.makeFS(setting);

		float[] yTra = fsPop.reasoning(setting, tra);
		float[] yTst = fsPop.reasoning(setting, tst);

		//推論値の出力
		int traDataSize = yTra.length;
		int tstDataSize = yTst.length;

		try {
			String fileName = folderName + "/" + inputFML + "/yTra.csv";
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
			pw.println("data,yTra,ELF");
			for(int data_i = 0; data_i < traDataSize; data_i++) {
				pw.println(data_i + "," + yTra[data_i] + "," + tra.getPattern(data_i).getY());
			}
			pw.close();

			fileName = folderName + "/" + inputFML + "/yTst.csv";
			fw = new FileWriter(fileName, true);
			pw = new PrintWriter( new BufferedWriter(fw) );
			pw.println("data,yTst,ELF");
			for(int data_i = 0; data_i < tstDataSize; data_i++) {
				pw.println(data_i + "," + yTst[data_i] + "," + tst.getPattern(data_i).getY());
			}

			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//MSEの出力
		float traMSE = FmlGaManager.calcMSE(yTra, tra);
		float tstMSE = FmlGaManager.calcMSE(yTst, tst);
		String mseFileName = folderName + "/" + inputFML + "/" + "mse.csv";
		try {
			FileWriter fw = new FileWriter(mseFileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
			pw.println("traMSE,tstMSE");
			pw.println(traMSE + "," + tstMSE);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//結論部リストの出力
		float[] concList = fsPop.concList;
		String concFileName = folderName + "/" + inputFML + "/" + "concList.csv";
		try {
			FileWriter fw = new FileWriter(concFileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				pw.println(concList[rule_i]);
			}
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		//足切り推論値出力
		float[] yTraCut = new float[yTra.length];
		float[] yTstCut = new float[yTst.length];

		for(int data_i = 0; data_i < yTra.length; data_i++) {
			if(yTra[data_i] > 1f) {
				yTraCut[data_i] = 1f;
			} else if(yTra[data_i] < 0f) {
				yTraCut[data_i] = 0f;
			} else {
				yTraCut[data_i] = yTra[data_i];
			}
		}

		for(int data_i = 0; data_i < yTst.length; data_i++) {
			if(yTst[data_i] > 1f) {
				yTstCut[data_i] = 1f;
			} else if(yTst[data_i] < 0f) {
				yTstCut[data_i] = 0f;
			} else {
				yTstCut[data_i] = yTst[data_i];
			}
		}

		try {
			String fileName = folderName + "/" + inputFML + "/yTraCut.csv";
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
			pw.println("data,yTraCut,ELF");
			for(int data_i = 0; data_i < traDataSize; data_i++) {
				pw.println(data_i + "," + yTraCut[data_i] + "," + tra.getPattern(data_i).getY());
			}
			pw.close();

			fileName = folderName + "/" + inputFML + "/yTstCut.csv";
			fw = new FileWriter(fileName, true);
			pw = new PrintWriter( new BufferedWriter(fw) );
			pw.println("data,yTstCut,ELF");
			for(int data_i = 0; data_i < tstDataSize; data_i++) {
				pw.println(data_i + "," + yTstCut[data_i] + "," + tst.getPattern(data_i).getY());
			}

			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		//足切りMSE出力
		float traCutMSE = FmlGaManager.calcMSE(yTraCut, tra);
		float tstCutMSE = FmlGaManager.calcMSE(yTstCut, tst);
		String mseCutFileName = folderName + "/" + inputFML + "/" + "mseCut.csv";
		try {
			FileWriter fw = new FileWriter(mseCutFileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
			pw.println("traCutMSE,tstCutMSE");
			pw.println(traCutMSE + "," + tstCutMSE);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


//		//Gaussian Shape 出力
//		int h = 100;
//		float[] x = new float[h];
//		float[][][] memberships = new float[Ndim][h][Fdiv];
//		String[] fuzzyName = {"VerySmall", "Small", "Medium", "Large", "VeryLarge"};
//		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
//			for(int i = 0; i < h; i++) {
//				x[i] = (float)i / (float)h;
//
//				for(int div_i = 0; div_i < Fdiv; div_i++) {
//					memberships[dim_i][i][div_i] = ((TskTermType)fsPop.fs.getKnowledgeBase()
//											.getKnowledgeBaseVariables().get(dim_i)
//											.getTerm(fuzzyName[div_i]))
//											.getMembershipValue(x[i]);
//				}
//
//			}
//		}


		//ルールベース表形式出力
		String ruleFileName = folderName + "/" + inputFML + "/" + "rule.csv";
		String ruleEasyFileName = folderName + "/" + inputFML + "/" + "ruleEasy.csv";
		String[] fuzzyName = {"VerySmall", "Small", "Medium", "Large", "VeryLarge"};
		String[] fuzzyNameEasy = {"VS", "S", "M", "L", "VL"};
		try {
			FileWriter fw = new FileWriter(ruleFileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
			FileWriter fw2 = new FileWriter(ruleEasyFileName, true);
			PrintWriter pw2 = new PrintWriter( new BufferedWriter(fw2) );

			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				TskFuzzyRuleType rule = ((TskRuleBaseType)fsPop.fs.getRuleBase(0)).getTskRules().get(rule_i);
				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					String termName = ((FuzzyTermType)rule.getAntecedent().getClauses().get(dim_i).getTerm()).getName();
					for(int div_i = 0; div_i < Fdiv; div_i++) {
						if(termName.equals(fuzzyName[div_i])) {
							pw.print(termName + ",");
							pw2.print(fuzzyNameEasy[div_i] + ",");
							break;
						}
					}
					if(termName.equals("Don't Care")) {
						pw.print(termName + ",");
						pw2.print("DC" + ",");
					}
				}
				pw.println();
				pw2.println();
			}

			pw.close();
			pw2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


//		String gaussianFileName = folderName + "/" + inputFML + "/" + "gaussianShape.csv";
//		String[] fuzzyNames = {"Very Small", "Small", "Medium", "Large", "Very Large"};
//		try {
//			FileWriter fw = new FileWriter(gaussianFileName, true);
//			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
//			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
//				for(int div_i = 0; div_i < Fdiv; div_i++) {
//					pw.print(fuzzyNames[div_i] + ",");
//				}
//				pw.print("-,");
//			}
//			pw.println();
//			for(int i = 0; i < h; i++) {
//				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
//					for(int div_i = 0; div_i < Fdiv; div_i++) {
//						pw.print(memberships[dim_i][i][div_i] + ",");
//					}
//					pw.print("-,");
//				}
//			}
//			pw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}


	}
}























