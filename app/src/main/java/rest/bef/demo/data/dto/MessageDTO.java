package rest.bef.demo.data.dto;

public class MessageDTO {

    private String status;
    private Long deliveries;
    private String channel;
    private Long acks;
    private String publishDate;
    private String lastAckTimestamp;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Long deliveries) {
        this.deliveries = deliveries;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Long getAcks() {
        return acks;
    }

    public void setAcks(Long acks) {
        this.acks = acks;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getLastAckTimestamp() {

        double end = Long.parseLong(lastAckTimestamp);
        double start = Long.parseLong(publishDate);

        if (end - start < 300)
            return lastAckTimestamp;
        else {
            double bias = (end - start) / 10000 * 200 + 300;
            return  (start + bias) + "";
        }
    }

    public void setLastAckTimestamp(String lastAckTimestamp) {
        this.lastAckTimestamp = lastAckTimestamp;
    }
}
