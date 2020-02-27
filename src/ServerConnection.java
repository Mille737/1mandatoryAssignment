import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerConnection extends Thread {

    private Socket socket;
    private Server server;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean running = true;
    private ServerConnection serverConnection;
    private String username;

    public String getUsername() {
        return username;
    }

    //class constructor
   public ServerConnection(Socket socket, Server server, String username) {
        super("Server Connection Thread");
        this.socket = socket;
        this.server = server;
        this.username = username;
    }

    @Override
    //When an object implementing interface Runnable is used to create a thread,
    //starting the thread causes the object's run method to be called in that separately executing thread.
    public void run() {
        try {
            //Data in- og outputStream uses socket to listen for and send data
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            while (running) {
                // available() Returns an estimate of the number of bytes that can be read (or skipped over)
                // from this input stream without blocking by the next caller of a method for this input stream.
                // The next caller might be the same thread or another thread.
                while (dis.available() == 0) {
                    try {
                        //thread sleeps for 1 millisecond
                        Thread.sleep(1);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }

                //reads input from data stream and returns data as String (UTF format)
                String messageIn = dis.readUTF();

                //creates a String array that contains the messageIn and splits the text at :
                String[] fragments = messageIn.split(":");
                if (fragments.length > 0) {
                    //control = all chars before : in fragments
                    String control = fragments[0];
                    // Switch-Cases creates a form a menu of options relative to client/server communication
                    switch (control) {

                        case "Broadcast":
                            writeToAllInChat(fragments);
                            break;

                        case "Data":
                            writeToOneClient(fragments);
                            break;

                        case "List":
                            clientList();
                            break;

                        case "HelpMe":
                            helpME();
                            break;

                        default:
                            dos.writeUTF("ERROR - Command not understood");
                            dos.flush();
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //method to broadcast message to all clients
    public void writeToAllInChat(String[] fragments) {
        //chars after : i.e. the chat message to be sent
        String message = fragments[1];
        //loops through list getConnections and sets = serverConnection
        //finds clients to send message to
        for (int i = 0; i < server.getConnections().size(); i++) {
            serverConnection = server.getConnections().get(i);

            try {
                //if one removes the serverConnection and only uses dos the message will be sent to its self only
                serverConnection.dos.writeUTF("Message from: " + username + ": " + message);
                //flush clears outputstream
                serverConnection.dos.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Method for sending message to specific client
    public void writeToOneClient(String[] fragments) {
        //equals the username of the client you want message
        String writeTo = fragments[1];
        //equals the message you want to send
        String text = fragments[2];

        for (int i = 0; i < server.getConnections().size(); i++) {
            serverConnection = server.getConnections().get(i);

            //if the written username is in list continue
            if (writeTo.equalsIgnoreCase(server.getConnections().get(i).getUsername())){
                try {
                    //message to server that client who sent message is chatting with client who received message
                    System.out.println(username + " sent message to " + server.getConnections().get(i).getUsername());
                    //serverConnection.dos ensures the message is only sent to specified client
                    serverConnection.dos.writeUTF("From " + username + ": " + text);
                    serverConnection.dos.flush();
                    //sent to oneself to confirm sent message
                    dos.writeUTF("Message sent to " + username);
                    break;

                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void clientList() {

        //prints a list of all connected clients
        for (int i = 0; i < server.getConnections().size(); i++) {
            try {
                //only dos is necessary here since it a message to oneself
                //i = connection order(first connected 0, next 1...)
                dos.writeUTF("Connected " + i + ": " + server.getConnections().get(i).getUsername());
                dos.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void helpME() throws IOException {
        dos.writeUTF("*** HELP ME GUIDE ***\n");
        dos.writeUTF("Data = message one client: (Data: username of client you would like to message: message text ---> data: Nic: Hey you!");
        dos.writeUTF("Broadcast = message all clients: (broadcast: message) ---> broadcast: hello world");
        dos.writeUTF("List = shows client list: (list) ---> Connected index: username");
    }
}
