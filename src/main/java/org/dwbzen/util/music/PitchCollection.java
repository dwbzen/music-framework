package org.dwbzen.util.music;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.dwbzen.common.data.RandomGenerator;
import org.dwbzen.common.util.IJson;
import org.dwbzen.music.element.Alteration;
import org.dwbzen.music.element.Pitch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A collection of Pitches maintained as a unit.<br>
 * Member pitches can be octave-neutral, meaning there is no octave assigned.<br>
 * For example "C" versus "C4". All the Pitches in the collection are either octave-neutral or octave-assigned.<br>
 * Certain operations make sense only for octave-assigned pitches, but they are not restricted:<br>
 * retrograde - collection in reverse order<br>
 * inversion - inverting the step direction (up, down)<br>
 * retrograde-inversion - an inversion of the retrograde<br>
 * transposition - by interval</p>
 * Transposition can be done in-place or on a clone of the PitchCollection.
 * Inversion and Retrograde always operate on a clone.
 * 
 * @author don_bacon
 *
 */
public class PitchCollection implements IJson, Cloneable, Comparable<PitchCollection> {
	
	@JsonProperty	private List<Pitch> pitches = new ArrayList<>();
	@JsonProperty	private boolean octaveNeutral = true;
	
	static ThreadLocalRandom random = ThreadLocalRandom.current();
	
	static PitchCollection allPitchesSharps = new PitchCollection();	// all pitches in the range C0 to C9, generated dynamically using sharps
	static PitchCollection allPitchesFlats = new PitchCollection();		// all pitches in the range C0 to C9, generated dynamically using flats
	public static Map<Alteration, PitchCollection> allPitches = new HashMap<>();
	public static final String[] octavePitchArraySharps =  { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };
	public static final String[] octavePitchArrayFlats =   { "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B" };
	
	static {
		generateAllPitches();
	}
	
	public PitchCollection() {
	}
	
	/**
	 * Create a new PitchCollection from an existing List<Pitch>.<br>
	 * Pitches are deep-copied. Also sets octaveNeutral for the new collection.
	 * 
	 * @param pitchlist
	 */
	public PitchCollection(List<Pitch> pitchlist) {
		for(Pitch p : pitchlist) {
			pitches.add(new Pitch(p));
			octaveNeutral &= p.getOctave() >= 0;
		}
	}
	
	public PitchCollection(PitchCollection other) {
		this(other.pitches);
	}
	
