
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nicol
 */


public class TypeGraph {
    String type;
    //Collection de Nodos contendo o tipos dos nodos permitidos
    Collection <String> allowedNodes;
    //Mapa associando ID usado nas arestas do grafo ID do tipo (confusão do AGG)
    Map <String, String> translationNodes;
    //ArrayList de Arestas  contedo obejtos Edges(tipo, source, target)
    List <EdgeType> allowedEdges;
  
    /*Construtor
    */
    public TypeGraph (){
        type = "TG";
        translationNodes = new HashMap<>();
        allowedNodes = translationNodes.values();       //terá sempre todos os valores
        allowedEdges = new ArrayList <> ();
    }
    
     /* Adiciona nodo a lista
    */
    public void addNode (String ID, String type){
       translationNodes.put(ID, type);
    }
    
    /* Adiciona aresta a lista
    */
    public void addEdge (EdgeType e){
        allowedEdges.add(e);
    }
    
    //Acessa hashmap e retorna tradução
    public String translate(String ID){
        return translationNodes.get(ID);
    }
}
