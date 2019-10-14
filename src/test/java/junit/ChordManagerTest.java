package junit;

import static org.junit.Assert.assertNotEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.song.ChordFormula;
import org.dwbzen.music.element.song.ChordFormulas;
import org.dwbzen.music.element.song.HarmonyChord;
import org.dwbzen.util.music.ChordManager;

public class ChordManagerTest extends TestCase {

	public static final String CONFIG_FILENAME = "/config.properties";
	static final org.apache.log4j.Logger log = Logger.getLogger(ChordManagerTest.class);
	static List<Pitch> rootPitches = new ArrayList<Pitch>();
	static ChordFormulas chordFormulas = null;
	static ChordManager chordManager = new ChordManager();

	@Test
	public void testLoadChordFormulas() {
		chordFormulas = chordManager.getChordFormulas();
		int nchords = chordFormulas.getChordFormulas().size();
		log.debug(nchords + " chords loaded");
		assertNotEquals(0, nchords);
	}
	
	
	@Test
	public void testCreateHaronyChords() {
		testLoadChordFormulas();
		Pitch p = new Pitch("C4");
		rootPitches.add(p);
		Map<String, HarmonyChord> harmonyChords = chordManager.createHarmonyChords(rootPitches);
		String root = rootPitches.get(0).getStep().name();
		if(harmonyChords != null && harmonyChords.size()>0) {

			HarmonyChord hc1 = harmonyChords.get(root + "m(M7)");		// C, Eb, G, B
			HarmonyChord hc2 = harmonyChords.get(root + "7");			// C, E,  G, Bb
			HarmonyChord hc3 = harmonyChords.get(root + "7-13");
			int ndiff = hc1.notesDifferent(hc2);	// should be 4
			log.debug(hc1.getName() + " notes different than " + hc2.getName() + " " + ndiff);
			int nsame = hc1.notesSame(hc2);	// should be 2
			log.debug(hc1.getName() + " notes the same as " + hc2.getName() + " " + nsame);
			
			// C9+11: C, E, G, Bb, D, Gb
			// C7+11: C, E, G, Bb,    Gb
			HarmonyChord hc7_11 = harmonyChords.get(root + "7-11");
			HarmonyChord hc9_11 = harmonyChords.get(root + "9-11");
			int diff = hc7_11.notesDifferent(hc9_11);
			System.out.println("7-11 notes different than 9-11 :" +  diff);	// should be 1
			assertEquals(diff, 1);
			int same = hc7_11.notesSame(hc9_11);
			log.debug("7-11 notes the same as 9-11 :" + same );	// should be 5
			assertEquals(same, 5);
			log.debug("Harmony chord " + hc1.getName() + hc1.toJson());
			log.debug("Harmony chord " + hc2.getName() + hc3.toJson());
		}

	}
}
