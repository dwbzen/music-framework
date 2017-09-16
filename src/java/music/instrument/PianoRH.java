package music.instrument;

import music.element.Cleff;

public class PianoRH extends Piano {
	private static final long serialVersionUID = 8520579724073011712L;

	public PianoRH() {
		super();
		getCleffs().add(Cleff.G);
	}
}
