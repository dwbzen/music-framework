package music.action;

import music.instrument.Instrument;

/**
 * Scales a Point to a pitch (for a particular instrument or range)
 * or a time interval (duration)
 * 
 * @author dbacon
 *
 */
public abstract class Scaler implements IScaler {
	protected Double maxVal = 1.0;
	protected Double minVal = 0.0;
	protected Instrument instrument = null;
	protected Double drange = null;

	/**
	 * Ceiling - ceiling to next half step
	 * Floor - floor to half step
	 * Round - round to nearest pitch - up or down
	 * UpKey - up to the next pitch in the selected key
	 * DownKey - down to previous pitch in selected key
	 * NOTE - UpKey and DownKey are deprecated. Use Transform instead.
	 * @author don_bacon
	 *
	 */
	public static enum Rounder {
		CEILING(0), FLOOR(1), ROUND(2);
		Rounder(int val) { this.value = val;}
		private final int value;
	    public int value() { return value; }
	};
	

	public Scaler(Instrument inst, Number maxX, Number minX) {
		this.instrument = inst;
		setRange(maxX, minX);
	}
	public Scaler(Instrument inst) {
		this.instrument = inst;
	}
	
	public void setRange(Number maxX, Number minX) {
		maxVal = new Double(maxX.doubleValue());
		minVal = new Double(minX.doubleValue());
	}
	
	public Instrument getInstrument() {
		return instrument;
	}

	public void setInstrument(Instrument instrument) {
		this.instrument = instrument;
	}
	public Double getMaxVal() {
		return maxVal;
	}

	public void setMaxVal(Double maxVal) {
		this.maxVal = maxVal;
	}

	public Double getMinVal() {
		return minVal;
	}

	public void setMinVal(Double minVal) {
		this.minVal = minVal;
	}

}
