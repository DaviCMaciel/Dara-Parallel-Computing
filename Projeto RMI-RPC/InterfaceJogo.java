import java.rmi.Remote;
import java.rmi.RemoteException;

public interface InterfaceJogo extends Remote {
    
    // Métodos para comunicação entre os jogadores (RMI)

    // 1. Cliente chama isto no servidor para iniciar a conexão
    void registrarOponente(InterfaceJogo oponente) throws RemoteException;
    
    // 2. Fase de Colocação: Envia a posição da peça 
    void receberJogada(int linha, int coluna) throws RemoteException;
    
    // 3. Fase de Movimentação: Envia origem e destino 
    void receberMovimento(int lOrigem, int cOrigem, int lDestino, int cDestino) throws RemoteException;
    
    // 4. Captura: Avisa que uma peça foi removida
    void receberRemocao(int linha, int coluna) throws RemoteException;
    
    // 5. Chat: Envia mensagem de texto 
    void receberMensagem(String autor, String msg) throws RemoteException;
    
    // 6. Desistência: Avisa que o oponente saiu 
    void receberDesistencia() throws RemoteException;

    // 7. Fim de Jogo: Avisa ao oponente que ele venceu a partida
    void anunciarVitoria() throws RemoteException;
}