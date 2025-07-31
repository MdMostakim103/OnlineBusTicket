package server;

import model.*;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BusFileHandler {
    private static final String FILE_PATH = "data/bus_owners.txt";
    private static final String DELIMITER = ",";
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);


    public static void saveBusOwner(BusOwner busOwner) {
        File file=new File("data/bus_owners.txt");
        if(!file.exists()){
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        lock.writeLock().lock();
        try(BufferedWriter writer=new BufferedWriter(new FileWriter(FILE_PATH,true))) {
            writer.write(busOwner.toCSV());
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean updateBusOwner(BusOwner busOwner) {
        lock.writeLock().lock();

        List<BusOwner> busOwners = new ArrayList<>();
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if(busOwner.getBusId().equals(parts[0])){
                    busOwners.add(busOwner);
                    found = true;
                }
                else{
                    busOwners.add(new BusOwner(parts[0], parts[1], parts[2], parts[3]));
                }
            }

            if(!found){
                return false;
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
                for (BusOwner busOwner1 : busOwners) {
                    writer.write(busOwner1.toCSV());
                    writer.newLine();
                }
            }

            return true;

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean confirmBooking(String str,String busId,String date) throws IOException {
        lock.writeLock().lock();
        String filePath = "data/BookedSeats/"+busId+"/"+date+".txt";
        try(BufferedWriter writer =new BufferedWriter(new FileWriter(filePath,true))) {
            writer.write(str);
            writer.newLine();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static boolean checkBusId(String busId) {
        lock.readLock().lock();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(DELIMITER);
                if (parts.length > 0 && parts[0].equals(busId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return false;
    }

    public static boolean logInBus(String busId,String password,String busName) {
        lock.readLock().lock();
        try{
            BufferedReader reader=new BufferedReader(new FileReader(FILE_PATH));
            String line;
            while((line=reader.readLine())!=null){
                String[] parts=line.split(DELIMITER);
                if(parts.length>0&&parts[0].equals(busId)&&parts[1].equals(password)&&parts[2].equals(busName)){
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

    public static List<PassengerInfo> loadTodayPassengers(String busId) {
        lock.readLock().lock();
        LocalDate date = LocalDate.now();
        String path = "data/BookedSeats/" + busId + "/" + date + ".txt";
        List<PassengerInfo> passengerInfoList = new ArrayList<>();
        try {
            File file = new File(path);
            if (!file.exists()) {
                return passengerInfoList;
            }
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 5) {
                    PassengerInfo passenger = new PassengerInfo(data[0], data[1], data[2], data[3], data[4]);
                    passengerInfoList.add(passenger);
                }
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();  // or log it properly
        } finally {
            lock.readLock().unlock();
        }

        return passengerInfoList;
    }

    public static List<PassengerInfo> loadUpcomingPassenger(String busId) {
        lock.readLock().lock();
        List<PassengerInfo> passengers = new ArrayList<>();
        File dir = new File("data/BookedSeats/" + busId + "/");

        if (!dir.exists() || !dir.isDirectory()) {
            return passengers;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt"));
        if (files == null) return passengers;

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 5) {
                        String email = parts[0].trim();
                        String route = parts[1].trim();
                        String seats = parts[2].trim();
                        String fare = parts[3].trim();
                        String date = parts[4].trim();
                        PassengerInfo p = new PassengerInfo(email, route, seats, fare, date);
                        passengers.add(p);
                    }
                }
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error reading file: " + file.getName());
                e.printStackTrace();
            } finally {
                lock.readLock().unlock();
            }
        }
        return passengers;
    }

    public static String getEmail(String busId){
        lock.readLock().lock();
        try{
            BufferedReader reader=new BufferedReader(new FileReader(FILE_PATH));
            String line;
            while((line=reader.readLine())!=null){
                String[] parts=line.split(DELIMITER);
                if(parts[0].equals(busId)){
                    return parts[3];
                }
            }
            return "";
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }
    public static boolean saveRoute(String data, String busId) throws IOException {
        lock.writeLock().lock();  // Ensure thread-safety
        try {
            // Save route file
            String routePath = "data/routes/" + busId + ".txt";
            File routeFile = new File(routePath);
            if (!routeFile.exists()) {
                routeFile.getParentFile().mkdirs();
                routeFile.createNewFile();
            }

            // Create today's empty bookedSeats file
            String todayStr = java.time.LocalDate.now().toString(); // e.g., 2025-07-27
            String bookedSeatsPath = "data/BookedSeats/" + busId + "/" + todayStr + ".txt";
            File bookedSeatsFile = new File(bookedSeatsPath);
            if (!bookedSeatsFile.exists()) {
                bookedSeatsFile.getParentFile().mkdirs();
                bookedSeatsFile.createNewFile();
            }

            // Write route data
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(routeFile))) {
                writer.write(data);
                writer.newLine();
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }
    public static List<Bus> getBuses(String from, String to, String date) throws IOException {
        lock.readLock().lock();
        List<Bus> availableBuses = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.strip();
                String[] data = line.split(",");
                String Id = data[0];
                String busName = data[2];
                String busfile = "data/routes/" + Id + ".txt";
                try (BufferedReader br2 = new BufferedReader(new FileReader(busfile))) {
                    String line2;
                    boolean departureFound = false;
                    boolean destinationFound = false;
                    Double fare1=0.0;
                    Double fare2=0.0;
                    String availableSeats="40";
                    String time="00:00";
                    line2 = br2.readLine();
                    String []temp=line2.split(",");
                    availableSeats=temp[1];
                    while ((line2 = br2.readLine()) != null) {
                        line2 = line2.strip();
                        String[] busData = line2.split(",");

                        if (busData[0].equalsIgnoreCase(from)) {
                            departureFound = true;
                            fare1 = Double.parseDouble(busData[3]);
                            time=busData[1];
                        }
                        if (busData[0].equalsIgnoreCase(to) && departureFound) {
                            destinationFound = true;
                            fare2 = Double.parseDouble(busData[3]);
                            break;
                        }
                    }
                    if (destinationFound && departureFound) {
                        double fare=fare2-fare1;
                        String route=generateRoute(Id);
                        int seatnum=findSeat(Id,date,from,to,route);
                        Bus bus = new Bus(Id, busName, time,seatnum,fare );
                        availableBuses.add(bus);
                    }
                } catch (IOException e) {
                    System.err.println("Error reading bus route file: " + busfile);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException("Could not read bus owner credentials");
        } finally {
            lock.readLock().unlock();
        }
        return availableBuses;
    }

    public static String generateRoute(String busId) throws IOException {
        String routePath = "data/routes/" + busId + ".txt";
        String route="";
        try(BufferedReader reader=new BufferedReader(new FileReader(routePath))) {
            String line;
            line=reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                route+=data[0]+",";
            }
        }
        return route;
    }

    private static int findSeat(String busId, String date, String from, String to, String route) throws IOException {
        String filePath = "data/BookedSeats/" + busId + "/" + date + ".txt";
        String[] stops = route.split(",");
        int totalSeats = 40;

        int fromIndex = indexOfStop(stops, from);
        int toIndex = indexOfStop(stops, to);

        // Check for invalid segment
        if (fromIndex == -1 || toIndex == -1 || fromIndex >= toIndex) {
            System.err.println("Invalid stop segment: " + from + " to " + to);
            return 0;
        }

        // If no booking file exists yet, all seats are available
        File bookingFile = new File(filePath);
        if (!bookingFile.exists()) {
            return totalSeats;
        }

        Set<Integer> occupiedSeats = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(bookingFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                String[] bookedSegment = parts[1].split("-");
                if (bookedSegment.length != 2) continue;

                int bookedFrom = indexOfStop(stops, bookedSegment[0]);
                int bookedTo = indexOfStop(stops, bookedSegment[1]);

                if (bookedFrom == -1 || bookedTo == -1 || bookedFrom >= bookedTo) continue;

                // Check if segments overlap
                boolean overlaps = !(toIndex <= bookedFrom || fromIndex >= bookedTo);
                if (overlaps) {
                    String[] seats = parts[2].split("-");
                    for (String seat : seats) {
                        try {
                            occupiedSeats.add(Integer.parseInt(seat.trim()));
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seat number format: " + seat);
                        }
                    }
                }
            }
        }

        return totalSeats - occupiedSeats.size();
    }

    private static int indexOfStop(String[] stops, String stop) {
        for (int i = 0; i < stops.length; i++) {
            if (stops[i].trim().equals(stop.trim())) {
                return i;
            }
        }
        return -1;
    }
    public static List<Integer> getAvailableSeats(String busId, String date, String from, String to, String route) throws IOException {
        String filePath = "data/BookedSeats/" + busId + "/" + date + ".txt";
        String[] stops = route.split(",");
        int totalSeats = 40;

        int fromIndex = indexOfStop(stops, from);
        int toIndex = indexOfStop(stops, to);

        // Check for invalid segment
        if (fromIndex == -1 || toIndex == -1 || fromIndex >= toIndex) {
            System.err.println("Invalid stop segment: " + from + " to " + to);
            return Collections.emptyList();
        }

        // If no booking file exists yet, all seats are available
        File bookingFile = new File(filePath);
        if (!bookingFile.exists()) {
            List<Integer> allSeats = new ArrayList<>();
            for (int i = 1; i <= totalSeats; i++) {
                allSeats.add(i);
            }
            return allSeats;
        }

        Set<Integer> occupiedSeats = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(bookingFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 3) continue;

                String[] bookedSegment = parts[1].split("-");
                if (bookedSegment.length != 2) continue;

                int bookedFrom = indexOfStop(stops, bookedSegment[0]);
                int bookedTo = indexOfStop(stops, bookedSegment[1]);

                if (bookedFrom == -1 || bookedTo == -1 || bookedFrom >= bookedTo) continue;

                // Check if segments overlap
                boolean overlaps = !(toIndex <= bookedFrom || fromIndex >= bookedTo);
                if (overlaps) {
                    String[] seatTokens = parts[2].split("-");
                    for (String seat : seatTokens) {
                        try {
                            occupiedSeats.add(Integer.parseInt(seat.trim()));
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid seat number: " + seat);
                        }
                    }
                }
            }
        }

        List<Integer> availableSeats = new ArrayList<>();
        for (int i = 1; i <= totalSeats; i++) {
            if (!occupiedSeats.contains(i)) {
                availableSeats.add(i);
            }
        }
        return availableSeats;
    }

    public static String getNotifications(String busId) {
        lock.readLock().lock();
        String filePath = "data/BusOwnerNotification/"+busId+".txt";
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            lock.readLock().unlock();
        }
        return "";
    }

    public static boolean writeMsg(Message msg) {
        lock.writeLock().lock();
        String filePath = "data/BusOwnerNotification/"+msg.getReceiver()+".txt";
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

    public static boolean deleteAccount(String busId) throws IOException {
        lock.writeLock().lock();
        String filePath = "data/BusOwnerNotification/"+busId+".txt";
        boolean flag1=false;
        try{
            File file=new File(filePath);
            if(!file.exists()){
                flag1=true;
            }
            else{
                file.delete();
                flag1=true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        boolean flag2=false;
        String filePath2 = "data/routes/"+busId+".txt";
        try{
            File file=new File(filePath2);
            if(!file.exists()){
                flag2=true;
            }
            else{
                file.delete();
                flag2=true;
            }
        } catch (Exception e) {
            flag2=false;
            throw new RuntimeException(e);
        }
        List<String> list=new ArrayList<>();
        boolean flag3=true;
        try(BufferedReader br=new BufferedReader(new FileReader(FILE_PATH))){
            String line;
            while((line=br.readLine())!=null){
                String []data=line.split(",");
                if(data[0].equals(busId)){
                    flag3=true;
                    continue;
                }
                list.add(line);
            }
            br.close();
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
        try{
            BufferedWriter writer=new BufferedWriter(new FileWriter(FILE_PATH));
            for(String line:list){
                writer.write(line);
                writer.newLine();
            }
            writer.close();
        }
        catch(IOException e){
            flag3=false;
            throw new RuntimeException(e);
        }
        finally{
            lock.writeLock().unlock();
            return flag1&&flag2&&flag3;
        }

    }
}
