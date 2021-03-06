package org.dwbzen.music.action;

import org.dwbzen.music.instrument.Instrument;

public interface IScaler {
	Instrument getInstrument() ;
	void setInstrument(Instrument instrument);
	
	Double getMaxVal();
	void setMaxVal(Double maxVal);
	
	Double getMinVal();
	void setMinVal(Double minVal);

	void setRange(Number maxX, Number minX);
}
