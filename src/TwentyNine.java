import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TwentyNine {

    private final String STOP_WORDS_FILE = "stop_words.txt";
    private final String DELIMITER = "[^a-zA-Z0-9]+";
    private final int WORKER_NUMBER = 5;
    private final String POISON = "#STOP_MARKER#";

    private BlockingQueue<String> wordSpace;
    private BlockingQueue<Map<String, Integer>> freqSpace;
    private Set<String> stopWords;
    private Map<String, Integer> wordFrequencies;

    private TwentyNine() {
        wordSpace = new LinkedBlockingQueue<>();
        freqSpace = new LinkedBlockingQueue<>();
        stopWords = new HashSet<>();
        wordFrequencies = new HashMap<>();
    }

    private void loadWords(String inputFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(inputFile)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line.toLowerCase());
                sb.append(" ");
            }
            br.close();
            wordSpace.addAll(Arrays.asList(sb.toString().split(DELIMITER)));
            wordSpace.offer(POISON);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStopWords() {
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

    private void processWords() {
        Map<String, Integer> frequencies = new HashMap<>();
        while (true) {
            try {
                String word = wordSpace.take();
                if (word.equals(POISON)) {
                    wordSpace.offer(POISON);
                    break;
                }
                if (!stopWords.contains(word)) {
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        System.out.println("bbbb");
        freqSpace.offer(frequencies);
    }

    private void exec() {
        try{
            List<Thread> workers = new ArrayList<>();
            for (int i = 0; i < WORKER_NUMBER; i++) {
                workers.add(new Thread(this::processWords));
            }
            workers.forEach(Thread::start);
            for (Thread worker : workers) {
                worker.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void mergeAndDisplay() {
        try{
            while (!freqSpace.isEmpty()) {
                Map<String, Integer> freq = freqSpace.take();
                for (Map.Entry<String, Integer> entry: freq.entrySet()) {
                    wordFrequencies.put(entry.getKey(), wordFrequencies.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(wordFrequencies.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        for (int i = 0; i < 25 && i < sortList.size(); i++) {
            Map.Entry<String, Integer> item = sortList.get(i);
            System.out.println(String.format("%s  -  %s", item.getKey(), item.getValue()));
        }
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0].endsWith(".txt")) {
            TwentyNine twentyNine = new TwentyNine();
            twentyNine.loadWords(args[0]);
            twentyNine.loadStopWords();
            twentyNine.exec();
            twentyNine.mergeAndDisplay();
        } else {
            System.out.println("Please add a text file as input.");
        }
    }

}
