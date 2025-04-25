package api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Pokemon {
    private int id;
    private String name;
    private int height;
    private int weight;
    
    @JsonProperty("base_experience")
    private int baseExperience;
    
    @JsonProperty("sprites")
    private Sprites sprites;
    
    // Clase interna para manejar los sprites
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sprites {
        @JsonProperty("front_default")
        private String frontDefault;

        public String getFrontDefault() {
            return frontDefault;
        }

        public void setFrontDefault(String frontDefault) {
            this.frontDefault = frontDefault;
        }
    }
    
    public Pokemon() {}
    
    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    
    public int getBaseExperience() { return baseExperience; }
    public void setBaseExperience(int baseExperience) { this.baseExperience = baseExperience; }
    
    public Sprites getSprites() { return sprites; }
    public void setSprites(Sprites sprites) { this.sprites = sprites; }
    
    @Override
    public String toString() {
        return name;
    }
}