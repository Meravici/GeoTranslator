package app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * @author Xatoo
 *
 */
public class GeoTranslator {
	private static final char[] GEORGIAN_ALPHABET = new char[]{'ა','ბ','გ','დ','ე','ვ','ზ','თ','ი','კ','ლ','მ','ნ','ო','პ','ჟ','რ','ს','ტ','უ','ფ','ქ','ღ','ყ','შ','ჩ','ც','ძ','წ','ჭ','ხ','ჯ','ჰ'};
	private static final char[] LATIN_ALPHABET =    new char[]{'a','b','g','d','e','v','z','T','i','k','l','m','n','o','p','J','r','s','t','u','f','q','R','y','S','C','c','Z','w','W','x','j','h'};
	private static final Set<Character> BIG_CHARS;
	static{
		BIG_CHARS = new HashSet<Character>();
		
		BIG_CHARS.add('T');
		BIG_CHARS.add('J');
		BIG_CHARS.add('R');
		BIG_CHARS.add('S');
		BIG_CHARS.add('C');
		BIG_CHARS.add('Z');
		BIG_CHARS.add('W');
	}
	
	private static final String[] PROBLEMATIC_ELEMENTS = new String[]{"sh","ch","dz","g", "t", "zh", "j", "kh"};
	private Map<Character, Character> latinMapping;
	private Map<Character, Character> georgianMapping;
	private StringMatcher stringMatcher;
	
	private static final int SH = 0;
	private static final int CH = 1;
	private static final int DZ = 2;
	private static final int G = 3;
	private static final int T = 4;
	private static final int ZH = 5;
	private static final int J = 6;
	private static final int KH = 7;
		
	private Map<String, String> exceptions;
	
	
	/** Creates new GeoTranslator object and load given lexicons
	 * @param words lexicon of georgian words (mainly used for ditiguishing თ and ტ, გ and ღ, and so on). can be optimized by giving only critical words
	 * @param shis lexicon of georgian words containing sh as სჰ  not შ
	 * @param chis lexicon of georgian words containing сh as ცჰ  not ჩ
	 * @param dzis lexicon of georgian words containing dz as დზ not ძ
	 * @param exceptions lexicon of user defined exceptions
	 * @return new GeoTranslator object
	 * @throws IOException
	 */
	public static GeoTranslator load(String words, String shis, String chis, String dzis,
			String exceptions) throws IOException {
		return load(new FileInputStream(words), new FileReader(shis), new FileReader(chis), new FileReader(
				dzis), new FileReader(exceptions));
	}

	/** Creates new GeoTranslator object and load given lexicons
	 * @param words lexicon of georgian words (mainly used for ditiguishing თ and ტ, გ and ღ, and so on). can be optimized by giving only critical words
	 * @param shis lexicon of georgian words containing sh as სჰ  not შ
	 * @param chis lexicon of georgian words containing сh as ცჰ  not ჩ
	 * @param dzis lexicon of georgian words containing dz as დზ not ძ
	 * @param exceptions lexicon of user defined exceptions
	 * @return new GeoTranslator object
	 * @throws IOException
	 */
	public static GeoTranslator load(File words, File shis, File chis, File dzis,
			File exceptions) throws IOException {
		return load(new FileInputStream(words), new FileReader(shis), new FileReader(chis), new FileReader(
				dzis), new FileReader(exceptions));
	}

	/** Creates new GeoTranslator object and load given lexicons
	 * @param words lexicon of georgian words (mainly used for ditiguishing თ and ტ, გ and ღ, and so on). can be optimized by giving only critical words
	 * @param shis lexicon of georgian words containing sh as სჰ  not შ
	 * @param chis lexicon of georgian words containing сh as ცჰ  not ჩ
	 * @param dzis lexicon of georgian words containing dz as დზ not ძ
	 * @param exceptions lexicon of user defined exceptions
	 * @return new GeoTranslator object
	 * @throws IOException
	 */
	public static GeoTranslator load(InputStream words, InputStream shis, InputStream chis,
			InputStream dzis, InputStream exceptions) throws IOException {
		return load(words, new InputStreamReader(shis), new InputStreamReader(chis),
				new InputStreamReader(dzis), new InputStreamReader(exceptions));
	}

