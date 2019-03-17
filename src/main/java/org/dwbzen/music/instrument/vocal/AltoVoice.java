package org.dwbzen.music.instrument.vocal;

import org.dwbzen.music.element.Cleff;

public class AltoVoice extends Voice{
	private static final long serialVersionUID = 4592483160368689664L;
	public final static String NAME = "Alto";

	public AltoVoice() {
		super();
		getCleffs().add(Cleff.G);
		setName(NAME);
	}
	
}
