package music.element.song;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;

import util.IJson;
import util.INameable;

/**
 * A Section is a logical division of a Song, such as "verse", "chorus" or "coda".
 * The name must be unique within a Song.
 * SongMeasure numbering should start at 1 always.
 * @author don_bacon
 *
 */
@Embedded
@Entity(value="section", noClassnameStored=true)
public class Section  implements IJson, INameable {

	private static final long serialVersionUID = -6094004826410293623L;
	private static Morphia morphia = new Morphia();
	
	@Property				private String name = "default";
	@Embedded("measures")	private List<SongMeasure> songMeasures = new ArrayList<SongMeasure>();
	/**
	 * optional #times this section repeated, default to 1. A repeat of 0 means "repeat until fade"
	 */
	@Property				private int repeat = 1;
	/**
	 * optional numbering - arbitrary integer, no ordering implied - just a number
	 */
	@Property				private int number = 1;
	@Transient				private int numberOfChords = 0;		// a sum of #Harmony in all the measures in this section.
	@Transient				private Song song = null;			// parent Song of this Section (if there is one)
	/**
	 * For each repeat, the name of the section to go to after this section complete.
	 * Optional, but there should be a nextSection specified for each repeat
	 */
	@Property("next")		private List<String> nextSections = new ArrayList<String>();

	/**
	 * Default constructor.
	 */
	public Section() {
		
	}
	public Section(String name) {
		this.name = name;
	}
	
	@Override
	public String toJSON() {
		return morphia.toDBObject(this).toString();
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}
	
	/**
	 * Do NOT add a SongMeasure using this method.
	 * @return
	 */
	public List<SongMeasure> getSongMeasures() {
		return songMeasures;
	}
	
	/**
	 * Adds a new SongMeasure and updates the numberOfChords.
	 * Sets the Section of the SongMeasure to this
	 * @param sm SongMeasure
	 * @return true if added
	 */
	public boolean addSongMeasure(SongMeasure sm) {
		numberOfChords += sm.getHarmony().size();
		sm.setSection(this);
		return getSongMeasures().add(sm);
	}

	public int getRepeat() {
		return repeat;
	}
	public void setRepeat(int repeat) {
		this.repeat = repeat;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getNumberOfChords() {
		return numberOfChords;
	}
	public int size() {
		return songMeasures.size();
	}
	public Song getSong() {
		return song;
	}
	public void setSong(Song song) {
		this.song = song;
	}
	public List<String> getNextSections() {
		return nextSections;
	}

}
