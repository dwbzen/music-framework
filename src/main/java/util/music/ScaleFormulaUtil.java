package util.music;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import music.element.IFormula;

public class ScaleFormulaUtil {

	/**
	 * Takes as input a .csv file of 12edo pitches
	 * and converts to scale formula, writing to stdout.
	 * 
	 * @param args
	 */
	public static void main(String... args)  {
		if(args.length == 0) {
			return;
		}
		String filename = args[0];
		String line;
		BufferedReader inputFileReader = null;
		try {
			File f = new File(filename);
			inputFileReader = new BufferedReader(new FileReader(f));
			while((line = inputFileReader.readLine()) != null) {
				String[] pitches = line.split(",");
				List<Integer> formula = convertPitchSetToFormula(pitches);
				System.out.println(formula);
			}
			inputFileReader.close();
		} catch(FileNotFoundException e) {
			System.err.println("file not found: " + filename);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static List<Integer> convertPitchSetToFormula(String[] pitches) {
		int[] pitchSet = new int[pitches.length];
		List<Integer> formula = null;
		int ind = 0;
		for(int i=0; i<pitches.length; i++) {
			if(pitches[i].length() > 0) {
				pitchSet[ind++] = Integer.parseInt(pitches[i]);
			}
		}
		formula = IFormula.pitchSetToFormula(pitchSet);
		return formula;
	}
}
