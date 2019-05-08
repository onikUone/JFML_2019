package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class KbManager implements Serializable{

	public ArrayList<KnowledgeBasePop> currentKB = new ArrayList<KnowledgeBasePop>();
	public ArrayList<KnowledgeBasePop> newKB = new ArrayList<KnowledgeBasePop>();
	public ArrayList<KnowledgeBasePop> margeKB = new ArrayList<KnowledgeBasePop>();

	FS bestFS;
	MersenneTwisterFast uniqueRnd;

	public KbManager(SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast( setting.rnd.nextInt() );
	}

	public void generateInitialKB(SettingForGA setting) {
		int popSize = setting.popKB;
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		float[][][] originFuzzyParams = new float[Ndim][Fdiv][2];
		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				originFuzzyParams[dim_i][div_i][0] = this.bestFS.fuzzyParams[dim_i][div_i][0];
				originFuzzyParams[dim_i][div_i][1] = this.bestFS.fuzzyParams[dim_i][div_i][1];
			}
		}

		//1つは元のKnowledgeBaseを保持
		KnowledgeBasePop kbPop = new KnowledgeBasePop(setting);
		kbPop.setFuzzyParams(originFuzzyParams);
		this.currentKB.add(kbPop);

		for(int pop_i = 1; pop_i < popSize; pop_i++) {

			int count = 0;
			float[][][] fuzzyParams = new float[Ndim][Fdiv][2];
			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				for(int div_i = 0; div_i < Fdiv; div_i++) {
					fuzzyParams[dim_i][div_i][0] = originFuzzyParams[dim_i][div_i][0];
					fuzzyParams[dim_i][div_i][1] = originFuzzyParams[dim_i][div_i][1];
					if(uniqueRnd.nextDoubleIE() < setting.perturbationRate) {
						//摂動を加える;
						count++;
						float[] delta = calcDelta();
						fuzzyParams[dim_i][div_i][0] += delta[0];
						fuzzyParams[dim_i][div_i][1] += delta[1];

						//0以下，1以上の処理
						if(fuzzyParams[dim_i][div_i][0] < 0f) {
							fuzzyParams[dim_i][div_i][0] = 0f;
						} else if(fuzzyParams[dim_i][div_i][0] > 1f) {
							fuzzyParams[dim_i][div_i][0] = 1f;
						}
						if(fuzzyParams[dim_i][div_i][1] < 0f) {
							fuzzyParams[dim_i][div_i][1] = 0.001f;
						} else if(fuzzyParams[dim_i][div_i][1] > 0.5f) {
							fuzzyParams[dim_i][div_i][1] = 0.5f;
						}
					}
				}
			}

			kbPop = new KnowledgeBasePop(setting);
			kbPop.setFuzzyParams(fuzzyParams);
			this.currentKB.add(kbPop);
			System.out.println("perturbation : " + count);
		}
		System.out.println();
	}

	//摂動を加えて子個体群生成
	public void perturbation(SettingForGA setting) {
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;
		int popSize = this.currentKB.size();

		int parent;
		KnowledgeBasePop kbPop;

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			parent = binaryT4(setting);
			float[][][] fuzzyParams = new float[Ndim][Fdiv][2];

			int count = 0;
			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				for(int div_i = 0; div_i < Fdiv; div_i++) {
					fuzzyParams[dim_i][div_i][0] = this.currentKB.get(parent).fuzzyParams[dim_i][div_i][0];
					fuzzyParams[dim_i][div_i][1] = this.currentKB.get(parent).fuzzyParams[dim_i][div_i][1];

					if(uniqueRnd.nextDoubleIE() < setting.perturbationRate) {
						//摂動を加える
						count++;
						float[] delta = calcDelta();
						fuzzyParams[dim_i][div_i][0] += delta[0];
						fuzzyParams[dim_i][div_i][1] += delta[1];

						//0以下，1以上の処理
						if(fuzzyParams[dim_i][div_i][0] < 0f) {
							fuzzyParams[dim_i][div_i][0] = 0f;
						} else if(fuzzyParams[dim_i][div_i][0] > 1f) {
							fuzzyParams[dim_i][div_i][0] = 1f;
						}
						if(fuzzyParams[dim_i][div_i][1] < 0f) {
							fuzzyParams[dim_i][div_i][1] = 0.001f;
						} else if(fuzzyParams[dim_i][div_i][1] > 0.5f) {
							fuzzyParams[dim_i][div_i][1] = 0.5f;
						}
					}

				}
			}

			kbPop = new KnowledgeBasePop(setting);
			kbPop.setFuzzyParams(fuzzyParams);
			this.newKB.add(kbPop);
			System.out.println("perturbation : " + count);
		}
		System.out.println();
	}

	public int binaryT4(SettingForGA setting) {
		int winner = 0;
		int select1, select2;

		//トーナメント出場者
		select1 = uniqueRnd.nextInt(setting.popKB);
		select2 = uniqueRnd.nextInt(setting.popKB);

		int optimizer = 1;	//最小化:1, 最大化:-1
		if( (optimizer * currentKB.get(select1).getFitness()) < (optimizer * currentKB.get(select2).getFitness()) ) {
			winner = select1;
		} else {
			winner = select2;
		}

		return winner;
	}

	public void populationUpdate(SettingForGA setting) {
		//現世代 + 子世代 を marge
		this.margeKB.clear();
		for(int pop_i = 0; pop_i < this.currentKB.size(); pop_i++) {
			this.margeKB.add(this.currentKB.get(pop_i));
		}
		for(int pop_i = 0; pop_i < this.newKB.size(); pop_i++) {
			this.margeKB.add(this.newKB.get(pop_i));
		}
		this.currentKB.clear();
		this.newKB.clear();
		//fitnessの値が低い順にソート
		this.margeKB.sort(java.util.Comparator.comparing(KnowledgeBasePop::getFitness));

		//fitnessの値が良い順にpopFSだけ次世代に個体を格納
		for(int pop_i = 0; pop_i < setting.popRB; pop_i++) {
			this.currentKB.add( new KnowledgeBasePop(this.margeKB.get(pop_i)) );
		}
	}

	public class kbComparetor implements Comparator<KnowledgeBasePop>{
		@Override
		public int compare(KnowledgeBasePop a, KnowledgeBasePop b) {
			float no1 = a.getFitness();
			float no2 = b.getFitness();

			//昇順でソート
			if(no1 > no2) {
				return 1;
			} else if(no1 == no2) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	public void evaluate(ArrayList<KnowledgeBasePop> kbList, SettingForGA setting, DataSetInfo tra, DataSetInfo eva) {
		int popSize = kbList.size();

		float[] y;
		float mse;

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			FS fs = new FS(this.bestFS, setting);
			fs.deepFuzzyParams(kbList.get(pop_i).getFuzzyParams());
			fs.resetConcList();
			fs.makeFS(setting);

			fs.calcConclusion(setting, tra);
			y = fs.reasoning(setting, eva);
			mse = FmlGaManager.calcMSE(y, eva);

			kbList.get(pop_i).setFitness(mse);
		}
	}

	public float[] calcDelta() {
		float[] delta = new float[2];

		int direction = 1;	//中心を左右どちらに移動させるか
		int contraction = 1;	//分散の収縮方向
		if(uniqueRnd.nextBoolean()) {
			direction = -1;
		}
		if(uniqueRnd.nextBoolean()) {
			contraction = -1;
		}

		float deltaM;	//中心変位(0.2以下)
		float deltaS;	//分散変位(0.1以下)

		if(uniqueRnd.nextBoolean()) {
			//分散だけ変更
			deltaM = 0f;
			deltaS = uniqueRnd.nextFloatII() / 100; //[0, 0.01]の範囲の乱数
		} else {
			//中心・分散どちらも変更
			deltaM = uniqueRnd.nextFloatII() / 20;	//[0, 0.05]の範囲の乱数
			deltaS = uniqueRnd.nextFloatII() / 100;	//[0, 0.01]の範囲の乱数
		}

		deltaM *= direction;
		deltaS *= contraction;

		delta[0] = deltaM;
		delta[1] = deltaS;

		return delta;
	}

	public void setBestFS(FS _best) {
		this.bestFS = _best;
	}

	public void outputFuzzyParams(String folderName, float[][][] fuzzyParams, SettingForGA setting) {
		String sep = File.separator;
		String dirName = setting.resultRoot + sep + folderName;
		File newdir = new File(dirName);
		newdir.mkdirs();

		String fileName = dirName + sep + "finalGene_FuzzySetParamator.csv";

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

}


























