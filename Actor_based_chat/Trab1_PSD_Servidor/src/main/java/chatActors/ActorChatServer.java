package chatActors;


import messages.Messages.Message;
import messages.Messages.MessageTypeActorRef;
import messages.Messages.MessageTypeAutentication;
import messages.Messages.MessageTypeFromAdmin;
import messages.Messages.MessageTypeNoRoom;
import messages.Messages.MessageTypePrivateMessage;
import messages.Messages.MessageTypeToUsers;
import messages.Messages.MessageTypeWithRoom;
import co.paralleluniverse.actors.*;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.io.FiberServerSocketChannel;
import co.paralleluniverse.fibers.io.FiberSocketChannel;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.zeromq.ZMQ;

/**
 *
 * @author Miguel
 */
public class ActorChatServer {
    static int MAX_LENGHT = 1024;
    public static enum Type {DATA, LINE, CREATE_ACCOUNT, CREATE_ACCOUNT_OK, CREATE_ACCOUNT_ERROR, LOGIN, LOGIN_OK, LOGIN_ERROR, JOIN_ROOM, JOIN_ROOM_AT_LOGIN, CHANGE_ROOM, CHANGE_ROOM_ERROR, ENTER, ENTER_AT_LOGIN, ENTER_OK, ENTER_ERROR, LEAVE, LEAVE_OK, LIST_ROOMS, LIST_MY_ROOM_USERS, PRIVATE_MESSAGE, LOGOUT, LIST_ROOMS_ADMIN, CREATE_ROOM_ADMIN, REMOVE_ROOM_ADMIN, LIST_USERS_IN_ROOM_ADMIN, CHECK_EMPTY, ROOM_EMPTY, ROOM_NOT_EMPTY, EOF}
    
    
    /* ---------------------------------------------------------------------------------------------------- */
    /* -------------------------------------------- ACTORES ----------------------------------------------- */
    /* ---------------------------------------------------------------------------------------------------- */
    
    //Classe/Actor "LineReader"
    static class LineReader extends BasicActor<Message, Void> {
        final FiberSocketChannel socket;
        final ActorRef<Message> destino;
        private ByteBuffer in = ByteBuffer.allocate(MAX_LENGHT);
        private ByteBuffer out = ByteBuffer.allocate(MAX_LENGHT);
        
        public LineReader(ActorRef<Message> destino, FiberSocketChannel socket) {
            this.socket = socket;
            this.destino = destino;
        }
        
        @Override
        protected Void doRun() throws InterruptedException, SuspendExecution {
            boolean EOF = false;
            byte b = 0;
            
            try {
                for(;;) { //blocking reader
                    if(this.socket.read(in) <= 0) EOF = true;
                    
                    in.flip();
                    
                    while(in.hasRemaining()) {
                        b = in.get(); //Le byte do array de entrada (o que vem do cliente)
                        out.put(b); //Vai colocando no array de saida os bytes que recebe
                        if(b == '\n') break;
                    }
                    
                    //Caso não haja mais nada para ler do array de entrada envia a linha
                    if(EOF || b == '\n') {
                        out.flip();
                        
                        if(out.remaining() > 0) {
                            byte[] ba = new byte[out.remaining()];
                            out.get(ba);
                            out.clear();
                            this.destino.send(new Message(Type.DATA, ba)); //Envia para o actor destino
                        }
                    }
                    
                    if(EOF && !in.hasRemaining()) break;
                    in.compact();
                }
                
                this.destino.send(new Message(Type.EOF, null));
                return null;
            }
            catch(IOException e) {
                this.destino.send(new Message(Type.EOF, null));
                return null;
            }
        }
    }
    
    //Classe/Actor "Room" constituida pelas referencias dos actores/utilizadores que pertencem aquela room
    static class Room extends BasicActor<Message, Void> {
        final HashMap<String, ActorRef> utilizadores; //Nome de utilizador e respetiva referencia para o seu actor !!!
        final String nome;
        final ZMQ.Socket socket;
        
        public Room(String nome, ZMQ.Socket s) {
            this.utilizadores = new HashMap<>();
            this.nome = nome;
            this.socket = s;
        }
        
