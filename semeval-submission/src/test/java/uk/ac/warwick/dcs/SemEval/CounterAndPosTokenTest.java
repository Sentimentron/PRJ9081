package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class CounterAndPosTokenTest {

	@Test
	public void basicCountingTest() {
		Counter<String> c = new Counter<String>();
		c.put("gregory");
		c.put("gorgeous");
		assertEquals((int)c.get("gorgeous"), 1);
		c.put("shmuck");
		assertEquals((int)c.get("shmuck"), 1);
		assertEquals((int)c.put("gorgeous"), 2);
		assertEquals((int)c.get("gorgeous"), 2);
		assertEquals((int)c.get("oddly"), 0);
	}
	
	@Test
	public void posCountingTest() {
		Counter<POSToken> c = new Counter<POSToken>();
		
		POSToken tag1a = new POSToken(0.1, 4, 5, 1, 1, ",", ",");
		POSToken tag1b = new POSToken(0.2, 4, 5, 1, 1, ",", ",");
		POSToken tag2  = new POSToken(0.4, 1, 3, 1, 1, "!", "hi!");
		POSToken tag3  = new POSToken(0.3, 1, 1, 1, 1, "", "STOPPED");
		
		assertEquals((int)c.put(tag1a), 1);
		assertEquals((int)c.get(tag1a), 1);
		assertEquals((int)c.put(tag1b), 2);
		assertEquals((int)c.get(tag1a), 2);
		assertEquals((int)c.get(tag1b), 2);
		assertEquals((int)c.put(tag1a), 3);
		assertEquals((int)c.get(tag1a), 3);
		assertEquals((int)c.put(tag2), 1);
		assertEquals((int)c.get(tag2), 1);
		assertEquals((int)c.get(tag3), 0);
	}
	
	@Test
	public void posAssignmentCheck() {
		POSToken t = new POSToken(0.1, 4, 5, 1, 1, ",", ",");
		assertEquals(t.setAnnotation(new AnnotationType(AnnotationKind.Positive)), true);
		assertEquals(t.setAnnotation(new AnnotationType(AnnotationKind.Positive)), true);
		assertEquals(t.setAnnotation(new AnnotationType(AnnotationKind.Negative)), false);
	}

}
