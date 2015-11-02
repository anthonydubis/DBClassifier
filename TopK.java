import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Stores top document urls and webtotal for a bing query
 */
public class TopK {
	
	protected int k;
	protected int webtotal; 
	protected String[] urls;
	
	public TopK(JSONObject data) throws JSONException {
		JSONArray results = data.getJSONArray("Web");
		urls = new String[results.length()];
		k = urls.length;
		webtotal = data.getInt("WebTotal");
		for (int i=0; i<results.length(); i++) {
			JSONObject json = results.getJSONObject(i);
			urls[i] = json.get("Url").toString();
		}
	}

}
