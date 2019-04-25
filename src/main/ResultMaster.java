package main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ResultMaster {
	//Field ******************************************
	String nameDir;
	ArrayList<ArrayList<Float>> mseTra = new ArrayList<ArrayList<Float>>();
	ArrayList<ArrayList<Float>> mseTst = new ArrayList<ArrayList<Float>>();
	int mseIdx = -1;
	// ************************************************

	//Constructor *************************************
	public ResultMaster() {}

	public ResultMaster(String _nameDir) {
		this.nameDir = _nameDir;
	}
	// ************************************************

	//Methods *****************************************
	public String getDirName() {
		return this.nameDir;
	}

	public void addMSE() {
		this.mseTra.add(new ArrayList<Float>());
		this.mseTst.add(new ArrayList<Float>());
		this.mseIdx++;
	}

	public void setMSE(float mseTra, float mseTst) {
		this.mseTra.get(this.mseIdx).add(mseTra);
		this.mseTst.get(this.mseIdx).add(mseTst);
	}

	public void writeMSE(String folderName, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		Output.writeList(this.mseTra, dirName + sep +"mseTra.csv");
		Output.writeList(this.mseTst, dirName + sep +"mseTst.csv");

	}

//	public void setMSE(RuleSet _ruleSet, DataSetInfo _tra, DataSetInfo _tst) {
//		int mTra = _tra.getDataSize();
//		int mTst = _tst.getDataSize();
//		double x, y, diff;
//		double mseTra = 0.0;
//		double mseTst = 0.0;
//
//		//学習用データMSE
//		for(int i = 0; i < mTra; i++) {
//			x = _ruleSet.calcY(_tra.getPattern(i));
//			y = _tra.getPattern(i).getY();
//			diff = x - y;
//			mseTra += diff * diff;
//		}
//		mseTra /= mTra;
//
//		//評価用データMSE
//		for(int i = 0; i < mTst; i++) {
//			x = _ruleSet.calcY(_tst.getPattern(i));
//			y = _tst.getPattern(i).getY();
//			diff = x - y;
//			mseTst += diff * diff;
//		}
//		mseTst /= mTst;
//
//		//結果保持
//		this.mseTra.add(mseTra);
//		this.mseTst.add(mseTst);
//	}


//	public void calcMSE(DataSetInfo tra, DataSetInfo tst, float[] yTra, float[] yTst) {
//		int mTra = tra.getDataSize();
//		int mTst = tst.getDataSize();
//		float x, y, diff;
//		float mseTra = 0f;
//		float mseTst = 0f;
//
//		//学習用データMSE
//		for(int data_i = 0; data_i < mTra; data_i++) {
//			x = yTra[data_i];
//			y = tra.getPattern(data_i).getY();
//			diff = x - y;
//			mseTra += diff * diff;
//		}
//		mseTra /= mTra;
//
//		//評価用データMSE
//		for(int data_i = 0; data_i < mTst; data_i++) {
//			x = yTst[data_i];
//			y = tst.getPattern(data_i).getY();
//			diff = x - y;
//			mseTst += diff * diff;
//		}
//		mseTst /= mTst;
//
//		//結果保持
//		this.mseTra.add(mseTra);
//		this.mseTst.add(mseTst);
//	}

	public void writeY(float[] _yTra, float[] _yTst, int _nowGene) throws IOException {
		String fileNameTra = this.nameDir + "final_yTra_gene-" + _nowGene + ".csv";
		String fileNameTst = this.nameDir + "final_yTst_gene-" + _nowGene + ".csv";
		Output.writeArray(_yTra, fileNameTra);
		Output.writeArray(_yTst, fileNameTst);
	}

//	public void writeMSE(int _nowGene) throws IOException {
//		String fileNameTra = this.nameDir + "gene" + _nowGene + "_mseTra" + ".csv";
//		String fileNameTst = this.nameDir + "gene" + _nowGene + "_mseTst" + ".csv";
//		Output.writeList(this.mseTra, fileNameTra);
//		Output.writeList(this.mseTst, fileNameTst);
//	}

//	public void writeConclusion(int _nowGene, RuleSet _ruleSet) throws IOException {
//		String fileNameConc = this.nameDir + "conclusion/gene_" + _nowGene + ".csv";
//		Output.writeConc(_ruleSet, fileNameConc);
//	}

	// ************************************************
}
