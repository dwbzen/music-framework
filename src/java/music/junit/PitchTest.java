package music.junit;

import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;
import music.element.Alteration;
import music.element.Key;
import music.element.Pitch;
import music.element.Scale;
import music.element.Step;

public class PitchTest extends TestCase {
	
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
	
	public static void testChromaticHash() {
		Scale cscale = Scale.FULL_RANGE_CHROMATIC_SCALE;
		Map<Pitch, Integer> pitchHash = new TreeMap<Pitch, Integer>();
		for(Pitch p : cscale.getPitches()) {
			pitchHash.put(p, p.getRangeStep());
		}
		System.out.println(c0 + " index: " + pitchHash.get(c0));
		System.out.println(c9 + " index: " + pitchHash.get(c9));
		assertEquals(0, pitchHash.get(c0).intValue());
		assertEquals(108, pitchHash.get(c9).intValue());
		
		Pitch cflat2 = new Pitch("Cb2");
		Pitch b1 = new Pitch("B1");
		System.out.println(cflat2 + " index: " + pitchHash.get(cflat2));
		System.out.println(b1 + " index: " + pitchHash.get(b1));
		assertEquals(23, pitchHash.get(cflat2).intValue());
		assertEquals(23,  pitchHash.get(b1).intValue());
		
		for(Pitch tp : Scale.CHROMATIC_12TONE_SCALE.getPitches()) {
			assertTrue(pitchHash.containsKey(tp));
			System.out.println(tp + " index: " + pitchHash.get(tp));
		}

		System.out.println(eflat4 + " " + eflat4.hashCode());
		System.out.println(eflat + " " + eflat.hashCode());
	}

	public static void testPitchDifference() {

		showDifference(b4, eflat4, -8);
		showDifference(b4, p3, -1);
		showDifference(b4, p4, 7);
		showDifference(b4, fsharp3, -17);
		showDifference(c0, csharp6, 73);
		showDifference(b4, e3, -19);
		showDifference(csharp6, csharp6, 0);
	}
	
	public static void testPitchDifferenceOctaveNeutral() {
		
		showDifference(c, a, 9);
		showDifference(c, eflat, 3);
		showDifference(bsharp, gflat, 6);
		showDifference(c, c, 0);
		showDifference(bsharpsharp3, eflatflat3, -11);
	}
	
	public static void testBoundryConditions() {
		showDifference(c0, c9, 108);
	}
	
	/**
	 * Tests PitchScaler setKey()
	 */
	public static void testPitchDifferenceWithKey() {
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
		System.out.println("root: " + root + " key:" + key + " steps:" + transposeSteps);
		assertEquals(expectedDiff, transposeSteps);
	}
	
	private static void showDifference(Pitch p1, Pitch p2, int expectedDiff) {
		int diff = p1.difference(p2);
		System.out.println(p1.toString() + "  " + p1.getRangeStep() + " " +
				p2.toString() + " " + p2.getRangeStep() + " diff: " + diff);
		
		int diff2 = p2.difference(p1);
		System.out.println(p2.toString() + "  " + p2.getRangeStep() + " " +
				p1.toString() + " " + p1.getRangeStep() + " diff: " + diff2);
		assertEquals(expectedDiff, diff);
		assertEquals((-expectedDiff), diff2);
		
	}
	
	public static void testEquality() {
		
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


	}
}
