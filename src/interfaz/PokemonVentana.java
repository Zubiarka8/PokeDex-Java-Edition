package interfaz;

import api.Pokemon;
import api.PokemonAPI;
import api.PokemonInfo;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class PokemonVentana extends JFrame {
    private JPanel mainPanel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton randomButton;
    private JPanel pokemonPanel;
    
    public PokemonVentana() {
        setTitle("Pokédex");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Crear componentes
        mainPanel = new JPanel(new BorderLayout());
        
        // Panel de búsqueda
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        searchButton = new JButton("Buscar");
        randomButton = new JButton("6 Pokémon Aleatorios");
        
        searchPanel.add(new JLabel("Nombre o ID:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(randomButton);
        
        // Panel de información de Pokémon
        pokemonPanel = new JPanel();
        pokemonPanel.setLayout(new GridLayout(2, 3, 10, 10)); // 2 filas, 3 columnas
        
        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(pokemonPanel);
        
        // Añadir componentes al panel principal
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(mainPanel);
        
        // Listeners
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchPokemon();
            }
        });
        
        randomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getRandomPokemons();
            }
        });
        
        // Mostrar 6 Pokémon aleatorios al iniciar
        getRandomPokemons();
    }
    
    private void searchPokemon() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingresa un nombre o ID de Pokémon", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            PokemonInfo pokemonInfo = new PokemonInfo();
            Pokemon pokemon = pokemonInfo.obtenerPokemon(searchTerm);
            displayPokemon(pokemon);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Pokémon no encontrado", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void getRandomPokemons() {
        pokemonPanel.removeAll(); // Limpiar panel
        
        PokemonInfo pokemonInfo = new PokemonInfo();
        Random random = new Random();
        
        for (int i = 0; i < 6; i++) {
            int randomId = random.nextInt(898) + 1; // Hay 898 Pokémon en la API
            try {
                Pokemon pokemon = pokemonInfo.obtenerPokemon(String.valueOf(randomId));
                addPokemonToPanel(pokemon);
            } catch (Exception ex) {
                i--; // Intentar de nuevo si hay error
                ex.printStackTrace();
            }
        }
        
        pokemonPanel.revalidate();
        pokemonPanel.repaint();
    }
    
    private void addPokemonToPanel(Pokemon pokemon) {
        JPanel singlePokemonPanel = new JPanel();
        singlePokemonPanel.setLayout(new BoxLayout(singlePokemonPanel, BoxLayout.Y_AXIS));
        singlePokemonPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        
        JLabel nameLabel = new JLabel(pokemon.getName(), JLabel.CENTER);
        JLabel idLabel = new JLabel("ID: " + pokemon.getId(), JLabel.CENTER);
        JLabel heightLabel = new JLabel("Altura: " + pokemon.getHeight(), JLabel.CENTER);
        JLabel weightLabel = new JLabel("Peso: " + pokemon.getWeight(), JLabel.CENTER);
        
        // Estilo
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        singlePokemonPanel.add(nameLabel);
        singlePokemonPanel.add(idLabel);
        singlePokemonPanel.add(heightLabel);
        singlePokemonPanel.add(weightLabel);
        
        pokemonPanel.add(singlePokemonPanel);
    }
    
    private void displayPokemon(Pokemon pokemon) {
        pokemonPanel.removeAll();
        addPokemonToPanel(pokemon);
        pokemonPanel.revalidate();
        pokemonPanel.repaint();
    }
}