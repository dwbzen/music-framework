package junit;

import org.apache.log4j.Logger;

import junit.framework.TestCase;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.Scales;
import org.dwbzen.util.music.PitchCollection;
import org.dwbzen.util.music.ScaleManager;

public class ScaleManagerTest   extends TestCase {
	static final org.apache.log4j.Logger log = Logger.getLogger(ScaleManagerTest.class);
	
	String[] commonScales = {"Harmonic minor", "JG Octatonic", "Pyramid Hexatonic", "Hirajoshi Japan"};
	String[] mappedScales = {"D-HarmonicMinor", "Hirajoshi Japan in D"};
	String[] theoreticalScales = {"Theoretical-2-1-1-1-4-1-1-1", "Theoretical-2-1-1-1-7"};
	ScaleManager scaleManager = new ScaleManager();

	public void testGetCommonScale() {
		Pitch rootPitch = new Pitch("G");
		for(String scaleName : commonScales) {
			Scale scale = scaleManager.getScale(scaleName, rootPitch);
			assertNotNull(scale);
			String s = scale.toString();
			log.debug(s);
		}
	}
	
	public void testGetTheoreticalScale() {
		Pitch rootPitch = new Pitch("D");
		for(String scaleName : theoreticalScales) {
			Scale scale = scaleManager.getScale(scaleName, rootPitch);
			assertNotNull(scale);
			log.debug(scale.toString());
		}
	}
	
	public void testGetMappedScale() {
		for(String scaleName : mappedScales) {
			Scale scale = scaleManager.getScale(scaleName);
			assertNotNull(scale);
			log.debug(scale.toString());
		}
	}
	
	public void testToJson() {
		Pitch pitch = new Pitch("Ab4");
		String ps = pitch.toJson();
		assertNotNull(ps);
		System.out.println(ps);

		Scale scale = Scales.G_HARMONIC_MINOR;
		String json = scale.toJson();
		assertNotNull(json);
		System.out.println(json);
		String sstring = scale.toString();
		assertNotNull(sstring);
		System.out.println("scale: " + sstring);
	}
	
	public void testGetUnknownScale() {
		Scale scale = scaleManager.getScale("Unknown Scale");
		assertNull(scale);
	}
	
	public void testGetScaleTriads() {
		Scale scale = Scales.EFlat_MINOR_PENTATONIC;
		PitchCollection pc = scale.createScaleTriads(4);
		System.out.println(pc.toString());
	}
	
	public void testGetScaleSevenths() {
		Scale scale = Scales.C_MAJOR;
		PitchCollection pc = scale.createScaleSevenths(3);
		System.out.println(pc.toString());
	}
}
