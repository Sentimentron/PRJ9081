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
import weka.classifiers.AbstractClassifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class PolarityApp extends SentimentApp {

	public PolarityApp(List<Tweet> list) throws IOException {
		super(list);
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
			if (e.getValue() < 2) continue;
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
	protected Instances createInstances() {
		Set<Pair<String, String>> thresholdedBigrams = this.createThresholdedBigrams();
		Map<Pair<String, String>, Attribute> attributeMap = this.createAttributeMap(thresholdedBigrams);
		return this.createInstances(thresholdedBigrams, attributeMap);
	}
	
	protected Instances createInstances(Set<Pair<String, String>> thresholdedBigrams,
			Map<Pair<String, String>, Attribute> attributeMap
		) {
		// Constructing attribute map 
		List<String> nominalVals = new ArrayList<String>();
		nominalVals.add("present");
		nominalVals.add("notPresent");
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		for (Attribute attr : attributeMap.values()) {
			attributes.add(attr);
		}
		
		// Constructing PNE percentage attribute
		Attribute pnePercentAttr = new Attribute("PNEPercentage");
		attributes.add(pnePercentAttr);
		
		// Constructing class attribute
		Attribute sentimentClassAttr = new Attribute("sentimentClass", AnnotationType.getNominalList());
		attributes.add(sentimentClassAttr);
		
		// Construct Instances, Instance template
		Instances ret = new Instances("bigramPolarity", attributes, 0);
		Instance templateInstance = new DenseInstance(attributes.size());
		
		for (POSTaggedTweet pt : this.taggedTweets) {
			
			TestingBTweet parent = (TestingBTweet)pt.getParent();
			
			Instance thisInstance = new DenseInstance(templateInstance);
			thisInstance.setDataset(ret);
			
			for (Pair<String, String> p : thresholdedBigrams) {
				thisInstance.setValue(attributeMap.get(p), "notPresent");
			}
			
			int totalSubjective = 0;
			List<POSToken> tList = pt.getPOSTokens();

			for (POSToken p : tList) {
				if (p.getAnnotation().isSubjective()) totalSubjective++;
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
			
			thisInstance.setValue(pnePercentAttr, totalSubjective*100.0f/tList.size());
			thisInstance.setValue(sentimentClassAttr, parent.getAnnotation().toNominalSentiment());
			ret.add(thisInstance);
		}
		
		// TODO Auto-generated method stub
		return ret;
	}

	@Override
	protected AbstractClassifier getUntrainedClassifier() {
		return new NaiveBayes();
	}

	@Override
	protected AbstractClassifier buildClassifier() throws Exception {
		AbstractClassifier clf = this.getUntrainedClassifier();
		
	}

	@Override
	protected void crossValidateSentences() throws Exception {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args) throws Exception {
		SemEvalTaskAReader subjReader = new SemEvalTaskAReader("tweeter-dev-full-A-tweets.tsv");
		SemEvalTaskBWriter testWriter = new SemEvalTaskBWriter("output.B.pred");
		SemEvalTaskBReader testReader = new SemEvalTaskBReader("twitter-test-gold-B.tsv");
		
		SubjectivityApp subjectivitySrc = new SubjectivityApp(subjReader);
		subjectivitySrc.readTweets();
		subjectivitySrc.posTagTweets();
		AbstractClassifier clfSubjective = subjectivitySrc.buildClassifier();
		
		SubjectivityApp subjectivityTarget = new SubjectivityApp(testReader);
		subjectivityTarget.readTweets();
		subjectivityTarget.posTagTweets();
		subjectivityTarget.setSubjectivityMap(subjectivitySrc.getSubjectivityMap());
		subjectivityTarget.applyPredictions(clfSubjective, subjectivitySrc.getSubjectivityMap());
		
		PolarityApp p = new PolarityApp(subjectivityTarget.getTweets());
		p.posTagTweets();
		
		Instances toExport = p.createInstances();
		BufferedWriter br = new BufferedWriter(new FileWriter("polarity.arff"));
		br.write(toExport.toString());
	}

}
