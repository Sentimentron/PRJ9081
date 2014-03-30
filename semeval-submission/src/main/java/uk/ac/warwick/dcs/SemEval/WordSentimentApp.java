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

import edu.stanford.nlp.util.Pair;
import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
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
	
	private List<Pair<Integer, Instance>> generatePosTaggedSentenceInstances(
			POSTaggedTweet t, Set<String> modifierWords,
			Map<String, Attribute> attributeMap,
			Attribute sentimentClassAttr,
			Instance template, Instances dataSet) {
		
		List<Pair<Integer, Instance>> toExport = new ArrayList<Pair<Integer,Instance>>();
		List<POSToken> pt = t.getPOSTokens();
		
		for (int i = 0; i < pt.size(); i++) {
			AnnotationType a;
			if (t.getParent() instanceof TestingATweet) {
				TestingATweet tw = (TestingATweet) t.getParent();
				if (!tw.inInterestingSection(i)) continue;
				a = new AnnotationType(AnnotationKind.Subjective);
			}
			else {
				// Don't bother exporting objective stuff
				a = t.annotations.get(i);
				if (a == null) continue;
				if (!a.isSubjective()) continue;
			}
			
			// Create an instance which represents the context 
			// of a given annotation
			DenseInstance outputInstance = new DenseInstance(template);
			outputInstance.setDataset(dataSet);
			
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
			
			// Add to return structure
			toExport.add(new Pair<Integer, Instance>(pt.get(i).endCharOffset, outputInstance));
		}
		
		return toExport;
	}
	
	private List<Pair<Integer, Instance>> generateOffsetInstances(
			List<POSTaggedTweet> taggedTweets, Set<String> modifierWords,
			Instance template, Instances dataSet, Map<String, Attribute>attributeMap, 
			ArrayList<Attribute> attrs, Attribute sentimentClassAttr 
			) {
				
		List<Pair<Integer, Instance>> toExport = new ArrayList<Pair<Integer, Instance>>();		
		int progressCounter = 0;
		
		for (POSTaggedTweet t : taggedTweets) {
			
			for (Pair<Integer, Instance> pair : 
				this.generatePosTaggedSentenceInstances(
						t, modifierWords, attributeMap, sentimentClassAttr, 
						template, dataSet 
						)) {
				toExport.add(pair);
			}
			progressCounter++;
			if (progressCounter % 100 == 1 || progressCounter == taggedTweets.size()) {
				System.err.printf("Creating instances (%d attribute(s), %d/%d complete, %.4f)\r", 
						attrs.size(), progressCounter, taggedTweets.size(), 
						progressCounter * 100.0 / taggedTweets.size()
				);
			}				
		}
		System.err.println();
		return toExport;
	}
	
	private ArrayList<Attribute> getAttributeList(Map<String, Attribute> attributeMap,
			Attribute sentimentClassAttr) {
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		for (Entry<String, Attribute> e : attributeMap.entrySet()) {
			attrs.add(e.getValue());
		}
		attrs.add(sentimentClassAttr);
		return attrs;
	}
	
	public Instances createInstances(List<POSTaggedTweet> taggedTweets, Set<String> modifierWords) {
		
		Map<String, Attribute> attributeMap = this.getAttributes(modifierWords);
		
		
		Attribute sentimentClassAttr = new Attribute(
				"sentimentClass", AnnotationType.getNominalList()
				);
		
		ArrayList<Attribute> attrs = this.getAttributeList(attributeMap, sentimentClassAttr);
		
		Instance template = new DenseInstance(attrs.size());
		Instances toExport = new Instances("sentiment", attrs, 0);
		toExport.setClass(sentimentClassAttr);
		template.setDataset(toExport);
		
		for (Pair<Integer, Instance> pairedInstance : 
			this.generateOffsetInstances(taggedTweets, modifierWords, 
					template, toExport, 
					attributeMap, attrs, 
					sentimentClassAttr)
				) {
			Instance outputInstance = pairedInstance.second;
			outputInstance.setDataset(toExport);
			toExport.add(outputInstance);
		}
		
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

	public void applyPredictions(AbstractClassifier clfWords,
			Set<String> modifierWords) throws Exception {
		
		Map<String, Attribute> attributeMap = this.getAttributes(modifierWords);
		Attribute sentimentClassAttr = new Attribute(
				"sentimentClass", AnnotationType.getNominalList()
				);
		
		ArrayList<Attribute> attributeList = this.getAttributeList(attributeMap, sentimentClassAttr);
		
		Instance templateInstance = new DenseInstance(attributeList.size());
		Instances dummyInstances = new Instances("sentiment", attributeList, 0);
		dummyInstances.setClass(sentimentClassAttr);
		
		for (POSTaggedTweet pt : this.taggedTweets) {
			
			List<Pair<Integer, Instance>> instancePairs = 
					this.generatePosTaggedSentenceInstances(
							pt, modifierWords, 
							attributeMap, sentimentClassAttr,
							templateInstance, dummyInstances
						);
			
			for (Pair<Integer, Instance> dat : instancePairs) {
				double prediction = clfWords.classifyInstance(dat.second);
				String predictionStr = sentimentClassAttr.value((int) prediction);
    			AnnotationType a = new AnnotationType(predictionStr);
    			pt.applyDerivedAnnotation(dat.first, a);
			}
		}
		
		this.tweets.clear();
		for (POSTaggedTweet p : this.taggedTweets) {
    		this.tweets.add(p.getParent());
    	}
	}

}
