package junit;

import java.util.List;

import org.apache.log4j.Logger;

import org.junit.Test;

import junit.framework.TestCase;
import org.dwbzen.common.math.MathUtil;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.RhythmScale;
import org.dwbzen.music.element.rhythm.IRhythmTextureMap;
import org.dwbzen.music.transform.ITransformer.Preference;
import org.dwbzen.util.Ratio;
import org.dwbzen.util.music.Monophonic16StandardRhythmScaleFactory;

public class RhythmScaleTest extends TestCase  {
	static final org.apache.log4j.Logger log = Logger.getLogger(RhythmScaleTest.class);
	static Monophonic16StandardRhythmScaleFactory factory = Monophonic16StandardRhythmScaleFactory.getInstance();
	static RhythmScale rs = factory.createRhythmScale("Rhythm Scale - no chords");
	// {30, 60, 90, 180, 120, 150, 210, 240, 270, 300, 330, 360, 390, 420, 450, 480};
	
	@Test
	public void testFindClosestUnits() {
		double rawUnits = 0;
		int units = 0;
		for(int bunit : rs.getBaseUnits()) {
			rawUnits = (double)bunit;
			units = rs.findClosestUnits(rawUnits, Preference.Up);
			log.info("Closest units to " + rawUnits + " is " + units);
			assertEquals(units, bunit);
		}
		rawUnits = 140;
		units = rs.findClosestUnits(rawUnits, Preference.Up);
		log.info("Closest units to " + rawUnits + " is " + units);
		assertEquals(150, units);
		
		rawUnits = 440;
		units = rs.findClosestUnits(rawUnits, Preference.Up);
		log.info("Closest units to " + rawUnits + " is " + units);
		assertEquals(450, units);
		
		rawUnits = 315;
		units = rs.findClosestUnits(rawUnits, Preference.Up);
		log.info("Closest units to " + rawUnits + " is (Preferrence.Up) " + units);
		assertEquals(330, units);
		units = rs.findClosestUnits(rawUnits, Preference.Down);
		log.info("Closest units to " + rawUnits + " is (Preference.Down) " + units);
		assertEquals(300, units);
		
	}
	
	@Test
	public void testGetNoteType() {
		for(int i=1; i<=8;i++) {
			log.debug(i + " " + MathUtil.log2(i));
		}
		
		Note note = null;
		Pitch pitch = Pitch.C;
		for(int units : rs.getBaseUnits()) {
			List<Duration> factors = rs.getFactors(units);
			log.info("units: " + units);
			for(Duration dur : factors) {
				note = new Note(pitch, dur);
				String noteType = rs.getNoteType(note);
				note.setNoteType(noteType);
				log.info("  " + note);
			}
		}
	}
	
	@Test
	public void testGetExtrametricNoteType() {
		int[] unitsArray = {120, 240, 480};
		Note note = null;
		Pitch pitch = Pitch.C;
		for(int units : unitsArray) {
			log.info("units: " + units);
			IRhythmTextureMap rtm = rs.getExpressions().get(units);
			for(IRhythmExpression rext : rtm.getRhythmExpressions()) {
				Ratio ratio = rext.getRatio();
				for(Duration dur : rext.getFactors()) {
					note = new Note(pitch, dur);
					String noteType = rs.getNoteType(note);
					note.setNoteType(noteType);
					log.info("  " + ratio + " " + note);
				}
			}
		}
	}
}
