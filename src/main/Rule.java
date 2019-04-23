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
		this.rule = new int[setting.Ndim];
	}

	//Deep Copy
	public Rule(Rule rule) {
		setParams(rule.params);
		setRule(rule.rule);
		this.uniqueRnd = new MersenneTwisterFast(rule.uniqueRnd.nextInt());
	}
	// ************************************************************

	//Methods *****************************************************

	public void mutation(int dim, SettingForGA setting) {
		int newFuzzySet = 0;
		int count = 0;
		do {
			if(count > 10) {
				break;
			}

			newFuzzySet = uniqueRnd.nextInt(setting.Fdiv);

		} while(newFuzzySet == this.rule[dim]);

		this.rule[dim] = newFuzzySet;

	}

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
