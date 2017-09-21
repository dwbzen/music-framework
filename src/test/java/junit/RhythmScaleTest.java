package junit;

import java.util.List;

import junit.framework.TestCase;
import math.MathUtil;
import music.element.Duration;
import music.element.IRhythmExpression;
import music.element.Note;
import music.element.Pitch;
import music.element.RhythmScale;
import music.element.rhythm.IRhythmTextureMap;
import util.Ratio;
import util.music.Monophonic16StandardRhythmScaleFactory;

public class RhythmScaleTest extends TestCase  {

	static Monophonic16StandardRhythmScaleFactory factory = Monophonic16StandardRhythmScaleFactory.getInstance();
	static RhythmScale rs = factory.createRhythmScale("Rhythm Scale - no chords");
	
	public void testGetNoteType() {
		for(int i=1; i<=8;i++) {
			System.out.println(i + " " + MathUtil.log2(i));
		}
		
		int root = 16;
		Note note = null;
		Pitch pitch = Pitch.C;
		for(int units = 1; units<=root; units++) {
			List<Duration> factors = rs.getFactors(units);
			System.out.println("units: " + units);
			for(Duration dur : factors) {
				note = new Note(pitch, dur);
				String noteType = rs.getNoteType(note);
				note.setNoteType(noteType);
				System.out.println("  " + note);
			}
		}
	}
	
	public void testGetExtrametricNoteType() {
		int[] unitsArray = {4, 8, 16};
		Note note = null;
		Pitch pitch = Pitch.C;
		for(int units : unitsArray) {
			System.out.println("units: " + units);
			IRhythmTextureMap rtm = rs.getExpressions().get(4);
			for(IRhythmExpression rext : rtm.getRhythmExpressions()) {
				Ratio ratio = rext.getRatio();
				for(Duration dur : rext.getFactors()) {
					note = new Note(pitch, dur);
					String noteType = rs.getNoteType(note);
					note.setNoteType(noteType);
					System.out.println("  " + ratio + " " + note);
				}
			}
		}
	}
}
