package music.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mathlib.Matrix;
import music.action.ScaleManager;
import music.element.Chord;
import music.element.Duration;
import music.element.IMeasurableContainer;
import music.element.Key;
import music.element.Measurable;
import music.element.Measurable.TieType;
import music.element.Measurable.TupletType;
import music.element.Measure;
import music.element.Note;
import music.element.Pitch;
import music.element.PitchClass;
import music.element.PitchRange;
import music.element.Scale;
import music.element.Scales;
import music.element.Score;
import music.element.ScorePartEntity;
import music.instrument.Instrument;

/**
 * A KeyTransformer alters all pitches by "rounding" up or down to the
 * nearest pitch in a given Scale. A Preference is set that determines
 * the direction which can also be random.
 * The Scale specified must have an associated Key, if not an IllegalArgumentException is thrown
 * That's because the Key is set for each Measure.
 * The Layer is transformed in situ; there is no history kept.
 * 
 * Note transformation is simplified by constructing a mapping table as part of the configuration.
 * This is a Pitch (source pitch) --> Pitch (scale pitch) mapping given
 * transform preference UP or DOWN. Included in the map is an octave adjustment.
 * With a transform UP preference, some notes will map to the next higher octave,
 * with a transform DOWN preference, some notes will map to the next lower octave.
 * This is indicated in the scale Pitch by setting the octave accordingly:
 * +1 to indicate adjust the target note up an octave,
 * -1 to adjust down an octave.
 * 
 * 
 * 
 * @author don_bacon
 *
 */
public class ScaleTransformer extends Transformer {
	protected static final Logger log = LogManager.getLogger(ScaleTransformer.class);
	static Map<Integer, List<Pitch>> percussionStaffPitches = new HashMap<Integer, List<Pitch>>();
	static Pitch E4 = new Pitch("E4");
	static Pitch G4 = new Pitch("G4");
	static Pitch B4 = new Pitch("B4");
	static Pitch D5 = new Pitch("D5");
	static Pitch F5 = new Pitch("F5");

	
	private Scale transformScale = null;
	private String scaleName = null;
	private Key transformKey = null;
		
	private Preference preference = Preference.Up;
	private boolean tied = false;	// global flag to prevent messing up tied notes
	private Pitch tiedPitch = null;	// global pitch set on START tie
	/*
	 * Key to use for a given instrument
	 * will be the same as transformKey for non-transposing
	 */
	private Key transposeTransformKey = null;


	private PitchRange pitchRange = null;	// set for each Instrument
	
	/**
	 * Pitch mapping for UP preference
	 */
	private Map<Pitch, Pitch> transformMapUP = new TreeMap<Pitch, Pitch>();
	/**
	 * Pitch mapping for DOWN preference
	 */
	private Map<Pitch, Pitch> transformMapDOWN = new TreeMap<Pitch, Pitch>();
	
	/**
	 * Octave adjustment if any for each Pitch: 0 (none), 1 (+1 octave), -1 (down 1 octave)
	 * 
	 */
	private Map<Pitch, Integer> octaveAdjustMap = new TreeMap<Pitch, Integer>();

	/*
	 * working arrays used to determine pitch transform.
	 * This is a 12 x n (where n=length of transform scale) for UP and DOWN respectively.
	 * Each entry is a Mod base 12 of the pitch difference (source note to scale note)
	 * 
	 */
	private Matrix<Integer> diffMod12 = null;
	private Matrix<Integer> diffRaw = null;
	private Matrix<Integer> minMaxMod12 = new Matrix<Integer>(12, 2);
	private List<Integer> octaveAdjust = new ArrayList<Integer>();
	
	public ScaleTransformer() {
		// use setters to set scale and preference
	}
	
	public ScaleTransformer(Scale scale,  Preference pref) throws IllegalArgumentException {
		setTransformScale(scale);
		this.preference = pref;
	}
	
