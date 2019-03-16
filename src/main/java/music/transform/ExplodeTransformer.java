package music.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import org.dwbzen.common.math.IntegerPair;
import music.element.Chord;
import music.element.Duration;
import music.element.Measurable;
import music.element.Measurable.TupletType;
import music.element.Measure;
import music.element.Note;
import music.element.Score;
import music.element.ScorePartEntity;
import music.instrument.Instrument;
import music.transform.IExploder.ExploderType;
import util.Configuration;

/**
 * Applies the configured NoteExploder and/or ChordExploder to each Measurable.
 * Constraint: all added NoteExploders and ChordExploders must have the same size.
 * They will have different formulas, but they must resolve to the same
 * number of Measurables in the exploded note/chord.
 * Each exploder has an associated frequency (1 to 100) that determines how often it is applied.
 *
 * @author don_bacon
 *
 */
public class ExplodeTransformer  extends Transformer {
	protected static final org.apache.log4j.Logger log = Logger.getLogger(ExplodeTransformer.class);
	public static Map<ExploderType, Map<String, IExploder>> mappedExploders = new HashMap<ExploderType, Map<String, IExploder>>();
	public final static String formulaNames = "score.transformers.ExplodeTransformer.formulaNames.";
	
	private List<IExploder> noteExploders = new ArrayList<IExploder>();		// all the NoteExploders
	/*
	 * The ARPEGIO NoteExploders that can be selected and applied
	 */
	private List<NoteExploder> noteExplodersSelectionArpegio =  new ArrayList<NoteExploder>();
	/*
	 * The CHORD NoteExploders that can be selected and applied
	 */
	private List<NoteExploder> noteExplodersSelectionChord =  new ArrayList<NoteExploder>();
	/*
	 * Map of noteExplodersSelection Lists by ExploderType
	 */
	private Map<ExploderType, List<NoteExploder>> noteExplodersSelectionMap = new HashMap<ExploderType, List<NoteExploder>>();
	
	/*
	 * If configured for 'all' instruments, need a type
	 */
	private ExploderType exploderType = ExploderType.ARPEGIO;	// just a default
	
	/*
	 *  if true, breaks ties when forming chords
	 */
	private boolean breakChordTies = false;

	
	static {	// initialize the builtin exploder maps
		mappedExploders.put(ExploderType.ARPEGIO, new HashMap<String, IExploder>());
		mappedExploders.put(ExploderType.CHORD, new HashMap<String, IExploder>());		
	}
	
	/**
	 * Quick test
	 * @param args
	 */
	public static void main(String... args) {
		Configuration configuration =  Configuration.getInstance("/config.properties");
		Properties configProperties = configuration.getProperties();

		ExplodeTransformer et = new ExplodeTransformer();
		et.configure(configProperties);
		log.info("ExplodeTransformer configured");
		
	}
	
	/**
	 * This sets up the built-in (mapped) exploders.
	 * Adding to  Map<ExploderType, Map<String, IExploder>> (the String is the exploder name)
	 */
	public ExplodeTransformer() {
		addBuiltinExploders();
		noteExplodersSelectionMap.put(ExploderType.ARPEGIO, noteExplodersSelectionArpegio);
		noteExplodersSelectionMap.put(ExploderType.CHORD, noteExplodersSelectionChord);
	}
	
	private void addBuiltinExploders() {
		createAndAddNoteExploder(ExploderType.ARPEGIO, NoteExploder.EIGHT, 0, "EIGHT");
		createAndAddNoteExploder(ExploderType.ARPEGIO, NoteExploder.EIGHT_RANDOM, 0, "EIGHT_RANDOM");
		createAndAddNoteExploder(ExploderType.ARPEGIO, NoteExploder.EIGHT_RANDOM2, 0, "EIGHT_RANDOM2");
		createAndAddNoteExploder(ExploderType.ARPEGIO, NoteExploder.TRIPLET_RANDOM, 0, NoteExploder.THREE_TWO, "TRIPLET_RANDOM");
		createAndAddNoteExploder(ExploderType.ARPEGIO, NoteExploder.QUINTUPLET_RANDOM, 0, NoteExploder.FIVE_FOUR, "QUINTUPLET_RANDOM");
		createAndAddNoteExploder(ExploderType.CHORD, NoteExploder.TRIPLE_RANDOM_CHORD, 0, "TRIPLE_RANDOM_CHORD");
		createAndAddNoteExploder(ExploderType.CHORD, NoteExploder.QUAD_RANDOM_CHORD, 0, "QUAD_RANDOM_CHORD");
		createAndAddNoteExploder(ExploderType.CHORD, NoteExploder.OCTAVE_DOUBLE_CHORD, 0, "OCTAVE_DOUBLE_CHORD");
	}

