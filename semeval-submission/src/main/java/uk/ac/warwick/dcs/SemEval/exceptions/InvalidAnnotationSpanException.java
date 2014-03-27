package uk.ac.warwick.dcs.SemEval.exceptions;

public class InvalidAnnotationSpanException extends Exception {

	private static final long serialVersionUID = -2510510073843636755L;
	private int startOff;
	private int endOff;
	
	public InvalidAnnotationSpanException(String message, int start, int end) {
		super(message);
		this.startOff = start;
		this.endOff = end;
	}

	public String toString() {
		return String.format("InvalidAnnotationSpanException(%s, %d, %d)", this.getMessage(), this.startOff, this.endOff);
	}
	
}
