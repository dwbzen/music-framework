package util.cp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import mathlib.cp.CollectorStats;
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
	 * Usage:  HarmonyChordCollectorRunner <br>
	 * 	-songs [file:filename | collection:collectionName] <br>
	 *  -query queryString <br>
	 *  -order n[,n2,...] <br>
	 *  -display [markov | summary | inverted] <br>
	 *  -output <list of output formats: json, text, csv, pretty, suppliers> <br>
	 *  -sorted [true | false ] <br>
	 *  -original <br>
	 *  -trace [true | false] <br>
	 * example: HarmonyChordCollectorRunner -songs collection:songs -query "artist:The Beatles" -order 1,2 -display markov -output text<br>
	 * Uses the MongoDB "chord_formulas" collection for chords; can override in command line.<br>
	 * Default Mongo song collection is "songs"
	 * 
	 * @param args
	 * @throws IOException
	 * 
	 */public static void main(String... args) throws IOException {
		String songInputFile = null;			// complete path to .JSON Song file TODO
		String songCollectionName = "songs";
		String query = null;
		boolean trace = false;
		boolean showSupplierCounts = false;
		boolean useOriginalKey = false;
		String orderstring = null;
		List<Integer> orderList = new ArrayList<Integer>();

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
			else if(args[i].equalsIgnoreCase("-query")) {
				query = args[++i];
			}
			else if(args[i].equalsIgnoreCase("-order")) {
				orderstring = args[++i];
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
					else if(f.equalsIgnoreCase("suppliers")) { showSupplierCounts = true;}
				}
			}
			else if(args[i].equalsIgnoreCase("-sorted")) {
				sorted = args[++i].equalsIgnoreCase("true") ? true : false;
			}
			else if(args[i].startsWith("-original")) {	// as in originalKey
				useOriginalKey = true;
			}
			else if(args[i].equalsIgnoreCase("-trace")) {
				trace = args[++i].equalsIgnoreCase("true") ? true : false;
			}
		}
		
		if(orderstring == null) {
			orderList.add(2);	// default order is 2 if not specified
		}
		else {
			for(String order : orderstring.split(",")) {
				orderList.add(Integer.parseInt(order));
			}
		}
		SongManager songMgr = new SongManager(songCollectionName, songInputFile, query);
		songMgr.loadSongs();
		Songbook songbook = songMgr.getSongbook();
		Map<Integer, MarkovChain<HarmonyChord, ChordProgression, Song>> markovChains = new TreeMap<>();
		for(Integer order : orderList) {
			HarmonyChordCollector collector = HarmonyChordCollector.getChordProgressionCollector(songbook, order);
			CollectorStats.trace = trace;
			collector.setTrace(trace);
			collector.setUseOriginalKey(useOriginalKey);
			collector.collect();
			markovChains.put(order, collector.getMarkovChain());
		}
		
		HarmonyChord.setIncludeSpellingInToString(false);	// set to true if you want to see the spelling of each chord
		MarkovChain<HarmonyChord, ChordProgression, Song> markovChain = combineMarkovChains(markovChains);
		if(displayMarkovChain) {
			if(sorted) {
				String s = markovChain.getSortedDisplayText(outputStyle, showSupplierCounts);
				System.out.println(s);
			}
			else {
				System.out.println( outputStyle==OutputStyle.JSON ? markovChain.toJson() :  markovChain.getMarkovChainDisplayText(showSupplierCounts)); 
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
	 
	 private static MarkovChain<HarmonyChord, ChordProgression, Song> combineMarkovChains(Map<Integer, MarkovChain<HarmonyChord, ChordProgression, Song>> markovChains) {
		 MarkovChain<HarmonyChord, ChordProgression, Song> markovChain = null;
			for(Integer ord : markovChains.keySet()) {
				if(markovChain == null) {
					markovChain = markovChains.get(ord);
				}
				else {
					markovChain.add(markovChains.get(ord));
				}
			}
			return markovChain;
	 }
	
}
