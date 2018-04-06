package junit;

import org.junit.Test;

import junit.framework.TestCase;
import music.element.Chord;

public class ChordTest extends TestCase {


	static String[] chord1Notes = {"C3", "E3", "G3", "Bb3", "C4" };
	static String[] chord2Notes = { "B2", "D#3", "F#3", "A3", "B3" };
	static String[] chord3Notes = { "D3", "F#3", "A3", "C4", "D4" };
	static String[] chord4Notes = { "D3", "F#3", "A3", "C4", "D3" };	//intentional unison
	static String[] chord4aNotes = { "D3", "F#3", "A3", "C4" };
	static String[] chord5Notes =  { "D3", "Gb3", "A3", "C4", "D4" };	// F# same as Gb
	static String[] chord6Notes =  { "C##3", "Gb3", "A3", "C4", "D4" };	// F# same as Gb, C## == D

	static Chord chord1 = Chord.createChord(chord1Notes, 1);
	static Chord chord2 = Chord.createChord(chord2Notes, 1);
	static Chord chord3 = Chord.createChord(chord3Notes, 1);
	static Chord chord4 = Chord.createChord(chord4Notes, 1);
	static Chord chord4a = Chord.createChord(chord4aNotes, 1);
	static Chord chord5 = Chord.createChord(chord5Notes, 1);
	static Chord chord6 = Chord.createChord(chord6Notes, 1);
	
	@Test
	public void testEquals() {
		assertTrue(chord1.equals(chord1));
		assertTrue(!chord3.equals(chord4));
		assertTrue(chord3.equals(chord5));
		assertTrue(chord3.equals(chord6));
	}
	
	@Test
	public void testRemoveUnisonNotes() {
		Chord chord = new Chord(chord4.removeUnisonNotes());
		assertTrue(chord.equals(chord4));
		assertTrue(chord.equals(chord4a));
	}
	
	@Test
	public void testCompare() {
		assertEquals(chord2.compareTo(chord1), -1);
		assertEquals(chord3.compareTo(chord1), 1);
	}
	
	@Test
	public void testToJson() {
		String json = chord5.toJson();
		assertTrue(json != null && json.contains("\"type\":\"chord\""));
	}
}
