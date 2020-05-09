## TranslationFileGenerator

Lors de l'expansion d'un programme au niveau multinational, vous aurez surement besoin de supporter différents langages. Au lieu d'entrer votre texte dans des guillemets (``"Bienvenue sur mon application"``), vous allez probablement appeler une méthode de traduction, pour traduire le texte dans une autre langue si nécessaire (``Translator.translate("Bienvenue sur mon application")``) ou, vous allez peut-être renseigner une clé (``Translator.translate("welcome.base")``).

**Si vous avez choisi cette méthode de traduction, vous pouvez vous poser la question de comment générer le fichier de traduction, qui donnera une valeur à chacune des clés ?**

Une des solutions est de rechercher touts les appels à ``Translator.translate`` avec votre IDE, puis de reporter les textes dans un fichier.
Mais TranslationFileGenerator vous propose bien plus efficace, vous lui renseignez la méthode à rechercher puis il vous crée le fichier, qui ressemblera à ceci : ``Bienvenue sur mon application=Welcome to my app``.

TranslationFileGenerator supporte aussi la modification du code, il s'occupe de fusionner l'ancien fichier de traduction avec le code : il rajoute des lignes de traduction pour les nouveaux appels à la méthode et retire les lignes de traductions si elles ne sont plus appelés dans le code.

TranslationFileGenerator a été utiliser pour générer les fichiers de traduction de [PDF4Teachers](https://github.com/themsou/PDF4Teachers/)

## Comment utiliser TranslationFileGenerator

TranslationFileGenerator n'est pas livré compilé, c'est à vous de l'exécuter avec gradle. Vous devez télécharger le projet puis exécuter ``./gradlew run`` en bash ou ``gradlew.bat run`` en batch (Avec Java d'installé).

Toute la configuration se fait dans la classe principale (``src/main/java/fr/themsou/translationFileGenerator/Main.java``), vous devrez paramétrer les variables de la "Settings zone".

Ci-dessous les variables que vous aurez à paramétrer:
<pre>
// Use this option if you want to update an already translated file,
// we will add to this file the new translations gets in the code,
// and remove the unused translations.
// Use false if you want to disable this option.
boolean READ_EXISTING_FILE = true;
// Path to the already translated file
String ALREADY_TRANSLATED_FILE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\resources\\translations\\English US.txt";

// The code where we have to get translations
final String CODE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\java\\fr\\themsou";

// The output file, with all translations
final String OUT_FILE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\template.txt";

// Prefix and suffix of the method that we have to catch
// Here, the method are TR.tr("key string")
final String PREFIX = "TR.tr(\"";
final String SUFFIX = "\")";

// Separator between the key and value in the translation file
// Keep only one char for your security
final String SEPARATOR = "=";
</pre>

Lors de son exécution, vous pourrez obtenir des logs de ce type : 

<pre>
> -------------------------
> -> Reading already translated file...
> -------------------------
> -------------------------
> -> Reading code files...
> -------------------------
> -------------------------
> -> Writing...
> -------------------------
> Add translation line to : Barème déjà présent
> Add translation line to : L'édition du fichier
> Add translation line to : contient déjà un barème
> Add translation line to : PDF4Teachers va essayer de récupérer les notes de l'ancien barème pour les ajouter au nouveau barème.
> Add translation line to : Vous serez avertis si une note va être écrasée.
> Add translation line to : Toujours continuer
> Add translation line to : Tout annuler
> Add translation line to : Écraser les notes non correspondantes
> Add translation line to : Aucune note du nouveau barème ne correspond à :
> Add translation line to : Dans le document
> Add translation line to : Arrêter
> Add translation line to : Tout arrêter
> Remove existing translation from file (unused) : - Charger remplacera la liste des éléments favoris par celle ci=Loading will replace the favorite elements list with this one
> Add translation line to : - Vider et charger remplacera la liste des éléments favoris par celle ci
> Add translation line to : - Charger ajoutera cette liste d'éléments à la liste des éléments favoris
> Add translation line to : Vider et charger
> -------------------------
> -> COMPLETED !
>    15 lines added
>    1 lines removed
>    299 lines writed (353 total)
>    54 class name lines (don't remove it please)
> -------------------------
</pre>