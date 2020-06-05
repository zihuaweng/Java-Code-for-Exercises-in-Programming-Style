public class Message {
    private String tag;
    private String data;

    public Message(String tag, String data) {
        this.tag = tag;
        this.data = data;
    }

    public Message(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public String getData() {
        return data;
    }
}



