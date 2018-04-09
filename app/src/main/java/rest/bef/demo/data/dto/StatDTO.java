package rest.bef.demo.data.dto;

public class StatDTO {
    
    private String storedMessages;
    private String publishedMessages;
    private Integer subscribers;

    public String getStoredMessages() {
        return storedMessages;
    }

    public void setStoredMessages(String storedMessages) {
        this.storedMessages = storedMessages;
    }

    public String getPublishedMessages() {
        return publishedMessages;
    }

    public void setPublishedMessages(String publishedMessages) {
        this.publishedMessages = publishedMessages;
    }

    public Integer getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(Integer subscribers) {
        this.subscribers = subscribers;
    }
}
