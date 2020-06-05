package plugin.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class App2 implements Consumer<String> {
    private static final String DELIMITER = "[^a-zA-Z0-9]+";

    public static class WordFrequencyController {
        private String STOP_WORD_FILE = "stop_words.txt";
        private DataStorageManager storageManager;
        private StopWordManager stopWordManager;
        private WordFrequencyManager wordFrequencyManager;

        private WordFrequencyController() {
            storageManager = new DataStorageManager();
            stopWordManager = new StopWordManager(STOP_WORD_FILE);
            wordFrequencyManager = new WordFrequencyManager();
        }

        public void run(String filePath) {
            for (String w : storageManager.words(filePath)) {
                if (!stopWordManager.isStopWord(w)) {
                    wordFrequencyManager.count(w);
                }
            }
            wordFrequencyManager.sortAndPrint();
        }
    }

    public static class DataStorageManager implements IDataStorage {


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

    public static class StopWordManager implements IStopWordFilter {

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

    public static class WordFrequencyManager implements IWordFrequencyCounter {

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

    @Override
    public void accept(String filePath) {
        try{
            WordFrequencyController wordFrequencyController = new WordFrequencyController();
            wordFrequencyController.run(filePath);
        } catch (Exception e) {
            e.printStackTrace();
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

}


