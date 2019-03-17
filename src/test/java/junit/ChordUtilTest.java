package junit;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.TestCase;
import org.dwbzen.music.element.song.ChordInfo;

public class ChordUtilTest extends TestCase {
	static final org.apache.log4j.Logger log = Logger.getLogger(ChordUtilTest.class);
	
	static String[] testCases = {
		"Am9", "A", "B7",  "Cdim", "Db9", 	"Em(M7)", "F7#11", "Gadd9", "G#sus6", "A5", "0",
		"A/E", "B7/A", "Cdim/Eb",  "Db9/Bb","Em(M7)/D#", "F7#11/C", "Gadd9/D", "Absus6/F", "A5/E"
	};
	@Test
	public void testParseChord() {
		for(int i=0; i<testCases.length;i++) {
			String chordName = testCases[i];
			ChordInfo result = ChordInfo.parseChordName(chordName);
			assertTrue(result != null);
			log.info(chordName + ": " + 
					"name: " + result.getChordName() + 	", root: " + result.getRootNote() +
					", bass: " + result.getBassNote() + ", symbol: " + result.getChordSymbol());
		}
	}

}
