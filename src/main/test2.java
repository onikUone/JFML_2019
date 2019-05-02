package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import jfml.FuzzyInferenceSystem;
import jfml.JFML;
import jfml.knowledgebase.variable.TskVariableType;

public class test2 {

	public static void main(String[] args) {
		File xml = new File("results/submit_gene14150/FML_14150.xml");
		FuzzyInferenceSystem fs = JFML.load(xml);

		float[] concList = new float[78125];

		fs.evaluate();

		for(int i = 0; i < concList.length; i++) {
			concList[i] = ((TskVariableType) fs.getKnowledgeBase().getVariable("EBWR")).getWZ().get(i).getZ();
		}


		String resultFile = "results/submit_gene14150/conclusionList.csv";

		try {
			FileWriter fw = new FileWriter(resultFile, true);
			PrintWriter pw = new PrintWriter( new BufferedWriter(fw) );

			for(int i = 0; i < concList.length; i++) {
				pw.println(String.valueOf(concList[i]));
			}
			pw.close();

		} catch(IOException e) {
			e.printStackTrace();
		}

	}

}
