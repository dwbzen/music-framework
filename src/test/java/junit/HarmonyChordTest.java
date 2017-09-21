package junit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mongodb.morphia.Morphia;

import music.element.Key;
import music.element.Pitch;
import music.element.song.ChordFormula;
import music.element.song.ChordManager;
import music.element.song.ChordProgression;
import music.element.song.ChordProgressionComparator;
import music.element.song.HarmonyChord;
import util.Configuration;

public class HarmonyChordTest {
	public static final String CONFIG_FILENAME = "/config.properties";
	static final org.apache.log4j.Logger log = Logger.getLogger(HarmonyChordTest.class);
	static Morphia morphia = new Morphia();
	static List<Pitch> rootPitches = new ArrayList<Pitch>();
	static Map<String,ChordFormula> chordFormulas = null;
	static Map<ChordProgression, Integer> collectorStatsMap = 
			new TreeMap<ChordProgression, Integer>(new ChordProgressionComparator());

	@Test
	public void testCreateHaronyChords1() {
		loadChordFormulas();
		Pitch p = new Pitch("C");
		rootPitches.add(p);
		Map<String, HarmonyChord> harmonyChords = ChordManager.createHarmonyChords(rootPitches, chordFormulas, Key.C_MAJOR);
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
		
		System.out.println("ChordProgression1: " + cp1.toString());
		System.out.println("ChordProgression2: " + cp2.toString());
		System.out.println("ChordProgression3: " + cp3.toString());
		
		// test TreeMap
		collectorStatsMap.put(cp1, new Integer(10));
		if(collectorStatsMap.containsKey(cp2)) {
			System.out.println("map contains key: " + cp2.toString());
			Integer val = collectorStatsMap.get(cp2);
			collectorStatsMap.put(cp1, new Integer(15));
		}
		else {
			System.out.println("map does NOT contain key: " + cp2.toString());
		}
		if(!collectorStatsMap.containsKey(cp3)) {
			System.out.println("map does NOT contain key: " + cp3.toString());
			collectorStatsMap.put(cp3,  new Integer(30));
		}
		System.out.println("Keys/Values");
		for(ChordProgression cp : collectorStatsMap.keySet()) {
			Integer ival = collectorStatsMap.get(cp);
			System.out.println(cp.toString() + "  " + ival);
		}
	}
	
	@Test
	public void testCreateHaronyChords2() {
		loadChordFormulas();
		HarmonyChord hc1 = new HarmonyChord("D7", chordFormulas);
		HarmonyChord hc2 = new HarmonyChord("C", chordFormulas);
		HarmonyChord hc3 = new HarmonyChord("C#", chordFormulas);
		HarmonyChord hc4 = new HarmonyChord("Bbm7", chordFormulas);

		System.out.println("D7: " + hc1);
		System.out.println("C: " + hc2);
		System.out.println("C#: " + hc3);
		System.out.println("Bbm7: " + hc4);
		// negative test
		HarmonyChord hc5 = new HarmonyChord("DbX", chordFormulas);
		System.out.println("DbX: " + hc5);
		
	}
	
	public void loadChordFormulas() {
		Configuration configuration =  Configuration.getInstance(CONFIG_FILENAME);
		Properties configProperties = configuration.getProperties();
		String dbname = configProperties.getProperty("mongodb.db.name");
		chordFormulas = ChordManager.loadChordFormulas(dbname, "chord_formulas");
		log.warn(chordFormulas.size() + " chords loaded");

	}
}
