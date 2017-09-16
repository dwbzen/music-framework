package music.musicxml;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;

import com.audiveris.proxymusic.AboveBelow;
import com.audiveris.proxymusic.Attributes;
import com.audiveris.proxymusic.Clef;
import com.audiveris.proxymusic.ClefSign;
import com.audiveris.proxymusic.Direction;
import com.audiveris.proxymusic.DirectionType;
import com.audiveris.proxymusic.Empty;
import com.audiveris.proxymusic.EmptyPlacement;
import com.audiveris.proxymusic.FormattedText;
import com.audiveris.proxymusic.Identification;
import com.audiveris.proxymusic.Notations;
import com.audiveris.proxymusic.NoteType;
import com.audiveris.proxymusic.PartList;
import com.audiveris.proxymusic.PartName;
import com.audiveris.proxymusic.ScoreInstrument;
import com.audiveris.proxymusic.ScorePartwise;
import com.audiveris.proxymusic.ScorePartwise.Part;
import com.audiveris.proxymusic.Sound;
import com.audiveris.proxymusic.StaffDetails;
import com.audiveris.proxymusic.StartStop;
import com.audiveris.proxymusic.StartStopContinue;
import com.audiveris.proxymusic.Tie;
import com.audiveris.proxymusic.Tied;
import com.audiveris.proxymusic.Time;
import com.audiveris.proxymusic.TimeModification;
import com.audiveris.proxymusic.Transpose;
import com.audiveris.proxymusic.Tuplet;
import com.audiveris.proxymusic.TypedText;
import com.audiveris.proxymusic.Work;
import com.audiveris.proxymusic.YesNo;
import com.audiveris.proxymusic.util.Marshalling;
import com.audiveris.proxymusic.util.Marshalling.MarshallingException;

import music.element.Chord;
import music.element.Cleff;
import music.element.Duration;
import music.element.Interval;
import music.element.Measurable;
import music.element.Measurable.TieType;
import music.element.Measurable.TupletType;
import music.element.Measure;
import music.element.Note;
import music.element.Pitch;
import music.element.PitchClass;
import music.element.Score;
import music.element.ScorePartEntity;
import music.element.Step;
import music.element.Tempo;
import music.instrument.Instrument;
import music.instrument.MidiInstrument;
import util.Ratio;

/**
 * Turns a Score into MusicXML, specifically ScorePartwise instance
 * For clarity, all com.audiveris.proxymusic instance
 * variables prefixed with an underscore (_)
 * 
 * NOTE - there's a problem with playback of certain percussion instruments
 * in Sibelius 8.5 MusicXML files. Appears to be independent of whether the XML
 * was generated by the framework or exported from a .sib file within Sibelius.
 * WORKAROUND - select the first rest in the score for the instrument -
 * all the single-line unpitched percussion have this issues -
 * and do a Change Instrument, selecting the same instrument (Cymbals for example).
 * This fixes the playback, now save a a .sib file.
 * Exporting back to XML however does not correct the problem.
 * 
 * NOTE - Sibelius does not import the PianoLH part correctly (Okay in MuseScore).
 * To correct do a change instrument to Piano as in the above then place an F-cleff in the first measure. 
 * Use Notations --> Bracket to group LH and RH parts in liew of using <part-group>
 * 
 * @see http://www.musicxml.com/
 * @author don_bacon
 *
 */
public class MusicXMLHelper {
	protected static final org.apache.log4j.Logger log = Logger.getLogger(MusicXMLHelper.class);
	
	static final Pitch UNPITCHED_DEFAULT = new Pitch(Step.E, 4);	// for unpitched percussion
	
	private Score score;
	private Properties configProperties = null;

	private ScorePartwise _scorePartwise;
	private Work _work = null;
	private boolean scoreMidi = false;	// TODO make this configurable
	
