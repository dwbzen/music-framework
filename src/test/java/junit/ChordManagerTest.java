package junit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mongodb.morphia.Morphia;

import music.element.Key;
import music.element.Pitch;
import music.element.song.ChordFormula;
import music.element.song.ChordManager;
import music.element.song.HarmonyChord;

public class ChordManagerTest {

	public static final String CONFIG_FILENAME = "/config.properties";
	static final org.apache.log4j.Logger log = Logger.getLogger(ChordManagerTest.class);
	static Morphia morphia = new Morphia();
	static List<Pitch> rootPitches = new ArrayList<Pitch>();
	static Map<String,ChordFormula> chordFormulas = null;

	@Test
	public void testLoadChordFormulas() {
		try {
			chordFormulas = ChordManager.loadChordFormulas("/data/music/chord_formulas.json");
		} catch (IOException ex) {
			log.error("Could not load chord formulas " + ex.toString());
			return;
		}
		System.out.println(chordFormulas.size() + " chords loaded");
	}
	
	
	@Test
	public void testCreateHaronyChords() {
		testLoadChordFormulas();
		Pitch p = new Pitch("C4");
		rootPitches.add(p);
		Map<String, HarmonyChord> harmonyChords = ChordManager.createHarmonyChords(rootPitches, chordFormulas, Key.C_MAJOR);
		String root = rootPitches.get(0).getStep().name();
		if(harmonyChords != null && harmonyChords.size()>0) {

			HarmonyChord hc1 = harmonyChords.get(root + "m(M7)");		// C, Eb, G, B
			HarmonyChord hc2 = harmonyChords.get(root + "7");			// C, E,  G, Bb
			HarmonyChord hc3 = harmonyChords.get(root + "7-13");
			int ndiff = hc1.notesDifferent(hc2);	// should be 4
			System.out.println(hc1.getName() + " notes different than " + hc2.getName() + " " + ndiff);
			int nsame = hc1.notesSame(hc2);	// should be 2
			System.out.println(hc1.getName() + " notes the same as " + hc2.getName() + " " + nsame);
			
			// C9+11: C, E, G, Bb, D, Gb
			// C7+11: C, E, G, Bb,    Gb
			System.out.println("7+11 notes different than 9+11 :" + 
					harmonyChords.get(root + "7+11").notesDifferent(harmonyChords.get(root + "9+11")) );	// should be 1
			System.out.println("7+11 notes the same as 9+11 :" + 
					harmonyChords.get(root + "7+11").notesSame(harmonyChords.get(root + "9+11")) );	// should be 5
			
			System.out.println("Harmony chord " + hc1.getName() + hc1.toJSON());
			System.out.println("Harmony chord " + hc2.getName() + hc3.toJSON());
		}

	}
}
