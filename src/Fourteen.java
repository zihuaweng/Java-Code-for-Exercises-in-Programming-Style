import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class Fourteen {

    public static void main(String[] args) {
        if (args.length == 1 && args[0].endsWith(".txt")) {
            WordFrequencyFramework wf = new WordFrequencyFramework();
            StopWordFilter stopWordFilter = new StopWordFilter(wf);
            DataStorage dataStorage = new DataStorage(wf, stopWordFilter);
            new WordFrequencyCounter(wf, dataStorage);
            new WordWithZ(wf, dataStorage);
            wf.run(args[0]);
        } else {
            System.out.println("Please add a text file as input.");
        }

    }

}

interface Process {
    void apply();
}

class WordFrequencyFramework {
    private List<Consumer<String>> loadEventHandlers;
    private List<Process> execEventHandlers;
    private List<Process> endEventHandlers;

    WordFrequencyFramework() {
        this.loadEventHandlers = new ArrayList<>();
        this.execEventHandlers = new ArrayList<>();
        this.endEventHandlers = new ArrayList<>();
    }

    void registerLoadEvent(Consumer<String> handler) {
        loadEventHandlers.add(handler);
    }

    void registerExecEvent(Process handler) {
        execEventHandlers.add(handler);
    }

    void registerEndEvent(Process handler) {
        endEventHandlers.add(handler);
    }

    public void run(String file) {
        for (Consumer<String> handler: loadEventHandlers) {
            handler.accept(file);
        }
        for (Process handler: execEventHandlers) {
            handler.apply();
        }
        for (Process handler: endEventHandlers) {
            handler.apply();
        }
    }
}

class DataStorage {

    private final String DELIMITER = "[^a-zA-Z0-9]+";

    private List<String> words;
    private List<Consumer<String>> eventHandlers;
    private StopWordFilter stopWordFilter;

    DataStorage(WordFrequencyFramework wf, StopWordFilter handler) {
        this.eventHandlers = new ArrayList<>();
        this.stopWordFilter = handler;
        wf.registerLoadEvent(this::load);
        wf.registerExecEvent(this::filterStopWord);
    }

    private void load(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line.toLowerCase());
                sb.append(" ");
            }
            br.close();
            this.words = Arrays.asList(sb.toString().split(DELIMITER));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterStopWord() {
        for (String w: words) {
            if (!stopWordFilter.isStopWord(w)) {
                for (Consumer<String> handler: eventHandlers) {
                    handler.accept(w);
                }
            }
        }
    }

    void register(Consumer<String> handler) {
        eventHandlers.add(handler);
    }

}

class StopWordFilter {

    private final String STOP_WORDS_FILE = "stop_words.txt";
    private final String DELIMITER = "[^a-zA-Z0-9]+";

    private Set<String> stopWords;

    StopWordFilter(WordFrequencyFramework wf) {
        stopWords = new HashSet<>();
        wf.registerLoadEvent(this::load);
    }

    private void load(String input) {
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

    boolean isStopWord(String word) {
        return stopWords.contains(word);
    }

}

class WordFrequencyCounter {

    private Map<String, Integer> frequencies;

    WordFrequencyCounter(WordFrequencyFramework wf, DataStorage handler) {
        frequencies = new HashMap<>();
        handler.register(this::exec);
        wf.registerEndEvent(this::end);
    }

    private void exec(String word) {
        frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
    }

    public void end() {
        System.out.println("=========Print 25 top frequency word=========");
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(frequencies.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        for (int i = 0; i < 25 && i < sortList.size(); i++) {
            Map.Entry<String, Integer> item = sortList.get(i);
            System.out.println(String.format("%s  -  %s", item.getKey(), item.getValue()));
        }
    }
}

class WordWithZ {
    private Set<String> filterWords;

    WordWithZ(WordFrequencyFramework wf, DataStorage handler) {
        filterWords = new HashSet<>();
        handler.register(this::exec);
        wf.registerEndEvent(this::end);
    }

    private void exec(String word) {
        if (word.contains("z")) {
            filterWords.add(word);
        }
    }

    public void end() {
        System.out.println("=========Print words with 'z'=========");
        filterWords.forEach(System.out::println);
    }

}