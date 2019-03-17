package org.dwbzen.music.instrument;

import java.io.Serializable;

public class MidiInstrument implements Serializable {

	private static final long serialVersionUID = -1984206755782635210L;
    protected int midiChannel;
    protected String midiName;
    protected int midiBank;
    protected int midiProgram;
    protected int midiUnpitched;
    protected double volume = 80;
    protected double pan = 0;
    protected double elevation;
    protected String id;

    public MidiInstrument(String id, int channel, String name) {
    	this.id = id;
    	this.midiChannel = channel;
    	this.midiName = name;
    }

	public int getMidiChannel() {
		return midiChannel;
	}

	public void setMidiChannel(int midiChannel) {
		this.midiChannel = midiChannel;
	}

	public String getMidiName() {
		return midiName;
	}

	public void setMidiName(String midiName) {
		this.midiName = midiName;
	}

	public int getMidiBank() {
		return midiBank;
	}

	public void setMidiBank(int midiBank) {
		this.midiBank = midiBank;
	}

	public int getMidiProgram() {
		return midiProgram;
	}

	public void setMidiProgram(int midiProgram) {
		this.midiProgram = midiProgram;
	}

	public int getMidiUnpitched() {
		return midiUnpitched;
	}

	public void setMidiUnpitched(int midiUnpitched) {
		this.midiUnpitched = midiUnpitched;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public double getPan() {
		return pan;
	}

	public void setPan(double pan) {
		this.pan = pan;
	}

	public double getElevation() {
		return elevation;
	}

	public void setElevation(double elevation) {
		this.elevation = elevation;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
    
}
