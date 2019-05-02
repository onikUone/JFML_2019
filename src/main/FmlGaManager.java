package main;

import java.util.ArrayList;

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

		//初期個体群評価
		evaluateFS(fmlPopulation.currentFS, setting, tra, eva);
		fmlPopulation.outputCurrentFML("fsGene_0/FML", 0, setting);
		fmlPopulation.outputCurrentMSE("fsGene_0/MSE", 0, tst, setting);

		//進化計算開始
		for(int gene_i = 0; gene_i < setting.fsGeneration; gene_i++) {
			System.out.println(".");

			//1. 子個体生成

			//2. 子個体の個体評価 = evaのMSE計算
			//3. 世代更新

		}

		System.out.println("---- GA for FS Finish ----");
	}

	//与えられたArrayList<FS>に対してevaのMSEを評価値としてセットする
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
