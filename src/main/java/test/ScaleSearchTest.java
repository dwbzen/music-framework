package test;

import music.element.Scale;
import music.element.ScaleFormula;
import util.music.ScaleSearch;

public class ScaleSearchTest {

	/**
	 * Search for a scale formula by name,
	 * or a scale by name and root
	 * Usage: ScaleSearch -scale name [-root pitch] -formula name
	 * @param args
	 */
	public static void main(String... args) {
		String formulaName = null;
		String scaleName = null;
		String root = null;
		for(int i = 0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("-formula")) {
				formulaName = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-scale")) {
				scaleName = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-root")) {
				root = args[++i];
			}
		}
		ScaleSearch search = new ScaleSearch();
		
		if(formulaName != null) {
			String fs = search.findScaleFormula(formulaName);
			System.out.println("formula: " + fs);
			ScaleFormula sf = search.getScaleFormula(formulaName);
			if(sf != null) {
				System.out.println("formula: " + sf.toJSON());
			}
		}
		if(formulaName != null && root != null) {
			String ss = search.findScaleFromFormula(formulaName, root);
			System.out.println("scale: " + ss);
			Scale scale = search.getScaleFromFormula(formulaName, root);
			if(scale != null) {
				System.out.println("scale: " + scale.toJSON());
			}
		}
		
		if(scaleName != null && root != null) {
			String ss = search.findScale(scaleName, root);
			if(ss != null) {
				System.out.println(ss);
				Scale scale = search.getScale(formulaName, root);
				if(scale != null) {
					System.out.println(scale.toJSON());
				}
			}
		}
	}

}
