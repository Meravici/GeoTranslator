package app;

import java.util.HashMap;
import java.util.Map;

public class StringMatcher {
	
	private Map<String, Integer> mapping = new HashMap<String, Integer>();
	
	
	public void addString(String str, int match){
		mapping.put(str, match);
	}
	
	public int match(String str){
		return mapping.get(str);
	}
	

}