	public static void main(String[] args) {
		boolean marshall = false;		// TODO
		boolean unmarshall = false;
		String filename = null;
		for(int i=0; i<args.length; i++) {
			if(args[i].equalsIgnoreCase("marshall")) {
				marshall = true;
			}
			else if(args[i].equalsIgnoreCase("unmarshall")) {
				unmarshall = true;
			}
			else if(args[i].equalsIgnoreCase("-f")) {
				filename = args[++i];
			}
		}
		try {
			if(unmarshall && filename != null) {
				FileInputStream fis = new FileInputStream(new File(filename));
				ScorePartwise scorePartwise = (ScorePartwise)Marshalling.unmarshal(fis);
				List<Part> parts = scorePartwise.getPart();
				ScorePartwise.Part part = parts.get(0);
				Object id = part.getId();
				System.out.println(id);
				fis.close();
			}
		}
		catch(Exception e) {
			System.err.println("Exception: " + e);
		}
	}


	public MusicXMLHelper(Score score, Properties props) {
		this.score = score;
		this.configProperties = props;
	}
	
	public void marshall(OutputStream os) {
		if(_scorePartwise == null) {
			convert();
		}
		try {
			// this also adds Identification information to the XML
			// signature composed of ProxyMusic version and date of marshalling.
			// and sets indent level to 2 spaces
			Marshalling.marshal(_scorePartwise, os, true, new Integer(2));
		} catch ( MarshallingException e) {
			System.err.println("Marshalling exception: " + e.toString());
		}
	}
	
