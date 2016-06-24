package AGG;


import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Edge{
    
    String type;    //TD que identifica o tipo da aresta
    String ID;      //ID que identifica esta instância de aresta
    String source;  //ID do nodo fonte
    String target;  //ID do nodo alvo
    
    public Edge (String type, String ID, String source, String target){
        this.type = type;
        this.ID = ID;
        this.source = source;
        this.target = target;
    }  
    
}
