import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        try{

            Socket clientSocket = new Socket("ftp.cs.brown.edu", 21);

            BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

            do{
                System.out.println(inFromServer.readLine());
            }while(inFromServer.ready());

            System.out.println("finished");

            outToServer.writeBytes("USER\r\n");
            do{
                System.out.println(inFromServer.readLine());
            }while(inFromServer.ready());


            outToServer.writeBytes("FEAT\r\n");

            do{
                System.out.println(inFromServer.readLine());
            }while(inFromServer.ready());

            outToServer.writeBytes("PASV\r\n");

            do{
                System.out.println(inFromServer.readLine());
            }while(inFromServer.ready());

            clientSocket.close();


        }catch(java.io.IOException e){
            e.printStackTrace();
        }
    }





    /** Converts an ip address and a portnumber into a correct port command,
     *  in the format:
     *      "PORT (h1,h2,h3,h4,p1,p2)
     */
    static String createPortCommand( String address, int portNumber ){

        int p1 =  portNumber/256;
        int p2 =  portNumber%256;

        String[] ip = address.split( Pattern.quote("."));

        String command = "PORT (";

        for( String sub : ip ){
            command += sub+",";
        }

        command += p1 + "," + p2+")";

        return command;
    }

}
