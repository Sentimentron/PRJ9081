package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;

public class WordSentimentApp extends SentimentApp {
	
	public WordSentimentApp() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}

	public static String processWord(String s) {
		PorterStemmer stemmer = new PorterStemmer();
		s = s.toLowerCase();
		s = stemmer.stem(s);
		s = s.replaceAll("[^a-z]", "");
		s = s.trim();
		return s;
	}
	
	private void createAttr() {
		this.modifierWords = new HashSet<String>();
		this.attrMap       = new HashMap<String, Attribute>();
		for (POSTaggedTweet t : this.taggedTweets) {
			for (POSToken pt : t.getPOSTokens()) {
				if (pt.tag.equals("R") || pt.getAnnotation().isSubjective()) {
					String stemmed = processWord(pt.token);
					if (stemmed.length() == 0) continue;
					modifierWords.add(stemmed);
				}
			}
		}
		
		// Construct instances
		List<String> nominalVals = new ArrayList<String>();
		nominalVals.add("before");
		nominalVals.add("after");
		nominalVals.add("notPresent");
		nominalVals.add("present");
		
		this.attrMap = new HashMap<String, Attribute>();
		for (String modifierWord : modifierWords) {
			Attribute at = new Attribute(modifierWord, nominalVals);
			this.attrMap.put(modifierWord, at);
		}
	}
	
	private Map<String, Attribute> getAttributeMap() {
		return this.attrMap;
	}
	
	private ArrayList<Attribute> getAttributes() {
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		for (Map.Entry<String, Attribute> e : this.attrMap.entrySet()) {
			attrs.add(e.getValue());
		}
		return attrs;
	}
	
	private Instances createInstances() {
		
		ArrayList<Attribute> attrs = this.getAttributes();
		
		Attribute sentimentClassAttr = new Attribute(
				"sentimentClass", AnnotationType.getNominalList()
				);
		
		attrs.add(sentimentClassAttr);
		
		Instances toExport = new Instances("sentiment", attrs, 0);
		toExport.setClass(sentimentClassAttr);
		
		for (POSTaggedTweet t : this.taggedTweets) {
			List<POSToken> pt = t.getPOSTokens();
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
				Set<Attribute> currentlyNotSet = new HashSet<Attribute>(attrMap.values());
				
				for (int j = 0; j < pt.size(); j++) {
					POSToken p = pt.get(j);
					// Ignore if not an adverb or anything subjective
					if (!p.tag.equals("R") && !p.getAnnotation().isSubjective()) continue;
					
					// If the word is blank, don't process
					String s = processWord(p.token);
					if (s.length() == 0) continue;
					
					// Set the column
					Attribute toSet = this.attrMap.get(s);
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
		
		return toExport;
	}
	

	public static void main(String[] args) throws Exception {

		WordSentimentApp wa = new WordSentimentApp();
		wa.readTweets();
		wa.posTagTweets();
		wa.updateSubjectivityMap();
		wa.createAttr();
		
		Instances toExport = wa.createInstances();
		
		 BufferedWriter writer = new BufferedWriter(new FileWriter("sentiment.arff"));
		 writer.write(toExport.toString());
		 writer.flush();
		 writer.close();
	}

}
