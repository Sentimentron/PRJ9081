package uk.ac.warwick.dcs.SemEval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Attribute;
import cmu.arktweetnlp.RawTagger;

public class SentimentApp {
	
	protected SemEvalTaskAReader r;
	protected SubjectivityMap sm;
	protected RawTagger posTagger;
	protected List<Tweet> tweets;
	protected List<POSTaggedTweet> taggedTweets;
	protected Set<String> modifierWords;
	protected Map<String, Attribute> attrMap;
	
	public SentimentApp(String model) throws IOException {
		this.sm = new SubjectivityMap();
		this.posTagger = new RawTagger();
		posTagger.loadModel(model);
	}
	
	public SentimentApp(String model, String tweets) throws IOException {
		this(model);
		this.r = new SemEvalTaskAReader(tweets);
	}
	
	public SentimentApp(List<Tweet> tweets) throws IOException {
		this("model.20120919");
		this.tweets = tweets;
	}
	
	public SentimentApp() throws IOException {
		this("model.20120919","tweeter-dev-full-A-tweets.tsv");
	}
	
	protected void readTweets() throws Exception {
		this.tweets = this.r.readTweets();
	}
	
	protected void posTagTweets() throws Exception {
		this.taggedTweets = new ArrayList<POSTaggedTweet>();
		for (Tweet t : tweets) {
			POSTaggedTweet p = new POSTaggedTweet(t, posTagger);
			taggedTweets.add(p);
		}
	}
	
	protected void updateSubjectivityMap() {
		for (POSTaggedTweet t : this.taggedTweets) {
			this.sm.updateFromTweet(t);
		}
	}
}
