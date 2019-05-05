package main;

import java.util.Date;

public class Main_new {

	public static void main(String[] args) {
		//読み込みファイル名
//		String traFileName = "DataSets/traData_raw_3.csv";	//evaをもともと抜いている
		String traFileName = "DataSets/traData_raw_2.csv";	//evaを抜いていない
		String evaFileName = "DataSets/evaData_raw_2.csv";
		String tstFileName = "DataSets/tstData_raw_2.csv";

		//データセット読み込み
		DataSetInfo tra = new DataSetInfo(traFileName);
//		DataSetInfo eva = new DataSetInfo(evaFileName);
		DataSetInfo tst = new DataSetInfo(tstFileName);
		SettingForGA setting = new SettingForGA(tra);

		FmlGaManager gaManager = new FmlGaManager();
		DataSetInfo eva = gaManager.pickEva(setting, tra);

		FmlManager fmlManager = new FmlManager(tra, tst, eva, setting);

		Date start = new Date();
		System.out.println(start);

		gaManager.gaFrame(setting, fmlManager, tra, tst, eva);


		Date end = new Date();
		System.out.println(end);
		System.out.println("");
	}

}
