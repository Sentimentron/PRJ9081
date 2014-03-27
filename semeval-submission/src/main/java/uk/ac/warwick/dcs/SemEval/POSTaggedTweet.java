package uk.ac.warwick.dcs.SemEval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import uk.ac.warwick.dcs.SemEval.exceptions.WordRangeMapException;
import cmu.arktweetnlp.RawTagger;
import cmu.arktweetnlp.RawTwokenize;

public class POSTaggedTweet extends Tweet {

	private List<RawTagger.TaggedToken> tokens;
	
	public POSTaggedTweet(Tweet t, RawTagger tagger) throws WordRangeMapException {
		super(t.getText(), t.getAnnotations());
		
		String rawText = t.getText();
		String textAfterLeftFiltering;
		String textReadyToTokenize;
		
		// First: some spans will correspond to extra spaces
		// removed by the pos tagger 
		
		// Have to build a word->offset map 
		WordRangeMap wm = new WordRangeMap();
		wm.addString(rawText);
		
		// Now identify the "words" that are affected and remove them
		Matcher leftMatcher = RawTwokenize.getLeftEdgePunctMatcher(rawText);
		while(leftMatcher.find()) {
			int startCharOffset = leftMatcher.start();
			int endCharOffset   = leftMatcher.end();
			int startWordOffset = wm.getWordStartOffset(startCharOffset);
			int endWordOffset   = wm.getWordEndOffset(endCharOffset);
			int groupCount      = leftMatcher.groupCount();
			if (groupCount != 3) continue;
			for (int i = 1; i <= groupCount; i++) {
				startCharOffset += leftMatcher.group(i).length();
				if (i == 2) this.annotations.insertSlice(startCharOffset);
			}
			//this.annotations.removeSliceRange(startWordOffset, endWordOffset);
		}
		
		leftMatcher = RawTwokenize.getLeftEdgePunctMatcher(rawText);
		textAfterLeftFiltering = leftMatcher.replaceAll("$1$2 $3");
		
		Matcher rightMatcher = RawTwokenize.getLeftEdgePunctMatcher(textAfterLeftFiltering);
		while(rightMatcher.find()) {
			int startCharOffset = rightMatcher.start();
			int endCharOffset   = rightMatcher.end();
			int startWordOffset = wm.getWordStartOffset(startCharOffset);
			int endWordOffset   = wm.getWordEndOffset(endCharOffset);
			this.annotations.removeSliceRange(startWordOffset, endWordOffset);
		}
		
		rightMatcher = RawTwokenize.getRightEdgePunctMatcher(textAfterLeftFiltering);
		textReadyToTokenize = rightMatcher.replaceAll("$1 $2$3");
		
		this.text   = textReadyToTokenize;
		this.tokens = tagger.tokenizeAndTag(textReadyToTokenize);
	}
}
