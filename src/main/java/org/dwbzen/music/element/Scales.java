package org.dwbzen.music.element;
import org.dwbzen.music.element.Scale;
import org.dwbzen.music.element.Key.Mode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class Scales {

	/**
	 * All the defined scales in this Map
	 */
	private static Map<String, Scale> scaleMap = Collections.synchronizedMap(new HashMap<String, Scale>());

	public static final String MAJOR = "major";
	public static final String MINOR = "minor";
	public static final String MODE = "mode";		// a modal scale or mode of some scale: Dorian, etc.
	
	public static final String WHOLE_TONE = "whole tone";
	public static final String CHROMATIC_12TONE = "chromatic 12-tone";
	public static final String PENTATONIC = "pentatonic";
	public static final String DISCRETE = "discrete";	// unpitched instruments
	public static final String HEXATONIC = "hexatonic";
	public static final String ASCENDING = " ascending";
	
	/**
	 * Mapped Diatonic Scales - Major and relative minor including harmonic minor
	 * Scales for all defined flat Keys
	 * TODO add melodic ascending for all keys
	 */
	public static final Scale C_MAJOR = new Scale(Key.C_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.C, IScaleFormula.MAJOR_SCALE_FORMULA, Key.C_MAJOR);
	public static final Scale A_MINOR = new Scale(Key.A_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.A, IScaleFormula.MINOR_SCALE_FORMULA, Key.A_MINOR);
	public static final Scale A_MEDLODIC_MINOR_ASCENDING = new Scale(Key.A_MINOR_NAME + ASCENDING, MINOR, ScaleType.DIATONIC, Pitch.A, IScaleFormula.MELODIC_MINOR_ASCENDING_SCALE_FORMULA, Key.A_MINOR);
	public static final Scale A_HARMONIC_MINOR = new Scale("A-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.A, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.A_MINOR);
	
	public static final Scale F_MAJOR = new Scale(Key.F_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.F, IScaleFormula.MAJOR_SCALE_FORMULA, Key.F_MAJOR);
	public static final Scale D_MINOR = new Scale(Key.D_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.D, IScaleFormula.MINOR_SCALE_FORMULA, Key.D_MINOR);
	public static final Scale D_HARMONIC_MINOR = new Scale("D-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.D, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.D_MINOR);

	public static final Scale BFlat_MAJOR = new Scale(Key.BFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.BFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.BFlat_MAJOR);
	public static final Scale G_MINOR = new Scale(Key.G_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.G, IScaleFormula.MINOR_SCALE_FORMULA, Key.G_MINOR);
	public static final Scale G_HARMONIC_MINOR = new Scale("G-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.G, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.G_MINOR);

	public static final Scale EFlat_MAJOR = new Scale(Key.EFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.EFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.EFlat_MAJOR);
	public static final Scale C_MINOR = new Scale(Key.C_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.C, IScaleFormula.MINOR_SCALE_FORMULA, Key.C_MINOR);
	public static final Scale C_HARMONIC_MINOR = new Scale("C-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.C, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.C_MINOR);

	public static final Scale AFlat_MAJOR = new Scale(Key.AFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.AFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.AFlat_MAJOR);
	public static final Scale F_MINOR = new Scale(Key.F_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.F, IScaleFormula.MINOR_SCALE_FORMULA, Key.F_MINOR);
	public static final Scale F_HARMONIC_MINOR = new Scale("F-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.F, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.F_MINOR);

	public static final Scale DFlat_MAJOR = new Scale(Key.DFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.DFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.DFlat_MAJOR);
	public static final Scale BFlat_MINOR = new Scale(Key.BFlat_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.BFlat, IScaleFormula.MINOR_SCALE_FORMULA, Key.BFlat_MINOR);
	public static final Scale BFlat_HARMONIC_MINOR = new Scale("Bb-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.BFlat, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.BFlat_MINOR);

	public static final Scale GFlat_MAJOR = new Scale(Key.GFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.GFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.GFlat_MAJOR);
	public static final Scale EFlat_MINOR = new Scale(Key.EFlat_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.EFlat, IScaleFormula.MINOR_SCALE_FORMULA, Key.EFlat_MINOR);
	public static final Scale EFlat_HARMONIC_MINOR = new Scale("Eb-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.EFlat, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.EFlat_MINOR);

	public static final Scale CFlat_MAJOR = new Scale(Key.CFlat_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.CFlat, IScaleFormula.MAJOR_SCALE_FORMULA, Key.CFlat_MAJOR);
	public static final Scale AFlat_MINOR = new Scale(Key.AFlat_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.EFlat, IScaleFormula.MINOR_SCALE_FORMULA, Key.AFlat_MINOR);
	public static final Scale AFlat_HARMONIC_MINOR = new Scale("Ab-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.AFlat, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.AFlat_MINOR);

	/**
	 * Scales for all defined sharp keys
	 * TODO add ascending minor for all KEYS
	 */
	public static final Scale G_MAJOR = new Scale(Key.G_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.G, IScaleFormula.MAJOR_SCALE_FORMULA, Key.G_MAJOR);
	public static final Scale E_MINOR = new Scale(Key.E_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.E, IScaleFormula.MINOR_SCALE_FORMULA, Key.E_MINOR);
	public static final Scale E_HARMONIC_MINOR = new Scale("E-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.E, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.E_MINOR);

	public static final Scale D_MAJOR = new Scale(Key.D_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.D, IScaleFormula.MAJOR_SCALE_FORMULA, Key.D_MAJOR);
	public static final Scale B_MINOR = new Scale(Key.B_MINOR_NAME , MINOR, ScaleType.DIATONIC, Pitch.B, IScaleFormula.MINOR_SCALE_FORMULA, Key.B_MINOR);
	public static final Scale B_HARMONIC_MINOR = new Scale("B-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.B, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.B_MINOR);

	public static final Scale A_MAJOR = new Scale(Key.A_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.A, IScaleFormula.MAJOR_SCALE_FORMULA, Key.A_MAJOR);
	public static final Scale FSharp_MINOR = new Scale(Key.FSharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.FSharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.FSharp_MINOR);
	public static final Scale FSharp_HARMONIC_MINOR = new Scale("F#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.FSharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.FSharp_MINOR);

	public static final Scale E_MAJOR = new Scale(Key.E_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.E, IScaleFormula.MAJOR_SCALE_FORMULA, Key.E_MAJOR);
	public static final Scale CSharp_MINOR = new Scale(Key.CSharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.CSharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.CSharp_MINOR);
	public static final Scale CSharp_HARMONIC_MINOR = new Scale("C#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.CSharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.CSharp_MINOR);

	public static final Scale B_MAJOR = new Scale(Key.B_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.B, IScaleFormula.MAJOR_SCALE_FORMULA, Key.B_MAJOR);
	public static final Scale GSharp_MINOR = new Scale(Key.GSharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.GSharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.GSharp_MINOR);
	public static final Scale GSharp_HARMONIC_MINOR = new Scale("G#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.GSharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.GSharp_MINOR);

	public static final Scale FSharp_MAJOR = new Scale(Key.FSharp_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.FSharp, IScaleFormula.MAJOR_SCALE_FORMULA, Key.FSharp_MAJOR);
	public static final Scale DSharp_MINOR = new Scale(Key.DSharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.DSharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.DSharp_MINOR);
	public static final Scale DSharp_HARMONIC_MINOR = new Scale("D#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.DSharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.DSharp_MINOR);

	public static final Scale CSharp_MAJOR = new Scale(Key.CSharp_MAJOR_NAME, MAJOR, ScaleType.DIATONIC, Pitch.CSharp, IScaleFormula.MAJOR_SCALE_FORMULA, Key.CSharp_MAJOR);
	public static final Scale ASharp_MINOR = new Scale(Key.ASharp_MINOR_NAME, MINOR, ScaleType.DIATONIC, Pitch.ASharp, IScaleFormula.MINOR_SCALE_FORMULA, Key.ASharp_MINOR);
	public static final Scale ASharp_HARMONIC_MINOR = new Scale("A#-HarmonicMinor", MINOR, ScaleType.DIATONIC, Pitch.ASharp, IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA, Key.ASharp_MINOR);

	/**
	 * Mapped non-Diatonic scales
	 */
	public static final Scale C_WHOLETONE = 
			new Scale("C-WholeTone", WHOLE_TONE, ScaleType.WHOLE_TONE, Pitch.C, IScaleFormula.WHOLE_TONE_SCALE_FORMULA, Key.C_MAJOR);
	public static final Scale GFlat_MAJOR_PENTATONIC = 
			new Scale("GFlat-Major-Pentatonic", PENTATONIC, ScaleType.PENTATONIC, Pitch.GFlat, IScaleFormula.PENTATONIC_MAJOR_SCALE_FORMULA, Key.DFlat_MAJOR);
	public static final Scale EFlat_MINOR_PENTATONIC = 
			new Scale("EFlat-Minor-Pentatonic", PENTATONIC, ScaleType.PENTATONIC, Pitch.EFlat, IScaleFormula.PENTATONIC_MINOR_SCALE_FORMULA, Key.BFlat_MINOR);
	public static final Scale BLUES_SCALE =
			new Scale("Blues in C", HEXATONIC, ScaleType.HEXATONIC, Pitch.C, IScaleFormula.BLUES_SCALE_FORMULA);
	public static final Scale HIRAJOSHI_SCALE =
			new Scale("Hirajoshi Japan in D", PENTATONIC, ScaleType.PENTATONIC, Pitch.D, IScaleFormula.HIRAJOSHI_SCALE_FORMULA);
	public static final Scale JEWISH_AHAVOH_RABBOH_SCALE =
			new Scale("Jewish Ahavoh-Rabboh in C", MODE, ScaleType.HEPTATONIC, Pitch.C, IScaleFormula.JEWISH_AHAVOH_RABBOH_SCALE_FORMULA);
	
	public static final Scale UNPITCHED_5_STEP_SCALE =
			new Scale("5-Line Unpitched Percussion", DISCRETE, ScaleType.DISCRETE_5LINE, Pitch.E, IScaleFormula.UNPITCHED_5_SCALE_FORMULA);
	public static final Scale UNPITCHED_4_STEP_SCALE =
			new Scale("4-Line Unpitched Percussion", DISCRETE, ScaleType.DISCRETE_4LINE, Pitch.E, IScaleFormula.UNPITCHED_4_SCALE_FORMULA);
	public static final Scale UNPITCHED_3_STEP_SCALE =
			new Scale("3-Line Unpitched Percussion", DISCRETE, ScaleType.DISCRETE_3LINE, Pitch.E, IScaleFormula.UNPITCHED_3_SCALE_FORMULA);
	public static final Scale UNPITCHED_2_STEP_SCALE =
			new Scale("2-Line Unpitched Percussion", DISCRETE, ScaleType.DISCRETE_2LINE, Pitch.E, IScaleFormula.UNPITCHED_2_SCALE_FORMULA);
	public static final Scale UNPITCHED_1_STEP_SCALE =
			new Scale("1-Line Unpitched Percussion", DISCRETE, ScaleType.DISCRETE_1LINE, Pitch.E, IScaleFormula.UNPITCHED_1_SCALE_FORMULA);
	
	static int[] chromaticSteps = {1,1,1,1,1,1,1,1,1,1,1};
	static int[] fullRangeChromaticSteps = new int[Pitch.C0.difference(Pitch.C9) ];		// 108 pitches
	static final ScaleFormula chromaticScaleFormula =  new ScaleFormula("12-Tone Chromatic", "chromatic", chromaticSteps);
	static ScaleFormula fullRangeChromaticScaleFormula = null;
	
	/**
	 * 12-note Chromatic scale stating at A, does not repeat the root 
	 */
	public static final Scale CHROMATIC_12TONE_SCALE =
			new Scale("Chromatic 12-tone", CHROMATIC_12TONE, ScaleType.CHROMATIC, Pitch.A, chromaticScaleFormula);
	
	/**
	 * Full range chromatic scale: C0 to C9 in Scientific Pitch Notation
	 */
	public static Scale FULL_RANGE_CHROMATIC_SCALE = null;
	
	static {
		for(int i=0; i<fullRangeChromaticSteps.length; i++) {
			fullRangeChromaticSteps[i] = 1;
		}
		fullRangeChromaticScaleFormula = new ScaleFormula("Full range Chromatic", "chromatic", fullRangeChromaticSteps);
		FULL_RANGE_CHROMATIC_SCALE = new Scale("Full Range Chromatic", CHROMATIC_12TONE, ScaleType.CHROMATIC, Pitch.C0, fullRangeChromaticScaleFormula);
	}
	
	/**
	 * Maps of chromatic step number (1 - 12) to scale degree (1 - 7 with accidental)
	 * There are four - for sharp keys (MAJOR and HARMONIC_MINOR) and flat keys
	 * Can be indexed directly by step number
	 */
	static String[] major_sharp_to_scale_degree = {
		"0", "1", "1#", "2", "2#", "3", "4", "4#", "5", "5#", "6", "6#", "7"
	};
	static String[] minor_sharp_to_scale_degree = {
		"0", "1", "1#", "2", "3", "3#", "4", "4#", "5", "6", "6#", "6##", "7"
	};
	static String[] major_flat_to_scale_degree = {
		"0", "1", "2b", "2", "3b", "3", "4", "5b", "5", "6b", "6", "7b", "7"
	};
	static String[] minor_flat_to_scale_degree = {
		"0", "1", "2b", "2", "3", "4b", "4", "5b", "5", "6", "7bb", "7b", "7"
	};
	

	public static String getScaleDegreeFromChromaticStep(Key key, int step) {
		String sd = null;
		Mode kmode = key.getMode();
		if(step >= 1 && step <= 12) {
			if(kmode.equals(Mode.MAJOR)) {
				if(key.isFlatKey()) {
					sd = major_flat_to_scale_degree[step];
				}
				else {
					sd = major_sharp_to_scale_degree[step];
				}
			}
			else if(kmode.equals(Mode.MINOR)) {
				if(key.isFlatKey()) {
					sd = minor_flat_to_scale_degree[step];
				}
				else {
					sd = minor_sharp_to_scale_degree[step];
				}
			}
		}
		return sd;
	}

	public static void addScaleToScaleMap(String name, Scale scale) {
		scaleMap.put(name, scale);
	}
	
	public static Scale getScale(String name) {
		return scaleMap.get(name);
	}
	
	public static Map<String, Scale> getScaleMap() {
		return scaleMap;
	}
}
