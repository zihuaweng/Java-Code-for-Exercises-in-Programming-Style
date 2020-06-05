import java.io.*;
import java.util.*;

class Four {
    private static List<Character> data = new ArrayList<>();
    private static List<String> words;
    private static List<Frequency> wordFrequency = new ArrayList<>();
    private final static String STOP_WORDS_FILE = "stop_words.txt";

    public void readFile(String filePath) throws IOException {
        File file = new File(filePath);
        BufferedReader br = new BufferedReader(new FileReader(file));
        int c;
        while ((c = br.read()) != -1) {
            char ch = (char) c;
            data.add(ch);
        }
        br.close();
    }

    public void filterCharsAndNormalize() {
        for (int i = 0; i < data.size(); i++) {
            if (!Character.isLetterOrDigit(data.get(i))) {
                data.set(i, ' ');
            } else {
                data.set(i, Character.toLowerCase(data.get(i)));
            }
        }
    }

    public void scan() {
        StringBuilder sb = new StringBuilder();
        for (Character ch : data) {
            sb.append(ch);
        }
        String dataString = sb.toString();
        words = new ArrayList<String>(Arrays.asList(dataString.split("\\s+")));
    }

    public void removeStopWords() throws IOException {
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
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            if (stopWords.contains(words.get(i))) {
                indexes.add(i);
            }
        }

        for (int i = indexes.size() - 1; i >= 0; i--) {
            words.remove((int) indexes.get((i)));
        }

    }

    public void frequencies() {
        for (String w : words) {
            boolean found = false;
            for (Frequency wq: wordFrequency) {
                if (w.equals(wq.getWord())) {
                    wq.addCount();
                    found = true;
                    break;
                }
            }
            if (!found) {
                wordFrequency.add(new Frequency(w));
            }
        }
    }

    public void sort() {
        Collections.sort(wordFrequency, Frequency.countComparator);
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 1) {
            String inputFile = args[0];
            if (inputFile.endsWith(".txt")) {
                Four termFrequency = new Four();
                termFrequency.readFile(inputFile);
                termFrequency.filterCharsAndNormalize();
                termFrequency.scan();
                termFrequency.removeStopWords();
                termFrequency.frequencies();
                termFrequency.sort();
                for (int i = 0; i < 25; i++) {
                    System.out.println(wordFrequency.get(i).getWord() + "  -  " + wordFrequency.get(i).getCount());
                }
            } else {
                System.out.println("Please add a text file as input.");
            }
        } else {
            System.out.println("Please add a text file as input.");
        }
    }
}

class Frequency {

    private String word;
    private Integer count;

    public Frequency(String word) {
        this.word = word;
        this.count = 1;
    }

    public String getWord() {
        return word;
    }

    public Integer getCount() {
        return count;
    }

    public void addCount() {
        count++;
    }

    public static Comparator<Frequency> countComparator = new Comparator<Frequency>() {
        @Override
        public int compare(Frequency f1, Frequency f2) {
            return f2.getCount().compareTo(f1.getCount());
        }
    };
}
