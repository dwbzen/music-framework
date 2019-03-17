package org.dwbzen.music.element.song;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.dwbzen.common.math.RaggedArray;
import org.dwbzen.common.util.IJson;
import org.dwbzen.common.util.INameable;

/**
 * A Song encapsulates a song as it would appear in a Guitar Fake Book
 * which typically is a single melody line and chord changes.
 * Lyrics may or may not be present in a Fake Book and they are not
 * included in the Song class.
 * Chord changes are represented by HarmonyChord instances in a Harmony structure.
 * Measures are encapsulated as SongMeasure that contains Harmony and Melody structures.
 * The structures mirror those in Score but simplified (in the same way a Fake Book is simplified).
 * Song metadata can be added to facilitate searching by composer, year, performance group (Band) etc.
 * 
 * @author don_bacon
 *
 */
public class Song implements IJson, INameable, Supplier<ChordProgression> {

	private static final long serialVersionUID = 8221829976304858453L;
	@JsonInclude(Include.NON_EMPTY)
	@JsonProperty("name")		private String name = null;		// the name is the song title
	@JsonProperty("artist")		private String artist = null;	// As in "The Beatles"
	@JsonProperty("composers")	private List<String> composers = new ArrayList<String>();
	@JsonProperty("year")		private int year;
	@JsonProperty("album")		private String album = null;
	@JsonProperty("track")		private int track = 0;
	@JsonProperty("sections")	private List<Section> sections = new ArrayList<Section>();
	@JsonProperty				private KeyLite performanceKey = null;	// default Key for performance (could be different than score Key)
	@JsonIgnore				private int index = -1;		// section counter for get()
	@JsonIgnore				private int numberOfMeasures = 0;
	@JsonIgnore				private int numberOfChords = 0;		// a sum of #Harmony in all the Sections in this song.
	@JsonIgnore				private RaggedArray<HarmonyChord, ChordProgression> cpArray = null;
	@JsonIgnore				private boolean originalKey = false;
	@JsonIgnore				private HarmonyChord sectionTerminator = HarmonyChord.TERMINAL_HARMONY_CHORD;

	public Song() {
		name = INameable.DEFAULT_NAME;
		artist = "None";
	}
	
	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public List<String> getComposers() {
		return composers;
	}
	
	public boolean addComposer(String composer) {
		return composers.add(composer);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Section> getSections() {
		return sections;
	}
	
	public boolean addSection(Section section) {
		numberOfMeasures += sections.size();
		numberOfChords += section.getNumberOfChords();
		section.setSong(this);
		return sections.add(section);
	}

	/**
	 * @return #of sections in the song
	 */
	public int size() {
		return sections.size();
	}
	
	public int getNumberOfMeasures() {
		return numberOfMeasures;
	}
	
	public KeyLite getPerformanceKey() {
		return performanceKey;
	}

	public void setPerformanceKey(KeyLite performanceKey) {
		this.performanceKey = performanceKey;
	}

	public int getTrack() {
		return track;
	}

	public void setTrack(int track) {
		this.track = track;
	}

	public int getNumberOfChords() {
		return numberOfChords;
	}

	public void setNumberOfChords(int numberOfChords) {
		this.numberOfChords = numberOfChords;
	}

	public void setComposers(List<String> composers) {
		this.composers = composers;
	}

	public void setNumberOfMeasures(int numberOfMeasures) {
		this.numberOfMeasures = numberOfMeasures;
	}

	public boolean isOriginalKey() {
		return originalKey;
	}

	public void setOriginalKey(boolean originalKey) {
		this.originalKey = originalKey;
	}

	public HarmonyChord getSectionTerminator() {
		return sectionTerminator;
	}

	public void setSectionTerminator(HarmonyChord sectionTerminator) {
		this.sectionTerminator = sectionTerminator;
	}

	/**
	 * Implements the Supplier get() function.
	 * Gets one section at a time. Returns NULL when no more sections;
	 * @return ChordProgression in the original (non-transposed) key
	 */
	@Override
	public ChordProgression get() {
		ChordProgression sectionChordProgression = null;
		if(++index <= size()-1 ) {
			List<SongMeasure> measures = sections.get(index).getSongMeasures();
			sectionChordProgression = new ChordProgression();
			sectionChordProgression.addAll(measures, originalKey);
		}
		return sectionChordProgression;
	}
	
	public void resetToBeginning() {
		index = -1;
	}
	
	/**
	 * Implements the Supplier get() function.
	 * if originalKey if true returns Chords in original key, otherwise transposed key
	 * @return ChordProgression in transposed key
	 */
	public ChordProgression get(boolean keepOriginalKey) {
		return get(this.originalKey, this.sectionTerminator);
	}
	
	/**
	 * Implements the Supplier get() function.
	 * The ChordProgression includes a trailing terminating HarmonyChord,
	 * typically HarmonyChord.TERMINAL_HARMONY_CHORD or HarmonyChord.SILENT, 
	 * at the end of each song Section.
	 * @param original if true returns Chords in original key, otherwise transposed key
	 * @param sectionTerminator the HarmonyChord to append to each section
	 * @return ChordProgression in transposed key
	 */
	protected ChordProgression get(boolean original, HarmonyChord sectionTerminator) {
		if(cpArray == null) {
			cpArray = new RaggedArray<HarmonyChord, ChordProgression>();
		}
		for(Section section : getSections()) {
			// the ChordProgressions need to be in transposed key
			List<SongMeasure> measures = section.getSongMeasures();
			ChordProgression chordProgression = new ChordProgression();
			chordProgression.addAll(measures, original);
			cpArray.add(chordProgression);
		}
		return sectionTerminator != null ? cpArray.flatten(sectionTerminator) : cpArray.flatten();
	}
	
}
