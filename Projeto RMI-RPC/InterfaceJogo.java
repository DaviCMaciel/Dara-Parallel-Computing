import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceJogo extends Remote {
    
    // 1. Fase de Colocação: Envia a posição da peça 
    void receberJogada(int linha, int coluna) throws RemoteException;
    
    // 2. Fase de Movimentação: Envia origem e destino 
    void receberMovimento(int lOrigem, int cOrigem, int lDestino, int cDestino) throws RemoteException;
    
    // 3. Captura: Avisa que uma peça foi removida
    void receberRemocao(int linha, int coluna) throws RemoteException;
    
    // 4. Chat: Envia mensagem de texto 
    void receberMensagem(String autor, String msg) throws RemoteException;
    
    // 5. Desistência: Avisa que o oponente saiu 
    void receberDesistencia() throws RemoteException;

    // 6. Fim de Jogo: Avisa ao oponente que ele venceu a partida
    void anunciarVitoria() throws RemoteException;
}