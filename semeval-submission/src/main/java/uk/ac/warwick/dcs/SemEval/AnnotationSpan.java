package uk.ac.warwick.dcs.SemEval;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.warwick.dcs.SemEval.exceptions.InvalidAnnotationSpanException;

public class AnnotationSpan extends AnnotationType {

	int startOffset;
	int endOffset;
	
	public AnnotationSpan clone() {
		return new AnnotationSpan(this.getKind(), this.startOffset, this.endOffset);
	}
	
	public AnnotationSpan(AnnotationKind k, int start, int end) throws InvalidAnnotationSpanException {
		super(k);
		this.startOffset = start;
		this.endOffset = end;
		if(!this.validate()) {
			throw new InvalidAnnotationSpanException("Invalid range", start, end);
		}
	}

	public boolean validate() {
		int start = this.startOffset;
		int end   = this.endOffset;
		return AnnotationSpan.isValid(start, end);
	}
	
	public static boolean isValid(int start, int end) {
		if (start > end) {
			return false;
		}
		if (start < 0) return false;
		if (end < 0) return false;
		return true;
	}
	
	public int getStart() {
		return this.startOffset;
	}
	
	public int getEnd() {
		return this.endOffset;
	}
	
	public AnnotationKind getKind() {
		return this.type;
	}

	public static List<AnnotationSpan> createSpansFromString(
			String annotationStr) {
		
		List<AnnotationSpan> ret = new ArrayList<AnnotationSpan>();
		
		char lastSeen = 0;
		int firstSeen = 0;
		int counter = 0;
		
		Matcher subjectiveMatcher = Pattern.compile("([p]+|[n]+|[e]+)").matcher(annotationStr);
		while(subjectiveMatcher.find()) {
			int startCharOffset = subjectiveMatcher.start();
			int groupCount      = subjectiveMatcher.groupCount();
			for (int i = 1; i <= groupCount; i++) {
				String group = subjectiveMatcher.group(i);
				char annotationChar = group.charAt(0);
				AnnotationKind t = AnnotationType.fromChar(annotationChar);
				ret.add(new AnnotationSpan(t, startCharOffset, group.length() + startCharOffset - 1));
				startCharOffset += group.length();
			}
		}
		
		/*for (char s : annotationStr.toCharArray()) {
			if (s != lastSeen) {
				if (lastSeen != 0) {
					AnnotationSpan appendSpan = new AnnotationSpan(
							AnnotationType.fromChar(s), 
							firstSeen, counter-1
						);
					ret.add(appendSpan);
				}
				firstSeen = counter;
				lastSeen  = s;
			}
			counter++;
		}*/
		
		return ret;
	}
	
}
