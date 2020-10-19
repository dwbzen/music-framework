package junit;

import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;
import org.dwbzen.music.element.Alteration;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.Scales;
import org.dwbzen.music.element.Step;

public class PitchTest extends TestCase {
	static final org.apache.log4j.Logger log = Logger.getLogger(PitchTest.class);
	static Pitch eflat4 = new Pitch("Eb4");
	static Pitch p3 = new Pitch("Bb4");
	static Pitch p4 = new Pitch("F#5");
	static Pitch fsharp3 = new Pitch("F#3");
	static Pitch c0 = new Pitch("C0");
	static Pitch b4 = new Pitch("B4");
	static Pitch e3 = new Pitch("E3");
	static Pitch csharp6 = new Pitch("C#6");
	static Pitch bsharpsharp3 = new Pitch("B##3");
	static Pitch eflatflat3 = new Pitch("Ebb3");
	
	/*
	 * Octave neutral
	 */
	static Pitch c = Pitch.C;
	static Pitch a = Pitch.A;
	static Pitch eflat = Pitch.EFlat;
	static Pitch bsharp = Pitch.BSharp;
	static Pitch gflat = Pitch.GFlat;
	static Pitch bflat = Pitch.BFlat;
	
	/*
	 * Out of bounds
	 */
	static Pitch c9 = new Pitch("C9");
	
	/*
	 * Keys
	 */
	static Key EbMajor = Key.EFlat_MAJOR;
	static Key BbMajor = Key.BFlat_MAJOR;
	static Key Fmajor = Key.F_MAJOR;
	
	/**
	 * @param args
	 */
	public static void main(String... args) {
	}
	
	@Test
	public void testChromaticHash() {
		Scale cscale = Scales.FULL_RANGE_CHROMATIC_SCALE;
		Map<Pitch, Integer> pitchHash = new TreeMap<Pitch, Integer>();
		for(Pitch p : cscale.getPitches()) {
			pitchHash.put(p, p.getRangeStep());
		}
		log.debug(c0 + " index: " + pitchHash.get(c0));
		log.debug(c9 + " index: " + pitchHash.get(c9));
		assertEquals(0, pitchHash.get(c0).intValue());
		assertEquals(108, pitchHash.get(c9).intValue());
		
		Pitch cflat2 = new Pitch("Cb2");
		Pitch b1 = new Pitch("B1");
		log.debug(cflat2 + " index: " + pitchHash.get(cflat2));
		log.debug(b1 + " index: " + pitchHash.get(b1));
		assertEquals(23, pitchHash.get(cflat2).intValue());
		assertEquals(23,  pitchHash.get(b1).intValue());
		
		for(Pitch tp : Scales.CHROMATIC_12TONE_SCALE.getPitches()) {
			assertTrue(pitchHash.containsKey(tp));
			log.debug(tp + " index: " + pitchHash.get(tp));
		}

		log.debug(eflat4 + " " + eflat4.hashCode());
		log.debug(eflat + " " + eflat.hashCode());
	}

	@Test
	public void testPitchDifference() {

		showDifference(b4, eflat4, -8);
		showDifference(b4, p3, -1);
		showDifference(b4, p4, 7);
		showDifference(b4, fsharp3, -17);
		showDifference(c0, csharp6, 73);
		showDifference(b4, e3, -19);
		showDifference(csharp6, csharp6, 0);
	}
	
	@Test
	public void testPitchDifferenceOctaveNeutral() {
		
		showDifference(c, a, 9);
		showDifference(c, eflat, 3);
		showDifference(bsharp, gflat, 6);
		showDifference(c, c, 0);
		showDifference(bsharpsharp3, eflatflat3, -11);
	}
	
	@Test
	public void testBoundryConditions() {
		showDifference(c0, c9, 108);
	}
	
	/**
	 * Tests PitchScaler setKey()
	 */
	@Test
	public void testPitchDifferenceWithKey() {
		showDifference(BbMajor, -2);
		showDifference(EbMajor, 3);
		showDifference(Fmajor, 5);
		showDifference(Key.C_MAJOR, 0);
		showDifference(Key.A_MAJOR, -3);
		showDifference(Key.DFlat_MAJOR, 0);		// should get a warning
	}
	
	private static void showDifference(Key key, int expectedDiff) {
		Pitch root = key.getRoot();
		int transposeSteps = root.difference(Pitch.C, key);
		log.debug("root: " + root + " key:" + key + " steps:" + transposeSteps);
		assertEquals(expectedDiff, transposeSteps);
	}
	
	private static void showDifference(Pitch p1, Pitch p2, int expectedDiff) {
		int diff = p1.difference(p2);
		log.info(p1.toString() + "  " + p1.getRangeStep() + " " +
				p2.toString() + " " + p2.getRangeStep() + " diff: " + diff);
		
		int diff2 = p2.difference(p1);
		log.info(p2.toString() + "  " + p2.getRangeStep() + " " +
				p1.toString() + " " + p1.getRangeStep() + " diff: " + diff2);
		assertEquals(expectedDiff, diff);
		assertEquals((-expectedDiff), diff2);
		
	}
	
	@Test
	public void testEquality() {
		
		assertEquals(eflat.compareTo(eflat4), -1);
		
		Pitch p1 = new Pitch(Step.C, 2, Alteration.UP_ONE);			// C#2
		Pitch p2 = new Pitch(Step.D, 2, Alteration.DOWN_ONE);		// Db2
		assertTrue(p1.equals(p2));
		assertEquals(p1.compareTo(p2),0);
		
		p1 = new Pitch(Step.C, 2, Alteration.DOWN_ONE);		// Cb2 (same as B1)
		p2 = new Pitch(Step.B, 1, Alteration.NONE);			// B1
		assertTrue(p1.equals(p2));
		assertEquals(p1.compareTo(p2),0);
		
		p1 = new Pitch(Step.C, 2, Alteration.NONE);		// C2
		p2 = new Pitch(Step.B, 1, Alteration.UP_ONE);	// B#1 (same as C2)
		assertTrue(p1.equals(p2));
		assertEquals(p1.compareTo(p2),0);

		p1 = new Pitch(Step.C, Alteration.NONE);		// C
		p2 = new Pitch(Step.B, Alteration.UP_ONE);		// B# (same as C)
		assertTrue(p1.equals(p2));
		assertEquals(p1.compareTo(p2),0);

		p1 = new Pitch(Step.C, 2, Alteration.NONE);		// C2
		p2 = new Pitch(Step.B, 2, Alteration.UP_ONE);	// B#2 (NOT same as C2, it's ~C3)
		assertTrue(!p1.equals(p2));
		assertEquals(p1.compareTo(p2),-1);
		assertEquals(p2.compareTo(p1), 1);
		
		p1 = new Pitch(Step.C, 4, Alteration.NONE);
		p2 = new Pitch(Step.D, 4, Alteration.NONE);
		assertEquals(p1.compareTo(p2),-1);
		assertEquals(p2.compareTo(p1), 1);
		
		// show ordering with a TreeMap
		Map<Pitch, String> pitchMap = new TreeMap<>();
		pitchMap.put(Pitch.A, "A");
		pitchMap.put(Pitch.B, "B");	
		pitchMap.put(Pitch.C, "C");		
		pitchMap.put(Pitch.D, "D");		
		pitchMap.put(Pitch.E, "E");		
		pitchMap.put(Pitch.F, "F");		
		pitchMap.put(Pitch.G, "G");
		System.out.println(pitchMap);
	}
}
