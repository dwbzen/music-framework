package junit;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;
import org.dwbzen.music.element.Alteration;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.Scales;
import org.dwbzen.music.element.Step;


public class ScaleTest extends TestCase {
	static final org.apache.log4j.Logger log = Logger.getLogger(ScaleTest.class);
	public ScaleTest() {
	}

	
	@Test
	public void testPitchMapping() {
		Pitch pitch = new Pitch(Step.G, 3, Alteration.NONE);
		log.info("pitch: " + pitch.toJson());
		assertEquals(pitch.getChromaticScaleDegree(), 8);
		assertEquals(pitch.getAbsoluteChromaticScaleDegree(), 44);
	}
	
	@Test
	public void testScaleMapping() {
		Scale gmajor = Scales.G_MAJOR;
		log.info(gmajor.toJson());
		assertEquals(gmajor.getNotes(), "G, A, B, C, D, E, F#, G");
	}
}
