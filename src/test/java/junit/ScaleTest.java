package junit;

import org.junit.Test;

import junit.framework.TestCase;
import music.element.Alteration;
import music.element.Pitch;
import music.element.Scale;
import music.element.Scales;
import music.element.Step;


public class ScaleTest extends TestCase {
	
	public ScaleTest() {
	}

	
	@Test
	public void testPitchMapping() {
		Pitch pitch = new Pitch(Step.G, 3, Alteration.NONE);
		System.out.println("pitch: " + pitch.toJson());
		assertEquals(pitch.getChromaticScaleDegree(), 8);
		assertEquals(pitch.getAbsoluteChromaticScaleDegree(), 44);
	}
	
	@Test
	public void testScaleMapping() {
		Scale gmajor = Scales.G_MAJOR;
		System.out.println(gmajor.toJson());
		assertEquals(gmajor.getNotes(), "G, A, B, C, D, E, F#, G");
	}
}
