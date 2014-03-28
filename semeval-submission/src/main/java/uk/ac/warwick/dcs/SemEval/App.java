package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import cmu.arktweetnlp.RawTagger;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Hello World!" );
        SemEvalTaskAReader r = new SemEvalTaskAReader("tweeter-dev-full-A-tweets.tsv");
		List<Tweet> tweets = r.readTweets();
		
		SubjectivityMap subjectivityMap = new SubjectivityMap();
		
		RawTagger posTagger = new RawTagger();
		posTagger.loadModel("model.20120919");
		
		List<POSTaggedTweet> taggedTweets = new ArrayList<POSTaggedTweet>();
		for (Tweet t : tweets) {
			POSTaggedTweet p = new POSTaggedTweet(t, posTagger);
			subjectivityMap.updateFromTweet(p);
			taggedTweets.add(p);
		}
		List<MLSubjectiveTweet> learningTweets = new ArrayList<MLSubjectiveTweet>();
		for (POSTaggedTweet p : taggedTweets) {
			MLSubjectiveTweet m = new MLSubjectiveTweet(p);
			learningTweets.add(m);
		}
		
		List<String> nominalVals = AnnotationType.getNominalSubjList();
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		attrs.add(new Attribute("conf1"));
		attrs.add(new Attribute("conf2"));
		attrs.add(new Attribute("conf3"));
		Attribute classAttr = new Attribute("subjective", nominalVals);
		attrs.add(classAttr);
		
		Instances setInstances = new Instances("subj", attrs, 0);
		setInstances.setClassIndex(3);
		
		Instance instanceTemplate = new DenseInstance(4);
		instanceTemplate.setDataset(setInstances);
		
		for (MLSubjectiveTweet m : learningTweets) {
			for (Instance dat : m.addInstanceData(instanceTemplate, setInstances, subjectivityMap)) {
				assertTrue(setInstances.checkInstance(dat));
				setInstances.add(dat);
			}
		}
		
		AbstractClassifier clfSelf = new SimpleLogistic();
		clfSelf.buildClassifier(setInstances);
		Evaluation elvSelf = new Evaluation(setInstances);
		elvSelf.evaluateModel(clfSelf, setInstances);
		System.out.println("***SELF EVALUATION***");
		System.out.println(elvSelf.toClassDetailsString());
		System.out.println(elvSelf.toSummaryString());
		System.out.println(elvSelf.toClassDetailsString());
		System.out.println(elvSelf.toMatrixString());
		System.out.println(clfSelf);
		
		AbstractClassifier clfCross = new SimpleLogistic();
		clfSelf.buildClassifier(setInstances);
		Evaluation elvCross = new Evaluation(setInstances);
		elvCross.crossValidateModel(clfCross, setInstances, 10, new Random());
		System.out.println("***CROSS VALIDATION (10 folds)***");
		System.out.println(elvCross.toClassDetailsString());
		System.out.println(elvCross.toSummaryString());
		System.out.println(elvCross.toClassDetailsString());
		System.out.println(elvCross.toMatrixString());
		
		System.out.println("***CROSS VALIDATION (sentences, 10 folds)***");
		// Shuffle the sentences 
		long seed = System.nanoTime();
		Collections.shuffle(learningTweets, new Random(seed));		
		for (int fold = 0; fold < 10; fold++) {
			// Create the fold data
			List<MLSubjectiveTweet> trainingSet = new ArrayList<MLSubjectiveTweet>();
			List<MLSubjectiveTweet> testingSet  = new ArrayList<MLSubjectiveTweet>();
			for (int i = 0; i < learningTweets.size(); i++) {
				if (i % 10 == fold) {
					trainingSet.add(learningTweets.get(i));
				}
				else {
					testingSet.add(learningTweets.get(i));
				}
			}
			
			// Create classifier, subjectivity map etc
			AbstractClassifier clfSent = new SimpleLogistic();
			SubjectivityMap sm = new SubjectivityMap();
			
			// Update subjectivity map
			for (MLSubjectiveTweet m : trainingSet) {
				sm.updateFromTweet(m.getWrappedTweet());
			}
			
			// Generate instances 
			Instances foldInstances = new Instances("subj", attrs, 0);
			foldInstances.setClassIndex(3);
			
			instanceTemplate = new DenseInstance(4);
			instanceTemplate.setDataset(foldInstances);
			
			for (MLSubjectiveTweet m : trainingSet) {
				for (Instance dat : m.addInstanceData(instanceTemplate, foldInstances, sm)) {
					assertTrue(setInstances.checkInstance(dat));
					foldInstances.add(dat);
				}
			}
			
			clfSent.buildClassifier(foldInstances);
			
			// Generate testing instances
			foldInstances = new Instances("subj", attrs, 0);
			foldInstances.setClassIndex(3);
			
			instanceTemplate = new DenseInstance(4);
			instanceTemplate.setDataset(foldInstances);
			
			for (MLSubjectiveTweet m : testingSet) {
				for (Instance dat : m.addInstanceData(instanceTemplate, foldInstances, sm)) {
					assertTrue(setInstances.checkInstance(dat));
					foldInstances.add(dat);
				}
			}
			
			Evaluation foldElv = new Evaluation(foldInstances);
			foldElv.evaluateModel(clfSent, foldInstances);
			System.out.printf("FOLD %d\n", fold);
			System.out.println(foldElv.toClassDetailsString());
			System.out.println(foldElv.toSummaryString());
			System.out.println(foldElv.toClassDetailsString());
			System.out.println(foldElv.toMatrixString());
		}
		
    }
}
