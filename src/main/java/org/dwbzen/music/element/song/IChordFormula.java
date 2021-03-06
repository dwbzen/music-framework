package org.dwbzen.music.element.song;

import java.util.List;

import org.dwbzen.music.element.IFormula;

public interface IChordFormula extends IFormula {
	
	public int getChordSize();
	public void setChordSize(int chordSize);
	
	public int getFormulaNumber();
	public void setFormulaNumber(int formulaNumber);
	
	public int getSpellingNumber();
	public void setSpellingNumber(int chordSpellingNumber);
	
	public List<String> getSymbols();
	
}
