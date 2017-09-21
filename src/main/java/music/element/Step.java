package music.element;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;

/**
 * Defines the step# in the chromatic scale, origin 1
 * Starting with C.
 * C(1), D(3), E(5), F(6), G(8), A(10), B(12)
 * There is also a SILENT Step (logically where no pitch is sounded).
 * 
 * @author don_bacon
 *
 */
@Embedded
@Entity(value="Step", noClassnameStored=true)
public enum Step {
	C(1),
	CSHARP(2), DFLAT(2),	/* enharmonic equivalent */
	D(3),
	DSHARP(4), EFLAT(4),	/* enharmonic equivalent */
	E(5),
	F(6),
	FSHARP(7), GFLAT(7),	/* enharmonic equivalent */
	G(8),
	GSHARP(9), AFLAT(9),	/* enharmonic equivalent */
	A(10),
	ASHARP(11), BFLAT(11),	/* enharmonic equivalent */
	B(12),
	SILENT(0);
	Step(int val) { this.value = val;}
	private final int value;
    public int value() { return value; }
    public int getOrdinal() { return ordinals[ordinal()]; }
    public Step add(int n) {
    	int ns = value + n;
    	if(ns >12) { ns -=12;}
    	return steps[ns-1];
    }
    public Integer getValue() {
    	return Integer.valueOf(value);
    }
    private static final int[] ordinals = {0,0,0, 1,1,1, 2, 3,3,3 ,4,4,4, 5,5,5, 6,7};
    private static final Step[] steps = {C, CSHARP, D, DSHARP, E, F, FSHARP, G, GSHARP, A, ASHARP, B};
}

