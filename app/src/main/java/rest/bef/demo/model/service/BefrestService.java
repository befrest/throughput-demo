package rest.bef.demo.model.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import rest.bef.demo.data.dto.AckDTO;
import rest.bef.demo.data.dto.MessageDTO;
import rest.bef.demo.data.dto.PublishDTO;
import rest.bef.demo.data.dto.StatDTO;
import rest.bef.demo.util.HttpUtil;
import rest.bef.demo.util.StringUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class BefrestService {

    private static final String BEFREST_URL = "https://api.bef.rest";
    private static final String BEFREST_PUB_URL = "/xapi/1/publish/%d/%s";
    private static final String BEFREST_SUB_URL = "/xapi/1/subscribe/%d/%s/%d";
    private static final String BEFREST_STAT_URL = "/xapi/1/channel-status/%d/%s";
    private static final String BEFREST_MSTAT_URL = "/xapi/1/message-status/%d/%s";

    private static Logger LOGGER = LogManager.getLogger();
    private static final Gson MAPPER = new Gson();

    private static String CHANNEL = "demo";
    private static final long UID = 11386;

    public static PublishDTO publish(String channel, String text) {
        String auth = BefrestAuth.generatePublishAuth(channel);

        Map<String, String> headers = new HashMap<>();
        headers.put("X-BF-TTL", "0");
        headers.put("X-BF-AUTH", auth);

        try {
            String url = String.format(BEFREST_URL + BEFREST_PUB_URL, UID, channel);
            HttpRequestBase pubReq = HttpUtil.buildHttpRequest("POST", url,null, headers, text);
            String resp = HttpUtil.fetchRawResponse(pubReq, 10);

            if (StringUtil.isValid(resp)) {
                AckDTO<PublishDTO> response = MAPPER.fromJson(resp, new TypeToken<AckDTO<PublishDTO>>(){}.getType());

                if (response != null && response.getEntity() != null) {
                    return response.getEntity();
                }
            }

        } catch (Exception e) {
            LOGGER.error("error befrest publish", e);
        }

        return null;
    }

    public static PublishDTO publish(String text) {
        return publish(CHANNEL, text);
    }

    public static StatDTO channelStatus(String channel) {
        String auth = BefrestAuth.generateChannelStatusAuth(channel);

        Map<String, String> headers = new HashMap<>();
        headers.put("X-BF-AUTH", auth);

        try {
            String url = String.format(BEFREST_URL + BEFREST_STAT_URL, UID, channel);
            HttpRequestBase statReq = HttpUtil.buildHttpRequest("GET", url,null, headers, null);
            String resp = HttpUtil.fetchRawResponse(statReq, 10);

            if (StringUtil.isValid(resp)) {
                AckDTO<StatDTO> response = MAPPER.fromJson(resp, new TypeToken<AckDTO<StatDTO>>(){}.getType());

                if (response != null && response.getEntity() != null) {
                    return response.getEntity();
                }
            }

        } catch (Exception e) {
            LOGGER.error("error befrest channel-status", e);
        }

        return null;
    }

    public static StatDTO channelStatus() {
        return channelStatus(CHANNEL);
    }

    public static MessageDTO messageStatus(String messageId) {
        String auth = BefrestAuth.generateMessageStatusAuth(messageId);

        Map<String, String> headers = new HashMap<>();
        headers.put("X-BF-AUTH", auth);

        try {
            String url = String.format(BEFREST_URL + BEFREST_MSTAT_URL, UID, messageId);
            HttpRequestBase statReq = HttpUtil.buildHttpRequest("GET", url,null, headers, null);
            String resp = HttpUtil.fetchRawResponse(statReq, 10);

            if (StringUtil.isValid(resp)) {
                AckDTO<MessageDTO> response = MAPPER.fromJson(resp, new TypeToken<AckDTO<MessageDTO>>(){}.getType());

                if (response != null && response.getEntity() != null) {
                    return response.getEntity();
                }
            }

        } catch (Exception e) {
            LOGGER.error("error befrest message-status", e);
        }

        return null;
    }

    public static String generateSubscriptionAuth(String channel) {
        return BefrestAuth.generateSubscriptionAuth(channel, 2);
    }

    static class BefrestAuth {

        private static final String API_KEY = "1654C8FC86A7CCA494DB1577D240198A";
        private static final String SHARED_KEY = "2006E0894779A35BB5586AA8E49CAEAB";

        static String generatePublishAuth(String chid) {
            return generateAuthToken(String.format(BEFREST_PUB_URL, UID, chid));
        }

        static String generateSubscriptionAuth(String chid, int sdkVersion) {
            return generateAuthToken(String.format(BEFREST_SUB_URL, UID, chid, sdkVersion));
        }

        static String generateChannelStatusAuth(String chid) {
            return generateAuthToken(String.format(BEFREST_STAT_URL, UID, chid));
        }

        static String generateMessageStatusAuth(String mid) {
            return generateAuthToken(String.format(BEFREST_MSTAT_URL, UID, mid));
        }

        /**
         * @param addr Please refer to https://bef.rest/documentation#generating-auth-string for instructions on
         *             how to build the addr parameter
         */
        private static String generateAuthToken(String addr) {
            try {
                String initialPayload = String.format("%s,%s", API_KEY, addr);
                byte[] md5 = md5(initialPayload);
                String base64 = base64Encode(md5);

                String payload = String.format("%s,%s", SHARED_KEY, base64);
                md5 = md5(payload);
                return base64Encode(md5);
            } catch (Exception e) {
                // Log the occurred exception
                return null;
            }
        }

        private static byte[] md5(String input) throws NoSuchAlgorithmException {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(input.getBytes());
            return messageDigest.digest();
        }

        private static String base64Encode(byte[] input) {
            return new String(Base64.encodeBase64(input))
                    .replaceAll("\\+", "-")
                    .replaceAll("=", "")
                    .replaceAll("/", "_");
        }
    }

    public static void main(String[] args) throws InterruptedException {

        PublishDTO pub = BefrestService.publish("salam");
        System.out.println("channelId: " + pub.getChannelId());
        System.out.println("messageId: " + pub.getMessageId());

        Thread.sleep(4000);

        MessageDTO mdto = BefrestService.messageStatus(pub.getMessageId());
        System.out.println("message acks: " + mdto.getAcks());
        System.out.println("message channel: " + mdto.getChannel());
        System.out.println("message dlvs" + mdto.getDeliveries());
        System.out.println("message time to deliver: " + (Long.parseLong(mdto.getLastAckTimestamp()) - Long.parseLong(mdto.getPublishDate())));
        System.out.println("message status: " + mdto.getStatus());

        StatDTO demo = BefrestService.channelStatus();
        System.out.println("channel subscribers: " + demo.getSubscribers());
        System.out.println("channel published messages: " + demo.getPublishedMessages());
        System.out.println("channel stored messages: " + demo.getStoredMessages());
    }

}