	/*
	 * Initialize those pesky percussion staff fake pitches - 1, 2,3,4 and 5 line (#lines is the key)
	 */
	static {
		for(int nlines=1; nlines<=5; nlines++) {
			List<Pitch> pitchList = new ArrayList<Pitch>();
			pitchList.add(new Pitch("E4"));
			pitchList.add(new Pitch("G4"));
			if(nlines >= 3) {
				pitchList.add(new Pitch("B4"));
			}
			if(nlines >= 4) {
				pitchList.add(new Pitch("D5"));
			}
			if(nlines == 5) {
				pitchList.add(new Pitch("F5"));
			}
			percussionStaffPitches.put(nlines, pitchList);
		}
	}
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String... args) {

	}
	
	@Override
	public void transform(Layer layer) {
		Score score = layer.getScore();
		Map<String, ScorePartEntity> scoreParts = null;
		ScorePartEntity scorePartEntity = null;
		if(score != null) {	// if Score layer
			scoreParts = score.getParts();
		}
		else {				// if ScorePartEntity layer
			scoreParts = new HashMap<String, ScorePartEntity>();
			scorePartEntity = layer.getScorePartEntity();
			scoreParts.put(scorePartEntity.getPartName(), scorePartEntity);
		}
		for(String partname : scoreParts.keySet()) {
			scorePartEntity = scoreParts.get(partname);
			Instrument partInstrument = scorePartEntity.getInstrument();
			/*
			 * Check if transformer is instrument-specific
			 */
			if(getInstrument() != null && !instrument.equals(partInstrument)) {
				continue;	// to next part as this transformer doesn't apply
			}
			transformKey = scorePartEntity.getScoreKey();
			transposeTransformKey = transformKey;	// TODO - handle transposing instruments if needed
			
			List<Measure> measures = scorePartEntity.getMeasures();
			int len = measures.size();
			log.info("ScaleTransformer " + partname + " " + len + " measures. transform scale: " + transformScale);
			
			for(Measure measure : measures) {
				measure.setKey(transposeTransformKey);
				int totalDur = 0;							// sum of note durations for this measure
				int divisions = measure.getDivisions();		// units allowed - from RhythmScale
				int measureNum = measure.getNumber();
				List<Measurable> measurables = measure.getMeasureables();
				for(Measurable measurable : measurables) {
					Duration duration = measurable.getDuration();
					int durationUnits = duration.getDurationUnits();
					if(duration.isRatioSame()) {
						totalDur += durationUnits;
					}
					else if(measurable.getTupletType().equals(TupletType.START)) {
						totalDur += durationUnits * duration.getRatio().getTimeOf();
					}
					if(measurable instanceof Note) {
						Note note = (Note)measurable;
						/*
						 * Handle ties before transforming the note
						 */
						if(note.getTieType().equals(TieType.STOP)) {
							setTied(false);
							if(tiedPitch == null) {
								log.warn("invalid tiedPitch: " + " note: " + note);
								continue;
							}
							note.setPitch(new Pitch(tiedPitch));
							log.trace(" tied to\t" + note.toString());
							continue;
						}
						else if(note.getTieType().equals(TieType.BOTH)) {
							note.setPitch(new Pitch(tiedPitch));
							log.trace(" tied to\t" + note.toString());
							continue;			
						}
						transformNote(note);
						/*
						 * Again look at ties
						 */
						if(note.getTieType().equals(TieType.START)) {
							setTied(true);
							setTiedPitch(note.getPitch());
						}
					}
					else if(measurable instanceof Chord) {
						Chord chord = (Chord)measurable;
						log.debug("transform " + chord.toString());
						/*
						 * transform each note individually
						 */
						Iterator<Note> it = chord.getNotes().iterator();
						while(it.hasNext()) {
							Note note = it.next();
							/*
							 * Handle ties before transforming the note
							 * In order to keep tied Pitches consistent
							 */
							if(note.getTieType().equals(TieType.STOP)) {
								if(tiedPitch == null) {
									log.warn("invalid tiedPitch: " + " note: " + note);
									continue;
								}
								note.setPitch(new Pitch(tiedPitch));
								log.trace(" tied to\t" + note.toString());
								continue;
							}
							else if(note.getTieType().equals(TieType.BOTH)) {
								note.setPitch(new Pitch(tiedPitch));
								log.trace(" tied to\t" + note.toString());
								continue;							
							}
							transformNote(note);
							/*
							 * Again look at ties
							 */
							if(note.getTieType().equals(TieType.START)) {
								setTied(true);
								setTiedPitch(note.getPitch());
							}
						}
						log.debug("--- after: " + chord);
					}
				}
				if(divisions != totalDur) {
					log.warn("ScaleTransformer incorrect total divisions part " + partname + " measure: " +
							  measureNum + " divisions: " + divisions + " totalDur: " + totalDur);
				}
				log.debug("measure " + measure.getNumber() + " complete");
			}
		}
	}

	/**
	 * Transforms a single note.
	 * @param transformScale Scale to use for transform
	 * @param note Note to transform
	 * @param pref desired Preference (UP or DOWN) when there the note to transform is exactly between 2 scale notes
	 * @param rand Random to use if Preference.Random
	 */
	public void transformNote(Note note) {
		
		Preference pref = preference;
		if(preference==Preference.Random && random != null) {
			pref = (random.nextInt(2)==0) ? Preference.Up : Preference.Down;
		}
		log.trace("*** transform " + note);
		Pitch notePitch = note.getPitch();
		Pitch closestUp = transformMapUP.get(notePitch);
		Pitch closestDown = transformMapDOWN.get(notePitch);
		if(closestUp == null) {
			System.out.println(notePitch.toString());
		}
		int diffUp = Math.abs(closestUp.difference(notePitch));
		int diffDown = Math.abs(closestDown.difference(notePitch));

		Pitch newNotePitch = null;
		if(diffUp == diffDown) {	// use Preference to resolve ties
			if(closestUp.compareTo(pitchRange.getHigh()) > 0) {
				newNotePitch = closestDown;
			}
			else if(closestDown.compareTo(pitchRange.getLow()) < 0) {
				newNotePitch = closestUp;
			}
			else {
				newNotePitch = (pref == Preference.Up) ? closestUp : closestDown;
			}
		}
		else if(diffUp < diffDown) {
			newNotePitch = closestUp;
			// check instrument range
			if(instrument != null && newNotePitch.compareTo(pitchRange.getHigh()) > 0) {
				newNotePitch = closestDown;
			}
		}
		else {
			newNotePitch = closestDown;
			if(instrument != null && newNotePitch.compareTo(pitchRange.getLow()) < 0) {
				newNotePitch = closestUp;
			}
		}
		note.setPitchTo(newNotePitch);
		IMeasurableContainer<Note> chord = note.getContainer();		// could be null

		if(note.getPitch().getOctave() < 0) {
			log.warn("ScaleTransformer.transformNote: octave < 0 " + note.toString());
		}
		log.trace("    to\t" + note);
		/*
		 * If Note is in a Chord then check if duplicate Pitch
		 * and if it is a duplicate, remove the Note from the Chord.
		 * TODO finish this for chords
		 */
		if(chord != null && chord.size() > 1) {
			log.info("--- process: " + chord.toString());
			if(chord.countPitches(newNotePitch) > 1) {
				log.debug("    remove " + note);
				/*
				 *  NOTE - can't remove the note while iterating.
				 *  Solution: use chord.removeUnisonNotes() to obtain a List<Note> 
				 *  with unison intervals removed. This doesn't affect the Chord itself
				 */
			}
		}
		if(newNotePitch.compareTo(pitchRange.getLow()) < 0 || newNotePitch.compareTo(pitchRange.getHigh()) > 0) {
			log.warn("new note pitch " + newNotePitch + " out of range: " + pitchRange.getLow() + " " + pitchRange.getHigh() +
					" for notePitch: " + notePitch);
		}
		
	}
	
	public Scale getTransformScale() {
		return transformScale;
	}

	public void setTransformScale(Scale scale) {
		this.transformScale = scale;
	}
	
	public Preference getPreference() {
		return preference;
	}

	public void setPreference(Preference preference) {
		this.preference = preference;
	}
	
	public void setPreference(String pref) {
		if(pref.equalsIgnoreCase("random")) {
			setPreference(Preference.Random);
		}
		else if(pref.equalsIgnoreCase("up")) {
			setPreference(Preference.Up);
		}
		else if(pref.equalsIgnoreCase("down")) {
			setPreference(Preference.Down);
		}
	}

	public boolean isTied() {
		return tied;
	}

	public void setTied(boolean tied) {
		this.tied = tied;
	}

	public Pitch getTiedPitch() {
		return tiedPitch;
	}

	public void setTiedPitch(Pitch tiedPitch) {
		this.tiedPitch = new Pitch(tiedPitch);
	}
	
	public Map<Pitch, Pitch> getTransformMapUP() {
		return transformMapUP;
	}

	public Map<Pitch, Pitch> getTransformMapDOWN() {
		return transformMapDOWN;
	}

	public Map<Pitch, Integer> getOctaveAdjustMap() {
		return octaveAdjustMap;
	}

	@Override
	public void setInstrument(Instrument instrument) {
		super.setInstrument(instrument);
		pitchRange = instrument.getPitchRange();
	}

	public String getScaleName() {
		return scaleName;
	}

	public void setScaleName(String scaleName) {
		this.scaleName = scaleName;
	}

	@Override
	public void configure(Properties props, Instrument instrument) {
		setDataSourceName(props.getProperty("dataSource", "file"));
		setInstrument(instrument);
		String prefKey = null;
		String pref = null;
		Scale scale = null;
		Pitch rootPitch = null;
		String scaleKey = "score.transformers." + instrument.getName() + ".ScaleTransformer.scale";
		if(props.containsKey(scaleKey)) {
			scaleName = props.getProperty(scaleKey);
			prefKey =  "score.transformers." + instrument.getName() + ".ScaleTransformer.preference";
			pref = props.getProperty(prefKey, "random");
			rootPitch = new Pitch(props.getProperty("score.transformers." + instrument.getName()  + ".ScaleTransformer.root"));
		}
		else {
			scaleName = props.getProperty("score.transformers.ScaleTransformer.scale");
			pref = props.getProperty("score.transformers.ScaleTransformer.preference");
			rootPitch = new Pitch(props.getProperty("score.transformers.ScaleTransformer.scale.root"));
		}
		scale = getTransformScale(props, rootPitch);
		setTransformScale(scale);
		setPreference(pref);
		createTransformMaps();
	}
	
	@Override
	/**
	 * TODO - this method is never invoked. Why and could it be removed?
	 */
	public void configure(Properties props) {
		Scale scale = null;
		String pref = props.getProperty("score.transformers.ScaleTransformer.preference");
		scaleName = props.getProperty("score.transformers.ScaleTransformer.scale");
		Pitch rootPitch = new Pitch(props.getProperty("score.transformers.ScaleTransformer.scale.root"));
		scale = getTransformScale(props, rootPitch);
		/*
		 * All the scale formulas repeat the root and are 1 note longer
		 * than the actual scale. Truncate the last note as a new Scale instance.
		 */
		Scale tScale = scale.truncate();
		setTransformScale(tScale);
		setPreference(pref);
		createTransformMaps();
	}

	/**
	 * Look first in internal scale map for the named scale
	 * If it's not there, check the dataSource.
	 * If mongodb, query the configured scale formulas collection (usually scale_formulas)
	 * If file, get the scale formula from the configured dataSource.file.scaleFormulas JSON file
	 * 
	 * NOTE that mapped Scales (public final static Scale instances in Scale class)
	 * 	use the instance root, not the configured root. So for example, "EFlat-Minor-Pentatonic"
	 *  scale root is Eb regardless of the configured ScaleTransformer root.
	 *  All mapped scales have unique names to indicate the root so there is no
	 *  conflict with the common or theoretical scales.
	 *  
	 * @throws IllegalArgumentException if specified scale name not found
	 */
	protected Scale getTransformScale(Properties props, Pitch rootPitch) {
		ScaleManager scaleManager = new ScaleManager();
		Scale scale = scaleManager.getScale(scaleName, rootPitch);
		
		if(scale == null) {
			throw new IllegalArgumentException("No such scale found: " + scaleName);
		}
		return scale.truncate();
	}
	
	/**
	 * Creates transform maps - transformMapUP and transformMapDOWN  for pitched instruments. 
	 */
	protected void createTransformMaps() {
		if(transformScale.getMode() != null && transformScale.getMode().equals(Scales.DISCRETE)) {
			createTransformMapsForUnpitched();
			return;
		}
		/*
		 * Most scale formulas repeat the root and are 1 note longer than the actual scale.
		 * Unpitched scales for percussion are discrete and should NOT be truncated
		 */
		//transformScale.truncate();
		int scaleSize = transformScale.size();
		diffMod12 = new Matrix<Integer>(12, scaleSize);
		diffRaw = new Matrix<Integer>(12, scaleSize);
	
		List<Pitch> chromaticPitches =  Scales.CHROMATIC_12TONE_SCALE.getPitches();
		int irow = 0; int icol = 0;
		// Mod base 12 of the pitch difference (source note to scale note)
		for(Pitch pitch : chromaticPitches) {
			for(Pitch scalePitch : transformScale.getPitches()) {
				int rawdiff = pitch.difference(scalePitch);
				int diff = rawdiff % 12;
				if(diff < 0) { diff+=12;}
				diffMod12.setValue(irow, icol, diff);
				diffRaw.setValue(irow, icol, rawdiff);
				icol++;
			}
			irow++;
			icol=0;
		}
		/*
		 * Fill in minMaxMod12 array.
		 * [0, irow] = minimum of diffMod12[, irow]
		 * [1, irow] = maximum of diffMod12[, irow] or 0 if present
		 * Fill in octaveAdjust array.
		 */
		int indMax = -1;
		int indMin = -1;
		int indRawMax = -1;
		int indRawMin = -1;
		for(int rowNumber = 0; rowNumber <12; rowNumber++) {
			indMax = diffMod12.indexOf(0, rowNumber);
			if(indMax < 0 ) {
				indMax = diffMod12.getMaxValueInRow(rowNumber);
			}
			indMin = diffMod12.getMinValueInRow(rowNumber);
			minMaxMod12.setValue(rowNumber, 0, diffMod12.index(rowNumber, indMin));
			minMaxMod12.setValue(rowNumber, 1, diffMod12.index(rowNumber, indMax));
			indRawMax = diffRaw.getMaxValueInRow(rowNumber);
			indRawMin = diffRaw.getMinValueInRow(rowNumber);
			
			if(diffRaw.index(rowNumber, indRawMax).intValue() < 0) {
				octaveAdjust.add(1);
			}
			else if(diffRaw.index(rowNumber, indRawMin).intValue() > 0) {
				octaveAdjust.add(-1);
			}
			else {
				octaveAdjust.add(0);
			}
		}
		/*
		 * now convert minMaxMod12 + octaveAdjust to transform maps
		 * The temp maps are octave neutral
		 */
		Map<Pitch, Pitch> transformMapUPTemp = new TreeMap<Pitch, Pitch>();
		Map<Pitch, Pitch> transformMapDOWNTemp = new TreeMap<Pitch, Pitch>();
		Map<Pitch, Integer> octaveAdjustMapTemp = new TreeMap<Pitch, Integer>();

		List<Pitch> transformScalePitches = transformScale.getPitches();
		for(int rowNumber = 0; rowNumber <12; rowNumber++) {
			Pitch cpitch = chromaticPitches.get(rowNumber);
			indMin = diffMod12.indexOf(minMaxMod12.index(rowNumber, 0), rowNumber);
			indMax =  diffMod12.indexOf(minMaxMod12.index(rowNumber, 1), rowNumber);
			transformMapUPTemp.put(cpitch, transformScalePitches.get(indMin));
			transformMapDOWNTemp.put(cpitch, transformScalePitches.get(indMax));
		    octaveAdjustMapTemp.put(cpitch, octaveAdjust.get(rowNumber));
		}
		/*
		 * finally expand the two transform maps and the octave adjust map
		 * to cover the entire pitch range of C0 to C9
		 */
		Scale cscale = Scales.FULL_RANGE_CHROMATIC_SCALE;
		for(Pitch p : cscale.getPitches()) {
			Pitch pNeutral = new Pitch(p.getStep(), -1, p.getAlteration());
			int octaveAdj = octaveAdjustMapTemp.get(pNeutral);
			int octaveAdjUP = (octaveAdj > 0) ? octaveAdj : 0;
			int octaveAdjDOWN = (octaveAdj < 0) ? octaveAdj : 0;
			Pitch mpup = transformMapUPTemp.get(pNeutral);
			Pitch mpdown = transformMapDOWNTemp.get(pNeutral);
			Pitch mappedUp = new Pitch(mpup.getStep(),  p.getOctave() + octaveAdjUP, mpup.getAlteration());
			Pitch mappedDown =  new Pitch(mpdown.getStep(),  p.getOctave() + octaveAdjDOWN, mpdown.getAlteration());
			transformMapUP.put(p, mappedUp);
			transformMapDOWN.put(p, mappedDown);
		}
		return;
	}

	/**
	 * Creates transform maps - transformMapUP and transformMapDOWN for unpitched
	 * instruments where the transform scale's mode is DISCRETE.
	 * 
	 * The Pitches in Discrete scale types, used for unpitched percussion instruments
	 *  are not true Pitches but reference lines in the appropriate percussion scale.
	 *  For example a 5-line percussion staff uses the lines corresponding to
	 *  pitches E4, G4, B4, D5, F5. The standard mapping won't work in this case
	 *  and needs to be discrete as well. For a 5-line scale the mapping is:
	 *  E4=E4, F4=E4, Gb4=E4, G4=G4, Ab4=G4, A4=G4, Bb4=G4, B4=B4, C5=B4, Db5=B4, D5=D5, Eb5=D5, E5=E5, F5=E5,
	 *  which covers the instrument's configured range (in this case E4 to F5).
	 *  Same pattern for 2,3, and 4 line percussion staff.
	 *  It is not necessary to map every pitch in the Chromatic range C0 to C9.
	 *  For these types of scales the discrete mapping is done, well, discretely.
	 *  
	 *  Handles 5-line, 2-line, and 1-line percussion staffs
	 *  
	 */
	protected void createTransformMapsForUnpitched() {
		PitchRange pitchRange = instrument.getPitchRange();		// not real pitches - just lines on a percussion staff
		for(Pitch p : Scales.FULL_RANGE_CHROMATIC_SCALE.getPitches()) {
			int rangeStep = p.getRangeStep();
			Pitch mappedUp = null;
			Pitch mappedDown = null;
			if(p.compareTo(pitchRange.getLow()) < 0) {
				transformMapUP.put(p, pitchRange.getLow());
				transformMapDOWN.put(p, pitchRange.getLow());
			}
			else if(p.compareTo(pitchRange.getHigh()) > 0) {
				transformMapUP.put(p, pitchRange.getHigh());
				transformMapDOWN.put(p, pitchRange.getHigh());
			}
			else if(instrument.getPitchClass().equals(PitchClass.DISCRETE_1LINE)) {
				transformMapUP.put(p, E4);
				transformMapDOWN.put(p, E4);
			}
			else if(instrument.getPitchClass().equals(PitchClass.DISCRETE_2LINE)) {
				// B3 to D4 for PitchClass.DISCRETE_2LINE
				switch(rangeStep) {
				case 52:	// E4
					mappedUp = mappedDown = E4;
					break;
				case 53:	// F4
				case 54:	// F#4 or Gb4
					mappedUp = G4;
					mappedDown = E4;
					break;
				case 55:	// G4
				default:
					mappedUp = mappedDown = G4;
				}
				transformMapUP.put(p, mappedUp);
				transformMapDOWN.put(p, mappedDown);
			}
			else if(instrument.getPitchClass().equals(PitchClass.DISCRETE_5LINE)) {
				// here's the discrete part - depends on the E4(52) to F5(65) range for 5-line, 
				switch(rangeStep) {
					case 52: // E4
						mappedUp = mappedDown = E4;
						break;
					case 53: // F4
					case 54: // F#4  or Gb4
						mappedUp = G4;
						mappedDown = E4;
						break;
					case 55: // G4
						mappedUp = mappedDown = G4;
						break;
					case 56: // G#4 or Ab4
					case 57: // A4
					case 58: // Bb4
						mappedUp = B4;
						mappedDown = G4;
						break;
					case 59: // B4
						mappedUp = mappedDown = B4;
						break;
					case 60: // C5
					case 61: // C#5 or Db5
						mappedUp = D5;
						mappedDown = B4;
						break;
					case 62: // D5
						mappedUp = mappedDown = D5;
						break;
					case 63: // D#5 or Eb5
					case 64: // E5
						mappedUp = F5;
						mappedDown = D5;
						break;
					case 65: // F5
					default:
						mappedUp = mappedDown = F5;
				}
				transformMapUP.put(p, mappedUp);
				transformMapDOWN.put(p, mappedDown);
			}
		}
		return;
	}

}
