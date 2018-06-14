
package notificationConsole;

import java.io.IOException;
import org.zeromq.ZMQ;

/**
 *
 * @author Miguel
 */

/* "CONSOLA" SEMPRE À ESPERA DE IMPRIMIR INFORMACAO À QUAL SUBSCREVEU */
class Console extends Thread {
    private final ZMQ.Socket socket;
    
    public Console(ZMQ.Socket s) {
        this.socket = s;
    }
    
    @Override
    public void run() {
        while (true) {
            byte[] b = this.socket.recv();
            System.out.println("\n------------ NOTIFICACAO ------------");
            System.out.println(new String(b));
            System.out.println("-------------------------------------\n");
        }
    }
}


public class NotificationConsole implements API {
    private ZMQ.Socket socket;
    private int opcao = -1;
    private InterfaceUtilizador iu = new InterfaceUtilizador();
    
    
    /* --- API --- */
    @Override
    public void subscreverCriacaoRoom() {
        this.socket.subscribe("ROOM CRIADA:".getBytes());
    }
    
    @Override
    public void subscreverRemocaoRoom() {
        this.socket.subscribe("ROOM REMOVIDA:".getBytes());
    }
    
    @Override
    public void subscreverEntrarUtilizador() {
        this.socket.subscribe("ENTROU NA ROOM:".getBytes());
    }
    
    @Override
    public void subscreverSairUtilizador() {
        this.socket.subscribe("SAIU DA ROOM:".getBytes());
    }
    /* ----------- */
    
    
    public void start() {
        ZMQ.Context context = ZMQ.context(1);
        this.socket = context.socket(ZMQ.SUB);
        this.socket.connect("tcp://localhost:12347"); //Porta 12347
        
        new Console(this.socket).start(); //Thread
        
        while (this.opcao != 0) {
            this.opcao = this.iu.menuPrincipal();
            
            switch(this.opcao) {
                case 1: { //Subscreve a eventos de criação de rooms
                    subscreverCriacaoRoom();
                    break;
                }
                case 2: { //Subscreve a eventos de remoção de rooms
                    subscreverRemocaoRoom();
                    break;
                }
                case 3: { //Subscreve a eventos de entrada de utilizadores nas rooms
                    subscreverEntrarUtilizador();
                    break;
                }
                case 4: { //Subscreve a eventos de saida de utilizadores nas rooms
                    subscreverSairUtilizador();
                    break;
                }
                case 0: { //Sai de forma segura do ambiente, fechando o socket
                    this.socket.close();
                    System.out.println("Ligacao Terminada");
                    break;
                }
                default: {
                    System.out.println("Problema de opcoes!");
                    break;
                }
            }
        }
        
        context.term();
    }
    
    
    public static void main(String[] args) throws IOException {
        NotificationConsole noteConsole = new NotificationConsole();
        noteConsole.start();
    }
}
