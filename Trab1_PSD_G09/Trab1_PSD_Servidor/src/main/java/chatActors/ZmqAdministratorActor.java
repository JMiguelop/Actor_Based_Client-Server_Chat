package chatActors;

import messages.Messages.Message;
import messages.Messages.MessageTypeFromAdmin;
import chatActors.ActorChatServer.Type;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.fibers.SuspendExecution;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeromq.ZMQ;

/**
 *
 * @author Miguel
 * 
 */

/* FOI NECESSARIO CRIAR ESTA CLASSE COM THREAD SO PARA LIDAR COM A ESPERA ATIVA SOBRE O QUE VEM DO SOCKET PORQUE DIRECTAMENTE NO ACTOR, DAVA UM CONJUNTO DE WARNINGS SEMPRE SEGUIDOS SEM PARAR A DIZER QUE HAVIA UMA THREAD A BLOQUEAR O CPU, COMO SERIA DE ESPERAR */
class thrededZMQ extends Thread {
    private final ActorRef myRef;
    private final ActorRef roomManagerRef;
    private final ZMQ.Socket socket;
    
    
    /* CONSTRUTOR */
    public thrededZMQ(ActorRef myRef, ActorRef roomManagerRef, ZMQ.Socket socket) {
        this.myRef = myRef;
        this.roomManagerRef = roomManagerRef;
        this.socket = socket;
    }
    
    
    @Override
    public void run() {
        byte[] b = this.socket.recv();
        String s = new String(b);
        String[] aux = s.split(" ");
        
        
        /* PARSER À STRING RECEBIDA  DO ADMINISTRADOR CLIENTE */
        switch(aux[0]) {
            case "LIST_ROOMS": 
                try {
                    this.roomManagerRef.send(new Message(Type.LIST_ROOMS_ADMIN, new MessageTypeFromAdmin(this.myRef, "")));
                } catch (SuspendExecution ex) {
                    Logger.getLogger(thrededZMQ.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
                
            case "CREATE_ROOM":
                String roomName = aux[1];
       
                try {
                    this.roomManagerRef.send(new Message(Type.CREATE_ROOM_ADMIN, new MessageTypeFromAdmin(this.myRef, roomName)));
                } catch (SuspendExecution ex) {
                    Logger.getLogger(thrededZMQ.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
                
            case "LIST_USERS_IN_ROOM":
                String roomName1 = aux[1];
                
                try {
                    this.roomManagerRef.send(new Message(Type.LIST_USERS_IN_ROOM_ADMIN, new MessageTypeFromAdmin(this.myRef, roomName1)));
                } catch (SuspendExecution ex) {
                    Logger.getLogger(thrededZMQ.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
                
            case "REMOVE_ROOM":
                String roomName2 = aux[1];
                
                try {
                    this.roomManagerRef.send(new Message(Type.REMOVE_ROOM_ADMIN, new MessageTypeFromAdmin(this.myRef, roomName2)));
                } catch (SuspendExecution ex) {
                    Logger.getLogger(thrededZMQ.class.getName()).log(Level.SEVERE, null, ex);
                }
            break;
        }
    }
}


public class ZmqAdministratorActor extends BasicActor<Message, Void> {
    private final ActorRef roomManagerActor; //Referencia para o RoomManager para que este actor possa fazer pedidos !!!
    private ZMQ.Socket sock;
    private ActorRef self;

    
    /* CONSTRUTOR */
    public ZmqAdministratorActor(ActorRef roomManager) {
        this.roomManagerActor = roomManager;
    }
  
    
    @Override
    @SuppressWarnings("empty-statement")
    protected Void doRun() throws InterruptedException, SuspendExecution {
        this.self = self();
        
        ZMQ.Context context = ZMQ.context(1); //1 thread
        this.sock = context.socket(ZMQ.REP); //Tipo REPLY
        this.sock.bind("tcp://*:12346"); //Porta 12346

        while(true) {
            new thrededZMQ(this.self, this.roomManagerActor, this.sock).start();
            
            //Aqui nao interessa o tipo das mensagens uma vez que vêm do servidor sempre em formato String e já formatadas a serem o resultado final a enviar para o cliente
            while(receive(msg -> {
                String s = (String)msg.obj;
                this.sock.send(s);
                                
                return false;
            }));
        }
    }
}
