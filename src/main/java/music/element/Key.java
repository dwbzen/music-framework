package music.element;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.dwbzen.common.util.IJson;

public class Key implements IJson {

	private static final long serialVersionUID = 8877503587449790917L;

	@JsonProperty("name")	private String name;
	@JsonProperty("mode")	private Mode mode = Mode.MAJOR;		// or Mode.MINOR
	@JsonProperty			private Pitch[] signature = null;	// a possibly empty list of octave-neutral accidentals
	@JsonProperty			private Pitch designation = null;	// defines the root - could be null
	@JsonIgnore				private int fifths = Integer.MIN_VALUE;
	@JsonIgnore				private Scale scale = null;			// the scale associated with the key
	
	public Key() { 	}
	
	/**
	 * Construct a Key given its canonical name (as in "F#-Major" etc.)
	 * @param name
	 * @throws IllegalArgumentException if no such key
	 */
	public Key(String name) {
		if(keyNameMap.containsKey(name)) {
			Key mk = keyNameMap.get(name);
			this.name = name;
			this.mode = mk.getMode();
			this.signature = mk.getSignature();
			this.designation = mk.getDesignation();
		}
		else {
			throw new IllegalArgumentException("No such key: " + name);
		}
	}
	
	public Key(String name, Pitch[] signature, Pitch designation, Mode mode ) {
		this.signature = signature;
		this.name = name;
		this.designation = designation;
		this.mode = mode;
	}
	
	public enum Mode {
		MAJOR(1), MINOR(2);
		Mode(int val) { this.value = val;}
		private final int value;
	    public int value() { return value; }
	};
	
	public static final String MAJOR_MODE = "major";
	public static final String MINOR_MODE = "minor";
	public final static Pitch[] C_Signature = { Pitch.C };
	public final static Pitch[] F_Signature = 	  { Pitch.BFlat };
	public final static Pitch[] BFlat_Signature = { Pitch.BFlat, Pitch.EFlat };
	public final static Pitch[] EFlat_Signature = { Pitch.BFlat, Pitch.EFlat, Pitch.AFlat };
	public final static Pitch[] AFlat_Signature = { Pitch.BFlat, Pitch.EFlat, Pitch.AFlat, Pitch.DFlat };
	public final static Pitch[] DFlat_Signature = { Pitch.BFlat, Pitch.EFlat, Pitch.AFlat, Pitch.DFlat, Pitch.GFlat };
	public final static Pitch[] GFlat_Signature = { Pitch.BFlat, Pitch.EFlat, Pitch.AFlat, Pitch.DFlat, Pitch.GFlat, Pitch.CFlat };
	public final static Pitch[] CFlat_Signature = { Pitch.BFlat, Pitch.EFlat, Pitch.AFlat, Pitch.DFlat, Pitch.GFlat, Pitch.CFlat, Pitch.FFlat };

	public final static Pitch[] G_Signature = { Pitch.FSharp };
	public final static Pitch[] D_Signature = { Pitch.FSharp, Pitch.CSharp };
	public final static Pitch[] A_Signature = { Pitch.FSharp, Pitch.CSharp, Pitch.GSharp };
	public final static Pitch[] E_Signature = { Pitch.FSharp, Pitch.CSharp, Pitch.GSharp, Pitch.DSharp };
	public final static Pitch[] B_Signature = { Pitch.FSharp, Pitch.CSharp, Pitch.GSharp, Pitch.DSharp, Pitch.ASharp };
	public final static Pitch[] FSharp_Signature = { Pitch.FSharp, Pitch.CSharp, Pitch.GSharp, Pitch.DSharp, Pitch.ASharp, Pitch.ESharp };
	public final static Pitch[] CSharp_Signature = { Pitch.FSharp, Pitch.CSharp, Pitch.GSharp, Pitch.DSharp, Pitch.ASharp, Pitch.ESharp, Pitch.BSharp };
	
	/**
	 * Initialize the key signatures
	 */
	public static Map<String, Pitch[]> keySignatureMap = new HashMap<String, Pitch[]>();
	public static Map<String, Key> keyNameMap = new HashMap<String, Key>();
	public static Map<String, Key> rootKeyMap = new TreeMap<String, Key>();
	
