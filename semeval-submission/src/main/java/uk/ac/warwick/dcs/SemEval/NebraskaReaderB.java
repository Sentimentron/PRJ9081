package uk.ac.warwick.dcs.SemEval;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import uk.ac.warwick.dcs.SemEval.AnnotationType.AnnotationKind;

public class NebraskaReaderB implements ITweetReader {
	
	private String path; 
	
	public NebraskaReaderB (String path) {
		this.path = path;
	}
	
	private Connection createConnection() throws ClassNotFoundException, SQLException {
		Connection ret;
		Class.forName("org.sqlite.JDBC");
		ret = DriverManager.getConnection(String.format("jdbc:sqlite:%s", this.path));
		ret.setAutoCommit(false);
		return ret;
	}
	
	public List<Tweet> readTweets() throws Exception {
		Connection conn = this.createConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs;
		
		List<Tweet> ret = new ArrayList<Tweet>();
		
		rs = stmt.executeQuery("SELECT identifier, document FROM input");
		while (rs.next()) {
			int documentIdentifier = rs.getInt(1);
			String documentString  = rs.getString(2);
			Tweet cur = new TestingBTweet(documentString, 0, documentIdentifier);
			ret.add(cur);
		}
		
		for (Tweet tweet : ret) {
			TestingBTweet t = (TestingBTweet)tweet;
			MultiAnnotationMap mam = new MultiAnnotationMap();
			PreparedStatement fetchSubStmt = conn.prepareStatement(
					"SELECT sentiment FROM subphrases WHERE document_identifier = ?"
				);
			fetchSubStmt.setInt(1, t.getId2());
			rs = fetchSubStmt.executeQuery();
			while (rs.next()) {
				String annotationStr = rs.getString(1);
				if (annotationStr.equals("neutral")) t.setAnnotation(new AnnotationType(AnnotationKind.Neutral));
				else if (annotationStr.equals("negative")) t.setAnnotation(new AnnotationType(AnnotationKind.Neutral));
				else if (annotationStr.equals("positive")) t.setAnnotation(new AnnotationType(AnnotationKind.Positive));
			}
			t.annotations = mam;
		}
		
		return ret;
		
	}
}
