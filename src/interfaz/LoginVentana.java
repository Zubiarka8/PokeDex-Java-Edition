package interfaz;

import javax.swing.*;
import bd.MysqlSesioKudeaketa;
import java.awt.*;
import java.awt.event.*;

public class LoginVentana extends JFrame {
    private MysqlSesioKudeaketa sesioKudeaketa;

    public LoginVentana() {
        // Configuración de la ventana
        setTitle("Saioa Hasi");
        setSize(350, 200); // Aumentado un poco el tamaño
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel principal con BorderLayout
        JPanel panelNagusia = new JPanel(new BorderLayout(10, 10));
        panelNagusia.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel para los campos de entrada
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        // Componentes para usuario y contraseña
        JLabel erabiltzaileLabel = new JLabel("Erabiltzailea:");
        JTextField erabiltzaileaField = new JTextField();
        JLabel pasahitzaLabel = new JLabel("Pasahitza:");
        JPasswordField pasahitzaField = new JPasswordField();

        inputPanel.add(erabiltzaileLabel);
        inputPanel.add(erabiltzaileaField);
        inputPanel.add(pasahitzaLabel);
        inputPanel.add(pasahitzaField);

        // Panel para el botón
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton sartuButton = new JButton("Sartu");

        // Configuración del botón
        sartuButton.setPreferredSize(new Dimension(100, 30));
        buttonPanel.add(sartuButton);

        // Añadir paneles al panel principal
        panelNagusia.add(inputPanel, BorderLayout.CENTER);
        panelNagusia.add(buttonPanel, BorderLayout.SOUTH);

        // Acción del botón
        this.sesioKudeaketa = new MysqlSesioKudeaketa();
        sartuButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String erabiltzailea = erabiltzaileaField.getText().trim();
                String pasahitza = new String(pasahitzaField.getPassword()).trim();
                
                if(erabiltzailea.isEmpty() || pasahitza.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginVentana.this, 
                        "Mesedez, bete erabiltzailea eta pasahitza", 
                        "Oharra", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                boolean autentifikazioa = sesioKudeaketa.irekiSesioa(erabiltzailea, pasahitza);
                
                if(autentifikazioa) {
                    // Cerrar la ventana de login
                    dispose();
                    
                    // Mostrar mensaje de bienvenida
                    JOptionPane.showMessageDialog(null, "Ongi etorri " + erabiltzailea);
                    
                    // Abrir la ventana de Pokémon
                    SwingUtilities.invokeLater(() -> {
                        PokemonVentana pokemonVentana = new PokemonVentana();
                        pokemonVentana.setVisible(true);
                    });
                } else {
                    JOptionPane.showMessageDialog(LoginVentana.this, 
                        "Errorea: Erabiltzailea edo pasahitza okerrak", 
                        "Errorea", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Añadir panel principal a la ventana
        add(panelNagusia);
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginVentana ventana = new LoginVentana();
            ventana.setSesioKudeaketa(new MysqlSesioKudeaketa());
        });
    }

    // Getters y setters
    public MysqlSesioKudeaketa getSesioKudeaketa() {
        return sesioKudeaketa;
    }

    public void setSesioKudeaketa(MysqlSesioKudeaketa sesioKudeaketa) {
        this.sesioKudeaketa = sesioKudeaketa;
    }
}