/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nicol
 */
public class NodeType {
    
    String type;


    /* Construtor da classe Node, que recebe nome e ID como parâmetros e cria uma definição de vértice para o grafo
    *  @param name - nome do nodo criado
    *  @param ID - ID do nodo criado
    */
    public NodeType(String type){
        this.type = type;
    }
    
     /* Método que retorna o tipo do nodo 
     *@return - retorna uma String representando o tipo do nodo
    */
    public String getType(){
        return type;
    }
}

