import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.rmi.RemoteException;

public class JanelaJogo extends JFrame implements InterfaceJogo {
    private NovoTabuleiro backend;
    private InterfaceJogo oponente;
    private Conexao rede;
    private JButton[][] botoes = new JButton[5][6];
    private JTextPane areaChat;
    private String historicoHTML = "";
    private JTextField campoTexto;

    // --- CORES E ESTILO WINDOWS XP ---
    private final Color CINZA_XP = new Color(212, 208, 200); 
    private final Color AZUL_XP = new Color(0, 0, 128);
    private final Color VERMELHO_XP = new Color(128, 0, 0);

    private int jogadorAtual = NovoTabuleiro.JOGADOR1;
    private int lOrigem = -1;
    private int cOrigem = -1;
    private boolean modoCaptura = false;
    private boolean jogoFinalizado = false;
    private int meuId;
    private int oponenteId;

    public JanelaJogo(NovoTabuleiro tabuleiro, int id) {
        this.backend = tabuleiro;
        this.meuId = id;
        this.oponenteId = (id == NovoTabuleiro.JOGADOR1) ? NovoTabuleiro.JOGADOR2 : NovoTabuleiro.JOGADOR1;

        // 1. Configurações da Janela
        setTitle("Jogo Dara - Estilo Vintage");
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

    // --- MÉTODOS DE ATUALIZAÇÃO ---

    private void atualizarTabuleiro() {
        for (int l = 0; l < 5; l++) {
            for (int c = 0; c < 6; c++) {
                int peca = backend.getPeca(l, c);
                
                // Reseta visual base
                botoes[l][c].setBackground(CINZA_XP);
                botoes[l][c].setBorder(BorderFactory.createRaisedBevelBorder());

                if (peca == Tabuleiro.JOGADOR1) {
                    botoes[l][c].setText("X");
                    botoes[l][c].setForeground(AZUL_XP);
                } else if (peca == Tabuleiro.JOGADOR2) {
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
        // Por enquanto, apenas um teste para ver se funciona
        System.out.println("Clicou na posição: " + l + "," + c);
    
        // Aqui virá a lógica de:
        // 1. Verificar se é o seu turno
        // 2. Chamar o backend.colocarPeca ou moverPeca
        // 3. Enviar a jogada pela rede
        // 4. atualizarTabuleiro();
    }
    
    // 2. Método para o botão de Desistir
    private void Desistir() {
        int resposta = JOptionPane.showConfirmDialog(this, "Deseja mesmo abandonar a partida?", 
        "Confirmação Windows XP", JOptionPane.YES_NO_OPTION);
        if (resposta == JOptionPane.YES_OPTION) {
            if (rede != null) rede.enviar("SURRENDER");
            System.exit(0);
        }
    }

    // 3. Método para enviar as mensagens do Chat
    private void enviarMensagem() {
        String texto = campoTexto.getText().trim();
        if (!texto.isEmpty()) {
            // Envia pela rede
            if (rede != null) rede.enviar("CHAT;" + texto);
        
            // Mostra localmente
            adicionarMensagemChat("Você", texto, "blue");
        
            // Limpa o campo
            campoTexto.setText("");
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
        adicionarMensagemChat(autor, msg, "red"); // Mensagem do oponente em vermelho
    }

    @Override
    public void receberDesistencia() throws RemoteException {
        JOptionPane.showMessageDialog(this, "O oponente desistiu! Vitória sua!");
        System.exit(0);
    }

    @Override
    public void receberRemocao(int l, int c) throws RemoteException {
        backend.removerPeca(oponenteId, l, c);
        atualizarTabuleiro();
        this.jogadorAtual = meuId; // Agora é o meu turno
        adicionarMensagemChat("Sistema", "O oponente capturou uma de suas peças! É a sua vez de jogar!", "gray");
    }

    @Override
    public void receberJogada(int l, int c) throws RemoteException {
        // Aqui atualizamos o backend e a UI com a jogada que veio da rede
        backend.colocarPeca(oponenteId, l, c);
        atualizarTabuleiro();

        if (backend.formouTrio(l, c, oponenteId)) {
            adicionarMensagemChat("Sistema", "O oponente formou um trio! Ele pode capturar uma peça sua.", "gray");
            
            
        }

        this.jogadorAtual = meuId; // Agora é o meu turno
        adicionarMensagemChat("Sistema", "É a sua vez de jogar!", "green");
    }
}