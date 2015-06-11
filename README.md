# GeoTranslator
Library for translating Latin encoded Georgian text to unicode and vise versa, has support for user provided lexicons and exclusions

# Usage

``` java
GeoTranslator translator = GeoTranslator.load("words.dat","shis.dat", "chis.dat", "dzis.dat", "exceptions.dat");
Scanner scanner = new Scanner(System.in);
while(true){
	String line = scanner.nextLine();
	if(line.length() == 0) break;
	System.out.println(translator.latToGeo(line));
}
scanner.close();
```



# Credits:
[android-dawg](https://github.com/icantrap/android-dawg)

Georgian word list © Nikoloz Barbaqadze