	public static final String C_MAJOR_NAME =		"C-Major";
	public static final String A_MINOR_NAME =		"A-Minor";
	public static final String F_MAJOR_NAME =		"F-Major";
	public static final String D_MINOR_NAME =		"D-Minor";
	public static final String BFlat_MAJOR_NAME =	"Bb-Major";
	public static final String G_MINOR_NAME = 		"G-Minor";
	public static final String EFlat_MAJOR_NAME = 	"Eb-Major";
	public static final String C_MINOR_NAME = 		"C-Minor";
	public static final String AFlat_MAJOR_NAME = 	"Ab-Major";
	public static final String F_MINOR_NAME = 		"F-Minor";
	public static final String DFlat_MAJOR_NAME = 	"Db-Major";
	public static final String BFlat_MINOR_NAME = 	"Bb-Minor";
	public static final String GFlat_MAJOR_NAME = 	"Gb-Major";
	public static final String EFlat_MINOR_NAME = 	"Eb-Minor";
	public static final String CFlat_MAJOR_NAME = 	"Cb-Minor";
	public static final String AFlat_MINOR_NAME = 	"Ab-Minor";
	
	public static final String G_MAJOR_NAME = 		"G-Major";
	public static final String E_MINOR_NAME = 		"E-Minor";
	public static final String D_MAJOR_NAME = 		"D-Major";
	public static final String B_MINOR_NAME = 		"B-Minor";
	public static final String A_MAJOR_NAME = 		"A-Major";
	public static final String FSharp_MINOR_NAME = 	"F#-Minor";
	public static final String E_MAJOR_NAME = 		"E-Major";
	public static final String CSharp_MINOR_NAME = 	"C#-Minor";
	public static final String B_MAJOR_NAME = 		"B-Major";
	public static final String GSharp_MINOR_NAME = 	"G#-Minor";
	public static final String FSharp_MAJOR_NAME =	"F#-Major";
	public static final String DSharp_MINOR_NAME =	"D#-Minor";
	public static final String CSharp_MAJOR_NAME = 	"C#-Major";
	public static final String ASharp_MINOR_NAME =	"A#-Minor";

	/**
	 * Map Key signatures by name
	 */
	static {
		keySignatureMap.put(C_MAJOR_NAME, C_Signature);
		keySignatureMap.put(D_MAJOR_NAME, D_Signature);
		keySignatureMap.put(E_MAJOR_NAME, E_Signature);
		keySignatureMap.put(F_MAJOR_NAME, F_Signature);
		keySignatureMap.put(G_MAJOR_NAME, G_Signature);
		keySignatureMap.put(A_MAJOR_NAME, A_Signature);
		keySignatureMap.put(B_MAJOR_NAME, B_Signature);
		
		keySignatureMap.put(CSharp_MAJOR_NAME, CSharp_Signature);
		keySignatureMap.put(FSharp_MAJOR_NAME, FSharp_Signature);
		
		keySignatureMap.put(BFlat_MAJOR_NAME, BFlat_Signature);
		keySignatureMap.put(DFlat_MAJOR_NAME, DFlat_Signature);
		keySignatureMap.put(EFlat_MAJOR_NAME, EFlat_Signature);
		keySignatureMap.put(GFlat_MAJOR_NAME, GFlat_Signature);
		keySignatureMap.put(AFlat_MAJOR_NAME, AFlat_Signature);
		
		keySignatureMap.put(A_MINOR_NAME, C_Signature);
		keySignatureMap.put(D_MINOR_NAME, F_Signature);
		keySignatureMap.put(G_MINOR_NAME, BFlat_Signature);
		keySignatureMap.put(C_MINOR_NAME, EFlat_Signature);	
		keySignatureMap.put(F_MINOR_NAME, AFlat_Signature);
		keySignatureMap.put(BFlat_MINOR_NAME, DFlat_Signature);
		keySignatureMap.put(EFlat_MINOR_NAME, GFlat_Signature);
		
		keySignatureMap.put(E_MINOR_NAME, G_Signature);
		keySignatureMap.put(B_MINOR_NAME, D_Signature);	
		keySignatureMap.put(FSharp_MINOR_NAME, A_Signature);
		keySignatureMap.put(CSharp_MINOR_NAME, E_Signature);	
		keySignatureMap.put(GSharp_MINOR_NAME, B_Signature);	
		keySignatureMap.put(DSharp_MINOR_NAME, FSharp_Signature);	
		keySignatureMap.put(ASharp_MINOR_NAME, CSharp_Signature);
	}
	/**
	 * Define all MAJOR and MINOR keys
	 */
	public static final Key C_MAJOR = new Key(C_MAJOR_NAME, C_Signature, Pitch.C, Mode.MAJOR);
	public static final Key A_MINOR = new Key(A_MINOR_NAME, C_Signature, Pitch.A, Mode.MINOR);
	
