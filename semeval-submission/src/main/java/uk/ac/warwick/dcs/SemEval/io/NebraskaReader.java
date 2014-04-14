package uk.ac.warwick.dcs.SemEval.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.ac.warwick.dcs.SemEval.models.AnnotationSpan;
import uk.ac.warwick.dcs.SemEval.models.AnnotationType;
import uk.ac.warwick.dcs.SemEval.models.ITweetReader;
import uk.ac.warwick.dcs.SemEval.models.Tweet;
import uk.ac.warwick.dcs.SemEval.models.AnnotationType.AnnotationKind;
import uk.ac.warwick.dcs.SemEval.subjectivity.MultiAnnotationMap;

public class NebraskaReader implements ITweetReader {
	
	public enum NebraskaDomain {
		Tech, Politics, Finance, All
	};
	
	private String path;
	private NebraskaDomain domain;
	private boolean skipReadingAnnotations;
	
	public NebraskaReader (String path, NebraskaDomain domain, boolean skip) {
		this.path = path;
		this.domain = domain;
		this.skipReadingAnnotations = skip;
	}
	
	public NebraskaReader (String path, NebraskaDomain domain) {
		this(path, domain, false);
	}
	
	public NebraskaReader (String path) {
		this(path, NebraskaDomain.All, true);
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
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		List<Tweet> ret = new ArrayList<Tweet>();
		StringBuilder inBuilder = new StringBuilder();
		boolean first = true;
		if (this.domain == NebraskaDomain.All) {
			stmt = conn.prepareStatement("SELECT identifier, document, date FROM input WHERE date IS NOT NULL");
		}
		else {
			List<Integer> domainLabels = this.getDomainList();
			for (@SuppressWarnings("unused") int label : domainLabels) {
				if (first) first = false;
				else inBuilder.append(',');
				inBuilder.append('?');
			}
			stmt = conn.prepareStatement("SELECT DISTINCT identifier, document, date FROM input "
					+ "WHERE identifier IN ("
					+ "SELECT DISTINCT document_identifier FROM label_amt "
					+ "WHERE label IN ("+inBuilder.toString()+")) AND date IS NOT NULL");
			
			for (int i = 0; i < domainLabels.size(); i++) {
				int label = domainLabels.get(i);
				stmt.setInt(i+1, label);
			}
		}
		
		// Phase 1 is to read all the tweets from the database
		rs = stmt.executeQuery();
		while (rs.next()) {
			int documentIdentifier = rs.getInt(1);
			String documentString  = rs.getString(2);
			String documentDate    = rs.getString(3);
			Date dt = sdf.parse(documentDate);
			Tweet cur = new Tweet(documentString, 0, documentIdentifier);
			cur.setDate(dt);
			ret.add(cur);
		}
		
		// If we don't have these tables...
		if (this.skipReadingAnnotations) return ret;
		
		// Phase 2 is to synthesize consensus annotations
		for (Tweet t : ret) {
			MultiAnnotationMap mam = new MultiAnnotationMap();
			PreparedStatement fetchSubStmt = conn.prepareStatement(
					"SELECT annotation, sentiment FROM subphrases WHERE document_identifier = ?"
				);
			fetchSubStmt.setInt(1, t.getId2());
			rs = fetchSubStmt.executeQuery();
			int votes[] = new int[3];
			for (int i = 0; i < 3; i++) votes[i] = 0;
			while (rs.next()) {
				List<AnnotationSpan> spans = AnnotationSpan.createSpansFromString(rs.getString(1));
				mam.addAll(spans);
				String sentiment = rs.getString(2);
				if (sentiment.equals("neutral")) {
					votes[0]++;
				}
				if (sentiment.equals("negative")) {
					votes[1]++;
				}
				if (sentiment.equals("positive")) {
					votes[2]++;
				}
			}
			// Decide maximum annotation
			int maxAnnotation = -1;
			int maxVal = 0;
			for (int i = 0; i < 3; i++) {
				if (votes[i] > maxVal) {
					maxVal = votes[i];
					maxAnnotation = i;
				}
			}
			switch(maxAnnotation) {
			case 0:
				t.setAnnotation(new AnnotationType(AnnotationKind.Neutral));
				break;
			case 1:
				t.setAnnotation(new AnnotationType(AnnotationKind.Negative));
				break;
			case 2:
				t.setAnnotation(new AnnotationType(AnnotationKind.Positive));
				break;
			default:
				throw new Exception("What am I doing here?");
			}
			t.setAnnotations(mam);
		}
		
		return ret;
	}

}
