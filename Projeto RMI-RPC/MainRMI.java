import javax.swing.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainRMI {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            String[] opcoes = {"Criar Sala (Servidor)", "Entrar na Sala (Cliente)"};
            int escolha = JOptionPane.showOptionDialog(null, "Escolha o modo de jogo:", "Dara RMI", 
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opcoes, opcoes[0]);

            NovoTabuleiro tabuleiro = new NovoTabuleiro();

            if (escolha == 0) { // MODO SERVIDOR (Jogador 1)
                try {
                    LocateRegistry.createRegistry(1099);
                } catch (Exception e) {}

                NovaJanelaJogo janelaServidor = new NovaJanelaJogo(tabuleiro, NovoTabuleiro.JOGADOR1);
                Naming.rebind("DaraP1", janelaServidor); // Nome padronizado

                JOptionPane.showMessageDialog(janelaServidor, "Sala criada! Aguardando oponente...");

                while (true) {
                    try {
                        // O Servidor procura o Cliente (DaraP2)
                        InterfaceJogo oponente = (InterfaceJogo) Naming.lookup("rmi://localhost/DaraP2");
                        janelaServidor.setOponente(oponente);
                        janelaServidor.postarAviso("Oponente conectado! Você começa.");
                        break; // IMPORTANTE: Sai do loop ao conectar
                    } catch (Exception e) {
                        Thread.sleep(1000); 
                    }
                }
            } else if (escolha == 1) { // MODO CLIENTE (Jogador 2)
                String ip = JOptionPane.showInputDialog("Digite o IP do servidor:", "localhost");
                
                NovaJanelaJogo janelaCliente = new NovaJanelaJogo(tabuleiro, NovoTabuleiro.JOGADOR2);
                Naming.rebind("DaraP2", janelaCliente); // Registra-se como DaraP2

                // O Cliente procura o Servidor (DaraP1)
                InterfaceJogo servidor = (InterfaceJogo) Naming.lookup("rmi://" + ip + "/DaraP1");
                janelaCliente.setOponente(servidor);
                
                // CORREÇÃO: Chama 'receberMensagem' (método da Interface) e não 'adicionarMensagemChat'
                servidor.receberMensagem("Sistema", "O Jogador 2 entrou na sala!");
                janelaCliente.postarAviso("Conectado ao Servidor!");
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Erro: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}