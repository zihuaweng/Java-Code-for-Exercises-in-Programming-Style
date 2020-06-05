import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings({"unchecked"})
public class Eight {
    private final static String STOP_WORDS_FILE = "stop_words.txt";
    private final static String DELIMITER = "[^a-zA-Z0-9]+";

    private static Consumer<List<Map.Entry<String, Integer>>> print = sortList -> {
        for (int i = 0; i < 25 && i < sortList.size(); i++) {
            Map.Entry<String, Integer> item = sortList.get(i);
            System.out.println(String.format("%s  -  %s", item.getKey(), item.getValue()));
        }
    };

    private static BiConsumer<Map<String, Integer>, Consumer> sort = (wordFrequencies, function) -> {
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(wordFrequencies.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        function.accept(sortList);
    };

    private static BiConsumer<List<String>, BiConsumer> frequencies = (words, function) -> {
        Map<String, Integer> wordFrequencies = new HashMap<>();
        for (String w : words) {
            wordFrequencies.put(w, wordFrequencies.getOrDefault(w, 0) + 1);
        }
        function.accept(wordFrequencies, print);
    };

    private static BiConsumer<List<String>, BiConsumer> removeStopWords = (words, function) -> {
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
        function.accept(cleanWords, sort);
    };

    private static BiConsumer<String, BiConsumer> scan = (dataString, function) -> {
        function.accept(Arrays.asList(dataString.split(DELIMITER)), frequencies);
    };

    private static BiConsumer<String, BiConsumer> filterCharsAndNormalize = (dataString, function) -> {
        function.accept(dataString.replaceAll(DELIMITER, " ").toLowerCase(), removeStopWords);
    };

    private static BiConsumer<String, BiConsumer> readFile = (filePath, function) -> {
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
        function.accept(dataString, scan);
    };

    public static void main(String[] args) {
        if (args.length == 1 && args[0].endsWith(".txt")) {
            readFile.accept(args[0], filterCharsAndNormalize);
        } else {
            System.out.println("Please add a text file as input.");
        }
    }
}