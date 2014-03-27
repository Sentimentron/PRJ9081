package uk.ac.warwick.dcs.SemEval;
import uk.ac.warwick.dcs.SemEval.exceptions.InvalidAnnotationSpanException;

public class AnnotationSpan extends AnnotationType {

	int startOffset;
	int endOffset;
	
	public AnnotationSpan(AnnotationKind k, int start, int end) throws InvalidAnnotationSpanException {
		super(k);
		this.startOffset = start;
		this.endOffset = end;
		if(!this.validate()) {
			throw new InvalidAnnotationSpanException("Invalid range", start, end);
		}
	}

	private boolean validate() {
		int start = this.startOffset;
		int end   = this.endOffset;
		if (start >= end) {
			return false;
		}
		if (start <= 0) return false;
		if (end <= 0) return false;
		return true;
	}
		
}