	public ExploderType getExploderType() {
		return exploderType;
	}

	public void setExploderType(ExploderType exploderType) {
		this.exploderType = exploderType;
	}

	public List<IExploder> getNoteExploders() {
		return noteExploders;
	}

	public Map<ExploderType, List<NoteExploder>> getNoteExplodersSelectionMap() {
		return noteExplodersSelectionMap;
	}

	@Override
	public void transform(Layer layer) {
		Score score = layer.getScore();
		Map<String, ScorePartEntity> scoreParts = null;
		ScorePartEntity scorePartEntity = null;
		NoteExploder noteExploder = null;
		ChordExploder chordExploder = null;
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
			Instrument instrument = scorePartEntity.getInstrument();
			/*
			 * Check if transformer is instrument-specific
			 */
			if(getInstrument() != null && !getInstrument().equals(instrument)) {
				continue;	// to next part as this transformer doesn't apply
			}
			// PitchRange pitchRange = instrument.getPitchRange();
			List<Measure> measures = scorePartEntity.getMeasures();
			int len = measures.size();
			log.info("ExplodeTransformer " + partname + " " + len + " measures.");
			int size = 0;
			int numberOfMeasures = 0;
			for(Measure measure : measures) {
				int totalDur = 0;
				int divisions = measure.getDivisions();
				int measureNum = measure.getNumber();
				List<Measurable> measurables = measure.getMeasureables();
				int measureSize = measurables.size();
				for(int index=0; index<measureSize; index++) {
					Measurable measurable = measurables.get(index);
					Duration duration = measurable.getDuration();
					int units = duration.getDurationUnits();
					if(duration.isRatioSame()) {
						totalDur += duration.getDurationUnits();
					}
					else if (duration.isTuplet() && measurable.getTupletType().equals(TupletType.START)) {
						// if a tuplet the same duration is in each tuplet note
						// so only add the duration of the START * #normal notes
						totalDur += duration.getDurationUnits() * duration.getInTheTimeOfNotes();
					}
					noteExploder = pickNoteExploder();		// could be null
					size = (noteExploder == null) ? 0 : noteExploder.size();
					if(size > 0 && units >= size && units%size == 0 ) {
						if( measurable instanceof Note && noteExploder != null ) {
							noteExploder.setBreakChordTies(breakChordTies);	// yes I know it's redundant
							Note note = (Note)measurable;
							log.debug("explode note " + note.toString());
							/*
							 * Explodes the note into an ARPEGIO of n-notes
							 * or a CHORD in which case notes.size() == 1 and the Measurable is a Chord instance
							 */
							List<Measurable> notes = noteExploder.explode(note, measure);
							/*
							 * Possible did not explode this note - depending on the explode rules
							 * If we did, then insert the notes in place of the exploded one
							 * Otherwise there's nothing to do
							 */
							if(notes.size() > 0) {
								measureSize+=(notes.size()-1);
								index = measure.insert(notes, index);
							}
						}
						else if( measurable instanceof Chord && chordExploder != null) {
							Chord chord = (Chord)measurable;
							log.debug("explode chord " + chord.toString());
							// TODO - how DO we explode a chord?
							// TODO I know - apply the formula to each note in the chord
							List<Measurable> chords = chordExploder.explode(chord, measure);
							measureSize+=(chords.size()-1);
							index = measure.insert(chords, index);
						}
					}
				}	// done with this Measure
				if(divisions != totalDur) {
					log.warn("ExplodeTransformer divisions don't match for part " + partname + " measure: " +
							  measureNum + " divisions: " + divisions + " totalDur: " + totalDur);
				}
				numberOfMeasures++;
				log.info("measure " + measure.getNumber() + " complete. #measures: " + numberOfMeasures);
			}
		}
	}
	
	public void createAndAddNoteExploder(ExploderType exptype, List<IntegerPair> explodeFormula, int freq, String name) {
		createAndAddNoteExploder(exptype, explodeFormula, freq, IExploder.ONE_TO_ONE, name);
	}

	/**
	 * Creates a NoteExploder of a given type and adds it to the static 
	 * Map<ExploderType, Map<String, IExploder>> mappedExploders
	 * @param exptype
	 * @param explodeFormula
	 * @param freq
	 * @param ratio
	 * @param name
	 */
	public void createAndAddNoteExploder(ExploderType exptype, List<IntegerPair> explodeFormula, int freq, IntegerPair ratio, String name) {
		NoteExploder noteExploder  = (ratio != null) ?
				new NoteExploder(exptype, explodeFormula, ratio) : new NoteExploder(exptype, explodeFormula);
		mappedExploders.get(exptype).put(name, noteExploder);
		noteExploder.setName(name);
		if(freq > 0) {
			addNoteExploderToSelectionList(freq, noteExploder);
		}
	}

	/**
	 * Adds freq number of the given NoteExploder to the appropriate list:
	 * noteExplodersSelectionArpegio or noteExplodersSelectionChord depending on the ExploderType,
	 * and also to List of all NoteExploders (noteExploders).
	 * If freq == 0 it is NOT added.
	 * @param freq a relative frequency (0 <= freq <= 100)
	 * @param noteExploder
	 */
	protected void addNoteExploderToSelectionList(int freq, NoteExploder noteExploder) {
		List<NoteExploder> noteExplodersSelection = noteExplodersSelectionMap.get(noteExploder.getExploderType());
		if(freq > 0) {
			noteExploders.add(noteExploder);
			for(int i=0; i<freq; i++) {
				noteExplodersSelection.add(noteExploder);
			}
		}
	}

	/**
	 * Picks a NoteExploder from selection list based on set ExploderType
	 * @return NoteExploder or null
	 */
	private NoteExploder pickNoteExploder() {
		return pickNoteExploder(this.exploderType);
	}

	/**
	 * Picks a NoteExploder using configured frequencies
	 * @param exploderType 
	 * @return NoteExploder or null depending on selection and frequencies
	 * 
	 */
	private NoteExploder pickNoteExploder(ExploderType exploderType) {
		/*
		 * pick a number from 0 to 99
		 */
		List<NoteExploder> noteExplodersSelection = noteExplodersSelectionMap.get(exploderType);
		int pick = random.nextInt(0, 100);
		NoteExploder noteExploder = (pick >= noteExplodersSelection.size()) ? null : noteExplodersSelection.get(pick);
		return noteExploder;
	}

	public boolean isBreakChordTies() {
		return breakChordTies;
	}

	public void setBreakChordTies(boolean breakChordTies) {
		this.breakChordTies = breakChordTies;
	}

	@Override
	public void configure(Properties props) {
		/*
		 * Look first in internal explode formula map for the named formula
		 */
		configureNoteExploders(props, ExploderType.ARPEGIO);
		configureNoteExploders(props, ExploderType.CHORD);
		/*
		 * Are we configured for a particular instrument?
		 * If so, get the configured type & set it.
		 */
		if(getInstrument() != null) {
			String key = "ExplodeTransformer." + getInstrument().getName();
			if(props.containsKey(key)) {
				String typeString = props.getProperty(key);
				if(typeString.equalsIgnoreCase(ExploderType.ARPEGIO.toString())) {
					this.exploderType = ExploderType.ARPEGIO;
				}
				else if(typeString.equalsIgnoreCase(ExploderType.CHORD.toString())) {
					this.exploderType = ExploderType.CHORD;
					String skey = key + ".breakChordTies";
					if(props.containsKey(skey)) {
						breakChordTies = props.getProperty(skey, "false").equalsIgnoreCase("true");
					}
				}
			}
		}
	}

	private void configureNoteExploders(Properties props, ExploderType exploderType) {
		String exploderTypeText = exploderType.toString();
		String key =  formulaNames + exploderTypeText;
		if(props.containsKey(key)) {
			String[] formulas = props.getProperty(key).split(",");
			for(int i=0; i<formulas.length; i++) {
				String[] fstring = formulas[i].split(":");
				String fname = fstring[0];
				int freq = Integer.parseInt(fstring[1]);
				/*
				 *  if builtin NoteExploder then add to selection list
				 */
				if(mappedExploders.get(exploderType).containsKey(fname)) {
					NoteExploder nexp = (NoteExploder)mappedExploders.get(exploderType).get(fname);
					addNoteExploderToSelectionList(freq, nexp);
				}
			}
		}

	}

}
