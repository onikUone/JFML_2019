package main;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {

		int Ndim = 7;	//入力次元数
		int Fdiv = 6;	//各次元分割数


		//読み込みファイル名
		String traFileName = "DataSets/FixedData/train_data_fixed3_6dim.csv";
		String tstFileName = "DataSets/FixedData/test_data_fixed3_6dim.csv";

		//データセット読み込み
		DataSetInfo traDataInfo = new DataSetInfo(traFileName);
		DataSetInfo tstDataInfo = new DataSetInfo(tstFileName);

		//結論部リスト
		float[] concList;


	}

}
