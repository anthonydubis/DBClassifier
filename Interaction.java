
public class Interaction {
	public static void main (String[] args) {
		String key = args[0];
		float t_es = Float.valueOf(args[1]);
		int t_ec = Integer.valueOf(args[2]);
		String host = args[3];
		
		System.out.println("Key: " + key + "\nt_es: " + t_es + "\nt_ec: " + t_ec + "\nhost: " + host);
	}
}
