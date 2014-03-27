package uk.ac.warwick.dcs.SemEval;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;
import uk.ac.warwick.dcs.SemEval.exceptions.WordRangeMapException;
import cmu.arktweetnlp.RawTagger;

public class POSToken implements Comparable<POSToken> {
	
	public final double posConfidence; // Confidence in POS assignment
	public final int startCharOffset;	  // Start character offset in tweet
	public final int endCharOffset;        // End character offset in tweet
	public final String tag;
	public final String token;
	public final int endWordOffset;
	public final int startWordOffset;

	private AnnotationType annotation = null;
	
	public POSToken(RawTagger.TaggedToken r, WordRangeMap wm) throws WordRangeMapException {
		this.posConfidence = r.confidence;
		this.startCharOffset = r.span.first;
		this.endCharOffset   = r.span.second;
		this.tag = r.tag;
		this.token = r.token;
		this.startWordOffset = wm.getWordStartOffset(this.startCharOffset);
		this.endWordOffset   = wm.getWordEndOffset(endCharOffset);
	}
	
	public POSToken(double confidence, int startCharOffset, int endCharOffset,
					int startWordOffset, int endWordOffset, String tag, String token) {
		this.posConfidence = confidence;
		this.startCharOffset = startCharOffset;
		this.endCharOffset = endCharOffset;
		this.startWordOffset = startWordOffset;
		this.endWordOffset = endWordOffset;
		this.tag = tag;
		this.token = token;
	}
	
	public POSToken(double confidence, int startCharOffset, int endCharOffset,
			int startWordOffset, int endWordOffset, String tag, String token,
			AnnotationType an) {
		this(confidence, startCharOffset, endCharOffset, startWordOffset, endWordOffset, tag, token);
		this.annotation = an;
		}	
	
	public POSToken clone() {
		return new POSToken(this.posConfidence, this.startCharOffset, this.endCharOffset,
							this.startWordOffset, this.endWordOffset, this.tag, this.token, this.annotation);
	}

	@Override
	public int compareTo(POSToken o) {
		int comparison = this.tag.compareTo(o.tag);
		comparison += this.token.compareTo(o.token);
		return comparison;
	}
	
	public boolean setAnnotation(AnnotationType t) throws Exception {
		boolean ret = true;
		if (t == null) {
			throw new Exception("Cannot be null");
		}
		if (this.annotation != null) {
			if (!this.annotation.equals(t)) {
				System.err.println("Warning: setAnnotation already called with something different!");
				ret = false;
			}
		}
		this.annotation = t;
		return ret;
	}
	
	public AnnotationType getAnnotation() {
		if (this.annotation == null) {
			return new AnnotationType(AnnotationKind.Objective);
		}
		return this.annotation;
	}
	
}
