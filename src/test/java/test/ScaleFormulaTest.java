package test;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.dwbzen.music.element.Key;
import org.dwbzen.music.element.Pitch;
import org.dwbzen.music.element.ScaleFormula;
import org.dwbzen.music.element.song.ChordFormula;

public class ScaleFormulaTest {
	static ObjectMapper mapper = new ObjectMapper();
	static final Logger log = LogManager.getLogger(ScaleFormula.class);
	
	/**
	 * Test serialization and deserialization
	 * 
	 * @param strings
	 */
	public static void main(String...strings) {
		int[] formula = {2 , 2 , 1 , 2 , 2 , 2 , 1};
		String[] groups = {"major", "diatonic"};
		
		// { "name" : "Major" , "groups" : [ "major", "diatonic"] , "formula" : [ 2 , 2 , 1 , 2 , 2 , 2 , 1] , "size" : 7}
		ScaleFormula sf = new ScaleFormula("Major", groups, formula, null );
		String jstr = sf.toJson();
		System.out.println(jstr);
		ScaleFormula scaleFormula = null;
		try {
			scaleFormula = mapper.readValue(jstr, ScaleFormula.class);
		} catch (IOException e) {
			log.error("Cannot deserialize " + jstr + "\nbecause " + e.toString());
		}
		if(scaleFormula != null) {
			System.out.println(scaleFormula.toJson());
		}
		
		String sfString = 
				"{ \"name\" : \"Minor\" , \"alternateNames\" : [ \"Natural minor\" , \"Melodic minor descending\"] , \"groups\" : [ \"minor\"] , \"formula\" : [ 2 , 1 , 2 , 2 , 1 , 2 , 2] , \"size\" : 7}";
		System.out.println(sfString);
		try {
			scaleFormula = mapper.readValue(sfString, ScaleFormula.class);
		} catch (IOException e) {
			log.error("Cannot deserialize " + sfString + "\nbecause " + e.toString());
		}
		if(scaleFormula != null) {
			System.out.println(scaleFormula.toJson());
		}
		
		/*
		 * test createPitches with serialization
		 */
		Pitch root = Pitch.D;
		createPitches(scaleFormula, root, null);
		
		root = new Pitch("F4");
		Key key = Key.F_MINOR;
		createPitches(scaleFormula, root, key);
		
		/*
		 * scale formula number
		 */
		int[] scaleFormula2 = {1, 2, 1, 2, 1, 2, 2, 1};
		int sfnumber = ChordFormula.computeFormulaNumber(scaleFormula2);
		System.out.println(sfnumber);
		
	}
	
	static void createPitches(ScaleFormula sf, Pitch root, Key key) {
		List<Pitch> scalePitches = (key == null) ? sf.createPitches(root) : sf.createPitches(root, key);
		try {
			String pitches = mapper.writeValueAsString(scalePitches);
			System.out.println("pitches: " + pitches);
		} catch (JsonProcessingException e) {
			System.err.println("Cannot serialize because " + e.toString());
			e.printStackTrace();
		}
		StringBuffer sb = new StringBuffer("{ ");
		for(Pitch p : scalePitches) {
			sb.append(p.toString()).append(", ");
		}
		sb.deleteCharAt(sb.length()-2);
		sb.append("}");
		System.out.println(sb);
	}
	


}
