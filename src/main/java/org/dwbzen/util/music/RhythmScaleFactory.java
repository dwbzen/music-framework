package org.dwbzen.util.music;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * All IRhythmScale factory instances should be created by the getRhythmScaleFactory instance returned.
 * 
 * @author bacond6
 *
 */
public class RhythmScaleFactory  {

	public final static int[] standardBaseUnits = {1, 2, 3, 4, 6, 8, 10, 12, 14, 16};
	
	public static int standardRoot = 16;
	public static final String DEFAULT_RHYTHM_SCALE_FACTORY = "util.music.Monophonic16StandardRhythmScaleFactory";
	public static final String DEFAULT_RHYTHM_SCALE_NAME = "Monophonic16StandardRhythmScale";
	
	static Map<String,  IRhythmScaleFactory> factoryClassMap = new HashMap<String, IRhythmScaleFactory>();
	
	/**
	 * RHYTHM_SCALE_TYPES used in config.properties to create a global RhythmScale
	 * and instrument-specific RhythmScales if any.
	 * For example 
	 * 	   score.rhytyhmScale.all.factory=Standard16RhythmScaleFactory
	 *     score.rhythmScale.instrument.PianoLH.factory=Monophonic16StandardRhythmScaleFactory
	 */
	static String[] RHYTHM_SCALE_FACTORIES = { 
			"util.music.Standard16RhythmScaleFactory",
			"util.music.Monophonic16StandardRhythmScaleFactory"
	};
	static String[] RHYTHM_SCALE_FACTORY_NAMES = { 
			"Standard16RhythmScale",
			"Monophonic16StandardRhythmScale"
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
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				System.err.println("Factory not found: " + factoryName);
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
