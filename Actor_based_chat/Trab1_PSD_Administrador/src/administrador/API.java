package administrador;

/**
 *
 * @author Miguel
 */
public interface API {
    /* Lista as rooms que existem */
    public void listRooms();
    
    /* Cria uma room com um dado nome */
    public void createRoom();
    
    /* Lista os users de uma determinada room */
    public void listUsersInRoom();
    
    /* Remove uma room de nome dado (A ROOM TEM DE ESTAR VAZIA, SEM UTILIZADORES) */
    public void removeRoom();
}
