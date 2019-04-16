package main;

import java.io.Serializable;

public class Pattern implements Serializable {

	//Field ************************************
	int Ndim;
	float[] x;	//パターン
	float y;	//教師ラベル
	// *****************************************

	//Constructor
	public Pattern(){}

	public Pattern(float[] _pattern) {
		Ndim = _pattern.length - 1;
		x = _pattern;
		y = _pattern[Ndim];
	}

	public Pattern(String[] _x, String _y) {
		this.Ndim = _x.length;
		this.x = new float[Ndim];
		for(int i = 0; i < Ndim; i++) {
			this.x[i] = Float.parseFloat(_x[i]);
		}
		this.y = Float.parseFloat(_y);
	}

	public Pattern(String[] _pattern) {
		int Ndim = _pattern.length - 1;
		x = new float[Ndim];
		for(int i = 0; i < Ndim; i++) {
			x[i] = Float.parseFloat(_pattern[i]);
		}
		y = Float.parseFloat(_pattern[Ndim]);
	}

	//Method
	public int getNdim() {
		return this.Ndim;
	}

	public float getDimValue(int i) {
		return x[i];
	}

	public void setDimValue(int i, float _newValue) {
		this.x[i] = _newValue;
	}

	public float[] getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

}
