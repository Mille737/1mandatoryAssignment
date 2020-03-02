
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    //Serverport same as the server
    final static int serverPort = 3030;

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        //Finds the local ip address
        InetAddress ip = InetAddress.getByName("localhost");

        //connects to server
        Socket socket = new Socket(ip, serverPort);

        //input and output streams use the socket to listen for data
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        //The syntax of an anonymous class expression is like the invocation of a constructor,
        // except that there is a class definition contained in a block of code.
        //The Runnable interface should be implemented by any class whose instances are intended to be executed by a thread.
        // The class must define a method of no arguments called run.
        Thread sendMessage = new Thread(new Runnable() {

            @Override
            public void run() {

                while (true){

                    //message = next input
                    String message = scanner.nextLine();

                    try {
                        //sends message to server
                        dos.writeUTF(message);

                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });

        //creates new thread for message to be read
        Thread readMessage = new Thread(new Runnable() {

            @Override
            public void run() {

                while (true){

                    try {
                        String inputMessage = dis.readUTF();
                        System.out.println(inputMessage);

                    } catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        //Starts threads
        sendMessage.start();
        readMessage.start();
    }
}
