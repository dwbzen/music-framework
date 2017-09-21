package music.element.song;

import music.element.Measurable.TieType;
import music.element.Measurable.TupletType;

import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import util.IJson;

/**
 * Provide note type (half, quarter etc. ) and tuplet information.
 * Also includes tie information.
 * @author don_bacon
 *
 */
@Embedded
@Entity(value="Notation")
public class Notation implements IJson {

	private static final long serialVersionUID = -3719234783577601054L;
	private static Morphia morphia = new Morphia();
	
	/**
	 * derived from Duration and time signature: whole, half, quarter, eighth, 16th, 32nd, 64th
	 */
	@Property("type")		private String noteType = null;
	@Property("tieType")	private TieType tieType = null;			// NONE(0), START(1), STOP(2), BOTH(3);
	@Property("tupletType")	private TupletType tupletType = null;		// NONE(0), START(1), CONTINUE(2), STOP(3)
	@Property("dots")		private int dots = 0;
	@Property("tuplet")		private String tuplet = null;		// as in "3/2" etc.

	
	public Notation() {
	}
	
	public Notation(String noteType) {
		this.noteType = noteType;
	}
	
	@Override
	public String toJSON() {
		return morphia.toDBObject(this).toString();
	}

	public String getTuplet() {
		return tuplet;
	}

	public void setTuplet(String tuplet) {
		this.tuplet = tuplet;
	}

	public String getNoteType() {
		return noteType;
	}

	public void setNoteType(String noteType) {
		this.noteType = noteType;
	}

	public TieType getTieType() {
		return tieType;
	}

	public void setTieType(TieType tieType) {
		this.tieType = tieType;
	}

	public TupletType getTupletType() {
		return tupletType;
	}

	public void setTupletType(TupletType tupletType) {
		this.tupletType = tupletType;
	}

	public int getDots() {
		return dots;
	}

	public void setDots(int dots) {
		this.dots = dots;
	}

}
