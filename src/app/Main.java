package app;

import java.io.IOException;
import java.util.Scanner;


public class Main {
	public static void main(String[] args) {
		try {
			GeoTranslator translator = GeoTranslator.load("data/words.dat","data/shis.dat", "data/chis.dat", "data/dzis.dat", "data/exceptions.dat");
			Scanner scanner = new Scanner(System.in);
			while(true){
				String line = scanner.nextLine();
				if(line.length() == 0) break;
				System.out.println(translator.latToGeo(line));
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
