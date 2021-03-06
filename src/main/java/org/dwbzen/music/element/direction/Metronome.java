package org.dwbzen.music.element.direction;

import org.dwbzen.music.element.NoteType;
import org.dwbzen.music.element.direction.ScoreDirection.ScoreDirectionType;

/**
 * A score metronome marking. Default is quaterNote = 80.
 * 
 * @author don_bacon
 *
 */
public class Metronome extends DirectionType {

	static final long serialVersionUID = 1L;
	private NoteType beatUnit = NoteType.QUARTER;
	private int beatsPerMinute = 80;
	
	public Metronome() {
		super("metronome");
		scoreDirectionType = ScoreDirectionType.METRONOME;
	}
	
	public Metronome(int bpm) {
		this();
		beatsPerMinute = bpm;
	}
	
	public Metronome(String name) {
		super(name);
	}

	public NoteType getBeatUnit() {
		return beatUnit;
	}

	public void setBeatUnit(NoteType beatUnit) {
		this.beatUnit = beatUnit;
	}

	public int getBeatsPerMinute() {
		return beatsPerMinute;
	}

	public void setBeatsPerMinute(int beatsPerMinute) {
		this.beatsPerMinute = beatsPerMinute;
	}

}
