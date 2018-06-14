package Workers;

import java.net.Socket;
import java.util.Scanner;
import trab1_psd_cliente.Cliente;

/**
 *
 * @author ricardo
 */
public class InputWorker implements Runnable{

    private Socket socket;
    private Cliente client;
    
    public InputWorker(Socket socket, Cliente cli) {
        this.socket = socket;
        this.client = cli;
    }

    @Override
    public void run() {
        
        Scanner in = null;
        try {
            in = new Scanner(socket.getInputStream());
            String line = in.nextLine();
            while (true) {
                if(line.equals("sair"))
                    break;
                System.out.println(line);
                line = in.nextLine();
            }
        } catch (Exception e) {
//					e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
}
