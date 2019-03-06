import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

public class FTPProtocol {

    public static void main(String[] args) {
        try {

            System.out.println("This FTP client is modelled after the 'Command Prompt', using similar commands.");

            //Get Host Name
            Scanner scan = new Scanner(System.in);
            System.out.println("Please enter the FTP servers Host name or IP (use for example ftp.cs.brown.edu): ");
            String host = scan.nextLine();

            //Create Control Channel
            Socket clientSocket = new Socket(host, 21);
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            //get acknowledgement on connection to new socket
            System.out.println("-----------------");
            getTxt(inFromServer);
            System.out.println("-----------------");

            //login
            System.out.println("Login to the FTP Server (if anonymous, hit enter with no input):");
            System.out.print("Username: ");
            String userName = scan.nextLine();
            if (userName.equals("")) {
                outToServer.writeBytes("USER\r\n");
                getTxt(inFromServer);
            } else {
                System.out.print("Password: ");
                String password = scan.nextLine();
                outToServer.writeBytes("USER " + userName + "\r\n");
                getTxt(inFromServer);
                outToServer.writeBytes("PASS " + password + "\r\n");
                getTxt(inFromServer);
            }
            System.out.println("-----------------");

            System.out.println("\nYou are now logged into the Server. You have the following command options:" +
                    "\npwd                      (to print working directory)" +
                    "\ndir                      (to List contents of working directory)" +
                    "\ncd 'directory name'      (to cd to specified directory)" +
                    "\nretr 'file name'         (to retrieve a named file)" +
                    "\nstore 'file name'        (to upload a named file to the server)" +
                    "\nappend 'file name'       (to append an existing file)" +
                    "\nexit                     (to exit program)");
            System.out.println("-----------------");

            int x = 1;
            while (x == 1) {
                String cmd = scan.nextLine();

                switch (cmd.substring(0, 3)) {
                    case "pwd":
                        outToServer.writeBytes("pwd\r\n");
                        getTxt(inFromServer);
                        System.out.println("-----------------");
                        break;

                    case "dir":
                        Socket dataSocket = generateDataSocket(outToServer, inFromServer);
                        BufferedReader inFromDataSocket = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));
                        outToServer.writeBytes("List\r\n");
                        getTxt(inFromDataSocket);
                        getTxt(inFromServer);
                        System.out.println("-----------------");
                        break;

                    case "ret":
                        String file = cmd.substring(5);
                        Socket dataSocket3 = generateDataSocket(outToServer, inFromServer);
                        BufferedReader inFromDataSocket3 = new BufferedReader(new InputStreamReader((dataSocket3.getInputStream())));
                        outToServer.writeBytes("retr " + file + "\r\n");
                        getTxt(inFromDataSocket3);
                        getTxt(inFromServer);
                        System.out.println("-----------------");
                        break;

                    case "app":
                        String editFile = cmd.substring(7);
                        Socket editDataSocket = generateDataSocket(outToServer, inFromServer);
                        BufferedReader inFromEditDataSocket = new BufferedReader(new InputStreamReader(editDataSocket.getInputStream()));
                        DataOutputStream toEditDataSocket = new DataOutputStream(editDataSocket.getOutputStream());

                        System.out.println("Input the text to append the file with:");
                        String editText = scan.nextLine();
                        outToServer.writeBytes("APPE " + editFile + "\r\n");
                        toEditDataSocket.writeBytes(editText);
                        toEditDataSocket.close();
                        getTxt(inFromServer);
                        System.out.println("-----------------");
                        break;

                    case "sto":
                        String uploadFile = cmd.substring(7);
                        Socket uploadDataSocket = generateDataSocket(outToServer, inFromServer);
                        DataOutputStream toUploadDataSocket = new DataOutputStream(uploadDataSocket.getOutputStream());

                        System.out.println("Input the text you want the file to contain:");
                        String uploadText = scan.nextLine();
                        outToServer.writeBytes("STOR " + uploadFile + "\r\n");
                        toUploadDataSocket.writeBytes(uploadText);
                        toUploadDataSocket.close();
                        getTxt(inFromServer);
                        System.out.println("-----------------");
                        break;

                    case "exi":
                        x = 0;
                        break;

                    case "cd ":
                        String directory = cmd.substring(3);
                        outToServer.writeBytes("cwd " + directory + "\r\n");
                        getTxt(inFromServer);
                        System.out.println("-----------------");
                        break;

                    default:
                        System.out.println("Command unknown, Try again.");
                        System.out.println("-----------------");
                }
            }

            System.out.println("Thank you for using this client, We hope it was an enjoyable experience! ❤💕❤💕");
            System.exit(0);

        } catch (java.io.IOException ioE) {
            System.out.println(ioE);
            System.exit(-2);
        }
    }

    public static void getTxt(BufferedReader inFromServer) throws java.io.IOException {
        do {
            String line = inFromServer.readLine();
            System.out.println(line);

        } while (inFromServer.ready());
    }

    public static Socket generateDataSocket(DataOutputStream outToServer, BufferedReader inFromServer) throws java.io.IOException {
        String newSocketAddress = null;
        outToServer.writeBytes("PASV\r\n");
        newSocketAddress = inFromServer.readLine();
        System.out.println(newSocketAddress);

        //convert socket address to IP and Port
        String[] line = newSocketAddress.split(Pattern.quote("("));
        String address = getAddress(line[1]);
        int port = getPort(line[1]);
        System.out.println("-----------------");
        System.out.println("Connecting to new socket...     IP: " + address + ",     Port Nmr: " + port);
        System.out.println("-----------------");

        //create new Socket & input/output Stream
        return new Socket(address, port);
    }

    public static String getAddress(String str) {
        String[] ip = str.split(Pattern.quote(","));
        String finalIP = ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];

        return finalIP;
    }

    public static int getPort(String str) {
        String[] port = str.split(Pattern.quote(","));


        int finalP = ((Integer.parseInt(port[4]) * 256)
                + Integer.parseInt(port[5].substring(0, port[5].length() - 1)));

        return finalP;
    }
}


