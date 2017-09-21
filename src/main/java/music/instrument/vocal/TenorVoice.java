package music.instrument.vocal;

import music.element.Cleff;

public class TenorVoice extends Voice {
	private static final long serialVersionUID = 3023254191642841093L;
	public final static String NAME = "Tenor";

	public TenorVoice() {
		super();
		getCleffs().add(Cleff.G8ma);
	}
	
	@Override
	public String getName() {
		if(name == null) {
			name = NAME;
		}
		return name;
	}
}
