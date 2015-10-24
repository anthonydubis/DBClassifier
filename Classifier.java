import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.json.JSONException;


public class Classifier {
	private String host;
	private String key;
	private HashMap<String, String> files;
	
	public Classifier(String host, String key) {
		this.host = host;
		this.key = key;
		files = new HashMap<String, String>();
		files.put("Root", "root.txt");
		files.put("Computers", "computers.txt");
		files.put("Health", "health.txt");
		files.put("Sports", "sports.txt");
	}
	
	private HashMap<String, Integer> getCoverage(String classification) throws IOException, JSONException {
		HashMap<String, Integer> coverages = new HashMap<String, Integer>();
		FileInputStream fstream = new FileInputStream(files.get(classification));
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String line = br.readLine();
		String db = line.split(" ")[0];
		int coverage = 0;
		while (line != null)   {
			// may need to treat query
			String query = line.split(" ")[1];
			if (db.equals(line.split(" ")[0])) {
				coverage += Utils.getNumDocs(key, host, query);
			} else {
				coverages.put(db, coverage);
				System.out.println(db);
				System.out.println(coverage);
				db = line.split(" ")[0];
				coverage = Utils.getNumDocs(key, host, query);
			}
			line = br.readLine();
		}
		br.close();
		return coverages;
	}

	public String classifyDB(int t_ec, float t_es) throws IOException, JSONException {
		StringBuilder classification = new StringBuilder("Root/");
		HashMap<String, Integer> coverages = getCoverage("Root");
		// For specificity, ignore for now until defining select * query
		//int n = Utils.getNumDocs(key, host, "");
		int maxCoverage = 0;
		String candidate = "";
		for (String db : coverages.keySet()) {
			// Only with coverages for now
			//float dbSpecificity = dbCoverages.get(db) / n;
			if (coverages.get(db) >= t_ec) {
				candidate = db;
				HashMap<String, Integer> subDbCoverages = getCoverage(db);
				for (String subDb : subDbCoverages.keySet()) {
					if (subDbCoverages.get(subDb) >= t_ec && subDbCoverages.get(subDb) > maxCoverage) {
						candidate = db + "/" + subDb;
						maxCoverage = subDbCoverages.get(subDb);
					}
				}
			}
		}
		classification.append(candidate);
		return classification.toString();
	}

}
