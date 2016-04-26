
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
 * Esta classe representa uma instancia de nodo no grafo host.
 * Ela herda da classe NodeType (nodos do grafo tipo) e difere da mesma pois
 * armazena o ID do nodo.
 */
public class Node extends NodeType{
    
    String ID;      //ID que identifica esta instância de nodo
    //HashMaps que armazenam os atributos, ID->Valor
    Map <String, Integer> integerAttributes;
    Map <String, Float> floatAttributes;
    Map <String, Double> doubleAttributes;
    Map <String, Boolean> booleanAttributes;
    Map <String, Long> longAttributes;
    Map <String, String> stringAttributes;
    //Pegar nomes dos atributos

    /*Construtor que recebe como parâmetro o tipo do nodo e sua identificação única*/
    public Node(String type, String ID){
        super(type);
        this.ID = ID;
        integerAttributes = new HashMap<>();
        floatAttributes = new HashMap<>();
        doubleAttributes = new HashMap<>();
        booleanAttributes = new HashMap<>();
        longAttributes = new HashMap<>();
        stringAttributes = new HashMap<>();
    }

}