        @Override
        @SuppressWarnings("empty-statement")
        protected Void doRun() throws InterruptedException, SuspendExecution {
            while(receive(msg -> {
                switch(msg.tipo) {
                    case ENTER_AT_LOGIN:
                        MessageTypeNoRoom mtnr = (MessageTypeNoRoom)msg.obj;
                        this.utilizadores.put(mtnr.nome, mtnr.utilizador); //Adiciono o nome do utilizador e a sua referencia ao hashmap de users desta room !!!
                        this.socket.send("ENTROU NA ROOM: " + this.nome + " -> " + mtnr.nome);
                        mtnr.utilizador.send(new Message(Type.ENTER_OK, new MessageTypeActorRef(self())));
                        return true;
                        
                    case ENTER:
                        MessageTypeNoRoom mNoRoom = (MessageTypeNoRoom)msg.obj;
                        this.utilizadores.put(mNoRoom.nome, mNoRoom.utilizador);
                        this.socket.send("ENTROU NA ROOM: " + this.nome + " -> " + mNoRoom.nome);
                        mNoRoom.utilizador.send(new Message(Type.ENTER_OK, new MessageTypeActorRef(self())));
                        return true;
                        
                    case LINE:
                        ActorRef actRef;
                        MessageTypeToUsers mToUsers = (MessageTypeToUsers)msg.obj;
                        for(String s : this.utilizadores.keySet()) {
                            if(!s.equals(mToUsers.nome)) {
                                actRef = this.utilizadores.get(s);
                                actRef.send(msg); //Envio a mensagem de volta para todos os utilizadores desta room !!!
                            }
                        }
                        return true;
                        
                    case LIST_MY_ROOM_USERS:
                        MessageTypeActorRef mActorRef = (MessageTypeActorRef)msg.obj;
                        String aux = "";
                        for(String s : this.utilizadores.keySet()) {
                            aux = aux + s + "\n";
                        }
                        mActorRef.roomRef.send(new Message(Type.LIST_MY_ROOM_USERS, (String)aux));
                        return true;
                        
                    case PRIVATE_MESSAGE:
                        MessageTypePrivateMessage mPrivateM = (MessageTypePrivateMessage)msg.obj;
                        
                        if(this.utilizadores.containsKey(mPrivateM.nomeUtilizadorDestino)) {
                            ActorRef userDestino = this.utilizadores.get(mPrivateM.nomeUtilizadorDestino);
                            userDestino.send(new Message(Type.PRIVATE_MESSAGE, new MessageTypeToUsers(mPrivateM.nomeUtilizadorOrigem, (String)mPrivateM.mensagem) /*(String)mPrivateM.mensagem*/));
                        }
                        return true;
                        
                    case CHECK_EMPTY:
                        ActorRef roomManager = (ActorRef)msg.obj;
                        if(this.utilizadores.isEmpty()) roomManager.send(new Message(Type.ROOM_EMPTY, null));
                        else roomManager.send(new Message(Type.ROOM_NOT_EMPTY, null));
                        return true;
                        
                    case LIST_USERS_IN_ROOM_ADMIN:
                        ActorRef acr = (ActorRef)msg.obj;
                        
                        if(!this.utilizadores.isEmpty()) {
                            String res = "Utilizadores na room:\n";
                            for(String s : this.utilizadores.keySet()) {
                                res = res + "-> " + s + "\n";
                            }
                            acr.send(new Message(Type.LIST_USERS_IN_ROOM_ADMIN, (String)res));
                        }
                        else acr.send(new Message(Type.LIST_USERS_IN_ROOM_ADMIN, (String)"A room nao tem utilizadores neste momento"));
                        return true;
                        
                    case LEAVE:
                        if(this.utilizadores.containsKey((String)msg.obj)) {
                            ActorRef oldUser = this.utilizadores.get((String)msg.obj);
                            this.utilizadores.remove((String)msg.obj);
                            this.socket.send("SAIU DA ROOM: " + this.nome + " -> " + (String)msg.obj);
                            oldUser.send(new Message(Type.LEAVE_OK, null));
                        }
                        return true;
                        
                    case LOGOUT:
                        if(this.utilizadores.containsKey((String)msg.obj)) this.utilizadores.remove((String)msg.obj);
                        this.socket.send("SAIU DA ROOM: " + this.nome + " -> " + (String)msg.obj);
                        return true;
                }
                return false;
            }));
            
            return null;
        }
    }
    
