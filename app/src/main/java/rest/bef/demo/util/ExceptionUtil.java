package rest.bef.demo.util;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.request.SendMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExceptionUtil {

    private static Logger LOGGER = LogManager.getLogger();
    private static final TelegramBot bot = TelegramBotAdapter.build("581386281:AAHxbaAOA2CHcDk-pJmc6qwchmp2A1jtWck");
    private static String[] admins = {
            "41853469", // mohammad anisi
            "53972755", // mehdi bakhtiari
            "70026531"  // hojjat imani
    };

    public static void error(String msg, Exception e) {
        if (e != null)
            LOGGER.error(msg, e);
        else
            LOGGER.error(msg);

        botSend("error: " + msg);
    }

    public static void error(String msg) {
        LOGGER.error(msg);
        botSend("error: " + msg);
    }

    public static void warn(String msg, Exception e) {
        if (e != null)
            LOGGER.warn(msg, e);
        else
            LOGGER.warn(msg);
        botSend("warn: " + msg);
    }

    public static void warn(String msg) {
        LOGGER.warn(msg);
        botSend("warn: " + msg);
    }

    private static void botSend(String msg) {
        for (String adminId : admins) {
            SendMessage m = new SendMessage(adminId, msg);
            m.disableWebPagePreview(false);
            bot.execute(m);
        }
    }
}
