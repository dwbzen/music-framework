package junit;

import org.apache.log4j.Logger;

import junit.framework.TestCase;
import music.element.Pitch;
import music.element.Scale;
import util.music.ScaleManager;

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
			log.debug(scale.toJson());
		}
	}
	
	public void testGetTheoreticalScale() {
		Pitch rootPitch = new Pitch("D");
		for(String scaleName : theoreticalScales) {
			Scale scale = scaleManager.getScale(scaleName, rootPitch);
			assertNotNull(scale);
			log.debug(scale.toJson());
		}
	}
	
	public void testGetMappedScale() {
		for(String scaleName : mappedScales) {
			Scale scale = scaleManager.getScale(scaleName);
			assertNotNull(scale);
			log.debug(scale.toJson());
		}
	}
	
	public void testGetUnknownScale() {
		Scale scale = scaleManager.getScale("Unknown Scale");
		assertNull(scale);
	}
}
