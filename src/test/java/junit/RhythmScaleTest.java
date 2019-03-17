package junit;

import java.util.List;

import org.apache.log4j.Logger;

import junit.framework.TestCase;
import org.dwbzen.common.math.MathUtil;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.IRhythmExpression;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.RhythmScale;
import org.dwbzen.music.element.rhythm.IRhythmTextureMap;
import org.dwbzen.util.Ratio;
import org.dwbzen.util.music.Monophonic16StandardRhythmScaleFactory;

public class RhythmScaleTest extends TestCase  {
	static final org.apache.log4j.Logger log = Logger.getLogger(RhythmScaleTest.class);
	static Monophonic16StandardRhythmScaleFactory factory = Monophonic16StandardRhythmScaleFactory.getInstance();
	static RhythmScale rs = factory.createRhythmScale("Rhythm Scale - no chords");
	
	public void testGetNoteType() {
		for(int i=1; i<=8;i++) {
			log.debug(i + " " + MathUtil.log2(i));
		}
		
		int root = 16;
		Note note = null;
		Pitch pitch = Pitch.C;
		for(int units = 1; units<=root; units++) {
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
	
	public void testGetExtrametricNoteType() {
		int[] unitsArray = {4, 8, 16};
		Note note = null;
		Pitch pitch = Pitch.C;
		for(int units : unitsArray) {
			log.info("units: " + units);
			IRhythmTextureMap rtm = rs.getExpressions().get(4);
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
