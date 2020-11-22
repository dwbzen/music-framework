package org.dwbzen.music.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import org.dwbzen.common.data.RandomGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A Set of Pitches maintained as a unit.<br>
 * Member pitches can be octave-neutral, meaning there is no octave assigned.<br>
 * For example "C" versus "C4". All the Pitches in the collection are either octave-neutral or octave-assigned.<br>
 * Certain operations make sense only for octave-assigned pitches, but they are not restricted:
 * <dl>
 * <dt>retrograde</dt><dd>collection in reverse order</dd>
 * <dt>inversion</dt><dd>inverting the step direction (up, down)</dd>
 * <dt>retrograde-inversion</dt><dd>an inversion of the retrograde</dd>
 * <dt>transposition</dt><dd>adjust the pitch by interval up or down</dd>
 *</dl>
 * Transposition can be done in-place or on a clone of the PitchSet. Inversion and Retrograde always operate on a clone.</p>
 * 
 * PitchSet contains a List<Pitch> in the order added. It is backed with a Set<Pitch> which has only the unique pitches in the list.<br>
 * Retrograde and Inversion operations are applied to the List<Picth> leaving the Set<Pitch> unchanged.</p>
 * 
 * A PitchSet is also the structure underlying Chord.<br>
 * PitchSet and Pitch both extend the abstract PitchElement. PitchElements are aggregated by PitchCollection.
 * 
 * @author don_bacon
 *
 */
public class PitchSet extends PitchElement implements Comparable<PitchSet> {

	@JsonProperty	private List<Pitch> pitches = new ArrayList<>();
	@JsonIgnore		private Set<Pitch> pitchSet = new TreeSet<>();
	@JsonProperty	private boolean octaveNeutral = true;
	
	static ThreadLocalRandom random = ThreadLocalRandom.current();
	
	private static PitchSet allPitchesSharps = new PitchSet();	// all pitches in the range C0 to C9, generated dynamically using sharps
	private static PitchSet allPitchesFlats = new PitchSet();		// all pitches in the range C0 to C9, generated dynamically using flats
	public  static Map<Alteration, PitchSet> allPitches = new HashMap<>();
	
