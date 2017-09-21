package music.junit;

import junit.framework.TestCase;
import music.action.PitchScaler;
import music.element.Pitch;
import music.element.PitchRange;
import music.instrument.Flute;
import music.instrument.Instrument;

public class ScalerTest extends TestCase  {

	private Double minY = 0.0;
	private Double maxY = .9990596;
	private Instrument instrument = null;
	private String[] pitchRange={"E4", "A6"};
	
	/**
	 * @param args
	 */
	public static void main(String... args) {
	}
	

	public void testPitchScaler() {
		setup();
		Pitch low = new Pitch(pitchRange[0]);
		Pitch high = new Pitch(pitchRange[1]);
		PitchRange pr = new PitchRange(low, high);
		instrument.setPitchRange(pr);
		PitchScaler pitchScaler = new PitchScaler(instrument, maxY, minY);
		Pitch pitch = null;
		double d = minY;
		System.out.println("range: " + low + " to " + high);
		while(d <= maxY) {
			pitch = pitchScaler.scale(d);
			assertTrue(pitch.compareTo(low) >= 0);
			assertTrue(pitch.compareTo(high) <= 0);
			System.out.println(d + ": " + pitch);
			d +=.01;
		}
		pitch = pitchScaler.scale(maxY);
		assertTrue(pitch.compareTo(high) <= 0);
		System.out.println(maxY + ": " + pitch);
	}
	
	private void setup() {
		instrument  = new Flute();
	}
}