	public static final Key F_MAJOR = new Key(F_MAJOR_NAME, F_Signature, Pitch.F, Mode.MAJOR);
	public static final Key D_MINOR = new Key(D_MINOR_NAME, F_Signature, Pitch.D, Mode.MINOR);
	
	public static final Key BFlat_MAJOR = new Key(BFlat_MAJOR_NAME, BFlat_Signature, Pitch.BFlat, Mode.MAJOR);
	public static final Key G_MINOR = new Key(G_MINOR_NAME, BFlat_Signature, Pitch.G, Mode.MINOR);

	public static final Key EFlat_MAJOR = new Key(EFlat_MAJOR_NAME, EFlat_Signature, Pitch.EFlat, Mode.MAJOR);
	public static final Key C_MINOR = new Key(C_MINOR_NAME, EFlat_Signature, Pitch.C, Mode.MINOR);

	public static final Key AFlat_MAJOR = new Key(AFlat_MAJOR_NAME, AFlat_Signature, Pitch.AFlat, Mode.MAJOR);
	public static final Key F_MINOR = new Key(F_MINOR_NAME, AFlat_Signature, Pitch.F, Mode.MINOR);

	public static final Key DFlat_MAJOR = new Key(DFlat_MAJOR_NAME, DFlat_Signature, Pitch.DFlat, Mode.MAJOR);
	public static final Key BFlat_MINOR = new Key(BFlat_MINOR_NAME, DFlat_Signature, Pitch.BFlat, Mode.MINOR);
	
	public static final Key GFlat_MAJOR = new Key(GFlat_MAJOR_NAME, GFlat_Signature, Pitch.GFlat, Mode.MAJOR);
	public static final Key EFlat_MINOR = new Key(EFlat_MINOR_NAME, GFlat_Signature, Pitch.EFlat, Mode.MINOR);
	
	public static final Key CFlat_MAJOR = new Key(CFlat_MAJOR_NAME, CFlat_Signature, Pitch.CFlat, Mode.MAJOR);
	public static final Key AFlat_MINOR = new Key(AFlat_MINOR_NAME, CFlat_Signature, Pitch.AFlat, Mode.MINOR);
	
	public static final Key G_MAJOR = new Key(G_MAJOR_NAME, G_Signature, Pitch.G, Mode.MAJOR);
	public static final Key E_MINOR = new Key(E_MINOR_NAME, G_Signature, Pitch.E, Mode.MINOR);
	
	public static final Key D_MAJOR = new Key(D_MAJOR_NAME, D_Signature, Pitch.D, Mode.MAJOR);
	public static final Key B_MINOR = new Key(B_MINOR_NAME, D_Signature, Pitch.B, Mode.MINOR);
	
	public static final Key A_MAJOR = new Key(A_MAJOR_NAME, A_Signature, Pitch.A, Mode.MAJOR);
	public static final Key FSharp_MINOR = new Key(FSharp_MINOR_NAME, A_Signature, Pitch.FSharp, Mode.MINOR);
	
	public static final Key E_MAJOR = new Key(E_MAJOR_NAME, E_Signature, Pitch.E, Mode.MAJOR);
	public static final Key CSharp_MINOR = new Key(CSharp_MINOR_NAME, E_Signature, Pitch.CSharp, Mode.MINOR);

	public static final Key B_MAJOR = new Key(B_MAJOR_NAME, B_Signature, Pitch.B, Mode.MAJOR);
	public static final Key GSharp_MINOR = new Key(GSharp_MINOR_NAME, B_Signature, Pitch.GSharp, Mode.MINOR);
	
	public static final Key FSharp_MAJOR = new Key(FSharp_MAJOR_NAME, FSharp_Signature, Pitch.FSharp, Mode.MAJOR);
	public static final Key DSharp_MINOR = new Key(DSharp_MINOR_NAME, FSharp_Signature, Pitch.DSharp, Mode.MINOR);
	
	public static final Key CSharp_MAJOR = new Key(CSharp_MAJOR_NAME, CSharp_Signature, Pitch.CSharp, Mode.MAJOR);
	public static final Key ASharp_MINOR = new Key(ASharp_MINOR_NAME, CSharp_Signature, Pitch.ASharp, Mode.MINOR);

