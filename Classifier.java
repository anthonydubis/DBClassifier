import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
		/* The parent node (category) of this node */
		Node parent;
		/* Sub-categories, null if this is a leaf node */
		Node[] children;
		/* Queries, null if this is a leaf node */
		String queriesFile;
		/* The number of web results, -1 indicates not set */
		int coverage;
		
		Node(String category, String file, Node[] subcategories)
		{
			this.category = category;
			this.queriesFile = file;
			this.children = subcategories;
			this.coverage = -1;
		}
	}
	
	public Classifier(String host, String key) 
	{
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
		
		/* Set the parent pointers */
		computers.parent = health.parent = sports.parent = root;
		programming.parent = hardware.parent = computers;
		fitness.parent = diseases.parent = health;
		basketball.parent = soccer.parent = sports;
	}
	
	/*
	 * Returns true if C (a category) is a leaf node.
	 */
	private boolean isLeafNode(Node node)
	{
		return node.children == null;
	}
	
	/*
	 * Sets the coverage of the child node by using the parent nodes query file.
	 * Returns the coverage that was set.
	 */
	private int computeCoverage(Node parent, Node child) throws IOException, JSONException
	{
		if (child.coverage >= 0)
			return child.coverage;
		
		child.coverage = 0;
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
			
			child.coverage += webCounts.get(query);
		}
		br.close();
		
		return child.coverage;
	}
	
	/*
	 * Following the algorithm as outline in Fig. 4
	 */
	private void classify(ArrayList<Node> qualifyingNodes, Node node, String host, int t_ec, double t_es, double parentSpecificity) throws IOException, JSONException
	{	
		qualifyingNodes.add(node);
		
		/* Remove parent because this is this node provides the most information */
		if (node.parent != null)
			qualifyingNodes.remove(node.parent);
		
		/* If this is a leaf node, return as it has no children to add */
		if (isLeafNode(node)) {
			return;
		}
		
		/* Get the number of matches for each of the children categories */
		double numDocs = 0;
		for (Node child : node.children) {
			numDocs += (double)computeCoverage(node, child);
			System.out.println("Coverage for " + child.category + ": " + child.coverage);
		}
		
		/* No matches for any of the children */
		if (numDocs == 0)
			return;
		
		/* Dive into sub-categories that meet criteria */
		for (Node child : node.children) {
			double childSpecificity = (parentSpecificity * child.coverage) / numDocs;
			System.out.println("Specificity for " + child.category + ": " + childSpecificity);
			if (childSpecificity >= t_es && child.coverage >= t_ec) {
				classify(qualifyingNodes, child, host, t_ec, t_es, childSpecificity);
			}
		}
	}
	
	private String[] getClassifications(ArrayList<Node> qualifyingNodes) 
	{
		String[] classifications = new String[qualifyingNodes.size()];
		
		for (int i = 0; i < qualifyingNodes.size(); i++) {
			Node n = qualifyingNodes.get(i);
			String result = n.category;
			while (n.parent != null) {
				n = n.parent;
				result = n.category + "/" + result;
			}
			classifications[i] = result;
		}
		
		return classifications;
	}

	public String[] classifyDB(int t_ec, double t_es) throws IOException, JSONException 
	{
		System.out.println("\nRelevant Coverages and Specificities:");
		ArrayList<Node> qualifyingNodes = new ArrayList<Node>();
		classify(qualifyingNodes, root, host, t_ec, t_es, 1);
		String[] classifications = getClassifications(qualifyingNodes);
		
		System.out.print("\n\n");
		System.out.println("Classification(s):");
		for (String s : classifications)
			System.out.println(s);

		return classifications;
	}
}
