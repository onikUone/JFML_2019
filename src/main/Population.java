package main;

import jfml.FuzzyInferenceSystem;

public class Population {
	//Fields ******************************************************
	FuzzyInferenceSystem fs;

	float[] params;

	MersenneTwisterFast uniqueRnd;
	// ************************************************************


	//Constructor *************************************************
	public Population() {}

	public Population(MersenneTwisterFast rnd, int Ndim, SettingForFML setting) {
		this.uniqueRnd = new MersenneTwisterFast(rnd.nextInt());
		this.params = new float[3 * Ndim];
		this.fs = new FuzzyInferenceSystem();
//		this.fs.setKnowledgeBase(setting.getKnowledgeBase());
	}

	// ************************************************************

	//Methods *****************************************************
	public FuzzyInferenceSystem getFS() {
		return this.fs;
	}

	public void setFS(FuzzyInferenceSystem fs) {
		this.fs = fs;
	}

	public void setParams(float[] _params) {
		for(int i = 0; i < _params.length; i++) {
			this.params[i] = _params[i];
		}
	}

	public void setMemberships(int[] setRule, SettingForFML setting) {
		for(int i = 0; i < fs.getVariables().size(); i++) {
		}
	}

	// ************************************************************

}
