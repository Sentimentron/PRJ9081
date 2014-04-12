package uk.ac.warwick.dcs.SemEval.subjectivity;

import uk.ac.warwick.dcs.SemEval.models.POSToken;

public class RegexpSubjectivityMap extends SubjectivityMap {

	@Override
		public String getMapRepresentation(POSToken p) {
			String key = p.token;
			key = key.toLowerCase();
			key.replaceAll("[^a-z]", "");
			key = key.trim();
			if (key.length() == 0) return null;
			return key;
		}

	
	
}
