package main;

import java.util.ArrayList;

public class FmlManager {

	//field
	MersenneTwisterFast uniqueRnd;

	int popSize;
	public ArrayList<FMLpopulation> currentFML = new ArrayList<FMLpopulation>();
	public ArrayList<FMLpopulation> newFML = new ArrayList<FMLpopulation>();
	public ArrayList<FMLpopulation> margeFML = new ArrayList<FMLpopulation>();

	int Ndim;
	int traDataSize;
	int evaDataSize;
	int tstDataSize;

	int ruleNum;

	int[][] count;

	//costructor
	public FmlManager(DataSetInfo tra, DataSetInfo tst, DataSetInfo eva, SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
		this.popSize = setting.popSize;
		this.Ndim = setting.Ndim;
		this.traDataSize = tra.getDataSize();
		this.evaDataSize = eva.getDataSize();
		this.tstDataSize = tst.getDataSize();
		this.ruleNum = setting.ruleNum;
	}

	//method
	public void generateInitialFML(SettingForGA setting) {
		int popSize = setting.popFML;

		//初期個体群生成
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			this.currentFML.add( new FMLpopulation(setting) );
			this.currentFML.get(pop_i).initializeFuzzyParams(setting);
			this.currentFML.get(pop_i).generateFS(setting);
		}
	}

	//fuzzyParams子個体 生成メソッド
	public void makeNewFML(SettingForGA setting) {
		int popSize = setting.popFML;
		for(int child_i = 0; child_i < popSize; child_i++) {
			//1. currentFMLからcontribute(もしくはFMLpopulation.fitness)によるバイナリトーナメントで親個体を二つ選択する
			this.crossOver(setting);
			//2. 親個体から一点交叉によってfuzzyParamsを生成する
			//3. 突然変異操作
		}
	}


	//子個体生成
	public void crossOver(SettingForGA setting) {
		int mom, dad;
		int Nmom, Ndad;

		this.newFML.clear();

		int popSize = setting.popFML;
		int Fdiv = setting.Fdiv;

		for(int  child_i = 0; child_i < popSize; child_i++) {
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
				this.newFML.add( new FMLpopulation(this.currentFML.get(parent), setting) );
			} else {
				Nmom = uniqueRnd.nextInt(Fdiv); //momから受け継ぐfuzzyParamsの数
				Ndad = Fdiv - Nmom;	//dadから受け継ぐfuzzyParamsの数

				int[] pmom = sampringWithout();

				//2親のfuzzyParamsからcontributeによるバイナリトーナメントで受け継ぐfuzzyParamsを決定する
				float[][][] newFuzzyParams = makeNewFuzzyParams(setting, this.currentFML.get(mom), this.currentFML.get(dad));

			}

		}
	}

	public float[][][]  makeNewFuzzyParams(SettingForGA setting, FMLpopulation mom, FMLpopulation dad) {
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;
		float[][][] newFuzzyParams = new float[Ndim][Fdiv][2];

		float[] parentsContribute = new float[Fdiv * 2];


		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				parentsContribute[div_i] = mom.contribute[dim_i][div_i];
			}
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				parentsContribute[div_i + Fdiv] = dad.contribute[dim_i][div_i];
			}

			for(int div_i = 0; div_i < Fdiv; div_i++) {

				int winner = 0;
				int select1, select2;
				//トーナメント出場者
				select1 = uniqueRnd.nextInt(Fdiv * 2);
				select2 = uniqueRnd.nextInt(Fdiv * 2);

				int optimizer = -1;	//最小化:1, 最大化:-1
				if( (optimizer * parentsContribute[select1]) < (optimizer * parentsContribute[select2]) ) {
					winner = select1;
				} else {
					winner = select2;
				}

				if(winner < Fdiv) {
					newFuzzyParams[dim_i][div_i][0] = mom.fuzzyParams[dim_i][winner][0];
					newFuzzyParams[dim_i][div_i][1] = mom.fuzzyParams[dim_i][winner][1];
				} else {
					newFuzzyParams[dim_i][div_i][0] = dad.fuzzyParams[dim_i][winner - Fdiv][0];
					newFuzzyParams[dim_i][div_i][1] = dad.fuzzyParams[dim_i][winner - Fdiv][1];
				}

			}
		}

		return newFuzzyParams;
	}

	public int binaryT4(SettingForGA setting) {
		int winner = 0;
		int select1, select2;

		//トーナメント出場者
		select1 = uniqueRnd.nextInt(setting.popFS);
		select2 = uniqueRnd.nextInt(setting.popFS);

		int optimizer = -1;	//最小化:1, 最大化:-1
		if( (optimizer * currentFML.get(select1).getFitness()) < (optimizer * currentFML.get(select2).getFitness()) ) {
			winner = select1;
		} else {
			winner = select2;
		}

		return winner;
	}
}






















