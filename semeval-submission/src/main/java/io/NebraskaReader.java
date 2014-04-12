package io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import uk.ac.warwick.dcs.SemEval.AnnotationSpan;
import uk.ac.warwick.dcs.SemEval.ITweetReader;
import uk.ac.warwick.dcs.SemEval.MultiAnnotationMap;
import uk.ac.warwick.dcs.SemEval.Tweet;

public class NebraskaReader implements ITweetReader {
	
	private String path;
	
	public NebraskaReader (String path) {
		this.path = path;
	}

	private Connection createConnection() throws ClassNotFoundException, SQLException {
		Connection ret;
		Class.forName("org.sqlite.JDBC");
		ret = DriverManager.getConnection(String.format("jdbc:sqlite:%s", this.path));
		ret.setAutoCommit(false);
		return ret;
	}
	
	@Override
	public List<Tweet> readTweets() throws Exception {
		Connection conn = this.createConnection();
		Statement stmt = conn.createStatement();
		ResultSet rs;
		
		List<Tweet> ret = new ArrayList<Tweet>();
		
		// Phase 1 is to read all the tweets from the database
		rs = stmt.executeQuery("SELECT identifier, document FROM input");
		while (rs.next()) {
			int documentIdentifier = rs.getInt(1);
			String documentString  = rs.getString(2);
			Tweet cur = new Tweet(documentString, 0, documentIdentifier);
			ret.add(cur);
		}
				
		// Phase 2 is to synthesize consensus annotations
		for (Tweet t : ret) {
			MultiAnnotationMap mam = new MultiAnnotationMap();
			PreparedStatement fetchSubStmt = conn.prepareStatement(
					"SELECT annotation FROM subphrases WHERE document_identifier = ?"
				);
			fetchSubStmt.setInt(1, t.getId2());
			rs = fetchSubStmt.executeQuery();
			while (rs.next()) {
				List<AnnotationSpan> spans = AnnotationSpan.createSpansFromString(rs.getString(1));
				mam.addAll(spans);
			}
			t.setAnnotations(mam);
		}
		
		return ret;
	}

}