	public static final String[] octavePitchArraySharps =  { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	public static final String[] octavePitchArrayFlats =   { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };
	
	static {
		generateAllPitches();
	}
	
	public PitchSet() {
	}
	
	/**
	 * Create a new PitchCollection from an existing List<Pitch>.<br>
	 * Pitches are deep-copied. Also sets octaveNeutral for the new collection.
	 * 
	 * @param pitchlist
	 */
	public PitchSet(List<Pitch> pitchlist) {
		for(Pitch p : pitchlist) {
			addPitch(new Pitch(p));
			octaveNeutral &= p.getOctave() >= 0;
		}
	}
	
	public PitchSet(PitchSet other) {
		this(other.pitches);
	}
	
	public PitchSet(String[] pitchArray) {
		for(String p : pitchArray) {
			addPitch(new Pitch(p));
		}
	}
	
	/**
	 * Makes a clone of this
	 */
	@Override
	public PitchSet clone() {
		return new PitchSet(this);
	}

	public List<Pitch> getPitches() {
		return pitches;
	}
	
	public Set<Pitch> getPitchSet() {
		return pitchSet;
	}
	
	public boolean remove(Pitch p) {
		return pitches.remove(p) && pitchSet.remove(p);
	}
	
	public boolean contains(Pitch p) {
		return pitches.contains(p);
	}

	/**
	 * Adds a given Pitch to the PitchSet.
	 * @param Pitch to add
	 */
	public void addPitch(Pitch p) {
		pitchSet.add(p);
		pitches.add(p);
		octaveNeutral = octaveNeutral && p.isOctaveNeutral();
	}
	
	/**
	 * Adds a given Pitch to the PitchSet if not already added.
	 * @param Pitch to add
	 */
	public void addUniquePitch(Pitch p) {
		if(!pitchSet.contains(p)) {
			pitches.add(p);
			octaveNeutral = octaveNeutral && p.isOctaveNeutral();
		}
		return;
	}
	
	/**
	 * Get the pitch at the specified index.
	 * @param index
	 * @return Pitch
	 * @throws IndexOutOfBoundsException if the index is out of bounds
	 */
	public Pitch getPitch(int index) {
		return pitches.get(index);
	}
	
	/**
	 * Creates a PitchCollection consisting of random pitches within a given range.<br>
	 * Constraints: lowPitch < highPitch, lowPitch >= "C0", highPitch <= "C9"
	 * This can be useful for testing.
	 * @param lowPitch the bottom of the range, inclusive
	 * @param highPitch the top of the range, inclusive
	 * @param n the number of pitches to create
	 * 
	 * @return a new PitchCollection of random pitches
	 */
	public static PitchSet getRandomPitches(Pitch lowPitch, Pitch highPitch, int n) {
		PitchSet pc = new PitchSet();
		int lowRangeStep = lowPitch.getRangeStep();
		int highRangeStep = highPitch.getRangeStep() + 1;
		
		for(int i = 1; i<=n; i++) {
			int rangeStep = random.nextInt(lowRangeStep, highRangeStep);
			Pitch p = new Pitch(getAllPitches().getPitch(rangeStep));
			pc.addPitch(p);
		}
		return pc;
	}
	
	public static PitchSet generateToneRow(Pitch origin) {
		PitchSet pc = new PitchSet();
		RandomGenerator rand = new RandomGenerator();
		List<Integer> steps = rand.randomIntegers(0, 12, 12, true);
		for(Integer step : steps) {
			Pitch p = origin.increment(step, -1);
			pc.addPitch(p);
		}
		return pc;
	}

	
	public static PitchSet getAllPitches() {
		return getAllPitches(Alteration.SHARP);
	}
	
	public static PitchSet getAllPitches(Alteration pref) {
		if(allPitches.isEmpty()) {
			generateAllPitches();
		}
		PitchSet pc = null;
		if(pref==Alteration.SHARP || pref==Alteration.DOUBLE_SHARP) {
			pc = allPitches.get(Alteration.SHARP);
		}
		else if(pref==Alteration.FLAT || pref==Alteration.DOUBLE_FLAT) {
			pc = allPitches.get(Alteration.FLAT);
		}
		else {
			pc = getAllPitches();
		}
		return pc;
	}

	public boolean isOctaveNeutral() {
		return octaveNeutral;
	}
	
	public int size() {
		return pitches.size();
	}
	
	public void transpose(int numberOfSteps) {
		for(Pitch p : pitches) {
			p.adjustPitch(numberOfSteps);
		}
	}
	
	public Pitch addNewPitch(Pitch p) {
		Pitch addedPitch = new Pitch(p);
		pitches.add(addedPitch);
		return addedPitch;
	}
	
	@Override
	public PitchSet getTransposition(int numberOfSteps) {
		PitchSet pc = clone();
		pc.transpose(numberOfSteps);
		return pc;
	}
	
	@Override
	public PitchSet getRetrograde() {
		PitchSet pc = new PitchSet();
		for(int i = size()-1; i>=0; i--) {
			pc.pitches.add(new Pitch(pitches.get(i)));
		}
		return pc;
	}
	
	@Override
	public PitchSet getInversion(Pitch startingPitch) {
		PitchSet inverted = new PitchSet();
		inverted.addNewPitch(startingPitch);
		int difference = 0;
		for(int index = 0; index<this.size()-1; index++) {
			difference = pitches.get(index + 1).getRangeStep() - pitches.get(index).getRangeStep();
			inverted.addPitch(inverted.pitches.get(index).increment(-difference, 0));
		}
		
		return inverted;
	}
	
	@Override
	public PitchSet getInversion() {
		return size()>0 ?  getInversion(pitches.get(0)) : new PitchSet();
	}
	
	/**
	 * @return A new PitchCollection that is the retrograde of the inversion of this.
	 */
	public PitchSet getRetrogradeInversion() {
		PitchSet pc = getInversion();
		return pc.getRetrograde();
	}


	@Override
	/**
	 * PitchSets are equal if they're the same size and contain the same Pitches in the same order
	 */
	public int compareTo(PitchSet other) {
		int sum = 0;
		if(size() != other.size()) {
			sum = size() - other.size();
		}
		else {
			for(int index = 0; index < size(); index ++) {
				sum = getPitch(index).difference(other.getPitch(index));
			}
		}
		return sum < 0 ? -1 : sum > 0 ? 1 : 0;
	}
	
	/**
	 * PitchSets are the same if they're the same size and contain the same Pitches regardless of order
	 */
	public boolean same(PitchSet other) {
		boolean same = false;
		for(Pitch p : pitchSet) {
			same &= other.pitchSet.contains(p);
		}
		return same;
	}

	@Override
	protected void setPitchElementType() {
		this.pitchElementType = PitchElementType.PITCH_SET;
	}
	
	private static void generateAllPitches() {
		generateAllPitches(Alteration.SHARP);
		generateAllPitches(Alteration.FLAT);
	}
	
	private static void generateAllPitches(Alteration pref) {
		String[] pitchArray = (pref == Alteration.SHARP) ? octavePitchArraySharps : octavePitchArrayFlats;
		PitchSet pc = (pref == Alteration.SHARP) ? allPitchesSharps : allPitchesFlats;
		for(int octave = 0; octave <=9; octave++) {
			for(String s : pitchArray) {
				pc.pitches.add(new Pitch(s+octave));
			}
		}
		pc.addPitch(new Pitch("C9"));
		pc.octaveNeutral = false;
		allPitches.put(pref, pc);
	}
	
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("{ ");
		for(Pitch p : pitches) {
			stringBuilder.append(p.toString(true));
			stringBuilder.append(", ");
		}
		stringBuilder.deleteCharAt(stringBuilder.length()-2);		// drop the trailing comma
		stringBuilder.append(" }");
		
		return stringBuilder.toString();
	}
	
