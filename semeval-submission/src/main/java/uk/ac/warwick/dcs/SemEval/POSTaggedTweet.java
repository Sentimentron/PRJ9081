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
	private WordRangeMap wm, origWm;
	private List<Integer> insertedOffsets;
	private Tweet parent;
	
	public List<POSToken> getPOSTokens() {
		List<POSToken> ret = new ArrayList<POSToken>();
		for (POSToken t : this.tokens) {
			ret.add(t.clone());
		}
		return ret;
	}
	
	public void applyDerivedAnnotation(int derivedEndCharOffset, AnnotationType a) throws WordRangeMapException {
		
		for (int i = insertedOffsets.size() - 1; i >= 0; i--) {
			int insertedOffset = this.insertedOffsets.get(i);
			// If the insertedOffset is greater than or equal to the derived offset
			// then that insertion won't have affected the derived offset because we only insert
			// Otherwise, have to decrement the insertedOffset
			if (insertedOffset < derivedEndCharOffset) {
				derivedEndCharOffset--;
			}
			
		}
		int wordOffset = this.origWm.getWordEndOffset(derivedEndCharOffset);
		
		this.parent.addAnnotation(new AnnotationSpan(a.getKind(), wordOffset, wordOffset));
	}
	
	public Tweet getParent() {
		return this.parent;
	}
	
	public POSTaggedTweet(Tweet t, RawTagger tagger) throws Exception {
		super(t.getText(), t.getAnnotations(), t.getId1(), t.getId2());
		
		this.parent = new Tweet(t.getText(), t.getAnnotations(), t.getId1(), t.getId2());
		
		String rawText = t.getText();
		String textAfterLeftFiltering;
		String textReadyToTokenize;
		
		this.origWm = new WordRangeMap(this.getText());
		this.insertedOffsets = new ArrayList<Integer>();
		
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
				if (i == 2) {
					this.annotations.insertSlice(this.origWm.getWordEndOffset(startCharOffset));
					this.insertedOffsets.add(startCharOffset);
				}
			}
			//this.annotations.removeSliceRange(startWordOffset, endWordOffset);
		}
		
		leftMatcher = RawTwokenize.getLeftEdgePunctMatcher(rawText);
		textAfterLeftFiltering = leftMatcher.replaceAll("$1$2 $3");
		
		WordRangeMap revisedWordMap = new WordRangeMap(textAfterLeftFiltering);
		
		Matcher rightMatcher = RawTwokenize.getRightEdgePunctMatcher(textAfterLeftFiltering);
		while(rightMatcher.find()) {
			int startCharOffset = rightMatcher.start();
			int groupCount      = rightMatcher.groupCount();
			if (groupCount != 3) continue;
			for (int i = 1; i <= groupCount; i++) {
				startCharOffset += rightMatcher.group(i).length();
				if (i == 1) {
					this.annotations.insertSlice(revisedWordMap.getWordEndOffset(startCharOffset));
					this.insertedOffsets.add(startCharOffset);
				}
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
		for (POSToken token : this.tokens) {
			for (int i = token.startWordOffset; i <= token.endWordOffset; i++) {
				AnnotationType an = this.annotations.get(i);
				if (an != null) {
					token.setAnnotation(an);
				}
			}
		}
		
	}
}
