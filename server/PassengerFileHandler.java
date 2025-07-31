package server;

import model.Message;
import model.Passenger;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PassengerFileHandler {
    private static final String FILE_PATH = "data/passengers.txt";
    private static final String DELIMITER = ",";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    static {
        initializeFile();
    }

    private static void initializeFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error initializing passenger file: " + e.getMessage());
            }
        }
    }

    static boolean loginCheck(String email, String name, String password) {
        lock.readLock().lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length >= 3 && parts[0].equals(email) && parts[1].equals(name) && parts[2].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error during login check: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    static boolean emailExists(String email) {
        lock.readLock().lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length > 0 && parts[0].equals(email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking email existence: " + e.getMessage());
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    static boolean updatePassenger(String email, String newName, String newPassword) {
        lock.writeLock().lock();
        try {
            List<Passenger> passengers = new ArrayList<>();
            boolean found = false;

            // Read all passengers
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(DELIMITER);
                    if (parts.length >= 3) {
                        if (parts[0].equals(email)) {
                            // Preserve the original name if newName is null (password change only)
                            String nameToUse = (newName != null && !newName.isEmpty()) ? newName : parts[1];
                            String passwordToUse = (newPassword != null && !newPassword.isEmpty()) ? newPassword : parts[2];
                            passengers.add(new Passenger(email, nameToUse, passwordToUse));
                            found = true;
                        } else {
                            passengers.add(new Passenger(parts[0], parts[1], parts[2]));
                        }
                    }
                }
            }

            if (!found) {
                return false;
            }

            // Write all passengers back
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
                for (Passenger passenger : passengers) {
                    writer.write(passengerToCSV(passenger));
                    writer.newLine();
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error updating passenger: " + e.getMessage());
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    static void signUp(Passenger passenger) {
        lock.writeLock().lock();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(passengerToCSV(passenger));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static String getNotifications(String email) {
        lock.readLock().lock();
        String filePath = "data/PassengerNotification/" + email + ".txt";
        StringBuilder notifications = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            List<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }

            // Reverse the list
            Collections.reverse(lines);

            // Append lines in reverse order
            for (String reversedLine : lines) {
                notifications.append(reversedLine).append("//");
            }
            return notifications.toString();
        } catch (FileNotFoundException e) {
            // Create the file if it doesn't exist
            try {
                File file = new File(filePath);
                file.getParentFile().mkdirs(); // Ensure directory exists
                file.createNewFile(); // Create empty file
                System.out.println("File created: " + filePath);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            return ""; // Return empty notification content
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        } finally {
            lock.readLock().unlock();
        }
    }

    public static boolean writeMsg(Message msg) {
        lock.writeLock().lock();
        String filePath = "data/PassengerNotification/"+msg.getReceiver()+".txt";
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


    private static String passengerToCSV(Passenger passenger) {
        return String.join(DELIMITER, passenger.getEmail(), passenger.getName(), passenger.getPassword());
    }
}