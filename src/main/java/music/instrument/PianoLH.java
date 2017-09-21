package music.instrument;

import music.element.Cleff;

public class PianoLH extends Piano {
	private static final long serialVersionUID = 4662584960835172115L;

	public PianoLH() {
		super();
		getCleffs().add(Cleff.F);
	}
}
