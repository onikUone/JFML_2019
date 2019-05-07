package data_methods;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MakeProgress {

	public static void main(String[] args) {
		String resultFolder = args[0];
		String sep = File.separator;


		int fmlGene = Integer.parseInt(args[1]);
		int fsGene = Integer.parseInt(args[2]);	//ファイル名用
		int popFML = Integer.parseInt(args[3]);
		int popFS = Integer.parseInt(args[4]);

		float[][][] mse = new float[fmlGene][popFML][popFS];
		float[][][] fitness = new float[fmlGene][popFML][popFS];

		for(int gene_i = 0; gene_i < fmlGene; gene_i++) {
			for(int pop_i = 0; pop_i < popFML; pop_i++) {
				String fileName = resultFolder + sep +
									"fmlGene_" + String.valueOf(gene_i) + sep +
									"FMLpop" + String.valueOf(pop_i) + sep +
									"fsGene_" + String.valueOf(fsGene) + sep +
									"MSE" + sep +
									"gene" + String.valueOf(fsGene) + "_tstMSE.csv";

				inputFinalMSE(fileName, mse[gene_i][pop_i], fitness[gene_i][pop_i]);
			}
		}

		String dirName = resultFolder + sep + "$MSE";
		File newdir = new File(dirName);
		newdir.mkdirs();

		dirName = resultFolder + sep + "$fitness";
		newdir = new File(dirName);
		newdir.mkdirs();

		for(int gene_i = 0; gene_i < fmlGene; gene_i++) {
			String fileName = resultFolder + sep +
								"$MSE" + sep +
								"fmlGene" + String.valueOf(gene_i) + "_MSE.csv";
			outputMSE(fileName, mse[gene_i]);
		}
		for(int gene_i = 0; gene_i < fmlGene; gene_i++) {
			String fileName = resultFolder + sep +
								"$fitness" + sep +
								"fmlGene" + String.valueOf(gene_i) + "_fitness.csv";
			outputMSE(fileName, fitness[gene_i]);
		}

		float[] aveMSE = new float[fmlGene];
		float[] aveFIT = new float[fmlGene];
		calcAVE(aveMSE, mse);
		calcAVE(aveFIT, fitness);

		String fileName = resultFolder + sep +
							"$MSE" + sep +
							"$allAVE_MSE.csv";
		outputAVE(fileName, aveMSE);

		fileName = resultFolder + sep +
					"$fitness" + sep +
					"$allAVE_fitness.csv";
		outputAVE(fileName, aveFIT);

		System.out.println();
	}

	public static void outputAVE(String fileName, float[] ave) {
		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			int Gene = ave.length;

			for(int gene_i = 0; gene_i < Gene; gene_i++) {
				pw.print("fmlGene" + String.valueOf(gene_i) + ",");
				pw.println(ave[gene_i]);
			}


			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void calcAVE(float[] ans, float[][][] array) {
		int Gene = array.length;
		int popFML = array[0].length;
		int popFS = array[0][0].length;

		for(int gene_i = 0; gene_i < Gene; gene_i++) {
			float aveFS;
			float aveFML = 0f;

			for(int fml_i = 0; fml_i < popFML; fml_i++) {
				aveFS = 0f;
				for(int fs_i = 0; fs_i < popFS; fs_i++) {
					aveFS += array[gene_i][fml_i][fs_i];
				}
				aveFML += (aveFS / popFS);
			}

			ans[gene_i] = (aveFML / popFML);
		}
	}

	public static void outputMSE(String fileName, float[][] array) {

		try {
			FileWriter fw = new FileWriter(fileName, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			int popFML = array.length;
			int popFS = array[0].length;

			for(int fml_i = 0; fml_i < popFML; fml_i++) {
				pw.print("FMLpop_" + String.valueOf(fml_i) + ",");
				for(int fs_i = 0; fs_i < popFS; fs_i++) {
					pw.print(array[fml_i][fs_i] + ",");
				}
				pw.println();
			}

			pw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}

	}

	public static void inputFinalMSE(String fileName, float[] _mse, float[] _fitness) {

		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String line;

			int count = 0;
			line = in.readLine();
			while( (line = in.readLine()) != null ) {
				float mse = Float.parseFloat(line.split(",")[0]);
				float fitness = Float.parseFloat(line.split(",")[1]);
				_mse[count] = mse;
				_fitness[count] = fitness;
				count++;
			}
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
