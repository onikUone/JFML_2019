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

public class CalcConclusion {

	public static void main(String[] args) {
//		String inputFML = args[0];
//		String inputFuzzyParams = args[1];
//		int calcGeneration = Integer.parseInt(args[2]);
//		int outputInterval = Integer.parseInt(args[3]);


		String inputFML = "Gene1_ruleNum20000";
		String inputFuzzyParams = "XML/relearning/Gene1_ruleNum20000.csv";
		int calcGeneration = 1000;
		int outputInterval = 50;

		String folderName = "XML/relearning/" + inputFML;

		File xml = new File(folderName + ".xml");
		FuzzyInferenceSystem fs = JFML.load(xml);


		File newdir = new File(folderName + "/MSE");
		newdir.mkdirs();
		newdir = new File(folderName + "/FML");
		newdir.mkdirs();

		int Ndim = fs.getKnowledgeBase().getVariables().size() - 1;	//EBWR分マイナス
		int Fdiv = fs.getKnowledgeBase().getVariable("DBSN").getTerms().size() - 1;	//don't careがマイナス1
		int ruleNum = fs.getKnowledgeBase().getVariable("EBWR").getTerms().size();

		//fuzzyParamsの読み込み
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

		String traFileName = null;
		if(Ndim == 7) {
			traFileName = "DataSets/traData_raw_2.csv";	//2は45Game分全て含む学習データ
		} else if(Ndim == 6) {
			traFileName = "DataSets/traData_raw_Ndim6.csv";	//2は45Game分全て含む学習データ
		}
		String tstFileName = "DataSets/tstData_raw_Ndim6.csv";
		DataSetInfo tra = new DataSetInfo(traFileName);
		DataSetInfo tst = new DataSetInfo(tstFileName);

		SettingForGA setting = new SettingForGA(tra);

//		float[][][] fuzzyParams = new float[Ndim][Fdiv][2];

//		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
//			for(int div_i = 0; div_i < Fdiv; div_i++) {
//				fuzzyParams[dim_i][div_i][0] = ((FuzzyTermType)fs
//												.getKnowledgeBase().getKnowledgeBaseVariables().get(dim_i)
//												.getTerms().get(div_i))
//												.getGaussianShape().getParam1();
//				fuzzyParams[dim_i][div_i][1] = ((FuzzyTermType)fs
//						.getKnowledgeBase().getKnowledgeBaseVariables().get(dim_i)
//						.getTerms().get(div_i))
//						.getGaussianShape().getParam2();
//			}
//		}

		FS fsPop = new FS(setting);
		fsPop.readFML(fs, fuzzyParams, setting);
		String fileName = folderName + "/FML/after0_" + inputFML + ".xml";
		ArrayList<float[]> allMSE = new ArrayList<float[]>();
		outputXML(fileName, fsPop);
		calcMSE(fsPop, setting, tra, tst, allMSE);
		outputMSE(folderName + "/MSE/gene0.csv", outputInterval, allMSE);

		for(int gene_i = 0; gene_i < calcGeneration; gene_i++) {
			fsPop.calcContinueConclusion(setting, outputInterval, tra);
			System.out.println("calc: " + String.valueOf((gene_i+1) * outputInterval));
			fileName = folderName + "/FML/after" + String.valueOf((gene_i+1) * outputInterval) + "_" + inputFML + ".xml";
			outputXML(fileName, fsPop);
			calcMSE(fsPop, setting, tra, tst, allMSE);
			outputMSE(folderName + "/MSE/gene" + String.valueOf((gene_i+1) * outputInterval) + ".csv", outputInterval, allMSE);
		}

		System.out.println();
	}

	public static void outputXML(String fileName, FS fsPop) {
		File outputXML = new File(fileName);
		JFML.writeFSTtoXML(fsPop.fs, outputXML);
	}

	public static void calcMSE(FS fsPop, SettingForGA setting, DataSetInfo tra, DataSetInfo tst, ArrayList<float[]> allMSE) {
		fsPop.makeFS(setting);
		float[] yTra = fsPop.reasoning(setting, tra);
		float[] yTst = fsPop.reasoning(setting, tst);

		float traMSE = main.FmlGaManager.calcMSE(yTra, tra);
		float tstMSE = main.FmlGaManager.calcMSE(yTst, tst);
		float[] newMSE = {traMSE, tstMSE};
		allMSE.add(newMSE);
	}

	public static void outputMSE(String fileName, int outputInterval, ArrayList<float[]> allMSE) {
		int generation = allMSE.size();

		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			pw.println("Gene,tra,tst");

			for(int gene_i = 0; gene_i < generation; gene_i++) {
				pw.print(String.valueOf((gene_i+1) * outputInterval) + ",");
				pw.print(String.valueOf(allMSE.get(gene_i)[0]) + ",");
				pw.println(String.valueOf(allMSE.get(gene_i)[1]));
			}

			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


	}



}
