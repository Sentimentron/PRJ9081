package uk.ac.warwick.dcs.SemEval;

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
