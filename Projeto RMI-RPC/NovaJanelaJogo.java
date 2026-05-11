import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class NovaJanelaJogo extends JFrame implements InterfaceJogo{
    private NovoTabuleiro backend;
    private InterfaceJogo oponente;
    private JButton[][] botoes = new JButton[5][6];
    private JTextPane areaChat;
    private String historicoHTML = "";
    private JTextField campoTexto;

    // --- CORES E ESTILO WINDOWS XP ---
    private final Color CINZA_XP = new Color(212, 208, 200); 
    private final Color AZUL_XP = new Color(0, 0, 128);
    private final Color VERMELHO_XP = new Color(128, 0, 0);
    private final Color VERDE_XP = new Color(0, 128, 0);

    private int jogadorAtual = NovoTabuleiro.JOGADOR1;
    private int lOrigem = -1;
    private int cOrigem = -1;
    private boolean modoCaptura = false;
    private boolean jogoFinalizado = false;
    private int meuId;
    private int oponenteId;

    public NovaJanelaJogo(NovoTabuleiro tabuleiro, int id) throws RemoteException {
        UnicastRemoteObject.exportObject(this, 0);
        this.backend = tabuleiro;
        this.meuId = id;
        this.oponenteId = (id == NovoTabuleiro.JOGADOR1) ? NovoTabuleiro.JOGADOR2 : NovoTabuleiro.JOGADOR1;

        // 1. Configurações da Janela
        setTitle("Jogo Dara");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(CINZA_XP);

        // 2. Tabuleiro com Borda de Grupo (TitledBorder)
        JPanel painelTabuleiro = new JPanel(new GridLayout(5, 6, 2, 2));
        painelTabuleiro.setBackground(CINZA_XP);
        painelTabuleiro.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), " Campo de Jogo ", 
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 12)));

        for (int l = 0; l < 5; l++) {
            for (int c = 0; c < 6; c++) {
                botoes[l][c] = new JButton(" ");
                
                // Estilo Retro: Fonte Tahoma 50 para X e O gigantes
                botoes[l][c].setFont(new Font("Tahoma", Font.BOLD, 50));
                
                // Efeito 3D clássico (Raised Bevel)
                botoes[l][c].setBorder(BorderFactory.createRaisedBevelBorder());
                botoes[l][c].setBackground(CINZA_XP);
                botoes[l][c].setFocusPainted(false);
                
                final int linha = l;
                final int coluna = c;
                botoes[l][c].addActionListener(e -> aoClicar(linha, coluna));
                
                painelTabuleiro.add(botoes[l][c]);
            }
        }
        add(painelTabuleiro, BorderLayout.CENTER);

        // 3. Painel Lateral
        JPanel painelDireito = new JPanel(new BorderLayout());
        painelDireito.setPreferredSize(new Dimension(320, 600));
        painelDireito.setBackground(CINZA_XP);

        // Topo: Botão Desistir
        JPanel painelTopo = new JPanel(new FlowLayout());
        painelTopo.setBackground(CINZA_XP);
        JButton btnDesistir = new JButton("Desistir / Sair");
        btnDesistir.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnDesistir.setBackground(CINZA_XP);
        btnDesistir.setBorder(BorderFactory.createRaisedBevelBorder());
        btnDesistir.setPreferredSize(new Dimension(150, 40));
        btnDesistir.addActionListener(e -> Desistir());
        painelTopo.add(btnDesistir);
        painelDireito.add(painelTopo, BorderLayout.NORTH);

        // Centro: Chat com Borda Gravada
        areaChat = new JTextPane();
        areaChat.setEditable(false);
        areaChat.setContentType("text/html");
        JScrollPane scrollChat = new JScrollPane(areaChat);
        scrollChat.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), " Mensagens ", 
            TitledBorder.LEFT, TitledBorder.TOP, new Font("Tahoma", Font.PLAIN, 11)));
        painelDireito.add(scrollChat, BorderLayout.CENTER);

        // Rodapé: Entrada de Texto
        campoTexto = new JTextField();
        campoTexto.setFont(new Font("Tahoma", Font.PLAIN, 14));
        campoTexto.addActionListener(e -> enviarMensagem());
        
        // Container para o campo com legenda
        JPanel painelInput = new JPanel(new BorderLayout());
        painelInput.setBackground(CINZA_XP);
        painelInput.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        painelInput.add(new JLabel(" Digite aqui: "), BorderLayout.NORTH);
        painelInput.add(campoTexto, BorderLayout.CENTER);
        painelDireito.add(painelInput, BorderLayout.SOUTH);

        add(painelDireito, BorderLayout.EAST);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void setOponente(InterfaceJogo oponente) {
        this.oponente = oponente;  
    }

    // Centraliza todas as mensagens do sistema
    public void postarAviso(String msg) {
        String cor;
        String msgLower = msg.toLowerCase();

        if (msgLower.contains("trio") || msgLower.contains("captur")) {
            cor = "orange"; // Laranja para qualquer evento de captura
        } else if (backend.isFaseColocacao()) {
            cor = "gray";   // Fase inicial de colocação
        } else {
            cor = "\"#008000\"s";   // Fase de movimentação
        }
        adicionarMensagemChat("Sistema", msg, cor);
    }

    public void atualizarTabuleiro() {
        for (int l = 0; l < 5; l++) {
            for (int c = 0; c < 6; c++) {
                int peca = backend.getPeca(l, c);
                botoes[l][c].setBorder(BorderFactory.createRaisedBevelBorder());
                
                if (l == lOrigem && c == cOrigem) {
                    botoes[l][c].setBackground(Color.YELLOW); // Destaca a peça selecionada
                    botoes[l][c].setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
                } else {
                    botoes[l][c].setBackground(CINZA_XP);
                    botoes[l][c].setBorder(BorderFactory.createRaisedBevelBorder());
                }

                if (peca == NovoTabuleiro.JOGADOR1) {
                    botoes[l][c].setText("X");
                    botoes[l][c].setForeground(AZUL_XP);
                } else if (peca == NovoTabuleiro.JOGADOR2) {
                    botoes[l][c].setText("O");
                    botoes[l][c].setForeground(VERMELHO_XP);
                } else {
                    botoes[l][c].setText(" ");
                }
            }
        }
    }

    //Método que processa os cliques no tabuleiro
    private void aoClicar(int l, int c) {
        if (jogoFinalizado || jogadorAtual != meuId) return;
        
        if (oponente == null) {
        JOptionPane.showMessageDialog(this, 
            "Aguarde! O oponente ainda não entrou na sala.", 
            "Dara RMI", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // MODO CAPTURA: Você formou um trio e precisa escolher uma peça do oponente para remover
            if (modoCaptura) {
                if (backend.removerPeca(meuId, l, c)) {
                    oponente.receberRemocao(l, c);
                    modoCaptura = false;
                    jogadorAtual = oponenteId; // Passa a vez para o oponente
                    postarAviso("Você capturou uma peça do oponente! Turno do oponente.");
                }
                atualizarTabuleiro();
                return;
            }

            // FASE DE COLOCAÇÃO: Coloca uma das 12 peças iniciais no tabuleiro
            if (backend.isFaseColocacao()) {
                if (backend.colocarPeca(meuId, l, c)) {
                    try {
                        if (oponente != null) oponente.receberJogada(l, c);

                        jogadorAtual = oponenteId; // Passa a vez para o oponente

                        atualizarTabuleiro();

                        if (!backend.isFaseColocacao()) {
                            postarAviso("Fase de Colocação encerrada! Começou a Movimentação.");
                            JOptionPane.showMessageDialog(this, "Fase de Colocação encerrada! Começou a Movimentação.");
                        } else {
                            postarAviso("Peça colocada! Turno do oponente.");
                        }
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Movimento Inválido!\nNão é permitido formar trios na fase de colocação.", "Regras do Dara", JOptionPane.WARNING_MESSAGE);
                }
            }

            // FASE DE MOVIMENTAÇÃO: Move uma peça para um espaço adjacente vazio
            else {
                if (lOrigem == -1 && cOrigem == -1) {
                    // Seleciona a peça de origem
                    if (backend.getPeca(l, c) == meuId) {
                        lOrigem = l;
                        cOrigem = c;
                    }

                } else {
                    if (l == lOrigem && c == cOrigem) {
                        // Deseleciona a peça
                        lOrigem = -1;
                        cOrigem = -1;
                    }
                    // Tenta mover para o destino
                    else if (backend.moverPeca(meuId, lOrigem, cOrigem, l, c)) {
                        try {
                            if (oponente != null) oponente.receberMovimento(lOrigem, cOrigem, l, c);

                            if (backend.formouTrio(l, c, meuId)) {
                                modoCaptura = true; // Ativa modo captura para o próximo clique
                                postarAviso("Você formou um trio! Escolha uma peça do oponente para capturar.");
                            } else {
                                jogadorAtual = oponenteId; // Passa a vez para o oponente
                                postarAviso("Peça movida! Turno do oponente.");
                            }
                        } catch (RemoteException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        if (!(l == lOrigem && c == cOrigem)) {
                            JOptionPane.showMessageDialog(this, "Movimento Inválido!\nSó é permitido mover para espaços adjacentes vazios.", "Regras do Dara", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    lOrigem = -1;
                    cOrigem = -1;
                }
            }
            atualizarTabuleiro();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    // 2. Método para o botão de Desistir
    private void Desistir() {
        int resposta = JOptionPane.showConfirmDialog(this, "Deseja mesmo abandonar a partida?", 
        "Confirmação Windows XP", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            try {
                if (oponente != null) oponente.receberDesistencia();
                JOptionPane.showMessageDialog(this, "Você desistiu! O oponente venceu!");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }
    }

    // 3. Método para enviar as mensagens do Chat
    private void enviarMensagem() {
        String texto = campoTexto.getText().trim();
        if (!texto.isEmpty()) {
            try {
                // Envia pela rede
                if (oponente != null) oponente.receberMensagem("Oponente", texto);
                // Mostra localmente
                adicionarMensagemChat("Você", texto, "blue");
                // Limpa o campo
                campoTexto.setText("");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // 4. Método auxiliar para formatar o Chat em HTML (Estilo Vintage)
    public void adicionarMensagemChat(String autor, String msg, String cor) {
        historicoHTML += "<b><font color='" + cor + "'>" + autor + ":</font></b> " + msg + "<br>";
        areaChat.setText("<html><body style='font-family: Tahoma; font-size: 10px;'>" + historicoHTML + "</body></html>");
    
        // Garante que o scroll desça automaticamente
        areaChat.setCaretPosition(areaChat.getDocument().getLength());
    }

    @Override
    public void receberMensagem(String autor, String msg) throws RemoteException {
        if (autor.equals("Sistema")) {
            postarAviso(msg);
        } else {
            adicionarMensagemChat(autor, msg, "red"); // Mensagem do oponente em vermelho
        }
    }

    @Override
    public void receberDesistencia() throws RemoteException {
        JOptionPane.showMessageDialog(this, "O oponente desistiu! Vitória sua!");
        System.exit(0);
    }

    @Override
    public void receberRemocao(int l, int c) throws RemoteException {
        this.lOrigem = -1; this.cOrigem = -1; // Limpa qualquer seleção pendente
        backend.removerPeca(oponenteId, l, c);
        atualizarTabuleiro();

        this.jogadorAtual = meuId; // Agora é o meu turno
        postarAviso("O oponente capturou uma de suas peças! É a sua vez de jogar!");

        int vencedor = backend.verificarVencedor();
        if (vencedor == oponenteId ) {
            try {
                if (oponente != null) oponente.anunciarVitoria();
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Você ficou com apenas 2 peças. O oponente venceu!", "Derrota", JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            });
            
        }
    }

    @Override
    public void receberJogada(int l, int c) throws RemoteException {
        this.lOrigem = -1; this.cOrigem = -1; // Limpa qualquer seleção pendente
        // Aqui atualizamos o backend e a UI com a jogada que veio da rede
        backend.colocarPeca(oponenteId, l, c);
        atualizarTabuleiro();

        if (!backend.isFaseColocacao()) {
            postarAviso("Fase de Colocação encerrada! Começou a Movimentação.");
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "Fase de Colocação encerrada! Começou a Movimentação.");
            });
        } 

        if (backend.formouTrio(l, c, oponenteId)) {
            this.jogadorAtual = oponenteId; // O oponente continua jogando para capturar
            postarAviso("O oponente formou um trio! Ele pode capturar uma peça sua.");
        } else {
            this.jogadorAtual = meuId; // Agora é o meu turno
            postarAviso("É a sua vez de jogar!");
        }
    }

    @Override
    public void receberMovimento(int lOrigem, int cOrigem, int lDestino, int cDestino) throws RemoteException {
        this.lOrigem = -1; this.cOrigem = -1; // Limpa qualquer seleção pendente
        backend.moverPeca(oponenteId, lOrigem, cOrigem, lDestino, cDestino);
        
        if (backend.formouTrio(lDestino, cDestino, oponenteId)) {
            this.jogadorAtual = oponenteId; // O oponente continua jogando para capturar
            postarAviso("O oponente formou um trio! Ele pode capturar uma peça sua.");
        } else {
            this.jogadorAtual = meuId; // Agora é o meu turno
            postarAviso("É a sua vez de jogar!");
        }
        atualizarTabuleiro();
    }

    @Override
    public void anunciarVitoria() throws RemoteException {
        jogoFinalizado = true;
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Parabéns! Você venceu o jogo Dara!", "Vitória!", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        });
        
    }
}