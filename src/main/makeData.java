package main;

import java.io.IOException;
import java.util.ArrayList;

public class makeData {

	public static void main(String[] args) throws IOException {
		// TODO 自動生成されたメソッド・スタブ



		String traFileName = "DataSets/traData_raw_2.csv";
		DataSetInfo traDataInfo = new DataSetInfo(traFileName);

		MersenneTwisterFast rnd = new MersenneTwisterFast(2019);
		int[][] rand = new int[5][50];	//5ゲーム50パターン
		int r;
		for(int i = 0; i < 50; i++) {
			for(int j = 0; j < 5; j++) {
				while(true) {
					r = rnd.nextInt(45) + 1;
					if(r != rand[0][i] && r != rand[1][i] && r != rand[2][i] && r != rand[3][i] && r != rand[4][i]) {
						rand[j][i] = r;
						break;
					}
				}
			}
		}

		ArrayList<Float[]> tra = new ArrayList<Float[]>();
		ArrayList<Float[]> eva = new ArrayList<Float[]>();

		for(int i = 0; i < 5; i++) {
			for(int j = 0; j < 50; j++) {

				int gameNo = 0;
				int index = 0;

				while(true) {
					float move = traDataInfo.getPattern(index).getDimValue(0);
					if(move == 0f) {
						gameNo++;
						if(gameNo == rand[i][j]) {
							for(int k = 0; k < j; k++) {
								index++;
							}


						}

					}
					index++;
				}

			}
		}







		System.out.println("");




	}

}
