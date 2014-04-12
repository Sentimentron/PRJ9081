package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Set;

import edu.stanford.nlp.util.Pair;
import uk.ac.warwick.dcs.SemEval.io.NebraskaReader;
import uk.ac.warwick.dcs.SemEval.io.NebraskaReaderB;
import uk.ac.warwick.dcs.SemEval.io.SemEvalTaskAReader;
import uk.ac.warwick.dcs.SemEval.io.SemEvalTaskBReader;
import uk.ac.warwick.dcs.SemEval.io.SemEvalTaskBWriter;
import uk.ac.warwick.dcs.SemEval.models.AnnotationType;
import uk.ac.warwick.dcs.SemEval.models.ITweetReader;
import uk.ac.warwick.dcs.SemEval.models.POSTaggedTweet;
import uk.ac.warwick.dcs.SemEval.models.POSToken;
import uk.ac.warwick.dcs.SemEval.models.TestingBTweet;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import uk.ac.warwick.dcs.SemEval.utils.Counter;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.Resample;

public class PolarityApp extends SentimentApp {

	public PolarityApp(List<Tweet> list) throws IOException {
		super(list);
	}

	public PolarityApp(ITweetReader r) throws IOException {
		super(r);
	}

	protected Set<Pair<String, String>> createThresholdedBigrams() {
		Counter<Pair<String, String>> bigrams = new Counter<Pair<String, String>>(); 
		
		for (POSTaggedTweet pt : this.taggedTweets) {
			List<POSToken> tList = pt.getPOSTokens();
			for (int i = 0; i < tList.size()-1; i++) {
				POSToken a = tList.get(i);
				POSToken b = tList.get(i+1);
				String aStr = String.format("%s/%s", a.token, a.tag);
				String bStr = String.format("%s/%s", b.token, b.tag);
				bigrams.put(new Pair<String, String>(aStr, bStr));
			}
		}
		
		// Thresholding 
		Set<Pair<String, String>> thresholdedBigrams = new TreeSet<Pair<String,String>>();
		for (Entry<Pair<String,String>,Integer> e : bigrams.entrySet()) {
			if (e.getValue() < 4) continue;
			thresholdedBigrams.add(e.getKey());
		}
		
		return thresholdedBigrams;
	}
	
	protected Map<Pair<String, String>, Attribute> createAttributeMap(
			Set<Pair<String, String>> thresholdedBigrams
			) {
		
		Map<Pair<String, String>, Attribute> attributeMap = new TreeMap<Pair<String,String>, Attribute>();
		List<String> nominalVals = new ArrayList<String>();
		nominalVals.add("present");
		nominalVals.add("notPresent");
		for (Pair<String, String> p : thresholdedBigrams) {
			Attribute attr = new Attribute(String.format("%s|%s", p.first, p.second), nominalVals);
			attributeMap.put(p, attr);
		}
		return attributeMap;
	}
	
	@Override
	protected Instances createInstances() throws Exception {
		Set<Pair<String, String>> thresholdedBigrams = this.createThresholdedBigrams();
		Map<Pair<String, String>, Attribute> attributeMap = this.createAttributeMap(thresholdedBigrams);
		return this.createInstances(thresholdedBigrams, attributeMap);
	}
	
