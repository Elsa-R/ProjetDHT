package helloWorld;

import peersim.edsim.*;

public class Message {

    public final static int HELLOWORLD = 0;

    private int type;
    private String content;
    private Long dest;

    Message(int type, String content, Long dest) {
        this.type = type;
        this.content = content;
        this.dest = dest;
    }

    public String getContent() {
        return this.content;
    }

    public int getType() {
        return this.type;
    }

    public Long getDest() {
        return this.dest;
    }

}