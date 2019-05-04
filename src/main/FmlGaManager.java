package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jfml.JFML;

public class FmlGaManager {
	//field

	//constructor
	public FmlGaManager() {}

	//method
	public void gaFrame(SettingForGA setting, FmlManager fmlManager, DataSetInfo tra, DataSetInfo tst, DataSetInfo eva) {
		System.out.println("---- GA Frame Start ----");

		//初期個体群生成
		fmlManager.generateInitialFML(setting);



		System.out.println("---- GA Frame Finish ----");
	}

	public void gaFsFrame(SettingForGA setting, FMLpopulation fmlPopulation, DataSetInfo tra, DataSetInfo tst, DataSetInfo eva) {
		System.out.println("---- GA for FS Start ----");
		outputFuzzyParams("$memo", fmlPopulation.fuzzyParams, setting);

		//初期個体群評価
//		fmlPopulation.outputCurrentFML("fsGene_0/FML", 0, setting);
//		fmlPopulation.outputCurrentMSE("fsGene_0/MSE", 0, tst, setting);
		evaluateFS(fmlPopulation.currentFS, setting, tra, eva);
		outputFML(fmlPopulation.currentFS, "fsGene_0_initial/FML", 0, setting);
		outputMSE(fmlPopulation.currentFS, "fsGene_0_initial/MSE", 0, tst, setting);

		//進化計算開始
		for(int gene_i = 0; gene_i < setting.fsGeneration; gene_i++) {
			System.out.print(".");

			//1. 子個体生成
			fmlPopulation.crossOver(setting);
			fmlPopulation.mutation(setting);

			//2. 子個体の個体評価 = evaのMSE計算
			evaluateFS(fmlPopulation.newFS, setting, tra, eva);

			//3. 世代更新
			fmlPopulation.populationUpdate(setting);

			//4. 現世代出力
			outputFML(fmlPopulation.currentFS, "fsGene_" + String.valueOf(gene_i+1) + "/FML", gene_i+1, setting);
			outputMSE(fmlPopulation.currentFS, "fsGene_" + String.valueOf(gene_i+1) + "/MSE", gene_i+1, tst, setting);
		}

		System.out.println("---- GA for FS Finish ----");
	}

	//与えられたArrayList<FS>に対してevaのMSEを評価値としてセットする
	//この中で結論部の学習は行われる
	public void evaluateFS(ArrayList<FS> fsList, SettingForGA setting, DataSetInfo tra, DataSetInfo eva) {
		int popSize = setting.popFS;
		float[] y;
		float mse;

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			//結論部学習（世代：setting.calcGeneration）
			fsList.get(pop_i).calcConclusion(setting, tra);
			//evaに対する推論値
			y = fsList.get(pop_i).reasoning(setting, eva);
			//yとevaのMSE
			mse = calcMSE(y, eva);
			//mseをFS個体の評価値としてセット
			fsList.get(pop_i).setFitness(mse);
		}

	}

	public void outputMSE(ArrayList<FS> fsList, String folderName, int nowGene, DataSetInfo tst, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		int popSize = setting.popFS;
		float[] mse = new float[popSize];
		float[] y;
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			y = fsList.get(pop_i).reasoning(setting, tst);
			mse[pop_i] = FmlGaManager.calcMSE(y, tst);
		}
		String fileName = dirName + sep +
				"gene" + String.valueOf(nowGene) +
				"_tstMSE.csv";
		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			pw.println("MSE,fitness");

			for(int pop_i = 0; pop_i < popSize; pop_i++) {
				pw.println(mse[pop_i] + "," + fsList.get(pop_i).fitness);
			}

			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void outputFML(ArrayList<FS> fsList, String folderName, int nowGene, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName =  setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		for(int pop_i = 0; pop_i < fsList.size(); pop_i++) {
			String fileName = dirName + sep +
							"gene" + String.valueOf(nowGene) +
							"_pop" + String.valueOf(pop_i) +
							".xml";
			File xml = new File(fileName);
			JFML.writeFSTtoXML(fsList.get(pop_i).fs, xml);
		}
	}

	public void outputFIT(ArrayList<FS> fsList, String folderName, int nowGene, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName =  setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();


		String fileName = dirName + sep + "fitness.csv";

		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int pop_i = 0; pop_i < fsList.size(); pop_i++) {
				pw.println(fsList.get(pop_i).fitness);
			}
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	public void outputFuzzyParams(String folderName, float[][][] fuzzyParams, SettingForGA setting) {
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		String fileName = dirName + sep + "FuzzySetParamator.csv";

		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;
		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				for(int i = 0; i < 2; i++) {
					for(int div_i = 0; div_i < Fdiv; div_i++) {
						pw.print(fuzzyParams[dim_i][div_i][i] + ",");
					}
					pw.println();
				}
				pw.println();
			}

			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//推論値yとdatasetとのMSEを計算する
	public static float calcMSE(float[] y, DataSetInfo dataset) {
		float mse = 0f;
		float diff;

		for(int data_i = 0; data_i < y.length; data_i++) {
			diff = y[data_i] - dataset.getPattern(data_i).getY();
			mse += diff * diff;
		}

		mse /= dataset.getDataSize();

		return mse;
	}
}