    //Classe/Actor "RoomManager". Associa um utilizador a uma room.
    static class RoomManager extends BasicActor<Message, Void> {
        final HashMap<String, ActorRef> rooms; //Nome da room e respetiva referência
        final ZMQ.Socket socket;
        
        public RoomManager(ZMQ.Socket socket) {
            this.rooms = new HashMap<>();
            this.socket = socket;
        }
        
        @Override
        @SuppressWarnings("empty-statement")
        protected Void doRun() throws InterruptedException, SuspendExecution {
            //Quando é criado o RoomManager é criado automaticamente uma e apenas uma room. Qualquer outra criação de novas rooms tem de ser a partir do administrador !!!! 
            ActorRef defaultRoomREF = new Room("defaultROOM", this.socket).spawn();
            this.rooms.put("defaultROOM", defaultRoomREF);
            //Acabou de ser criada a defaultROOM logo tem de ser captado pelo zmq publisher para enviar à consola de subscricoes
            this.socket.send("ROOM CRIADA: defaultROOM");
            
            
            while(receive(msg -> {
                switch(msg.tipo) {
                    case JOIN_ROOM_AT_LOGIN:
                        defaultRoomREF.send(new Message(Type.ENTER_AT_LOGIN, msg.obj));
                        return true;
                        
                    case LIST_ROOMS:
                        String aux = "";
                        MessageTypeActorRef mActorRef = (MessageTypeActorRef)msg.obj;
                        for(String s : this.rooms.keySet()) {
                            aux = aux + s + "\n";
                        }
                        mActorRef.roomRef.send(new Message(Type.LIST_ROOMS, (String)aux));
                        return true;
                        
                    case PRIVATE_MESSAGE:
                        for(String s : this.rooms.keySet()) {
                            ActorRef ar = this.rooms.get(s);
                            
                            ar.send(new Message(Type.PRIVATE_MESSAGE, msg.obj));
                        }
                        return true;
                        
                    case CHANGE_ROOM:
                        MessageTypeWithRoom mWithRoom = (MessageTypeWithRoom)msg.obj;
                        if(this.rooms.containsKey(mWithRoom.roomName)) {
                            for(String s : this.rooms.keySet()) {
                                if(this.rooms.get(s).equals(mWithRoom.roomRef)) {
                                    this.rooms.get(s).send(new Message(Type.LEAVE, (String)mWithRoom.nomeUtilizador));
                                    break;
                                }
                            }
                        }
                        else mWithRoom.refUtilizador.send(new Message(Type.CHANGE_ROOM_ERROR, null));
                        return true;
                        
                    case JOIN_ROOM:
                        MessageTypeWithRoom mWithRoom1 = (MessageTypeWithRoom)msg.obj;
                        this.rooms.get(mWithRoom1.roomName).send(new Message(Type.ENTER, new MessageTypeNoRoom(mWithRoom1.nomeUtilizador, mWithRoom1.refUtilizador)));
                        return true;
                        
                    case LIST_ROOMS_ADMIN:
                        String aux1 = "";
                        MessageTypeFromAdmin mFromAdmin = (MessageTypeFromAdmin)msg.obj;
                        for(String s : this.rooms.keySet()) {
                            aux1 = aux1 + s + "\n";
                        }
                        mFromAdmin.adminRef.send(new Message(Type.LIST_ROOMS_ADMIN, (String)aux1));
                        return true;
                        
                    case CREATE_ROOM_ADMIN:
                        MessageTypeFromAdmin mFromAdmin1 = (MessageTypeFromAdmin)msg.obj;
                        String roomName = mFromAdmin1.info;
                        
                        if(!this.rooms.containsKey(roomName)) {
                            ActorRef room = new Room(roomName, this.socket).spawn();
                            this.rooms.put(roomName, room);
                            this.socket.send("ROOM CRIADA: " + roomName);
                            mFromAdmin1.adminRef.send(new Message(Type.CREATE_ROOM_ADMIN, (String)"Room criada com sucesso"));
                        }
                        else mFromAdmin1.adminRef.send(new Message(Type.CREATE_ROOM_ADMIN, (String)"Room ja existe ou nao foi possivel criar room"));
                        return true;
                        
                    case REMOVE_ROOM_ADMIN:
                        MessageTypeFromAdmin mFromAdmin2 = (MessageTypeFromAdmin)msg.obj;
                        
                        if(this.rooms.containsKey(mFromAdmin2.info)) {
                            this.rooms.get(mFromAdmin2.info).send(new Message(Type.CHECK_EMPTY, (ActorRef)self()));
                            //Espera resposta da room para saber se tá vazia ou nao
                            while(receive(msg2 -> {
                                switch(msg2.tipo) {
                                    case ROOM_EMPTY:
                                        this.rooms.remove(mFromAdmin2.info);
                                        this.socket.send("ROOM REMOVIDA: " + mFromAdmin2.info);
                                        mFromAdmin2.adminRef.send(new Message(Type.REMOVE_ROOM_ADMIN, (String)"Room removida com sucesso"));
                                    break;
                                        
                                    case ROOM_NOT_EMPTY:
                                        mFromAdmin2.adminRef.send(new Message(Type.REMOVE_ROOM_ADMIN, (String)"Room nao esta vazia! Para poder remover nao pode ter utilizadores na room escolhida!"));
                                    break;
                                }
                                return false;
                            }));
                            return true;
                        }
                        else mFromAdmin2.adminRef.send(new Message(Type.REMOVE_ROOM_ADMIN, (String)"Room nao existe!"));
                        return true;
                        
                    case LIST_USERS_IN_ROOM_ADMIN:
                        MessageTypeFromAdmin mFromAdmin3 = (MessageTypeFromAdmin)msg.obj;
                        
                        if(this.rooms.containsKey(mFromAdmin3.info)) {
                            this.rooms.get(mFromAdmin3.info).send(new Message(Type.LIST_USERS_IN_ROOM_ADMIN, (ActorRef)self()));
                            //Espera por a resposta da room em questão para que envie os users que tão nela
                            while(receive(msg3 -> {
                                String s = (String)msg3.obj;
                                mFromAdmin3.adminRef.send(new Message(Type.LIST_USERS_IN_ROOM_ADMIN, (String)s));
                                return false;
                            }));
                            return true;
                        }
                        else mFromAdmin3.adminRef.send(new Message(Type.LIST_USERS_IN_ROOM_ADMIN, (String)"Room nao existe!"));
                        return true;
                }
                
                return false;
            }));
                
            return null;
        }
    }
    
