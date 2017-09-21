package music.element.song;

import java.util.List;

import music.element.IScaleFormula;

public interface IChordFormula extends IScaleFormula {
	
	public int getChordSize();
	public void setChordSize(int chordSize);
	
	public int getFormulaNumber();
	public void setFormulaNumber(int formulaNumber);
	
	public int getSpellingNumber();
	public void setSpellingNumber(int chordSpellingNumber);
	
	public List<String> getSymbols();
	public void setSymbols(List<String> symbols);
	
}
