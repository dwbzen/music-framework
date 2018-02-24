package util.music;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import music.element.ScaleFormula;

import org.apache.log4j.Logger;

/**
 * Imports a tab-delimited text file with the fields:
 * Name Of Scale, Alternative Scale Names, Groups, Formula
 * creating ScaleFormula instances from each line, writing the JSON
 * string to stdout. Suitable for import into MongoDB, for example:</p>
 * <code>
 * mongoimport --type json --collection scale_formulas --db music --file common_scaleFormulas.json
 * </p>
 * mongoimport --type json --collection scale_formulas --db music --file theoretical_scaleFormulas.json
 * </code>
 * </p>
 * > db.scale_formulas.ensureIndex( {name:1})</p>
 * 
 * NOTE - errors are fixed in the .csv, then re-save as tab-delimited
 * Also you will need to remove Invalid UTF8 characters manually: 0x92 (’), 0x95 (•)
 * 
 * @author don_bacon
 *
 */
public class ScaleFormulaImport {

	static final org.apache.log4j.Logger log = Logger.getLogger(ScaleFormulaImport.class);
			
	public static void main(String... args)  {
		if(args.length == 0) {
			return;
		}
		String filename = args[0];
		String line;
		BufferedReader inputFileReader = null;
		Map<String, ScaleFormula> formulaMap = new TreeMap<String, ScaleFormula>();
		try {
			File f = new File(filename);
			inputFileReader = new BufferedReader(new FileReader(f));
			int lineNumber = 0;
			ScaleFormula scaleFormula;
			String[] alternates;
			while((line = inputFileReader.readLine()) != null) {
				lineNumber++;
				if(line.startsWith("Name Of Scale")) {
					continue;	// skip header line
				}
				String[] fields = line.split("\t");
				log.trace("line: " + lineNumber + ": " + line + " fields: " + fields.length);
				String scaleName = fields[0].trim().replaceAll("\"", "");
				String alternativeNames = fields[1];
				if(alternativeNames != null && alternativeNames.length()>0) {
					alternates = alternativeNames.split(",");
				}
				else {
					alternates = null;
				}
				String groups = fields[2];
				int[] formula = convertFormulaString(fields[3]);
				log.trace(Arrays.toString(formula));
				scaleFormula = new ScaleFormula(scaleName, groups, formula);
				if(alternates != null) {
					for(int i=0; i<alternates.length; i++) {
						scaleFormula.getAlternateNames().add(alternates[i]);
					}
				}
				if(formulaMap.containsKey(scaleName)) {
					// name needs to be unique
					// this is probably a re-spelling of the scale using same formula
					ScaleFormula sf = formulaMap.get(scaleName);
					if(isSameFormula(sf, scaleFormula)) {
						continue;
					}
					else {
						log.error("Duplicate name: " + scaleName + " line: " + lineNumber);
					}
				}
				formulaMap.put(scaleName, scaleFormula);
				System.out.println(scaleFormula.toJSON());
			}
			inputFileReader.close();
		} catch(FileNotFoundException e) {
			System.err.println("file not found: " + filename);
		} catch (IOException e) {
			System.err.println("Exception: " + e.toString());
			e.printStackTrace();
		}

	}
	
	private static boolean isSameFormula(ScaleFormula sf, ScaleFormula scaleFormula) {
		List<Integer> sfForm = sf.getFormula();
		List<Integer> scaleForm = scaleFormula.getFormula();
		boolean same = true;
		if(sfForm.size() != scaleForm.size()) {
			same = false;
		}
		else {
			for(int i=0; i<sfForm.size(); i++) {
				if(sfForm.get(i) != scaleForm.get(i)) {
					same = false;
				}
			}
		}
		return same;
	}

	static int[] convertFormulaString(String raw) {
		String field = raw.replaceAll("[\" ]", "");
		String[] formulaString = field.split(",");
		int[] formula = new int[formulaString.length];
		for(int i=0; i<formulaString.length; i++) {
			formula[i] = Integer.parseInt(formulaString[i]);
		}
		return formula;
	}
}
