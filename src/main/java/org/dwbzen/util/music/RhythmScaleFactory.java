package org.dwbzen.util.music;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * All IRhythmScale factory instances should be created by the getRhythmScaleFactory instance returned.
 * 
 * @author don_bacon
 *
 */
public class RhythmScaleFactory  {

	public static final String DEFAULT_RHYTHM_SCALE_FACTORY = "org.dwbzen.util.music.StandardRhythmScaleFactory";
	public static final String DEFAULT_RHYTHM_SCALE_NAME = "StandardRhythmScale";
	
	static Map<String,  IRhythmScaleFactory> factoryClassMap = new HashMap<String, IRhythmScaleFactory>();
	
	/**
	 * RHYTHM_SCALE_TYPES used in config.properties to create a global RhythmScale
	 * and instrument-specific RhythmScales if any.
	 * For example 
	 * 	   score.rhytyhmScale.all.factory=StandardRhythmScaleFactory
	 *     score.rhythmScale.instrument.PianoLH.factory=Monophonic16StandardRhythmScaleFactory
	 */
	static String[] RHYTHM_SCALE_FACTORIES = { 
			"org.dwbzen.util.music.Monophonic16StandardRhythmScaleFactory",
			"org.dwbzen.util.music.StandardRhythmScaleFactory",
			"org.dwbzen.util.music.PolyphonicRhythmScaleFactory"
	};
	static String[] RHYTHM_SCALE_FACTORY_NAMES = { 
			"Monophonic16StandardRhythmScale",
			"StandardRhythmScale",
			"PolyphonicRhythmScale"
	};
	
	static {
		String factoryName = null;
		String rsName = null;
		for(int index = 0; index < RHYTHM_SCALE_FACTORIES.length; index++) {
			try {
				factoryName = RHYTHM_SCALE_FACTORIES[index];
				rsName = RHYTHM_SCALE_FACTORY_NAMES[index];
				@SuppressWarnings("unchecked")
				Class<IRhythmScaleFactory> fclass = (Class<IRhythmScaleFactory>) Class.forName(factoryName);
				Constructor<IRhythmScaleFactory> constructor = fclass.getConstructor();
				IRhythmScaleFactory factory = constructor.newInstance((Object[])null);
				factoryClassMap.put(rsName, factory);
			} catch (Exception e) {
				System.err.println("RhythmScaleFactory Could not create Factory: " + factoryName);
				e.printStackTrace();
			}
		}
	}
	

	public RhythmScaleFactory() {
	}
	
	public static IRhythmScaleFactory getRhythmScaleFactory(String rhythmScaleName) {
		IRhythmScaleFactory factory = null;
		if(factoryClassMap.containsKey(rhythmScaleName)) {
			factory = factoryClassMap.get(rhythmScaleName);
		}
		
		return factory;		
	}

}
