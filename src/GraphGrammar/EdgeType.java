package GraphGrammar;


import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class EdgeType {
    private String type;
    private ArrayList <String> source; //Lista com IDs que representam os tipos dos nodos fonte
    private ArrayList <String> target; //Lista com IDs que representam os tipos dos nodos alvo
    
    /* Construtor da classe Edge, que recebe nome e ID como parâmetros e cria uma definição de aresta para o grafo
    *  @param name - nome da aresta criada
    *  @param ID - ID da aresta criada
    
    */
    public EdgeType(String type){
        this.type = type;
        source = new ArrayList<>();
        target = new ArrayList<>();
        
    }
    
     /* Método que retorna o ID da aresta
     *@return - retorna uma String representando o ID da aresta
    */
    public String getType(){
        return type;
    }
    
    /**
     * Método de acesso a lista de nodos fonte
     * @return - retorna lista com nodos fonte desta aresta
     */
    public List<String> getSource(){
        return source;
    }
    
    /**
     * Método de acesso a lista de nodos destino.
     * @return - retorna lista com nodos destino desta aresta
     */
    public List<String> getTarget(){
        return target;
    }
    
    /**
     * Método para adição de um novo tipo de nodo fonte para a aresta
     * @param type - tipo de nodo a ser adicionado a lista
     */
    public void addSource(String type){
        source.add(type);
    }
    
    /**
     * Método para adição de um novo tipo de nodo fonte para a aresta.
     * @param type - tipo de nodo a ser adicionado a lista.
     */
    public void addTarget(String type){
        target.add(type);
    }
}
