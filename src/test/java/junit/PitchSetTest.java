package junit;

import java.util.Map;

import org.apache.log4j.Logger;
import org.dwbzen.music.element.Alteration;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.PitchSet;
import org.junit.Test;

import junit.framework.TestCase;

public class PitchSetTest extends TestCase {
	static final org.apache.log4j.Logger log = Logger.getLogger(PitchTest.class);
	
	Map<Alteration, PitchSet> apcmap = PitchSet.allPitches;
	PitchSet apc = PitchSet.getAllPitches(Alteration.FLAT);
	String[] pitcharray = { "A2", "G2", "F#4", "A2", "F#2"  }; // inversion: 
	String[] inverted = {  "A2", "B2", "C1", "A2", "C3"  };
	String[] retrograde = {"F#2", "A2", "F#4", "G2", "A2" };
	String[] retrogradeInversion = { "C3", "A2", "C1", "B2", "A2"};
	
	PitchSet pc = new PitchSet(pitcharray);
	PitchSet pci = new PitchSet(inverted);
	PitchSet pcr = new PitchSet(retrograde);
	PitchSet pcri = new PitchSet(retrogradeInversion);
	
	String outputFormat = "both";
	
	String[] pitchArray2 = {"E5", "F5", "Db5", "Eb5", "C5", "D5", "G#4", "A4", "Bb4", "F#4", "G4", "B4" };		// Webern 
	String[] inverted2 = {"F#4", "F4", "A4", "G4", "Bb4", "Ab4", "D5 ", "Db5", "C5", "E5", "Eb5", "B4" };	// inverted from F#4
	
	@Test
	public void testInversion() {
		PitchSet result = pc.getInversion();
		PitchSet.displayPitchSet(pc, outputFormat);
		PitchSet.displayPitchSet(result, outputFormat);
		assertEquals(result.compareTo(pci), 0);
		
		PitchSet pc2 = new PitchSet(pitchArray2);
		PitchSet pci2 = new PitchSet(inverted2);
		Pitch fsharp4 = new Pitch("F#4");
		result = pc2.getInversion(fsharp4);
		PitchSet.displayPitchSet(pc, outputFormat);
		PitchSet.displayPitchSet(result, outputFormat);
		assertEquals(0, result.compareTo(pci2));

	}
	
	public void testRetrogradeInversion() {
		PitchSet result = pc.getRetrograde();
		assertEquals(0, result.compareTo(pcr));
		
		result = pc.getRetrogradeInversion();
		assertEquals(0, result.compareTo(pcri));
	}

}
