/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trab1_psd_cliente;

/**
 *
 * @author ricardo
 */
public class main {
    
    public static void main(String[] args) {
        
        String host = "localhost";
        int port = 12345;
            
        Cliente cliente = new Cliente(host, port);
        cliente.connect();
        
    }
    
}
