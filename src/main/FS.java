package main;

import java.util.ArrayList;
import java.util.Arrays;

import jfml.FuzzyInferenceSystem;
import jfml.knowledgebase.KnowledgeBaseType;
import jfml.knowledgebase.variable.FuzzyVariableType;
import jfml.knowledgebase.variable.KnowledgeBaseVariable;
import jfml.knowledgebase.variable.TskVariableType;
import jfml.rule.AntecedentType;
import jfml.rule.ClauseType;
import jfml.rule.TskConsequentType;
import jfml.rule.TskFuzzyRuleType;
import jfml.rulebase.TskRuleBaseType;
import jfml.term.FuzzyTermType;
import jfml.term.TskTermType;

public class FS{

	//field
	public FuzzyInferenceSystem fs;
	int ruleNum;
	float[][][] fuzzyParams;
	public ArrayList<int[]> rules = new ArrayList<int[]>();
	float[] concList;
	boolean[] coveredFlg;
	int[][] count;

	float fitness = 100f;

	ArrayList<Pattern> heuristicList = new ArrayList<Pattern>();

	MersenneTwisterFast uniqueRnd;


	//constructor
	public FS() {}

	public FS(SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
	}

	//DeepCopy
	public FS(FS oldFS, SettingForGA setting){
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
		setFuzzyParams(oldFS.fuzzyParams);
		setRuleNum(oldFS.ruleNum);
		this.concList = new float[oldFS.ruleNum];
		this.coveredFlg = new boolean[oldFS.ruleNum];
		for(int rule_i = 0; rule_i < oldFS.ruleNum; rule_i++) {
			deepAddRule(oldFS.rules.get(rule_i));
			this.concList[rule_i] = oldFS.concList[rule_i];

			this.coveredFlg[rule_i] = oldFS.coveredFlg[rule_i];
		}
//		this.count = new int[oldFS.count.length][oldFS.count[0].length];
//		for(int dim_i = 0; dim_i < this.count.length; dim_i++) {
//			for(int div_i = 0; div_i < this.count[0].length; div_i++) {
//				this.count[dim_i][div_i] = oldFS.count[dim_i][div_i];
//			}
//		}
		this.fitness = oldFS.fitness;
		makeFS(setting);
	}


	//method

	//dim次元目のルールがdivの部分をdon't careにしたときのMSEを計算
	public float calcContribute(int _dim, int _div, SettingForGA setting, DataSetInfo tra, DataSetInfo eva) {

		FS fs = new FS(this, setting);
		int ruleNum = fs.rules.size();
		int Ndim = setting.Ndim;

		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			//don't careに変更
			if(fs.rules.get(rule_i)[_dim] == _div) {
				fs.rules.get(rule_i)[_dim] = -1;
			}
		}

		fs.makeFS(setting);
		fs.calcConclusion(setting, tra);
		float[] y = fs.reasoning(setting, eva);
		float mse = FmlGaManager.calcMSE(y, eva);

