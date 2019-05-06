package main;

import static java.util.Comparator.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class FMLpopulation implements Serializable{

	//field
	float[][][] fuzzyParams;
	boolean[][] mutationFlg;

	public ArrayList<FS> currentFS = new ArrayList<FS>();
	public ArrayList<FS> newFS = new ArrayList<FS>();
	public ArrayList<FS> margeFS = new ArrayList<FS>();

	int Ndim;
	int Fdiv;
	int traDataSize;
	int evaDataSize;
	int tstDataSize;

	float[][] contribute;
	float fitness;	//contributeの総和

	MersenneTwisterFast uniqueRnd;


	//constructor
	public FMLpopulation() {}

	public FMLpopulation(SettingForGA setting) {
		this.Ndim = setting.Ndim;
		this.Fdiv = setting.Fdiv;
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());

		this.fuzzyParams = new float[Ndim][Fdiv][2];	//各属性にFdiv種類のファジィ集合を定める（mとs）
		this.mutationFlg = new boolean[Ndim][Fdiv];
	}


	//Deep Copy
	public FMLpopulation(FMLpopulation oldFML, SettingForGA setting) {
		this.Ndim = oldFML.Ndim;
		this.Fdiv = oldFML.Fdiv;
		this.traDataSize = oldFML.traDataSize;
		this.evaDataSize = oldFML.evaDataSize;
		this.tstDataSize = oldFML.tstDataSize;

		this.fuzzyParams = new float[Ndim][Fdiv][2];
		this.mutationFlg = new boolean[Ndim][Fdiv];
		this.contribute = new float[Ndim][Fdiv];
		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				this.fuzzyParams[dim_i][div_i][0] = oldFML.fuzzyParams[dim_i][div_i][0];
				this.fuzzyParams[dim_i][div_i][1] = oldFML.fuzzyParams[dim_i][div_i][1];

				this.mutationFlg[dim_i][div_i] = oldFML.mutationFlg[dim_i][div_i];

				this.contribute[dim_i][div_i] = oldFML.contribute[dim_i][div_i];
			}
		}
		this.uniqueRnd = new MersenneTwisterFast(oldFML.uniqueRnd.nextInt());
		this.fitness = oldFML.fitness;

		int popSize = oldFML.currentFS.size();
		this.currentFS.clear();
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			this.currentFS.add(new FS(oldFML.currentFS.get(pop_i), setting));
		}
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

	//Deep Copy
	public void setFuzzyParams(float[][][] _fuzzyParams) {
		int Ndim = _fuzzyParams.length;
		int Fdiv = _fuzzyParams[0].length;
		this.fuzzyParams = new float[Ndim][Fdiv][2];

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				this.fuzzyParams[dim_i][div_i][0] = _fuzzyParams[dim_i][div_i][0];
				this.fuzzyParams[dim_i][div_i][1] = _fuzzyParams[dim_i][div_i][1];
			}
		}

	}

	//DeepCopy
	public void setMutationFlg(boolean[][] _mutationFlg) {
		int Ndim = _mutationFlg.length;
		int Fdiv = _mutationFlg[0].length;
		this.mutationFlg = new boolean[Ndim][Fdiv];

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				this.mutationFlg[dim_i][div_i] = _mutationFlg[dim_i][div_i];
			}
		}

	}

	//fuzzyParams[dim][div]の突然変異
	public void mutationParams(SettingForGA setting, int dim, int div) {
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
			deltaS = uniqueRnd.nextFloatII() / 10; //[0, 0.1]の範囲の乱数
		} else {
			//中心・分散どちらも変更
			deltaM = uniqueRnd.nextFloatII() * 2 / 10;	//[0, 0.2]の範囲の乱数
			deltaS = uniqueRnd.nextFloatII() / 10;
		}

		deltaM *= direction;
		deltaS *= contraction;

		this.fuzzyParams[dim][div][0] += deltaM;
		this.fuzzyParams[dim][div][1] += deltaS;

	}

	public void setContribute(float[][] _contribute) {
		int Ndim = _contribute.length;
		int Fdiv = _contribute[0].length;
		this.contribute = new float[Ndim][Fdiv];

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				this.contribute[dim_i][div_i] = _contribute[dim_i][div_i];
			}
		}
	}

	public void calcFitness() {
		//contributeの総和をfitnessとする
		this.fitness = 0f;
		int Ndim = this.contribute.length;
		int Fdiv = this.contribute[0].length;

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				this.fitness += this.contribute[dim_i][div_i];
			}
		}
	}

	public float getFitness() {
		return this.fitness;
	}

	//子個体生成
	public void crossOver(SettingForGA setting) {
		int mom, dad;
		int Nmom, Ndad;

		this.newFS.clear();

		int popSize = setting.popFS;
		for(int child_i = 0; child_i < popSize; child_i++) {
			//親選択
			mom = binaryT4(setting);	//mom個体のインデックス
			dad = binaryT4(setting);	//dad個体のインデックス

			if(uniqueRnd.nextDoubleIE() < setting.rateCrossOver) {
				//交叉操作を行わない場合
				int parent;
				if(uniqueRnd.nextBoolean()) {
					parent = mom;
				} else {
					parent = dad;
				}
				//子個体生成
				this.newFS.add( new FS(this.currentFS.get(parent), setting) );	//DeepCopy
			} else {
				//交叉操作を行う(Pittsburgh型)
				Nmom = uniqueRnd.nextInt(this.currentFS.get(mom).ruleNum) + 1;	//momから取り出すルールの個数を選択
				Ndad = uniqueRnd.nextInt(this.currentFS.get(dad).ruleNum) + 1;	//dadから取り出すルールの個数を選択

				//子個体のルール数がruleMaxを超えないように調整
				if( (Nmom + Ndad) > setting.ruleMax ) {
					int delNum = Nmom + Ndad -  setting.ruleMin;	//減らすルール数
					for(int i = 0; i < delNum; i++) {
						if(Ndad <= 0) {
							Nmom--;
							continue;
						} else if(Nmom <= 0) {
							Ndad--;
							continue;
						}
						if(uniqueRnd.nextBoolean()) {
							Nmom--;
						} else {
							Ndad--;
						}
					}
				}

				int[] pmom = sampringWithout(Nmom, this.currentFS.get(mom).ruleNum);
				int[] pdad = sampringWithout(Ndad, this.currentFS.get(dad).ruleNum);

				//子個体生成
				this.newFS.add( new FS(setting) );
				this.newFS.get(child_i).setFuzzyParams(this.currentFS.get(mom).fuzzyParams);
				this.newFS.get(child_i).setRuleNum(Nmom + Ndad);
				this.newFS.get(child_i).resetConcList();
				for(int mom_i = 0; mom_i < Nmom; mom_i++) {
					this.newFS.get(child_i).deepAddRule(this.currentFS.get(mom).rules.get(pmom[mom_i]));
				}
				for(int dad_i = 0; dad_i < Ndad; dad_i++) {
					this.newFS.get(child_i).deepAddRule(this.currentFS.get(dad).rules.get(pdad[dad_i]));
				}
				this.newFS.get(child_i).makeFS(setting);
			}
		}
	}

	public void mutation(SettingForGA setting) {
		int popSize = this.newFS.size();

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			int ruleNum = this.newFS.get(pop_i).ruleNum;

			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				if(uniqueRnd.nextDoubleIE() < setting.rateMutation) {
					int mutDim = uniqueRnd.nextInt(setting.Ndim);
					this.newFS.get(pop_i).mutation(rule_i, mutDim, setting);
				}
			}

		}
	}

	public void populationUpdate(SettingForGA setting) {
		//現世代 + 子世代 を marge
		this.margeFS.clear();
		for(int pop_i = 0; pop_i < this.currentFS.size(); pop_i++) {
			this.margeFS.add(this.currentFS.get(pop_i));
		}
		for(int pop_i = 0; pop_i < this.newFS.size(); pop_i++) {
			this.margeFS.add(this.newFS.get(pop_i));
		}
		this.currentFS.clear();
		this.newFS.clear();
		//fitnessの値が低い順にソート
		this.margeFS.sort(comparing(FS::getFitness));

		//fitnessの値が良い順にpopFSだけ次世代に個体を格納
		for(int pop_i = 0; pop_i < setting.popFS; pop_i++) {
			this.currentFS.add( new FS(this.margeFS.get(pop_i), setting) );
		}
	}

	public class fsComparator implements Comparator<FS>{
		@Override
		public int compare(FS a, FS b) {
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


	public int[] sampringWithout(int num, int ruleNum) {
		int[] ans = new int[num];

		for(int i = 0; i < num; i++) {
			boolean isSame = false;
			ans[i] = uniqueRnd.nextInt(ruleNum);
			for(int j = 0; j < i; j++) {
				if(ans[i] == ans[j]) {
					isSame = true;
				}
			}
			if(isSame) {
				i--;
			}
		}

		return ans;
	}

	public int binaryT4(SettingForGA setting) {
		int winner = 0;
		int select1, select2;

		//トーナメント出場者
		select1 = uniqueRnd.nextInt(setting.popFS);
		select2 = uniqueRnd.nextInt(setting.popFS);

		int optimizer = 1;	//最小化:1, 最大化:-1
		if( (optimizer * currentFS.get(select1).getFitness()) < (optimizer * currentFS.get(select2).getFitness()) ) {
			winner = select1;
		} else {
			winner = select2;
		}

		return winner;
	}


	//現世代個体FML出力
//	public void outputCurrentFML(String folderName, int nowGene, SettingForGA setting) {
//		//ディレクトリ生成
//		String sep = File.separator;
//		String dirName =  setting.resultFileName + sep + folderName;
//		File newdir = new File(dirName);
//		newdir.mkdirs();
//
//		for(int pop_i = 0; pop_i < this.currentFS.size(); pop_i++) {
//			String fileName = dirName + sep +
//							"gene" + String.valueOf(nowGene) +
//							"_pop" + String.valueOf(pop_i) +
//							".xml";
//			File xml = new File(fileName);
//			JFML.writeFSTtoXML(this.currentFS.get(pop_i).fs, xml);
//		}
//	}

//	public void outputCurrentMSE(String folderName, int nowGene, DataSetInfo tst, SettingForGA setting) {
//		//ディレクトリ生成
//		String sep = File.separator;
//		String dirName = setting.resultFileName + sep + folderName;
//		File newdir = new File(dirName);
//		newdir.mkdirs();
//
//		int popSize = setting.popFS;
//		float[] mse = new float[popSize];
//		float[] y;
//		for(int pop_i = 0; pop_i < popSize; pop_i++) {
//			y = this.currentFS.get(pop_i).reasoning(setting, tst);
//			mse[pop_i] = FmlGaManager.calcMSE(y, tst);
//		}
//		String fileName = dirName + sep +
//				"gene" + String.valueOf(nowGene) +
//				"_tstMSE.csv";
//		try {
//			Output.writeArray(mse, fileName);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//
//	}

//	public void outputFuzzyParams(int pop_i) {
//		String fileName = "results/gaussian/test" + String.valueOf(pop_i) + ".csv";
//		try {
//			FileWriter fw = new FileWriter(fileName, true);
//			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
//
//			for(int dim_i = 0; dim_i < this.Ndim; dim_i++) {
//				for(int i = 0; i < 2; i++) {
//					for(int div_i = 0; div_i < this.Fdiv; div_i++) {
//						pw.print(this.fuzzyParams[dim_i][div_i][i] + ",");
//					}
//					pw.println();
//				}
//				pw.println();
//			}
//
//			pw.close();
//		} catch(IOException e) {
//			e.printStackTrace();
//		}
//	}


}
