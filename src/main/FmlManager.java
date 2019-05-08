package main;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class FmlManager implements Serializable{

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
			//20190509_third
//			//1. currentFMLからcontribute(もしくはFMLpopulation.fitness)によるバイナリトーナメントで親個体を二つ選択する
			this.binaryT4Choice(setting);

			//20190508_first
			//1. currentFMLからバイナリトーナメントで親個体を二つ選択し，fuzzyParamsの実数値交叉を行う
//			this.crossOver(setting);

			//20190508_second
			//1. 一様交叉
//			this.uniformCrossover(setting);

			//20190509_fourth
			this.nsga2crossOver(setting);

			//2. 突然変異操作
			this.mutation(this.newFML, setting);
		}
	}

	//TODO 2目的最適化子個体生成
	public void nsga2crossOver(SettingForGA setting) {

	}

	//子個体生成（一様交叉）
	public void uniformCrossover(SettingForGA setting) {
		int mom, dad;

		this.newFML.clear();

		int popSize = setting.popFML;
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		for(int child_i = 0; child_i < popSize; child_i++) {
			//親選択
			mom = binaryT4(setting);
			dad = binaryT4(setting);

			if(uniqueRnd.nextDoubleIE() > setting.rateCrossOver) {
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
				float[][][] newFuzzyParams = new float[Ndim][Fdiv][2];
				boolean[][] mutationFlg = new boolean[Ndim][Fdiv];
				int parent;

				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					//一様交叉
					if(uniqueRnd.nextBoolean()) {
						parent = mom;
					} else {
						parent = dad;
					}

					for(int div_i = 0; div_i < Fdiv; div_i++) {
						newFuzzyParams[dim_i][div_i][0] = this.currentFML.get(parent).fuzzyParams[dim_i][div_i][0];
						newFuzzyParams[dim_i][div_i][1] = this.currentFML.get(parent).fuzzyParams[dim_i][div_i][1];
						mutationFlg[dim_i][div_i] = false;
					}
				}

				FMLpopulation newPop = new FMLpopulation(setting);
				newPop.setFuzzyParams(newFuzzyParams);
				newPop.setMutationFlg(mutationFlg);
				newPop.generateFS(setting);
				this.newFML.add(newPop);
			}

		}
	}

	//子個体生成（一点交叉）
	public void crossOver(SettingForGA setting) {
		int mom, dad;
		int Nmom, Ndad;

		this.newFML.clear();

		int popSize = setting.popFML;
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		for(int child_i = 0; child_i < popSize; child_i++) {
			//親選択
			mom = binaryT4(setting);
			dad = binaryT4(setting);

			if(uniqueRnd.nextDoubleIE() > setting.rateCrossOver) {
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
				int crossPoint = uniqueRnd.nextInt(setting.Ndim - 1) + 1;	//交叉点
				boolean firstParent = uniqueRnd.nextBoolean();	//先頭をどちらの親にするか
				float[][][] newFuzzyParams = new float[Ndim][Fdiv][2];
				boolean[][] mutationFlg = new boolean[Ndim][Fdiv];
				int parent;

				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					if(dim_i < crossPoint) {	//交叉前 先頭
						if(firstParent) {
							parent = mom;
						} else {
							parent = dad;
						}
					} else {	//交叉後 後尾
						if(firstParent) {
							parent = dad;
						} else {
							parent = mom;
						}
					}

					for(int div_i = 0; div_i < Fdiv; div_i++) {
						newFuzzyParams[dim_i][div_i][0] = this.currentFML.get(parent).fuzzyParams[dim_i][div_i][0];
						newFuzzyParams[dim_i][div_i][1] = this.currentFML.get(parent).fuzzyParams[dim_i][div_i][1];
						mutationFlg[dim_i][div_i] = false;
					}

				}

				FMLpopulation newPop = new FMLpopulation(setting);
				newPop.setFuzzyParams(newFuzzyParams);
				newPop.setMutationFlg(mutationFlg);
				newPop.generateFS(setting);
				this.newFML.add(newPop);
			}
		}
	}

	//子個体生成（受け継ぐfuzzyParamsをバイナリトーナメントで選択）
	public void binaryT4Choice(SettingForGA setting) {
		int mom, dad;
		int Nmom, Ndad;

		this.newFML.clear();

		int popSize = setting.popFML;
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		for(int  child_i = 0; child_i < popSize; child_i++) {
			//親選択
			mom = binaryT4(setting);	//mom個体のインデックス
			dad = binaryT4(setting);	//dad個体のインデックス

			if(uniqueRnd.nextDoubleIE() > setting.rateCrossOver) {
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

				//新しいfuzzyParamsの生成
				float[][][] newFuzzyParams = new float[Ndim][Fdiv][2];
				boolean[][] mutationFlg = new boolean[Ndim][Fdiv];

				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					Nmom = uniqueRnd.nextInt(Fdiv); //momから受け継ぐfuzzyParamsの数
					Ndad = Fdiv - Nmom;	//dadから受け継ぐfuzzyParamsの数

					//親から, contributeによるバイナリトーナメントで非復元抽出
					int[] pmom = sampringWithoutBinaryT4(setting, Nmom, dim_i, this.currentFML.get(mom));
					int[] pdad = sampringWithoutBinaryT4(setting, Ndad, dim_i, this.currentFML.get(dad));

					for(int mom_i = 0; mom_i < Nmom; mom_i++) {
						if(pmom[mom_i] < 0) {
							// 強制mutationは、-Fdivしているから、+Fdivすることで元のインデックスを復元できる
							newFuzzyParams[dim_i][mom_i][0] = this.currentFML.get(mom).fuzzyParams[dim_i][pmom[mom_i] + Fdiv][0];
							newFuzzyParams[dim_i][mom_i][1] = this.currentFML.get(mom).fuzzyParams[dim_i][pmom[mom_i] + Fdiv][1];
							mutationFlg[dim_i][mom_i] = true;
						} else {
							newFuzzyParams[dim_i][mom_i][0] = this.currentFML.get(mom).fuzzyParams[dim_i][pmom[mom_i]][0];
							newFuzzyParams[dim_i][mom_i][1] = this.currentFML.get(mom).fuzzyParams[dim_i][pmom[mom_i]][1];
							mutationFlg[dim_i][mom_i] = false;
						}
					}
					for(int dad_i = 0; dad_i < Ndad; dad_i++) {
						if(pdad[dad_i] < 0) {
							newFuzzyParams[dim_i][dad_i + Nmom][0] = this.currentFML.get(dad).fuzzyParams[dim_i][pdad[dad_i] + Fdiv][0];
							newFuzzyParams[dim_i][dad_i + Nmom][1] = this.currentFML.get(dad).fuzzyParams[dim_i][pdad[dad_i] + Fdiv][1];
							mutationFlg[dim_i][dad_i + Nmom] = true;
						} else {
							newFuzzyParams[dim_i][dad_i + Nmom][0] = this.currentFML.get(dad).fuzzyParams[dim_i][pdad[dad_i]][0];
							newFuzzyParams[dim_i][dad_i + Nmom][1] = this.currentFML.get(dad).fuzzyParams[dim_i][pdad[dad_i]][1];
							mutationFlg[dim_i][dad_i + Nmom] = false;
						}
					}
				}

				FMLpopulation newPop = new FMLpopulation(setting);
				newPop.setFuzzyParams(newFuzzyParams);
				newPop.setMutationFlg(mutationFlg);
				newPop.generateFS(setting);
				this.newFML.add(newPop);
			}

		}
	}

	//突然変異
	//同じfuzzyParamsを持っている場合(mutationFlg == true)は強制的に突然変異を行う
	public void mutation(ArrayList<FMLpopulation> fmlList, SettingForGA setting) {
		int popSize = fmlList.size();
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				for(int div_i = 0; div_i < Fdiv; div_i++) {
					if(fmlList.get(pop_i).mutationFlg[dim_i][div_i]) {
						fmlList.get(pop_i).mutationParams(setting, dim_i, div_i);
					} else if(uniqueRnd.nextDoubleIE() < setting.rateMutation) {
						fmlList.get(pop_i).mutationParams(setting, dim_i, div_i);
					}
				}
			}
		}

	}

	public void populationUpdate(SettingForGA setting) {
		//現世代 + 子世代 を marge
		this.margeFML.clear();
		for(int pop_i = 0; pop_i < this.currentFML.size(); pop_i++) {
			this.margeFML.add(this.currentFML.get(pop_i));
		}
		for(int pop_i = 0; pop_i < this.newFML.size(); pop_i++) {
			this.margeFML.add(this.newFML.get(pop_i));
		}
		this.currentFML.clear();
		this.newFML.clear();
		//fitnessの値が高い順にソート
		this.margeFML.sort(java.util.Comparator.comparing(FMLpopulation::getFitness).reversed());

		//fitnessの値が良い順にpopFMLだけ次世代に個体を格納
		for(int pop_i = 0; pop_i < setting.popFML; pop_i++) {
			this.currentFML.add( new FMLpopulation(this.margeFML.get(pop_i), setting) );
		}
	}

	public class fmlComparator implements Comparator<FMLpopulation>{
		@Override
		public int compare(FMLpopulation a, FMLpopulation b) {
			float no1 = a.getFitness();
			float no2 = b.getFitness();

			//降順でソート
			if(no1 > no2) {
				return 1;
			} else if(no1 == no2) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	//
	public int[] sampringWithoutBinaryT4(SettingForGA setting, int num, int dim, FMLpopulation parent) {
		int[] ans = new int[num];
		int count = 0;

		for(int i = 0; i < num; i++) {

			boolean isSame = false;

			//binary tournament
			int winner = 0;
			int select1, select2;
			select1 = uniqueRnd.nextInt(setting.Fdiv);
			select2 = uniqueRnd.nextInt(setting.Fdiv);
			int optimizer = -1;	//最小化:1, 最大化:-1
			if( (optimizer * parent.contribute[dim][select1]) < (optimizer * parent.contribute[dim][select2])) {
				winner = select1;
			} else {
				winner = select2;
			}
			ans[i] = winner;

			for(int j = 0; j < i; j++) {
				if(ans[i] == ans[j]) {
					isSame = true;
				}
			}

			if(count > 10) {
				//10回探したら強制的にそのままにしておく
				//同じファジィ集合の時は強制的に突然変異すること
				isSame = false;
				ans[i] -= setting.Fdiv;
			}

			if(isSame) {
				i--;
				count++;
			} else {
				count = 0;
			}
		}

		return ans;
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
		select1 = uniqueRnd.nextInt(setting.popFML);
		select2 = uniqueRnd.nextInt(setting.popFML);

		int optimizer = -1;	//最小化:1, 最大化:-1
		if( (optimizer * currentFML.get(select1).getFitness()) < (optimizer * currentFML.get(select2).getFitness()) ) {
			winner = select1;
		} else {
			winner = select2;
		}

		return winner;
	}

	public void calcRank(ArrayList<FMLpopulation> fmlList) {
		ArrayList<FMLpopulation> rankedList = new ArrayList<FMLpopulation>();

		int popSize = fmlList.size();

		//n_i[pop_i] : pop_i を 優越する個体の数
		int[] n_i = new int[popSize];

		@SuppressWarnings("unchecked")

		//S_i[pop_i] : pop_i が 優越する個体のインデックスをリストで保持
		ArrayList<Integer>[] S_i = new ArrayList[popSize];

		//rankが昇順になるようなruleSetsのインデックスリスト
		ArrayList<Integer> F_i = new ArrayList<Integer>();

		//優越ランク計算開始
		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			n_i[pop_i] = 0;
			S_i[pop_i] = new ArrayList<Integer>();

			for(int pop_j = 0; pop_j < popSize; pop_j++) {
				if(pop_i != pop_j) {
					if( isDominate(pop_i, pop_j, fmlList) ) {
						//pop_i が pop_j を優越する
						S_i[pop_i].add(pop_j);
					} else if(isDominate(pop_j, pop_i, fmlList)) {
						n_i[pop_i]++;
					}
				}
			}
			if(n_i[pop_i] == 0) {
				//pop_i を 優越する個体が存在しない
				fmlList.get(pop_i).setRank(0);
				rankedList.add(fmlList.get(pop_i));
				F_i.add(pop_i);
			}
		}

		//Crowding Distance 計算開始
		double firstMax = 0;
		double firstMin = 100;
		boolean isNormalize = false;
		ArrayList<Float> firstObj = new ArrayList<Float>();
		ArrayList<Float> nowFirst = new ArrayList<Float>();

		for(int pop_i = 0; pop_i < popSize; pop_i++) {
			firstObj.add(fmlList.get(pop_i).getFitnesses(0));
		}

		//全ランクについてCrowding Distanceを計算する
		int i = 0;
		calcDistance(rankedList);
		rankedList.clear();
		ArrayList<Integer> Q = new ArrayList<Integer>();	//対象とするランクの個体群を保持するリスト
		while(F_i.size() != 0) {
			for(int p = 0; p < F_i.size(); p++) {
				for(int q = 0; q < S_i[F_i.get(p)].size(); q++) {
					n_i[S_i[F_i.get(p)].get(q)] -= 1;
					if(n_i[S_i[F_i.get(p)].get(q)] == 0) {
						fmlList.get( S_i[F_i.get(p)].get(q) ).setRank(i + 1);
						Q.add(S_i[F_i.get(p)].get(q));
						rankedList.add(fmlList.get( S_i[F_i.get(p)].get(q) ));
					}
				}
			}
			if(rankedList.size() != 0) {
				calcDistance(rankedList);
			}
			rankedList.clear();
			i++;
			F_i.clear();
			for(int k = 0; k < Q.size(); k++) {
				F_i.add(Q.get(k));
			}
			Q.clear();
		}

	}

	public void calcDistance(ArrayList<FMLpopulation> fmlList) {
		int popSize = fmlList.size();
		//TODO
	}

	//pがqを優越しているかどうか
	protected boolean isDominate(int p, int q, ArrayList<FMLpopulation> fmlPop) {
		// Minimize fitness
		//if p dominate q then true
		//	else false

		boolean ans = false;
		//Minimize したい目的関数には i = 1, Maximize したい目的関数には i = -1, とすれば不等号をこのまま使える
		int[] i = new int[2];
		i[0] = -1;
		i[1] = 1;
		for(int o = 0; o < 2; o++) {
			if( i[o] * fmlPop.get(p).getFitnesses(o) > i[o] * fmlPop.get(q).getFitnesses(o)) {
				// どこか一つでも「p > q」 ならば pはqを優越しない
				ans = false;
				break;
			} else if(i[o] * fmlPop.get(p).getFitnesses(o) < i[o] * fmlPop.get(q).getFitnesses(o)) {
				ans = true;
			}
		}
		return ans;
	}

//	//子個体生成(親個体のcontributeが高いfuzzyParamsを基準に突然変異で探索)
//	public void choiceHighContribute(SettingForGA setting) {
//		int mom, dad;
//		int Nmom, Ndad;
//
//		this.newFML.clear();
//
//		int popSize = setting.popFML;
//		int Ndim = setting.Ndim;
//		int Fdiv = setting.Fdiv;
//
//		for(int child_i = 0; child_i < popSize; child_i++) {
//			//親選択
//			mom = binaryT4(setting);
//			dad = binaryT4(setting);
//
//			if(uniqueRnd.nextDoubleIE() > setting.rateCrossOver) {
//				//交叉操作を行わない場合
//				int parent;
//				if(uniqueRnd.nextBoolean()) {
//					parent = mom;
//				} else {
//					parent = dad;
//				}
//				//子個体生成
//				this.newFML.add( new FMLpopulation(this.currentFML.get(parent), setting) );
//			} else {
//				float[][][] testFuzzyParams = new float[Ndim][Fdiv][2];
//				float[][][] newFuzzyParams = new float[Ndim][Fdiv][2];
//				boolean[][] mutationFlg = new boolean[Ndim][Fdiv];
//
//				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
//					Nmom = uniqueRnd.nextInt(Fdiv);
//					Ndad = Fdiv - Nmom;
//
//					ArrayList<FuzzySet> momFuzzySet = new ArrayList<FuzzySet>();
//					ArrayList<FuzzySet> dadFuzzySet = new ArrayList<FuzzySet>();
//					ArrayList<FuzzySet> parentFuzzySet = new ArrayList<FuzzySet>();
//					for(int div_i = 0; div_i < Fdiv; div_i++) {
//						momFuzzySet.add(new FuzzySet(this.currentFML.get(mom).fuzzyParams[dim_i][div_i], this.currentFML.get(mom).contribute[dim_i][div_i]));
//						dadFuzzySet.add(new FuzzySet(this.currentFML.get(dad).fuzzyParams[dim_i][div_i], this.currentFML.get(dad).contribute[dim_i][div_i]));
//					}
//					for(int div_i = 0; div_i < Fdiv; div_i++) {
//						parentFuzzySet.add(new FuzzySet(this.currentFML.get(mom).fuzzyParams[dim_i][div_i], this.currentFML.get(mom).contribute[dim_i][div_i]));
//					}
//					for(int div_i = 0; div_i < Fdiv; div_i++) {
//						parentFuzzySet.add(new FuzzySet(this.currentFML.get(dad).fuzzyParams[dim_i][div_i], this.currentFML.get(dad).contribute[dim_i][div_i]));
//					}
//					momFuzzySet.sort(comparing(FuzzySet::getContribute).reversed());
//					dadFuzzySet.sort(comparing(FuzzySet::getContribute).reversed());
//					parentFuzzySet.sort(comparing(FuzzySet::getContribute).reversed());
//
//					for(int mom_i = 0; mom_i < Nmom; mom_i++) {
//						newFuzzyParams[dim_i][mom_i][0] = momFuzzySet.get(mom_i).fuzzyParam[0];
//						newFuzzyParams[dim_i][mom_i][1] = momFuzzySet.get(mom_i).fuzzyParam[1];
//						mutationFlg[dim_i][mom_i] = true;
//					}
//					for(int dad_i = 0; dad_i < Ndad; dad_i++) {
//						newFuzzyParams[dim_i][dad_i + Nmom][0] = dadFuzzySet.get(dad_i).fuzzyParam[0];
//						newFuzzyParams[dim_i][dad_i + Nmom][1] = dadFuzzySet.get(dad_i).fuzzyParam[1];
//						mutationFlg[dim_i][dad_i + Nmom] = true;
//					}
//					for(int div_i = 0; div_i < Fdiv; div_i++) {
//						testFuzzyParams[dim_i][div_i][0] = parentFuzzySet.get(div_i).fuzzyParam[0];
//						testFuzzyParams[dim_i][div_i][1] = parentFuzzySet.get(div_i).fuzzyParam[1];
//						mutationFlg[dim_i][div_i] = true;
//					}
//
//				}
//			}
//		}
//
//	}

//	public class fuzzySetComparator implements Comparator<FuzzySet>{
//		@Override
//		public int compare(FuzzySet a, FuzzySet b) {
//			float no1 = a.getContribute();
//			float no2 = b.getContribute();
//
//			//昇順でソート
//			if(no1 > no2) {
//				return 1;
//			} else if(no1 == no2) {
//				return 0;
//			} else {
//				return -1;
//			}
//		}
//	}
}






















