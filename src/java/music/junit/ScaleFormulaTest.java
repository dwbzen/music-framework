package music.junit;

import java.util.List;

import music.element.IScaleFormula;
import music.element.ScaleFormula;

public class ScaleFormulaTest {
	public static void main(String... args) {
		System.out.println(IScaleFormula.CHROMATIC_SCALE_FORMULA.toJSON());
		
		ScaleFormula sf =IScaleFormula.MAJOR_SCALE_FORMULA;
		sf.addGroup("mode");
		sf.getAlternateNames().add("Ionian mode");
		System.out.println(sf.toJSON());
		
		System.out.println(IScaleFormula.HARMONIC_MINOR_SCALE_FORMULA.toJSON());
		
		sf = IScaleFormula.MELODIC_MINOR_ASCENDING_SCALE_FORMULA;
		sf.getAlternateNames().add("Melodic minor ascending");
		System.out.println(IScaleFormula.MELODIC_MINOR_ASCENDING_SCALE_FORMULA.toJSON());
		
		sf = IScaleFormula.MINOR_SCALE_FORMULA;
		sf.getAlternateNames().add("Melodic minor descending");
		sf.getAlternateNames().add("Natural minor");
		sf.getAlternateNames().add("Aeolian mode");
		sf.addGroup("mode");
		System.out.println(sf.toJSON());
		
		System.out.println(IScaleFormula.PENTATONIC_MAJOR_SCALE_FORMULA.toJSON());
		System.out.println(IScaleFormula.PENTATONIC_MINOR_SCALE_FORMULA.toJSON());
		System.out.println(IScaleFormula.WHOLE_TONE_SCALE_FORMULA.toJSON());
		System.out.println(IScaleFormula.BLUES_SCALE_FORMULA.toJSON());
		
		
		int[] pitchSet = {0, 1, 3, 4, 6, 7, 9, 10};
		List<Integer>  formula1 = ScaleFormula.pitchSetToFormula(pitchSet);
		System.out.println("[0, 1, 3, 4, 6, 7, 9, 10] == " + formula1);
		
		int[] pitchSet2 = {0, 2, 4, 5, 7, 9, 11};
		List<Integer>  formula2 = ScaleFormula.pitchSetToFormula(pitchSet2);
		System.out.println("[0, 2, 4, 5, 7, 9, 11]  == " +formula2);
		
		System.out.println(formula1 + " == " + ScaleFormula.formulaToPitchSet(formula1));
		System.out.println(formula2 + " == " + ScaleFormula.formulaToPitchSet(formula2));
	}


}
