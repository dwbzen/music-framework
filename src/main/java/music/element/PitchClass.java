package music.element;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

@Embedded
@Entity(value="PitchClass", noClassnameStored=true)
public enum PitchClass {
	UNPITCHED(0), 
	PITCHED(1), 
	DISCRETE_5LINE(5),
	DISCRETE_2LINE(2),
	DISCRETE_1LINE(0);
	
	PitchClass(int val) { this.value = val;}
	private final int value;
    public int value() { return value; }
}
