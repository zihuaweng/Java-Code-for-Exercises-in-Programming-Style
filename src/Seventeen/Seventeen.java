package Seventeen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Seventeen {

    public static void main(String[] args) {
        if (args.length == 1 && args[0].endsWith(".txt")) {
            WordFrequencyController wordFrequencyController = new WordFrequencyController();
            wordFrequencyController.run(args[0]);

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("\nHey!!\nPlease input one of the following class names:\n" +
                        "Seventeen.DataStorageManager\n" +
                        "Seventeen.StopWordManager\n" +
                        "Seventeen.WordFrequencyManager\n" +
                        "Seventeen.WordFrequencyController\n" +
                        "(Enter \'stop\' to exit.)\n");
                String input = scanner.nextLine().strip();
                if (input.toLowerCase().equals("stop")) break;
                printReflectionResults(input);
            }
            scanner.close();
        } else {
            System.out.println("Please add a text file as input.");
        }
    }

    /**
     * Print all class's fields (name & types) & method & superclasses & implemented interfaces.
     */
    public static void printReflectionResults(String className) {
        try {
            Class cls = Class.forName(className);
            Field[] fields = cls.getDeclaredFields();
            System.out.println("****Fields:");
            Arrays.stream(fields).forEach(f ->System.out.println("Field name: " + f.getName() + " --- Field type: " + f.getType()));

            Method[] methods = cls.getDeclaredMethods();
            System.out.println("****Methods:");
            Arrays.stream(methods).forEach(System.out::println);

            Class superclass = cls.getSuperclass();
            System.out.println("****Superclasses:");
            while (superclass != null) {
                System.out.println(superclass.getName());
                superclass = superclass.getSuperclass();
            }

            Class[] interfaces = cls.getInterfaces();
            System.out.println("****Interfaces:");
            Arrays.stream(interfaces).forEach(System.out::println);
        } catch (Exception e) {
            System.out.println("Invalid class name.");
        }
    }
}


interface IDataStorage {
    List<String> words(String filePath);
}

interface IStopWordFilter {
    boolean isStopWord(String word);
}

interface IWordFrequencyCounter {
    void count(String word);
    void sortAndPrint();
}

class WordFrequencyController {
    private String STOP_WORD_FILE = "stop_words.txt";
    private DataStorageManager storageManager;
    private StopWordManager stopWordManager;
    private WordFrequencyManager wordFrequencyManager;
    private Class clsDsm;
    private Class clsSwm;
    private Class clsWfm;

    public WordFrequencyController() {
        try {
            clsDsm = Class.forName("Seventeen.DataStorageManager");
            clsSwm = Class.forName("Seventeen.StopWordManager");
            clsWfm = Class.forName("Seventeen.WordFrequencyManager");
            storageManager = (DataStorageManager) clsDsm.getDeclaredConstructor().newInstance();
            stopWordManager = (StopWordManager) clsSwm.getDeclaredConstructor(String.class).newInstance(STOP_WORD_FILE);
            wordFrequencyManager = (WordFrequencyManager) clsWfm.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run(String filePath) {
        try {
            for (String w : (List<String>) clsDsm.getDeclaredMethod("words", String.class).invoke(storageManager, filePath)) {
                if (!(boolean) clsSwm.getDeclaredMethod("isStopWord", String.class).invoke(stopWordManager, w)) {
                    clsWfm.getDeclaredMethod("count", String.class).invoke(wordFrequencyManager, w);
                }
            }
            clsWfm.getDeclaredMethod("sortAndPrint").invoke(wordFrequencyManager);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class DataStorageManager implements IDataStorage {

    private final String DELIMITER = "[^a-zA-Z0-9]+";

    @Override
    public List<String> words(String filePath) {
        List<String> words = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append(" ");
            }
            String[] wordArray = sb.toString().toLowerCase().split(DELIMITER);
            words.addAll(Arrays.asList(wordArray));
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }
}


class WordFrequencyManager implements IWordFrequencyCounter {

    private Map<String, Integer> frequencies;

    WordFrequencyManager() {
        frequencies = new HashMap<>();
    }

    @Override
    public void count(String word) {
        frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
    }

    @Override
    public void sortAndPrint() {
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(frequencies.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        for (int i = 0; i < 25 && i < sortList.size(); i++) {
            Map.Entry<String, Integer> item = sortList.get(i);
            System.out.println(String.format("%s  -  %s", item.getKey(), item.getValue()));
        }
    }

}


class StopWordManager implements IStopWordFilter {

    private final String DELIMITER = "[^a-zA-Z0-9]+";
    private Set<String> stopWords;

    StopWordManager(String filePath) {
        stopWords = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            String line;
            while ((line = br.readLine()) != null) {
                String[] wordArray = line.split(DELIMITER);
                for (String w : wordArray) {
                    stopWords.add(w.toLowerCase());
                }
            }
            for (char c = 'a'; c <= 'z'; c++) {
                stopWords.add(String.valueOf(c));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isStopWord(String word) {
        return stopWords.contains(word);
    }
}
