package messages;


import chatActors.ActorChatServer.Type;
import co.paralleluniverse.actors.ActorRef;

/**
 *
 * @author Miguel
 */
public class Messages {
    
    //Tipo de mensagem utilizada quando um utilizador ainda não tem room atribuida
    public static class MessageTypeNoRoom {
        public final String nome;
        public final ActorRef utilizador;
        
        public MessageTypeNoRoom(String nome, ActorRef utilizador) {
            this.nome = nome;
            this.utilizador = utilizador;
        }
    }
    
    //Tipo de mensagem utilizada quando um utilizador tem room atribuida e quer mudar de room por exemplo
    public static class MessageTypeWithRoom {
        public final String nomeUtilizador;
        public final ActorRef refUtilizador;
        public final String roomName;
        public final ActorRef roomRef;
        
        public MessageTypeWithRoom(String nome, ActorRef utilizador, String roomName, ActorRef roomRef) {
            this.nomeUtilizador = nome;
            this.refUtilizador = utilizador;
            this.roomName = roomName;
            this.roomRef = roomRef;
        }
    }
    
    //Tipo de mensagem utilizada quando um utilizador quer fazer login ou criar conta
    public static class MessageTypeAutentication {
        public final String nome;
        public final String pass;
        public final ActorRef utilizador;
        
        public MessageTypeAutentication(String nome, String pass, ActorRef utilizador) {
            this.nome = nome;
            this.pass = pass;
            this.utilizador = utilizador;
        }
    }
    
    //Tipo de mensagem utilizada para passar a referencia de um actor a outro.
    public static class MessageTypeActorRef {
        public final ActorRef roomRef;
        
        public MessageTypeActorRef(ActorRef roomRef) {
            this.roomRef = roomRef;
        }
    }
    
    //Tipo de mensagem utilizada para a comunicação das mensagens de texto entre utilizadores.
    public static class MessageTypeToUsers {
        public final String nome;
        public final Object obj;
        
        public MessageTypeToUsers(String nome, Object obj) {
            this.nome = nome;
            this.obj = obj;
        }
    }
    
    //Tipo de mensagem utilizada para a comunicação das mensagens privadas de um utilizador para outro.
    public static class MessageTypePrivateMessage {
        public final ActorRef referenciaActorOrigem;
        public final String nomeUtilizadorOrigem;
        public final String nomeUtilizadorDestino;
        public final String mensagem;
        
        public MessageTypePrivateMessage(ActorRef rao, String nuo, String nud, String m) {
            this.referenciaActorOrigem = rao;
            this.nomeUtilizadorOrigem = nuo;
            this.nomeUtilizadorDestino = nud;
            this.mensagem = m;
        }
    }
    
    //Tipo de mensagem utilizada para a comunicacao entre zmqAdministrador e actores (roomManager neste caso)
    public static class MessageTypeFromAdmin {
        public final ActorRef adminRef;
        public final String info;
        
        public MessageTypeFromAdmin(ActorRef ar, String info) {
            this.adminRef = ar;
            this.info = info;
        }
    }
    
    //Mensagens (constituidas por um tipo e objecto)
    public static class Message {
        public final Type tipo;
        public final Object obj;
        
        public Message(Type tipo, Object obj) {
            this.tipo = tipo;
            this.obj = obj;
        }
    }
}
