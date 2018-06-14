
package notificationConsole;

/**
 *
 * @author Miguel
 */
public interface API {
    /* Subscreve à criação de rooms */
    public void subscreverCriacaoRoom();
    
    /* Subscrever à remocao de rooms */
    public void subscreverRemocaoRoom();
    
    /* Subscreve à entrada de utilizadores nas rooms */
    public void subscreverEntrarUtilizador();
    
    /* Subscreve à saida de utilizadores nas rooms */
    public void subscreverSairUtilizador();
}
