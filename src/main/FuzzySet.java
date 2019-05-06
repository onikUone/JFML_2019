package main;

public class FuzzySet {
	//field
	float[] fuzzyParam;
	float contribute;

	public FuzzySet(float[] _fuzzyParam, float _contribute) {
		this.fuzzyParam = new float[2];
		this.fuzzyParam[0] = _fuzzyParam[0];
		this.fuzzyParam[1] = _fuzzyParam[1];
		this.contribute = _contribute;
	}

	public float getContribute() {
		return this.contribute;
	}

}
