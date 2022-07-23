package src.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

    public static Properties properties;

    public static void open(String configPath) {
        File configFile = new File(configPath);
        if (!configFile.exists()) createDefault(configPath);
        else {
            try {
                FileInputStream fileInputStream = new FileInputStream(configFile);
                properties = new Properties();
                properties.load(fileInputStream);
                fileInputStream.close();
            } catch (IOException e) { throw new RuntimeException(e); }
        }
    }

    public static void createDefault(String configPath) {
        File configFile = new File(configPath);
        try {
            if (configFile.createNewFile()) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(configFile)) {
                    properties = new Properties();
                    properties.setProperty("server.ip", "localhost");
                    properties.setProperty("server.port", "8080");
                    properties.setProperty("server.backlog", "50");
                    properties.setProperty("server.password", "12345");

                    properties.setProperty("secure.RSAKeyLength", "2048");
                    properties.setProperty("secure.serverKeyLength", "256");
                    properties.setProperty("secure.serverKeyFileDir", "./keys/");
                    properties.setProperty("secure.serverKeyFileName", "serverKey.txt");

                    properties.setProperty("logs.on", "true");
                    properties.setProperty("logs.dir", "./logs/");

                    properties.setProperty("userDB.dir", "./");
                    properties.setProperty("userDB.name", "user_db.db");
                    properties.setProperty("userDB.fullPath",
                            properties.getProperty("userDB.dir") + properties.getProperty("userDB.name"));

                    properties.setProperty("messagesStorageFile.dir", "./");
                    properties.setProperty("messagesStorageFile.name", "messages.txt");
                    properties.setProperty("messagesStorageFile.fullPath",
                            properties.getProperty("messagesStorageFile.dir") + properties.getProperty("messagesStorageFile.name"));

                    properties.setProperty("messagesStorageBackupFile.backup", "true");
                    properties.setProperty("messagesStorageBackupFile.backupFrequency", "1800000");
                    properties.setProperty("messagesStorageBackupFile.dir", "./backup/");

                    properties.store(fileOutputStream, null);
                }
            }

        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public static String getValue(String key) { return properties.getProperty(key); }

}
