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
	private Node root;
	private Map<String, Integer> webCounts;
	
	public class Node {
		/* The category name - Root, Health, etc. */
		String category;
		/* Sub-categories, null if this is a leaf node */
		Node[] children;
		/* Queries, null if this is a leaf node */
		String queriesFile;
		
		Node(String category, String file, Node[] subcategories)
		{
			this.category = category;
			this.queriesFile = file;
			this.children = subcategories;
		}
	}
	
	public Classifier(String host, String key) {
		this.host = host;
		this.key = key;
		this.webCounts = new HashMap<String, Integer>();
		buildClassificationTree();
	}
	
	/*
	 * Build the tree structure that represents the classification hierarchy
	 */
	private void buildClassificationTree()
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
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(parent.queriesFile)));
		String line = null;
		
		while ((line = br.readLine()) != null) {
			String[] parts = line.split(" ");
			
			/* If this query is not related with this node, skip it */
			if (!parts[0].equalsIgnoreCase(child.category))
				continue;
			
			String query = line.substring(line.indexOf(" "));
			
			/* Caching */
			if (!webCounts.containsKey(query))
				webCounts.put(query, Utils.getNumDocs(key, host, query));
			
			coverage += webCounts.get(query);
		}
		br.close();
		
		return coverage;
	}
	
	/*
	 * Following the algorithm as outline in Fig. 4
	 */
	private String classify(Node node, String host, int t_ec, float t_es, float specificity) throws IOException, JSONException
	{
		String result = "";
		
		/* If this is a leaf node, return the category */
		if (isLeafNode(node))
			return "/" + node.category;
		
		/* Get the number of matches for each of the children categories */
		Map<String, Float> coverages = new HashMap<String, Float>();
		float numDocs = 0;
		for (Node child : node.children) {
			coverages.put(child.category, (float)getCoverage(node, child));
			numDocs += coverages.get(child.category);
			System.out.println("Coverage for " + child.category + ": " + coverages.get(child.category));
		}
		
		/* Dive into sub-categories that meet criteria */
		for (Node child : node.children) {
			specificity = coverages.get(child.category) / numDocs;
			System.out.println("Specificity for " + child.category + ": " + specificity);
			if (specificity >= t_es && coverages.get(child.category) >= t_ec) {
				result += node.category + classify(child, host, t_ec, t_es, specificity);
			}
		}
		
		if (result.equals(""))
			result = node.category;
		
		if (node != root)
			result = "/" + result;

		return result;
	}

	public String[] classifyDB(int t_ec, float t_es) throws IOException, JSONException {
		String classification = classify(root, host, t_ec, t_es, 1);

		return new String[] {classification};
	}

}
