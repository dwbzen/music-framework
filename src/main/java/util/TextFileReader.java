package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateless text file reader.
 * 
 * @author don_bacon
 *
 */
public class TextFileReader {
	public final static Character SPACE = ' ';
	private List<String> lines = new ArrayList<String>();
	private Character delimiter = null;
	private String endOfLine = null;
	private String fileName = null;
	
	private static Character delim = SPACE;
	private static String eolString = "";
	
	protected TextFileReader(String inputFile, Character cdelim, String eol) {
		setDelimiter(cdelim);
		setEndOfLine(eol);
		setFileName(inputFile);
	}
	
	public static TextFileReader getInstance(String inputFile, Character cdelim, String eol) {
		return new TextFileReader(inputFile, cdelim, eol);
	}
	
	public static TextFileReader getInstance(String inputFile, String eol) {
		return new TextFileReader(inputFile, delim, eol);
	}
	public static TextFileReader getInstance(String inputFile) {
		return new TextFileReader(inputFile, delim, eolString);
	}

	/**
	 * Gets the contents of a text file as a single String.
	 * Appends the set delim Character and eolString (if not null) after trimming the line.
	 * @param inputFile the file to read. If null, reads from STDIN.
	 * @return String text of the entire file, lines separated by configured delimiter (normally SPACE)
	 * 
	 * @throws IOException
	 */
	public String getFileText() throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader inputFileReader = (fileName != null) ?
					new BufferedReader(new FileReader(fileName)) :
					new BufferedReader(new InputStreamReader(System.in));
		String line;
		while((line = inputFileReader.readLine()) != null) {
			if(line.length()>0) { 
				sb.append(line.trim());
				lines.add(line);
				sb.append(delimiter);
				if(endOfLine != null) {
					sb.append(endOfLine);
				}
			}
		}
		inputFileReader.close();
		return sb.toString();
	}
	
	/**
	 * Gets the contents of a file as a List<String>  where each List element is a physical line in the file.
	 * Appends the set delim Character and eolString (if not null) after trimming the line.
	 * @param inputFile
	 * @return
	 * @throws IOException
	 */
	public List<String> getFileLines() throws IOException {
		if(lines.size() == 0) {
			BufferedReader inputFileReader = (fileName != null) ?
					new BufferedReader(new FileReader(fileName)) :
					new BufferedReader(new InputStreamReader(System.in));
			String line;
			while((line = inputFileReader.readLine()) != null) {
				if(line.length()>0) {
					StringBuffer sb = new StringBuffer(line.trim());
					sb.append(delimiter);
					if(endOfLine != null) {
						sb.append(endOfLine);
					}
					lines.add(sb.toString()); 
				}
			}
			inputFileReader.close();
		}
		return lines;
	}
	
	public static void main(String... args) throws IOException {
		String filename = args[0];
		TextFileReader reader = TextFileReader.getInstance(filename);
		String text = reader.getFileText();
		System.out.println(text);
		System.out.println("length: " + text.length());
		
		TextFileReader reader2 = TextFileReader.getInstance(filename, "\n");
		text = reader2.getFileText();
		System.out.println(text);
		System.out.println("length: " + text.length());

		List<String> lines = reader.getFileLines();
		System.out.println("#lines: " + reader2.size());
		int lnum = 1;
		for(String line : lines) {
			System.out.println(lnum + ". " + line);
			lnum++;
		}
	}

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
	}

	public Character getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(Character delimiter) {
		this.delimiter = delimiter;
	}

	public String getEndOfLine() {
		return endOfLine;
	}

	public void setEndOfLine(String endOfLine) {
		this.endOfLine = endOfLine;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public int size() {
		return lines.size();
	}
}
