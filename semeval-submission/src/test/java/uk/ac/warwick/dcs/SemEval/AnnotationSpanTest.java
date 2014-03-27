package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;
import uk.ac.warwick.dcs.SemEval.exceptions.InvalidAnnotationSpanException;

public class AnnotationSpanTest {

	@Test
	public void createValidAnnotationSpan() {
		try {
			AnnotationSpan s = new AnnotationSpan(AnnotationKind.Negative, 1, 3);
		} catch (InvalidAnnotationSpanException e) {
			fail(e.toString());
		}
	}

	@Test
	public void createInvalidAnnotationSpan() {
		try {
			AnnotationSpan s = new AnnotationSpan(AnnotationKind.Negative, 3, 1);
		} catch (InvalidAnnotationSpanException e) {
			return;
		}
		fail("Expected exception!");
	}
	
	@Test
	public void testSubjectiveExpectTrue() throws InvalidAnnotationSpanException {
		AnnotationSpan t = new AnnotationSpan(AnnotationKind.Negative, 1, 3);
		assert(t.isSubjective());
		t = new AnnotationSpan(AnnotationKind.Positive, 1, 3);
		assert(t.isSubjective());
		t = new AnnotationSpan(AnnotationKind.Neutral, 1, 3);
		assert(t.isSubjective());
	}
	
	@Test
	public void testSubjectiveExpectFalse() throws InvalidAnnotationSpanException {
		AnnotationSpan t = new AnnotationSpan(AnnotationKind.Negative, 1, 3);
		assert(!t.isSubjective());
	}
	
}
