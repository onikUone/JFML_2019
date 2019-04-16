package main;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {

		int Fdiv = 5;	//各次元分割数

		//読み込みファイル名
		String traFileName = "DataSets/traData_raw_2.csv";
		String tstFileName = "DataSets/tstData_raw_2.csv";

		//データセット読み込み
		DataSetInfo traDataInfo = new DataSetInfo(traFileName);
		DataSetInfo tstDataInfo = new DataSetInfo(tstFileName);

		//結論部リスト
		float[] firstConcList = null;

		startExperiment(Fdiv, traDataInfo, tstDataInfo, firstConcList);

	}

	public static void startExperiment(int Fdiv, DataSetInfo tra, DataSetInfo tst, float[] concList) {
		int Ndim = tra.getNdim();	//入力次元数

		int seed = 2019;
		MersenneTwisterFast rnd = new MersenneTwisterFast(seed);

		//結果書き出し
		String resultFileName = "results/20190416_gaussian/";
		Output out = new Output(resultFileName);
		ResultMaster resultMaster = new ResultMaster(resultFileName);

		SettingForFML setting = new SettingForFML(Ndim);
		setting.setNdim(Ndim);
		setting.setFdiv(Fdiv);

		int[][] setRule = makeSetRule(Ndim, Fdiv);

		PopulationManager popManager = new PopulationManager(rnd, tra, tst, setting);

		GaManager gaManager = new GaManager();
		gaManager.gaFrame(setting, popManager, setRule, concList, tra, tst, resultMaster);


		System.out.println();
	}

	public static int[][] makeSetRule(int Ndim, int Fdiv) {
		int ruleNum = (int)Math.pow(Fdiv, Ndim);	//6種類のファジィ集合 の Ndim乗
		int[][] setRule = new int[ruleNum][Ndim];
		int index = 0;
		for(int Ndim_i = 0; Ndim_i < Ndim; Ndim_i++) {
			while(index < ruleNum) {
				for(int rule_i = 0; rule_i < Fdiv; rule_i++) {
					if(Ndim_i == 0) {
						setRule[index][Ndim_i] = rule_i;
						index++;
					} else {
						for(int j = 0; j < (int)Math.pow(Fdiv, Ndim_i); j++) {
							setRule[index][Ndim_i] = rule_i;
							index++;
						}
					}
				}
			}
			index = 0;
		}
		return setRule;
	}

}
