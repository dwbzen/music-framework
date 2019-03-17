package test;

import java.util.List;

import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.PitchRange;
import org.dwbzen.music.instrument.Bassoon;
import org.dwbzen.music.action.PitchScaler;

public class BassoonTest {

	public static void main(String... args) {
		Bassoon instrument = new Bassoon();
		PitchRange pr = instrument.getPitchRange();
		System.out.println("range: " + pr.getLow().toString() + " to " + pr.getHigh().toString());
		List<Pitch> notes = instrument.getNotes();
		for(Pitch p : notes) {
			System.out.print(p.toString() + ", " );
		}
		System.out.println("");
		PitchScaler ps = instrument.getPitchScaler();
		ps.setMinVal(.001);
		ps.setMaxVal(.999);
		System.out.println("scale .99: " + ps.scale(.99));
		System.out.println("scale .01: " + ps.scale(.01));
		Pitch pitch = instrument.stepNumberToPitch(1);
		System.out.println("step #1 pitch: " + pitch.toString());

		int len = instrument.getLength();
		System.out.println("len: "  + len);
		pitch = instrument.stepNumberToPitch(len);
		System.out.println("step #" + len + " pitch: " + pitch.toString());
	}
}


