package main;

public class FuzzySet {
	//field
	String name;
	float[] fuzzyParam = new float[2];
	float contribute;

	float fitness;

	//Constructor
	public FuzzySet() {}

	public FuzzySet(float[] _fuzzyParam, float _contribute) {
		this.fuzzyParam = new float[2];
		this.fuzzyParam[0] = _fuzzyParam[0];
		this.fuzzyParam[1] = _fuzzyParam[1];
		this.contribute = _contribute;
	}

	public void setFuzzyParam(float[] _fuzzyParam) {
		this.fuzzyParam[0] = _fuzzyParam[0];
		this.fuzzyParam[1] = _fuzzyParam[1];
	}

	public void setFuzzyParam(float m, float s) {
		this.fuzzyParam[0] = m;
		this.fuzzyParam[1] = s;
	}

	public float getContribute() {
		return this.contribute;
	}

}
