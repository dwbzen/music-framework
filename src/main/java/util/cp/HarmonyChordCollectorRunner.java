package util.cp;

import java.io.IOException;

import mathlib.cp.MarkovChain;
import mathlib.cp.OutputStyle;
import music.element.song.ChordProgression;
import music.element.song.HarmonyChord;
import music.element.song.Song;
import music.element.song.Songbook;
import util.music.SongManager;

public class HarmonyChordCollectorRunner {
	static boolean displayMarkovChain = false;
	static boolean sorted = false;	// applies to MarkovChain
	static boolean displaySummaryMap = false;
	static boolean displayInvertedSummary = false;

	static OutputStyle outputStyle = OutputStyle.TEXT;
	
	/**
	 * Syntax:  SongCollector -songs [file:filename | collection:collectionName] [-query queryString] -keylen n [-print | -noprint] [-summary] <br>
	 * example: SongCollector -songs collection:songs -query "artist:The Beatles" -keylen 2 -print -summary<br>
	 * Uses the MongoDB "chord_formulas" collection for chords; can override in command line.<br>
	 * Default Mongo song collection is "songs"
	 * 
	 * @param args
	 * @throws IOException
	 * 
	 */public static void main(String... args) throws IOException {
		String songInputFile = null;			// complete path to .JSON Song file TODO
		String songCollectionName = "songs";
		String chordFormulaCollectionName = "chord_formulas";
		String query = null;

		boolean useOriginalKey = false;
		int order = 2;

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
			else if(args[i].startsWith("-display")) {
				String[] formats = args[++i].split(",");
				for(String f : formats) {
					if(f.startsWith("markov")) { displayMarkovChain = true; }
					else if(f.startsWith("summary")) { displaySummaryMap = true; }
					else if(f.startsWith("inverted")) { displayInvertedSummary = true; }
				}
			}
			else if(args[i].equalsIgnoreCase("-output")) {
				String[] outputFormats = args[++i].split(",");
				// text, csv, json, pretty
				for(String f : outputFormats) {
					if(f.equalsIgnoreCase("json")) { outputStyle = OutputStyle.JSON; }
					else if(f.startsWith("pretty")) { outputStyle = OutputStyle.PRETTY_JSON; }
					else if(f.equalsIgnoreCase("text")) { outputStyle = OutputStyle.TEXT; }
					else if(f.equalsIgnoreCase("csv")) { outputStyle = OutputStyle.CSV; }
				}
			}
			else if(args[i].equalsIgnoreCase("-sorted")) {
				sorted = args[++i].equalsIgnoreCase("true") ? true : false;
			}
			else if(args[i].startsWith("-original")) {	// as in originalKey
				useOriginalKey = true;
			}
		}
		SongManager songMgr = new SongManager(songCollectionName, songInputFile, query);
		songMgr.loadSongs();
		Songbook songbook = songMgr.getSongbook();
		HarmonyChordCollector collector = HarmonyChordCollector.getChordProgressionCollector(songbook, order);
		collector.setUseOriginalKey(useOriginalKey);
		collector.collect();
		
		MarkovChain<HarmonyChord, ChordProgression, Song> markovChain = collector.getMarkovChain();
		HarmonyChord.setIncludeSpellingInToString(false);	// set to true if you want to see the spelling of each chord
		if(displayMarkovChain) {
			if(sorted) {
				String s = markovChain.getSortedDisplayText(outputStyle);
				System.out.println(s);
			}
			else {
				System.out.println( outputStyle==OutputStyle.JSON ? markovChain.toJson() :  markovChain.getMarkovChainDisplayText()); 
			}
		}
		if(displaySummaryMap) { 
			System.out.println(markovChain.getSummaryMapText()); 
		}
		if(displayInvertedSummary) { 
			boolean displayJson = outputStyle==OutputStyle.JSON || outputStyle==OutputStyle.PRETTY_JSON;
			System.out.println(markovChain.getInvertedSummaryMapText(displayJson , outputStyle==OutputStyle.PRETTY_JSON));
		}
	}
	
}