    //Classe/Actor "Login"
    static class Login extends BasicActor<Message, Void> {
        final HashMap<String, String> logins; //Relacao user(nome utilizador) e pass !!!
        
        public Login() {
            this.logins = new HashMap<>();
        }
        
        @Override
        @SuppressWarnings("empty-statement")
        protected Void doRun() throws InterruptedException, SuspendExecution {
            while(receive(msg -> {
                MessageTypeAutentication mta = (MessageTypeAutentication)msg.obj;
                
                switch(msg.tipo) {
                    case CREATE_ACCOUNT:
                        if(this.logins.containsKey(mta.nome) == false) {
                            this.logins.put(mta.nome, mta.pass);
                            mta.utilizador.send(new Message(Type.CREATE_ACCOUNT_OK, msg.obj));
                        }
                        else {
                            mta.utilizador.send(new Message(Type.CREATE_ACCOUNT_ERROR, msg.obj));
                        }
                        return true;
                    
                    case LOGIN:
                        if(this.logins.containsKey(mta.nome) && this.logins.get(mta.nome).equals(mta.pass)) {
                            mta.utilizador.send(new Message(Type.LOGIN_OK, msg.obj));
                        }
                        else {
                            mta.utilizador.send(new Message(Type.LOGIN_ERROR, msg.obj));
                        }
                        return true;
                }
                
                return false;
            }));
            return null;
        }
    }
    
