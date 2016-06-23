
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
 * @author Nícolas Oreques de Araujo
 * Esta classe representa uma instancia de nodo no grafo host.
 * Ela herda da classe NodeType (nodos do grafo tipo) e difere da mesma pois
 * armazena o ID do nodo.
 */
public class Node extends NodeType{
    
    String ID;      //ID que identifica esta instância de nodo

    /*Construtor que recebe como parâmetro o tipo do nodo e sua identificação única*/
    public Node(String type, String ID){
        super(type);
        this.ID = ID;
    }

    public void insertAttribute(String type, String name, String ID, String value){
        Attribute newAtt = new Attribute(type, ID, name, value);
        attributes.add(newAtt);
    }
    
}

