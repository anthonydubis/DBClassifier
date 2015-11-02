
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
	
	public static JSONObject queryBing(String key, String host, String query, int top) throws IOException {
		/* Setup query URL */
		query = query.replaceAll(" ", "%20");
		String urlPattern = "https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Composite?Query=%%27site%%3a%s%%20%s%%27&$top=%s&$format=JSON";
		String bingUrl = String.format(urlPattern, host, query, top);
		/* Setup account key */
		byte[] accountKeyBytes = Base64.encodeBase64((key + ":" + key).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		URL url = new URL(bingUrl);
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

		JSONObject results = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));	
		try {
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			final JSONObject json = new JSONObject(response.toString());
			results = json.getJSONObject("d").getJSONArray("results").getJSONObject(0);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			in.close();
		}

		return results;
	}

	public static int getNumDocs(String key, String host, String query) throws IOException, JSONException {
		JSONObject metadata = queryBing(key, host, query, 10);
		return metadata.getInt("WebTotal");
	}
	
	public static String[] getTopDocs(String key, String host, String query) throws JSONException, IOException {
		JSONObject data = queryBing(key, host, query, 4);
		JSONArray results = data.getJSONArray("Web");
		String[] docs = new String[results.length() + 1];
		int i;
		for (i=0; i<results.length(); i++) {
			JSONObject json = results.getJSONObject(i);
			docs[i] = json.get("Url").toString();
			System.out.println("Getting page " + json.get("Url").toString() + "\n");
		}
		docs[i] = (String) data.get("WebTotal");
		return docs;
	}
}