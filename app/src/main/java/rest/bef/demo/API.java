package rest.bef.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.MainMapLookup;
import rest.bef.demo.data.Constants;
import rest.bef.demo.data.JsonTransformer;
import rest.bef.demo.data.dto.*;
import rest.bef.demo.data.redis.JedisSession;
import rest.bef.demo.model.job.BurstPublishJob;
import rest.bef.demo.model.job.JobThreadPool;
import rest.bef.demo.model.service.BefrestService;
import rest.bef.demo.util.ConfigUtil;
import rest.bef.demo.util.PidUtil;
import rest.bef.demo.util.StringUtil;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.security.Security;
import java.util.Map;

import static spark.Spark.*;


public class API {

    static {
        Security.setProperty("networkaddress.cache.ttl", "600");
        Security.setProperty("networkaddress.cache.negative.ttl", "0");
        Security.setProperty("http.keepAlive", "true");
        Security.setProperty("http.maxConnections", "100");
    }

    private static Logger LOGGER;

    public static void main(String[] args) throws Exception {

        String processGroup = args[0];
        int processIndex = Integer.parseInt(args[1]);

        String logFile = processGroup + "." + processIndex + ".log";
        String pidFile = processGroup + "." + processIndex + ".pid";

        MainMapLookup.setMainArguments(logFile);
        LOGGER = LogManager.getLogger();

        PidUtil.register(pidFile);

        Integer basePort = ConfigUtil.getInt(processGroup + ".listen.port");
        Spark.port(basePort + processIndex - 1);
        Spark.threadPool(1000, 400, 60000);

        JsonTransformer transformer = new JsonTransformer();

        post("/api/channel/:channelId/publish", (req, res) -> {

            String channelId = req.params(":channelId");
            String text = req.body();
            PublishDTO dto = BefrestService.publish(channelId, text);
            if (dto != null)
                return new AckDTO<>(Constants.System.OKAY, "published", dto);

            return new AckDTO<>(Constants.System.GENERAL_ERROR);
        }, transformer);

        get("/api/channel/:channelId/stat", (req, res) -> {

            String channelId = req.params(":channelId");
            StatDTO dto = BefrestService.channelStatus(channelId);
            if (dto != null)
                return new AckDTO<>(Constants.System.OKAY, "stat fetched", dto);

            return new AckDTO<>(Constants.System.GENERAL_ERROR);
        }, transformer);

        get("/api/channel/:channelId/auth/sub", (req, res) -> {
            String channelId = req.params(":channelId");

            return new AckDTO<>(Constants.System.OKAY, BefrestService.generateSubscriptionAuth(channelId));
        }, transformer);

        get("/api/message/:messageId/stat", (req, res) -> {

            String messageId = req.params(":messageId");
            if (!StringUtil.isValid(messageId))
                return new AckDTO<>(Constants.System.GENERAL_ERROR);

            MessageDTO dto = BefrestService.messageStatus(messageId);
            if (dto != null)
                return new AckDTO<>(Constants.System.OKAY, "stat fetched", dto);

            return new AckDTO<>(Constants.System.GENERAL_ERROR);
        }, transformer);

        post("/api/test/start/:cliCount/:pubsCount", (req, res) -> {
            String cliCount = req.params(":cliCount");
            String pubsCount = req.params(":pubsCount");

            for (int i = 1; i <= Integer.parseInt(cliCount); i++) {
                JobThreadPool.getInstance().submit(new BurstPublishJob("demo" + i, Integer.parseInt(pubsCount)));
            }

            return new AckDTO<>(Constants.System.OKAY);
        }, transformer);

        get("/api/test/report/:cliCount", (req, res) -> {

            try {
                int clientCount = Integer.parseInt(req.params(":cliCount"));
                ReportDTO report = new ReportDTO(0.0, 0.0, 0.0);
                for (int i = 1; i <= clientCount; i++) {
                    ReportDTO cliReport = new BurstPublishJob("demo" + i).getReport();

                    if (cliReport.getAvg() == null || cliReport.getStdd() == null || cliReport.getSum() == null)
                        continue;

                    report.setSum(report.getSum() + cliReport.getSum());
                }

                report.setAvg(report.getSum() / clientCount);

                return new AckDTO<>(Constants.System.OKAY, "report fetched", report);
            } catch (Exception e){
                LOGGER.error("", e);
                return new AckDTO<>(Constants.System.GENERAL_ERROR);
            }
        }, transformer);

        exception(Exception.class, (e, req, res) -> {

            JedisSession.closeSession();

            AckDTO<String> resp = new AckDTO<>();
            res.status(500);
            resp.setMessage(e.getMessage());
            resp.setErrorCode(500);

            try {
                res.body(transformer.render(resp));
            } catch (Exception e1) {
                LOGGER.debug("exception handling failed", e1);
            }

            logError(req, res, e);
        });

        after((req, res) -> {
            JedisSession.closeSession();

            res.type("application/json");
            logRequest(req, res);
        });

        options("*", (req, res) -> "{}");

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "content-type, access-control-allow-headers, access-control-allow-method, x-rs-id, x-rs-token");
            res.header("Access-Control-Request-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        });
    }

    private static void logRequest(Request req, Response res) {
        LOGGER.info(logLine(req, res));
    }

    private static void logError(Request req, Response res, Exception e) {
        LOGGER.error(logLine(req, res), e);
    }

    private static String logLine(Request req, Response res) {
        StringBuilder logLine = new StringBuilder();

        String logDelim = " - ";

        logLine
                .append(req.requestMethod())
                .append(" ")
                .append(req.uri())
                .append(logDelim);

        Map<String, String[]> parameterMap = req.raw().getParameterMap();
        for (String name : parameterMap.keySet()) {
            logLine.append(" ").append(name).append(":{");
            String[] value = parameterMap.get(name);
            for (String part : value) {
                if (part.length() < 2048)
                    logLine.append(part).append(",");
                else
                    logLine.append("length#").append(part.length()).append(",");
            }

            String body = req.body();
            if (body != null && body.length() < 2048)
                logLine.append(body).append(",");

            int lastIndex = logLine.length() - 1;
            if (logLine.charAt(lastIndex) == ',')
                logLine.delete(lastIndex, logLine.length());

            logLine.append("}");
        }

        int stat = res.raw().getStatus();

        if (stat == 0)
            stat = 200;

        logLine.append(logDelim).append(stat);
        return logLine.toString();
    }

}
