package org.dwbzen.music.element;

import java.util.AbstractMap;
import java.util.Map;

/**
 * Defines the step# in the chromatic scale, origin 1
 * Starting with C and including enharmonic equivalents.<br>
 * C(1), D(3), E(5), F(6), G(8), A(10), B(12)<br>
 * There is also a SILENT Step (logically where no pitch is sounded).<br>
 * 
 * @author don_bacon
 *
 */
public enum Step {
	SILENT(0),
	C(1), BSHARP(1),
	CSHARP(2), DFLAT(2),	/* enharmonic equivalents */
	D(3),
	DSHARP(4), EFLAT(4),
	E(5), FFLAT(5),
	F(6), ESHARP(6),
	FSHARP(7), GFLAT(7),
	G(8),
	GSHARP(9), AFLAT(9),
	A(10),
	ASHARP(11), BFLAT(11),
	B(12), CFLAT(12);
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
    private static final int[] ordinals = {0,0,0,0, 1,1,1, 2,2, 3,3,3,3, 4,4,4, 5,5,5, 6,6, 7};
    public static final Step[] steps = {C, CSHARP, D, DSHARP, E, F, FSHARP, G, GSHARP, A, ASHARP, B};
    /**
     * Map Step to proxymusic Step for musicXML creation. Accidentals applied in musicXML are not attached to the Step.
     */
    public final static Map<Step, org.audiveris.proxymusic.Step> StepMap =  Map.ofEntries(
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(A, org.audiveris.proxymusic.Step.A),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(B, org.audiveris.proxymusic.Step.B),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(C, org.audiveris.proxymusic.Step.C),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(D, org.audiveris.proxymusic.Step.D),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(E, org.audiveris.proxymusic.Step.E),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(F, org.audiveris.proxymusic.Step.F),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(G, org.audiveris.proxymusic.Step.G),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(ASHARP, org.audiveris.proxymusic.Step.A),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(AFLAT, org.audiveris.proxymusic.Step.A),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(BFLAT, org.audiveris.proxymusic.Step.B),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(CFLAT, org.audiveris.proxymusic.Step.B),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(BSHARP, org.audiveris.proxymusic.Step.C),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(CSHARP, org.audiveris.proxymusic.Step.C),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(DFLAT, org.audiveris.proxymusic.Step.D),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(DSHARP, org.audiveris.proxymusic.Step.D),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(EFLAT, org.audiveris.proxymusic.Step.E),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(FFLAT, org.audiveris.proxymusic.Step.E),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(ESHARP, org.audiveris.proxymusic.Step.F),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(FSHARP, org.audiveris.proxymusic.Step.F),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(GFLAT, org.audiveris.proxymusic.Step.G),
    		  new AbstractMap.SimpleEntry<Step, org.audiveris.proxymusic.Step>(GSHARP, org.audiveris.proxymusic.Step.G)
    );

}

