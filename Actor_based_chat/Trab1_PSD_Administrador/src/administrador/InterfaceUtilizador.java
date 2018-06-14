package administrador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 *
 * @author Miguel
 */
public class InterfaceUtilizador {
    private Scanner scanner = new Scanner(System.in);
    private String input;
    private BufferedReader in;
    
    
    /* CONSTRUTOR */
    public InterfaceUtilizador() {}
    
    
    /* GET */
    public String getInputs() {
        return this.input;
    }
    
    
    public void header() {
        System.out.println("-------------------------------");
        System.out.println("       ADMINISTRATOR API       ");
        System.out.println("-------------------------------");
    }
    
    public void opcao() {
        System.out.println("Escolha opcao");
    }
    
    public void preencher() {
        System.out.println("Preencha os campos:");
    }
    
    public int menuPrincipal() {
        Integer opcao;
        
        header();
        opcao();
        
        System.out.println("1 - Listar Rooms");
        System.out.println("2 - Criar Room");
        System.out.println("3 - Listar Utilizadores de uma Room");
        System.out.println("4 - Remover Room");
        System.out.println("0 - Sair");
        System.out.print(">");
        
        opcao = new Integer(this.scanner.nextInt());
        
        return opcao.intValue();
    }
    
    
    public void createRoom() throws IOException {
        preencher();
        
        System.out.println("Nome da room a criar: ");
        this.in = new BufferedReader(new InputStreamReader(System.in));
        String room = in.readLine();
        
        if(!room.isEmpty()) this.input = room;
    }
    
    public void removeRoom() throws IOException {
        preencher();
        
        System.out.println("Nome da room a remover: ");
        this.in = new BufferedReader(new InputStreamReader(System.in));
        String room = in.readLine();
        
        if(!room.isEmpty()) this.input = room;
    }
    
    public void listUsersInRoom() throws IOException {
        preencher();
        
        System.out.println("Nome da room onde listar: ");
        this.in = new BufferedReader(new InputStreamReader(System.in));
        String room = in.readLine();
        
        if(!room.isEmpty()) this.input = room;
    }
}