		return mse;
	}

	public void countFuzzySet(SettingForGA setting) {
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;
		int ruleNum = this.rules.size();

		this.count = new int[Ndim][Fdiv];
		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			Arrays.fill(this.count[dim_i], 0);
		}

		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {

			for(int dim_i = 0; dim_i < Ndim; dim_i++) {

				int set = this.rules.get(rule_i)[dim_i];
				this.count[dim_i][set]++;

			}

		}

	}

	public void deepAddRule(int[] rule) {
		int[] newRule = new int[rule.length];
		for(int i = 0; i < rule.length; i++) {
			newRule[i] = rule[i];
		}
		this.rules.add(newRule);
	}

	public void setRuleNum(int _ruleNum) {
		this.ruleNum = _ruleNum;
	}

	public void resetConcList() {
		this.concList = new float[this.ruleNum];
		Arrays.fill(this.concList, 0.5f);
		this.coveredFlg = new boolean[this.ruleNum];
	}

	public void mutation(int rule_i, int mutDim, SettingForGA setting) {
		int newFuzzySet = 0;
		int count = 0;
		do {
			if(count > 10) {
				break;
			}

			newFuzzySet = uniqueRnd.nextInt(setting.Fdiv);
		} while(newFuzzySet == this.rules.get(rule_i)[mutDim]);

		this.rules.get(rule_i)[mutDim] = newFuzzySet;

	}

	public void mutation2(int rule_i, int mutDim, SettingForGA setting) {
		int newFuzzySet = 0;
		int count = 0;
		do {
			if(count > 10) {
				break;
			}

			newFuzzySet = uniqueRnd.nextInt(setting.Fdiv + 1);	//Don't Careを含む
		} while(newFuzzySet == this.rules.get(rule_i)[mutDim]);

		this.rules.get(rule_i)[mutDim] = newFuzzySet;

	}

	public void generateRuleIdx(SettingForGA setting) {
		//[ruleMin, ruleMax]のランダム値
		this.ruleNum = this.uniqueRnd.nextInt(setting.ruleMax - setting.ruleMin) + setting.ruleMin;

		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			int[] rule = new int[setting.Ndim];
			//ruleIdx 生成
			for(int dim_i = 0; dim_i < setting.Ndim; dim_i++) {
				rule[dim_i] = this.uniqueRnd.nextInt(setting.Fdiv);
			}
			this.rules.add(rule);
		}

		this.concList = new float[ruleNum];
		Arrays.fill(this.concList, 0.5f);
		this.coveredFlg = new boolean[ruleNum];
	}

	public void generateRuleIdx2(SettingForGA setting) {
		this.ruleNum = setting.ruleNum;

		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			int[] rule = new int[setting.Ndim];
			//ruleIdx 生成
			for(int dim_i = 0; dim_i < setting.Ndim; dim_i++) {
				rule[dim_i] = this.uniqueRnd.nextInt(setting.Fdiv + 1);	//Don't care含む
			}
			this.rules.add(rule);
		}

		this.concList = new float[ruleNum];
		Arrays.fill(this.concList, 0.5f);
		this.coveredFlg = new boolean[ruleNum];
	}

	public void generateRuleIdx3(SettingForGA setting, DataSetInfo tra) {
		this.ruleNum = 0;

		this.heuristicGenerateRules(setting, tra);

		this.concList = new float[ruleNum];
		Arrays.fill(this.concList, 0.5f);
		this.coveredFlg = new boolean[ruleNum];
	}


	public void setFuzzyParams(float[][][] _fuzzyParams) {
		this.fuzzyParams = _fuzzyParams;
	}

	public void deepFuzzyParams(float[][][] _fuzzyParams) {
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

	//this.rules:ArrayList<int[]>からFMLを生成するメソッド
	public void makeFS(SettingForGA setting) {
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;

		this.fs = new FuzzyInferenceSystem();

		//KnowledgeBase ***********************************************
		KnowledgeBaseType kb = new KnowledgeBaseType();

		//inputVariable
		FuzzyVariableType[] inputVariable = new FuzzyVariableType[Ndim];

		if(Ndim == 7) {
			inputVariable[0] = new FuzzyVariableType("MoveNo", 0, 1);
			inputVariable[1] = new FuzzyVariableType("DBSN", 0, 1);
			inputVariable[2] = new FuzzyVariableType("DWSN", 0, 1);
			inputVariable[3] = new FuzzyVariableType("DBWR", 0, 1);
			inputVariable[4] = new FuzzyVariableType("DWWR", 0, 1);
			inputVariable[5] = new FuzzyVariableType("DBTMR", 0, 1);
			inputVariable[6] = new FuzzyVariableType("DWTMR", 0, 1);
		} else if(Ndim == 6) {
			//Ndim = 6 , MoveNo無しversion
			inputVariable[0] = new FuzzyVariableType("DBSN", 0, 1);
			inputVariable[1] = new FuzzyVariableType("DWSN", 0, 1);
			inputVariable[2] = new FuzzyVariableType("DBWR", 0, 1);
			inputVariable[3] = new FuzzyVariableType("DWWR", 0, 1);
			inputVariable[4] = new FuzzyVariableType("DBTMR", 0, 1);
			inputVariable[5] = new FuzzyVariableType("DWTMR", 0, 1);
		}

		//outputVariable
		TskVariableType EBWR = new TskVariableType("EBWR");
		EBWR.setType("output");
		EBWR.setCombination("WA");


		//Fuzzy Set for Input Variable (= FuzzyTerm)
		FuzzyTermType[][] gaussians = new FuzzyTermType[Ndim][Fdiv];
		String[] name = setting.fuzzySetName;

		FuzzyTermType dontCare = new FuzzyTermType("Don't Care", FuzzyTermType.TYPE_rectangularShape, new float[] {0f, 1f});

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			for(int div_i = 0; div_i < Fdiv; div_i++) {
				gaussians[dim_i][div_i] = new FuzzyTermType(name[div_i], FuzzyTermType.TYPE_gaussianShape, this.fuzzyParams[dim_i][div_i]);
				inputVariable[dim_i].addFuzzyTerm(gaussians[dim_i][div_i]);
			}
			inputVariable[dim_i].addFuzzyTerm(dontCare);
			kb.addVariable(inputVariable[dim_i]);
		}

		//Fuzzy Set for Output Variable
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			EBWR.addTskTerm( new TskTermType("Conclusion" + String.valueOf(rule_i), 0, new float[] {this.concList[rule_i]} ) );
		}
		kb.addVariable(EBWR);

		this.fs.setKnowledgeBase(kb);
		// ************************************************************

		if(this.rules.size() == 0) {
			return;
		}
		//RuleBase ****************************************************
		TskRuleBaseType ruleBase = new TskRuleBaseType();
		ruleBase.setActivationMethod("PROD");
		ruleBase.setAndMethod("PROD");
		//rule initialize
		TskFuzzyRuleType rule;
		AntecedentType ant;
		TskConsequentType con;
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {

			//前件部 生成
			ant = new AntecedentType();
			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				if(this.rules.get(rule_i)[dim_i] == -1 || this.rules.get(rule_i)[dim_i] == Fdiv) {
					ant.addClause(new ClauseType(inputVariable[dim_i], dontCare));
					continue;
				}
				ant.addClause(new ClauseType(inputVariable[dim_i], gaussians[dim_i][this.rules.get(rule_i)[dim_i]]));
			}

			//後件部 生成
			con = new TskConsequentType();
			con.addTskThenClause(EBWR, EBWR.getTerms().get(rule_i));

			rule = new TskFuzzyRuleType("rule" + String.valueOf(rule_i), "and", "PROD", 1.0f);
			rule.setAntecedent(ant);
			rule.setTskConsequent(con);

			ruleBase.addTskRule(rule);

		}

		this.fs.addRuleBase(ruleBase);
		// ************************************************************
	}


	//結論部の学習メソッド
	public void calcConclusion(SettingForGA setting, DataSetInfo tra) {
		int dataSize = tra.getDataSize();
		int Ndim = setting.Ndim;
		int ruleNum = this.rules.size();

		boolean[][][] alphaFlg = new boolean[dataSize][ruleNum][Ndim];

		Pattern[] lines = new Pattern[dataSize];
		float[] y = new float[dataSize];
		float diff;
		float memberSum;
		float[][] memberships = new float[dataSize][ruleNum];
		float[] newConcList = new float[ruleNum];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable output;

		if(Ndim == 7) {
			input[0] = this.fs.getVariable("MoveNo");
			input[1] = this.fs.getVariable("DBSN");
			input[2] = this.fs.getVariable("DWSN");
			input[3] = this.fs.getVariable("DBWR");
			input[4] = this.fs.getVariable("DWWR");
			input[5] = this.fs.getVariable("DBTMR");
			input[6] = this.fs.getVariable("DWTMR");
		} else if(Ndim == 6) {
			//Ndim = 6 , MoveNo無しversion
			input[0] = this.fs.getVariable("DBSN");
			input[1] = this.fs.getVariable("DWSN");
			input[2] = this.fs.getVariable("DBWR");
			input[3] = this.fs.getVariable("DWWR");
			input[4] = this.fs.getVariable("DBTMR");
			input[5] = this.fs.getVariable("DWTMR");
		}

		for(int data_i = 0; data_i < dataSize; data_i++) {
			lines[data_i] = tra.getPattern(data_i);

			if(Ndim == 7) {
				if(lines[data_i].getDimValue(2) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue(lines[data_i].getDimValue(dim_i));
					}
				} else {
					input[0].setValue(lines[data_i].getDimValue(0));
					input[1].setValue(lines[data_i].getDimValue(1));
					input[2].setValue(lines[data_i].getDimValue(1));
					input[3].setValue(lines[data_i].getDimValue(3));
					input[4].setValue(1f - lines[data_i].getDimValue(3));
					input[5].setValue(lines[data_i].getDimValue(5));
					input[6].setValue(lines[data_i].getDimValue(5));
				}
			} else if(Ndim == 6) {
				if(lines[data_i].getDimValue(1) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue(lines[data_i].getDimValue(dim_i));
					}
				} else {
					//Ndim = 6 , MoveNo無しversion
					input[0].setValue(lines[data_i].getDimValue(0));
					input[1].setValue(lines[data_i].getDimValue(0));
					input[2].setValue(lines[data_i].getDimValue(2));
					input[3].setValue(1f - lines[data_i].getDimValue(2));
					input[4].setValue(lines[data_i].getDimValue(4));
					input[5].setValue(lines[data_i].getDimValue(4));
				}
			}



			this.fs.evaluate();
			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				//読み込んだデータに対してのメンバシップ値を保持
				memberships[data_i][rule_i] = ((TskVariableType) this.fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
				if(memberships[data_i][rule_i] > setting.alphaCut) {
					this.coveredFlg[rule_i] = true;
				}
				//現在の結論部の値を保持
				newConcList[rule_i] = ((TskVariableType) this.fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getZ();
			}
		}

		//学習計算開始
		for(int gene_i = 0; gene_i < setting.calcGeneration; gene_i++) {
			for(int data_i = 0; data_i < dataSize; data_i++) {
				memberSum = 0f;
				y[data_i] = 0f;
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					memberSum += memberships[data_i][rule_i];
					y[data_i] += memberships[data_i][rule_i] * newConcList[rule_i];
				}
				y[data_i] /= memberSum;

				//修正量計算
				diff = lines[data_i].getY() - y[data_i];
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					newConcList[rule_i] += setting.eta * diff * memberships[data_i][rule_i] / memberSum;
				}
			}
		}

		//concList更新
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			this.concList[rule_i] = newConcList[rule_i];
		}

		this.makeFS(setting);
	}

	public void alphaCut(SettingForGA setting, DataSetInfo tra) {
		int dataSize = tra.getDataSize();
		int Ndim = setting.Ndim;
		int ruleNum = this.rules.size();

//		//test
//		int dataSize = 2;
//		int Ndim = 3;
//		int ruleNum = 3;
//		boolean[][][] alphaFlg = {	//Pattern1
//		{{true, true, true},	//rule1
//		 {true, true, true},	//rule2
//		 {true, true, true}},	//rule3
//
//		//Pattern2
//		{{false, false, false},	//rule1
//		 {true, true, false},	//rule2
//		 {true, true, true}}	//rule3
//		 };

		boolean[][][] alphaFlg = new boolean[dataSize][ruleNum][Ndim];

		float membership;

		Pattern[] lines = new Pattern[dataSize];

		for(int data_i = 0; data_i < dataSize; data_i++) {
			lines[data_i] = tra.getPattern(data_i);
			float[] x = new float[Ndim];

			if(lines[data_i].getDimValue(1) >= 0) {
				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					x[dim_i] = lines[data_i].getDimValue(dim_i);
				}
			} else {	//欠損値処理
				x[0] = lines[data_i].getDimValue(0);
				x[1] = lines[data_i].getDimValue(0);
				x[2] = lines[data_i].getDimValue(2);
				x[3] = lines[data_i].getDimValue(2);
				x[4] = lines[data_i].getDimValue(4);
				x[5] = lines[data_i].getDimValue(4);
			}

			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				for(int dim_i = 0; dim_i < Ndim; dim_i++) {
					membership = this.fs.getKnowledgeBase()
							.getVariable(setting.dimName[dim_i])
							.getTerm(setting.fuzzySetName[this.rules.get(rule_i)[dim_i]])
							.getMembershipValue(x[dim_i]);
					alphaFlg[data_i][rule_i][dim_i] = (membership >= setting.alpha);
				}
			}
		}

		boolean[][] alphaTable = new boolean[dataSize][ruleNum];
		for(int data_i = 0; data_i < dataSize; data_i++) {
			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				alphaTable[data_i][rule_i] = alphaFlg[data_i][rule_i][0];
				for(int dim_i = 1; dim_i < Ndim; dim_i++) {
					alphaTable[data_i][rule_i] = (alphaTable[data_i][rule_i] && alphaFlg[data_i][rule_i][dim_i]);
				}
			}
		}

		//削除するルールのインデックス保持
		ArrayList<Integer> delRuleIdx = new ArrayList<Integer>();
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			boolean delRuleFlg = alphaTable[0][rule_i];
			for(int data_i = 1; data_i < dataSize; data_i++) {
				delRuleFlg = (delRuleFlg || alphaTable[data_i][rule_i]);
			}
			delRuleFlg = !delRuleFlg;

			if(delRuleFlg) {
				delRuleIdx.add(rule_i);
			}
		}

		ArrayList<int[]> newRules = new ArrayList<int[]>();
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			if(delRuleIdx.size() != 0) {
				if(rule_i == delRuleIdx.get(0)) {
					//rule_iが消すルールだったとき
					delRuleIdx.remove(0);
					this.ruleNum--;
				} else {
					//rule_iが消さないルールだった時
					newRules.add(this.rules.get(rule_i));
				}
			} else {
				//rule_iが消さないルールだった時
				newRules.add(this.rules.get(rule_i));
			}
		}
		this.rules = newRules;

		//ヒューリスティック生成する必要のあるパターン保持
		this.heuristicList = new ArrayList<Pattern>();
		for(int data_i = 0; data_i < dataSize; data_i++) {
			boolean savePatternFlg = alphaTable[data_i][0];
			for(int rule_i = 1; rule_i < ruleNum; rule_i++) {
				savePatternFlg = (savePatternFlg || alphaTable[data_i][rule_i]);
			}
			savePatternFlg = !savePatternFlg;

			if(savePatternFlg) {
				this.heuristicList.add(lines[data_i]);
			}
		}

		//新しいルール集合でFuzzyInferenceSystemを生成しておく
