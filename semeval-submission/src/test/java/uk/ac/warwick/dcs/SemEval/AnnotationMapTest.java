/**
 * 
 */
package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.AnnotationMap.DuplicationStrategy;
import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

/**
 * @author cs407
 *
 */
public class AnnotationMapTest {
	
	AnnotationMap map;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.map = new AnnotationMap(DuplicationStrategy.Replace);
	}

	/**
	 * Test method for {@link uk.ac.warwick.dcs.SemEval.AnnotationMap#removeSliceShiftRightToLeft()}.
	 */
	@Test
	public void testSliceRemove() {
		this.testRegenerate();
		this.map.removeSliceShiftRightToLeft(5);
		for (Entry<Integer, AnnotationType>e : this.map.entrySet()) {
			AnnotationType t = e.getValue();
			int p = e.getKey();
			if (t.getKind() == AnnotationKind.Neutral) {
				if (p != 6) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else if (t.getKind() == AnnotationKind.Positive) {
				if (p!= 5) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else if (t.getKind() == AnnotationKind.Negative) {
				if (p != 1 && p != 2 && p != 7 && p != 8 && p != 9 && p != 10 && p != 11) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
		}
	}
	
	/**
	 * Test method for {@link uk.ac.warwick.dcs.SemEval.AnnotationMap#insertSlice()}.
	 */
	@Test
	public void testInsertSlice() {
		AnnotationSpan a1 = new AnnotationSpan(AnnotationKind.Negative, 1, 2);
		AnnotationSpan a2 = new AnnotationSpan(AnnotationKind.Positive, 5, 6);
		AnnotationSpan a3 = new AnnotationSpan(AnnotationKind.Neutral, 7, 10);
		AnnotationSpan a4 = new AnnotationSpan(AnnotationKind.Negative, 8, 12);
		
		this.map.put(a1);
		this.map.put(a2);
		this.map.put(a3);
		this.map.put(a4);
		
		this.map.insertSlice(8);
		
		for (Entry<Integer, AnnotationType>e : this.map.entrySet()) {
			AnnotationType t = e.getValue();
			int p = e.getKey();
			if (t.getKind() == AnnotationKind.Neutral) {
				if (p != 7) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else if (t.getKind() == AnnotationKind.Positive) {
				if (p!= 5 && p != 6) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else if (t.getKind() == AnnotationKind.Negative) {
				if (p != 1 && p != 2 && p != 8 && p != 9 && p != 10 && p != 11 && p != 12 && p != 13) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else {
				assert(t.getKind() == AnnotationKind.Objective);
			}
		}
	}
	
	/**
	 * Test method for {@link uk.ac.warwick.dcs.SemEval.AnnotationMap#regenerate()}.
	 */
	@Test
	public void testRegenerate() {
		AnnotationSpan a1 = new AnnotationSpan(AnnotationKind.Negative, 1, 2);
		AnnotationSpan a2 = new AnnotationSpan(AnnotationKind.Positive, 5, 6);
		AnnotationSpan a3 = new AnnotationSpan(AnnotationKind.Neutral, 7, 10);
		AnnotationSpan a4 = new AnnotationSpan(AnnotationKind.Negative, 8, 12);
		
		this.map.put(a1);
		this.map.put(a2);
		this.map.put(a3);
		this.map.put(a4);
		
		for (Entry<Integer, AnnotationType>e : this.map.entrySet()) {
			AnnotationType t = e.getValue();
			int p = e.getKey();
			if (t.getKind() == AnnotationKind.Neutral) {
				if (p != 7) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else if (t.getKind() == AnnotationKind.Positive) {
				if (p!= 5 && p != 6) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else if (t.getKind() == AnnotationKind.Negative) {
				if (p != 1 && p != 2 && p != 8 && p != 9 && p != 10 && p != 11 && p != 12) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else {
				assert(t.getKind() == AnnotationKind.Objective);
			}
		}
	}

	/**
	 * Test method for {@link uk.ac.warwick.dcs.SemEval.AnnotationMap#remove(uk.ac.warwick.dcs.SemEval.AnnotationSpan)}.
	 */
	@Test
	public void testRemoveAnnotationSpan() {
		this.testRegenerate();
		AnnotationSpan a5 = new AnnotationSpan(AnnotationKind.Negative, 1, 12);
		this.map.put(a5);
		for (Entry<Integer, AnnotationType>e : this.map.entrySet()) {
			AnnotationType t = e.getValue();
			int p = e.getKey();
			if (t.getKind() != AnnotationKind.Negative) {
				fail("Wrong type: replacing in the wrong order");
			}
		}
		this.map.remove(a5);
		for (Entry<Integer, AnnotationType>e : this.map.entrySet()) {
			AnnotationType t = e.getValue();
			int p = e.getKey();
			if (t.getKind() == AnnotationKind.Neutral) {
				if (p != 7) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else if (t.getKind() == AnnotationKind.Positive) {
				if (p!= 5 && p != 6) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else if (t.getKind() == AnnotationKind.Negative) {
				if (p != 1 && p != 2 && p != 8 && p != 9 && p != 10 && p != 11 && p != 12) {
					fail(String.format("%d, %s (unexpected)", p, t));
				}
			}
			else {
				assert(t.getKind() == AnnotationKind.Objective);
			}
		}
	}

}
