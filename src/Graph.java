
import java.util.ArrayList;
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
public class Graph {
    
    String type;
    List <Edge> graphEdges;
    List <Node> graphNodes;
    Map <String, String> morphism;
    
    public Graph (String type){
        this.type = type;
        graphEdges = new ArrayList <> ();
        graphNodes = new ArrayList <> ();
        morphism = new HashMap <> ();
    }
    
    public void addNode(Node newNode){
        graphNodes.add(newNode);
    }
    
    public void addEdge(Edge newEdge){
        graphEdges.add(newEdge);
    }
    
    public Map<String, String> getMorphism(){
        return morphism;
    }
}
