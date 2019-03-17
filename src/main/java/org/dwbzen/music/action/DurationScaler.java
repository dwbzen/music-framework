package org.dwbzen.music.action;

import org.dwbzen.common.math.Point2D;
import org.dwbzen.music.element.Duration;
import org.dwbzen.music.element.IRhythmScale;
import org.dwbzen.music.instrument.Instrument;

public class DurationScaler extends Scaler {

	private IRhythmScale rhythmScale = null;
	
	public DurationScaler(Instrument inst) {
		super(inst);
		rhythmScale = inst.getRhythmScale();
	}

	public DurationScaler(Instrument inst, Number maxX, Number minX) {
		super(inst, maxX, minX);
		rhythmScale = inst.getRhythmScale();
	}
	
	public Duration scale(Point2D<Double> dval) {
		return scale(dval.getY());
	}
	
	public Duration scale(double dval) {
		return scale(Double.valueOf(dval));
	}
	
	/**
	 * This takes a raw Number in a given range, scales and rounds
	 * according to the duration range of the associated Instrument
	 * @param num a Number in the range [maxVal, minVal]
	 * @return Duration with durationSeconds set
	 */
	public Duration scale(Number num) {
		if(drange == null) {
			drange = maxVal  - minVal ;
		} 
		double[] dr = instrument.getDurationRangeSeconds();
		double d = dr[0] + ( ((num.doubleValue() - minVal)/drange) *(dr[1] - dr[0]) );
		Duration duration = new Duration(d);
		return duration;
	}
	
	/**
	 * Scales a raw number to the instrument's RhythmScale range
	 * @param num - Number to scale
	 * @return new Duration with rawValue set to the scaled value
	 */
	public Duration scaleToRhythmScale(Number num) {
		if(drange == null) {
			drange = maxVal  - minVal ;
		} 
		Duration duration = null;
		double scaleFactor = rhythmScale.getRange() / drange;
		double numScaled = scaleFactor * (num.doubleValue() - minVal);
		duration= new Duration(numScaled);
		return duration;
	}

	public IRhythmScale getRhythmScale() {
		return rhythmScale;
	}

	public void setRhythmScale(IRhythmScale rhythmScale) {
		this.rhythmScale = rhythmScale;
	}
	
}
