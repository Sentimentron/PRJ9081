package uk.ac.warwick.dcs.SemEval.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import uk.ac.warwick.dcs.SemEval.models.AnnotationType;
import uk.ac.warwick.dcs.SemEval.models.ITweetReader;
import uk.ac.warwick.dcs.SemEval.models.Tweet;

public class SemEvalTaskBReader implements ITweetReader {

	private String path;
	
	public SemEvalTaskBReader(String pathToFile) {
		this.path = pathToFile;
	}
	
	public List<Tweet> readTweets() throws NumberFormatException, IOException, Exception {
		List<Tweet> ret = new ArrayList<Tweet>();
		String line;
		BufferedReader br = new BufferedReader(new FileReader(this.path));
		while ((line = br.readLine()) != null) {
			String[] fields = line.split("\t");
			long identifier1 = 0;
			if (!fields[0].equals("NA")) {
				identifier1 = Long.parseLong(fields[0]);
			}
			else {
				identifier1 = 0;
			}
		    int identifier2 = Integer.parseInt(fields[1]);
		    String polarity = fields[2];
		    String tweet    = fields[3];
		   
		   if (tweet.equals("Not Available")) continue;
		   
		   Tweet out = new Tweet(tweet, 
				   identifier1, identifier2, 
				   AnnotationType.fromSemEvalString(polarity)
				  );
		   ret.add(out);
		}
		br.close();
		return ret;
	}
	
}