	protected Map<TestingBTweet, Instance> createInstancesForTweets(
			Set<Pair<String, String>> thresholdedBigrams,
			Map<Pair<String, String>, Attribute> attributeMap, 
			Instance templateInstance, Instances dataSet,
			Attribute pnePercentPAttr,
			Attribute pnePercentNAttr,
			Attribute pnePercentEAttr,
			Attribute sentimentClassAttr
			) throws Exception {
		
		int progressCounter = 0;
		Map<TestingBTweet, Instance> ret = new TreeMap<TestingBTweet, Instance>();
		for (POSTaggedTweet pt: this.taggedTweets) {
			TestingBTweet parent = (TestingBTweet)pt.getParent();
			progressCounter++;
			if (progressCounter % 100 == 1 || progressCounter == taggedTweets.size()) {
				System.err.printf("Creating instances (%d attribute(s), %d/%d complete, %.4f)\r", 
						attributeMap.size()+4, progressCounter, taggedTweets.size(), 
						progressCounter * 100.0 / taggedTweets.size()
				);
			}
			
			Instance thisInstance = new DenseInstance(templateInstance);
			thisInstance.setDataset(dataSet);
			
			for (Attribute a : attributeMap.values()) {
				thisInstance.setValue(a, "notPresent");
			}
			
			int totalPositive = 0;
			int totalNegative = 0;
			int totalNeutral  = 0;
			List<POSToken> tList = pt.getPOSTokens();

			for (POSToken p : tList) {
				switch(p.getAnnotation().getKind()) {
				case Negative:
					totalNegative++;
					break;
				case Neutral:
					totalNeutral++;
					break;
				case Positive:
					totalPositive++;
					break;
				case Objective:
					break;
				default:
					throw new Exception("Why am I here?");
				}
			}
			
			for (int i = 0; i < tList.size()-1; i++) {
				POSToken a = tList.get(i);
				POSToken b = tList.get(i+1);
				String aStr = String.format("%s/%s", a.token, a.tag);
				String bStr = String.format("%s/%s", b.token, b.tag);
				Pair<String, String> attrKey = new Pair<String, String>(aStr, bStr);
				if (attributeMap.containsKey(attrKey)) {
					thisInstance.setValue(attributeMap.get(attrKey), "present");
				}
			}
			
			thisInstance.setValue(pnePercentEAttr, 1.0 * totalNeutral / tList.size());
			thisInstance.setValue(pnePercentNAttr, 1.0 * totalNegative / tList.size());
			thisInstance.setValue(pnePercentPAttr, 1.0 * totalPositive / tList.size());
			thisInstance.setValue(sentimentClassAttr, parent.getAnnotation().toNominalSentiment());
			ret.put(parent, thisInstance);
		}
		return ret;
	}
	
	protected Instances createInstances(Set<Pair<String, String>> thresholdedBigrams,
			Map<Pair<String, String>, Attribute> attributeMap
		) throws Exception {
		// Constructing attribute map 
		List<String> nominalVals = new ArrayList<String>();
		nominalVals.add("present");
		nominalVals.add("notPresent");
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		for (Attribute attr : attributeMap.values()) {
			attributes.add(attr);
		}
		
		// Constructing PNE percentage attribute
		Attribute pnePercentPAttr = new Attribute("PNEPercentageP");
		Attribute pnePercentNAttr = new Attribute("PNEPercentageN");
		Attribute pnePercentEAttr = new Attribute("PNEPercentageE");
		attributes.add(pnePercentPAttr);
		attributes.add(pnePercentNAttr);
		attributes.add(pnePercentEAttr);
		
		// Constructing class attribute
		Attribute sentimentClassAttr = new Attribute("sentimentClass", AnnotationType.getNominalList());
		attributes.add(sentimentClassAttr);
		
		// Construct Instances, Instance template
		Instances ret = new Instances("bigramPolarity", attributes, 0);
		Instance templateInstance = new DenseInstance(attributes.size());
		ret.setClass(sentimentClassAttr);
		
		for (Instance instance : this.createInstancesForTweets(thresholdedBigrams, 
				attributeMap, templateInstance, 
				ret, pnePercentPAttr, 
				pnePercentNAttr, pnePercentEAttr, sentimentClassAttr).values()) {
			ret.add(instance);
		}
		
		Resample resamplingFilter = new Resample();
		resamplingFilter.setBiasToUniformClass(0.75);
		resamplingFilter.setInputFormat(ret);
		return Filter.useFilter(ret, resamplingFilter);
	}

	@Override
	protected AbstractClassifier getUntrainedClassifier() {
		return new NaiveBayes();
	}
	
	@Override
	protected AbstractClassifier buildClassifier() throws Exception {
		Set<Pair<String, String>> thresholdedBigrams = this.createThresholdedBigrams();
		Map<Pair<String, String>, Attribute> polarityAttrMap = this.createAttributeMap(thresholdedBigrams);
		return this.buildClassifier(thresholdedBigrams, polarityAttrMap);
	}
	
	protected AbstractClassifier buildClassifier(Set<Pair<String, String>> thresholdedBigrams,
			Map<Pair<String, String>, Attribute> polarityAttrMap
	) throws Exception {
		Instances trainingInstances = this.createInstances(thresholdedBigrams, polarityAttrMap);
		AbstractClassifier clf = this.getUntrainedClassifier();
		clf.buildClassifier(trainingInstances);
		return clf;
	}
	
