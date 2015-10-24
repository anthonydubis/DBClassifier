
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

	public static int getNumDocs(String key, String host, String query) throws IOException, JSONException {
		int numDocs = 0;
		
		/* Setup query URL */
		query = query.replaceAll(" ", "%20");
		String site = "site%3a" + host;
		String bingUrl = "https://api.datamarket.azure.com/Data.ashx/Bing/SearchWeb/v1/Composite?Query=%27" + site + "%20" + query + "%27&$top=10&$format=JSON";

		/* Setup account key */
		byte[] accountKeyBytes = Base64.encodeBase64((key + ":" + key).getBytes());
		String accountKeyEnc = new String(accountKeyBytes);

		URL url = new URL(bingUrl);
		URLConnection connection = url.openConnection();
		connection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

		JSONArray jsonArr = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));	
		try {
			String line;
			StringBuilder response = new StringBuilder();
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			final JSONObject json = new JSONObject(response.toString());
			jsonArr = json.getJSONObject("d").getJSONArray("results");
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
		
		JSONObject json = jsonArr.getJSONObject(0);
		numDocs = json.getInt("WebTotal"); 
		return numDocs;
	}
}