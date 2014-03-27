package uk.ac.warwick.dcs.SemEval;

import uk.ac.warwick.dcs.SemEval.exceptions.InvalidAnnotationSpanException;

public class Tweet {

	protected AnnotationMap annotations;
	protected String text;
	private int maxLength;
	
	public Tweet(String text) {
		String[] arr;
		
		this.text = text;
		arr = this.text.split(" ");
		this.annotations = new AnnotationMap(AnnotationMap.DuplicationStrategy.Replace);
		this.maxLength = arr.length;
	}
	
	public Tweet(String text, AnnotationMap m) {
		this(text);
		this.annotations = m;
	}
	
	public void addAnnotation(AnnotationSpan s) throws InvalidAnnotationSpanException {
		if (s.getEnd() > this.maxLength) {
			throw new InvalidAnnotationSpanException("Longer than the tweet", this.maxLength, s.getEnd());
		}
		this.annotations.put(s);
	}
	
	public String getText() {
		return this.text;
	}
	
	public AnnotationMap getAnnotations() {
		return this.annotations.clone();
	}
	
	public void clearAnnotations() {
		this.annotations.clear();
	}
	
	public boolean equal(Tweet other) {
		if (this.getText().equals(other.getText())) {
			if (this.annotations.equal(other.getAnnotations())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean subjEqual(Tweet other) {
		if (this.getText().equals(other.getText())) {
			if (this.annotations.subjEqual(other.getAnnotations())) {
				return true;
			}
		}
		return false;
	}
}
