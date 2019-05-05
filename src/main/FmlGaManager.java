package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import jfml.JFML;

public class FmlGaManager {
	//field

	//constructor
	public FmlGaManager() {}

	//method
	public void gaFrame(SettingForGA setting, FmlManager fmlManager, DataSetInfo tra, DataSetInfo tst, DataSetInfo eva) {
		System.out.println("---- GA Frame Start ----");
		int popSize = setting.popFML;

		//初期個体群生成
		fmlManager.generateInitialFML(setting);
		//GA FS実行 (= contribute獲得)
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			gaFsFrame(setting, pop_i, 0, fmlManager.currentFML.get(pop_i), tra, tst, eva);
		}

		for(int gene_i = 0; gene_i < setting.fmlGeneration; gene_i++) {
			//1. 子個体群(= 新しいfuzzyParamを持つnewFML)の生成
			fmlManager.makeNewFML(setting);
			//2. gaFsFrameの実行
			for(int pop_i = 0; pop_i < popSize; pop_i++) {
				gaFsFrame(setting, pop_i, gene_i+1, fmlManager.newFML.get(pop_i), tra, tst, eva);
			}
			//3. 個体群更新
		}




		System.out.println("---- GA Frame Finish ----");
	}

	public void gaFsFrame(SettingForGA setting, int popFML, int nowGene, FMLpopulation fmlPopulation, DataSetInfo tra, DataSetInfo tst, DataSetInfo eva) {
		System.out.println();
		System.out.println("------ popFML:" + String.valueOf(popFML) + "  gene:" + String.valueOf(nowGene) + " ------");
		Date start = new Date();
		System.out.println(start);

		String dirName = "fmlGene_" + String.valueOf(nowGene) + "/FMLpop" + String.valueOf(popFML);

		outputFuzzyParams(dirName + "/$memo_fs", fmlPopulation.fuzzyParams, setting);

		//初期個体群評価
//		fmlPopulation.outputCurrentFML("fsGene_0/FML", 0, setting);
//		fmlPopulation.outputCurrentMSE("fsGene_0/MSE", 0, tst, setting);
		evaluateFS(fmlPopulation.currentFS, setting, tra, eva);
		outputFML(fmlPopulation.currentFS, dirName + "/fsGene_0_initial/FML", 0, setting);
		outputMSE(fmlPopulation.currentFS, dirName + "/fsGene_0_initial/MSE", 0, tst, setting);

		//進化計算開始
		for(int gene_i = 0; gene_i < setting.fsGeneration; gene_i++) {
			System.out.print(".");

			//1. 子個体生成
			fmlPopulation.crossOver(setting);
			fmlPopulation.mutation(setting);

			//2. 子個体の個体評価 = evaのMSE計算
			evaluateFS(fmlPopulation.newFS, setting, tra, eva);

			//3. 世代更新
			fmlPopulation.populationUpdate(setting);

			//4. 現世代出力
			outputFML(fmlPopulation.currentFS, dirName + "/fsGene_" + String.valueOf(gene_i+1) + "/FML", gene_i+1, setting);
			outputMSE(fmlPopulation.currentFS, dirName + "/fsGene_" + String.valueOf(gene_i+1) + "/MSE", gene_i+1, tst, setting);
		}
		System.out.println();

		fmlPopulation.setContribute( calcContribute(fmlPopulation.currentFS, setting, tra, eva) );
		fmlPopulation.calcFitness();

		Date end = new Date();
		System.out.println(end);
		System.out.println("------ GA for FS Finish ------");
	}

	//
	public float[][] calcContribute(ArrayList<FS> fsList, SettingForGA setting, DataSetInfo tra, DataSetInfo eva) {

		int popSize = fsList.size();
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		float[][] contribute = new float[Ndim][Fdiv];
		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			Arrays.fill(contribute[dim_i], 0f);
		}

		float newMSE;
		float originMSE;

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			originMSE = fsList.get(pop_i).fitness;

			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				for(int div_i = 0; div_i < Fdiv; div_i++) {
					newMSE = fsList.get(pop_i).calcContribute(dim_i, div_i, setting, tra, eva);
					if( (newMSE - originMSE) > 0 ) {
						contribute[dim_i][div_i] += (newMSE - originMSE);
					}
				}
			}
		}

		return contribute;
	}

	//与えられたArrayList<FS>に対してevaのMSEを評価値としてセットする
	//この中で結論部の学習は行われる
	public void evaluateFS(ArrayList<FS> fsList, SettingForGA setting, DataSetInfo tra, DataSetInfo eva) {
		int popSize = fsList.size();
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

			//使われているファジィ集合をカウントする
			fsList.get(pop_i).countFuzzySet(setting);
		}

	}

	//自身のいくつかのpatternsをクリアして、新しいリストにしてreturnするメソッド
	public DataSetInfo pickEva(SettingForGA setting, DataSetInfo tra){
		DataSetInfo eva = new DataSetInfo(tra.getNdim());
		eva.copyAttribute(tra.Attribute);
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
		int evaSize = setting.evaSize;
		int idx;

		for(int eva_i = 0; eva_i < evaSize; eva_i++) {

			do {
				idx = uniqueRnd.nextInt( tra.patterns.size() );
			} while( tra.getPattern(idx).getDimValue(2) < 0 );
			eva.addPattern( tra.getPattern(idx) );
			tra.patterns.remove(idx);
		}

		tra.setDataSize(tra.patterns.size());
		eva.setDataSize(eva.patterns.size());

		outputEVA(eva, "$dataset", setting);
		outputTRA(tra, "$dataset", setting);

		return eva;
	}

	public int[] sampringWithout(int num, int evaSize, SettingForGA setting) {
		int[] ans = new int[num];
		MersenneTwisterFast uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
		for(int i = 0; i < num; i++) {
			boolean isSame = false;
			ans[i] = uniqueRnd.nextInt(evaSize);
			for(int j = 0; j < i; j++) {
				isSame = true;
			}
			if(isSame) {
				i--;
			}
		}
		return ans;
	}

	public void outputEVA(DataSetInfo eva, String folderName, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		int dataSize = eva.getDataSize();
		int Ndim = eva.getNdim();
		String fileName = dirName + sep + "evaDataSet.csv";
		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int data_i = 0; data_i < dataSize; data_i++) {
				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					pw.print(eva.getPattern(data_i).getDimValue(dim_i) +  ",");
				}
				pw.println(eva.getPattern(data_i).getY());
			}

			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void outputTRA(DataSetInfo tra, String folderName, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		int dataSize = tra.getDataSize();
		int Ndim = tra.getNdim();
		String fileName = dirName + sep + "traDataSet.csv";
		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int data_i = 0; data_i < dataSize; data_i++) {
				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					pw.print(tra.getPattern(data_i).getDimValue(dim_i) +  ",");
				}
				pw.println(tra.getPattern(data_i).getY());
			}

			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void outputMSE(ArrayList<FS> fsList, String folderName, int nowGene, DataSetInfo tst, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		int popSize = setting.popFS;
		float[] mse = new float[popSize];
		float[] y;
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			y = fsList.get(pop_i).reasoning(setting, tst);
			mse[pop_i] = FmlGaManager.calcMSE(y, tst);
		}
		String fileName = dirName + sep +
				"gene" + String.valueOf(nowGene) +
				"_tstMSE.csv";
		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			pw.println("MSE,fitness");

			for(int pop_i = 0; pop_i < popSize; pop_i++) {
				pw.println(mse[pop_i] + "," + fsList.get(pop_i).fitness);
			}

			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void outputFML(ArrayList<FS> fsList, String folderName, int nowGene, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName =  setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		for(int pop_i = 0; pop_i < fsList.size(); pop_i++) {
			String fileName = dirName + sep +
							"gene" + String.valueOf(nowGene) +
							"_pop" + String.valueOf(pop_i) +
							".xml";
			File xml = new File(fileName);
			JFML.writeFSTtoXML(fsList.get(pop_i).fs, xml);
		}
	}

	public void outputFIT(ArrayList<FS> fsList, String folderName, int nowGene, SettingForGA setting) {
		//ディレクトリ生成
		String sep = File.separator;
		String dirName =  setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();


		String fileName = dirName + sep + "fitness.csv";

		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int pop_i = 0; pop_i < fsList.size(); pop_i++) {
				pw.println(fsList.get(pop_i).fitness);
			}
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	public void outputFuzzyParams(String folderName, float[][][] fuzzyParams, SettingForGA setting) {
		String sep = File.separator;
		String dirName = setting.resultFileName + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		String fileName = dirName + sep + "FuzzySetParamator.csv";

		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;
		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				for(int i = 0; i < 2; i++) {
					for(int div_i = 0; div_i < Fdiv; div_i++) {
						pw.print(fuzzyParams[dim_i][div_i][i] + ",");
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
