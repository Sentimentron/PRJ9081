package uk.ac.warwick.dcs.SemEval;
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
		return this.isValid(start, end);
	}
	
	public static boolean isValid(int start, int end) {
		if (start > end) {
			return false;
		}
		if (start <= 0) return false;
		if (end <= 0) return false;
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
	
}
