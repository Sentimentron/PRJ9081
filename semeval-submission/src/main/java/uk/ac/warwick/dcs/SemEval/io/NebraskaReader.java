package uk.ac.warwick.dcs.SemEval.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import uk.ac.warwick.dcs.SemEval.models.AnnotationSpan;
import uk.ac.warwick.dcs.SemEval.models.ITweetReader;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import uk.ac.warwick.dcs.SemEval.subjectivity.MultiAnnotationMap;

public class NebraskaReader implements ITweetReader {
	
	public enum NebraskaDomain {
		Tech, Politics, Finance, All
	};
	
	private String path;
	private NebraskaDomain domain;
	
	public NebraskaReader (String path, NebraskaDomain domain) {
		this.path = path;
		this.domain = domain;
	}
	
	public NebraskaReader (String path) {
		this(path, NebraskaDomain.All);
	}

	private Connection createConnection() throws ClassNotFoundException, SQLException {
		Connection ret;
		Class.forName("org.sqlite.JDBC");
		ret = DriverManager.getConnection(String.format("jdbc:sqlite:%s", this.path));
		ret.setAutoCommit(false);
		return ret;
	}
	
	private List<Integer> getDomainList() throws SQLException, ClassNotFoundException {
		List<Integer> ret = new ArrayList<Integer>();
		Connection conn = this.createConnection();
		PreparedStatement stmt;
		if (this.domain != NebraskaDomain.All) {
			stmt = conn.prepareStatement("SELECT label_identifier "
						+"FROM label_names_amt "
						+"WHERE label	 LIKE "
						+"?");
			stmt.setString(1, "%"+NebraskaReader.getDomainString(this.domain)+"%");	
		}
		else {
			stmt = conn.prepareStatement("SELECT label_identifier "
					+"FROM label_names_amt ");
		}
		ResultSet rs = stmt.executeQuery();
		while(rs.next()) {
			int labelIdentifier = rs.getInt(1);
			ret.add(labelIdentifier);
		}
		return ret;
	}
	
	private static String getDomainString(NebraskaDomain domain) {
		switch(domain) {
		case Tech:
			return "Tech";
		case Politics:
			return "Politics";
		case Finance:
			return "Finance";
		default:
			return "*";					
		}
	}

	@Override
	public List<Tweet> readTweets() throws Exception {
		Connection conn = this.createConnection();
		PreparedStatement stmt;
		ResultSet rs;
		
		List<Tweet> ret = new ArrayList<Tweet>();
		List<Integer> domainLabels = this.getDomainList();
		StringBuilder inBuilder = new StringBuilder();
		boolean first = true;
		for (@SuppressWarnings("unused") int label : domainLabels) {
			if (first) first = false;
			else inBuilder.append(',');
			inBuilder.append('?');
		}
		
		stmt = conn.prepareStatement("SELECT DISTINCT identifier, document FROM input "
				+ "WHERE identifier IN ("
				+ "SELECT DISTINCT document_identifier FROM label_amt "
				+ "WHERE label IN ("+inBuilder.toString()+"))");
		
		for (int i = 0; i < domainLabels.size(); i++) {
			int label = domainLabels.get(i);
			stmt.setInt(i+1, label);
		}
		
		// Phase 1 is to read all the tweets from the database
		rs = stmt.executeQuery();
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
