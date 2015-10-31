import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONException;

public class Summarizer {
	
	private String host;
	private String key;
	private Classifier classifier;
	private TreeMap<String, Integer> frequencies;
	private Set<String> samples;
	
	public Summarizer(String host, String key) {
		this.host = host;
		this.key = key;
		samples = new HashSet<String>();
		frequencies = new TreeMap<String, Integer>();
	}
	
	public void addFrequencies(String url) {
		Set<String> words = getWordsLynx.runLynx(url);
		int frequency;
		for (String word : words) {
			frequency = 1;
			if (frequencies.containsKey(word)) {
				frequency = frequencies.get(word) + 1;
			}
			frequencies.put(word, frequency);
		}
	}

	// For each query, get 4 top docs and if docs are new, run lynx and add word frequencies.
	public void sampleAndSummarize(String classification) throws IOException, JSONException {
		FileInputStream fstream = new FileInputStream(classifier.files.get(classification));
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String line = br.readLine();
		int counter = 1;
		System.out.println("\n\nQuerying for classification " + classification);
		while (line != null)   {
			int queryStart = line.indexOf(" ") + 1;
			String query = line.substring(queryStart);
			System.out.println("Round " + counter);
			String[] docs = Utils.getTopDocs(key, host, query);
			for (int i=0; i<docs.length; i++) {
				if (!samples.contains(docs[i])) {
					addFrequencies(docs[i]);
					samples.add(docs[i]);
				}
			}
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
			summary.write(word + " " + frequencies.get(word) + "\n");
		}
		summary.close();
	}

	public void buildSummaries(int t_ec, float t_es) throws IOException, JSONException {
		classifier = new Classifier(key, host);
		// For now I am not running the classifier, just doing part 2.
		//String[] classes = classifier.classifyDB(t_ec, t_es).split("/");
		String[] classes = {"Root","Health"};
		for (int i=(classes.length-1); i>=0; i--) {
			System.out.println("Classification: " + classes[i]);
			sampleAndSummarize(classes[i]);
			writeSummary(classes[i]);
		}
	}
}
