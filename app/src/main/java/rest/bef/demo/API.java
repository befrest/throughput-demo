package rest.bef.demo;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.MainMapLookup;
import rest.bef.demo.data.Constants;
import rest.bef.demo.data.JsonTransformer;
import rest.bef.demo.data.dto.*;
import rest.bef.demo.data.hibernate.HibernateUtil;
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
    private static Gson MAPPER = new Gson();

    public static void main(String[] args) throws Exception {

        String processGroup = args[0];
        int processIndex = Integer.parseInt(args[1]);

        String logFile = processGroup + "." + processIndex + ".log";
        String pidFile = processGroup + "." + processIndex + ".pid";

        MainMapLookup.setMainArguments(logFile);
        LOGGER = LogManager.getLogger();

        HibernateUtil.getSession();

        PidUtil.register(pidFile);

        Integer basePort = ConfigUtil.getInt(processGroup + ".listen.port");
        Spark.port(basePort + processIndex - 1);
        Spark.threadPool(1000, 400, 60000);

        JsonTransformer transformer = new JsonTransformer();

        post("/api/channel/:chid/publish", (req, res) -> {

            String text = req.body();
            PublishDTO dto = BefrestService.publish(text);
            if (dto != null)
                return new AckDTO<>(Constants.System.OKAY, "published", dto);

            return new AckDTO<>(Constants.System.GENERAL_ERROR);
        }, transformer);

        get("/api/channel/:chid/stat", (req, res) -> {

            StatDTO dto = BefrestService.channelStatus();
            if (dto != null)
                return new AckDTO<>(Constants.System.OKAY, "stat fetched", dto);

            return new AckDTO<>(Constants.System.GENERAL_ERROR);
        }, transformer);

        get("/api/channel/:channelId/auth/sub", (req, res) -> {
            String channelId = req.params(":channelId");
            return new AckDTO<>(Constants.System.OKAY, BefrestService.generateSubscriptionAuth(channelId));
        }, transformer);

        get("/api/message/:messageId/stat", (req, res) -> {

            String messageId = req.params("messageId");
            if (!StringUtil.isValid(messageId))
                return new AckDTO<>(Constants.System.GENERAL_ERROR);

            MessageDTO dto = BefrestService.messageStatus(messageId);
            if (dto != null)
                return new AckDTO<>(Constants.System.OKAY, "stat fetched", dto);

            return new AckDTO<>(Constants.System.GENERAL_ERROR);
        }, transformer);

        post("/api/test/start", (req, res) -> {
            JobThreadPool.getInstance().submit(new BurstPublishJob());

            return new AckDTO<>(Constants.System.GENERAL_ERROR);
        }, transformer);

        get("/api/test/report", (req, res) -> {

            ReportDTO report = BurstPublishJob.getReport();
            if (report.getAvg() == null || report.getStdd() == null || report.getSum() == null)
                return new AckDTO<>(Constants.System.GENERAL_ERROR);

            return new AckDTO<>(Constants.System.OKAY, "report fetched", report);
        }, transformer);

        exception(Exception.class, (e, req, res) -> {

            HibernateUtil.closeSession();
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
            HibernateUtil.closeSession();
            JedisSession.closeSession();

            res.type("application/json");
            logRequest(req, res);
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
