package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import uk.ac.warwick.dcs.SemEval.io.NebraskaReader;
import uk.ac.warwick.dcs.SemEval.io.NebraskaReader.NebraskaDomain;
import uk.ac.warwick.dcs.SemEval.models.AnnotationType;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import uk.ac.warwick.dcs.SemEval.models.AnnotationType.AnnotationKind;
import uk.ac.warwick.dcs.SemEval.subjectivity.SubjectivityMap;
import weka.classifiers.AbstractClassifier;
import weka.core.Attribute;
import edu.stanford.nlp.util.Pair;

public class BatchAnnotateWordPolApp {
	private NebraskaDomain domain;
	private NebraskaReader reader;
	private AbstractClassifier clfWord;
	private AbstractClassifier clfSubjective;
	private SubjectivityMap subjectivityMap;
	private Set<String> modifierWords;
	
	public BatchAnnotateWordPolApp(String srcDatabase, NebraskaDomain d) {
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
	
	private static void analyseBatch(List<Tweet> input, 
			AbstractClassifier subj, AbstractClassifier word, 
			SubjectivityMap sm, Set<String> modifierWords,
			BufferedWriter output
			) throws Exception {
		
		SubjectivityApp sa = new SubjectivityApp(input);
		sa.posTagTweets(); 
		sa.applyPredictions(subj, sm);
		WordSentimentApp wa = new WordSentimentApp(sa.getTweets());
		wa.posTagTweets();
		wa.applyPredictions(word, modifierWords);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Pair<Tweet, AnnotationKind>> ret = new LinkedList<Pair<Tweet, AnnotationKind>>();
		for (Tweet t : wa.getTweets()) {
			int positive = 0;
			int negative = 0; 
			int neutral  = 0;
			int subjective = 0;
			for (Entry<Integer, AnnotationType> a : t.getAnnotations().entrySet()) {
				if (a.getValue().isSubjective()) {
					subjective++;
				}
				switch(a.getValue().getKind()) {
				case Negative:
					negative++;
					break;
				case Positive:
					positive++;
					break;
				case Neutral:
					neutral++;
					break;
				default:
					break;
				}
			}
			String outputDate = sdf.format(t.getDate());
			String outputLine = String.format("%d\t%s\t%d\t%d\t%d\t%d\n", t.getId2(), outputDate, positive, negative, neutral, subjective);
			System.out.printf("%s\t%s", t.getText(), outputLine);
			output.write(outputLine);
		}
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
	
	private void loadData() throws Exception {
		System.err.println("Loading classifiers and data...");
		this.clfSubjective = this.getSubjectiveClassifier();
		this.clfWord = this.getWordClassifier();
		this.subjectivityMap = this.getSubjectivityMap();
		this.modifierWords = this.getModifierWords();
	}
	
	private void annotate(String outputFilePath) throws Exception {
		BufferedWriter outputStream = new BufferedWriter(new FileWriter(outputFilePath));
		System.err.println("Reading tweets...");
		List<List<Tweet>> inputData = this.segmentBatches();
		for (int i = 0; i < inputData.size(); i++) {
			System.err.printf("Processing batch %d of %d (%.2f %% done)\n", i, inputData.size(), i * 100.0f / inputData.size());
			BatchAnnotateWordPolApp.analyseBatch(inputData.get(i), 
					this.clfSubjective, 
					this.clfWord, 
					this.subjectivityMap, 
					this.modifierWords,
					outputStream
				);
		}
		outputStream.close();
	}
	
	public static void main(String[] args) throws Exception {

		String srcDatabase = "apple_filtered.sqlite";
		BatchAnnotateWordPolApp ba = new BatchAnnotateWordPolApp(srcDatabase, NebraskaDomain.Politics);
		ba.loadData();
		ba.annotate("DT4270.csv");
	}
}
