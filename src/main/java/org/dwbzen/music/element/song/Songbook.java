package org.dwbzen.music.element.song;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.dwbzen.common.util.INameable;

/**
 * A collection of Song(s) treated as a unit.
 * 
 * @author don_bacon
 *
 */
public class Songbook extends ArrayList<Song> implements INameable {

	private static final long serialVersionUID = -2295768600438776098L;
	private String name = "My Songbook";
	private Map<String, Integer> index = new TreeMap<String, Integer>();	// index of Song names
	
	public Songbook() {
		super();
	}
	
	@Override
	public boolean add(Song song) {
		boolean added = super.add(song);
		if(added) {
			index.put(song.getName(), size()-1);
		}
		return added;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Creates a ChordProgression from the Song's Sections, delimited
	 * by silent HarmonyChords.
	 * @param songName
	 * @return ChordProgression
	 */
	public ChordProgression getChordProgressionView(String songName) {
		Song song = get(songName);
		ChordProgression chordProgression = new ChordProgression();
		if(song != null) {
			for(Section section : song.getSections()) {
				List<SongMeasure> measures = section.getSongMeasures();
				chordProgression.addAll(measures);
				/*
				 * Add a silent HarmonyChord to indicate end of section
				 */
				chordProgression.add(HarmonyChord.SILENT);
			}
		}
		return chordProgression;
	}
	/**
	 * Creates a ChordProgression from all the Sections of all
	 * the Song's in the Songbook, delimited by silent HarmonyChords.
	 * 
	 * @return
	 */
	public ChordProgression getChordProgressionView() {
		ChordProgression chordProgression = new ChordProgression();
		for(String name : index.keySet()) {
			chordProgression.add(getChordProgressionView(name));
		}
		return chordProgression;
	}
	
	public boolean containsKey(Object key) {
		return index.containsKey(key);
	}

	public boolean containsValue(int value) {
		return value >=0 && value < size();
	}

	public Song get(Object key) {
		Song song = null;
		if(index.containsKey(key)) {
			Integer ind = index.get(key);
			song = this.get(ind);
		}
		return song;
	}

	public Set<String> keySet() {
		return index.keySet();
	}
}
