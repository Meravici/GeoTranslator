package app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import com.icantrap.collections.dawg.Dawg;

public class ProblemSolver {
	private static final String SH = "sh";
	private static final String S = "S";

	private static final String CH = "ch";
	private static final String C = "C";

	private static final String J = "J";
	private static final String _J = "j";

	private static final String DZ = "dz";
	private static final String Z = "Z";

	private static final String KH = "kh";
	private static final String X = "x";
	
	private static final String W = "W";
	
	private static final String R = "R";
	
	private static final String G = "g";
	
	private static final String T = "T";
	private static final String _T = "t";

	private final Set<String> SHwords;
	private final Set<String> CHwords;
	private final Set<String> DZwords;

	private Dawg dawg;

	public ProblemSolver(InputStream words, Reader shis, Reader chis, Reader dzis) throws IOException {

		SHwords = new HashSet<String>();
		CHwords = new HashSet<String>();
		DZwords = new HashSet<String>();

		loadFile(SHwords, shis);
		loadFile(SHwords, chis);
		loadFile(SHwords, dzis);

		try {
			dawg = Dawg.load(words);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadFile(Set<String> words, Reader is) {
		BufferedReader br = new BufferedReader(is);
		String line;
		try {
			while ((line = br.readLine()) != null)
				words.add(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String SHSolve(String text) {
		return containsDict(text.toLowerCase(), SHwords) ? SH : S;
	}

	public String CHSolve(String text, int index) {
		if(containsDict(text.toLowerCase(), CHwords)) return CH;
		else{
			text = text.substring(0, index) + C + text.substring(index+2, text.length());
			return dawg.contains(text) ? C : W;
		}
	}

	public String ZHSolve(String text) {
		return J;
	}

	public String DZSolve(String text) {
		return containsDict(text.toLowerCase(), DZwords) ? DZ : Z;
	}

	public String GSolve(String text, int index) {
		text = text.substring(0, index) + R
				+ text.substring(index + 1, text.length());
		return dawg.contains(text) ? R : G;
	}

	public String TSolve(String text, int index) {
		text = text.substring(0, index) + T
				+ text.substring(index + 1, text.length());
		return dawg.contains(text) ? T : _T;
	}

	public String JSolve(String text) {
		return dawg.contains(text) ? _J : J;
	}

	public String KHSolve(String text) {
		return text.contains("stokholm") ? KH : X;
	}

	private boolean containsDict(String text, Set<String> dict) {
		for (String word : dict) {
			if (text.toLowerCase().contains(word))
				return true;
		}
		return false;
	}

	public boolean isGeorgian(String line) {
		return dawg.contains(line);
	}

}
