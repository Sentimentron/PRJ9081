package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class TweetTests {

	@Test
	public void testEquality() {
		Tweet t1 = new Tweet("Barclays Profit Driven by Investment Banking: LONDON--Barclays PLC (BCS) Wednesday continued to reap the rewards... http://bit.ly/W5t0au ");
		Tweet t2 = new Tweet("Barclays Profit Driven by Investment Banking: LONDON--Barclays PLC (BCS) Wednesday continued to reap the rewards... http://bit.ly/W5t0au ");

		assertTrue(t1.equal(t2));
		
		t1.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 1, 3));
		assertFalse(t1.equal(t2));
		
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 1, 1));
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 2, 2));
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 3, 3));
		assertTrue(t1.equal(t2));
		
		t1.addAnnotation(new AnnotationSpan(AnnotationKind.Negative, 4, 4));
		t2.addAnnotation(new AnnotationSpan(AnnotationKind.Positive, 4, 4));
		assertFalse(t1.equal(t2));
		assertTrue(t1.subjEqual(t2));
	}

}
