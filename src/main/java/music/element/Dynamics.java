package music.element;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Dynamics implements Serializable {

	private static final long serialVersionUID = 3305343395503717813L;

	/**
	 * Roughly arranged as softest to loudest
	 * @author don_bacon
	 *
	 */
	public static enum Dynamic {
		PPPPPP(0), PPPPP(1), PPPP(2), PPP(3), SFPP(4), PP(5), SFP(6), P(7),
		MP(8), FP(9), MF(10), F(11), RF(12), SF(13), FZ(14), SFZ(15), RFZ(16), 
		FF(17), SFFZ(18), FFF(19), FFFF(20), FFFFF(21), FFFFFF(22);
		Dynamic(int val) { this.value = val;}
		private final int value;
	    public int value() { return value; }
	}
	public static final String[] DynamicNames = {
		"pppppp", "ppppp", "pppp", "ppp", "sffp", "pp", "sfp", "p",
		"mp", "fp", "mf", "f", "rf", "sf", "fz", "sfz", "rfz",
		"ff", "sffz", "fff", "ffff", "fffff", "ffffff"
	};

	@JsonProperty("dynamic") private Dynamic dynamic = Dynamic.F;
	
	public Dynamics() {
		this.dynamic = Dynamic.F;	// sensible default
	}
	public Dynamics(Dynamic d) {
		this.dynamic = d;
	}

	public String getDynamicName() {
		return DynamicNames[dynamic.value];
	}
	
	public Dynamic getDynamic() {
		return dynamic;
	}

	public void setDynamic(Dynamic dynamic) {
		this.dynamic = dynamic;
	}
	
}
