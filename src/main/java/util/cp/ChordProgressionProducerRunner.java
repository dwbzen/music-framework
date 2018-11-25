package util.cp;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mathlib.cp.CollectorStats;
import mathlib.cp.MarkovChain;
import music.element.song.ChordFormula;
import music.element.song.ChordProgression;
import music.element.song.ChordProgressionScrapbook;
import music.element.song.HarmonyChord;
import music.element.song.Song;
import music.element.song.Songbook;
import util.music.SongManager;

public class ChordProgressionProducerRunner {
	protected static final Logger log = LogManager.getLogger(ChordProgressionProducer.class);
	
	/**
	 * -seed : the chord names to use as a seed for production. For example, "G7 C".
	 * 		   The #chords must equal keylen specified.
	 * @param args
	 */
	public static void main(String...args) {
		
		String songInputFile = null;			// complete path to .JSON Song file TODO
		String songCollectionName = "songs";
		String chordFormulaCollectionName = "chord_formulas";
		String query = null;
		int num = 5;
		int order = 2;
		boolean sort = false;
		boolean reuse = false;
		boolean statistical = true;
		int recycleSeedNumber = 10;
		String seedString = null;		// specify as Chord names as in "C7 F" for example
		int repeats = 1;
		boolean useOriginalKey = false;
		boolean debug = false;
		int minlength = 0;
		int maxlength = 0;
		boolean enableDisplay = true;
		boolean trace = false;
		
		for(int i=0; i<args.length; i++) {
			if(args[i].startsWith("-song")) {
				String[] songargs = args[++i].split(":");
				if(songargs[0].equalsIgnoreCase("file")) {
					songInputFile = songargs[1];
				}
				else if(songargs[0].equalsIgnoreCase("collection")) {
					songCollectionName = songargs[1];
				}
			}
			else if(args[i].startsWith("-chords")) {
				String[] chordargs = args[++i].split(":");
				if(chordargs[0].equalsIgnoreCase("collection")) {
					chordFormulaCollectionName = chordargs[1];
				}
			}
			else if(args[i].equalsIgnoreCase("-query")) {
				query = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-order")) {
				order = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-num")) {
				num = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-sort")) {
				sort = true;
			}
			else if(args[i].equalsIgnoreCase("-debug")) {
				debug = true;
			}
			else if(args[i].equalsIgnoreCase("-repeat")) {
				repeats = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-minLength")) {
				minlength = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-maxLength")) {
				maxlength = Integer.parseInt(args[++i]);
			}
			else if(args[i].startsWith("-recycle")) {
				recycleSeedNumber = Integer.parseInt(args[++i]);
			}
			else if(args[i].equalsIgnoreCase("-seed")) {
				seedString = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-reuse")) {
				reuse = true;
			}
			else if(args[i].startsWith("-original")) {	// as in originalKey
				useOriginalKey = true;
			}
			else if(args[i].equalsIgnoreCase("-trace")) {
				trace = args[++i].equalsIgnoreCase("true") ? true : false;
			}
		}
		
		if(debug) {
			HarmonyChord.setIncludeSpellingInToString(false);	// set to true if you want to see the spelling of each chord
		}
		SongManager songMgr = new SongManager(songCollectionName, songInputFile, query);
		songMgr.loadSongs();
		Songbook songbook = songMgr.getSongbook();

		Map<String, ChordFormula> chordFormulas = songMgr.getChordFormulas();
		HarmonyChordCollector collector = new HarmonyChordCollector(order);
		CollectorStats.trace = trace;
		collector.setTrace(trace);
		collector.setUseOriginalKey(useOriginalKey);
		for(Song song : songbook) {
			collector.accept(song);
		}
		MarkovChain<HarmonyChord, ChordProgression, Song> markovChain = collector.getMarkovChain();
		
		ChordProgression seedProgression = null;
		if(seedString != null) {
			/*
			 * Create a ChordProgression from the seed on the command line
			 */
			seedProgression = ChordProgressionProducer.createSeedChordProgression(chordFormulas, seedString);
		}
		else {
			seedProgression = new ChordProgression();	// let the Producer pick the seed
		}
		log.debug("seed ChordProgression: " + seedProgression.toString());
		
		ChordProgressionProducer chordProgressionProducer = ChordProgressionProducer.getChordProgressionProducer(order, markovChain, seedProgression);
		chordProgressionProducer.setNumberToGenerate(num);
		chordProgressionProducer.setSortedResult(sort);
		chordProgressionProducer.setStatisticalPick(statistical);
		chordProgressionProducer.setRecycleSeedNumber(recycleSeedNumber);
		chordProgressionProducer.setReuseSeed(reuse);
		if(minlength > 0) {
			chordProgressionProducer.setMinimumLength(minlength);
		}
		if(maxlength > 0) {
			chordProgressionProducer.setMaximumLength(maxlength);
		}

		for(int nr=1; nr<=repeats; nr++) {
			ChordProgressionScrapbook chordProgressionSet = chordProgressionProducer.produce(enableDisplay);
			if(chordProgressionSet != null && !enableDisplay) {
				for(ChordProgression cp : chordProgressionSet) {
					System.out.println(cp);
				}
			}
		}
		
	}

}
