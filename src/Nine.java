import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings({"unchecked"})
public class Nine {
    private final static String STOP_WORDS_FILE = "stop_words.txt";
    private final static String DELIMITER = "[^a-zA-Z0-9]+";

    private static Function<Object, Object> readFile = input -> {
        String filePath = (String) input;
        String dataString = "";
        try {
            File file = new File(filePath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String content;
            while ((content = br.readLine()) != null) {
                sb.append(content);
                sb.append(" ");
            }
            br.close();
            dataString = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataString;
    };

    private static Function<Object, Object> filterCharsAndNormalize = input -> {
        String dataString = (String) input;
        return dataString.replaceAll(DELIMITER, " ").toLowerCase();
    };

    private static Function<Object, Object> scan = input -> {
        String dataString = (String) input;
        return Arrays.asList(dataString.split(DELIMITER));
    };

    private static Function<Object, Object> removeStopWords = input -> {
        List<String> words = (List<String>) input;
        List<String> cleanWords = new ArrayList<>();
        try {
            File file = new File(STOP_WORDS_FILE);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String content;
            Set<String> stopWords = new HashSet<>();
            while ((content = br.readLine()) != null) {
                for (String w : content.split(DELIMITER)) {
                    stopWords.add(w.toLowerCase());
                }
            }
            for (char c = 'a'; c <= 'z'; c++) {
                stopWords.add(String.valueOf(c));
            }
            br.close();

            for (int i = 0; i < words.size(); i++) {
                if (!stopWords.contains(words.get(i))) {
                    cleanWords.add(words.get(i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cleanWords;
    };

    private static Function<Object, Object> frequencies = input -> {
        List<String> words = (List<String>) input;
        Map<String, Integer> wordFrequencies = new HashMap<>();
        for (String w : words) {
            wordFrequencies.put(w, wordFrequencies.getOrDefault(w, 0) + 1);
        }
        return wordFrequencies;
    };

    private static Function<Object, Object> sort = input -> {
        Map<String, Integer> wordFrequencies = (Map<String, Integer>) input;
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(wordFrequencies.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        return sortList;
    };

    private static Function<Object, Object> print = input -> {
        List<Map.Entry<String, Integer>> sortList = (List<Map.Entry<String, Integer>>) input;
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s  -  %s", sortList.get(0).getKey(), sortList.get(0).getValue()));
        for (int i = 1; i < 25 && i < sortList.size(); i++) {
            Map.Entry<String, Integer> item = sortList.get(i);
            sb.append(String.format("\n%s  -  %s", item.getKey(), item.getValue()));
        }
        return sb.toString();
    };

    private static class TFTheOne {
        private Object value;

        private TFTheOne(Object value) {
            this.value = value;
        }

        private TFTheOne bind(Function function) {
            this.value = function.apply(value);
            return this;
        }

        private void print() {
            System.out.println(value);
        }
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].endsWith(".txt")) {
            (new TFTheOne(args[0]))
                    .bind(readFile)
                    .bind(filterCharsAndNormalize)
                    .bind(scan).bind(removeStopWords)
                    .bind(frequencies).bind(sort)
                    .bind(print)
                    .print();
        } else {
            System.out.println("Please add a text file as input.");
        }
    }
}