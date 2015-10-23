import java.io.IOException;

import org.json.JSONException;

public class Interaction {
	public static void main (String[] args) throws IOException, JSONException {
		String key = args[0];
		float t_es = Float.valueOf(args[1]);
		int t_ec = Integer.valueOf(args[2]);
		String host = args[3];
		
		System.out.println("Key: " + key + "\nt_es: " + t_es + "\nt_ec: " + t_ec + "\nhost: " + host);
		
		int n = Utils.getNumDocs(key, host, "cancer");
		System.out.println("Number of docs: " + n);
	}
}
