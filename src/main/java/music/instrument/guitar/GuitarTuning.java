package music.instrument.guitar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import music.element.Pitch;

/**
 * 
 * @author Don Bacon
 * 
 * <p>See 
 * <a href="https://en.wikipedia.org/wiki/Guitar_tunings">Guitar Tunings</a> on Wikipedia
 *
 */
public class GuitarTuning {
	
	static final Pitch D3 = new Pitch("D3");
	static final Pitch E3 = new Pitch("E3");
	static final Pitch A3 = new Pitch("A3");
	static final Pitch D4 = new Pitch("D4");
	static final Pitch G4 = new Pitch("G4");
	static final Pitch B4 = new Pitch("B4");
	static final Pitch E5 = new Pitch("E5");

	
	private List<Pitch> tuning = new ArrayList<Pitch>();
	
	public GuitarTuning() {
		
	}
	
	public GuitarTuning(List<Pitch> notes) {
		notes.forEach(e -> tuning.add(new Pitch(e)));
	}
	
	protected GuitarTuning(Pitch[] notes) {
		tuning.addAll(Arrays.asList(notes));
	}

	public List<Pitch> getTuning() {
		return tuning;
	}

	static final Pitch[] standardNotes = {E3, A3, D3, G4, B4, E5};
	public static final GuitarTuning STANDARD = new GuitarTuning(standardNotes);
	
	static final Pitch[] dropDNotes = {D3, A3, D3, G4, B4, E5};
	public static final GuitarTuning DROP_D = new GuitarTuning(dropDNotes);

	
}
