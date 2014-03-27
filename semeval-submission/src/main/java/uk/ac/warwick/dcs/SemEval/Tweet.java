package uk.ac.warwick.dcs.SemEval;

import uk.ac.warwick.dcs.SemEval.exceptions.InvalidAnnotationSpanException;

public class Tweet {

	private AnnotationMap annotations;
	private String text;
	private int maxLength;
	
	public Tweet(String text) {
		String[] arr;
		
		this.text = text;
		arr = this.text.split(" ");
		this.annotations = new AnnotationMap(AnnotationMap.DuplicationStrategy.Replace);
		this.maxLength = arr.length;
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
}
