import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

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
		int counter = 1;
		while (line != null)   {
			int queryStart = line.indexOf(" ") + 1;
			String query = line.substring(queryStart);
			samples.addAll(Utils.getTopDocs(key, host, query));
			System.out.println("Round " + counter);
			counter++;
			line = br.readLine();
		}
		br.close();
		return samples;
	}
	
	public void writeSummary(TreeMap<String, Float> frequencies, String host, String classification) throws IOException {
		String filename = "%s-%s.txt";
		filename = String.format(filename, classification, host);
		FileWriter summary = new FileWriter(filename);
		for (String word : frequencies.keySet()) {
			summary.write(word + " " + frequencies.get(word) + "\n");
		}
		summary.close();
	}
	
	public void summarize(String key, String host, String classification) throws IOException, JSONException {
		Set<String> samples = sample(key, host, classification);
		System.out.println("Sample size: " + samples.size());
		TreeMap<String, Float> frequencies = new TreeMap<String, Float>();
		float n = (float) samples.size();
		float frequency;
		for (String sample : samples) {
			Set<String> words = getWordsLynx.runLynx(sample);
			for (String word : words) {
				if (frequencies.containsKey(word)) {
					frequency = frequencies.get(word) + (1/n);
				} else {
					frequency = (1/n);
				}
				frequencies.put(word, frequency);
			}
		}
		writeSummary(frequencies, host, classification);
	}

	public void buildSummaries(String key, String host, int t_ec, float t_es) throws IOException, JSONException {
		classifier = new Classifier(key, host);
		// For now I am not running the classifier, just doing part 2.
		//String[] classes = classifier.classifyDB(t_ec, t_es).split("/");
		String[] classes = {"Root"};
		for (int i=0; i<classes.length; i++) {
			System.out.println("Classification: " + classes[i]);
			summarize(key, host, classes[i]);
		}
	}
}
