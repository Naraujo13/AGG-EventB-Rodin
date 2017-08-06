package GraphGrammar;

import java.util.LinkedHashSet;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class NodeType {
    
    private String type;
    private LinkedHashSet<AttributeType> attributes;

    /* Construtor da classe Node, que recebe nome e ID como parâmetros e cria uma definição de vértice para o grafo
    *  @param name - nome do nodo criado
    *  @param ID - ID do nodo criado
    */
    public NodeType(String type){
        this.type = type;
        attributes = new LinkedHashSet <>();
    }
    
     /* Método que retorna o tipo do nodo 
     *@return - retorna uma String representando o tipo do nodo
    */
    public String getType(){
        return type;
    }
    
    /**
     * Método que retorna arrayList com atributos do nodo.
     * @return - ArrayList com atributos do nodo
     */
    public LinkedHashSet<AttributeType> getAttributes(){
        return attributes;
    }
    
    public void addAttribute(AttributeType attType){
        attType.setIndex(Integer.toString(this.attributes.size()));
        attributes.add(attType);
    }
    
   
}

