import javax.swing.*;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;

public class MainRMI {
    public static void main(String[] args) {
        try {
            String ipLocal = java.net.InetAddress.getLocalHost().getHostAddress();
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                java.util.Enumeration<java.net.InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    java.net.InetAddress addr = addrs.nextElement();
                    if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                        String candidate = addr.getHostAddress();
                        if (!candidate.startsWith("192.168.56")) { ipLocal = candidate; }
                        }
                    }
                }

            System.setProperty("java.rmi.server.hostname", ipLocal);
            System.out.println("IP local detectado: " + ipLocal);

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            String[] opcoes = {"Criar Sala (Servidor)", "Entrar na Sala (Cliente)"};
            int escolha = JOptionPane.showOptionDialog(null, "Escolha o modo de jogo:", "Dara RMI", 
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, opcoes, opcoes[0]);

            NovoTabuleiro tabuleiro = new NovoTabuleiro();

            try { LocateRegistry.createRegistry(1099); } catch (Exception e) {}
            
            if (escolha == 0) { // MODO SERVIDOR (Jogador 1)

                NovaJanelaJogo janelaServidor = new NovaJanelaJogo(tabuleiro, NovoTabuleiro.JOGADOR1);
                Naming.rebind("DaraP1", janelaServidor); // Nome padronizado

                JOptionPane.showMessageDialog(janelaServidor, "Sala criada! Aguardando oponente...");


            } else if (escolha == 1) { // MODO CLIENTE (Jogador 2)
                String ip = JOptionPane.showInputDialog("Digite o IP do servidor:", "localhost");
                
                NovaJanelaJogo janelaCliente = new NovaJanelaJogo(tabuleiro, NovoTabuleiro.JOGADOR2);

                // O Cliente procura o Servidor (DaraP1)
                InterfaceJogo servidor = (InterfaceJogo) Naming.lookup("rmi://" + ip + "/DaraP1");
                servidor.registrarOponente(janelaCliente);
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