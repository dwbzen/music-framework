package org.dwbzen.music.instrument.vocal;

import org.dwbzen.music.element.Cleff;

public class BassVoice extends Voice {
	private static final long serialVersionUID = -7491778882675941870L;
	public final static String NAME = "Bass";

	public BassVoice() {
		super();
		getCleffs().add(Cleff.F);
		setName(NAME);
	}

}
