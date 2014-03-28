package uk.ac.warwick.dcs.SemEval;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import cmu.arktweetnlp.RawTagger;
import edu.stanford.nlp.util.Pair;

/**
 * Hello world!
 *
 */
public class SubjectivityApp extends SentimentApp {
	
	private Attribute classAttr = null;
	
    public SubjectivityApp() throws IOException {
		super();
	}
    
    protected ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		attrs.add(new Attribute("conf1"));
		attrs.add(new Attribute("conf2"));
		attrs.add(new Attribute("conf3"));
		if (this.classAttr == null) {
	    	List<String> nominalVals = AnnotationType.getNominalSubjList();
			this.classAttr = new Attribute("subjective", nominalVals);
		}
		
		attrs.add(this.classAttr);
		return attrs;
	}
    
    protected Instances createInstances(List<MLSubjectiveTweet> learningTweets,
    		SubjectivityMap sm) {
    	Instances setInstances = new Instances("subj", this.getAttributes(), 0);
		setInstances.setClassIndex(3);
		
		Instance instanceTemplate = new DenseInstance(4);
		instanceTemplate.setDataset(setInstances);
		
		for (MLSubjectiveTweet m : learningTweets) {
			for (Instance dat : m.addInstanceData(instanceTemplate, setInstances, sm)) {
				assertTrue(setInstances.checkInstance(dat));
				setInstances.add(dat);
			}
		}
		
		setInstances.setClassIndex(3);
		return setInstances;
    }
    
    protected Instances createInstances(List<MLSubjectiveTweet> lT) {
    	return this.createInstances(lT, this.sm);
    }
    
    @Override
    protected Instances createInstances() {
    	return this.createInstances(this.getLearningTweets());
    }
    
    @Override
    protected AbstractClassifier getUntrainedClassifier() {
    	return new SimpleLogistic();
    }
    
    @Override
    public AbstractClassifier buildClassifier() throws Exception {
    	Instances setInstances = this.createInstances();
    	AbstractClassifier clf = this.getUntrainedClassifier();
    	clf.buildClassifier(setInstances);
    	return clf;
    }
    
    protected List<MLSubjectiveTweet> getLearningTweets() {
    	List<MLSubjectiveTweet> learningTweets = new ArrayList<MLSubjectiveTweet>();
		for (POSTaggedTweet p : this.taggedTweets) {
			MLSubjectiveTweet m = new MLSubjectiveTweet(p);
			learningTweets.add(m);
		}
		return learningTweets;
    }
    
    protected void crossValidateSentences(int fold, List<MLSubjectiveTweet> learningTweets) 
    		throws Exception {
    	
		Collections.shuffle(learningTweets, new Random(System.nanoTime()));
		
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
		
		// Update subjectivity map
		SubjectivityMap sm = new SubjectivityMap();
		for (MLSubjectiveTweet m : trainingSet) {
			sm.updateFromTweet(m.getWrappedTweet());
		}
		
		AbstractClassifier clfSent = this.getUntrainedClassifier();
		
		Instances trainingInstances = this.createInstances(trainingSet, sm);
		Instances testingInstances  = this.createInstances(testingSet, sm);
		
		clfSent.buildClassifier(trainingInstances);
		
		Evaluation foldElv = new Evaluation(testingInstances);
		foldElv.evaluateModel(clfSent, testingInstances);
		System.out.printf("FOLD %d\n", fold);
		SentimentApp.printEvaluationSummary(foldElv);
    }
    
    @Override
    protected void crossValidateSentences() throws Exception {
		System.out.println("***CROSS VALIDATION (sentences, 10 folds)***");
		List<MLSubjectiveTweet> learningTweets = this.getLearningTweets();
    	for (int fold = 0; fold < 10; fold ++) {
    		this.crossValidateSentences(fold, learningTweets);
    	}
    }
    
    protected void applyPredictions(AbstractClassifier clf, SubjectivityMap s) throws Exception {
    	
    	Instance instanceTemplate = new DenseInstance(4);
    	ArrayList<Attribute> attrList = this.getAttributes();
    	Instances setInstances = new Instances("subj", attrList, 0);
    	setInstances.setClassIndex(3);
    	
    	for (MLSubjectiveTweet m : this.getLearningTweets()) {
    		for (Pair<Integer, Instance> dat : 
    			m.getPredictionInstances(instanceTemplate, setInstances, s)) {
    			double prediction = clf.classifyInstance(dat.second);
    			String predictionStr = classAttr.value((int) prediction);
    			AnnotationType a = new AnnotationType(predictionStr);
    			m.getWrappedTweet().applyDerivedAnnotation(dat.first, a);
    		}
    	}
    	
    	// POS tags create copies of their parents
    	// We've just applied subjectivity annotations to their parents
    	// We need to replace our original tweets with those
    	this.tweets.clear();
    	
    	for (POSTaggedTweet p : this.taggedTweets) {
    		this.tweets.add(p.getParent());
    	}
    	
    }
    
	public static void main( String[] args ) throws Exception
    {		
    	SubjectivityApp sa = new SubjectivityApp();
    	sa.readTweets();
    	sa.posTagTweets();
    	sa.updateSubjectivityMap();
    	sa.selfEvaluate();
    	sa.crossValidate();
    	sa.crossValidateSentences();		
    }
}
