import java.io.*;
import java.util.*;

class TermFrequency {

    static String STOP_WORDS = "stop_words.txt";

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            String inputFile = args[0];
            if (inputFile.endsWith(".txt")) {
                getTermFrequency(inputFile);
            } else {
                System.out.println("Please add a text file as input.");
            }
        } else {
            System.out.println("Please add a text file as input.");
        }
    }

    public static void getTermFrequency(String inputFile) throws IOException {
        File file;
        BufferedReader br;
        String content;
        String[] contentList;
        Map<String, Integer> counter = new HashMap<>();
        Set<String> stopWords = new HashSet<>();

        file = new File(STOP_WORDS);
        br = new BufferedReader(new FileReader(file));
        while ((content = br.readLine()) != null) {
            contentList = content.split(",");
            for (String w : contentList) {
                stopWords.add(w.toLowerCase());
            }
        }
        for (char c = 'a'; c <= 'z'; c++) {
            stopWords.add(String.valueOf(c));
        }
        br.close();

        file = new File(inputFile);
        br = new BufferedReader(new FileReader(file));

        while ((content = br.readLine()) != null) {
            contentList = content.split("[^a-zA-Z]+");
            for (String w : contentList) {
                if (w.length() > 0 && !stopWords.contains(w.toLowerCase())) {
                    counter.put(w.toLowerCase(), counter.getOrDefault(w.toLowerCase(), 0) + 1);
                }
            }
        }
        br.close();

        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(counter.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        List<String> sortContentList = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Map.Entry<String, Integer> item = sortList.get(i);
            sortContentList.add(String.format("%s  -  %s", item.getKey(), item.getValue()));
        }

        System.out.println(String.join("\n", sortContentList));
    }
}