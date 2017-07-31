package GraphGrammar;


import java.util.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */


public class TypeGraph {

    private String type;

    //Collection de Nodos contendo o tipos dos nodos permitidos
    private LinkedHashSet<NodeType> allowedNodes;

    //Mapa associando ID usado nas arestas do grafo ID do tipo (confusão do AGG)
    private LinkedHashMap<String, String> translationNodes;

    //ArrayList de Arestas  contedo obejtos Edges(tipo, source, target)
    private LinkedHashSet<EdgeType> allowedEdges;
    
    //Nodes with Attributes
    private LinkedHashMap<String, NodeType> attNodes;
  
    /*Construtor
    */
    public TypeGraph (){
        type = "TG";
        translationNodes = new LinkedHashMap<>();
        allowedNodes = new LinkedHashSet <> ();
        allowedEdges = new LinkedHashSet <> ();
        attNodes = new LinkedHashMap<>();
    }
    
      /**
     * Método de acesso ao hashmap para tradução da confusão do agg.
     * @return hashmap com idConfusao->idRealDoTipo
     */
    public LinkedHashMap<String, String> getTranslationNodes(){
        return translationNodes;
    }
    
     /**
      * Adiciona nodo em um hasmap para tradução.
      * @param ID - ID associado a esta instancia.
      * @param type - ID relacionado ao tipo
      */
    public void addTranslNode (String ID, String type){
       translationNodes.put(ID, type);
       
    }
    
    /**
     * Método de acesso aos nodos de um grafo tipo.
     * @return - retorna arraylist com nodos do grafo tipo
     */
    public LinkedHashSet<NodeType> getAllowedNodes(){
        return allowedNodes;
    }
    
    /**
     * Função para adicionar nodo 
     * @param node - nodo a ser adicionado
     */
    public void addAllowedNode(NodeType node){
        allowedNodes.add(node);
        if (!node.getAttributes().isEmpty())
            attNodes.put(node.getType(), node);
    }
    
    /**
     * Método de acesso as arestas de um grafo tipo.
     * @return retorna arraylist com arestas do grafo tipo.
     */
    public LinkedHashSet<EdgeType> getAllowedEdges(){
        return allowedEdges;
    }
    
    /**
     * Função para adicionar arestas 
     * @param edge - aresta a ser adicionada
     */
    public void addAllowedEdge(EdgeType edge){
        allowedEdges.add(edge);
    }
    
      /**
     * Função que adiciona aresta ao grafo tipo atual.
     * @param e - ser adicionada 
     */
    public void addEdge (EdgeType e){
        allowedEdges.add(e);
    }
    
    /**
     * Função get para hashmap contendo nodos com atributos
     * @return 
     */
    public LinkedHashMap<String, NodeType> getAttNodes(){
        return attNodes;
    }
    
    /**
     * Função privada para adicionar os nodos com atributos em um hashmap para
     * cálculos da tradução.
     * @param attNode 
     */
    private void addAttNode(NodeType attNode){
        attNodes.put(attNode.getType(), attNode);
    }
    
    /**
     * Função que realiza a tradução dos IDs de um grafo tipo para desfazer confusão do AGG
     * @param ID - ID a ser traduzido
     * @return - Retorna uma string contendo o ID traduzido
     */
    public String translate(String ID){
        return translationNodes.get(ID);
    }
}
