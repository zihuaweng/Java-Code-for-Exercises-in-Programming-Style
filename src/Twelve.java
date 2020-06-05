import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Twelve {
    private static final String DELIMITER = "[^a-zA-Z0-9]+";
    private static final String STOP_WORD_FILE = "stop_words.txt";

    @SuppressWarnings({"unchecked"})
    public static void main(String[] args) {

        // dataStorage
        Map<String, Object> dataStorageObj = new HashMap<>();
        Function<String, List<String>> getWords = filePath -> {
            List<String> words = new ArrayList<>();
            try {
                BufferedReader bf = new BufferedReader(new FileReader(new File(filePath)));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bf.readLine()) != null) {
                    sb.append(line);
                    sb.append(" ");
                }
                String[] wordArray = sb.toString().toLowerCase().split(DELIMITER);
                words.addAll(Arrays.asList(wordArray));
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return words;
        };
        dataStorageObj.put("data", getWords);

        // stopWords
        Map<String, Object> stopWordsObj = new HashMap<>();
        stopWordsObj.put("data", new HashSet<String>());

        Process getStopWords = () -> {
            Set<String> stopWords = (Set<String>) stopWordsObj.get("data");
            try {
                BufferedReader bf = new BufferedReader(new FileReader(new File(STOP_WORD_FILE)));
                String line;
                while ((line = bf.readLine()) != null) {
                    String[] wordArray = line.split(DELIMITER);
                    for (String w : wordArray) {
                        stopWords.add(w.toLowerCase());
                    }
                }
                for (char c = 'a'; c <= 'z'; c++) {
                    stopWords.add(String.valueOf(c));
                }
                bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        stopWordsObj.put("words", getStopWords);

        Predicate<String> isStopWord = word -> ((Set<String>) stopWordsObj.get("data")).contains(word);
        stopWordsObj.put("isStopWord", isStopWord);


        // wordFrequenciesObj
        Map<String, Object> wordFrequenciesObj = new HashMap<>();
        wordFrequenciesObj.put("data", new HashMap<String, Integer>());
        Consumer<String> incrementCount = word -> {
            Map<String, Integer> frequencies = (HashMap<String, Integer>) wordFrequenciesObj.get("data");
            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
        };
        wordFrequenciesObj.put("incrementCount", incrementCount);

        Supplier<List<Map.Entry<String, Integer>>> sort = () -> {
            List<Map.Entry<String, Integer>> sortList = new ArrayList<>(((HashMap<String, Integer>) wordFrequenciesObj.get("data")).entrySet());
            sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

            return sortList;
        };
        wordFrequenciesObj.put("sort", sort);

        Consumer<List<Map.Entry<String, Integer>>> top25 = sortList -> {
            for (int i = 0; i < 25 && i < sortList.size(); i++) {
                Map.Entry<String, Integer> item = sortList.get(i);
                System.out.println(String.format("%s  -  %s", item.getKey(), item.getValue()));
            }
        };
        wordFrequenciesObj.put("top25", top25);

        if (args.length == 1 && args[0].endsWith(".txt")) {
            // Initiate data
            ((Process) stopWordsObj.get("words")).apply();

            // Get frequencies
            for (String word : ((Function<String, List<String>>) dataStorageObj.get("data")).apply(args[0])) {
                if (!((Predicate<String>) stopWordsObj.get("isStopWord")).test(word)) {
                    ((Consumer<String>) wordFrequenciesObj.get("incrementCount")).accept(word);
                }
            }

            // Sort frequencies
            List<Map.Entry<String, Integer>> sortList = ((Supplier<List<Map.Entry<String, Integer>>>) wordFrequenciesObj.get("sort")).get();

            // Print top 25 frequencies
            ((Consumer<List<Map.Entry<String, Integer>>>) wordFrequenciesObj.get("top25")).accept(sortList);

        } else {
            System.out.println("Please add a text file as input.");
        }

    }

}

interface Process {
    void apply();
}