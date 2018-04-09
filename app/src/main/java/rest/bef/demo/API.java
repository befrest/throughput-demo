package rest.bef.demo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.lookup.MainMapLookup;
import redis.clients.jedis.Jedis;
import rest.bef.demo.data.Constants;
import rest.bef.demo.data.JsonTransformer;
import rest.bef.demo.data.dto.AckDTO;
import rest.bef.demo.data.dto.PublishDTO;
import rest.bef.demo.data.dto.ReportDTO;
import rest.bef.demo.data.dto.StatDTO;
import rest.bef.demo.data.hibernate.HibernateUtil;
import rest.bef.demo.data.redis.JedisSession;
import rest.bef.demo.model.service.BefrestServiceHelper;
import rest.bef.demo.util.ConfigUtil;
import rest.bef.demo.util.PidUtil;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.security.Security;
import java.util.List;
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

        HibernateUtil.getSession();

        PidUtil.register(pidFile);

        Integer basePort = ConfigUtil.getInt(processGroup + ".listen.port");
        Spark.port(basePort + processIndex - 1);
        Spark.threadPool(1000, 400, 60000);

        JsonTransformer transformer = new JsonTransformer();

        post("/api/channel/publish", (req, res) -> {
            String text = req.body();
            PublishDTO dto = BefrestServiceHelper.publish(text);
            if (dto != null)
                return new AckDTO<>(Constants.System.OKAY, "published", dto);

            return new AckDTO<>(Constants.System.GENERAL_ERROR);
        }, transformer);

        get("/api/channel/stat", (req, res) -> {
            StatDTO dto = BefrestServiceHelper.channelStatus();
            if (dto != null)
                return new AckDTO<>(Constants.System.OKAY, "stat fetched", dto);

            return new AckDTO<>(Constants.System.GENERAL_ERROR);
        }, transformer);

        get("/api/channel/report", (req, res) -> {
            Jedis jedis = JedisSession.get();

            List<String> dlvTokens = jedis.lrange("demo", 0, Integer.MAX_VALUE);

            if (dlvTokens == null || dlvTokens.isEmpty())
                return new AckDTO<>(Constants.System.OKAY, "report generated", new ReportDTO(0.0, 0.0, 0.0));

            double sum = 0, stdd = 0, avg;

            for (String dlvToken : dlvTokens) sum += Integer.parseInt(dlvToken);

            avg = sum / dlvTokens.size();

            for (String dlvToken : dlvTokens) stdd += Math.pow(Integer.parseInt(dlvToken) - avg, 2);

            return new AckDTO<>(Constants.System.OKAY, "report generated", new ReportDTO(sum, avg, stdd));
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