	/**
	 * convert the Score to a MusicXML ScorePartwise
	 * For clarity, all com.audiveris.proxymusic class instance
	 * variables start with an underscore (_)
	 * @return
	 */
	public ScorePartwise convert() {
		_scorePartwise = new ScorePartwise();
		// build ScorePartwise from Score and other configuration information
		_work = new Work();
		_work.setWorkNumber(score.getWorkNumber());
		_work.setWorkTitle(score.getTitle());
		_scorePartwise.setWork(_work);
		Identification _id = new Identification();
		com.audiveris.proxymusic.ScorePartwise.Part.Measure _measure = null;
		for(String type : score.getCreators().keySet()) {
			TypedText _tt = new TypedText(); 
			_tt.setType(type);
			_tt.setValue(score.getCreators().get(type));
			_id.getCreator().add(_tt);
		}
		TypedText _rights = new TypedText();
		_rights.setType("rights");
		_rights.setValue("Copyright (C) 2017 Donald W. Bacon");
		_id.getRights().add(_rights);
		
		_scorePartwise.setIdentification(_id);
		List<ScorePartwise.Part> _parts = _scorePartwise.getPart();
		PartList _partList = new PartList();	// add com.audiveris.proxymusic.ScorePart
		int partnum = 1;
		com.audiveris.proxymusic.MidiInstrument _midiInstrument = null;
		for(String instrumentName : score.getInstrumentNames()) {
			ScorePartEntity scorePartEntity = score.getScorePartEntityForInstrument(instrumentName);
			Instrument instrument = scorePartEntity.getInstrument();
			String partname = instrument.getPartName();
			MidiInstrument midiInstrument = instrument.getMidiInstrument();
			midiInstrument.setMidiChannel(partnum);

			com.audiveris.proxymusic.ScorePart _scorePart = new com.audiveris.proxymusic.ScorePart();
			PartName _partName = new PartName();
			PartName _abbreviationPartName = new PartName();
			_partName.setValue(partname);
			_abbreviationPartName.setValue(instrument.getAbreviation());
			_scorePart.setPartName(_partName);
			String partId = scorePartEntity.getPartId();
			_scorePart.setId(partId);
			_scorePart.setPartAbbreviation(_abbreviationPartName);
			ScoreInstrument _scoreInstrument = new ScoreInstrument();
			_scoreInstrument.setId(partId + "I" + partnum);
			_scoreInstrument.setInstrumentName(instrument.getInstrumentName());
			_scoreInstrument.setInstrumentAbbreviation(instrument.getAbreviation());
			_scoreInstrument.setInstrumentSound(instrument.getInstrumentSound());
			com.audiveris.proxymusic.VirtualInstrument _virtualInstrument = new com.audiveris.proxymusic.VirtualInstrument();
			_virtualInstrument.setVirtualLibrary(instrument.getVirtualLibrary());
			_virtualInstrument.setVirtualName(instrument.getVirtualName());
			_scoreInstrument.setVirtualInstrument(_virtualInstrument);
			/*
			 * Score midi instrument (optional)
			 */
			if(scoreMidi) {
				_midiInstrument = new com.audiveris.proxymusic.MidiInstrument();
				// _midiInstrument.setId(partId);
				_midiInstrument.setMidiChannel(midiInstrument.getMidiChannel());
				_midiInstrument.setVolume(BigDecimal.valueOf(80));
				_midiInstrument.setMidiProgram(midiInstrument.getMidiProgram());
				_midiInstrument.setPan(BigDecimal.ZERO);
			}
			_partList.getPartGroupOrScorePart().add(_scorePart);
			_scorePart.getMidiDeviceAndMidiInstrument().add(_midiInstrument);
			_scorePart.getScoreInstrument().add(_scoreInstrument);
			Part _part = new Part();
			_part.setId(_scorePart);
			_parts.add(_part);
			_measure = new com.audiveris.proxymusic.ScorePartwise.Part.Measure();
			_measure.setImplicit(YesNo.YES);
			_measure.setNumber("0");
			Attributes _attributes = new Attributes();
			int measNum = 0;
			for(Measure measure : scorePartEntity.getMeasures()) {
				/*
				 * Measure 0 sets Attributes, Staff & Cleff info
				 */
				if(measNum == 0) {
					_attributes.setDivisions(BigDecimal.valueOf(measure.getDivisions()));
					com.audiveris.proxymusic.Key _key = new com.audiveris.proxymusic.Key();
					music.element.Key key = measure.getKey();
					if(instrument.isTransposes()) {
						// create <transpose> section
						Interval transposeInterval = instrument.getTransposeInterval();
						Transpose _transpose = new Transpose();
						_transpose.setChromatic(BigDecimal.valueOf(transposeInterval.getInterval()));
						_transpose.setDiatonic(BigInteger.valueOf(transposeInterval.getInterval()));
						_transpose.setOctaveChange(BigInteger.valueOf(transposeInterval.getOctave()));
						_attributes.getTranspose().add(_transpose);
					}
					/*
					 * Set Key and Time Signature
					 */
					String modename = key.getModeName();
					int fifths = key.getFifths();
					_key.setFifths(BigInteger.valueOf(fifths));
					_key.setMode(modename);
					_attributes.getKey().add(_key);
					_attributes.setStaves(BigInteger.valueOf(1));	// NOTE - will be 2 for piano, 3 for Organ
					Time _time = new Time();
					JAXBElement<String> _e1 = new JAXBElement<String>(new QName("beats"), String.class, "4");
					JAXBElement<String> _e2 = new JAXBElement<String>(new QName("beat-type"), String.class, "4");
					_time.getTimeSignature().add(_e1);
					_time.getTimeSignature().add(_e2);
					_attributes.getTime().add(_time);
					
					// set the Cleff(s) for this instrument
					setClefsInAttributes(_attributes, instrument);
					_measure.getNoteOrBackupOrForward().add(_attributes);
					_part.getMeasure().add(_measure);
					
					// add StaffDetails
					StaffDetails _staffDetails = new StaffDetails();
					_staffDetails.setNumber(BigInteger.ONE);
					_staffDetails.setPrintObject(YesNo.YES);
					if(instrument.getPitchClass()==PitchClass.UNPITCHED) {
						_staffDetails.setStaffLines(BigInteger.ONE);
					}
					else if(instrument.getPitchClass()==PitchClass.DISCRETE_2LINE) {
						_staffDetails.setStaffLines(BigInteger.valueOf(2));
					}
					_attributes.getStaffDetails().add(_staffDetails);
					
					// set measure direction - includes direction-type words and sound tempo=nnn
					if(partnum == 1) {
						Direction _direction = new Direction();
						_direction.setPlacement(AboveBelow.ABOVE);
						DirectionType _directionType = new DirectionType();
						FormattedText _formattedText = new FormattedText();
						Tempo tempo = measure.getTempo();
						_formattedText.setValue(Tempo.getTempoMarking(tempo.getBeatsPerMinute()));
						_directionType.getWords().add(_formattedText);
						_direction.getDirectionType().add(_directionType);
						_direction.setStaff(BigInteger.ONE);
						Sound _sound = new Sound();
						_sound.setTempo(new BigDecimal(tempo.getBeatsPerMinute()));
						_direction.setSound(_sound);
						_measure.getNoteOrBackupOrForward().add(_direction);
					}
				}
				_measure = new com.audiveris.proxymusic.ScorePartwise.Part.Measure();
				_measure.setNumber(String.valueOf(++measNum));
				for(Measurable m : measure.getMeasureables()) {
					log.trace("xml measure: " + measure.getNumber() + " note: " + m);
					if(m.getType().equals(Measurable.CHORD)) {
						/*
						 * notes in a chord indicated by <chord/> in all the notes following the first one
						 */
						Chord chord = (Chord)m;
						log.info("chord: " + chord.toString());
						java.util.Iterator<Note> it = chord.removeUnisonNotes().iterator();
						boolean inChord = false;
						while(it.hasNext()) {
							Note note = it.next();
							convertNote(instrument, measure, note, inChord, _measure);
							inChord = true;
						}
					}
					else {
						convertNote(instrument, measure, m, false, _measure);
					}
				}
				_part.getMeasure().add(_measure);
			}
			partnum++;
		}
		_scorePartwise.setPartList(_partList);
		return _scorePartwise;
	}

