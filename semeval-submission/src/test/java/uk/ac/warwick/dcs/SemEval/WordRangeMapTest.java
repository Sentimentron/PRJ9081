/**
 * 
 */
package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.exceptions.WordRangeMapException;

/**
 * @author cs407
 *
 */
public class WordRangeMapTest {

	/**
	 * Test method for {@link uk.ac.warwick.dcs.SemEval.WordRangeMap#addWord(java.lang.String)}.
	 * @throws WordRangeMapException 
	 */
	@Test
	public void testAddWord() throws WordRangeMapException {
		WordRangeMap wm = new WordRangeMap();
		wm.addString("hello");
		wm.addString("world");
		wm.addWord("how");
		wm.addString("are you?");
		
		assertEquals(wm.getWordStartOffset(0), 1); // 'h'
		assertEquals(wm.getWordStartOffset(1), 1); // e
		assertEquals(wm.getWordStartOffset(2), 1); // l
		assertEquals(wm.getWordStartOffset(3), 1); // l
		assertEquals(wm.getWordStartOffset(4), 1); // o
		assertEquals(wm.getWordStartOffset(6), 2); // w
		assertEquals(wm.getWordStartOffset(7), 2); // o
		assertEquals(wm.getWordStartOffset(8), 2); // r
		assertEquals(wm.getWordStartOffset(9), 2); // l
		assertEquals(wm.getWordStartOffset(10),2); // d
		assertEquals(wm.getWordStartOffset(12), 3); // h
		assertEquals(wm.getWordStartOffset(13), 3); // o
		assertEquals(wm.getWordStartOffset(14), 3); // w
		assertEquals(wm.getWordStartOffset(16), 4); // a
		assertEquals(wm.getWordStartOffset(17), 4); // r
		assertEquals(wm.getWordStartOffset(18), 4); // e
		assertEquals(wm.getWordStartOffset(20), 5); // y
		assertEquals(wm.getWordStartOffset(21), 5); // o
		assertEquals(wm.getWordStartOffset(22), 5); // u

		assertEquals(wm.getWordEndOffset(0), 1); // 'h'
		assertEquals(wm.getWordEndOffset(1), 1); // e
		assertEquals(wm.getWordEndOffset(2), 1); // l
		assertEquals(wm.getWordEndOffset(3), 1); // l
		assertEquals(wm.getWordEndOffset(4), 1); // o
		assertEquals(wm.getWordEndOffset(6), 2); // w
		assertEquals(wm.getWordEndOffset(7), 2); // o
		assertEquals(wm.getWordEndOffset(8), 2); // r
		assertEquals(wm.getWordEndOffset(9), 2); // l
		assertEquals(wm.getWordEndOffset(10),2); // d
		assertEquals(wm.getWordEndOffset(12), 3); // h
		assertEquals(wm.getWordEndOffset(13), 3); // o
		assertEquals(wm.getWordEndOffset(14), 3); // w
		assertEquals(wm.getWordEndOffset(16), 4); // a
		assertEquals(wm.getWordEndOffset(17), 4); // r
		assertEquals(wm.getWordEndOffset(18), 4); // e
		assertEquals(wm.getWordEndOffset(20), 5); // y
		assertEquals(wm.getWordEndOffset(21), 5); // o
		assertEquals(wm.getWordEndOffset(22), 5); // u
		
	}

}
