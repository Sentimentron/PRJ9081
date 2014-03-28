package uk.ac.warwick.dcs.SemEval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import opennlp.tools.stemmer.Stemmer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instances;
import cmu.arktweetnlp.RawTagger;

public class WordSentimentApp {
	
	public static String processWord(String s) {
		PorterStemmer stemmer = new PorterStemmer();
		s = s.toLowerCase();
		s = stemmer.stem(s);
		s = s.replaceAll("[^a-z]", "");
		s = s.trim();
		return s;
	}

	public static void main(String[] args) throws Exception {
		
		//
		// Reading tweets
		SemEvalTaskAReader r = new SemEvalTaskAReader("tweeter-dev-full-A-tweets.tsv");
		List<Tweet> tweets = r.readTweets();
		
		PorterStemmer stemmer = new PorterStemmer();
		
		SubjectivityMap sm = new SubjectivityMap();
				
		RawTagger posTagger = new RawTagger();
		posTagger.loadModel("model.20120919");
		
		List<POSTaggedTweet> taggedTweets = new ArrayList<POSTaggedTweet>();
		for (Tweet t : tweets) {
			POSTaggedTweet p = new POSTaggedTweet(t, posTagger);
			taggedTweets.add(p);
		}
		
		// Go through POS-tagged tweets and pull out the ADVERB (R) tags
		// also pull out any subjective information
		Set<String> modifierWords = new HashSet<String>();
		for (POSTaggedTweet t : taggedTweets) {
			sm.updateFromTweet(t);
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
		
		ArrayList<Attribute> attrs = new ArrayList<Attribute>();
		Map<String, Attribute> attrMap = new HashMap<String, Attribute>();
		for (String modifierWord : modifierWords) {
			Attribute at = new Attribute(modifierWord, nominalVals);
			attrMap.put(modifierWord, at);
		}
		
		Map<String, Attribute> fullAttrMap = new HashMap<String, Attribute>(attrMap);
		
		// fullAttrMap.put("classificationWord", new Attribute("classificationWord", (FastVector)null));
		fullAttrMap.put("classificationSubValue", new Attribute("classifionSubValue"));
		Attribute sentimentClassAttr = new Attribute("sentimentClass", AnnotationType.getNominalList());
		
		for (Map.Entry<String, Attribute> e : fullAttrMap.entrySet()) {
			attrs.add(e.getValue());
		}
		
		attrs.add(sentimentClassAttr);
		
		Instances toExport = new Instances("sentiment", attrs, 0);
		toExport.setClass(sentimentClassAttr);
		for (POSTaggedTweet t : taggedTweets) {
			
			List<POSToken> pt = t.getPOSTokens();
			for (int i = 0; i < pt.size(); i++) {
				AnnotationType a = t.annotations.get(i);
				if (a == null) continue;
				if (!a.isSubjective()) continue;
				DenseInstance outputInstance = new DenseInstance(modifierWords.size() + 2);
				outputInstance.setDataset(toExport);
				Set<Attribute> currentlyNotSet = new HashSet<Attribute>(attrMap.values());
				for (int j = 0; j < pt.size(); j++) {
					POSToken p = pt.get(j);
					if (!p.tag.equals("R") && !p.getAnnotation().isSubjective()) continue;
					String s = processWord(p.token);
					if (s.length() == 0) continue;
					Attribute toSet = attrMap.get(s);
					if (i < j) {
						outputInstance.setValue(toSet, "after");
					}
					else if (i == j) {
						outputInstance.setValue(toSet, "present");
					}
					else {
						outputInstance.setValue(toSet, "before");
					}
					currentlyNotSet.remove(toSet);
				}
				for (Attribute attr : currentlyNotSet) {
					outputInstance.setValue(attr, "notPresent");
				}
				
				outputInstance.setValue(fullAttrMap.get("classificationSubValue"), sm.get(pt.get(i)));
				outputInstance.setValue(sentimentClassAttr, a.toNominalSentiment());
				
				toExport.add(outputInstance);
			}
		}
		
		 BufferedWriter writer = new BufferedWriter(new FileWriter("sentiment.arff"));
		 writer.write(toExport.toString());
		 writer.flush();
		 writer.close();
	}

}