	/**
	 * Map Keys by Root expressed as octave neutral - used to determine accidental preference (b or #) in chords
	 */
	static {
		rootKeyMap.put(Pitch.C.toString(-1), Key.C_MAJOR);
		rootKeyMap.put(Pitch.D.toString(-1), Key.D_MAJOR);
		rootKeyMap.put(Pitch.E.toString(-1), Key.E_MAJOR);
		rootKeyMap.put(Pitch.F.toString(-1), Key.F_MAJOR);
		rootKeyMap.put(Pitch.G.toString(-1), Key.G_MAJOR);
		rootKeyMap.put(Pitch.A.toString(-1), Key.A_MAJOR);
		rootKeyMap.put(Pitch.B.toString(-1), Key.B_MAJOR);
		
		rootKeyMap.put(Pitch.CSharp.toString(-1), Key.CSharp_MAJOR);
		rootKeyMap.put(Pitch.DSharp.toString(-1), Key.D_MAJOR);			// there is no D# key
		rootKeyMap.put(Pitch.ESharp.toString(-1), Key.E_MAJOR);			// there is no E# key
		rootKeyMap.put(Pitch.FSharp.toString(-1), Key.FSharp_MAJOR);
		rootKeyMap.put(Pitch.GSharp.toString(-1), Key.GSharp_MINOR);		// there is no G# major key
		rootKeyMap.put(Pitch.ASharp.toString(-1), Key.ASharp_MINOR);		// there is no A# Major key
		rootKeyMap.put(Pitch.BSharp.toString(-1), Key.B_MAJOR);			// there is no B# Major key

		rootKeyMap.put(Pitch.CFlat.toString(-1), Key.CFlat_MAJOR);
		rootKeyMap.put(Pitch.BFlat.toString(-1), Key.BFlat_MAJOR);
		rootKeyMap.put(Pitch.AFlat.toString(-1), Key.AFlat_MAJOR);
		rootKeyMap.put(Pitch.GFlat.toString(-1), Key.GFlat_MAJOR);
		rootKeyMap.put(Pitch.FFlat.toString(-1), Key.F_MAJOR);		// there is no Fb major key
		rootKeyMap.put(Pitch.EFlat.toString(-1), Key.EFlat_MAJOR);
		rootKeyMap.put(Pitch.DFlat.toString(-1), Key.DFlat_MAJOR);
	}
	
	/**
	 * All the major and minor keys mapped by canonical name
	 */
	static {
		keyNameMap.put(C_MAJOR_NAME, C_MAJOR);
		keyNameMap.put(C_MINOR_NAME, C_MINOR);
		keyNameMap.put(CSharp_MAJOR_NAME, CSharp_MAJOR);
		keyNameMap.put(CSharp_MINOR_NAME, CSharp_MINOR);
		keyNameMap.put(DFlat_MAJOR_NAME, DFlat_MAJOR);
		// there is no DFlat_MINOR key. It would have 8 flats
		keyNameMap.put(D_MAJOR_NAME, D_MAJOR);
		keyNameMap.put(D_MINOR_NAME, D_MINOR);
		// there is no DSharp_MAJOR key
		keyNameMap.put(DSharp_MINOR_NAME, DSharp_MINOR);
		keyNameMap.put(EFlat_MAJOR_NAME, EFlat_MAJOR);
		keyNameMap.put(EFlat_MINOR_NAME, EFlat_MINOR);
		keyNameMap.put(E_MAJOR_NAME, E_MAJOR);
		keyNameMap.put(E_MINOR_NAME, E_MINOR);
		keyNameMap.put(F_MAJOR_NAME, F_MAJOR);
		keyNameMap.put(F_MINOR_NAME, F_MINOR);
		keyNameMap.put(FSharp_MAJOR_NAME, FSharp_MAJOR);
		keyNameMap.put(FSharp_MINOR_NAME, FSharp_MINOR);
		keyNameMap.put(GFlat_MAJOR_NAME, GFlat_MAJOR);
		// there is no GFlat_MINOR key
		keyNameMap.put(G_MAJOR_NAME, G_MAJOR);
		keyNameMap.put(G_MINOR_NAME, G_MINOR);
		// there is no GSharp_MAJOR key
		keyNameMap.put(GSharp_MINOR_NAME, GSharp_MINOR);
		keyNameMap.put(AFlat_MAJOR_NAME, AFlat_MAJOR);
		keyNameMap.put(AFlat_MINOR_NAME, AFlat_MINOR);
		keyNameMap.put(A_MAJOR_NAME, A_MAJOR);
		keyNameMap.put(A_MINOR_NAME, A_MINOR);
		// there is no ASharp_MAJOR key
		keyNameMap.put(ASharp_MINOR_NAME, ASharp_MINOR);
		keyNameMap.put(BFlat_MAJOR_NAME, BFlat_MAJOR);
		keyNameMap.put(BFlat_MINOR_NAME, BFlat_MINOR);
		keyNameMap.put(B_MAJOR_NAME,B_MAJOR);
		keyNameMap.put(B_MINOR_NAME, B_MINOR);
	}
	/**
	 * Octave-neutral Map of all the defined Major and Minor keys
	 * The Integer value of the Step is the key because of enharmonic equivalence.
	 * For example, EFLAT and DSHARP are the same values, but different Step instances.
	 */
	public static Map<Integer, Key> majorKeys = new HashMap<Integer, Key>();
	public static Map<Integer, Key> minorKeys = new HashMap<Integer, Key>();

