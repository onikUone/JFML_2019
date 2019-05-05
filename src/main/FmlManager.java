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
	public void makeNewFuzzyParams() {
		//1. currentFMLからcontribute(もしくはFMLpopulation.fitness)によるバイナリトーナメントで親個体を二つ選択する
		//2. 親個体から一点交叉によってfuzzyParamsを生成する
		//3. 突然変異操作

	}
}