	@Override
	public String toJson() {
		StringBuilder stringBuilder = new StringBuilder("{ octaveNeutral:" + octaveNeutral + ", pitches:[\n");
		for(Pitch p : pitches) {
			stringBuilder.append(p.toJson());
			stringBuilder.append(",\n");
		}
		stringBuilder.deleteCharAt(stringBuilder.length()-2);		// drop the trailing comma
		stringBuilder.append("]\n }");
		
		return stringBuilder.toString();

	}

	/**
	 * Usage: PitchSet low high numberOfPitches operation
	 * @param args
	 */
	public static void main(String... args) {
		
		Pitch low = Pitch.C0;
		Pitch high = Pitch.C9;
		Pitch root = new Pitch("C4");
		int n = 10;
		String operation = "";
		String outputFormat = "none";		// string, json or both
		boolean toneRow = false;
    	if(args.length > 0) {
    		for(int i = 0; i<args.length; i++) {
    			if(args[i].equalsIgnoreCase("-format")) {
     				outputFormat = args[++i].toLowerCase();
     			}
    			else if(args[i].equalsIgnoreCase("-low")) {
    				low = new Pitch(args[++i]);
    			}
    			else if(args[i].equalsIgnoreCase("-high")) {
    				high = new Pitch(args[++i]);
    			}
    			else if(args[i].startsWith("-op")) {
    				operation =  args[++i];
    			}
    			else if(args[i].equalsIgnoreCase("-n")) {
    				n = Integer.parseInt(args[++i]);
    			}
    			else if(args[i].equalsIgnoreCase("-row")) {
    				// generates a 12-note tone row instead of n-random pitches
    				n = 12;
    				toneRow = true;
    			}
    			else if(args[i].equalsIgnoreCase("-root")) {
    				root = new Pitch(args[++i]);
    			}
    		}
    	}
    	
		PitchSet pc = toneRow ? 
				PitchSet.generateToneRow(root) :
				PitchSet.getRandomPitches(low, high, n);
		displayPitchSet(pc, outputFormat);
		
		if(operation != null) {
			PitchSet pc2 = null;
			if(operation.equalsIgnoreCase("inv")) {
				pc2 = pc.getInversion();
				displayPitchSet(pc2, outputFormat, operation);
			}
			else if(operation.equalsIgnoreCase("retro")) {
				pc2 = pc.getRetrograde();
				displayPitchSet(pc2, outputFormat, operation);
			}
			else if(operation.equalsIgnoreCase("retroinv")) {
				pc2 = pc.getRetrogradeInversion();
				displayPitchSet(pc2, outputFormat, operation);
			}
		}
		
	}
	
	public static void displayPitchSet(PitchSet pc, String outputFormat, String op) {
		String opstr = (op == null || op.length()== 0) ? "" : op + " :" ;
		if(outputFormat.equals("string")) {
			System.out.println(opstr + pc.toString());
		}
		else if(outputFormat.equals("json")) {
			System.out.println(pc.toJson());
		}
		else if(outputFormat.equals("both")) {
			System.out.println(opstr + pc.toString());
			System.out.println(pc.toJson());
		}
	}
	
	public static void displayPitchSet(PitchSet pc, String outputFormat) {
		displayPitchSet(pc, outputFormat, "");
	}

	@Override
	/**
	 * Adjusts the Pitches in the PitchSet by the number of steps indicated
	 * @param numberOfSteps
	 */
	public void adjustPitch(int numberOfSteps) {
		for(Pitch p : pitches) {
			p.adjustPitch(numberOfSteps);
		}
	}

	@Override
	public void decrement(int numberOfSteps) {
		for(Pitch p : pitches) {
			p.decrement(numberOfSteps);
		}		
	}

	@Override
	public void increment(int numberOfSteps) {
		for(Pitch p : pitches) {
			p.increment(numberOfSteps);
		}	
	}

}
