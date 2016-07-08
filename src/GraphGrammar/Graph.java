package GraphGrammar;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Graph {
    
    String type;
    HashSet <Edge> graphEdges;
    HashSet <Node> graphNodes;
    Map <String, String> morphism;
    
    /**
     * Construtor da classe grafo. Cria novo grafo baseado no tipo passado como
     * parâmetro.
     * @param type - tipo do grafo a ser criado 
     */
    public Graph (String type){
        this.type = type;
        graphEdges = new HashSet <> ();
        graphNodes = new HashSet <> ();
        morphism = new HashMap <> ();
    }
    
    /**
     * Função que adiciona novo nodo ao grafo atual.
     * @param newNode - nodo a ser adicionado ao arraylist
     */
    public void addNode(Node newNode){
        graphNodes.add(newNode);
    }
    
    /**
     * Função que adiciona nova aresta ao arraylist de arestas do grafo
     * @param newEdge aresta ser adicionada ao arraylist
     */
    public void addEdge(Edge newEdge){
        graphEdges.add(newEdge);
    }
    
    /**
     * Função que retorna o morfismo do grafo.
     * @return retorna hashmap com o morfismo do grafo (ID->ID)
     */
    public Map<String, String> getMorphism(){
        return morphism;
    }

    
    

    
    
      public boolean printGraph(){
        int i;
        System.out.println("\t\t Grafo:");
        if (this.type == null){
            System.out.println("\t\tErro: o grafo atual não possui tipo.");
            return false;
        }
        else
            System.out.println("\t\t  Tipo: " + this.type);
        
        if (this.graphNodes.isEmpty())
            System.out.println("\t\tO grafo atual não possui nodos.");
        else{
            System.out.println("\t\t  Nodos:");
            i=0;
            for (Node node: graphNodes){
                System.out.println("\t\t   Nodo " + i+":");
                i++;
                if (!node.printNode())
                    return false;
            }
            if (this.graphEdges.isEmpty())
                System.out.println("\t\tO grafo atual não possui arestas.");
            else{
                System.out.println("\t\t  Arestas:");
                i=0;
                for (Edge edge: graphEdges){
                    System.out.println("\t\t   Aresta " + i+":");
                    i++;
                    if(!edge.printEdge())
                        return false;
                }                
            }
        }
        //Imprimir morfismo do grafo.
        return true;
    }
}
