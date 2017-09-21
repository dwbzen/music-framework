package junit;

import java.util.Map;

import music.element.Key;
import music.element.song.ChordFormula;
import music.element.song.ChordInfo;
import music.element.song.ChordManager;
import music.element.song.HarmonyChord;

import org.junit.Test;

public class ChordUtilTest {

	static String[] testCases = {
		"Am9", "A", "B7",  "Cdim", "Db9", 	"Em(M7)", "F7#11", "Gadd9", "G#sus6", "A5", "0",
		"A/E", "B7/A", "Cdim/Eb",  "Db9/Bb","Em(M7)/D#", "F7#11/C", "Gadd9/D", "Absus6/F", "A5/E"
	};
	@Test
	public void testParseChord() {
		for(int i=0; i<testCases.length;i++) {
			String chordName = testCases[i];
			ChordInfo result = ChordManager.parseChordName(chordName);
			System.out.println(chordName + ": " + 
					"name: " + result.getChordName() + 	", root: " + result.getRootNote() +
					", bass: " + result.getBassNote() + ", symbol: " + result.getChordSymbol());
		}
	}

	@Test
	public void testCreateHarmonyChord() {
		HarmonyChord hc = null;
		Map<String, ChordFormula> formulas = ChordManager.loadChordFormulas("test", "chord_formulas");
		for(int i=0; i<testCases.length;i++) {
			String chordName = testCases[i];
			ChordInfo result = ChordManager.parseChordName(chordName);
			System.out.println(chordName + ": " + 
					"name: " + result.getChordName() + 	", root: " + result.getRootNote() +
					", bass: " + result.getBassNote() + ", symbol: " + result.getChordSymbol());
			hc = ChordManager.createHarmonyChord(result.getChordName(), formulas, Key.D_MAJOR);
			if(hc != null) {
				System.out.println("  Harmony chord: " + hc.getName() + ", " + hc.toJSON());
				System.out.println("  formula number: " + hc.getChordFormula().computeFormulaNumber());
			}
			else {
				System.err.println("Could not create HarmonyChord for " + result.getChordName() + "\n");
			}
		}
	}
}
