package music.instrument.vocal;

import music.element.Cleff;

public class SopranoVoice extends Voice {
	private static final long serialVersionUID = -4522973109005892078L;
	public final static String NAME = "Soprano";

	public SopranoVoice() {
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
