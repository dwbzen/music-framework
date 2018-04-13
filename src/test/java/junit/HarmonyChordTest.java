package junit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;
import music.element.Pitch;
import music.element.song.ChordFormula;
import music.element.song.ChordProgression;
import music.element.song.ChordProgressionComparator;
import music.element.song.HarmonyChord;
import util.music.ChordManager;

public class HarmonyChordTest extends TestCase {
	public static final String CONFIG_FILENAME = "/config.properties";
	static final org.apache.log4j.Logger log = Logger.getLogger(HarmonyChordTest.class);
	static List<Pitch> rootPitches = new ArrayList<Pitch>();
	static Map<String,ChordFormula> chordFormulas = null;
	static Map<ChordProgression, Integer> collectorStatsMap = 
			new TreeMap<ChordProgression, Integer>(new ChordProgressionComparator());
	static ChordManager chordManager = new ChordManager();
	
	@Test
	public void testCreateHaronyChords1() {
		Pitch p = new Pitch("C");
		rootPitches.add(p);
		Map<String, HarmonyChord> harmonyChords = chordManager.createHarmonyChords(rootPitches);
		String root = rootPitches.get(0).getStep().name();

		HarmonyChord hc7a = harmonyChords.get(root + "7");
		HarmonyChord hc9 = harmonyChords.get(root + "9");
		HarmonyChord hm7 = harmonyChords.get(root + "m7");
		HarmonyChord hc7b = harmonyChords.get(root + "7");
		ChordProgression cp1 = new ChordProgression();
		ChordProgression cp2 = new ChordProgression();
		ChordProgression cp3 = new ChordProgression();
		cp1.add(hc7a);
		cp1.add(hc9);
		// cp1 and cp2 should be the same from key POV
		cp2.add(hc7b);
		cp2.add(hc9);
		cp3.add(hc7a);
		cp3.add(hm7);
		
		log.debug("ChordProgression1: " + cp1.toString());
		log.debug("ChordProgression2: " + cp2.toString());
		log.debug("ChordProgression3: " + cp3.toString());
		
		// test TreeMap
		collectorStatsMap.put(cp1, 10);
		if(collectorStatsMap.containsKey(cp2)) {
			log.debug("map contains key: " + cp2.toString());
			collectorStatsMap.put(cp1, 15);
		}
		else {
			log.debug("map does NOT contain key: " + cp2.toString());
		}
		if(!collectorStatsMap.containsKey(cp3)) {
			log.debug("map does NOT contain key: " + cp3.toString());
			collectorStatsMap.put(cp3,  30);
		}
		log.debug("Keys/Values");
		for(ChordProgression cp : collectorStatsMap.keySet()) {
			Integer ival = collectorStatsMap.get(cp);
			log.debug(cp.toString() + "  " + ival);
		}
	}
	
	@Test
	public void testCreateHaronyChords2() {
		loadChordFormulas();
		HarmonyChord hc1 = new HarmonyChord("D7", chordFormulas);
		HarmonyChord hc2 = new HarmonyChord("C", chordFormulas);
		HarmonyChord hc3 = new HarmonyChord("C#", chordFormulas);
		HarmonyChord hc4 = new HarmonyChord("Bbm7", chordFormulas);

		log.info("D7: " + hc1);
		assert(hc1.toString().equals("D7 [D, Gb, A, C]"));
		
		log.info("C: " + hc2);
		assert(hc2.toString().equals("C [C, E, G]"));

		log.info("C#: " + hc3);
		assert(hc3.toString().equals("C# [C#, F, Ab]"));

		log.info("Bbm7: " + hc4);
		assert(hc4.toString().equals("Bbm7 [Bb, Db, F, Ab]"));
		
	}
	
	static void loadChordFormulas() {
		
		chordFormulas = chordManager.getChordFormulas();
		log.info(chordFormulas.size() + " chords loaded");
		assertTrue(chordFormulas != null && chordFormulas.size() > 0);
	}
}
