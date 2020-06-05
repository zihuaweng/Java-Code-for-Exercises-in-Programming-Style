import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Twenty {
    private final static String STOP_WORDS_FILE = "stop_words.txt";
    private final static String DELIMITER = "[^a-zA-Z0-9]+";
    private final static int TOP = 25;

    private static List<String> readFile(String filePath) {
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
        Map<String, Integer> frequencies = new HashMap<>();
        words.forEach(w -> frequencies.put(w, frequencies.getOrDefault(w, 0) + 1));
        return frequencies;
    }

    private static Map<String, Integer> sort(Map<String, Integer> frequencies) {
        Map<String, Integer> sortedFrequencies = new LinkedHashMap<>();
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(frequencies.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        sortList.forEach(entry -> sortedFrequencies.put(entry.getKey(), entry.getValue()));
        return sortedFrequencies;
    }

    private static void print(Map<String, Integer> frequencies) {
        Iterator<String> iterator = frequencies.keySet().iterator();
        int i = 0;
        while (iterator.hasNext() && (i < TOP)) {
            String key = iterator.next();
            System.out.println(String.format("%s  -  %s", key, frequencies.get(key)));
            i++;
        }
    }

    public static void main(String[] args) {
        String filePath = "pride-and-prejudice.txt";
        if (args.length >= 1) {
            filePath = args[0];
        } else {
            System.out.println("Warming: Reading default file pride-and-prejudice.txt...");
        }
        Twenty.print(Twenty.sort(Twenty.frequencies(Twenty.removeStopWords(Twenty.readFile(filePath)))));
    }
}