	protected void applyPredictions(AbstractClassifier clf, 
			Map<Pair<String, String>, Attribute> attributeMap,
			Set<Pair<String, String>> thresholdedBigrams) throws Exception {
		List<String> nominalVals = new ArrayList<String>();
		nominalVals.add("present");
		nominalVals.add("notPresent");
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		for (Attribute attr : attributeMap.values()) {
			attributes.add(attr);
		}
		
		// Constructing PNE percentage attribute
		Attribute pnePercentPAttr = new Attribute("PNEPercentageP");
		Attribute pnePercentNAttr = new Attribute("PNEPercentageN");
		Attribute pnePercentEAttr = new Attribute("PNEPercentageE");
		attributes.add(pnePercentPAttr);
		attributes.add(pnePercentNAttr);
		attributes.add(pnePercentEAttr);
		
		// Constructing class attribute
		Attribute sentimentClassAttr = new Attribute("sentimentClass", AnnotationType.getNominalList());
		attributes.add(sentimentClassAttr);
		
		// Construct Instances, Instance template
		Instances ret = new Instances("bigramPolarity", attributes, 0);
		ret.setClass(sentimentClassAttr);
		Instance templateInstance = new DenseInstance(attributes.size());
		
		for (Entry<TestingBTweet, Instance> e : this.createInstancesForTweets(thresholdedBigrams, 
				attributeMap, templateInstance, 
				ret, pnePercentPAttr, 
				pnePercentNAttr, pnePercentEAttr, sentimentClassAttr).entrySet()) {
			
			double prediction = clf.classifyInstance(e.getValue());
			String predictionStr = sentimentClassAttr.value((int) prediction);
			AnnotationType a = new AnnotationType(predictionStr);
			e.getKey().setAnnotation(a);
		}
		
		this.tweets.clear();
		for (POSTaggedTweet p : this.taggedTweets) {
    		this.tweets.add(p.getParent());
    	}
	}

	@Override
	protected void crossValidateSentences() throws Exception {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args) throws Exception {
		SemEvalTaskBWriter testWriter = new SemEvalTaskBWriter("output.B.pred");
		SemEvalTaskBReader testReader = new SemEvalTaskBReader("SemEval2014-task9-test-B-input.txt");
		
		MultiTweetReader trainSrc = new MultiTweetReader();
		MultiTweetReader bTrainSrc = new MultiTweetReader();
		trainSrc.addReader(new SemEvalTaskAReader("tweeter-dev-full-A-tweets.tsv"));
		trainSrc.addReader(new SemEvalTaskAReader("twitter-train-full-A.tsv"));
		trainSrc.addReader(new NebraskaReader("amt.sqlite"));
		bTrainSrc.addReader(new SemEvalTaskBReader("tweeter-dev-full-B.tsv"));
		bTrainSrc.addReader(new SemEvalTaskBReader("twitter-train-full-B.tsv"));
		bTrainSrc.addReader(new NebraskaReaderB("amt.sqlite"));
		
		SubjectivityApp subjectivitySrc = new SubjectivityApp(trainSrc);
		subjectivitySrc.readTweets();
		subjectivitySrc.posTagTweets();
		AbstractClassifier clfSubjective = subjectivitySrc.buildClassifier();
		
		SubjectivityApp subjectivityTarget = new SubjectivityApp(testReader);
		subjectivityTarget.readTweets();
		subjectivityTarget.posTagTweets();
		subjectivityTarget.setSubjectivityMap(subjectivitySrc.getSubjectivityMap());
		subjectivityTarget.applyPredictions(clfSubjective, subjectivitySrc.getSubjectivityMap());
		
		WordSentimentApp wordAnnotationSource = new WordSentimentApp(trainSrc);
		wordAnnotationSource.readTweets();
		wordAnnotationSource.posTagTweets();
		wordAnnotationSource.updateSubjectivityMap();
		AbstractClassifier clfWords = wordAnnotationSource.buildClassifier();
		
		WordSentimentApp wordAnnotationTarget = new WordSentimentApp(subjectivityTarget.getTweets());
		wordAnnotationTarget.posTagTweets();
		wordAnnotationTarget.applyPredictions(clfWords, wordAnnotationSource.generateModifierWords());
				
		PolarityApp polaritySrc = new PolarityApp(bTrainSrc);
		polaritySrc.readTweets();
		polaritySrc.posTagTweets();
		Set<Pair<String, String>> thresholdedBigrams = polaritySrc.createThresholdedBigrams();
		Map<Pair<String, String>, Attribute> polarityAttrMap = polaritySrc.createAttributeMap(thresholdedBigrams);
		AbstractClassifier clfPolarity = polaritySrc.buildClassifier(thresholdedBigrams, polarityAttrMap);
		
		PolarityApp polarityTarget = new PolarityApp(subjectivityTarget.getTweets());
		polarityTarget.posTagTweets();
		polarityTarget.applyPredictions(clfPolarity, polarityAttrMap, thresholdedBigrams);
		for (Tweet p : polarityTarget.getTweets()) {
			testWriter.writeTweet((TestingBTweet)p);
		}
		
		testWriter.finish();
	}

}
