package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Output {
	//Field ******************************************
	String fileNameDir = "result/";
	// ************************************************

	//Constructor *************************************
	public Output(String _fileNameDir) {
		this.fileNameDir = _fileNameDir;
	}
	// ************************************************

	//Methods *****************************************
//	public static void writeList(ArrayList<Float> _list, String _fileName) throws IOException {
//		FileWriter fw = new FileWriter(_fileName, true);
//		PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
//
//		for(int i = 0; i < _list.size(); i++) {
//			pw.println(_list.get(i));
//		}
//		pw.close();
//	}

	public static void writeArray(double[] y, String _fileName) throws IOException {
		FileWriter fw = new FileWriter(_fileName, true);
		PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
		for(int i = 0; i < y.length; i++) {
			pw.println(y[i]);
		}
		pw.close();
	}

	//float Array 用
	public static void writeArray(float[] _array, String _fileName) throws IOException {
		FileWriter fw = new FileWriter(_fileName, true);
		PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
		for(int i = 0; i < _array.length; i++) {
			pw.println(_array[i]);
		}
		pw.close();
	}

	//int array[][] 用
	public static void writeArray(int[][] _array, String _fileName) {
		try {
			FileWriter fw = new FileWriter(_fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int i = 0; i < _array.length; i++) {
				for(int j = 0; j < _array[i].length; j++) {
					pw.print(_array[i][j] + ",");
				}
				pw.println("");
			}

			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//resultManager MSEリスト用
	public static void writeList(ArrayList<ArrayList<Float>> _list, String _fileName) {
		try {
			FileWriter fw = new FileWriter(_fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );



			for(int i = 0; i < _list.get(0).size(); i++) {
				for(int j = 0; j < _list.size(); j++) {
					pw.print(_list.get(j).get(i) + ",");
				}
				pw.println("");
			}
			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void writeList(ArrayList<Float> mseTra, ArrayList<Float> mseTst, String _fileName) {
		try {
			FileWriter fw = new FileWriter(_fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int i = 0; i < mseTra.size(); i++) {
				pw.println(String.valueOf(mseTra.get(i)) + "," + String.valueOf(mseTst.get(i)));
			}

			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	//ガウシアン分布をみるためのtest用
	public static void writeGaussian(float[][] array, String fileName) throws IOException{
		FileWriter fw = new FileWriter(fileName, true);
		PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

		for(int i = 0; i < array.length; i++) {
			for(int j = 0; j < array[0].length; j++) {
				pw.print(String.valueOf(array[i][j]) + ",");
			}
			pw.println("");
		}
		pw.close();
	}

//	public static void writeConc(RuleSet _ruleSet, String _fileName) throws IOException {
//		FileWriter fw = new FileWriter(_fileName, true);
//		PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );
//
//		for(int i = 0; i < _ruleSet.rules.length; i++) {
//			pw.println(_ruleSet.rules[i].getConclution());
//		}
//		pw.close();
//	}
	// ************************************************
}
