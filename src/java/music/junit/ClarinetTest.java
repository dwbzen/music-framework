package music.junit;

import java.util.List;

import music.action.PitchScaler;
import music.element.Key;
import music.element.Pitch;
import music.element.PitchRange;
import music.instrument.Clarinet;

public class ClarinetTest {

	public static void main(String... args) {
		Clarinet instrument = new Clarinet();
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

		Key key1 = instrument.getKey();
		Key keyc = instrument.getKey(Key.C_MAJOR);
		Key key2 = instrument.getKey(Key.G_MINOR);
		
		System.out.println("instrument key: " + key1.toString());
		System.out.println("instrument key (C): " + keyc.toString());
		System.out.println("instrument key (Gm): " + key2.toString());
		
	}
}
