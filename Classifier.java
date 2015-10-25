import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;


public class Classifier {
	private String host;
	private String key;
	
	/* Option 1 */
	public HashMap<String, String> files;
	
	/* Option 2 */
	private Node root;
	
	/* Option 2 */
	public class Node {
		/* The category name - Root, Health, etc. */
		String C;
		/* Sub-categories, null if this is a leaf node */
		Node[] children;
		/* Queries, null if this is a leaf node */
		String queries_file;
		
		Node(String category, String file, Node[] subcategories)
		{
			C = category;
			queries_file = file;
			children = subcategories;
		}
	}
	
	public Classifier(String host, String key) {
		this.host = host;
		this.key = key;
		
		/* Option 1 */
		files = new HashMap<String, String>();
		files.put("Root", "root.txt");
		files.put("Computers", "computers.txt");
		files.put("Health", "health.txt");
		files.put("Sports", "sports.txt");
		
		/* Option 2 - use this.root to access nodes and file names */
		buildClassification();
	}
	
	/* ---------------------------- Option 1 -------------------------------- */
	
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
	
	/* -------------------------------- Option 2 -------------------------------- */
	
	/*
	 * Build the tree structure that represents the classification hierarchy
	 */
	private void buildClassification()
	{
		/* Level 2 Categories - The leaf nodes */
		Node programming = new Node("Programming", null, null);
		Node hardware = new Node("Hardware", null, null);
		Node fitness = new Node("Fitness", null, null);
		Node diseases = new Node("Diseases", null, null);
		Node basketball = new Node("Basketball", null, null);
		Node soccer = new Node("Soccer", null, null);
		
		/* Level 1 Categories */
		Node computers = new Node("Computers", "computers.txt", new Node[] {programming, hardware});
		Node health = new Node("Health", "health.txt", new Node[] {fitness, diseases});
		Node sports = new Node("Sports", "sports.txt", new Node[] {basketball, soccer});
		
		/* Level 0 - the root */
		this.root = new Node("Root", "root.txt", new Node[] {computers, health, sports});
	}
	
	/*
	 * Returns true if C (a category) is a leaf node.
	 */
	private boolean isLeafNode(Node node)
	{
		return node.children == null;
	}
	
	/*
	 * Returns the number of docs that the host contains for the category node
	 * given the probing queries associated with it.
	 */
	private int getCoverage(Node parent, Node child) throws IOException, JSONException
	{
		int coverage = 0;
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(parent.queries_file)));
		String line = null;
		
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			
			/* If this query is not related with this node, skip it */
			if (!parts[0].equalsIgnoreCase(child.C))
				continue;
			
			/* Handle queries with multiple words */
			String query = "";
			for (int i = 1; i < parts.length; i++)
				query = query + parts[i];
			
			coverage += Utils.getNumDocs(key, host, query);
		}
		br.close();
		
		return coverage;
	}
	
	/*
	 * Following the algorithm as outline in Fig. 4
	 */
	private String classify(Node node, String host, int t_ec, float t_es, float e_specificity) throws IOException, JSONException
	{
		String result = "";
		
		/* If this is a leaf node, return the category */
		if (isLeafNode(node))
			return "/" + node.C;
		
		/* Get the number of matches for each of the children categories */
		Map<String, Float> coverages = new HashMap<String, Float>();
		float numDocs = 0;
		for (Node child : node.children) {
			coverages.put(child.C, (float)getCoverage(node, child));
			numDocs += coverages.get(child.C);
			System.out.println("Node: " + child.C + " with matches: " + coverages.get(child.C));
		}
		
		/* Dive into sub-categories that meet criteria */
		for (Node child : node.children) {
			System.out.println("Checking specificity for " + child.C);
			e_specificity = coverages.get(child.C) / numDocs;
			System.out.println("e_specificity equals " + e_specificity + " coverage equals: " + coverages.get(child.C));
			if (e_specificity >= t_es && coverages.get(child.C) >= t_ec) {
				System.out.println("Criteria was met");
				result += node.C + classify(child, host, t_ec, t_es, e_specificity);
			}
		}
		
		if (result.equals(""))
			result = node.C;
		
		if (node != root)
			result = "/" + result;

		return result;
	}

	public String classifyDB(int t_ec, float t_es) throws IOException, JSONException {
		/* Option 2 */
		String ajd_classification = classify(root, host, t_ec, t_es, 1);
		System.out.println("AJD_Classification: " + ajd_classification);
		
		/* Option 1 */
		StringBuilder classification = new StringBuilder("Root");
		HashMap<String, Integer> coverages = getCoverage("Root");

		int maxCoverage = 0;
		String candidate = "";
		for (String db : coverages.keySet()) {
			// Only with coverages for now
			//float dbSpecificity = dbCoverages.get(db) / n;
			if (coverages.get(db) >= t_ec) {
				candidate = "/" + db;
				HashMap<String, Integer> subDbCoverages = getCoverage(db);
				for (String subDb : subDbCoverages.keySet()) {
					if (subDbCoverages.get(subDb) >= t_ec && subDbCoverages.get(subDb) > maxCoverage) {
						candidate = candidate + "/" + subDb;
						maxCoverage = subDbCoverages.get(subDb);
					}
				}
			}
		}
		classification.append(candidate);
		return classification.toString();
	}

}
