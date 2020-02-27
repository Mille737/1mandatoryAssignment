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


    public void serverM() throws IOException{

        //Laver en ny socket som venter på Clienter på PORT
        ServerSocket serverSocket = new ServerSocket(PORT);

        Socket socket;

        System.out.println("**** Starter server på PORT: "+ PORT + " ****\n");
        System.out.println("Venter for klienter til at connect.....\n");

        //Et while loop bliver lavet når en forbindelse fra en client bliver fundet
        while (true) {

            //accepterer clienten
            socket = serverSocket.accept();

            //DataInd- og OutputStream bliver brugt til at sende data fra klient til serveren og omvendt.
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            //Serveren skriver til klienten og beder om et brugernavn
            dos.writeUTF("Type Join: and then your username: ");
            //flush ryder OutputStream så den er tom
            dos.flush();

            //Serveren modtager indput fra klienten og sætten det til klientens brugernavn
            String brugerNavn = dis.readUTF();

            //Her bliver vores meddelse delt op med ':' da der kan være flere valg og funktioner i en
            //meddelse fra clienten.
            String[] fragments = brugerNavn.split(":");
            if (fragments.length > 0) {
                String part1 = fragments[0];
                setName(fragments[1]);

                //Før en klient kan joine serveren skal de skrive 'join:' og deres brugernavn
                //hvis dette ikke er opfyldt kaster den en fejl
                if (part1.equalsIgnoreCase("join")) {

                    while (true) {
                        userName(name);

                        if (valid){

                        ServerConnection serverConnection = new ServerConnection(socket, this, name);

                        //Starter tråden
                        serverConnection.start();

                        //vores connection bliver her tilføjet til vores 'ArrayListe'
                        connections.add(serverConnection);
                            System.out.println(name + " har joinet serveren\n");
                            break;

                    } else{
                        // Ved indtastet forkert brugernavn.
                        dos.writeUTF("Prøv igen: ");
                        dos.flush();

                        //nyt input fra klienten.
                        String newTextIn = dis.readUTF();
                            fragments = newTextIn.split(":");

                            if (fragments.length > 0) {
                                setName(fragments[1]);
                            }
                        }
                    }
                }else {
                    // Fejl: forkert format.
                    dos.writeUTF("Forkert format");
                    dos.flush();
                    break;
                }
            }else{
                // Fejl: forkert format.
                dos.writeUTF("Forkert format");
                dos.flush();
                break;
            }
        }
    }

    //metode til at checke om brugernavn er brugbart og ikke findes i forvejen
    public void userName(String inputUserName){

        //løber listen igennem for klienter
        for (int i = 0; i < connections.size(); i++){

            //hvis et brugernavn allerede findes bliver klienten afvist.
            if (connections.get(i).getUsername().equalsIgnoreCase(inputUserName)){
                setStop(true);
                break;
            }
        }

        if (stop){
            try {
                //Når klienten bliver afvist
                dos.writeUTF("Brugernavn eksistere allerede");
                dos.flush();
                valid = false;
                stop = false;

            }catch (IOException e){
                e.printStackTrace();
            }

        }
        //Brugernavnet findes ikke og bliver godkendt
        else {
            try {
                dos.writeUTF("Brugernavn OK");
                dos.writeUTF("Du kan nu chatte!");
                dos.writeUTF("Skriv 'HELPME' for en guide");
                dos.flush();

                valid = true;

            }catch (IOException E){
                E.printStackTrace();
            }
        }
    }
}
