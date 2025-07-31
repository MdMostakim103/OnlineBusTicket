package server;

import model.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {

            System.out.println("Client connected: " + socket.getRemoteSocketAddress());

            while (true) {
                try {
                    Request req = (Request) ois.readObject();
                    if (req == null) {
                        System.out.println("Received null request, closing connection");
                        break;
                    }

                    Response res = handleRequest(req);
                    oos.writeObject(res);
                    oos.flush();

                    if ("LogOut".equals(req.getAction())) {
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Invalid request format: " + e.getMessage());
                    break;
                } catch (EOFException e) {
                    System.out.println("Client disconnected normally");
                    break;
                }
            }
        } catch (SocketException e) {
            System.out.println("Client disconnected abruptly: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Client handling error: " + e.getMessage());
        } finally {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
            System.out.println("Connection closed for: " + socket.getRemoteSocketAddress());
        }
    }

    private Response handleRequest(Request req) {
        try {
            switch (req.getRole()) {
                case "Passenger":
                    return handlePassengerRequest(req);
                case "BusOwner":
                    return handleBusOwnerRequest(req);
                case "Admin":
                    return handleAdminRequest(req);
                default:
                    return new Response(false, "Invalid role specified");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Response(false, "Error processing request: me checking"  + e.getMessage());
        }
    }

    private Response handlePassengerRequest(Request req) throws IOException {
        String[] data = req.getData().split(",");

        switch (req.getAction()) {
            case "LogIn":
                if (data.length != 3) return new Response(false, "Invalid login data");
                if (PassengerFileHandler.loginCheck(data[0], data[1], data[2])) {
                    Server.activePassengers.put(data[0], socket);
                    return new Response(true, "Login successful");
                }
                return new Response(false, "Invalid credentials");

            case "LogOut":
                if (data.length < 1) return new Response(false, "Invalid logout data");
                Server.activePassengers.remove(data[0]);
                return new Response(true, "Logged out successfully");

            case "SignUp":
                if (data.length != 3) return new Response(false, "Invalid registration data");
                if (PassengerFileHandler.emailExists(data[0])) {
                    return new Response(false, "Email already registered");
                }
                Passenger p = new Passenger(data[0], data[1], data[2]);
                PassengerFileHandler.signUp(p);
                return new Response(true, "Registration successful");

            case "CheckEmail":
                if (PassengerFileHandler.emailExists(req.getData())) {
                    return new Response(true, "Email exists");
                }
                return new Response(false, "Email available");

            case "ForgetPassword":
                if (data.length != 3) return new Response(false, "Invalid password reset data");
                if (!PassengerFileHandler.emailExists(data[0])) {
                    return new Response(false, "Email not registered");
                }
                PassengerFileHandler.updatePassenger(data[0], data[1], data[2]);
                return new Response(true, "Password updated successfully");
            case "SearchBus":
                String from=data[0];
                String to=data[1];
                String date=data[2];
                List<Bus> buses=new ArrayList<>();
                buses=BusFileHandler.getBuses(from,to,date);
                return new Response(true, buses);
            case "ChooseSeat":
                //if(data.length<=5) return new Response(false, "Invalid number of seats");
                List<Integer> seats=new ArrayList<>();
                String route=BusFileHandler.generateRoute(data[0]);
                seats=BusFileHandler.getAvailableSeats(data[0],data[1],data[2],data[3],route);
                String s=seats.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                return new Response(true, s);
            case "ConfirmBooking":
                String busId=data[0];
                String Date=data[5];
                String str = String.join(",", Arrays.copyOfRange(data, 1, 6));
                if(BusFileHandler.confirmBooking(str,busId,Date)){
                    return new Response(true, "Booking successful");
                }
                return new Response(false,"booking denied");
            case "CheckNotification":
                String email=req.getData();
                String str1=PassengerFileHandler.getNotifications(email.split("@")[0]);
                return new Response(true,str1);
            case "GetAvailableSeats":
                List<Integer> seat=new ArrayList<>();
                String route1=BusFileHandler.generateRoute(data[0]);
                seats=BusFileHandler.getAvailableSeats(data[0],data[1],data[2],data[3],route1);
                String s2=seats.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                return new Response(true, s2);
            case "ContactAdmin":
                String admin=data[3];
                if(!AdminFileHandler.checkName(admin)){
                    return new Response(false, "Invalid admin name");
                }

                if(AdminFileHandler.writeMsg(new Message(data[0],data[1],data[2],data[3],data[4]))){
                    return new Response(true, "Contact successful");
                }
                return new Response(false,"contact unsuccessful");
            default:
                return new Response(false, "Invalid action for Passenger");
        }
    }

    private Response handleBusOwnerRequest(Request req) throws IOException {
        Response res=new Response(false,null);
        if (req.getAction().equals("LogIn")){
            String []data = req.getData().split(",");
            if(BusFileHandler.logInBus(data[0],data[1],data[2])){
                Server.activeBusOwners.put(data[0], socket);
                res = new Response(true, data[0]);
                return res;
            }
            return new Response(false, "Invalid Username or Password");
        }
        else if(req.getAction().equals("SignUp")){
            String [] data=req.getData().split(",");
            if(BusFileHandler.checkBusId(data[0])){
                return new Response(false,"Bus Id already exists");
            }
            BusFileHandler.saveBusOwner(new BusOwner(data[0],data[1],data[2],data[3]));
            return new Response(true,data[0]);
        }
        else if(req.getAction().equals("LogOut")){
            String []data=req.getData().split(",");
            Server.activeBusOwners.remove(data[0]);
            return new Response(true,"Bye");
        }
        else if(req.getAction().equals("loadTodayPassengers")){
            String []data=req.getData().split(",");
            List<PassengerInfo> passengerInfoList=BusFileHandler.loadTodayPassengers(data[0]);
            if(passengerInfoList==null){
                res=new Response(false,passengerInfoList);
            }
            res=new Response(true,passengerInfoList);
            return res;
        }
        else if(req.getAction().equals("LoadUpcomingPassengers")){
            String data=req.getData();
            List<PassengerInfo> passengerInfoList=BusFileHandler.loadUpcomingPassenger(data);
            return new Response(true,passengerInfoList);
        }
        else if(req.getAction().equals("UpdateInfo")){
            String []data=req.getData().split(",");
            String email=BusFileHandler.getEmail(data[0]);
            boolean success= BusFileHandler.updateBusOwner(new BusOwner(data[0],data[1],data[2],email));
            if(success){
                res=new Response(true,data[0]);
                return res;
            }
            return new Response(false,"Failed to update");
        }
        else if(req.getAction().equals("SaveBusRoute")){
            String rawData=req.getData();
            System.out.println(rawData);
            String []data=req.getData().split(",");
            boolean success=BusFileHandler.saveRoute(rawData,data[0]);
            if(success){
                return new Response(success,"bus");
            }
            return new Response(false,"Failed to save");
        }

        else if(req.getAction().equals("CheckNotification")){
            String busId=req.getData();
            String str=BusFileHandler.getNotifications(busId);
            return new Response(true,str);
        }

        else if(req.getAction().equals("SendNotification")){
            String [] str=req.getData().split(",");
            Message msg=new Message(str[0],str[1],str[2],str[3],str[4]);
            if(msg.getSenderRole().equals("BusOwner")){
                boolean success=AdminFileHandler.writeMsg(msg);
                if(success){
                    return new Response(true,"Message sent");
                }
                return new Response(false,"Failed to send message");
            }
        }
        else if(req.getAction().equals("ContactAdmin")){
            String []data=req.getData().split(",");
            String admin=data[3];
            if(!AdminFileHandler.checkName(admin)){
                return new Response(false, "Invalid admin name");
            }

            if(AdminFileHandler.writeMsg(new Message(data[0],data[1],data[2],data[3],data[4]))){
                return new Response(true, "Contact successful");
            }
            return new Response(false,"contact unsuccessful");
        }

        else if(req.getAction().equals("Delete Account")){
            String busId= req.getData();
            if(BusFileHandler.deleteAccount(busId)){
                return new Response(true,"Account deleted successfully");
            }
            return new Response(false,"Failed to delete account");
        }
        return new Response(false, null);
    }

    private Response handleAdminRequest(Request req) {
        if(req.getAction().equals("LogIn")){
            String []str=req.getData().split(",");
            if(AdminFileHandler.checkLogIn(str[0],str[1])){
                return new Response(true,str[0]);
            }
            else{
                return new Response(false,"Invalid Username or Password");
            }

        }
        else if(req.getAction().equals("ForgotPassword")){
            String [] str=req.getData().split(",");
            if(AdminFileHandler.changePass(str[0],str[1])){
                return new Response(true,str[0]);
            }
            else{
                return new Response(false,"Failed to change password");
            }
        }
        else if(req.getAction().equals("CheckNotification")){
            String name= req.getData();
            String str=AdminFileHandler.loadNotification(name);
            return new Response(true,str);
        }
        else if(req.getAction().equals("ContactPassenger")){
            String [] str=req.getData().split(",");
            if(!PassengerFileHandler.emailExists(str[3])){
                return new Response(false,"Email does not exist");
            }
            String email = str[3].split("@")[0];
            Message msg=new Message(str[0],str[1],str[2],email,str[4]);
            if(PassengerFileHandler.writeMsg(msg)){
                return new Response(true,"Message sent");
            }
            return new Response(false,"Wrinting to file failed");
        }
        else if(req.getAction().equals("ContactBusOwner")){
            String [] str=req.getData().split(",");
            Message msg=new Message(str[0],str[1],str[2],str[3],str[4]);
            if(!BusFileHandler.checkBusId(str[3])){
                return new Response(false,"BusId does not exist");
            }
            if(BusFileHandler.writeMsg(msg)){
                return new Response(true,"Message sent");
            }
            return new Response(false,"Wrinting to file failed");
        }
        return new Response(false, "Admin functionality not implemented");
    }
}