	static {
		majorKeys.put(Pitch.C.getStepValue(), C_MAJOR);
		minorKeys.put(Pitch.A.getStepValue(), A_MINOR);
		// Major flat keys
		majorKeys.put(Pitch.F.getStepValue(), F_MAJOR);
		majorKeys.put(Pitch.BFlat.getStepValue(), BFlat_MAJOR);
		majorKeys.put(Pitch.EFlat.getStepValue(), EFlat_MAJOR);
		majorKeys.put(Pitch.AFlat.getStepValue(), AFlat_MAJOR);
		majorKeys.put(Pitch.DFlat.getStepValue(), DFlat_MAJOR);
		majorKeys.put(Pitch.GFlat.getStepValue(), GFlat_MAJOR);
		majorKeys.put(Pitch.CFlat.getStepValue(), CFlat_MAJOR);
		// Minor flat keys
		minorKeys.put(Pitch.D.getStepValue(), D_MINOR);
		minorKeys.put(Pitch.G.getStepValue(), G_MINOR);
		minorKeys.put(Pitch.C.getStepValue(), C_MINOR);
		minorKeys.put(Pitch.F.getStepValue(), F_MINOR);
		minorKeys.put(Pitch.BFlat.getStepValue(), BFlat_MINOR);
		minorKeys.put(Pitch.EFlat.getStepValue(), EFlat_MINOR);
		minorKeys.put(Pitch.AFlat.getStepValue(), AFlat_MINOR);
		// Major sharp keys
		majorKeys.put(Pitch.G.getStepValue(), G_MAJOR);
		majorKeys.put(Pitch.D.getStepValue(), D_MAJOR);
		majorKeys.put(Pitch.A.getStepValue(), A_MAJOR);
		majorKeys.put(Pitch.E.getStepValue(), E_MAJOR);
		majorKeys.put(Pitch.B.getStepValue(), B_MAJOR);
		majorKeys.put(Pitch.FSharp.getStepValue(), FSharp_MAJOR);
		majorKeys.put(Pitch.CSharp.getStepValue(), CSharp_MAJOR);
		// Minor sharp keys
		minorKeys.put(Pitch.E.getStepValue(), E_MINOR);
		minorKeys.put(Pitch.B.getStepValue(), B_MINOR);
		minorKeys.put(Pitch.FSharp.getStepValue(), FSharp_MINOR);
		minorKeys.put(Pitch.CSharp.getStepValue(), CSharp_MINOR);
		minorKeys.put(Pitch.GSharp.getStepValue(), GSharp_MINOR);
		minorKeys.put(Pitch.DSharp.getStepValue(), DSharp_MINOR);
		minorKeys.put(Pitch.ASharp.getStepValue(), ASharp_MINOR);
	}
	
	/**
	 * The instrument's key tells which pitch will sound when the player plays a note written as C.
	 * For transposing instruments, maps the Key to the transposition steps
	 * Only defined for Keys that actually have instruments.
	 * When transposing from concert pitch to instrument pitch, add the #steps indicated.
	 * When transposing from instrument pitch to concert pitch, subtract the #steps.
	 * 
	 */
	public static Map<Key, Integer> transpositions = new HashMap<Key, Integer>();
	