//		resetConcList();
//		makeFS(setting);

	}

	public void heuristicGenerateRules(SettingForGA setting, DataSetInfo tra) {
		int Nrule;
		float dcRate = 2f / (float)setting.Ndim;	//don't care確率
		if(this.ruleNum < setting.ruleNum) {
			Nrule = setting.ruleNum - this.ruleNum;
			this.makeFS(setting);


//			int[] pPattern = sampringWithout(Nrule, heuristicList.size());
			int[] pPattern;
			Pattern line;

			for(int rule_i = 0; rule_i < Nrule; rule_i++) {
				int idx;
				if(this.heuristicList.size() != 0) {
					idx = uniqueRnd.nextInt(this.heuristicList.size());
					line = this.heuristicList.get(idx);
					this.heuristicList.remove(idx);
				} else {
					idx = uniqueRnd.nextInt(tra.getDataSize());
					line = tra.getPattern(idx);
				}

				int[] newRule = heuristicRule(line, setting);
				for(int dim_i = 0; dim_i < setting.Ndim; dim_i++) {
					if(uniqueRnd.nextDoubleIE() < dcRate) {
						newRule[dim_i] = setting.Fdiv;	//don't careにする
					}
				}
				this.rules.add(newRule);
				this.ruleNum++;
			}
		}
	}

	public int[] heuristicRule(Pattern pattern, SettingForGA setting) {
		int Ndim = setting.Ndim;
		int Fdiv = setting.Fdiv;
		int[] newRule = new int[Ndim];
		float max;
		float membership;
		int maxIdx;

		for(int dim_i = 0; dim_i < Ndim; dim_i++) {
			max = this.fs.getKnowledgeBase()
					.getVariable(setting.dimName[dim_i])
					.getTerm(setting.fuzzySetName[0])
					.getMembershipValue(pattern.getDimValue(dim_i));
			maxIdx = 0;
			for(int div_i = 1; div_i < Fdiv; div_i++) {
				membership = this.fs.getKnowledgeBase()
						.getVariable(setting.dimName[dim_i])
						.getTerm(setting.fuzzySetName[div_i])
						.getMembershipValue(pattern.getDimValue(dim_i));
				if(membership > max) {
					max = membership;
					maxIdx = div_i;
				}
			}
			newRule[dim_i] = maxIdx;
		}

		return newRule;
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

	public void calcContinueConclusion(SettingForGA setting, int interval, DataSetInfo tra) {
		int dataSize = tra.getDataSize();
		int Ndim = setting.Ndim;
		int ruleNum = this.rules.size();

		Pattern[] lines = new Pattern[dataSize];
		float[] y = new float[dataSize];
		float diff;
		float memberSum;
		float[][] memberships = new float[dataSize][ruleNum];
		float[] newConcList = new float[ruleNum];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable output;

		if(Ndim == 7) {
			input[0] = this.fs.getVariable("MoveNo");
			input[1] = this.fs.getVariable("DBSN");
			input[2] = this.fs.getVariable("DWSN");
			input[3] = this.fs.getVariable("DBWR");
			input[4] = this.fs.getVariable("DWWR");
			input[5] = this.fs.getVariable("DBTMR");
			input[6] = this.fs.getVariable("DWTMR");
		} else if(Ndim == 6) {
			//Ndim = 6 , MoveNo無しversion
			input[0] = this.fs.getVariable("DBSN");
			input[1] = this.fs.getVariable("DWSN");
			input[2] = this.fs.getVariable("DBWR");
			input[3] = this.fs.getVariable("DWWR");
			input[4] = this.fs.getVariable("DBTMR");
			input[5] = this.fs.getVariable("DWTMR");
		}

		for(int data_i = 0; data_i < dataSize; data_i++) {
			lines[data_i] = tra.getPattern(data_i);

			if(Ndim == 7) {
				if(lines[data_i].getDimValue(2) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue(lines[data_i].getDimValue(dim_i));
					}
				} else {
					input[0].setValue(lines[data_i].getDimValue(0));
					input[1].setValue(lines[data_i].getDimValue(1));
					input[2].setValue(lines[data_i].getDimValue(1));
					input[3].setValue(lines[data_i].getDimValue(3));
					input[4].setValue(1f - lines[data_i].getDimValue(3));
					input[5].setValue(lines[data_i].getDimValue(5));
					input[6].setValue(lines[data_i].getDimValue(5));
				}
			} else if(Ndim == 6) {
				if(lines[data_i].getDimValue(1) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue(lines[data_i].getDimValue(dim_i));
					}
				} else {
					//Ndim = 6 , MoveNo無しversion
					input[0].setValue(lines[data_i].getDimValue(0));
					input[1].setValue(lines[data_i].getDimValue(0));
					input[2].setValue(lines[data_i].getDimValue(2));
					input[3].setValue(1f - lines[data_i].getDimValue(2));
					input[4].setValue(lines[data_i].getDimValue(4));
					input[5].setValue(lines[data_i].getDimValue(4));
				}
			}



			this.fs.evaluate();
			for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
				//読み込んだデータに対してのメンバシップ値を保持
				memberships[data_i][rule_i] = ((TskVariableType) this.fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getW();
				//現在の結論部の値を保持
				newConcList[rule_i] = ((TskVariableType) this.fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(rule_i).getZ();
			}
		}

		//学習計算開始
		for(int gene_i = 0; gene_i < interval; gene_i++) {
			for(int data_i = 0; data_i < dataSize; data_i++) {
				memberSum = 0f;
				y[data_i] = 0f;
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					memberSum += memberships[data_i][rule_i];
					y[data_i] += memberships[data_i][rule_i] * newConcList[rule_i];
				}
				y[data_i] /= memberSum;

				//修正量計算
				diff = lines[data_i].getY() - y[data_i];
				for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
					newConcList[rule_i] += setting.eta * diff * memberships[data_i][rule_i] / memberSum;
				}
			}
		}

		//concList更新
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			this.concList[rule_i] = newConcList[rule_i];
		}
		this.makeFS(setting);
	}


	//与えられたdatasetの推論値y[dataset.dataSize]を返すメソッド
	public float[] reasoning(SettingForGA setting, DataSetInfo dataset) {
		int dataSize = dataset.DataSize;
		int Ndim = setting.Ndim;
		float[] y = new float[dataSize];

		KnowledgeBaseVariable[] input = new KnowledgeBaseVariable[Ndim];
		KnowledgeBaseVariable output;
		Pattern line;

		if(Ndim == 7) {
			input[0] = this.fs.getVariable("MoveNo");
			input[1] = this.fs.getVariable("DBSN");
			input[2] = this.fs.getVariable("DWSN");
			input[3] = this.fs.getVariable("DBWR");
			input[4] = this.fs.getVariable("DWWR");
			input[5] = this.fs.getVariable("DBTMR");
			input[6] = this.fs.getVariable("DWTMR");
		} else if(Ndim == 6) {
			//Ndim = 6 , MoveNo無しversion
			input[0] = this.fs.getVariable("DBSN");
			input[1] = this.fs.getVariable("DWSN");
			input[2] = this.fs.getVariable("DBWR");
			input[3] = this.fs.getVariable("DWWR");
			input[4] = this.fs.getVariable("DBTMR");
			input[5] = this.fs.getVariable("DWTMR");
		}

		for(int data_i = 0; data_i < dataSize; data_i++) {
			line = dataset.getPattern(data_i);

			if(Ndim == 7) {
				if(line.getDimValue(2) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue( line.getDimValue(dim_i) );
					}
				} else {
					input[0].setValue(line.getDimValue(0));
					input[1].setValue(line.getDimValue(1));
					input[2].setValue(line.getDimValue(1));
					input[3].setValue(line.getDimValue(3));
					input[4].setValue(1f - line.getDimValue(3));
					input[5].setValue(line.getDimValue(5));
					input[6].setValue(line.getDimValue(5));
				}
			} else if(Ndim == 6) {
				if(line.getDimValue(1) >= 0) {
					for(int dim_i = 0; dim_i < Ndim; dim_i++) {
						input[dim_i].setValue( line.getDimValue(dim_i) );
					}
				} else {
					//Ndim = 6 , MoveNo無しversion
					input[0].setValue(line.getDimValue(0));
					input[1].setValue(line.getDimValue(0));
					input[2].setValue(line.getDimValue(2));
					input[3].setValue(1f - line.getDimValue(2));
					input[4].setValue(line.getDimValue(4));
					input[5].setValue(line.getDimValue(4));
				}
			}

			this.fs.evaluate();

			output = this.fs.getVariable("EBWR");
			y[data_i] = output.getValue();
		}

		return y;
	}

	public void readFML(FuzzyInferenceSystem xml, float[][][] fuzzyParams, SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
		this.fs = xml;
		this.ruleNum = xml.getKnowledgeBase().getVariable("EBWR").getTerms().size();
		this.concList = new float[ruleNum];
//		Arrays.fill(this.concList, 0.5f);
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			this.concList[rule_i] = ((TskVariableType) this.fs.getKnowledgeBase().getVariable("EBWR")).getTerms().get(rule_i).getTskValue().get(0);
		}
		this.setFuzzyParams(fuzzyParams);

		//rulesを読み取る
		int Ndim = fs.getKnowledgeBase().getVariables().size() - 1;	//EBWRがマイナス1
		int Fdiv = fs.getKnowledgeBase().getVariable("DBSN").getTerms().size() - 1;	//don't careがマイナス1
		int ruleNum = fs.getKnowledgeBase().getVariable("EBWR").getTerms().size();
		String[] fuzzyName = new String[Fdiv];
		for(int div_i = 0; div_i < Fdiv; div_i++) {
			fuzzyName[div_i] = ((FuzzyTermType)fs.getKnowledgeBase().getVariable("DBSN").getTerms().get(div_i)).getName();
		}

		this.rules.clear();
		for(int rule_i = 0; rule_i < ruleNum; rule_i++) {
			int[] ruleIdx = new int[Ndim];
			TskFuzzyRuleType rule = ((TskRuleBaseType)fs.getRuleBase(0)).getTskRules().get(rule_i);
			for(int dim_i = 0; dim_i < Ndim; dim_i++) {
				String termName = ((FuzzyTermType)rule.getAntecedent().getClauses().get(dim_i).getTerm()).getName();
				for(int div_i = 0; div_i < Fdiv; div_i++) {
					if(termName.equals(fuzzyName[div_i])) {
						ruleIdx[dim_i] = div_i;
						break;
					}
				}
				if(termName.equals("Don't Care")) {
					ruleIdx[dim_i] = Fdiv;
				}
			}
			this.rules.add(ruleIdx);
		}
		this.makeFS(setting);
	}


	public void setFitness(float _fitness) {
		this.fitness = _fitness;
	}

	public float getFitness() {
		return this.fitness;
	}

}

































