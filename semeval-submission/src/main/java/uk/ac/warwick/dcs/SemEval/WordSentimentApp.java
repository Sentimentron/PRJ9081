package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;

public class WordSentimentApp extends SentimentApp {
	
	public WordSentimentApp() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public WordSentimentApp(List<Tweet> tweets) throws IOException {
		super(tweets);
	}
	
	public WordSentimentApp(ITweetReader r) throws IOException {
		super(r);
	}

	public static String processWord(String s) {
		PorterStemmer stemmer = new PorterStemmer();
		s = s.toLowerCase();
		s = stemmer.stem(s);
		s = s.replaceAll("[^a-z]", "");
		s = s.trim();
		return s;
	}
	
	private Map<String, Attribute> getAttributes(Set<String> modifierWords) {
		
		Map<String, Attribute> attributeMap = new TreeMap<String, Attribute>();
		List<String> nominalVals = new ArrayList<String>();
		nominalVals.add("before");
		nominalVals.add("after");
		nominalVals.add("notPresent");
		nominalVals.add("present");
		
		for (String modifierWord : new TreeSet<String>(modifierWords)) {
			Attribute at = new Attribute(modifierWord, nominalVals);
			attributeMap.put(modifierWord, at);
		}
		
		return attributeMap;
	}
	
	public Set<String> generateModifierWords() {
		Set<String> ret = new HashSet<String>();
		for (POSTaggedTweet t : this.taggedTweets) {
			for (POSToken pt : t.getPOSTokens()) {
				if (pt.tag.equals("R") || pt.getAnnotation().isSubjective()) {
					String stemmed = processWord(pt.token);
					if (stemmed.length() == 0) continue;
					ret.add(stemmed);
				}
			}
		}
		return ret;
	}
	
	private Map<String, Attribute> getAttributes() {
		return this.getAttributes(this.generateModifierWords());
	}
	
	public Instances createInstances() {
		return this.createInstances(this.taggedTweets);
	}
	
	public Instances createInstances(List<POSTaggedTweet> taggedTweets) { 
		return this.createInstances(taggedTweets, this.generateModifierWords());
	}
	
	public Instances createInstances(Set<String> modifierWords) {
		return this.createInstances(this.taggedTweets, modifierWords);
	}
	
	public Instances createInstances(List<POSTaggedTweet> taggedTweets, Set<String> modifierWords) {
		
		Map<String, Attribute> attributeMap = this.getAttributes(modifierWords);
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		for (Entry<String, Attribute> e : attributeMap.entrySet()) {
			attrs.add(e.getValue());
		}
		
		// Need to remove dependence on *this* 
		
		Attribute sentimentClassAttr = new Attribute(
				"sentimentClass", AnnotationType.getNominalList()
				);
		
		attrs.add(sentimentClassAttr);
		
		Instances toExport = new Instances("sentiment", attrs, 0);
		toExport.setClass(sentimentClassAttr);
		
		int progressCounter = 0;
		
		for (POSTaggedTweet t : taggedTweets) {
			List<POSToken> pt = t.getPOSTokens();
			progressCounter++;
			if (progressCounter % 100 == 1 || progressCounter == taggedTweets.size()) {
				System.err.printf("Creating instances (%d attribute(s), %d/%d complete, %.4f)\r", 
						attrs.size(), progressCounter, taggedTweets.size(), 
						progressCounter * 100.0 / taggedTweets.size()
				);
			}
			for (int i = 0; i < pt.size(); i++) {
				
				// Don't bother exporting objective stuff
				AnnotationType a = t.annotations.get(i);
				if (a == null) continue;
				if (!a.isSubjective()) continue;
				
				// Create an instance which represents the context 
				// of a given annotation
				DenseInstance outputInstance = new DenseInstance(attrs.size());
				outputInstance.setDataset(toExport);
				
				// This keeps track of what's not in this tweet
				Set<Attribute> currentlyNotSet = new HashSet<Attribute>(attributeMap.values());
				
				for (int j = 0; j < pt.size(); j++) {
					POSToken p = pt.get(j);
					// Ignore if not an adverb or anything subjective
					if (!p.tag.equals("R") && !p.getAnnotation().isSubjective()) continue;
					
					// If the word is blank, don't process
					String s = processWord(p.token);
					if (s.length() == 0) continue;
					
					// Set the column
					Attribute toSet = attributeMap.get(s);
					if (toSet == null) {
						System.err.printf("WARNING: can't find '%s' attribute\n", s);
						continue;
					}
					
					if (i < j) {
						outputInstance.setValue(toSet, "after");
					}
					else if (i == j) {
						outputInstance.setValue(toSet, "present");
					}
					else {
						outputInstance.setValue(toSet, "before");
					}
					
					// Don't need to mark this as not present
					currentlyNotSet.remove(toSet);
				}
				
				for (Attribute attr : currentlyNotSet) {
					// Everything else, better to set "notPresent" than missing
					outputInstance.setValue(attr, "notPresent");
				}
				
				// Set the class attribute
				outputInstance.setValue(sentimentClassAttr, a.toNominalSentiment());
				
				// Add to dataset
				toExport.add(outputInstance);
			}
		}
		System.err.println();
		return toExport;
	}

	@Override
	protected AbstractClassifier getUntrainedClassifier() {
		return new RandomForest();
	}

	@Override
	protected AbstractClassifier buildClassifier() throws Exception {
		Instances instances = this.createInstances();
		AbstractClassifier clf = this.getUntrainedClassifier();
		clf.buildClassifier(instances);
		return clf;
	}
	
	public void evaluateOn(AbstractClassifier clf, Set<String> modifierWords) throws Exception {
		Instances setInstances = this.createInstances(modifierWords);
		super.evaluateOn(clf, setInstances);
	}

	protected void crossValidateSentence(int fold, List<POSTaggedTweet> learningTweets) throws Exception {
		Collections.shuffle(learningTweets);
		
		List<POSTaggedTweet> trainingSet = new ArrayList<POSTaggedTweet>();
		List<POSTaggedTweet> testingSet  = new ArrayList<POSTaggedTweet>();
		for (int i = 0; i < learningTweets.size(); i++) {
			if (i % 10 == fold) {
				trainingSet.add(learningTweets.get(i));
			}
			else {
				testingSet.add(learningTweets.get(i));
			}
		}
		
		AbstractClassifier clf = this.getUntrainedClassifier();
		
		Instances trainingInstances = this.createInstances(trainingSet);
		Instances testingInstances = this.createInstances(trainingSet);
		
		clf.buildClassifier(trainingInstances);
		
		Evaluation elv = new Evaluation(testingInstances);
		elv.evaluateModel(clf, testingInstances);
		
		SentimentApp.printEvaluationSummary(elv);
	}
	
	@Override
	protected void crossValidateSentences() throws Exception {
		for (int fold = 0; fold < 10; fold++) {
			this.crossValidateSentence(fold, this.taggedTweets);
		}
	}
	
	public static void main(String[] args) throws Exception {

		WordSentimentApp wa = new WordSentimentApp();
		wa.readTweets();
		wa.posTagTweets();
		wa.updateSubjectivityMap();
		
		wa.selfEvaluate();
    	wa.crossValidate();
    	wa.crossValidateSentences();	
		
		Instances toExport = wa.createInstances();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter("sentiment.arff"));
		writer.write(toExport.toString());
		writer.flush();
		writer.close();
	}

}
