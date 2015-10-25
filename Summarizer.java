import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;

public class Summarizer {
	
	private Classifier classifier;
	
	// Put top 4 docs per query in a set of urls so no repetition
	public Set<String> sample(String key, String host, String classification) throws IOException, JSONException {
		Set<String> samples = new HashSet<String>();
		FileInputStream fstream = new FileInputStream(classifier.files.get(classification));
		BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
		String line = br.readLine();
		int coverage = 0;
		while (line != null)   {
			int queryStart = line.indexOf(" ") + 1;
			String query = line.substring(queryStart);
			samples.addAll(Utils.getTopDocs(key, host, query));
		}
		br.close();
		return samples;
	}
	
	public void writeSummary(HashMap<String, Float> frequencies, String host, String classification) throws IOException {
		String filename = "%s-%s.txt";
		filename = String.format(filename, classification, host);
		FileWriter summary = new FileWriter(filename);
		for (String word : frequencies.keySet()) {
			summary.write(word + " " + frequencies.get(word));
		}
		summary.close();
	}
	
	public void summarize(String key, String host, String classification) throws IOException, JSONException {
		Set<String> samples = sample(key, host, classification);
		System.out.println(samples.size());
		HashMap<String, Float> frequencies = new HashMap<String, Float>();
		int n = samples.size();
		float frequency;
		for (String sample : samples) {
			Set<String> words = getWordsLynx.runLynx(sample);
			for (String word : words) {
				frequency = frequencies.containsKey(word) ? (1/n) : frequencies.get(word) + (1/n);
			}
		}
		writeSummary(frequencies, host, classification);
	}

	public void buildSummaries(String key, String host, int t_ec, float t_es) throws IOException, JSONException {
		classifier = new Classifier(key, host);
		//String[] classes = classifier.classifyDB(t_ec, t_es).split("/");
		String[] classes = {"Root"};
		for (int i=0; i<classes.length; i++) {
			summarize(key, host, classes[i]);
		}
	}
}
