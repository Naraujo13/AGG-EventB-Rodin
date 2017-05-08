package GraphGrammar;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * String type; - indica tipo do grafo, normalmente sendo "RHS", "LHS" ou "HOST".
 * HashSet <Edge> graphEdges - HashSet representando o conjunto de arestas do grafo.
 * HashSet <Node> graphNodes - HashSet representando o conjunto de nodos do grafo.
 * Map <String, String> morphism - HashMap representando do grafo em relação a outro, tal que:
 *  chave -> ID no grafo atual
 *  valor -> ID no grafo que está sendo mapeado
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Graph {
    
    private String type;
    private HashSet <Edge> graphEdges;
    private HashSet <Node> graphNodes;
    private Map <String, String> morphism;
    
    //Nodes with Attributes
    private HashMap <String, Node> attNodes;
    
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
        attNodes = new HashMap<>();
    }
     
    /**
     * Método de acesso aos nodos do grafo
     * @return - hashset com nodos do grafo
     */
    public HashSet<Node> getNodes(){
        return graphNodes;
    }
    
    /**
     * Método de acesso as arestas do grafo.
     * @return - hashset com arestas do grafo
     */
    public HashSet<Edge> getEdges(){
        return graphEdges;
    }
    
    /**
     * Função que adiciona novo nodo ao grafo atual.
     * @param newNode - nodo a ser adicionado ao arraylist
     */
    public void addNode(Node newNode){
        graphNodes.add(newNode);
        if (!newNode.getAttributes().isEmpty())
            attNodes.put(newNode.getType(), newNode);
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

    /**
     * Função get para hashmap contendo nodos com atributos
     * @return - retorna HashMap com nodos que possue atributos (Nome -> Nodo)
     */
    public HashMap<String, Node> getAttNodes(){
        return attNodes;
    }
    
     /**
     * Função privada para adicionar os nodos com atributos em um hashmap para
     * cálculos da tradução.
     * @param attNode - nodo a ser adicionado
     */
    private void addAttNode(Node attNode){
        attNodes.put(attNode.getType(), attNode);
    }
    
    
    

    
    /**
     * Função que imprime o grafo. Usado para criaçãod e log.
     * @return - função que gera log  para estrutura
     */
      boolean printGraph(){
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
