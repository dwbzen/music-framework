package music.instrument.vocal;

import music.element.Cleff;

public class AltoVoice extends Voice{
	private static final long serialVersionUID = 4592483160368689664L;
	public final static String NAME = "Alto";

	public AltoVoice() {
		super();
		getCleffs().add(Cleff.G);
	}
	
	@Override
	public String getName() {
		if(name == null) {
			name = NAME;
		}
		return name;
	}
}
