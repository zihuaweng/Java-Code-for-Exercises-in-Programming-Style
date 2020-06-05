import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class TwentyOne {
    private final static String STOP_WORDS_FILE = "stop_words.txt";
    private final static String DELIMITER = "[^a-zA-Z0-9]+";
    private final static int TOP = 25;

    private static List<String> readFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) throw new AssertionError("Please provide a non-empty string as filePath");
        List<String> words = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            String line;
            while ((line = br.readLine()) != null) {
                words.addAll(Arrays.asList(line.toLowerCase().split(DELIMITER)));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    private static List<String> removeStopWords(List<String> words) {
        if (words == null || words.isEmpty()) throw new AssertionError("Please provide a non-empty ArrayList as input");
        Set<String> stopWords = new HashSet<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(STOP_WORDS_FILE)));
            String line;
            while ((line = br.readLine()) != null) {
                stopWords.addAll(Arrays.asList(line.toLowerCase().split(DELIMITER)));
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words.stream().filter(x -> !stopWords.contains(x) && x.length() > 1).collect(Collectors.toList());
    }

    private static Map<String, Integer> frequencies(List<String> words) {
        if (words == null || words.isEmpty()) throw new AssertionError("Please provide a non-empty ArrayList as input");
        Map<String, Integer> frequencies = new HashMap<>();
        words.forEach(w -> frequencies.put(w, frequencies.getOrDefault(w, 0) + 1));
        return frequencies;
    }

    private static Map<String, Integer> sort(Map<String, Integer> frequencies) {
        if (frequencies == null || frequencies.isEmpty()) throw new AssertionError("Please provide a non-empty HashMap as input");
        Map<String, Integer> sortedFrequencies = new LinkedHashMap<>();
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(frequencies.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        sortList.forEach(entry -> sortedFrequencies.put(entry.getKey(), entry.getValue()));
        return sortedFrequencies;
    }

    private static void print(Map<String, Integer> frequencies) {
        if (frequencies == null || frequencies.isEmpty()) throw new AssertionError("Please provide a non-empty HashMap as input");
        if (frequencies.size() < TOP) throw new AssertionError("Document should contain at lease " + TOP + " distinct words.");
        Iterator<String> iterator = frequencies.keySet().iterator();
        for (int i = 0; i < TOP; i++) {
            String key = iterator.next();
            System.out.println(String.format("%s  -  %s", key, frequencies.get(key)));
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) throw new AssertionError("Please add one file as input.");
        TwentyOne.print(TwentyOne.sort(TwentyOne.frequencies(TwentyOne.removeStopWords(TwentyOne.readFile(args[0])))));
    }
}
