package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.exceptions.InvalidAnnotationSpanException;
import uk.ac.warwick.dcs.SemEval.models.AnnotationSpan;
import uk.ac.warwick.dcs.SemEval.models.AnnotationType.AnnotationKind;

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
	
	@Test
	public void testBuildFromString() {
							//  01234567
		String annotationStr = "qqnnenpq";
		List<AnnotationSpan> computed = AnnotationSpan.createSpansFromString(annotationStr);
		AnnotationSpan test;
		
		assertTrue(computed.size() == 4);
		
		test = computed.get(0);
		assertTrue(test.getKind() == AnnotationKind.Negative);
		assertTrue(test.getStart() == 2);
		assertTrue(test.getEnd() == 3);
		
		test = computed.get(1);
		assertTrue(test.getKind() == AnnotationKind.Neutral);
		assertTrue(test.getStart() == 4);
		assertTrue(test.getEnd() == 4);
		
		test = computed.get(2);
		assertTrue(test.getKind() == AnnotationKind.Negative);
		assertTrue(test.getStart() == 5);
		assertTrue(test.getEnd() == 5);
		
		test = computed.get(3);
		assertTrue(test.getKind() == AnnotationKind.Positive);
		assertTrue(test.getStart() == 6);
		assertTrue(test.getEnd() == 6);
		
	}
	
}
