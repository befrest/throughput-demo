package rest.bef.demo.util;

import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class MessageBot {
    private static Properties properties;

    static {
        final InputStream is;

        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream("particulars.properties");
            properties = new Properties();
            properties.load(new InputStreamReader(is, "utf8"));
        } catch (IOException e) {
            LogManager.getLogger().error(e.getMessage(), e);
        }
    }

    public static String getWrongInput(){
        return properties.getProperty("WRONG_INPUT");
    }

    public static String getOrderNow(){
        return properties.getProperty("ORDER_NOW");
    }

    public static String getStateDiscription(){
        return properties.getProperty("STATE_DESCRIPTION");
    }

    public static String getEnterYourNamePlease(){
        return properties.getProperty("ENTER_YOUR_NAME_PLEASE");
    }

    public static String getEnterYourPartnerName(){
        return properties.getProperty("ENTER_YOUR_PARTNER_NAME_PLEASE");
    }

    public static String getItsEnough(){
        return properties.getProperty("ITS_ENOUGH");
    }

    public static String getReset(){
        return properties.getProperty("RESET");
    }

    public static String getPhotoState(){
        return properties.getProperty("PHOTO_STATE");
    }

    public static String getTextState(){
        return properties.getProperty("TEXT_STATE");
    }

    public static String getSure(){
        return properties.getProperty("SURE");
    }

    public static String getPending(){
        return properties.getProperty("PENDING");
    }



    public static String getSixthStateYes(){ return properties.getProperty("SIXTH_STATE_YES"); }

    public static String getSixthStateNo(){ return properties.getProperty("SIXTH_STATE_NO"); }


    public static String getStartError(){
        return properties.getProperty("ERROR_START_STATE");
    }
    public static String getFirstError(){
        return properties.getProperty("ERROR_FIRST_STATE");
    }
    public static String getSecondError(){
        return properties.getProperty("ERROR_SECOND_STATE");
    }
    public static String getThirdError(){
        return properties.getProperty("ERROR_THIRD_STATE");
    }
    public static String getForthError(){
        return properties.getProperty("ERROR_FORTH_STATE");
    }
    public static String getFifthError(){
        return properties.getProperty("ERROR_SIXTH_STATE");
    }
    public static String getSixthError(){
        return properties.getProperty("ERROR_SIXTH_STATE");
    }
    public static String getSeventhError(){
        return properties.getProperty("ERROR_SEVENTH_STATE");
    }

    public static String getEnoughPhotoError(){return properties.getProperty("ENOUGH_PHOTO");}

    public static String getMessageBadClient(){ return properties.getProperty("BAD_CLIENT_SEND_TOO_MANY_PHOTO"); }

    public static String getMessageWantSendPhoto(){ return properties.getProperty("YOU_WANT_SEND_PHOTO");}

}
