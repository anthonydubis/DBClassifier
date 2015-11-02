import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;

public class Summarizer {

	private String host;
	private String key;
	private Classifier classifier;
	private HashMap<String, String> files;
	private TreeMap<String, Integer> frequencies;
	private TreeMap<String, Integer> matches;
	private Set<String> samples;

	public Summarizer(String host, String key) {
		this.host = host;
		this.key = key;
		// files references the category's query probe file.
		files = new HashMap<String, String>();
		files.put("Root", "root.txt");
		files.put("Computers", "computers.txt");
		files.put("Health", "health.txt");
		files.put("Sports", "sports.txt");
	}

	public void addFrequencyAndMatches(String url) {
		Set<String> words = getWordsLynx.runLynx(url);
		int frequency;
		int match;
		for (String word : words) {
			frequency = 1;
			match = -1;
			if (frequencies.containsKey(word)) {
				frequency = frequencies.get(word) + 1;
				match = matches.get(word);
			}
			frequencies.put(word, frequency);
			matches.put(word, match);
		}
	}

	// For each query, get 4 top docs and if docs are new, run lynx and add word frequencies.
	public void sampleAndSummarize(String classification) throws IOException, JSONException {
		FileInputStream fstream = new FileInputStream(files.get(classification));
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String line = br.readLine();
		int counter = 1;
		System.out.println("\n\nQuerying for classification " + classification);
		while (line != null)   {
			int queryStart = line.indexOf(" ");
			String query = line.substring(queryStart).trim();
			System.out.println("Round " + counter + ":" + query);
			TopK topK = Utils.getTopDocs(key, host, query);
			for (int i=0; i<topK.k; i++) {
				if (!samples.contains(topK.urls[i])) {
					addFrequencyAndMatches(topK.urls[i]);
					samples.add(topK.urls[i]);
				}
			}
			// Query probe frequency + matches. TODO: function 
			frequencies.put(query, topK.k);
			matches.put(query, topK.webtotal);
			counter++;
			line = br.readLine();
		}
		br.close();
	}

	public void writeSummary(String classification) throws IOException {
		String filename = "%s-%s.txt";
		filename = String.format(filename, classification, host);
		FileWriter summary = new FileWriter(filename);
		for (String word : frequencies.keySet()) {
			summary.write(word + "#" + frequencies.get(word) + "#" + matches.get(word) + "\n");
		}
		summary.close();
	}

	public void restart() {
		frequencies = new TreeMap<String, Integer>();
		matches = new TreeMap<String, Integer>();
		samples = new HashSet<String>();
	}

	public void buildSummaries(int t_ec, float t_es) throws IOException, JSONException {
		classifier = new Classifier(host, key);
		String[] classifications = classifier.classifyDB(t_ec, t_es);
		for (int j=0; j<classifications.length; j++) {
			restart();
			String[] categories = classifications[j].split("/");
			// Ignore leaf nodes.
			int n = Math.min(1, (categories.length-1));
			for (int i=n; i>=0; i--) {
				System.out.println("Classification: " + categories[i]);
				sampleAndSummarize(categories[i]);
				writeSummary(categories[i]);
			}
		}
	}
}
