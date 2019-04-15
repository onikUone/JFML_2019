package main;

import java.io.Serializable;

public class Pattern implements Serializable {

	//Field ************************************
	double[] x;	//パターン
	double y;	//教師ラベル
	// *****************************************

	//Constructor
	public Pattern(){}

	public Pattern(double[] _pattern) {
		int Ndim = _pattern.length - 1;
		x = _pattern;
		y = _pattern[Ndim];
	}

	public Pattern(String[] _x, String _y) {
		int Ndim = _x.length;
		x = new double[Ndim];
		for(int i = 0; i < Ndim; i++) {
			x[i] = Double.parseDouble(_x[i]);
		}
		y = Double.parseDouble(_y);
	}

	public Pattern(String[] _pattern) {
		int Ndim = _pattern.length - 1;
		x = new double[Ndim];
		for(int i = 0; i < Ndim; i++) {
			x[i] = Double.parseDouble(_pattern[i]);
		}
		y = Double.parseDouble(_pattern[Ndim]);
	}

	//Method
	public double getDimValue(int i) {
		return x[i];
	}

	public void setDimValue(int i, double _newValue) {
		this.x[i] = _newValue;
	}

	public double[] getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

}
