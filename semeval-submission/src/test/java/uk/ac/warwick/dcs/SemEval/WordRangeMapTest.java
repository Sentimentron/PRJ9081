/**
 * 
 */
package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.*;

import org.junit.Test;

import uk.ac.warwick.dcs.SemEval.exceptions.WordRangeMapException;
import uk.ac.warwick.dcs.SemEval.utils.WordRangeMap;

/**
 * @author cs407
 *
 */
public class WordRangeMapTest {

	/**
	 * Test method for {@link uk.ac.warwick.dcs.SemEval.utils.WordRangeMap#addWord(java.lang.String)}.
	 * @throws WordRangeMapException 
	 */
	@Test
	public void testAddWord() throws WordRangeMapException {
		WordRangeMap wm = new WordRangeMap();
		wm.addString("hello");
		wm.addString("world");
		wm.addWord("how");
		wm.addString("are you?");
		
		assertEquals(wm.getWordStartOffset(0), 0); // 'h'
		assertEquals(wm.getWordStartOffset(1), 0); // e
		assertEquals(wm.getWordStartOffset(2), 0); // l
		assertEquals(wm.getWordStartOffset(3), 0); // l
		assertEquals(wm.getWordStartOffset(4), 0); // o
		assertEquals(wm.getWordStartOffset(6), 1); // w
		assertEquals(wm.getWordStartOffset(7), 1); // o
		assertEquals(wm.getWordStartOffset(8), 1); // r
		assertEquals(wm.getWordStartOffset(9), 1); // l
		assertEquals(wm.getWordStartOffset(10),1); // d
		assertEquals(wm.getWordStartOffset(12), 2); // h
		assertEquals(wm.getWordStartOffset(13), 2); // o
		assertEquals(wm.getWordStartOffset(14), 2); // w
		assertEquals(wm.getWordStartOffset(16), 3); // a
		assertEquals(wm.getWordStartOffset(17), 3); // r
		assertEquals(wm.getWordStartOffset(18), 3); // e
		assertEquals(wm.getWordStartOffset(20), 4); // y
		assertEquals(wm.getWordStartOffset(21), 4); // o
		assertEquals(wm.getWordStartOffset(22), 4); // u

		assertEquals(wm.getWordEndOffset(0), 0); // 'h'
		assertEquals(wm.getWordEndOffset(1), 0); // e
		assertEquals(wm.getWordEndOffset(2), 0); // l
		assertEquals(wm.getWordEndOffset(3), 0); // l
		assertEquals(wm.getWordEndOffset(4), 0); // o
		assertEquals(wm.getWordEndOffset(6), 1); // w
		assertEquals(wm.getWordEndOffset(7), 1); // o
		assertEquals(wm.getWordEndOffset(8), 1); // r
		assertEquals(wm.getWordEndOffset(9), 1); // l
		assertEquals(wm.getWordEndOffset(10),1); // d
		assertEquals(wm.getWordEndOffset(12), 2); // h
		assertEquals(wm.getWordEndOffset(13), 2); // o
		assertEquals(wm.getWordEndOffset(14), 2); // w
		assertEquals(wm.getWordEndOffset(16), 3); // a
		assertEquals(wm.getWordEndOffset(17), 3); // r
		assertEquals(wm.getWordEndOffset(18), 3); // e
		assertEquals(wm.getWordEndOffset(20), 4); // y
		assertEquals(wm.getWordEndOffset(21), 4); // o
		assertEquals(wm.getWordEndOffset(22), 4); // u
		
	}

}
