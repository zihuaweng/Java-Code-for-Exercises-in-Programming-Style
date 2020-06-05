import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TwentySeven {

    private static final String STOP_WORDS_FILE = "stop_words.txt";
    private static final String DELIMITER = "[^a-zA-Z0-9]+";
    private static final int TOP = 25;

    public static void main(String[] args) {
        if (args.length >= 1) {
            GetWords getWords = new GetWords(args[0]);
            NonStopWord nonStopWord = new NonStopWord(getWords);
            CountAndSort countAndSort = new CountAndSort(nonStopWord);
            while (countAndSort.hasNext()) {
                System.out.println("========================");
                print(countAndSort.next());
            }
        } else {
            System.out.println("Please add a text file as input.");
        }
    }

    static class GetWords implements Iterator<String> {
        private BufferedReader br;
        private String line;
        private List<String> wordsPerLine;
        private int index;

        GetWords(String filePath) {
            try {
                this.br = new BufferedReader(new FileReader(new File(filePath)));
                this.wordsPerLine = nextLineWords();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public List<String> nextLineWords() {
            try {
                if ((line = br.readLine()) != null) {
                    return Arrays.asList(line.toLowerCase().split(DELIMITER));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public boolean hasNext() {
            if (wordsPerLine != null && wordsPerLine.size() != index) return true;
            while ((wordsPerLine = nextLineWords()) != null) {
                if (!wordsPerLine.isEmpty()) {
                    index = 0;
                    return true;
                }
            }
            return false;
        }

        @Override
        public String next() {
            String next;
            if (hasNext()) {
                next = wordsPerLine.get(index);
                index++;
                return next;
            } else {
                return null;
            }
        }
    }

    static class NonStopWord implements Iterator<String> {
        private Set<String> stopWords;
        private Iterator<String> allWords;
        private String next;
        private boolean ready;

        NonStopWord(Iterator<String> allWords) {
            this.stopWords = readFile();
            this.allWords = allWords;
            this.ready = false;
        }

        public Set<String> readFile() {
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
            return stopWords;
        }

        @Override
        public boolean hasNext() {
            if (ready) return true;
            while ((next = allWords.next()) != null) {
                if (!stopWords.contains(next) && next.length() > 1) {
                    ready = true;
                    return true;
                }
            }
            ready = false;
            return false;
        }

        @Override
        public String next() {
            if (hasNext()) {
                ready = false;
                return next;
            } else {
                ready = false;
                return null;
            }
        }
    }

    static class CountAndSort implements Iterator<Map<String, Integer>> {
        private Map<String, Integer> frequencies;
        private Iterator<String> nonStopWord;
        private int index;

        CountAndSort(Iterator<String> nonStopWord) {
            this.frequencies = new HashMap<>();
            this.nonStopWord = nonStopWord;
            this.index = 1;
        }

        @Override
        public boolean hasNext() {
            return nonStopWord.hasNext();
        }

        @Override
        public Map<String, Integer> next() {
            while (index % 5000 != 0) {
                if (hasNext()) {
                    String word = nonStopWord.next();
                    frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
                } else {
                    return sort(frequencies);
                }
                index++;
            }
            index = 1;
            return sort(frequencies);
        }

        public Map<String, Integer> sort(Map<String, Integer> frequencies) {
            Map<String, Integer> sortedFrequencies = new LinkedHashMap<>();
            List<Map.Entry<String, Integer>> sortList = new ArrayList<>(frequencies.entrySet());
            sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
            sortList.forEach(entry -> sortedFrequencies.put(entry.getKey(), entry.getValue()));
            return sortedFrequencies;
        }
    }

    public static void print(Map<String, Integer> frequencies) {
        Iterator<String> iterator = frequencies.keySet().iterator();
        int i = 0;
        while (iterator.hasNext() && (i < TOP)) {
            String key = iterator.next();
            System.out.println(String.format("%s  -  %s", key, frequencies.get(key)));
            i++;
        }
    }
}
