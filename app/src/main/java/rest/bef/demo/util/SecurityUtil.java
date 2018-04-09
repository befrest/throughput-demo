package rest.bef.demo.util;

import java.util.UUID;

@SuppressWarnings("SpellCheckingInspection")
public class SecurityUtil {
    public static String getToken() {
        return EncryptUtil.md5(String.format(
                "%s_%s*%s", UUID.randomUUID().toString(), System.currentTimeMillis(), UUID.randomUUID().toString()));
    }

    public static String getPasswordHash(String password) {
        return EncryptUtil.md5(
                String.format("%s    %s",
                        EncryptUtil.md5(EncryptUtil.sha1(password)),
                        "!MY-BLOVED_PH-SY^")
        );
    }
}
