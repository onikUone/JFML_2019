package main;

import java.io.File;

import jfml.FuzzyInferenceSystem;
import jfml.JFML;

public class Main_calcContinue {

	public static void main(String[] args) {
		//読み込みファイル名
		String inputFML = args[0];
		System.out.println("input: " + inputFML);

		String traFileName = "DataSets/traData_raw_2.csv";	//2は45Game分全て含む学習データ
		String tstFileName = "DataSets/tstData_raw_2.csv";
		DataSetInfo tra = new DataSetInfo(traFileName);
		DataSetInfo tst = new DataSetInfo(tstFileName);

		File xml = new File("XML/" + inputFML);
		FuzzyInferenceSystem fs = JFML.load(xml);

		SettingForGA setting = new SettingForGA(tra);

		PopulationManager popManager = new PopulationManager(tra, tst, setting);

		RuleSet one = new RuleSet(setting);
		one.setFS(fs);
		one.calcConclusion(setting, tra, tst, 100);

		//FML出力
		//ディレクトリ生成
		String fileName =  setting.resultFileName + "/FML/after_" + inputFML;
		File outputXML = new File(fileName);
		JFML.writeFSTtoXML(one.fs, outputXML);


	}

}
