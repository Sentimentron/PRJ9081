package uk.ac.warwick.dcs.SemEval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import uk.ac.warwick.dcs.SemEval.io.SemEvalTaskAReader;
import uk.ac.warwick.dcs.SemEval.models.ITweetReader;
import uk.ac.warwick.dcs.SemEval.models.POSTaggedTweet;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import uk.ac.warwick.dcs.SemEval.subjectivity.SubjectivityMap;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instances;
import cmu.arktweetnlp.RawTagger;

/**
 * @author Richard Townsend
 * 
 * Abstract base class which handles cross validation etc.
 *
 */
public abstract class SentimentApp {

	protected ITweetReader r;
	protected SubjectivityMap sm;
	protected RawTagger posTagger;
	protected List<Tweet> tweets;
	protected List<POSTaggedTweet> taggedTweets;

	protected long timeCreated;
	
	public SubjectivityMap getSubjectivityMap() {
		return this.sm;
	}

	public List<Tweet> getTweets() {
		return this.tweets;
	}

	/**
	 * Create SentimentApp without reading any tweets.
	 * @param model Path to Gimpel model
	 * @throws IOException
	 */
	public SentimentApp(String model) throws IOException {
		this.timeCreated = System.nanoTime();
		this.sm = new SubjectivityMap();
		this.posTagger = new RawTagger();
		posTagger.loadModel(model);
	}
	
	/**
	 * Create sentiment app with a custom Gimpel model path.
	 * @param model
	 * @param tweets
	 * @throws IOException
	 */
	public SentimentApp(String model, String tweets) throws IOException {
		this(model);
		this.r = new SemEvalTaskAReader(tweets);
	}
	
	/**
	 * Creates a SentimentApp with a custom tweet set but the 
	 * standard Gimpel POS tag path.
	 * @param tweets
	 * @throws IOException
	 */
	public SentimentApp(List<Tweet> tweets) throws IOException {
		this("model.20120919");
		this.tweets = tweets;
	}
	
	public SentimentApp(ITweetReader r) throws IOException {
		this("model.20120919");
		this.r = r;
	}

	/**
	 * Creates a SentimentApp with default parameters.
	 * @throws IOException
	 */
	public SentimentApp() throws IOException {
		this("model.20120919", "tweeter-dev-full-A-tweets.tsv");
	}

	/**
	 * Reads any tweets available.
	 * @throws Exception IOException, myriad others.
	 */
	protected void readTweets() throws Exception {
		this.tweets = this.r.readTweets();
	}

	/**
	 * POS tags all the Tweets managed by this class.
	 * @throws Exception 
	 */
	protected void posTagTweets() throws Exception {
		this.taggedTweets = new ArrayList<POSTaggedTweet>();
		int counter = 0;
		int total   = this.tweets.size();
		for (Tweet t : tweets) {
			POSTaggedTweet p;
			try {
				p = new POSTaggedTweet(t, posTagger);
			}
			catch(Exception ex) {
				System.err.printf("Cannot POS tag: %s (exception: %s)\n", t.getText(), ex.toString());
				continue;
			}
			taggedTweets.add(p);
			counter++;

			if (((counter % 100 == 0) && (counter != 0)) || counter == total) {
				System.err.printf("POS tagging... (%d/%d done, %.2f %%)\r", counter, total, counter*100.0f/total);
			}
		}
		System.err.println();
	}

	/**
	 * Generates the internal SubjectivityMap from POS-tagged tweets
	 */
	protected void updateSubjectivityMap() {
		for (POSTaggedTweet t : this.taggedTweets) {
			this.sm.updateFromTweet(t);
		}
	}

	/**
	 * Replaces the home-grown SubjectivityMap with another.
	 * You might want to do this if your annotations have rendered
	 * the locally-generated map meaningless.
	 * @param sm A SubjectivityMap
	 */
	protected void setSubjectivityMap(SubjectivityMap sm) {
		this.sm = sm;
	}

