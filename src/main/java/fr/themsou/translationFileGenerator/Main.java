package fr.themsou.translationFileGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Main {

    /////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////// SETTINGS ZONE /////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    // Use this option if you want to update an already translated file,
    // we will add to this file the new translations gets in the code,
    // and remove the unused translations.
    // Use false if you want to disable this option.
    public static final boolean READ_EXISTING_FILE = true;
    // Path to the already translated file
    public static final String ALREADY_TRANSLATED_FILE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\resources\\translations\\English US.txt";

    // The code where we have to get translations
    public static final String CODE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\src\\main\\java\\fr\\themsou";

    // The output file, with all translations
    public static final String OUT_FILE_PATH = "C:\\Users\\Clement\\Developpement\\Java\\PDF4Teachers\\template.txt";

    // Prefix and suffix of the method that we have to catch
// Here, the method are TR.tr("key string")
    public static final String PREFIX = "TR.tr(\"";
    public static final String SUFFIX = "\")";

    // Separator between the key and value in the translation file
    // Keep only one char for your security
    public static final String SEPARATOR = "=";

    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////////

    static ArrayList<String> codeKeys = new ArrayList<>();
    static HashMap<String, ArrayList<String>> codeFilesKeys = new HashMap<>();

    static ArrayList<String> existingKeys = new ArrayList<>();
    static HashMap<String, HashMap<String, String>> existingFilesTranslations = new HashMap<>();

    static int added = 0;
    static int removed = 0;
    static int lines = 0;
    static int commentLines = 0;

    public static void main(String[] args) {

        File in = new File(ALREADY_TRANSLATED_FILE_PATH);
        File dir = new File(CODE_PATH);
        File out = new File(OUT_FILE_PATH);

        if(READ_EXISTING_FILE){
            if(in.exists()){
                System.out.println("-------------------------");
                System.out.println("-> Reading already translated file...");
                System.out.println("-------------------------");
                readExistingFile(in);
            }else{
                System.out.println(in.getAbsolutePath() + " does not exist !");
            }

        }

        System.out.println("-------------------------");
        System.out.println("-> Reading code files...");
        System.out.println("-------------------------");
        fetchFiles(dir);

        System.out.println("-------------------------");
        System.out.println("-> Writing...");
        System.out.println("-------------------------");
        write(out);

        System.out.println("-------------------------");
        System.out.println("-> COMPLETED !");
        System.out.println("   " + added + " lines added");
        System.out.println("   " + removed + " lines removed");
        System.out.println("   " + lines + " lines writed (" + (lines+commentLines) + " total)");
        System.out.println("   " + commentLines + " class name lines (don't remove it please)");
        System.out.println("-------------------------");

    }

    public static void readExistingFile(File file){

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));

            HashMap<String, String> translationsToAdd = new HashMap<>();
            String fileName = null;
            String line;
            while((line = reader.readLine()) != null){
                if(line.startsWith("# ")){
                    if(fileName != null){
                        existingFilesTranslations.put(fileName, translationsToAdd);
                        translationsToAdd = new HashMap<>();
                    }
                    fileName = line.replaceFirst(Pattern.quote("# "), "");
                }else{
                    String key = removeAfterNotEscaped(line, SEPARATOR);
                    String value = removeBeforeNotEscaped(line, SEPARATOR);
                    if(!existingKeys.contains(key)){
                        existingKeys.add(key);
                        translationsToAdd.put(key, value);

                        if(value.isEmpty()) System.out.println("WARNING : no translations in the existing file for : " + key);
                    }else System.out.println("WARNING : a key is twice in the existing file : " + key);
                }
            }
            if(fileName != null) existingFilesTranslations.put(fileName, translationsToAdd);

        }catch (IOException e){ e.printStackTrace(); }
    }

    public static void fetchFiles(File dir){
        for(File file : dir.listFiles()){
            if(file.isDirectory()) fetchFiles(file);
            else readFile(file);
        }
    }

    public static void readFile(File file){
        String fileName = file.getName().replaceAll(Pattern.quote(".java"), "");
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

            String[] trStarts = fileText.toString()
                    .replaceAll(Pattern.quote("\" + \""), "")
                    .replaceAll(Pattern.quote("\"+ \""), "")
                    .replaceAll(Pattern.quote("\" +\""), "")
                    .replaceAll(Pattern.quote("\"+\""), "")
                    .split(Pattern.quote(PREFIX));

            ArrayList<String> textsToAdd = new ArrayList<>();
            int i = 0;
            for(String trStart : trStarts){
                if(i != 0){
                    String key = removeAfter(trStart, SUFFIX);
                    if(!codeKeys.contains(key)){
                        codeKeys.add(key);
                        textsToAdd.add(key);
                    }
                }
                i++;
            }
            codeFilesKeys.put(fileName, textsToAdd);

        }catch (IOException e){ e.printStackTrace(); }

    }

    public static void write(File file){

        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

            for(Map.Entry<String, HashMap<String, String>> fileTranslations : existingFilesTranslations.entrySet()){
                if(codeFilesKeys.containsKey(fileTranslations.getKey())) {
                    writer.write("# " + fileTranslations.getKey());
                    commentLines++;
                    writer.newLine();
                    for(Map.Entry<String, String> translations : fileTranslations.getValue().entrySet()){
                        if(codeKeys.contains(translations.getKey())){
                            writer.write(translations.getKey() + SEPARATOR + translations.getValue());
                            lines++;
                            writer.newLine();
                            codeFilesKeys.get(fileTranslations.getKey()).remove(translations.getKey());
                            codeKeys.remove(translations.getKey());
                        }else{
                            System.out.println("Remove existing translation from file (unused) : " + translations.getKey() + SEPARATOR + translations.getValue());
                            removed ++;
                        }
                    }

                    for(String key : codeFilesKeys.get(fileTranslations.getKey())){
                        if(!existingKeys.contains(key)){
                            writer.write(key + SEPARATOR);
                            System.out.println("Add translation line to : " + key);
                            added++; lines++;
                            writer.newLine();
                        }
                    }
                    codeFilesKeys.remove(fileTranslations.getKey());

                }else{
                    System.out.println("Remove existing translation file from file (unused) : # " + fileTranslations.getKey());
                    removed += fileTranslations.getKey().length();
                }
            }

            for(Map.Entry<String, ArrayList<String>> fileKeys : codeFilesKeys.entrySet()){
                writer.write("# " + fileKeys.getKey());
                System.out.println("Add file : # " + fileKeys.getKey());
                commentLines++;
                writer.newLine();
                for(String key : fileKeys.getValue()){
                    writer.write(key + SEPARATOR);
                    System.out.println("Add translation line to : " + key);
                    added++; lines++;
                    writer.newLine();
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
}