	/** Creates new GeoTranslator object and load given lexicons
	 * @param words lexicon of georgian words (mainly used for ditiguishing თ and ტ, გ and ღ, and so on). can be optimized by giving only critical words
	 * @param shis lexicon of georgian words containing sh as სჰ  not შ
	 * @param chis lexicon of georgian words containing сh as ცჰ  not ჩ
	 * @param dzis lexicon of georgian words containing dz as დზ not ძ
	 * @param exceptions lexicon of user defined exceptions
	 * @return new GeoTranslator object
	 * @throws IOException
	 */
	public static GeoTranslator load(InputStream words, Reader shis, Reader chis, Reader dzis,
			Reader exeptions) throws IOException {
		GeoTranslator instance = new GeoTranslator(words, shis, chis, dzis, exeptions);
		
		words.close();
		shis.close();
		chis.close();
		dzis.close();
		exeptions.close();
		
		return instance;
	}
	
	private ProblemSolver resolver;

	private GeoTranslator(InputStream words, Reader shis, Reader chis, Reader dzis, Reader eis) throws IOException{
		resolver = new ProblemSolver(words, shis, chis, dzis);
		latinMapping = new HashMap<Character, Character>();
		georgianMapping = new HashMap<Character, Character>();
		exceptions = new HashMap<String, String>();
		for(int i=0; i<GEORGIAN_ALPHABET.length; i++){
			latinMapping.put(LATIN_ALPHABET[i], GEORGIAN_ALPHABET[i]);
			georgianMapping.put(GEORGIAN_ALPHABET[i], LATIN_ALPHABET[i]);
		}
		
		stringMatcher = new StringMatcher();
		
		for(int i=0; i<PROBLEMATIC_ELEMENTS.length; i++)
			stringMatcher.addString(PROBLEMATIC_ELEMENTS[i], i);
		
		loadExceptions(eis);
	}
	
	/** Converts passed Latin encoded georgian string to corresponding unicode text
	 * @param text 
	 * @return unicode encoded string
	 */
	public String latToGeo(String text){
		if(text.length()==0) return text;
		if(exceptions.containsKey(text)) return exceptions.get(text);
		if(!BIG_CHARS.contains(text.charAt(0)))
			text = Character.toLowerCase(text.charAt(0)) + text.substring(1, text.length());
		
		String returnString = "";
		
		StringTokenizer st = new StringTokenizer(text);
		while(st.hasMoreTokens()){
			String word = st.nextToken();
			word = resolveProblematicElements(word);
			word = translateGeo(word);
			returnString += word + " ";
		}
		return returnString;
	}	
	
	/** Converts passed unicode encoded georgian string to corresponding Latin text
	 * @param text
	 * @return Latin encoded string
	 */
	public String geoToLat(String text){
		String newLine = "";
		for(int i=0; i<text.length(); i++){
			char c = text.charAt(i);
			if(georgianMapping.containsKey(c))
				newLine += georgianMapping.get(c);
			else
				newLine += c;
		}
		return newLine;
	}
	
	/** Add new user defined exception, always traslate given latin string to given unicode striong
	 * @param latin 
	 * @param georgian
	 */
	public void addException(String latin, String georgian) {
		exceptions.put(latin, georgian);
	}

	/** Remove exception
	 * @param latin
	 */
	public void removeException(String latin) {
		exceptions.remove(latin);
	}	
	
	private void loadExceptions(Reader eis) throws IOException {
		BufferedReader br = new BufferedReader(eis);
		String line;
		while((line = br.readLine()) != null){
			String[] elements = line.split(" ");
			exceptions.put(elements[0], elements[1]);
		}
	}
	
	private String resolveProblematicElements(String line) {
		for(int i=0; i<PROBLEMATIC_ELEMENTS.length; i++){
			int index;
			int start = 0;
			while((index = line.indexOf(PROBLEMATIC_ELEMENTS[i], start))!= -1){
				if(index != -1){
					start = index + PROBLEMATIC_ELEMENTS[i].length();
					String replacement = resolveProblem(line, stringMatcher.match(PROBLEMATIC_ELEMENTS[i]), index);
					if(replacement != null){
						String before = line.substring(0, index);
						String after = line.substring(index + PROBLEMATIC_ELEMENTS[i].length(), line.length());
						line = before + replacement + after;
					}
				}
			}
		}
		return line;
	}
	
	private String resolveProblem(String line, int match, int index){
		switch(match){
		case SH:
			return resolver.SHSolve(line);
		case CH:
			return resolver.CHSolve(line, index);
		case ZH:
			return "J";
		case DZ:
			return resolver.DZSolve(line);
		case G:
			return resolver.GSolve(line, index);
		case T:
			return resolver.TSolve(line, index);
		case J:
			return resolver.JSolve(line);
		case KH:
			return resolver.KHSolve(line);
		}
		return line;
	}
	
	private String translateGeo(String line){
		String newLine = "";
		for(int i=0; i<line.length(); i++){
			char c = line.charAt(i);
			if(latinMapping.containsKey(c))
				newLine += latinMapping.get(c);
			else
				newLine += c;
		}
		return newLine;
	}
}
