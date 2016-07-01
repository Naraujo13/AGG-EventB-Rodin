package GraphGrammar;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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
    String type;
    //Collection de Nodos contendo o tipos dos nodos permitidos
    
    List <NodeType> allowedNodes;
    //Collection <String> allowedNodes;
    //Mapa associando ID usado nas arestas do grafo ID do tipo (confusão do AGG)
    Map <String, String> translationNodes;
    //ArrayList de Arestas  contedo obejtos Edges(tipo, source, target)
    List <EdgeType> allowedEdges;
  
    /*Construtor
    */
    public TypeGraph (){
        type = "TG";
        translationNodes = new HashMap<>();
        allowedNodes = new ArrayList <> ();
        allowedEdges = new ArrayList <> ();
    }
    
      /**
     * Método de acesso ao hashmap para tradução da confusão do agg.
     * @return hashmap com idConfusao->idRealDoTipo
     */
    public Map<String,String> getTranslationNodes(){
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
    public List<NodeType> getAllowedNodes(){
        return allowedNodes;
    }
    
    /**
     * Método de acesso as arestas de um grafo tipo.
     * @return retorna arraylist com arestas do grafo tipo.
     */
    public List<EdgeType> getAllowedEdges(){
        return allowedEdges;
    }
    
      /**
     * Função que adiciona aresta ao grafo tipo atual.
     * @param e - ser adicionada 
     */
    public void addEdge (EdgeType e){
        allowedEdges.add(e);
    }
    
    /**
     * Função que realiza a tradução dos IDs de um grafo tipo para desfazer confusão do AGG
     * @param ID - ID a ser traduzido
     * @return - Retorna uma string contendo o ID traduzido
     */
    public String translate(String ID){
        return translationNodes.get(ID);
    }
    
    /**
     * Função que define nodos do grafo tipo atual, extraindo-os do arquivo corrente.
     * @param tokenAtual - tokenAtualmente sendo analisado no arquivo
     * @param entrada - scanner do arquivo sendo lido no momento
     */
    public void defineTypeGraphEdges(String tokenAtual,Scanner entrada){
        EdgeType newEdgeType;
        while(tokenAtual.contains("Edge")){
            
             //Vetores de String Auxiliares para quebrar comandos
            String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
            String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
            
            //Extrai tipo da aresta e salva como ID
            auxiliar = tokenAtual.split(" ");
            auxiliar2 = auxiliar[4].split("\"");
            newEdgeType = new EdgeType(auxiliar2[1]);
            
            //Extrai Source
            auxiliar2 = auxiliar[2].split("\"");
            newEdgeType.addSource(this.translate((auxiliar2[1])));
            
            //Extrai Target
            auxiliar2 = auxiliar[3].split("\"");
            newEdgeType.addTarget(this.translate(auxiliar2[1]));
            
            //Descarta opções de Layout
            while (!tokenAtual.contains("/Edge")){
                tokenAtual = entrada.next(); 
            }
            //Descarta /Edge
            tokenAtual = entrada.next();
        }
    }  
}
