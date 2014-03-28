package uk.ac.warwick.dcs.SemEval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import cmu.arktweetnlp.RawTagger;

public abstract class SentimentApp {

	protected SemEvalTaskAReader r;
	protected SubjectivityMap sm;
	protected RawTagger posTagger;
	protected List<Tweet> tweets;
	protected List<POSTaggedTweet> taggedTweets;
	protected Set<String> modifierWords;
	protected Map<String, Attribute> attrMap;

	public SubjectivityMap getSubjectivityMap() {
		return this.sm;
	}

	public List<Tweet> getTweets() {
		return this.tweets;
	}

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
		this("model.20120919", "tweeter-dev-full-A-tweets.tsv");
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

	protected void setSubjectivityMap(SubjectivityMap sm) {
		this.sm = sm;
	}

	protected static void printEvaluationSummary(Evaluation elv)
			throws Exception {
		System.out.println(elv.toClassDetailsString());
		System.out.println(elv.toSummaryString());
		System.out.println(elv.toClassDetailsString());
		System.out.println(elv.toMatrixString());
		System.out.println(elv);
	}

	protected void selfEvaluate(boolean suppressPrint) throws Exception {
		Instances setInstances = this.createInstances();
		Classifier clfSelf = this.getUntrainedClassifier();
		Evaluation elvSelf = new Evaluation(setInstances);
		if (!suppressPrint) {
			System.out.println("***SELF EVALUATION***");
		}
		clfSelf.buildClassifier(setInstances);
		elvSelf.evaluateModel(clfSelf, setInstances);
		SubjectivityApp.printEvaluationSummary(elvSelf);
	}

	protected void selfEvaluate() throws Exception {
		this.selfEvaluate(false);
	}

	protected void crossValidate() throws Exception {
		Instances setInstances = this.createInstances();
		AbstractClassifier clfCross = this.getUntrainedClassifier();
		Evaluation elvCross = new Evaluation(setInstances);
		System.out.println("***CROSS VALIDATION (10 folds)***");
		elvCross.crossValidateModel(clfCross, setInstances, 10, new Random());
		SubjectivityApp.printEvaluationSummary(elvCross);
	}

	protected abstract Instances createInstances();
	protected abstract AbstractClassifier getUntrainedClassifier();
	protected abstract AbstractClassifier buildClassifier() throws Exception;
	protected abstract void crossValidateSentences() throws Exception;
}