	private void convertNote(Instrument instrument, Measure measure, Measurable m, boolean inChord, com.audiveris.proxymusic.ScorePartwise.Part.Measure _measure) {
		Note note = (Note)m;
		if(instrument.isTransposes()) {
			// note.getPitch().increment(instrument.getTranspositionSteps());
		}
		PitchClass pitchClass = instrument.getPitchClass();		// PITCHED, UNPITCHED, DISCRETE_2LINE or DISCRETE_5LINE
		/*
		 * Assume that Duration units have already been factored and set
		 * according to Measure attributes
		 */
		Cleff currentClef = instrument.getCleffs().get(0);
		Duration duration = note.getDuration();
		int units = duration.getDurationUnits();
		int ndots = duration.getDots();
		Ratio ratio = duration.getRatio();	// tuplets
		boolean isTuplet = !duration.isRatioSame();
		
		com.audiveris.proxymusic.Note _note = new com.audiveris.proxymusic.Note();
		
		/*
		 * Sample unpitched
		 *	<unpitched>
     			<display-step>F</display-step>
     			<display-octave>5</display-octave>
    		</unpitched>
		 */
		int octave = 0;
		int octaveShift = 0;
		int alt = 0;
		com.audiveris.proxymusic.Step _step = null;
		switch(pitchClass) {
		case UNPITCHED:
		case DISCRETE_2LINE:
			octave = note.getPitch().getOctave();
			_step = getStep(note);
			com.audiveris.proxymusic.Unpitched _unpitched = new com.audiveris.proxymusic.Unpitched();
			_unpitched.setDisplayOctave(octave);
			_unpitched.setDisplayStep(_step);
			_note.setUnpitched(_unpitched);
			break;
		case DISCRETE_5LINE:
		case PITCHED:
			com.audiveris.proxymusic.Pitch _pitch = new com.audiveris.proxymusic.Pitch();
			octave = note.getPitch().getOctave();
			octaveShift = currentClef.getOctaveShift();
			_step = getStep(note);
			alt = note.getPitch().getAlteration();
			octave += octaveShift;	// adjust the pitch Octave if 8va, 15va, 8ma, or 15ma
			_pitch.setOctave(octave);
			_pitch.setStep(_step);
			_pitch.setAlter(BigDecimal.valueOf(alt));
			_note.setPitch(_pitch);
		}

		if(inChord) {
			_note.setChord(new Empty());
		}
		if(ndots > 0) {
			for(int id=0; id<ndots; id++) {
				_note.getDot().add(new EmptyPlacement());
			}
		}
		_note.setVoice(String.valueOf(note.getVoice()));
		_note.setDuration(BigDecimal.valueOf(units));
		String nt = note.getNoteType();
		if(isTuplet) {
			createTupletNote(note, _note, ratio, nt);
		}
		if(nt != null && !nt.equals("?")) {
			NoteType _noteType = new NoteType();
			_noteType.setValue(nt);
			_note.setType(_noteType);
		}
		if(note.getTieType().equals(TieType.START)) {
			/*
			 * tied to the next note
			 */
			Tie _tie = new Tie();
			_tie.setType(StartStop.START);
			_note.getTie().add(_tie);
			Notations _notations = new Notations();
			Tied _tied = new Tied();
			_tied.setType(StartStopContinue.START);
			_notations.getTiedOrSlurOrTuplet().add(_tied);
			_note.getNotations().add(_notations);
		}
		else if(note.getTieType().equals(TieType.STOP)) {
			/*
			 * tie from previous note stops
			 */
			Tie _tie = new Tie();
			_tie.setType(StartStop.STOP);
			_note.getTie().add(_tie);
			Notations _notations = new Notations();
			Tied _tied = new Tied();
			_tied.setType(StartStopContinue.STOP);
			_notations.getTiedOrSlurOrTuplet().add(_tied);
			_note.getNotations().add(_notations);
		}
		else if(note.getTieType().equals(TieType.BOTH)) {
			Tie _tie1 = new Tie();
			_tie1.setType(StartStop.START);
			_note.getTie().add(_tie1);
			Tie _tie2 = new Tie();
			_tie2.setType(StartStop.STOP);
			_note.getTie().add(_tie2);
			Tied _tied1 = new Tied();
			Tied _tied2 = new Tied();
			Notations _notations = new Notations();
			_tied1.setType(StartStopContinue.START);
			_tied2.setType(StartStopContinue.STOP);
			_notations.getTiedOrSlurOrTuplet().add(_tied1);
			_notations.getTiedOrSlurOrTuplet().add(_tied2);
			_note.getNotations().add(_notations);
		}
		_measure.getNoteOrBackupOrForward().add(_note);
	}


