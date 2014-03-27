package uk.ac.warwick.dcs.SemEval;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;
import junit.framework.TestCase;

public class AnnotationTypeTest extends TestCase {

	@Test
	public void testSubjectiveExpectTrue() {
		AnnotationType t = new AnnotationType(AnnotationKind.Negative);
		assert(t.isSubjective());
		t = new AnnotationType(AnnotationKind.Positive);
		assert(t.isSubjective());
		t = new AnnotationType(AnnotationKind.Neutral);
		assert(t.isSubjective());
	}
	
	@Test
	public void testSubjectiveExpectFalse() {
		AnnotationType t = new AnnotationType(AnnotationKind.Objective);
		assert(!t.isSubjective());
	}
	
}
