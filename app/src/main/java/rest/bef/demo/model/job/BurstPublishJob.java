package rest.bef.demo.model.job;

import redis.clients.jedis.Jedis;
import rest.bef.demo.data.dto.MessageDTO;
import rest.bef.demo.data.dto.PublishDTO;
import rest.bef.demo.data.dto.ReportDTO;
import rest.bef.demo.data.redis.JedisSession;
import rest.bef.demo.data.redis.JedisUtil;
import rest.bef.demo.model.service.BefrestService;
import rest.bef.demo.util.StringUtil;

import java.util.Map;
import java.util.Set;

public class BurstPublishJob implements Runnable {

    private static final String REDIS_BURST_MSGS = "burst-msgs";
    private static final String REDIS_BURST_STATS = "burst-stats";
    private static final String REDIS_BURST_REPORT = "burst-report";

    private int channelsCount;
    private int publishesCount;

    public BurstPublishJob(int channelsCount, int publishesCount) {
        this.channelsCount = channelsCount;
        this.publishesCount = publishesCount;
    }

    @Override
    public void run() {

        try (Jedis jedis = JedisUtil.getJmJedis()) {

            int counter = 0;
            for (int i = 0; i < publishesCount; i++) {
                for (int j = 1; j <= channelsCount; j++) {
                    String client = "cli" + j;
                    PublishDTO dto = BefrestService.publish(client, "msg-" + client);

                    if (!StringUtil.isValid(dto.getMessageId()))
                        continue;

                    jedis.zadd(REDIS_BURST_MSGS, System.currentTimeMillis(), dto.getMessageId());
                    if (++counter % 1000 == 0) {
                        BefrestService.publish(String.format("%d msgs published", counter));
                    }
                }
            }

            BefrestService.publish("all messages published");
            BefrestService.publish("start checking messages stats ...");
            sleep();

            counter = 0;
            Set<String> members = jedis.zrange(REDIS_BURST_MSGS, 0, Integer.MAX_VALUE);
            for (String msg : members) {
                MessageDTO mstatus = BefrestService.messageStatus(msg);

                if (mstatus == null)
                    continue;

                String publishDate = mstatus.getPublishDate();
                String lastAckTimestamp = mstatus.getLastAckTimestamp();

                if (!StringUtil.isValid(lastAckTimestamp))
                    lastAckTimestamp = publishDate;

                if (StringUtil.isNumeric(publishDate) && StringUtil.isNumeric(lastAckTimestamp)) {
                    Long dlvTime = Long.parseLong(lastAckTimestamp) - Long.parseLong(publishDate);
                    jedis.hset(REDIS_BURST_STATS, msg, dlvTime + "");

                    if (++counter % 1000 == 0) {
                        BefrestService.publish(String.format("%d message stats retrieved", counter));
                    }
                }
            }

            BefrestService.publish("all stats retrieved");

            BefrestService.publish("generating report ...");

            Map<String, String> stats = jedis.hgetAll(REDIS_BURST_STATS);
            double sum = 0, stdd = 0, avg;

            for (String dlvToken : stats.values()) sum += Integer.parseInt(dlvToken);

            avg = sum / stats.values().size();

            for (String dlvToken : stats.values()) stdd += Math.pow(Integer.parseInt(dlvToken) - avg, 2);

            jedis.hset(REDIS_BURST_REPORT, "avg", avg + "");
            jedis.hset(REDIS_BURST_REPORT, "sum", sum + "");
            jedis.hset(REDIS_BURST_REPORT, "stdd", stdd + "");

            BefrestService.publish("all done");
        }
    }

    private void sleep() {
        try {
            Thread.sleep(5000);
        } catch (Exception ignored) {

        }
    }

    public static ReportDTO getReport() {

        Jedis jedis = JedisSession.get();

        Double avg = Double.parseDouble(jedis.hget(REDIS_BURST_REPORT, "avg"));
        Double sum = Double.parseDouble(jedis.hget(REDIS_BURST_REPORT, "sum"));
        Double stdd = Double.parseDouble(jedis.hget(REDIS_BURST_REPORT, "stdd"));

        return new ReportDTO(sum, avg, stdd);
    }
}
