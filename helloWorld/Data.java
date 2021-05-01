package helloWorld;

import java.util.UUID;

public class Data {
	
    private long uuid = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    private String content;

    public Data(String content) {
        this.content = content;
    }
    
    public String getContent() {
    	return this.content;
    }

    public long getUUID() {
        return this.uuid;
    }
}