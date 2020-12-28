package fr.themsou.translationFileGenerator;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Main {

    /////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// SETTINGS ZONE /////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    // Use this option if you want to add automatically the translations contained in another already translated file,
    // this could be helpful if you want to update an already translated file.
    // Use false if you want to disable this option.
    public static final boolean READ_EXISTING_FILE = true;
    // Path to the already translated file
    public static final String ALREADY_TRANSLATED_FILE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\resources\\translations\\it-it.txt";

    // The code where we have to get translations
    public static final String CODE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\java\\fr\\clementgre";

    // The output file, with all translations
    public static final String OUT_FILE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\resources\\translations\\it-it.txt";

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

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    static ArrayList<String> codeKeys = new ArrayList<>();
    static HashMap<String, ArrayList<String>> codeFilesKeys = new HashMap<>();

    static HashMap<String, String> existingTranslations = new HashMap<>();

    static int added = 0;
    static int lines = 0;
    static int commentLines = 0;

    public static void main(String[] args) {

        File in = new File(ALREADY_TRANSLATED_FILE_PATH);
        File dir = new File(CODE_PATH);
        File out = new File(OUT_FILE_PATH);

        if(READ_EXISTING_FILE){
            System.out.println("-------------------------");
            System.out.println("-> Reading already translated file...");
            System.out.println("-------------------------");
            if(in.exists()){
                readExistingFile(in);
                System.out.println("-> Stored " + existingTranslations.size() + " already existing translations");
            }else{
                System.out.println(in.getAbsolutePath() + " does not exist !");
            }
            System.out.println();
        }

        System.out.println("-------------------------");
        System.out.println("-> Reading code files...");
        System.out.println("-------------------------");
        fetchFiles(dir);
        System.out.println("-> Caught " + codeKeys.size() + " translations keys in " + codeFilesKeys.size() + " files.");
        System.out.println();

        System.out.println("-------------------------");
        System.out.println("-> Writing...");
        System.out.println("-------------------------");
        write(out);
        System.out.println("-> COMPLETED !");
        System.out.println("   " + added + " new empty translations");
        System.out.println("   " + (lines-added) + " old translations reused");
        System.out.println("   " + existingTranslations.size() + " old translations not reused");
        System.out.println("   " + lines + " lines wrote (" + (lines+commentLines) + " total)");
        System.out.println("   " + commentLines + " file name lines");

    }

    public static void readExistingFile(File file){

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            while((line = reader.readLine()) != null){
                if(!line.startsWith("# ")){
                    String key = removeAfterNotEscaped(line, SEPARATOR);
                    String value = removeBeforeNotEscaped(line, SEPARATOR);
                    if(!existingTranslations.containsKey(key)){

                        if(value.isEmpty()) System.out.println("WARNING : no translations in the existing file for : " + key);
                        else existingTranslations.put(key, value);

                    }else System.out.println("WARNING : a key is twice in the existing file : " + key + " (This issue is solved automatically)");
                }
            }

        }catch (IOException e){ e.printStackTrace(); }
    }

    public static void fetchFiles(File dir){
        if(!dir.exists()) throw new RuntimeException("The dir of code files to read does not exist ! (Constant CODE_PATH)");
        for(File file : dir.listFiles()){
            if(file.isDirectory()) fetchFiles(file);
            else readFile(file);
        }
    }

    public static void readFile(File file){
        String fileName = file.getName();
        boolean canContinue = false;
        for(String acceptedExtension : ACCEPTED_EXTENSIONS){
            if(file.getName().toLowerCase().endsWith(acceptedExtension.toLowerCase())) {
                fileName = removeAfterLastRegex(fileName, acceptedExtension);
                canContinue = true; break;
            }
        }
        if(!canContinue) return;

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            StringBuilder fileText = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null){
                while(line.startsWith(" ")){
                    line = line.substring(1);
                }
                fileText.append(line);
            }
            reader.close();
            ArrayList<String> textsToAdd = new ArrayList<>();

            for(Map.Entry<String, String> prefixSuffix : PREFIXES_SUFFIXES.entrySet()){

                String[] trStarts = fileText.toString()
                        .replaceAll(Pattern.quote("\" + \""), "") // Protection against useless escapes
                        .replaceAll(Pattern.quote("\"+ \""), "")
                        .replaceAll(Pattern.quote("\" +\""), "")
                        .replaceAll(Pattern.quote("\"+\""), "")
                        .split(Pattern.quote(prefixSuffix.getKey()));


                int i = 0;
                for(String trStart : trStarts){
                    if(i != 0){
                        String key = removeAfter(trStart, prefixSuffix.getValue());
                        if(!codeKeys.contains(key)){
                            codeKeys.add(key);
                            textsToAdd.add(key);
                        }
                    }
                    i++;
                }
            }

            codeFilesKeys.put(fileName, textsToAdd);

        }catch (IOException e){ e.printStackTrace(); }

    }

    public static void write(File file){

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

            for(Map.Entry<String, ArrayList<String>> fileNameKeys : codeFilesKeys.entrySet()){
                if(fileNameKeys.getValue().size() == 0) continue;

                writer.write("# " + fileNameKeys.getKey());
                commentLines++;
                writer.newLine();

                for(String codeKey : fileNameKeys.getValue()){

                    if(existingTranslations.containsKey(codeKey)){
                        writer.write(codeKey + SEPARATOR + existingTranslations.get(codeKey));
                        lines++;
                        writer.newLine();
                        existingTranslations.remove(codeKey);
                    }else{
                        writer.write(codeKey + SEPARATOR);
                        added++; lines++;
                        writer.newLine();
                    }
                }
            }

            writer.flush();
            writer.close();

        }catch(IOException e){
            e.printStackTrace();
        }

    }

    // UTILS

    public static String removeAfter(String string, String rejex){

        int index = string.indexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);

        return "";
    }
    public static String removeBeforeNotEscaped(String string, String rejex){

        int fromIndex = 0;
        while(true){

            int index = string.indexOf(rejex, fromIndex);
            if(index == -1) return string;

            if(!string.startsWith("\\", index-1)){
                if(index < string.length()) return string.substring(index + rejex.length());
                return "";
            }else{
                fromIndex = index + 1;
            }

        }
    }
    public static String removeAfterNotEscaped(String string, String rejex){

        int fromIndex = 0;
        while(true){

            int index = string.indexOf(rejex, fromIndex);
            if(index == -1) return string;

            if(!string.startsWith("\\", index-1)){
                if(index < string.length()) return string.substring(0, index);
                return "";
            }else{
                fromIndex = index + 1;
            }

        }
    }
    public static String removeAfterLastRegex(String string, String rejex){
        if(rejex.isEmpty()) return string;
        int index = string.lastIndexOf(rejex);

        if(index == -1) return string;
        if(index < string.length()) return string.substring(0, index);

        return "";
    }
}