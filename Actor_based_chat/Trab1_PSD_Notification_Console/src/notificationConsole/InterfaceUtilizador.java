
package notificationConsole;

import java.util.Scanner;

/**
 *
 * @author Miguel
 */
public class InterfaceUtilizador {
    private Scanner scanner = new Scanner(System.in);
    
    
    /* CONSTRUTOR */
    public InterfaceUtilizador() {}
    
    
    public void header() {
        System.out.println("--------------------------------------");
        System.out.println("       CONSOLE NOTIFICATION API       ");
        System.out.println("--------------------------------------");
    }
    
    public void opcao() {
        System.out.println("Escolha opcao");
    }
    
    public int menuPrincipal() {
        Integer opcao;
        
        header();
        opcao();
        
        System.out.println("1 - Subscrever a Criacao de Rooms");
        System.out.println("2 - Subscrever a Remocao de Rooms");
        System.out.println("3 - Subscrever a Utilizador Entrar em Room");
        System.out.println("4 - Subscrever a Utilizador Sair de Room");
        System.out.println("0 - Sair");
        System.out.print(">");
        
        opcao = new Integer(this.scanner.nextInt());
        
        return opcao.intValue();
    }
}
