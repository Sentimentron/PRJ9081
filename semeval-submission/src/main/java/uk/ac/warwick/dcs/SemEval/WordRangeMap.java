package uk.ac.warwick.dcs.SemEval;

import java.util.TreeMap;
import java.util.Map;

import uk.ac.warwick.dcs.SemEval.exceptions.WordRangeMapException;

public class WordRangeMap {

	private Map<Integer, Integer> wordStartCharOffsetToWord;
	private Map<Integer, Integer> wordEndCharOffsetToWord;
	private Map<Integer, String> wordOffsetMap;
	
	private int currentCharOffset = 0;
	private int currentWordOffset = 0;
	
	public WordRangeMap() {
		this.wordEndCharOffsetToWord = new TreeMap<Integer, Integer>();
		this.wordStartCharOffsetToWord = new TreeMap<Integer, Integer>();
		this.wordOffsetMap = new TreeMap<Integer, String>();
	}
	
	public WordRangeMap(String text) {
		this();
		this.addString(text);
	}

	public void addWord(String s) {
		int wordLength = s.length();
		this.wordOffsetMap.put(this.currentWordOffset, s);
		this.wordStartCharOffsetToWord.put(this.currentCharOffset, this.currentWordOffset);
		this.currentCharOffset += wordLength;
		this.wordEndCharOffsetToWord.put(this.currentCharOffset, this.currentWordOffset);
		this.currentCharOffset++;
		this.currentWordOffset++;
	}
	
	public void addString(String s) {
		for (String w : s.split("[ ,]")) {
			this.addWord(w);
		}
	}
	
	public int getWordStartOffset(int charOffsetStart) throws WordRangeMapException {
		int wordStart = -1;
		for (Map.Entry<Integer, Integer> e: this.wordStartCharOffsetToWord.entrySet()) {
			if (e.getKey() <= charOffsetStart) {
				wordStart = e.getValue();
			}
		}
		if (wordStart == -1) {
			throw new WordRangeMapException("Char offset never found!");
		}
		return wordStart;
	}
	
	public int getWordEndOffset(int charOffsetEnd) throws WordRangeMapException {
		int wordEnd = -1;
		for (Map.Entry<Integer, Integer> e: this.wordStartCharOffsetToWord.entrySet()) {
			if (e.getKey() <= charOffsetEnd) {
				wordEnd = e.getValue();
			}
		}
		if (wordEnd == -1) {
			throw new WordRangeMapException("Char offset never found!");
		}
		return wordEnd;
	}
}
