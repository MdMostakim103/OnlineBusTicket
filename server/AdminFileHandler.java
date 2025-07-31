package server;

import model.Message;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AdminFileHandler {
    private static final String ADMIN_FILE_NAME = "data/admin.txt";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public static boolean checkLogIn(String name, String pass) {
        lock.readLock().lock();
        try{
            BufferedReader reader =new BufferedReader(new FileReader(ADMIN_FILE_NAME));
            String line;
            while((line= reader.readLine())!=null){
                String[] parts = line.split(",");
                if(parts.length>=2 && parts[0].equals(name) && parts[1].equals(pass)){
                    return true;
                }
            }
            return false;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }
    public static boolean changePass(String name, String pass) {
        lock.writeLock().lock();
        try {
            File file = new File(ADMIN_FILE_NAME);
            if (!file.exists()) return false;

            List<String> updatedLines = new ArrayList<>();
            boolean found = false;

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2 && parts[0].equals(name)) {
                        updatedLines.add(name + "," + pass); // Replace line
                        found = true;
                    } else {
                        updatedLines.add(line); // Keep original line
                    }
                }
            }

            if (!found) return false;

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                for (String updatedLine : updatedLines) {
                    writer.write(updatedLine);
                    writer.newLine();
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static String loadNotification(String name) {
        lock.readLock().lock();
        String filePath = "data/AdminNotification/"+name+".txt";
        StringBuilder notifications= new StringBuilder();
        try(BufferedReader reader=new BufferedReader(new FileReader(filePath))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            Collections.reverse(lines);
            for (String reversedLine : lines) {
                notifications.append(reversedLine).append("//");
            }
            return notifications.toString();
        } catch (IOException e) { return "file not found+name"+name;}
        finally {
            lock.readLock().unlock();
        }
    }

    public static boolean writeMsg(Message msg) {
        lock.writeLock().lock();
        String filePath = "data/AdminNotification/"+msg.getReceiver()+".txt";
        try{
            BufferedWriter writer=new BufferedWriter(new FileWriter(filePath,true));
            writer.write(msg.getSender()+","+msg.getMessage()+","+ LocalDate.now().toString() );
            writer.newLine();
            writer.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }   finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean checkName(String admin) {
        lock.readLock().lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(ADMIN_FILE_NAME))) {
            String line;
            while((line= reader.readLine())!=null){
                String[] parts = line.split(",");
                if(parts.length>=2 && parts[0].equals(admin)){
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            lock.readLock().unlock();
        }
    }
}
