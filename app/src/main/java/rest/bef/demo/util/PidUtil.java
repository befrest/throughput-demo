package rest.bef.demo.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

public class PidUtil {

    private static Long PID;

    public static long getPid() {
        if (PID == null) {
            String[] nameTokens = ManagementFactory.getRuntimeMXBean().getName().split("@");
            PID = Long.valueOf(nameTokens[0]);
        }

        return PID;
    }

    public static void register(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(String.valueOf(getPid()));
        writer.flush();
        writer.close();
    }
}