	/**
	 * Gee, I really wonder what this could do.
	 * @param elv WEKA Evaluation to erm... print an evaluation summary of.
	 * @throws Exception
	 */
	protected static void printEvaluationSummary(Evaluation elv)
			throws Exception {
		System.out.println(elv.toSummaryString());
		System.out.println(elv.toClassDetailsString());
		System.out.println(elv.toMatrixString());
	}

	/**
	 * Self-validates all the Instances generated by createInstances
	 * (i.e. the same data is used for training and test) and prints a 
	 * summary.
	 * @param suppressPrint If true, doesn't print ***SELF EVALUATION*** 
	 * @throws Exception
	 */
	protected void selfEvaluate(boolean suppressPrint) throws Exception {
		if (!suppressPrint) {
			System.out.println("***SELF EVALUATION***");
		}
		Instances setInstances = this.createInstances();
		AbstractClassifier clfSelf = this.getUntrainedClassifier();
		clfSelf.buildClassifier(setInstances);
		this.evaluateOn(clfSelf, setInstances);
	}
	
	
	/**
	 * Evaluate a classifier (maybe generated somewhere else) on the given instances.
	 * 
	 * WARNING: Instances must be compatible (same attributes, in the same order)
	 * 
	 * @param clf The classifier to evaluate
	 * @param setInstances The set of instances to evaluate the classifier on
	 * @throws Exception
	 */
	public void evaluateOn(AbstractClassifier clf, Instances setInstances) throws Exception {
		Evaluation elvSelf = new Evaluation(setInstances);
		elvSelf.evaluateModel(clf, setInstances);
		SentimentApp.printEvaluationSummary(elvSelf);
	}
	
	/**
	 * Evaluate a classifier generated somewhere else on the Instances generated
	 * from createInstances()
	 * 
	 * WARNING: Instances generated by this class must be compatible with the training
	 * data (same attributes, same order)
	 * 
	 * @param clf The WEKA AbstractClassifier to evaluate
	 * @throws Exception
	 */
	public void evaluateOn(AbstractClassifier clf) throws Exception {
		Instances setInstances = this.createInstances();
		this.evaluateOn(clf, setInstances);
	}

	/**
	 * Self-validates all the Instances generated by createInstances
	 * (i.e. the same data is used for training and test) and prints
	 * a summary.
	 * @throws Exception
	 */
	protected void selfEvaluate() throws Exception {
		this.selfEvaluate(false);
	}

	/**
	 * Cross-validates all the Instances generated by createInstances
	 * across 10 folds using a WEKA Evaluation and prints a summary.
	 * @throws Exception
	 */
	protected void crossValidate() throws Exception {
		Instances setInstances = this.createInstances();
		AbstractClassifier clfCross = this.getUntrainedClassifier();
		Evaluation elvCross = new Evaluation(setInstances);
		System.out.println("***CROSS VALIDATION (10 folds)***");
		elvCross.crossValidateModel(clfCross, setInstances, 10, new Random());
		SentimentApp.printEvaluationSummary(elvCross);
	}
	
	/**
	 * Creates WEKA instances from whatever Tweets are in this class
	 * WordSentimentApp and SubjecitivityApp have customised versions
	 * which allow generating a subset of all possible instances.
	 * @return A list of WEKA instances for classification
	 * @throws Exception 
	 */
	protected abstract Instances createInstances() throws Exception;
	/**
	 * Create a classifier with the default options + return it
	 * @return A WEKA AbstractClassifier ready for training.
	 */
	protected abstract AbstractClassifier getUntrainedClassifier();
	/**
	 * Returns a classifier trained with the Instances generated
	 * by createInstances()
	 * @return A trained WEKA AbstractClassifier 
	 * @throws Exception
	 */
	protected abstract AbstractClassifier buildClassifier() throws Exception;
	/**
	 * Cross-validates the generated classifier at the sentence
	 * level
	 * @throws Exception
	 */
	protected abstract void crossValidateSentences() throws Exception;
	
	
}
