// Project: PokemonApi
package api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Clase principal que demuestra cómo consumir la API de Pokémon (PokeAPI) para obtener y mostrar información sobre un
 * Pokémon específico.
 */

public class PokemonAPI {
    public static final HttpClient conn = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String getPokemonDatuak(String pokemon) throws Exception
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://pokeapi.co/api/v2/pokemon/" + pokemon))
                .GET()
                .build();
        
        HttpResponse<String> response = conn.send(request, HttpResponse.BodyHandlers.ofString());
        
        if(response.statusCode() == 200) {
            return response.body();
        }
        
         throw new RuntimeException("Pokémon no encontrado");
        
    }

}
