package main;

import jfml.knowledgebase.variable.FuzzyVariableType;
import jfml.knowledgebase.variable.TskVariableType;
import jfml.term.FuzzyTermType;

public class SettingForGA {
	//Fields ********************************************
	MersenneTwisterFast rnd;
	ResultMaster resultMaster;
	int Ndim;

	int Fdiv = 6;
	int gaGeneration = 1000;	//GA世代数
	int popSize = 100;
	int ruleNum = 100;	//1個体あたりのルール数

	int evaSize = 250;
	int fsGeneration = 1000;	//ルール最適化 世代数
	int fmlGeneration = 1000;	//ファジィ集合最適化 世代数
	int calcGeneration = 50;	//結論部学習エポック数
	int popFML = 1;
	int popFS = 100;
	int ruleMax = 500;
	int ruleMin = 100;

	float eta = 0.5f;
	int seed = 2021;
//	String resultFileName = "results/20190502/" +
//							"gaGene" + String.valueOf(gaGeneration) +
//							"_calcGene" + String.valueOf(calcGeneration) +
//							"_pop" + String.valueOf(popSize) +
//							"_rule" + String.valueOf(ruleNum) +
//							"_seed" + String.valueOf(seed);
	String resultFileName = "results/20190504_seed" + String.valueOf(seed);

	//GA設定
	float rateCrossOver = 0.9f;
	float rateMutation = 0.1f;


	FuzzyVariableType[] inputVariable;
	TskVariableType EBWR;
	//初期ファジイ集合
	FuzzyTermType[] MoveNo;
	FuzzyTermType[] DBSN;
	FuzzyTermType[] DWSN;
	FuzzyTermType[] DBWR;
	FuzzyTermType[] DWWR;
	FuzzyTermType[] DBTMR;
	FuzzyTermType[] DWTMR;

	float[][] MoveNo_param;
	float[][] DBSN_param;
	float[][] DWSN_param;
	float[][] DBWR_param;
	float[][] DWWR_param;
	float[][] DBTMR_param;
	float[][] DWTMR_param;

	// **************************************************

	//Constructor ***************************************
	public SettingForGA(DataSetInfo tra) {
		this.Ndim = tra.getNdim();
		this.rnd = new MersenneTwisterFast(this.seed);
		this.resultMaster = new ResultMaster(this.resultFileName);

//		//input variable
//		inputVariable = new FuzzyVariableType[this.Ndim];
//		inputVariable[0] = new FuzzyVariableType("Move", 0, 1);
//		inputVariable[1] = new FuzzyVariableType("DBSN", 0, 1);
//		inputVariable[2] = new FuzzyVariableType("DWSN", 0, 1);
//		inputVariable[3] = new FuzzyVariableType("DBWR", 0, 1);
//		inputVariable[4] = new FuzzyVariableType("DWWR", 0, 1);
//		inputVariable[5] = new FuzzyVariableType("DBTMR", 0, 1);
//		inputVariable[6] = new FuzzyVariableType("DWTMR", 0, 1);

//		//output variable
//		EBWR = new TskVariableType("EBWR");
//		EBWR.setType("output");
//		EBWR.setCombination("WA");

		//make default params
		MoveNo_param = new float[][] {{0f, 0.21f}, {1f, 0.21f}, {0.1f, 0.05f}, {0.2f, 0.05f}, {0.3f, 0.05f}, {0.4f, 0.05f}};
		DBSN_param = new float[][] {{0f, 0.21f}, {1f, 0.21f}, {0.08f, 0.2f}, {0.16f, 0.2f}, {0.24f, 0.2f}, {0.32f, 0.2f}};
		DWSN_param = new float[][] {{0f, 0.21f}, {1f, 0.21f}, {0.08f, 0.2f}, {0.16f, 0.2f}, {0.24f, 0.2f}, {0.32f, 0.2f}};
		DBWR_param = new float[][] {{0f, 0.21f}, {1f, 0.21f}, {0.3f, 0.05f}, {0.4f, 0.05f}, {0.5f, 0.05f}, {0.6f, 0.05f}};
		DWWR_param = new float[][] {{0f, 0.21f}, {1f, 0.21f}, {0.4f, 0.05f}, {0.5f, 0.05f}, {0.6f, 0.05f}, {0.7f, 0.05f}};
		DBTMR_param = new float[][] {{0f, 0.21f}, {1f, 0.21f}, {0.66f, 0.1f}, {0.72f, 0.1f}, {0.78f, 0.1f}, {0.84f, 0.1f}};
		DWTMR_param = new float[][] {{0f, 0.21f}, {1f, 0.21f}, {0.66f, 0.1f}, {0.72f, 0.1f}, {0.78f, 0.1f}, {0.84f, 0.1f}};

		//make default gaussian fuzzy sets

		//20190423
		MoveNo = new FuzzyTermType[6];
		MoveNo[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.21f});
		MoveNo[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.21f});
		MoveNo[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.1f, 0.05f});
		MoveNo[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.2f, 0.05f});
		MoveNo[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.3f, 0.05f});
		MoveNo[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.05f});

		DBSN = new FuzzyTermType[6];
		DBSN[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.21f});
		DBSN[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.21f});
		DBSN[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.08f, 0.2f});
		DBSN[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.16f, 0.2f});
		DBSN[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.24f, 0.2f});
		DBSN[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.32f, 0.2f});

		DWSN = new FuzzyTermType[6];
		DWSN[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.21f});
		DWSN[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.21f});
		DWSN[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.08f, 0.2f});
		DWSN[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.16f, 0.2f});
		DWSN[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.24f, 0.2f});
		DWSN[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.32f, 0.2f});

		DBWR = new FuzzyTermType[6];
		DBWR[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.21f});
		DBWR[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.21f});
		DBWR[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.3f, 0.05f});
		DBWR[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.05f});
		DBWR[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.5f, 0.05f});
		DBWR[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.6f, 0.05f});

		DWWR = new FuzzyTermType[6];
		DWWR[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.21f});
		DWWR[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.21f});
		DWWR[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.05f});
		DWWR[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.5f, 0.05f});
		DWWR[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.6f, 0.05f});
		DWWR[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.7f, 0.05f});

		DBTMR = new FuzzyTermType[6];
		DBTMR[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.21f});
		DBTMR[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.21f});
		DBTMR[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.66f, 0.1f});
		DBTMR[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.72f, 0.1f});
		DBTMR[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.78f, 0.1f});
		DBTMR[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.84f, 0.1f});

		DWTMR = new FuzzyTermType[6];
		DWTMR[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.21f});
		DWTMR[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.21f});
		DWTMR[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.66f, 0.1f});
		DWTMR[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.72f, 0.1f});
		DWTMR[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.78f, 0.1f});
		DWTMR[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.84f, 0.1f});