	static {
		transpositions.put(Key.C_MAJOR, 0);
		transpositions.put(Key.D_MAJOR, 2);
		transpositions.put(Key.EFlat_MAJOR, 3);
		transpositions.put(Key.F_MAJOR, 5);
		transpositions.put(Key.G_MAJOR, -5);
		transpositions.put(Key.A_MAJOR, -3);
		transpositions.put(Key.BFlat_MAJOR, -2);
	}
	
	
	public String getName() {
		return name;
	}

	public Pitch[] getSignature() {
		return signature;
	}

	public Pitch getDesignation() {
		return designation;
	}

	public Mode getMode() {
		return mode;
	}
	public String getModeName() {
		return mode.equals(Mode.MAJOR) ? MAJOR_MODE : MINOR_MODE;
	}
	/**
	 * Synonym for designation - the designation IS the root
	 */
	public Pitch getRoot() {
		return designation;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setSignature(Pitch[] signature) {
		this.signature = signature;
	}
	
	/**
	 * Sets the signature from the name
	 */
	public void setSignature() {
		this.signature = keySignatureMap.containsKey(name) ? keySignatureMap.get(name) : C_Signature;
	}

	public void setDesignation(Pitch designation) {
		this.designation = designation;
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	/**
	 * Determine how many fifths there are in the key signature
	 * as in circle of fifths. 0 = Key of C, <0 = flat keys, >0 sharp keys
	 * 
	 * @return #fifths in the key signature
	 */
	public int getFifths() {
		if(fifths==Integer.MIN_VALUE) {
			fifths = 0;
			if(! (name.equalsIgnoreCase(C_MAJOR.name) || name.equalsIgnoreCase(A_MINOR.name)) ) {
				fifths = signature.length * (isFlatKey() ? -1 :1);
			}
		}
		return fifths;
	}
	
	public int getAlterationPreference() {
		int alt = 0;
		if(signature != null && signature.length > 0 ) {
			alt = signature[0].getAlteration();
		}
		return alt;
	}
	
	public int getAlterationPreference(int defaultIfNone) {
		int alt = defaultIfNone;
		if(signature != null && signature.length > 0 ) {
			alt = signature[0].getAlteration();
		}
		return alt;
	}

	/**
	 * If incomplete, can add the designation and signature array
	 * from the name.
	 */
	public void setDesignationAndSignature() {
		if(name != null && keyNameMap.containsKey(name)) {
			Key temp = keyNameMap.get(name);
			setDesignation(temp.getDesignation());
			setSignature();
		}
	}
	
	public String toString() {
		return getName();
	}
	
	public String toJSON() {
		return toJson();
	}
	
	public static Key getKey(Pitch root, Mode mode) {
		Integer sval = root.getStepValue();
		Key key = (mode.equals(Mode.MAJOR)) ? majorKeys.get(sval) : minorKeys.get(sval);
		return key;
	}
	
	/**
	 * Sets the Scale appropriate for this Key. Depends on the fact that for Major keys
	 * and also for natural minor, the Key name is the same as the Scale name.
	 * For Minor keys, the harmonic scale is used. This depends on the naming
	 * convention that the String "HARMONIC_" is included in the scale name
	 * for example, "A_HARMONIC_MINOR"
	 * NOTE that a Scale is not automatically associated when the Key is created.
	 * It must be added later if needed, for example in Song analysis.
	 * @throws IllegalAccessError if Scale not found
	 */
	public void setAssociatedScale() {
		if(mode.equals(Mode.MINOR)) {
			// get the harmonic minor version
			this.scale = Scale.getScale(name.substring(0, name.indexOf('-')) + "-HarmonicMinor");
		}
		if(mode.equals(Mode.MAJOR)) {
			this.scale = Scale.getScale(name);
		}
		if(this.scale == null) {
			throw new IllegalAccessError("Corresponding Scale for Key " + getName() + " was not found");
		}
	}

	public boolean isFlatKey() {
		return signature != null ? signature[0].getAlteration() <= 0 : true;	// C is considered a flat key
	}
	
	public boolean isSharpKey() {
		return signature != null ? signature[0].getAlteration() > 0 : false;
	}

	public Scale getScale() {
		return scale;
	}

	public void setScale(Scale scale) {
		this.scale = scale;
	}

	public ScaleFormula getScaleFormula() {
		return (scale != null) ? scale.getScaleFormula() : null;
	}
	
}
