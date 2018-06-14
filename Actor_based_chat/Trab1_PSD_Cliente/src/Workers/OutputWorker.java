package Workers;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import trab1_psd_cliente.Cliente;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ricardo
 */
public class OutputWorker implements Runnable {

    private Socket socket;
    private Cliente client;

    public OutputWorker(Socket socket, Cliente client) {
        this.client = client;
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Started...");
        PrintWriter out = null;
        Scanner sysIn = new Scanner(System.in);
        try {
            out = new PrintWriter(socket.getOutputStream());
            
            //while (sysIn.hasNext() && !client.isFinished) {
            while (sysIn.hasNext() ) {
                String line = sysIn.nextLine();
                if ("EXIT ".equals(line)) {
                    break;
                }
                out.println(line);
                out.flush();
            }
        } catch (IOException e) {
            System.out.println("ERRO!");
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
}
