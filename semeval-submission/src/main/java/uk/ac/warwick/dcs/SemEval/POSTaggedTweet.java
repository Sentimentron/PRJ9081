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

	private List<RawTagger.TaggedToken> rawTokens;
	private List<POSToken> tokens;
	private WordRangeMap wm;
	
	public POSTaggedTweet(Tweet t, RawTagger tagger) throws WordRangeMapException {
		super(t.getText(), t.getAnnotations());
		
		String rawText = t.getText();
		String textAfterLeftFiltering;
		String textReadyToTokenize;
		
		// First: some spans will correspond to extra spaces
		// inserted by the pos tagger
		// Now identify the "words" that are affected
		Matcher leftMatcher = RawTwokenize.getLeftEdgePunctMatcher(rawText);
		while(leftMatcher.find()) {
			int startCharOffset = leftMatcher.start();
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
			int startCharOffset = leftMatcher.start();
			int groupCount      = leftMatcher.groupCount();
			if (groupCount != 3) continue;
			for (int i = 1; i <= groupCount; i++) {
				startCharOffset += leftMatcher.group(i).length();
				if (i == 1) this.annotations.insertSlice(startCharOffset);
			}
		}
		
		rightMatcher = RawTwokenize.getRightEdgePunctMatcher(textAfterLeftFiltering);
		textReadyToTokenize = rightMatcher.replaceAll("$1 $2$3");
		
		this.text   = textReadyToTokenize;
		this.wm     = new WordRangeMap(this.text);
		this.rawTokens = tagger.tokenizeAndTag(textReadyToTokenize);
		this.tokens = new ArrayList<POSToken>();
		for (RawTagger.TaggedToken token : this.rawTokens) {
			this.tokens.add(new POSToken(token, this.wm));
		}
		for (POSToken t : this.tokens) {
			for (int i = t.startWordOffset; i <= t.endWordOffset; i++) {
				t.annotate(this.annotations.get(i));
			}
		}
		
	}
}
