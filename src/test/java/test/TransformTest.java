package test;

import java.util.List;

import org.dwbzen.music.element.IScaleFormula;
import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Note;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.Scales;
import org.dwbzen.music.transform.ScaleTransformer;
import org.dwbzen.music.transform.ITransformer.Preference;

public class TransformTest {

	public static void main(String... args) {
		Scale scale = Scales.G_MINOR;
		Preference pref = Preference.Down;
		Key key = Key.C_MAJOR;
		List<Pitch> pitches = IScaleFormula.createPitches(IScaleFormula.CHROMATIC_SCALE_FORMULA, key.getRoot(), key);

		System.out.println("Scale to use: " + scale.toString());
		ScaleTransformer st = new ScaleTransformer(scale, pref);
		for(Pitch pitch : pitches) {
			Note noteToTransform = new Note(pitch, 4);	// duration doesn't matter for the test
			System.out.println("note Before: " + noteToTransform.getPitch().toString());
			st.transformNote(noteToTransform);
			System.out.println("     After: " + noteToTransform.getPitch().toString());
		}
	}
}
