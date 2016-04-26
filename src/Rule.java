
import java.util.ArrayList;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nicol
 */
public class Rule {
    
    String name;
    Graph RHS;
    Graph LHS;
    List <Graph> NAC;
    
    public Rule (String name, Graph RHS, Graph LHS){
        this.name = name;
        this.RHS = RHS;
        this.LHS = LHS;
        NAC = new ArrayList<>();
    }
   
    public void insertNAC(Graph NAC){
        this.NAC.add(NAC);
    }
}
