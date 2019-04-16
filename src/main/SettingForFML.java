package main;

import jfml.knowledgebase.KnowledgeBaseType;
import jfml.knowledgebase.variable.FuzzyVariableType;
import jfml.knowledgebase.variable.TskVariableType;
import jfml.term.FuzzyTermType;

public class SettingForFML {

	//Fields ********************************************
	FuzzyVariableType[] inputVariable;
	TskVariableType EBWR = new TskVariableType("EBWR");
	KnowledgeBaseType kb = new KnowledgeBaseType();
	FuzzyTermType dont = new FuzzyTermType("don't care", FuzzyTermType.TYPE_rectangularShape, new float[] {0f, 1f});

	//(Default) Gaussian Fuzzy Set
	FuzzyTermType[] gaussians;
	FuzzyTermType SS_G = new FuzzyTermType("SS_G", FuzzyTermType.TYPE_rightGaussianShape, new float[] {0f, 0.105f});
	FuzzyTermType S_G = new FuzzyTermType("S_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.25f, 0.105f});
//	FuzzyTermType S_G = new FuzzyTermType("S_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.25f, 0.0625f});
	FuzzyTermType M_G = new FuzzyTermType("M_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.5f, 0.105f});
	FuzzyTermType L_G = new FuzzyTermType("L_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.75f, 0.105f});
//	FuzzyTermType L_G = new FuzzyTermType("L_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.75f, 0.0625f});
	FuzzyTermType LL_G = new FuzzyTermType("LL_G", FuzzyTermType.TYPE_leftGaussianShape, new float[] {1f, 0.105f});


	//各種設定値
	int Ndim;	//入力属性数
	int Fdiv;	//ファジィ集合数
	int generation = 3000;	//世代数
	int popSize = 100;	//個体群サイズ
	int ruleNum = 30;	//1個体あたりのルール数
	int parallelCores = 4;
	float eta = 0.5f;
	// **************************************************

	//Constructor ***************************************
	public SettingForFML(int Ndim) {
		this.Ndim = Ndim;
		//input variable
		inputVariable = new FuzzyVariableType[Ndim];
		inputVariable[0] = new FuzzyVariableType("Move", 0, 1);
		inputVariable[1] = new FuzzyVariableType("DBSN", 0, 1);
		inputVariable[2] = new FuzzyVariableType("DWSN", 0, 1);
		inputVariable[3] = new FuzzyVariableType("DBWR", 0, 1);
		inputVariable[4] = new FuzzyVariableType("DWWR", 0, 1);
		inputVariable[5] = new FuzzyVariableType("DBTMR", 0, 1);
		inputVariable[6] = new FuzzyVariableType("DWTMR", 0, 1);

		//output variable
		EBWR.setType("output");
		EBWR.setCombination("WA");
//		kb.addVariable(EBWR);


		//make default gaussian fuzzy set
		gaussians = new FuzzyTermType[5];
		gaussians[0] = new FuzzyTermType("SS_G", FuzzyTermType.TYPE_rightGaussianShape, new float[] {0f, 0.105f});
		gaussians[1] = new FuzzyTermType("S_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.25f, 0.105f});
//		gaussians[1] = new FuzzyTermType("S_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.25f, 0.0625f});
		gaussians[2] = new FuzzyTermType("M_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.5f, 0.105f});
		gaussians[3] = new FuzzyTermType("L_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.75f, 0.105f});
//		gaissoams[3] = new FuzzyTermType("L_G", FuzzyTermType.TYPE_gaussianShape, new float[] {0.75f, 0.0625f});
		gaussians[4] = new FuzzyTermType("LL_G", FuzzyTermType.TYPE_leftGaussianShape, new float[] {1f, 0.105f});


//		for(int i = 0; i < Ndim; i++) {
//			for(int j = 0; j < Fdiv; j++) {
//				inputVariable[i].addFuzzyTerm(gaussians[j]);
//			}
//		}

//		for(int i = 0; i < Ndim; i++) {
//			kb.addVariable(inputVariable[i]);
//		}

	}
	// **************************************************

	//Methods *******************************************
	public void setNdim(int Ndim) {
		this.Ndim = Ndim;
	}

	public void setFdiv(int Fdiv) {
		this.Fdiv = Fdiv;
	}

	public KnowledgeBaseType getKnowledgeBase() {
		return this.kb;
	}

//	public static FuzzyVariableType[] makeInputVariables(int setRule ) {
//
//	}
	// **************************************************
}
