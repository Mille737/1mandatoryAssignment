import java.util.*;
import java.io.*;
import java.net.*;

public class Server {

    //Server port number
    private static int PORT = 3030;
    //fields
    private static String name;
    private boolean stop = false;
    private boolean valid = false;
    private DataInputStream dis;
    private DataOutputStream dos;
    //setters
    private void setStop(boolean stop) {
        this.stop = stop;
    }
    private void setName(String name) {
        Server.name = name;
    }
    //lists
    private ArrayList<ServerConnection> connections = new ArrayList<>();
    public List<ServerConnection> getConnections() {
        return connections;
    }


    public void server() throws IOException {

        //creates new socket on chosen port
        ServerSocket serverSocket = new ServerSocket(PORT);
        Socket socket;

        System.out.println("*** Starting server on PORT: "+ PORT + " ***\n");
        System.out.println("Waiting for client...\n");

        while (true) {

            //client gets accepted
            socket = serverSocket.accept();

            //used to communicate between the server and client
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            //Server writes to client
            dos.writeUTF("Type Join: and then your username: ");
            //clears outputstream
            dos.flush();

            //Sets usernanme = the input from client
            String userName = dis.readUTF();

            //here it splits the string in text before and after the :
            String[] fragments = userName.split(":");
            if (fragments.length > 0) {
                //fragments[0] = text before the :
                String part1 = fragments[0];
                //fragments[1] = text after the :
                setName(fragments[1]);

                //checks if fragments[0] = join
                if (part1.equalsIgnoreCase("join")) {

                    while (true) {
                        //method to check if username is valid
                        checkUserName(name);
                        //if valid is true continue
                        if (valid) {
                            //creates new serverConnection object
                            ServerConnection serverConnection = new ServerConnection(socket, this, name);

                            //Starts the thread
                            serverConnection.start();

                            //the serverConnection is added to the connections arrayList
                            connections.add(serverConnection);
                            System.out.println(name + " has joined the server\n");
                            break;

                        } else {
                            //if not valid try again
                            dos.writeUTF("Try again: ");
                            dos.flush();

                            //new input text from client
                            String newTextIn = dis.readUTF();
                            fragments = newTextIn.split(":");

                            if (fragments.length > 0) {
                                //sets name to text after :
                                setName(fragments[1]);
                            }
                        }
                    }
                } else {
                    //incorrect format: part1 != join
                    dos.writeUTF("Wrong format");
                    dos.flush();
                    break;
                }
            } else {
                //incorrect format: fragments length is < 0
                dos.writeUTF("Wrong format");
                dos.flush();
                break;
            }
        }
    }

    //metode to check if the username is usable and doesn't already exist
    public void checkUserName(String inputUserName) {

        //loops through list of connected clients
        for (int i = 0; i < connections.size(); i++) {

            //if username is already used the client will be rejected
            if (connections.get(i).getUsername().equalsIgnoreCase(inputUserName)) {
                setStop(true);
                break;
            }
        }

        if (stop) {
            try {
                //enforms client that username exists
                dos.writeUTF("Username already exists.");
                dos.flush();
                valid = false;
                stop = false;

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //Username is valid
        else {
            try {
                dos.writeUTF("Username OK");
                dos.writeUTF("You can now start chatting!");
                dos.writeUTF("Write 'HelpMe' for a guide");
                dos.flush();

                valid = true;

            } catch (IOException E) {
                E.printStackTrace();
            }
        }
    }
}
