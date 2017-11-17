package music.element;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Label implements Serializable, Comparable<Label> {

	private static final long serialVersionUID = 3903761745238964639L;
	@JsonProperty("number")	private int number = -1;
	@JsonProperty("label")	private String label = null;
	
	public Label(int n, String l) {
		this.number = n;
		this.label = l;
	}
	public Label(Number n, String l) {
		this.label = l;
		number = n.intValue();
	}

	public int getNumber() {
		return number;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * Format is number+label.
	 * For example, if number = 10, label = "A"
	 * toString() is "10A"
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if(number > 0) {
			sb.append(number);
		}
		if(label != null) {
			sb.append(label);
		}
		return sb.toString();
	}

	@Override
	public int compareTo(Label other) {
		return toString().compareTo(other.toString());
	}
	
	public boolean equals(Label other) {
		return toString().equals(other.toString());
	}
}
