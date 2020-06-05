package TwentyEight;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;

public class TwentyEight {

    public static void main(String[] args) throws InterruptedException {
        if (args.length == 1 && args[0].endsWith(".txt")) {
            BiConsumer<Actor, Message> lambda = (recipient, msg) -> recipient.receive(msg);
            WordFrequencyManager wordFrequencyManager = new WordFrequencyManager();
            StopWordManager stopWordManager = new StopWordManager();
            lambda.accept(stopWordManager, new Message("init", wordFrequencyManager, null));

            DataStorageManager dataStorageManager = new DataStorageManager();
            lambda.accept(dataStorageManager, new Message("init", stopWordManager, args[0]));

            WordFrequencyController wordFrequencyController = new WordFrequencyController();
            lambda.accept(wordFrequencyController, new Message("run", dataStorageManager, null));

            List<Actor> threads = new ArrayList<>();
            threads.add(wordFrequencyManager);
            threads.add(stopWordManager);
            threads.add(dataStorageManager);
            threads.add(wordFrequencyController);
            for (Actor t: threads) {
                t.getThread().join();
            }

        } else {
            System.out.println("Please add a text file as input.");
        }
    }
}

abstract class Actor {
    enum STATE {
        alive, dead
    }

    private final BlockingQueue<Message> queue;
    private STATE state;
    private Thread thread;

    Actor() {
        this.queue = new LinkedBlockingQueue<>();
        this.state = STATE.alive;
        thread = new Thread(this::run);
        thread.start();
    }

    public void run() {
        while (state.equals(STATE.alive)) {
            try {
                Message msg = queue.take();
                work(msg);
                if (msg.getTag().equals("stop")) {
                    state = STATE.dead;
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Thread getThread() {
        return thread;
    }

    protected abstract void work (Message message);

    public void receive(Message msg) {
        queue.offer(msg);
    }

    public void setState(STATE state) {
        this.state = state;
    }

    public void send(Actor recipient, String tag, Actor operator, String data) {
        Message msg = new Message(tag, operator, data);
        recipient.receive(msg);
    }

    public void send(Actor recipient, Message msg) {
        recipient.receive(msg);
    }
}

class Message {
    private String tag;
    private String data;
    private Actor operator;

    public Message(String tag, Actor operator, String data) {
        this.tag = tag;
        this.operator = operator;
        this.data = data;
    }

    public String getTag() {
        return tag;
    }

    public String getData() {
        return data;
    }

    public Actor getOperator() {
        return operator;
    }
}


class DataStorageManager extends Actor{

    private final String DELIMITER = "[^a-zA-Z0-9]+";

    private List<String> words;
    private Actor stopWordManager;

    private void init(Message msg) {
        stopWordManager = msg.getOperator();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(msg.getData())));
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

    private  void process(Message msg) {
        Actor recipient = msg.getOperator();
        for (String w: words) {
            send(stopWordManager, "filter", null, w);
        }
        send(stopWordManager, "top25", recipient, null);
    }

    @Override
    public void work(Message msg) {
        if (msg.getTag().equals("init")) {
            init(msg);
        } else if (msg.getTag().equals("send_word_freqs")) {
            process(msg);
        } else {
            send(stopWordManager, msg);
        }
    }
}

class StopWordManager extends Actor {

    private final String STOP_WORDS_FILE = "stop_words.txt";
    private final String DELIMITER = "[^a-zA-Z0-9]+";

    private Set<String> stopWords;
    private Actor wordFreqManager;

    StopWordManager() {
        super();
        stopWords = new HashSet<>();
    }

    private void init(Message msg) {
        wordFreqManager = msg.getOperator();
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

    private void filter(Message msg) {
        String word = msg.getData();
        if (!stopWords.contains(word)) {
            send(wordFreqManager, "word", null, word);
        }
    }

    @Override
    public void work(Message msg) {
        if (msg.getTag().equals("init")) {
            init(msg);
        } else if (msg.getTag().equals("filter")) {
            filter(msg);
        } else {
            send(wordFreqManager, msg);
        }
    }

}

class WordFrequencyManager extends Actor {

    private Map<String, Integer> frequencies;

    WordFrequencyManager() {
        super();
        frequencies = new HashMap<>();
    }

    private void incrementCount(Message msg) {
        String word = msg.getData();
        frequencies.put(word, frequencies.getOrDefault(word, 0) + 1);
    }

    private void top25(Message msg) {
        Actor recipient = msg.getOperator();
        List<Map.Entry<String, Integer>> sortList = new ArrayList<>(frequencies.entrySet());
        sortList.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 25 && i < sortList.size(); i++) {
            Map.Entry<String, Integer> item = sortList.get(i);
            sb.append(String.format("%s  -  %s\n", item.getKey(), item.getValue()));
        }
        send(recipient, "top25", null, sb.toString());
    }

    @Override
    public void work(Message msg) {
        try {
            if (msg.getTag().equals("word")) {
                incrementCount(msg);
            } else if (msg.getTag().equals("top25")) {
                top25(msg);
            } else if (!msg.getTag().equals("stop")){
                throw new Exception("TwentyEight.Message not understood.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class WordFrequencyController extends Actor {

    private Actor storageManager;

    WordFrequencyController() {
        super();
    }

    @Override
    public void work(Message msg) {
        if (msg.getTag().equals("run")) {
            run(msg);
        } else if (msg.getTag().equals("top25")) {
            display(msg);
        }
    }

    private void run(Message msg) {
        storageManager = msg.getOperator();
        send(storageManager, "send_word_freqs", this, null);
    }

    private void display(Message msg) {
        System.out.print(msg.getData());
        send(storageManager, "stop", null, null);
        super.setState(STATE.dead);
    }
}