//		//20190425
//		MoveNo = new FuzzyTermType[6];
//		MoveNo[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.1f});
//		MoveNo[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.1f});
//		MoveNo[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.2f, 0.1f});
//		MoveNo[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.1f});
//		MoveNo[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.6f, 0.1f});
//		MoveNo[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.8f, 0.1f});
//
//		DBSN = new FuzzyTermType[6];
//		DBSN[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.1f});
//		DBSN[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.1f});
//		DBSN[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.2f, 0.1f});
//		DBSN[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.1f});
//		DBSN[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.6f, 0.1f});
//		DBSN[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.8f, 0.1f});
//
//		DWSN = new FuzzyTermType[6];
//		DWSN[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.1f});
//		DWSN[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.1f});
//		DWSN[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.2f, 0.1f});
//		DWSN[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.1f});
//		DWSN[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.6f, 0.1f});
//		DWSN[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.8f, 0.1f});
//
//		DBWR = new FuzzyTermType[6];
//		DBWR[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.1f});
//		DBWR[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.1f});
//		DBWR[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.2f, 0.1f});
//		DBWR[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.1f});
//		DBWR[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.6f, 0.1f});
//		DBWR[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.8f, 0.1f});
//
//		DWWR = new FuzzyTermType[6];
//		DWWR[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.1f});
//		DWWR[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.1f});
//		DWWR[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.2f, 0.1f});
//		DWWR[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.1f});
//		DWWR[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.6f, 0.1f});
//		DWWR[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.8f, 0.1f});
//
//		DBTMR = new FuzzyTermType[6];
//		DBTMR[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.1f});
//		DBTMR[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.1f});
//		DBTMR[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.2f, 0.1f});
//		DBTMR[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.1f});
//		DBTMR[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.6f, 0.1f});
//		DBTMR[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.8f, 0.1f});
//
//		DWTMR = new FuzzyTermType[6];
//		DWTMR[0] = new FuzzyTermType("left", FuzzyTermType.TYPE_gaussianShape, new float[] {0f, 0.1f});
//		DWTMR[1] = new FuzzyTermType("right", FuzzyTermType.TYPE_gaussianShape, new float[] {1f, 0.1f});
//		DWTMR[2] = new FuzzyTermType("one", FuzzyTermType.TYPE_gaussianShape, new float[] {0.2f, 0.1f});
//		DWTMR[3] = new FuzzyTermType("two", FuzzyTermType.TYPE_gaussianShape, new float[] {0.4f, 0.1f});
//		DWTMR[4] = new FuzzyTermType("three", FuzzyTermType.TYPE_gaussianShape, new float[] {0.6f, 0.1f});
//		DWTMR[5] = new FuzzyTermType("four", FuzzyTermType.TYPE_gaussianShape, new float[] {0.8f, 0.1f});



	}

	// **************************************************
}

