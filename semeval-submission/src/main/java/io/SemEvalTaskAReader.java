package io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import uk.ac.warwick.dcs.SemEval.AnnotationSpan;
import uk.ac.warwick.dcs.SemEval.AnnotationType;
import uk.ac.warwick.dcs.SemEval.ITweetReader;
import uk.ac.warwick.dcs.SemEval.Tweet;
import edu.stanford.nlp.util.Pair;

public class SemEvalTaskAReader implements ITweetReader {
	
	private String path;
	
	public SemEvalTaskAReader(String pathToFile) {
		this.path = pathToFile;
	}

	private Map<Pair<Long, Integer>, Tweet> readFromFile() throws Exception {
		String line;
		Map<Pair<Long, Integer>, Tweet> ret = new TreeMap<Pair<Long, Integer>, Tweet>();
		
		BufferedReader br = new BufferedReader(new FileReader(this.path));
		while ((line = br.readLine()) != null) {
		   String[] fields = line.split("\t");
		   long identifier1 = Long.parseLong(fields[0]);
		   int identifier2 = Integer.parseInt(fields[1]);
		   int start = Integer.parseInt(fields[2]);
		   int end   = Integer.parseInt(fields[3]);
		   String polarity = fields[4];
		   String tweet    = fields[5];
		   
		   if (tweet.equals("Not Available")) continue;
		   
		   Tweet obj;
		   Pair<Long, Integer> identity = new Pair<Long, Integer>(identifier1, identifier2);
		   if(ret.containsKey(identity)) {
			   obj = ret.get(identity);
		   }
		   else {
			   obj = new Tweet(tweet, identifier1, identifier2);
			   ret.put(identity, obj);
		   }
		   
		   int approximateWords = tweet.split(" ").length;
		   if (end > approximateWords) {
			   System.err.printf(
					   "Warning: truncating annotation for "
					 + "'%s' (%d, %d) to %d (maximum length)\n", 
					 tweet, identifier1, identifier2, 
					 approximateWords
				);
			   end = approximateWords;
		   }
		   if (start > approximateWords) {
			   System.err.printf(
					   "Warning: truncating annotation for "
					 + "'%s' (%d, %d) to %d (maximum start)\n", 
					 tweet, identifier1, identifier2, 
					 approximateWords
				);
			   start = approximateWords;
		   }
		   
		   AnnotationType spanPolarity = AnnotationType.fromSemEvalString(polarity);
		   AnnotationSpan span = new AnnotationSpan(spanPolarity.getKind(), start, end);
		   obj.addAnnotation(span);
		}
		
		br.close();
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.warwick.dcs.SemEval.ITweetReader#readTweets()
	 */
	@Override
	public List<Tweet> readTweets() throws Exception {
		List<Tweet> ret = new ArrayList<Tweet>();
		for (Map.Entry<Pair<Long, Integer>, Tweet> e: this.readFromFile().entrySet()) {
			ret.add(e.getValue());
		}
		return ret;
	}

}