	private void setClefsInAttributes(Attributes _attributes, Instrument instrument) {
		Clef _clef = null;
		int clefnumber = 1;
		for(music.element.Cleff clef : instrument.getCleffs()) {
			_clef = new Clef();
			_clef.setNumber(BigInteger.valueOf(clefnumber++));
			int octaveShift = clef.getOctaveShift();
			switch(clef) {
			case G:
				_clef.setSign(ClefSign.G);
				_clef.setLine(BigInteger.valueOf(2));
				break;
			case G8va:
				_clef.setSign(ClefSign.G);
				_clef.setLine(BigInteger.valueOf(2));
				break;
			case G15va:
				_clef.setSign(ClefSign.G);
				_clef.setLine(BigInteger.valueOf(2));
				break;
			case F:
				_clef.setSign(ClefSign.F);
				_clef.setLine(BigInteger.valueOf(4));
				break;
			case C:
				_clef.setSign(ClefSign.C);
				_clef.setLine(BigInteger.valueOf(3));
				break;
			case F8ma:
				_clef.setSign(ClefSign.F);
				_clef.setLine(BigInteger.valueOf(3));
				break;
			case F15ma:
				_clef.setSign(ClefSign.F);
				_clef.setLine(BigInteger.valueOf(3));
				break;
			case PERCUSSION:
				_clef.setSign(ClefSign.PERCUSSION);
				_clef.setLine(BigInteger.valueOf(2));
				break;
			case PERCUSSION_2LINE:
				_clef.setSign(ClefSign.PERCUSSION);
				_clef.setLine(BigInteger.valueOf(2));
				break;
			default:
				log.error("Invalid Cleff: " + clef);
			}
			
			//<clef-octave-change>-1</clef-octave-change> for example
			// NOTE that Sibelius 6 ignores this attribute - need to add manually to score
			//
			if(octaveShift != 0) {
				_clef.setClefOctaveChange(BigInteger.valueOf(-1 * octaveShift));
			}
			_attributes.getClef().add(_clef);
		}
	}
	
