package music.element;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

@Embedded
@Entity(value="ExpressionType", noClassnameStored=true)
public class ExpressionType {
	
	private TextureType textureType = null;
	private RhythmicUnitType rhythmicUnitType = null;
	
	public ExpressionType(TextureType t, RhythmicUnitType r) {
		textureType = t;
		rhythmicUnitType = r;
	}

	public TextureType getTextureType() {
		return textureType;
	}

	public void setTextureType(TextureType textureType) {
		this.textureType = textureType;
	}

	public RhythmicUnitType getRhythmicUnitType() {
		return rhythmicUnitType;
	}

	public void setRhythmicUnitType(RhythmicUnitType rhythmicUnitType) {
		this.rhythmicUnitType = rhythmicUnitType;
	}
	
}
