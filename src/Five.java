import java.io.*;
import java.util.*;

class Five {
    final static String STOP_WORDS_FILE = "stop_words.txt";

    public String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        StringBuilder sb = new StringBuilder();
        String content;
        while ((content = br.readLine()) != null) {
            sb.append(content);
            sb.append(" ");
        }
        br.close();
        return sb.toString();
    }

    public String filterCharsAndNormalize(String dataString) {
        return dataString.replaceAll("[^a-zA-Z0-9]+", " ").toLowerCase();
    }

    public List<String> scan(String dataString) {
        return Arrays.asList(dataString.split("\\s+"));
    }

    public List<String> removeStopWords(List<String> words) throws IOException {
        File file = new File(STOP_WORDS_FILE);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String content;
        Set<String> stopWords = new HashSet<>();
        while ((content = br.readLine()) != null) {
            for (String w : content.split(",")) {
                stopWords.add(w.toLowerCase());
            }
        }
        for (char c = 'a'; c <= 'z'; c++) {
            stopWords.add(String.valueOf(c));
        }
        br.close();
        List<String> cleanWords = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            if (!stopWords.contains(words.get(i))) {
                cleanWords.add(words.get(i));
            }
        }
        return cleanWords;
    }

    public Map<String, Integer> frequencies(List<String> words) {
        Map<String, Integer> wordFrequencies = new HashMap<>();
        for (String w : words) {
            wordFrequencies.put(w, wordFrequencies.getOrDefault(w, 0) + 1);
        }
        return wordFrequencies;
    }

    public List<Map.Entry<String, Integer>> sort(Map<String, Integer> wordFrequencies) {
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(wordFrequencies.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        return sortList;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            String inputFile = args[0];
            if (inputFile.endsWith(".txt")) {
                Five termFrequency = new Five();
                String dataString = termFrequency.filterCharsAndNormalize(termFrequency.readFile(inputFile));
                List<String> words = termFrequency.removeStopWords(termFrequency.scan(dataString));
                List<Map.Entry<String, Integer>> sortList = termFrequency.sort(termFrequency.frequencies(words));
                for (int i = 0; i < 25; i++) {
                    Map.Entry<String, Integer> item = sortList.get(i);
                    System.out.println(String.format("%s  -  %s", item.getKey(), item.getValue()));
                }
            } else {
                System.out.println("Please add a text file as input.");
            }
        } else {
            System.out.println("Please add a text file as input.");
        }
    }
}