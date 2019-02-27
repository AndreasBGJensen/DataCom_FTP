import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {

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
