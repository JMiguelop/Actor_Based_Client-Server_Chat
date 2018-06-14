package administrador;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeromq.ZMQ;

/**
 *
 * @author Miguel
 */
public class Administrador implements API {
    private ZMQ.Socket socket;
    private int opcao = -1;
    private InterfaceUtilizador iu = new InterfaceUtilizador();
    
    
    /* --- API --- */
    @Override
    public void listRooms() {
        String s = "LIST_ROOMS";
        this.socket.send(s);
        byte[] b = this.socket.recv();
        System.out.println("Rooms disponiveis:");
        System.out.println(new String(b));
    }
    
    @Override
    public void createRoom() {
        String roomName;
        
        try {
            this.iu.createRoom();
            roomName = iu.getInputs();
            String s = "CREATE_ROOM " + roomName;
            this.socket.send(s);
            byte[] b = this.socket.recv();
            System.out.println(new String(b));
        } catch (IOException ex) {
            Logger.getLogger(Administrador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void listUsersInRoom() {
        String roomName;
        
        try {
            this.iu.listUsersInRoom();
            roomName = iu.getInputs();
            String s = "LIST_USERS_IN_ROOM " + roomName;
            this.socket.send(s);
            byte[] b = this.socket.recv();
            System.out.println(new String(b));
        } catch (IOException ex) {
            Logger.getLogger(Administrador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void removeRoom() {
        String roomName;
        
        try {
            this.iu.removeRoom();
            roomName = iu.getInputs();
            if(!roomName.equals((String)"defaultROOM")) {
                String s = "REMOVE_ROOM " + roomName;
                this.socket.send(s);
                byte[] b = this.socket.recv();
                System.out.println(new String(b));
            }
            else System.out.println("Nao pode remover a defaultROOM!");
        } catch (IOException ex) {
            Logger.getLogger(Administrador.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /* ----------- */
    
    
    public void start() {
        ZMQ.Context context = ZMQ.context(1);
        this.socket = context.socket(ZMQ.REQ); //Tipo REQUEST
        this.socket.connect("tcp://localhost:12346"); //Porta 12346
        
        while (this.opcao != 0) {
            this.opcao = this.iu.menuPrincipal();
            
            switch(this.opcao) {
                case 1: { //Listar rooms disponiveis
                    listRooms();
                    break;
                }
                case 2: { //Criar uma room nova
                    createRoom();
                    break;
                }
                case 3: { //Listar os utilizadores de uma certa room
                    listUsersInRoom();
                    break;
                }
                case 4: { //Remover uma room (A ROOM TEM DE ESTAR VAZIA (SEM UTILIZADORES) !!!)
                    removeRoom();
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
        Administrador admin = new Administrador();
        admin.start();
    }
}