	public PitchCollection(String[] pitchArray) {
		for(String p : pitchArray) {
			pitches.add(new Pitch(p));
		}
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
	
	public PitchCollection getTransposition(int numberOfSteps) {
		PitchCollection pc = clone();
		pc.transpose(numberOfSteps);
		return pc;
	}
	
	public PitchCollection getRetrograde() {
		PitchCollection pc = new PitchCollection();
		for(int i = size()-1; i>=0; i--) {
			pc.pitches.add(new Pitch(pitches.get(i)));
		}
		return pc;
	}
	
	public PitchCollection getInversion(Pitch startingPitch) {
		PitchCollection inverted = new PitchCollection();
		inverted.addNewPitch(startingPitch);
		int difference = 0;
		for(int index = 0; index<this.size()-1; index++) {
			difference = pitches.get(index + 1).getRangeStep() - pitches.get(index).getRangeStep();
			inverted.addPitch(inverted.pitches.get(index).increment(-difference, 0));
		}
		
		return inverted;
	}
	
	public PitchCollection getInversion() {
		return size()>0 ?  getInversion(pitches.get(0)) : new PitchCollection();
	}
	
	/**
	 * @return A new PitchCollection that is the retrograde of the inversion of this.
	 */
	public PitchCollection getRetrogradeInversion() {
		PitchCollection pc = getInversion();
		return pc.getRetrograde();
	}
	
	/**
	 * Makes a clone of this
	 */
	@Override
	public PitchCollection clone() {
		return new PitchCollection(this.pitches);
	}

	public List<Pitch> getPitches() {
		return pitches;
	}

	public void addPitch(Pitch p) {
		pitches.add(p);
	}
	
	public boolean isOctaveNeutral() {
		return octaveNeutral;
	}
	
	public int size() {
		return pitches.size();
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
	public static PitchCollection getRandomPitches(Pitch lowPitch, Pitch highPitch, int n) {
		PitchCollection pc = new PitchCollection();
		int lowRangeStep = lowPitch.getRangeStep();
		int highRangeStep = highPitch.getRangeStep() + 1;
		
		for(int i = 1; i<=n; i++) {
			int rangeStep = random.nextInt(lowRangeStep, highRangeStep);
			Pitch p = new Pitch(getAllPitches().getPitch(rangeStep));
			pc.addPitch(p);
		}
		return pc;
	}
	
	public static PitchCollection generateToneRow(Pitch origin) {
		PitchCollection pc = new PitchCollection();
		RandomGenerator rand = new RandomGenerator();
		List<Integer> steps = rand.randomIntegers(0, 12, 12, true);
		for(Integer step : steps) {
			Pitch p = origin.increment(step, -1);
			pc.addPitch(p);
		}
		return pc;
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
	
	public static PitchCollection getAllPitches() {
		return getAllPitches(Alteration.SHARP);
	}
	
	public static PitchCollection getAllPitches(Alteration pref) {
		if(allPitches.isEmpty()) {
			generateAllPitches();
		}
		PitchCollection pc = null;
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

	private static void generateAllPitches() {
		generateAllPitches(Alteration.SHARP);
		generateAllPitches(Alteration.FLAT);
	}
	
	private static void generateAllPitches(Alteration pref) {
		String[] pitchArray = (pref == Alteration.SHARP) ? octavePitchArraySharps : octavePitchArrayFlats;
		PitchCollection pc = (pref == Alteration.SHARP) ? allPitchesSharps : allPitchesFlats;
		for(int octave = 0; octave <=9; octave++) {
			for(String s : pitchArray) {
				pc.pitches.add(new Pitch(s+octave));
			}
		}
		pc.addPitch(new Pitch("C9"));
		pc.octaveNeutral = false;
		allPitches.put(pref, pc);
	}
	
	/**
	 * Usage: PitchCollection low high numberOfPitches operation
	 * @param args
	 */
	public static void main(String... args) {
		
		Pitch low = Pitch.C0;
		Pitch high = Pitch.C9;
		int n = 10;
		String operation = null;
		String outputFormat = "none";		// string, json or both
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
    		}
    	}
    	
		PitchCollection pc = PitchCollection.getRandomPitches(low, high, n);
		displayPitchCollection(pc, outputFormat);
		
		if(operation != null) {
			PitchCollection pc2 = null;
			if(operation.equalsIgnoreCase("inv")) {
				pc2 = pc.getInversion();
				displayPitchCollection(pc2, outputFormat);
			}
			else if(operation.equalsIgnoreCase("retro")) {
				pc2 = pc.getRetrograde();
				displayPitchCollection(pc2, outputFormat);
			}
			else if(operation.equalsIgnoreCase("retroinv")) {
				pc2 = pc.getRetrogradeInversion();
				displayPitchCollection(pc2, outputFormat);
			}
		}
		
		pc = PitchCollection.generateToneRow(new Pitch("G4"));
		System.out.println("Tone Row");
		displayPitchCollection(pc, "string");
	}
	
	public static void displayPitchCollection(PitchCollection pc, String outputFormat) {
		if(outputFormat.equals("string")) {
			System.out.println(pc.toString());
		}
		else if(outputFormat.equals("json")) {
			System.out.println(pc.toJson());
		}
		else if(outputFormat.equals("both")) {
			System.out.println(pc.toString());
			System.out.println(pc.toJson());
		}
	}

	@Override
	public int compareTo(PitchCollection other) {
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
}
