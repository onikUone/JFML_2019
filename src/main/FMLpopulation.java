package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import jfml.JFML;

public class FMLpopulation {

	//field
	float[][][] fuzzyParams;

	ArrayList<FS> currentFS = new ArrayList<FS>();
	ArrayList<FS> newFS = new ArrayList<FS>();
	ArrayList<FS> margeFS = new ArrayList<FS>();

	int Ndim;
	int Fdiv;
	int traDataSize;
	int evaDataSize;
	int tstDataSize;

	MersenneTwisterFast uniqueRnd;


	//constructor
	public FMLpopulation() {}

	public FMLpopulation(SettingForGA setting) {
		this.Ndim = setting.Ndim;
		this.Fdiv = setting.Fdiv;
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());

		this.fuzzyParams = new float[Ndim][Fdiv][2];	//各属性にFdiv種類のファジィ集合を定める（mとs）
	}


	//method

	//ファジィ集合ランダム生成
	public void initializeFuzzyParams(SettingForGA setting) {
		float posMin = 0f;

		for(int dim_i = 0; dim_i < this.Ndim; dim_i++) {
			posMin = 0f;
			for(int div_i = 0; div_i < this.Fdiv; div_i++) {
				//中心位置
				float rnd = posMin + this.uniqueRnd.nextFloatIE();
				while(rnd <= 0 || rnd >= 1) {
					rnd = posMin + this.uniqueRnd.nextFloatIE();
				}
				posMin = rnd;
				this.fuzzyParams[dim_i][div_i][0] = rnd;
				//分散値
				rnd = this.uniqueRnd.nextFloatIE();
				while(rnd <= 0 || rnd >= 0.5) {
					rnd = this.uniqueRnd.nextFloatIE();
				}
				this.fuzzyParams[dim_i][div_i][1] = rnd;
			}
		}
	}

	public void generateFS(SettingForGA setting) {
		int popSize = setting.popFS;

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			this.currentFS.add(new FS(setting));
			this.currentFS.get(pop_i).generateRuleIdx(setting);
			this.currentFS.get(pop_i).setFuzzyParams(this.fuzzyParams);
			this.currentFS.get(pop_i).makeFS(setting);
		}

	}

	//子個体生成
	public void crossOver(SettingForGA setting) {
		int mom, dad;
		int Nmom, Ndad;

		this.newFS.clear();

		int popSize = setting.popFS;
		for(int child_i = 0; child_i < popSize; child_i++) {
			//親選択
			mom = binaryT4(setting);
			dad = binaryT4(setting);

//TODO
		}

	}

	public int binaryT4(SettingForGA setting) {
		int winner = 0;
		int select1, select2;

		//トーナメント出場者
		select1 = uniqueRnd.nextInt(setting.popSize);
		select2 = uniqueRnd.nextInt(setting.popSize);

		int optimizer = 1;	//最小化:1, 最大化:-1
		if( (optimizer * currentFS.get(select1).getFitness()) < (optimizer * currentFS.get(select2).getFitness()) ) {
			winner = select1;
		} else {
			winner = select2;
		}

		return winner;
	}


	//現世代個体FML出力
	public void outputCurrentFML(String folderName, int nowGene, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName =  setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		for(int pop_i = 0; pop_i < this.currentFS.size(); pop_i++) {
			String fileName = dirName + sep +
							"gene" + String.valueOf(nowGene) +
							"_pop" + String.valueOf(pop_i) +
							".xml";
			File xml = new File(fileName);
			JFML.writeFSTtoXML(this.currentFS.get(pop_i).fs, xml);
		}
	}

	public void outputCurrentMSE(String folderName, int nowGene, DataSetInfo tst, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		int popSize = setting.popFS;
		float[] mse = new float[popSize];
		float[] y;
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			y = this.currentFS.get(pop_i).reasoning(setting, tst);
			mse[pop_i] = FmlGaManager.calcMSE(y, tst);
		}
		String fileName = dirName + sep +
				"gene" + String.valueOf(nowGene) +
				"_MSE.csv";
		try {
			Output.writeArray(mse, fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	public void outputFuzzyParams(int pop_i) {
		String fileName = "results/gaussian/test" + String.valueOf(pop_i) + ".csv";
		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int dim_i = 0; dim_i < this.Ndim; dim_i++) {
				for(int i = 0; i < 2; i++) {
					for(int div_i = 0; div_i < this.Fdiv; div_i++) {
						pw.print(this.fuzzyParams[dim_i][div_i][i] + ",");
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


}
