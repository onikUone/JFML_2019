package main;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataSetInfo {

	//Field ******************************************
	int Ndim = 6;
	int DataSize;
	ArrayList<String> Attribute = new ArrayList<String>();

	ArrayList<Pattern> patterns = new ArrayList<Pattern>();
	// ************************************************

	//Constructor *************************************
	public DataSetInfo() {}

	public DataSetInfo(String _filePath) throws IOException{
		inputFile(_filePath);
//		normalize(patterns, 0);
//		normalize(patterns, 1);
	}

	public DataSetInfo(int _DataSize, int _Ndim) {
		this.DataSize = _DataSize;
		this.Ndim = _Ndim;
	}

	public DataSetInfo(int _DataSize, int _Ndim, ArrayList<Pattern> _patterns) {
		this.DataSize = _DataSize;
		this.Ndim = _Ndim;
		this.patterns = _patterns;
	}
	// ************************************************

	//Method ******************************************

	public void inputFile(String _filePath) throws IOException {
		int datasize = 0;

		//読込用変数
		List<String[]> lines = new ArrayList<String[]>();
		BufferedReader in = new BufferedReader(new FileReader(_filePath));
		String line;
		String[] x = new String[this.Ndim];
		String y;

		//ファイル読込
		while( (line = in.readLine()) != null ) {
			for(int i = 0; i < this.Ndim; i++) {
				x[i] = line.split(",")[i];
			}
			y = line.split(",")[this.Ndim];
			this.patterns.add(new Pattern(x, y));
			datasize++;
		}
		this.DataSize = datasize;
	}

	public static void inputConcList(String _filePath, double[] _concList) throws IOException {
		//読み込み用変数
		List<String[]> lines = new ArrayList<String[]>();
		BufferedReader in = new BufferedReader(new FileReader(_filePath));
		String line;

		for(int i = 0; i < _concList.length; i++) {
			line = in.readLine();
			_concList[i] = Double.parseDouble(line);
		}
	}

//	public void inputFiles_old(String _filePath) throws IOException {
//		int datasize = 0;
//
//		List<String[]> lines = new ArrayList<String[]>();
//		BufferedReader in = new BufferedReader(new FileReader(_filePath));
//		String line;
//		line = in.readLine();
//		this.Ndim = 6;
//		for(int i = 1; i < (line.split(",").length - 1); i++) {
//			this.Attribute.add( line.split(",")[i] );
//		}
//		while( (line = in.readLine()) != null ) {
//			String[] tmp = new String[this.Ndim + 1];
//			for(int i = 0; i < tmp.length; i++) {
//				if(line.split(",").length < tmp.length && i == tmp.length - 1) {
//					tmp[i] = "-1";
//				}
//				tmp[i] = line.split(",")[i + 1];
//			}
//			for(int i = 0; i < tmp.length; i++) {
//				if(tmp[i].length() == 0) {
//					tmp[i] = "-1";
//				}
//			}
//			this.patterns.add(new Pattern(tmp));
//			datasize++;
//		}
//		this.DataSize = datasize;
//	}

//	public void normalize(ArrayList<Pattern> _patterns, int _idx) {
//		double max, min, x;
//		max = _patterns.get(0).getDimValue(_idx);
//		min = _patterns.get(0).getDimValue(_idx);
//		//最大値・最小値 獲得
//		for(int pattern_i = 1; pattern_i < _patterns.size(); pattern_i++) {
//			x = _patterns.get(pattern_i).getDimValue(_idx);
//			if(max < x) {
//				max = x;
//			}
//			if(min > x) {
//				min = x;
//			}
//		}
//		//正規化
//		double a, b;
//		for(int pattern_i = 0; pattern_i < _patterns.size(); pattern_i++) {
//			x = _patterns.get(pattern_i).getDimValue(_idx);
//			a = x - min;
//			b = max - min;
//			_patterns.get(pattern_i).setDimValue(_idx, (a / b) );
//		}
//
//	}

	public void setPattern(ArrayList<Pattern> _patterns) {
		this.patterns = _patterns;
	}

	public void addPattern(Pattern _pattern) {
		this.patterns.add(_pattern);
	}

	public void setNdim(int _num) {
		this.Ndim = _num;
	}

	public void setDataSize(int _num) {
		this.DataSize = _num;
	}

	public void setAttribute(ArrayList<String> _attribute) {
		this.Attribute = _attribute;
	}

	public Pattern getPattern(int _index) {
		return this.patterns.get(_index);
	}

	public ArrayList<Pattern> getPatterns(){
		return this.patterns;
	}

	public int getNdim() {
		return this.Ndim;
	}

	public int getDataSize() {
		return this.DataSize;
	}

	public ArrayList<String> getAttribute() {
		return this.Attribute;
	}
}
