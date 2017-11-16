package music.element.song;

import java.util.List;

import music.element.IFormula;

public interface IChordFormula extends IFormula {
	
	public int getChordSize();
	public void setChordSize(int chordSize);
	
	public int getFormulaNumber();
	public void setFormulaNumber(int formulaNumber);
	
	public int getSpellingNumber();
	public void setSpellingNumber(int chordSpellingNumber);
	
	public List<String> getSymbols();
	public void setSymbols(List<String> symbols);
	
}
