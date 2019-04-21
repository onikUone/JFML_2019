package main;

public class Rule {
	//Fields ******************************************************

	float[] params;
	int[] rule;

	MersenneTwisterFast uniqueRnd;
	// ************************************************************


	//Constructor *************************************************
	public Rule() {}

	public Rule(MersenneTwisterFast rnd, int Ndim, SettingForFML setting) {
		this.uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		this.params = new float[3 * Ndim];
		this.rule = new int[Ndim];
//		this.fs.setKnowledgeBase(setting.getKnowledgeBase());
	}

	public Rule(SettingForGA setting) {
		this.uniqueRnd = new MersenneTwisterFast(setting.rnd.nextInt());
		this.params = new float[2 * setting.Ndim];
	}
	// ************************************************************

	//Methods *****************************************************



	public void setParams(float[] _params) {
		for(int i = 0; i < _params.length; i++) {
			this.params[i] = _params[i];
		}
	}

	public void setRule(int[] ruleIdx) {
		for(int i = 0; i < ruleIdx.length; i++) {
			this.rule[i] = ruleIdx[i];
		}
	}

	// ************************************************************

}
