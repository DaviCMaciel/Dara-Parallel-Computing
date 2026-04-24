import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceJogo extends Remote {
    // Chat 
    void receberMensagem(String autor, String msg) throws RemoteException;

    // Colocação de peças (Fase 1) 
    void receberJogada(int l, int c) throws RemoteException;

    // Movimentação (Fase 2) 
    void receberMovimento(int lo, int co, int ld, int cd) throws RemoteException;

    // Captura (Quando forma trio) 
    void receberRemocao(int l, int c) throws RemoteException;

    // Desistência 
    void receberDesistencia() throws RemoteException;
}