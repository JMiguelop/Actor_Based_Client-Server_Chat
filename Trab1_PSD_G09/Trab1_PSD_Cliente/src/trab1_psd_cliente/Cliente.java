/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trab1_psd_cliente;

import java.io.IOException;
import java.net.Socket;
import Workers.*;

/**
 *
 * @author ricardo
 */
public class Cliente {

    private String host;
    private int port;
    private Socket socket;
    
    public volatile Boolean isFinished;
        
    Cliente(String host, int port) {
        this.host = host;
        this.port = port;
    }

    void connect() {
        
        try {
            socket = new Socket(host, port);
        } catch (Exception e) {
            System.out.format("ERROR Connecting to server %s:%d!\n", host, port);
            return;
        }
        System.out.format("Connected to server %s:%d!\n", host, port);

        Thread inThread = new Thread(new InputWorker(socket, this));
        Thread outThread = new Thread(new OutputWorker(socket,this));
        inThread.start();
        outThread.start();
    }

    public void disconnect() {
        try {
            socket.close();
        } catch (IOException e) {
            System.out.format("Fail to disconnet");
        }
    }

    public boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }
}