	private void createTupletNote(Note note, com.audiveris.proxymusic.Note _note, Ratio ratio, String noteType) {
		TimeModification _timeModification = new TimeModification();
		_timeModification.setActualNotes(BigInteger.valueOf(ratio.getNumberOfNotes()));
		_timeModification.setNormalNotes(BigInteger.valueOf(ratio.getTimeOf()));
		_note.setTimeModification(_timeModification);
		Notations _notations = null;
		Tuplet _tuplet = null;
		com.audiveris.proxymusic.TupletType _tupletType = null;
		if(note.getTupletType().equals(TupletType.START)) {
			_notations = new Notations();
			_tuplet = new Tuplet();
			_tuplet.setType(StartStop.START);
			if(noteType.equalsIgnoreCase("half") || noteType.equalsIgnoreCase("quarter") || noteType.equalsIgnoreCase("whole")) {
				_tuplet.setBracket(YesNo.YES);
			}
			else {
				_tuplet.setBracket(YesNo.NO);
			}
			_notations.getTiedOrSlurOrTuplet().add(_tuplet);
		}
		else if(note.getTupletType().equals(TupletType.STOP)) {
			_notations = new Notations();
			_tuplet = new Tuplet();
			_tuplet.setType(StartStop.STOP);
			_notations.getTiedOrSlurOrTuplet().add(_tuplet);
		}
		if(_notations != null) {
			_note.getNotations().add(_notations);
		}
		log.trace("add tuplet note: " + note.getTupletType() + " " + ratio + " " + note);
	}
	
	public Score getScore() {
		return score;
	}

	public void setScore(Score score) {
		this.score = score;
	}

	public Properties getConfigProperties() {
		return configProperties;
	}

	public void setConfigProperties(Properties configProperties) {
		this.configProperties = configProperties;
	}

	public ScorePartwise getScorePartwise() {
		return _scorePartwise;
	}
	
	public static com.audiveris.proxymusic.Step getStep(Note note) {
		return getStep(note.getPitch().getStep());
	}
	
	public static com.audiveris.proxymusic.Step getStep(Step step) {
		com.audiveris.proxymusic.Step _step = null;
		switch(step) {
		case A:	_step = com.audiveris.proxymusic.Step.A;
			break;
		case B: _step = com.audiveris.proxymusic.Step.B;
			break;
		case C: _step = com.audiveris.proxymusic.Step.C;
			break;
		case D: _step = com.audiveris.proxymusic.Step.D;
			break;
		case E: _step = com.audiveris.proxymusic.Step.E;
			break;
		case F: _step = com.audiveris.proxymusic.Step.F;
			break;
		case G: _step = com.audiveris.proxymusic.Step.G;
			break;
		case AFLAT: _step = com.audiveris.proxymusic.Step.A;
			break;
		case ASHARP: _step = com.audiveris.proxymusic.Step.A;
			break;
		case BFLAT: _step = com.audiveris.proxymusic.Step.B;
			break;
		case CSHARP: _step = com.audiveris.proxymusic.Step.C;
			break;
		case DFLAT: _step = com.audiveris.proxymusic.Step.D;
			break;
		case DSHARP: _step = com.audiveris.proxymusic.Step.D;
			break;
		case EFLAT: _step = com.audiveris.proxymusic.Step.E;
			break;
		case FSHARP: _step = com.audiveris.proxymusic.Step.F;
			break;
		case GFLAT: _step = com.audiveris.proxymusic.Step.G;
			break;
		case GSHARP: _step = com.audiveris.proxymusic.Step.G;
			break;
		default:
			break;
		}
		return _step;
	}
	
}
