## TranslationFileGenerator

Lors de l'expansion d'un programme au niveau multinational, vous aurez surement besoin de supporter différents langages. Au lieu d'entrer votre texte dans des guillemets (``"Bienvenue sur mon application"``), vous allez probablement appeler une méthode de traduction, pour traduire le texte dans une autre langue si nécessaire (``Translator.translate("Bienvenue sur mon application")``) ou, vous allez peut-être utiliser une clé (``Translator.translate("welcome.base")``).

**Si vous avez choisi cette méthode de traduction, vous pouvez vous poser la question de comment générer le fichier de traduction, qui donnera une valeur à chacune des clés ?**

Une des solutions est de rechercher touts les appels à ``Translator.translate`` avec votre IDE, puis de reporter les textes dans un fichier.
Mais TranslationFileGenerator vous propose une méthode bien plus efficace, vous lui renseignez la méthode à rechercher puis il vous crée le fichier, qui ressemblera à ceci : ``Bienvenue sur mon application=Welcome to my app``.

TranslationFileGenerator supporte aussi la modification du code, il s'occupe de fusionner l'ancien fichier de traduction avec le code : il peut reprendre les traductions d'un fichier existant lors de la génération d'un nouveau fichier de traduction.

TranslationFileGenerator a été utiliser pour générer les fichiers de traduction de [PDF4Teachers](https://github.com/themsou/PDF4Teachers/)

## Comment utiliser TranslationFileGenerator

TranslationFileGenerator n'est pas livré compilé, c'est à vous de l'exécuter avec gradle. Vous devez télécharger le projet puis exécuter ``./gradlew run`` en bash ou ``gradlew.bat run`` en batch (Avec Java 14 d'installé (JDK 14 + JAVA_HOME)).

Toute la configuration se fait dans la classe principale (``src/main/java/fr/themsou/translationFileGenerator/Main.java``), vous devrez paramétrer les variables de la "Settings zone".

Ci-dessous les variables que vous aurez à paramétrer:
```
// Use this option if you want to add automatically the translations contained in another already translated file,
// this could be helpful if you want to update an already translated file.
// Use false if you want to disable this option.
public static final boolean READ_EXISTING_FILE = true;
// Path to the already translated file
public static final String ALREADY_TRANSLATED_FILE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\resources\\translations\\en-us.txt";

// The code where we have to get translations
public static final String CODE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\java\\fr\\clementgre";

// The output file, with all translations
public static final String OUT_FILE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\resources\\translations\\en-us-new.txt";

// Each of the arguments of Arrays.asList is an extension which will be authorized to be read.
// If you wrote your code in Java, you can keep .java. If your code is in JavaScript, replace .java by .js.
// The case is not taken in account.
public static final List<String> ACCEPTED_EXTENSIONS = Arrays.asList(".java");

// Prefix and suffix of the methods that we have to catch
// Theses prefix/suffix are stored as a Map (a list of key/value pairs). Map.of is generating this map with the impair args as a key and the pair args as a value. Here, we have 2 pairs.
// Here, the method are TR.tr("key string") or TR.ct("key string")
public static final Map<String, String> PREFIXES_SUFFIXES = Map.of("TR.tr(\"", "\")", "TR.ct(\"", "\")");

// Separator between the key and value in the translation file
// Keep only one char for your security
public static final String SEPARATOR = "=";
```

Lors de son exécution, vous pourrez obtenir des logs de ce type : 

```
-------------------------
-> Reading already translated file...
-------------------------
-> Stored 384 already existing translations

-------------------------
-> Reading code files...
-------------------------
-> Caught 513 translations keys in 103 files.

-------------------------
-> Writing...
-------------------------
-> COMPLETED !
   142 new empty translations
   371 old translations reused
   13 old translations not reused
   513 lines wrote (559 total)
   46 file name lines
```