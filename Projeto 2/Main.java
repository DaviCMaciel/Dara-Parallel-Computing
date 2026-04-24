import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Define o visual nativo do sistema (Windows XP/Luna no seu caso)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        NovoTabuleiro t = new NovoTabuleiro();
        new JanelaJogo(t, 1).setVisible(true);
    }
}