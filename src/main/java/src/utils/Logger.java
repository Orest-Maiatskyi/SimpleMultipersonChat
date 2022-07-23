package src.utils;


import src.config.Config;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    public static File logFile = null;

    public static synchronized void writeLog(String log) {
        if (Boolean.parseBoolean(Config.getValue("logs.on"))) {
            if (logFile != null) {
                try (FileWriter fileWriter = new FileWriter(logFile, true)) { fileWriter.write(log + "\n"); }
                catch (IOException e) { throw new RuntimeException(e); }
            } else {
                try {
                    DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");
                    String fileName = timeStampPattern.format(LocalDateTime.now()) + ".log";
                    File dir = new File(Config.getValue("logs.dir"));
                    if (dir.mkdirs())
                        System.out.println("Logs dir created at: " + Config.getValue("logs.dir"));
                    logFile = new File(Config.getValue("logs.dir") + fileName);
                    if (logFile.createNewFile())
                        System.out.println("Log file created at: " + Config.getValue("logs.dir") + fileName);
                    writeLog(log);
                } catch (IOException e) { throw new RuntimeException(e); }
            }
        }
    }

    public static synchronized void writeLog(Status status) { writeLog(status.getLogForServer()); }

}
