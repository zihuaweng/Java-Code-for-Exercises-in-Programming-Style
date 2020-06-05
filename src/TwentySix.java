import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public class TwentySix {

    private static final String STOP_WORDS_FILE = "stop_words.txt";
    private static final String DELIMITER = "[^a-zA-Z0-9]+";
    private static final int TOP = 25;

    private static Column allWords = new Column(new ArrayList<>(), null);
    private static Column stopWords = new Column(new HashSet(), null);
    private static Column nonStopWords = new Column(null, () -> {
        List<String> words = new ArrayList<>();
        for (String w : (List<String>) allWords.getData()) {
            if (!((Set<String>) stopWords.getData()).contains(w) && w.length() > 1) {
                words.add(w);
            }
        }
        return words;
    });
    private static Column uniqueWords = new Column(null, () -> {
        Set<String> words = new HashSet<>();
        words.addAll((List<String>) nonStopWords.getData());
        return words;
    });
    private static Column counts = new Column(null, () -> {
        List<Count> counts = new ArrayList<>();
        for (String w : (Set<String>) uniqueWords.getData()) {
            counts.add(new Count(w, Collections.frequency((List<String>) nonStopWords.getData(), w)));
        }
        return counts;
    });
    private static Column sortedData = new Column(null, () -> {
        List<Count> sortedCounts = (List<Count>) counts.getData();
        sortedCounts.sort((a, b) -> b.getCount().compareTo(a.getCount()));
        return sortedCounts;
    });

    private static Column[] spreadsheet = new Column[]{allWords, stopWords, nonStopWords, uniqueWords, counts, sortedData};

    public static void main(String[] args) {
        if (args.length >= 1) {
            Scanner scanner = new Scanner(System.in);
            stopWords.setData(readStopWords());
            do {
                List<String> words = (List<String>) allWords.getData();
                words.addAll(readFile(args[0]));
                allWords.setData(words);
                update();
                print((List<Count>) sortedData.getData());
                System.out.println("Please add file to calculate cumulative word frequency (e.g. pride-and-prejudice.txt). Enter \"stop\" to exit.");
                String input = scanner.nextLine();
                if (input.equals("stop")) {
                    System.exit(1);
                }
            } while (true);
        } else {
            System.out.println("Please add a text file as input.");
        }
    }

    private static void update() {
        for (Column c : spreadsheet) {
            if (c.getLambda() != null) {
                c.setData(c.getLambda().get());
            }
        }
    }

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

    private static Set<String> readStopWords() {
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

    private static void print(List<Count> counts) {
        Iterator<Count> iterator = counts.iterator();
        int i = 0;
        while (iterator.hasNext() && (i < TOP)) {
            Count count = iterator.next();
            System.out.println(String.format("%s  -  %s", count.getWord(), count.getCount()));
            i++;
        }
    }
}

class Count {
    private String word;
    private Integer count;

    Count(String word, int count) {
        this.word = word;
        this.count = count;
    }

    public Integer getCount() {
        return count;
    }

    public String getWord() {
        return word;
    }
}

class Column {
    private Object data;
    private Supplier lambda;

    Column(Object data, Supplier lambda) {
        this.data = data;
        this.lambda = lambda;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public Supplier getLambda() {
        return lambda;
    }
}