    //Classe/Actor "User" 
    static class User extends BasicActor<Message, Void> {
        final FiberSocketChannel socket;
        final ActorRef loginActor;
        final ActorRef roomManagerActor;
        private ActorRef roomActor; //Referencia para o actor (room) a que vai pertencer este utilizador
        private boolean estado; //False se nao estiver logado, true se tiver logado !!!
        private boolean temRoom; //False se nao tiver room, true se tiver room !!!
        private String nomeUtilizador; //Nome do utilizador quando já tiver login efetuado !!!
        private String newRoom; //Utilizado apenas quando o utilizador quer trocar de room !!!
        
        public User(FiberSocketChannel fsc, ActorRef login, ActorRef roomManager) {
            this.socket = fsc;
            this.loginActor = login;
            this.roomManagerActor = roomManager;
            this.estado = false;
            this.temRoom = false;
        }
        
        @Override
        @SuppressWarnings("empty-statement")
        protected Void doRun() throws InterruptedException, SuspendExecution {
            new LineReader(self(), this.socket).spawn();
            
            while(receive(msg -> {
                try {
                    switch(msg.tipo) {
                        case DATA: //msg.o é um bytearray. Basta delimitar os vários casos de acordo com um separador pré-definido !!!
                            String s = new String((byte[])msg.obj);
                            String[] aux = s.split(" ");
                            
                            
                            switch(aux[0]) {
                                case "CREATE_ACCOUNT": this.loginActor.send(new Message(Type.CREATE_ACCOUNT, new MessageTypeAutentication(aux[1], aux[2], self())));
                                break;
                                
                                case "LOGIN":
                                              if(this.estado == false) this.loginActor.send(new Message(Type.LOGIN, new MessageTypeAutentication(aux[1], aux[2], self())));
                                              if(this.estado == true) {
                                                  String s4 = "Login ja efetuado com user: " + this.nomeUtilizador + " \n";
                                                  this.socket.write(ByteBuffer.wrap(s4.getBytes()));
                                              };
                                break;
                                
                                //Listar o nome de todas as rooms que existem. Tem de ter login efetuado para isso !!!
                                case "LIST_ROOMS":
                                    if(this.estado) this.roomManagerActor.send(new Message(Type.LIST_ROOMS, new MessageTypeActorRef(self())));
                                    else {
                                        String s6 = "Tem de possuir conta, ter login efetuado e pertenceer a uma room para poder utilizar o servico\n";
                                        this.socket.write(ByteBuffer.wrap(s6.getBytes()));
                                    }
                                break;
                                    
                                case "LIST_MY_ROOM_USERS":
                                    if(this.estado && this.temRoom) this.roomActor.send(new Message(Type.LIST_MY_ROOM_USERS, new MessageTypeActorRef(self())));
                                    else {
                                        String s6 = "Tem de possuir conta, ter login efetuado e pertenceer a uma room para poder utilizar o servico\n";
                                        this.socket.write(ByteBuffer.wrap(s6.getBytes()));
                                    }
                                break;
                                    
                                case "PRIVATE_MESSAGE":
                                    if(this.estado && this.temRoom) {
                                        String aux2 = "";
                                        for(int i = 2; i < aux.length; i++) aux2 = aux2 + aux[i] + " ";
                                        this.roomManagerActor.send(new Message(Type.PRIVATE_MESSAGE, new MessageTypePrivateMessage(self(), this.nomeUtilizador, aux[1], aux2)));
                                    }
                                    else {
                                        String s6 = "Tem de possuir conta, ter login efetuado e pertenceer a uma room para poder utilizar o servico\n";
                                        this.socket.write(ByteBuffer.wrap(s6.getBytes()));
                                    }
                                break;
                                    
                                case "CHANGE_ROOM":
                                    if(this.estado && this.temRoom) {
                                        this.temRoom = false;
                                        this.newRoom = aux[1];
                                        this.roomManagerActor.send(new Message(Type.CHANGE_ROOM, new MessageTypeWithRoom(this.nomeUtilizador, self(), aux[1], this.roomActor)));
                                    }
                                    else {
                                        String s6 = "Tem de possuir conta, ter login efetuado e pertenceer a uma room para poder utilizar o servico\n";
                                        this.socket.write(ByteBuffer.wrap(s6.getBytes()));
                                    }
                                break;
                                    
                                case "LOGOUT":
                                    if(this.estado && this.temRoom) {
                                        this.roomActor.send(new Message(Type.LOGOUT, (String)this.nomeUtilizador));
                                        this.estado = false;
                                        this.temRoom = false;
                                        this.nomeUtilizador = "";
                                    }
                                    else {
                                        String s6 = "Tem de possuir conta, ter login efetuado e pertenceer a uma room para poder utilizar o servico\n";
                                        this.socket.write(ByteBuffer.wrap(s6.getBytes()));
                                    }
                                break;
                                    
                                case "EXIT": //Para fazer exit decidimos que primeiro tem de fazer logout mas sem nenhum motivo em especial, so mesmo para facilitar no codigo porque nao é algo essencial, é mais do mesmo basicamente !!!
                                    if(!this.estado && !this.temRoom) this.socket.close();
                                    else {
                                        String s6 = "Antes de fazer EXIT tem de fazer LOGOUT\n";
                                        this.socket.write(ByteBuffer.wrap(s6.getBytes()));
                                    }
                                break;
                                    
                                default: //Quando nao é enviado nenhum comando então é porque é uma mensagem a ser enviada a todos os utilizadores da room a que pertence !!!!
                                    if(this.estado && this.temRoom) this.roomActor.send(new Message(Type.LINE, new MessageTypeToUsers(this.nomeUtilizador, msg.obj)));
                                    else if(this.estado && !this.temRoom) {
                                        String s6 = "Antes de poder enviar mensagens tem de pertencer a uma room!";
                                        this.socket.write(ByteBuffer.wrap(s6.getBytes()));
                                    }
                                    else {
                                        String s6 = "Tem de possuir conta e ter login efetuado para poder utilizar o servico\n";
                                        this.socket.write(ByteBuffer.wrap(s6.getBytes()));
                                    }
                            }
                            return true;
                        
                        case CREATE_ACCOUNT_OK:
                            String s1 = "Conta criada com sucesso\n";
                            this.socket.write(ByteBuffer.wrap(s1.getBytes()));
                            return true;
                        
                        case CREATE_ACCOUNT_ERROR:
                            String s2 = "Erro a criar conta: nome de utilizador ja existe\n";
                            this.socket.write(ByteBuffer.wrap(s2.getBytes()));
                            return true;
                            
                        case LOGIN_OK: //Quando faço login para alem de ficar logado é-me atribuida logo uma room. Se por algum motivo a room nao for atribuida, CONTINUO LOGADO e posso depois escolher uma room manualmente !!!!!!!!
                            MessageTypeAutentication mta = (MessageTypeAutentication)msg.obj;
                            this.estado = true;
                            this.nomeUtilizador = mta.nome;
                            this.roomManagerActor.send(new Message(Type.JOIN_ROOM_AT_LOGIN, new MessageTypeNoRoom(this.nomeUtilizador, self()))); //Mando mensagem para o roomManager a dizer que acabei de fazer login e quero que me seja atribuida uma room (mando-lhe o tipo de pedido e a minha referencia para que me responda de volta)
                            //Espero ja por uma resposta da room a confirmar ou nao que fiquei associado a ela !!!!!!!
                            while(receive(msgLOGIN -> {
                                try {
                                    switch(msgLOGIN.tipo) {
                                        case ENTER_OK:
                                            MessageTypeActorRef rtu = (MessageTypeActorRef)msgLOGIN.obj;
                                            this.temRoom = true;
                                            this.roomActor = rtu.roomRef; //Referencia do actor (room) onde fiquei !!!!
                                            String s3 = "Login efetuado com user: " + this.nomeUtilizador + " e room atribuida\n";
                                            this.socket.write(ByteBuffer.wrap(s3.getBytes()));
                                        break;
                                    }
                                }
                                catch (IOException ex) {
                                    Logger.getLogger(ActorChatServer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                return false;
                            }));
                            return true;
                        
                        case LOGIN_ERROR:
                            String s5 = "ERRO: Tem de criar conta primeiro ou user e pass erradas\n";
                            this.socket.write(ByteBuffer.wrap(s5.getBytes()));
                            return true;
                            
                        case LINE:
                            MessageTypeToUsers mToUsers = (MessageTypeToUsers)msg.obj;
                            String aux1 = new String((byte[])mToUsers.obj, "UTF-8");
                            String s7 = "--------------------------------------------------\n" + mToUsers.nome + ": " + aux1;
                            this.socket.write(ByteBuffer.wrap(s7.getBytes()));
                            return true;
                            
                        case LIST_ROOMS:
                            String s8 = (String)msg.obj;
                            this.socket.write(ByteBuffer.wrap(s8.getBytes()));
                            return true;
                            
                        case LIST_MY_ROOM_USERS:
                            String s9 = (String)msg.obj;
                            this.socket.write(ByteBuffer.wrap(s9.getBytes()));
                            return true;
                            
                        case PRIVATE_MESSAGE:
                            MessageTypeToUsers mToUsers1 = (MessageTypeToUsers)msg.obj;
                            
                            String s10 = "--------------------------------------------------\n" + mToUsers1.nome + ": " + (String)mToUsers1.obj /*(String)msg.obj*/;
                            this.socket.write(ByteBuffer.wrap(s10.getBytes()));
                            return true;
                            
                        case CHANGE_ROOM_ERROR:
                            this.temRoom = true;
                            String s11 = "Room escolhida nao existe!\n";
                            this.socket.write(ByteBuffer.wrap(s11.getBytes()));
                            return true;
                            
                        case LEAVE_OK:
                            this.roomManagerActor.send(new Message(Type.JOIN_ROOM, new MessageTypeWithRoom(this.nomeUtilizador, self(), this.newRoom, null)));
                            return true;
                            
                        case ENTER_OK:
                            MessageTypeActorRef rtu = (MessageTypeActorRef)msg.obj;
                            this.temRoom = true;
                            this.roomActor = rtu.roomRef; //Referencia do actor (room) onde fiquei !!!!
                            String s12 = "Nova room atribuida\n";
                            this.socket.write(ByteBuffer.wrap(s12.getBytes()));
                            return true;
                    }
                }
                catch(Exception e) {
                    System.out.println("Erro ao receber no user!");
                }
                return false;
            }));
            
            return null;
        }
    }
    
    //Classe/Actor "Acceptor" que trata as novas ligações criando novos utilizadores (actores)
    static class Acceptor extends BasicActor {
        final int porta;
        final ActorRef login;
        final ActorRef roomManager;
        
        public Acceptor(int porta, ActorRef login, ActorRef roomManager) {
            this.porta = porta;
            this.login = login;
            this.roomManager = roomManager;
        }
        
        @Override
        protected Void doRun() throws InterruptedException, SuspendExecution {
            try {
                FiberServerSocketChannel ss = FiberServerSocketChannel.open();
                ss.bind(new InetSocketAddress(this.porta));
                
                while(true) {
                    System.out.println("A espera de ligacoes...");
                    
                    FiberSocketChannel socket = ss.accept();
                    
                    System.out.println("Nova ligacao aceite...");
                    
                    new User(socket, login, roomManager).spawn();
                }
            }
            catch(IOException e) {
                System.out.println("Impossivel fazer ligacao !!!");
            }
            
            return null;
        }
    }
    
    //Main function
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        int porta = 12345; //Porta por onde fica à escuta
        
        ActorRef roomManager;
        ActorRef login;
        Acceptor acceptor;
        ActorRef zmqAdmin;
        
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.PUB);
        socket.bind("tcp://*:12347");
        
        
        roomManager = new RoomManager(socket).spawn();
        zmqAdmin = new ZmqAdministratorActor(roomManager).spawn();
        login = new Login().spawn();
        acceptor = new Acceptor(porta, login, roomManager);
        
        
        acceptor.spawn();
        acceptor.join();
    }
}
