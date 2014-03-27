package uk.ac.warwick.dcs.SemEval;

import uk.ac.warwick.dcs.SemEval.exceptions.InvalidAnnotationSpanException;

public class Tweet {

	protected AnnotationMap annotations;
	private String text;
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
		return this.getText();
	}
	
	public AnnotationMap getAnnotations() {
		return this.annotations.clone();
	}
}
