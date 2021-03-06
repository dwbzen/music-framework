package org.dwbzen.util;

import org.dwbzen.common.util.IJson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Ratio  implements IJson {

	/*
	 * some common ratios
	 */
	public static final Ratio ONE_TO_ONE = new Ratio(1,1);
	public static final Ratio THREE_TO_TWO = new Ratio(3,2);
	public static final Ratio FIVE_TO_FOUR = new Ratio(5,4);
	
	@JsonProperty("beats")	private Integer x = 0;
	@JsonProperty("timeOf")	private Integer y = 0;
	
	public Ratio(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public Ratio() {
		this(0, 0);
	}
	
	public Ratio(Ratio r) {
		x = r.x;
		y = r.y;
	}
	
	public Integer getBeats() {
		return getX();
	}
	/**
	 * Convenience method. #notes same as beats.
	 * @return
	 */
	public Integer getNumberOfNotes() {
		return getX();
	}
	
	public Integer getTimeOf() {
		return getY();
	}
	
	protected Integer getX() {
		return x;
	}
	protected void setX(Number x) {
		this.x = x.intValue();
	}

	protected Integer getY() {
		return y;
	}
	protected void setY(Number y) {
		this.y = y.intValue();
	}

	public String toString() {
		return x + ":" + y;
	}

}
