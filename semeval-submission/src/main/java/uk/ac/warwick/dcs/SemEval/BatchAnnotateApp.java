package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.util.Pair;
import uk.ac.warwick.dcs.SemEval.io.NebraskaReader;
import uk.ac.warwick.dcs.SemEval.io.NebraskaReader.NebraskaDomain;
import uk.ac.warwick.dcs.SemEval.models.AnnotationType;
import uk.ac.warwick.dcs.SemEval.models.AnnotationType.AnnotationKind;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import uk.ac.warwick.dcs.SemEval.subjectivity.SubjectivityMap;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.misc.InputMappedClassifier;
import weka.core.Attribute;

public class BatchAnnotateApp {

	protected NebraskaDomain domain;
	protected NebraskaReader reader;
	protected AbstractClassifier clfWord;
	protected AbstractClassifier clfSubjective;
	protected AbstractClassifier clfPolarity;
	protected SubjectivityMap subjectivityMap;
	protected Set<String> modifierWords;
	protected Set<Pair<String, String>> thresholdedBigrams;
	protected Map<Pair<String, String>, Attribute> attributeMap;
	
	public BatchAnnotateApp(String srcDatabase, NebraskaDomain d) {
		this.reader = new NebraskaReader(srcDatabase);
		this.domain = d;
	}

	private AbstractClassifier loadClassifier(String name) throws FileNotFoundException, IOException, ClassNotFoundException {
		String path = ReferenceClassifiersApp.buildClassifierName(name, this.domain);
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(path)
				);
		AbstractClassifier ret = (AbstractClassifier) ois.readObject();
		//InputMappedClassifier wrap = new InputMappedClassifier();
		//wrap.setClassifier(ret);
		ois.close();
		return ret;
	}
	
	private AbstractClassifier getSubjectiveClassifier() throws FileNotFoundException, ClassNotFoundException, IOException {
		return this.loadClassifier("subj");
	}
	
	private AbstractClassifier getWordClassifier() throws FileNotFoundException, ClassNotFoundException, IOException {
		return this.loadClassifier("word");
	}
	
	private AbstractClassifier getTweetClassifier() throws FileNotFoundException, ClassNotFoundException, IOException {
		return this.loadClassifier("tweet");
	}
	
	private SubjectivityMap getSubjectivityMap() throws FileNotFoundException, IOException, ClassNotFoundException {
		String path = ReferenceClassifiersApp.buildClassifierName("subjMap", this.domain);
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(path)
				);
		
		SubjectivityMap sm = (SubjectivityMap) ois.readObject();
		ois.close();
		return sm;
	}
	
	private Set<String> getModifierWords() throws FileNotFoundException, IOException, ClassNotFoundException {
		String path = ReferenceClassifiersApp.buildClassifierName("wordMod", this.domain);
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(path)
				);
		
		Set<String> words = (Set<String>) ois.readObject();
		ois.close();
		return words;
	}
	
	private Map<Pair<String, String>, Attribute> getAttributeMap() throws IOException, ClassNotFoundException {
		String path = ReferenceClassifiersApp.buildClassifierName("tweetModAnn", this.domain);
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(path)
				);
		
		ois.readObject(); // Skip thresholded bigrams
		Map<Pair<String, String>, Attribute> attrMap = (Map<Pair<String, String>, Attribute>) ois.readObject();
		ois.close();
		return attrMap;
	}
	
	private Set<Pair<String, String>> getThresholdedBigrams() throws IOException, ClassNotFoundException {
		String path = ReferenceClassifiersApp.buildClassifierName("tweetModAnn", this.domain);
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(path)
				);
		
		Set<Pair<String, String>> bigrams = (Set<Pair<String, String>>) ois.readObject(); // Skip thresholded bigrams
		ois.close();
		return bigrams;
	}
	
	private static List<Pair<Tweet, AnnotationKind>> analyseBatch(List<Tweet> input, 
			AbstractClassifier subj, AbstractClassifier word, AbstractClassifier tweet, 
			SubjectivityMap sm, Set<String> modifierWords, 
			Map<Pair<String, String>, Attribute> attributeMap,
			Set<Pair<String, String>> thresholdedBigrams) throws Exception {
		
		SubjectivityApp sa = new SubjectivityApp(input);
		sa.posTagTweets(); 
		sa.applyPredictions(subj, sm);
		WordSentimentApp wa = new WordSentimentApp(sa.getTweets());
		wa.posTagTweets();
		wa.applyPredictions(word, modifierWords);
		PolarityApp pa = new PolarityApp(wa.getTweets());
		pa.posTagTweets();
		pa.applyPredictions(tweet, attributeMap, thresholdedBigrams);
		
		List<Pair<Tweet, AnnotationKind>> ret = new LinkedList<Pair<Tweet, AnnotationKind>>();
		for (Tweet t : pa.getTweets()) {
			AnnotationKind k = t.getAnnotation().getKind();
			ret.add(new Pair<Tweet, AnnotationKind>(t, k));
		}
		
		return ret;
	}
	
	private List<List<Tweet>> segmentBatches() throws Exception {
		List<List<Tweet>> ret = new LinkedList<List<Tweet>>();
		List<Tweet> cur = new ArrayList<Tweet>();
		int counter = 0; 
		for (Tweet t : this.reader.readTweets()) {
			if (counter % 64 == 0) {
				if (cur.size() > 0) ret.add(cur);
				cur = new ArrayList<Tweet>();
			}
			cur.add(t);
			counter++;
		}
		if (cur.size() > 0) ret.add(cur);
		return ret;
	}
	
	protected void loadData() throws Exception {
		System.err.println("Loading classifiers and data...");
		this.clfPolarity = this.getTweetClassifier();
		this.clfSubjective = this.getSubjectiveClassifier();
		this.clfWord = this.getWordClassifier();
		this.subjectivityMap = this.getSubjectivityMap();
		this.modifierWords = this.getModifierWords();
		this.thresholdedBigrams = this.getThresholdedBigrams();
		this.attributeMap = this.getAttributeMap();
	}
	
	private void annotate(String outputFilePath) throws Exception {
		BufferedWriter outputStream = new BufferedWriter(new FileWriter(outputFilePath));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.err.println("Reading tweets...");
		List<List<Tweet>> inputData = this.segmentBatches();
		for (int i = 0; i < inputData.size(); i++) {
			System.err.printf("Processing batch %d of %d (%.2f %% done)\n", i, inputData.size(), i * 100.0f / inputData.size());
			List<Pair<Tweet, AnnotationKind>> results = BatchAnnotateApp.analyseBatch(inputData.get(i), 
					this.clfSubjective, 
					this.clfWord, 
					this.clfPolarity, 
					this.subjectivityMap, 
					this.modifierWords, 
					this.attributeMap,
					this.thresholdedBigrams
				);
			for (Pair<Tweet, AnnotationKind> p : results) {
				String date = sdf.format(p.first.getDate());
				String outputLine = String.format("%d,%s,%s\n", p.first.getId2(), date, p.second.toString());
				System.out.printf("%s\t%s", p.first.getText(), outputLine);
				outputStream.write(outputLine);
			}
		}
		outputStream.close();
	}
	
	public static void main(String[] args) throws Exception {

		String srcDatabase = "finance.sqlite";
		BatchAnnotateApp ba = new BatchAnnotateApp(srcDatabase, NebraskaDomain.Finance);
		ba.loadData();
		ba.annotate("DT4283.csv");
	}

}
