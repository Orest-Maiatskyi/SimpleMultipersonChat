package src.db;

import src.config.Config;
import src.secure.AES;
import src.secure.RSA;

import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class MessagesFileStorage {

    public static File storageFile;
    public static Key serverKey;
    public static boolean backup = Boolean.parseBoolean(Config.getValue("massagesStorageBackupFile.backup"));

    public static class BackupThread implements Runnable {

        @Override
        public void run() {
            try {
                while (backup) {
                    backup();
                    Thread.sleep(Integer.parseInt(Config.getValue("massagesStorageBackupFile.backupFrequency")));
                }
            } catch (InterruptedException e) { throw new RuntimeException(e); }
        }
    }

    public static void backup() {
        File backupDir = new File(Config.getValue("massagesStorageBackupFile.dir"));
        if (backupDir.mkdir())
            System.out.println("Messages backup dir create at: " + Config.getValue("massagesStorageBackupFile.dir"));

        DateTimeFormatter timeStampPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd_hh-mm-ss");

        File backupFile = new File(Config.getValue("massagesStorageBackupFile.dir") +
                timeStampPattern.format(LocalDateTime.now()) + ".txt");
        try {
            if (backupFile.createNewFile()) {
                Files.copy(storageFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Messages storage backup at: " + backupFile.getPath());
            }
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public static void open() {
        if (storageFile == null) {
            storageFile = new File(Config.getValue("massagesStorageFile.fullPath"));
            if (!storageFile.exists()) {
                File storageDir = new File(Config.getValue("massagesStorageFile.dir"));
                if (storageDir.mkdir())
                    System.out.println("Messages storage dir create at: " + Config.getValue("massagesStorageFile.dir"));
                try {
                    if (storageFile.createNewFile())
                        System.out.println("Messages storage file create at: " + Config.getValue("massagesStorageFile.fullPath"));
                }
                catch (IOException e) { throw new RuntimeException(e); }
            }
        }

        if (serverKey == null) {
            String fullPath = Config.getValue("secure.serverKeyFileDir") +
                    Config.getValue("secure.serverKeyFileName");

            if (new File(Config.getValue("secure.serverKeyFileDir")).mkdir())
                System.out.println("Server password dir create at: " + Config.getValue("secure.serverKeyFileDir"));

            File serverPasswordFullPath = new File(fullPath);

            try {
                if (serverPasswordFullPath.createNewFile()) {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(serverPasswordFullPath));
                    serverKey = AES.generateSymmetricKey(Integer.parseInt(Config.getValue("secure.serverKeyLength")));
                    writer.write(RSA.encodeBase64(serverKey.getEncoded()));
                    writer.close();
                } else {
                    Scanner myReader = new Scanner(serverPasswordFullPath);
                    String tempStringKey = "";
                    while (myReader.hasNextLine()) tempStringKey += myReader.nextLine();
                    myReader.close();
                    byte[] tempByteKey = RSA.decodeBase64(tempStringKey);
                    serverKey = new SecretKeySpec(tempByteKey, 0, tempByteKey.length, "AES");
                }
            } catch (IOException e) { throw new RuntimeException(e); }
        }

        new Thread(new BackupThread()).start();
    }

    public static void addMessage(LocalDateTime time, String nickname, String message) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile, true));
            writer.write("MESSAGE >> " + time.toString() + " " + nickname + "\n" +
                    RSA.encodeBase64(AES.encrypt(serverKey, message)));
            writer.newLine();
            writer.close();
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public static void getMessagesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {

    }

}
