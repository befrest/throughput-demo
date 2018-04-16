package rest.bef.demo.model.job;

import redis.clients.jedis.Jedis;
import rest.bef.demo.data.dto.MessageDTO;
import rest.bef.demo.data.dto.PublishDTO;
import rest.bef.demo.data.dto.ReportDTO;
import rest.bef.demo.data.redis.JedisSession;
import rest.bef.demo.data.redis.JedisUtil;
import rest.bef.demo.model.service.BefrestService;
import rest.bef.demo.util.StringUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BurstPublishJob implements Runnable {

    private static final String AVG = "avg";
    private static final String SUM = "sum";
    private static final String STDD = "stdd";

    private int publishesCount;
    private String clientId;

    private String burstMessagesKey;
    private String burstStatsKey;
    private String burstReportKey;

    public BurstPublishJob(String clientId, int publishesCount) {
        this.publishesCount = publishesCount;
        this.clientId = clientId;

        this.burstMessagesKey = "burst-msgs-" + clientId;
        this.burstStatsKey = "burst-stats-" + clientId;
        this.burstReportKey = "burst-report-" + clientId;
    }

    public BurstPublishJob(String clientId) {
        this.burstMessagesKey = "burst-msgs-" + clientId;
        this.burstStatsKey = "burst-stats-" + clientId;
        this.burstReportKey = "burst-report-" + clientId;
    }

    @Override
    public void run() {

        try (Jedis jedis = JedisUtil.getJmJedis()) {

            jedis.del(burstMessagesKey);
            jedis.del(burstReportKey);
            jedis.del(burstStatsKey);

            int counter = 0;
            for (int i = 0; i < publishesCount; i++) {
                String msgBody = clientId + ".msg." + i;
                PublishDTO dto = BefrestService.publish(clientId, msgBody);

                if (!StringUtil.isValid(dto.getMessageId()))
                    continue;

                jedis.zadd(burstMessagesKey, System.currentTimeMillis(), dto.getMessageId());
                if (++counter % 1000 == 0)
                    publish(String.format("%d msgs published", counter));
            }

            publish("all messages published");
            publish("start checking messages stats ...");

            sleep();

            counter = 0;
            Set<String> members = jedis.zrange(burstMessagesKey, 0, Integer.MAX_VALUE);
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
                    jedis.hset(burstStatsKey, msg, dlvTime + "");

                    if (++counter % 1000 == 0) {
                        publish(String.format("%d message stats retrieved", counter));
                    }
                }
            }

            publish("all stats retrieved");

            publish("generating report ...");

            List<String> stats = jedis.hvals(burstStatsKey);
            double sum = 0/*, stdd = 0*/, avg;

            for (String dlvToken : stats) sum = sum + Integer.parseInt(dlvToken);

            avg = sum / stats.size();

            jedis.hset(burstReportKey, AVG, avg + "");
            jedis.hset(burstReportKey, SUM, sum + "");

            publish("all done");
        }
    }

    private void publish(String text) {
        BefrestService.publish(String.format("[%s] %s", clientId, text));
    }

    private void sleep() {
        try {
            Thread.sleep(10000);
        } catch (Exception ignored) {

        }
    }

    public ReportDTO getReport() {

        Jedis jedis = JedisSession.get();

        Double avg = Double.parseDouble(jedis.hget(burstReportKey, AVG));
        Double sum = Double.parseDouble(jedis.hget(burstReportKey, SUM));
//        Double stdd = Double.parseDouble(jedis.hget(burstReportKey, STDD));

        return new ReportDTO(sum, avg, 0.0);
    }
}
