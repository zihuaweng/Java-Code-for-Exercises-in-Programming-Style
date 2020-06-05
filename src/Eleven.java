import java.io.*;
import java.util.*;

public class Eleven {

    private final String STOP_WORDS_FILE = "stop_words.txt";
    private final String DELIMITER = "[^a-zA-Z0-9]+";
    private DataStorageManager dataStorageManager;
    private StopWordManager stopWordManager;
    private WordFrequencyManager wordFrequencyManager;

    private void dispatch(TwentyEight.Message message) throws Exception {
        String tag = message.getTag();
        if (tag.equals("init")) {
            init(message.getData());
        } else if (tag.equals("run")) {
            run();
        } else {
            throw new Exception("message tag should be \"init\" or \"run\"");
        }
    }

    private void init(String filePath) throws Exception {
        dataStorageManager = new DataStorageManager();
        stopWordManager = new StopWordManager();
        wordFrequencyManager = new WordFrequencyManager();
        dataStorageManager.dispatch(new TwentyEight.Message("init", filePath));
        stopWordManager.dispatch(new TwentyEight.Message("init"));
    }

    private void run() throws Exception {
        for (String w : dataStorageManager.dispatch(new TwentyEight.Message("words"))) {
            if (!stopWordManager.dispatch(new TwentyEight.Message("isStopWords", w))) {
                wordFrequencyManager.dispatch(new TwentyEight.Message("count", w));
            }
        }
        wordFrequencyManager.dispatch(new TwentyEight.Message("sortAndPrint"));

    }

    public static void main(String[] args) throws Exception {
        if (args.length == 1 && args[0].endsWith(".txt")) {
            Eleven eleven = new Eleven();
            eleven.dispatch(new TwentyEight.Message("init", args[0]));
            eleven.dispatch(new TwentyEight.Message("run"));
        } else {
            System.out.println("Please add a text file as input.");
        }
    }

    public class DataStorageManager {

        private String data;

        String[] dispatch(TwentyEight.Message message) throws Exception {
            String tag = message.getTag();
            if (tag.equals("init")) {
                init(message.getData());
                return new String[0];
            } else if (tag.equals("words")) {
                return words();
            } else {
                throw new Exception("message tag should be \"init\" or \"words\"");
            }
        }

        private void init(String filePath) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line.toLowerCase());
                    sb.append(" ");
                }
                br.close();
                data = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String[] words() {
            return data.split(DELIMITER);
        }
    }

    public class StopWordManager {
        private Set<String> stopWords = new HashSet<>();

        boolean dispatch(TwentyEight.Message message) throws Exception {
            String tag = message.getTag();
            if (tag.equals("init")) {
                init();
                return true;
            } else if (tag.equals("isStopWords")) {
                return isStopWords(message.getData());
            } else {
                throw new Exception("message tag should be \"init\" or \"isStopWords\"");
            }
        }

        private void init() {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File(STOP_WORDS_FILE)));
                String line;
                while ((line = br.readLine()) != null) {
                    String[] words = line.split(DELIMITER);
                    for (String word : words) {
                        stopWords.add(word.toLowerCase());
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

        private boolean isStopWords(String word) {
            return stopWords.contains(word);
        }
    }

    public class WordFrequencyManager {
        private Map<String, Integer> frequencies = new HashMap<>();

        void dispatch(TwentyEight.Message message) throws Exception {
            String tag = message.getTag();
            if (tag.equals("count")) {
                count(message.getData());
            } else if (tag.equals("sortAndPrint")) {
                sortAndPrint();
            } else {
                throw new Exception("message tag should be \"count\" or \"sortAndPrint\"");
            }
        }

        private void count(String word) {
            frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
        }

        private void sortAndPrint() {
            List<Map.Entry<String, Integer>> sortList = new ArrayList<>(frequencies.entrySet());
            sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
            for (int i = 0; i < 25 && i < sortList.size(); i++) {
                Map.Entry<String, Integer> item = sortList.get(i);
                System.out.println(String.format("%s  -  %s", item.getKey(), item.getValue()));
            }
        }
    }
}

