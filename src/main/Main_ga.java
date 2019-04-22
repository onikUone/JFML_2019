package main;

import java.io.IOException;

public class Main_ga {

	public static void main(String[] args) throws IOException{
		//読み込みファイル名
		String traFileName = "DataSets/traData_raw_3.csv";
		String evaFileName = "DataSets/evaData_raw_2.csv";
		String tstFileName = "DataSets/tstData_raw_2.csv";

		//データセット読み込み
		DataSetInfo traDataInfo = new DataSetInfo(traFileName);
		DataSetInfo evaDataInfo = new DataSetInfo(evaFileName);
		DataSetInfo tstDataInfo = new DataSetInfo(tstFileName);

		startExperiment(traDataInfo, tstDataInfo, evaDataInfo);

	}

	public static void startExperiment(DataSetInfo tra, DataSetInfo tst, DataSetInfo eva) {

		SettingForGA setting = new SettingForGA(tra);

		PopulationManager popManager = new PopulationManager(tra, tst, eva, setting);
		GaManager gaManager = new GaManager();

		gaManager.gaFrame2(setting, popManager, tra, tst, eva);

	}

}
