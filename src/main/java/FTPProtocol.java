import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Pattern;

public class FTPProtocol {

    public static void main(String[] args) {
        try {

            System.out.println("This FTP client is modelled after the 'Command Prompt'. Use the commands the same commands.");

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
                    "\n'directory name'         (to cd to named directory)" +
                    "\nretr 'file name'         (to retrieve a named file)" +
                    "\nget 'file name'                   (to store file locally )" +
                    "\nstore 'file name'        (to upload a named file)" +
                    "\nexit                     (to exit program)");

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
                        BufferedReader inFromDataSocket3 = new BufferedReader(new InputStreamReader(dataSocket3.getInputStream()));
                        outToServer.writeBytes("retr " + file + "\r\n");
                        getTxt(inFromDataSocket3);
                        break;

                    case "get":
                        String dwlFile = cmd.substring(4);
                        Socket downloadDataSocket = generateDataSocket(outToServer,inFromServer);
                        InputStreamReader inFromDownloadSocket = new InputStreamReader(downloadDataSocket.getInputStream());
                        outToServer.writeBytes("retr " + dwlFile + "\r\n");
                        dwlFile(inFromDownloadSocket,dwlFile);

                    case "sto":
                        String uploadFile = cmd.substring(7);
                        Socket uploadDataSocket = generateDataSocket(outToServer, inFromServer);
                        BufferedReader inFromUploadDataSocket = new BufferedReader(new InputStreamReader(uploadDataSocket.getInputStream()));
                        DataOutputStream toUploadDataSocket = new DataOutputStream(uploadDataSocket.getOutputStream());

                        outToServer.writeBytes("APPE" + uploadFile + "\r\n");
                        getTxt(inFromServer);
                        toUploadDataSocket.writeBytes("info");
                        toUploadDataSocket.close();
                        getTxt(inFromUploadDataSocket);
                        getTxt(inFromServer);
                        break;

                    case "exi":
                        x = 0;
                        break;

                    default:
                        outToServer.writeBytes("cwd " + cmd + "\r\n");
                        getTxt(inFromServer);
                        System.out.println("-----------------");
                        break;
                }
            }

            System.out.println("Thank you for using this client, We hope it was an enjoyable experience! ❤💕❤💕");
            System.exit(0);

        } catch (java.io.IOException ioE) {
            System.out.println(ioE);
            System.exit(1);
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public static void /*int*/ getTxt(BufferedReader inFromServer) throws java.io.IOException {
        do {
            String line = inFromServer.readLine();
            System.out.println(line);
            //if(line == null || line.length() == 0){return 0;}
        } while (inFromServer.ready());
        //return 1;
    }

    public static Socket generateDataSocket(DataOutputStream outToServer, BufferedReader inFromServer) throws java.io.IOException {
        int x;
        String newSocketAddress = null;
        outToServer.writeBytes("PASV\r\n");
        //while (true) {
        newSocketAddress = inFromServer.readLine();
        System.out.println(newSocketAddress);
        //if(newSocketAddress != null){break;}
        //}

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

        //System.out.println(finalIP);
        return finalIP;
    }

    public static int getPort(String str) {
        String[] port = str.split(Pattern.quote(","));


        int finalP = ((Integer.parseInt(port[4]) * 256)
                + Integer.parseInt(port[5].substring(0, port[5].length() - 1)));

        //System.out.println(finalP);
        return finalP;
    }

    public static void dwlFile(InputStreamReader inFromDownloadSocket, String dwlFile)throws java.io.IOException {

        File file = new File(dwlFile);
        FileOutputStream fileOutputStream = new FileOutputStream(file,true);

        try {
            do {
                int line = inFromDownloadSocket.read();
                fileOutputStream.write(line);
            }
            while (inFromDownloadSocket.ready());
        }finally {
            if (inFromDownloadSocket != null){
                inFromDownloadSocket.close();
            }
        }
        fileOutputStream.close();



        /*File file = new File(dwlFile);

        try {
            OutputStream outputStream = new FileOutputStream(file);

            while (inFromServer.ready()){
                String line = inFromServer.readLine();
                outputStream.write(Integer.parseInt(line));
                break;
            }

            outputStream.close();

        }

        catch (java.io.IOException e){
            System.out.println(e);
        }*/
    }
}


