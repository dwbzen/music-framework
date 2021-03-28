package org.dwbzen.util.music;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dwbzen.music.element.Chord;
import org.dwbzen.music.element.IFormula;
import org.dwbzen.music.element.song.ChordFormula;

public class ChordUtil {
	
	static ChordManager chordManager = new ChordManager();

	/**
	 * Computes & displays chord info of a given formula or list of notes/pitches<br>
	 * -formula format is a chord formula delimited by commas, for example, "4,4,2,5"<br>
	 * -notes format is a list of comma-delimited notes, for example "D,F,Bb"<br>
	 *  or "E3,G3,Bb3,C3" for specific pitches.
	 * 
	 * @param strings formula string
	 */
	public static void main(String...args) {
		String[] fstring = null;
		String[] notes = null;
		String formulaString = null;
		String notesString = null;
    	if(args.length > 0) {
    		for(int i = 0; i<args.length; i++) {
    			if(args[i].equalsIgnoreCase("-formula")) {
    				fstring = args[++i].split(",");
    				formulaString = args[i];
    			}
    			else if(args[i].equalsIgnoreCase("-notes")) {
    				notes = args[++i].split(",");
    				notesString = args[i];
    			}
    		}
    	}

		List<Integer> formula = new ArrayList<>();
		if(formulaString != null) {
			for(String s : fstring) {
				formula.add(Integer.parseInt(s));
			}
			List<Integer> pitchIndexes = IFormula.formulaToPitchIndexes(formula);
			StringBuilder sb = new StringBuilder();
			pitchIndexes.forEach(n -> sb.append(n + " "));
			System.out.println("Formula: " + formulaString);
			System.out.println("pitchIndexes: " + sb.toString());
			
			int formulaNumber = ChordManager.computeFormulaNumber(formula);
			System.out.println("formula number: " + formulaNumber);
			
			int spellingNumber = ChordManager.computeSpellingNumber(formula);
			System.out.println("spelling number: " + spellingNumber);
			
			ChordFormula cf = chordManager.find(formulaNumber);
			if(cf != null) {
				System.out.println(cf.getName() + " (" +  cf.getSymbol() +  "): " + cf.getSpelling());
				Map<Integer, List<Integer>> inversions = cf.getInversions();
				List<Integer> invFnums = cf.getInversionFormulaNumbers();
				int invnum = 0;
				System.out.println(inversions.size() + " Inversions:");
				for(Integer i : inversions.keySet()) {
					List<Integer> inv = inversions.get(i);
					String s = Arrays.toString(inv.toArray(new Integer[0]));
					
					System.out.println("  " + s + "   " + invFnums.get(invnum++));
				}
			}
			else {
				System.err.println("Formula not found");
			}
		}
		else if(notesString != null) {
			Chord chord = Chord.createChord(notes, 0);
			ChordFormula chordFormula = chordManager.addChordFormulaToChord(chord);
			if(chordFormula != null) {
				System.out.println("chord: " + chord.toString(true));
				System.out.println(chordFormula.toJson(true));
			}
		}
	}
	
}
