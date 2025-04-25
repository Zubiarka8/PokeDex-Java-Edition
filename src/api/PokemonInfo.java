package api;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PokemonInfo {
    // Convierte JSON a objeto Pokemon
    public Pokemon obtenerPokemon(String nombrePokemon) {
        try {
            // 1. Obtener el JSON de la API
            String json = PokemonAPI.getPokemonDatuak("pikachu");
            
            // 2. Crear el traductor JSON->Objeto
            ObjectMapper mapeador = new ObjectMapper();
            
            // 3. Convertir el JSON a objeto Pokemon
            return mapeador.readValue(json, Pokemon.class);
            
        } catch (Exception e) {
            System.out.println("Error al obtener el Pokémon: " + e.getMessage());
            return null;
        }
    }
}