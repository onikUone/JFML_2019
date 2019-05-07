package data_methods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import jfml.FuzzyInferenceSystem;
import jfml.JFML;
import jfml.term.FuzzyTermType;
import main.DataSetInfo;
import main.FS;
import main.SettingForGA;

public class CalcContinue2 {

	public static void main(String[] args) {
		String inputFML = args[0];
		System.out.println("input: " + inputFML + ".xml");

		int continueGeneration = Integer.parseInt(args[1]);
		int outputInterval = Integer.parseInt(args[2]);

		File xml = new File("XML/" + inputFML + ".xml");
		FuzzyInferenceSystem fs = JFML.load(xml);

		String folderName = "XML/" + inputFML;
		File newdir = new File(folderName);
		newdir.mkdirs();
		newdir = new File(folderName + "/MSE");
		newdir.mkdirs();

		int Ndim = fs.getKnowledgeBase().getVariables().size() - 1;	//EBWR分マイナス
		int Fdiv = fs.getKnowledgeBase().getVariable("DBSN").getTerms().size() - 1;	//don't careがマイナス1
		int ruleNum = fs.getKnowledgeBase().getVariable("EBWR").getTerms().size();

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

		float[][][] fuzzyParams = new float[Ndim][Fdiv][2];

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				fuzzyParams[dim_i][div_i][0] = ((FuzzyTermType)fs
												.getKnowledgeBase().getKnowledgeBaseVariables().get(dim_i)
												.getTerms().get(div_i))
												.getGaussianShape().getParam1();
				fuzzyParams[dim_i][div_i][1] = ((FuzzyTermType)fs
						.getKnowledgeBase().getKnowledgeBaseVariables().get(dim_i)
						.getTerms().get(div_i))
						.getGaussianShape().getParam2();
			}
		}

		FS fsPop = new FS(setting);
		fsPop.readFML(fs, fuzzyParams, setting);
		String fileName = folderName + "/after0_" + inputFML + ".xml";
		outputXML(fileName, fsPop);
		outputMSE(fsPop, folderName + "/MSE/gene0.csv", tra, tst, setting);


		for(int gene_i = 0; gene_i < continueGeneration; gene_i++) {
			fsPop.calcContinueConclusion(setting, outputInterval, tra);
			fileName = folderName + "/after" + String.valueOf((gene_i+1) * outputInterval) + "_" + inputFML + ".xml";
			outputXML(fileName, fsPop);
			outputMSE(fsPop, folderName + "/MSE/gene" + String.valueOf((gene_i+1) * outputInterval) + ".csv", tra, tst, setting);
		}


		System.out.println();

	}

	public static void outputXML(String fileName, FS fsPop) {
		File outputXML = new File(fileName);
		JFML.writeFSTtoXML(fsPop.fs, outputXML);
	}

	public static void outputMSE(FS fsPop, String fileName, DataSetInfo tra, DataSetInfo tst, SettingForGA setting) {
		float[] yTra = fsPop.reasoning(setting, tra);
		float[] yTst = fsPop.reasoning(setting, tst);

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
