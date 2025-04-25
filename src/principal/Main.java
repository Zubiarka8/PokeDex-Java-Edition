package principal;

import api.Pokemon;
import api.PokemonInfo;

public class Main {
    public static void main(String[] args) {
        PokemonInfo info = new PokemonInfo();
        Pokemon p = info.obtenerPokemon("pikachu");
        
        if(p != null) {
            System.out.println("Pokémon obtenido: " + p);
            System.out.println("ID: " + p.getId());
            System.out.println("Nombre: " + p.getName());
            System.out.println("Altura: " + p.getHeight());
            System.out.println("Peso: " + p.getWeight());
            System.out.println("Experiencia base: " + p.getBaseExperience());
        } else {
            System.out.println("No se pudo obtener el Pokémon");
        }
    }
}