package GraphGrammar;


import java.util.ArrayList;
import java.util.HashSet;
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
public class Rule {
    
    String name;
    Graph RHS;
    Graph LHS;
    HashSet <Graph> NAC;
    
    public Rule (String name, Graph RHS, Graph LHS){
        this.name = name;
        this.RHS = RHS;
        this.LHS = LHS;
        NAC = new HashSet<>();
    }
   
    public void insertNAC(Graph NAC){
        this.NAC.add(NAC);
    }
 
    public boolean printRule(){
        System.out.println("\t\tImprimindo a regra " + this.name + ":");
        if (this.LHS == null){
            System.out.println("\t\t\tErro. A regra possui erros no LHS.");
            return false;
        }
        
        this.LHS.printGraph();
    
        
        if (this.RHS == null){
            System.out.println("\t\t\tErro. A regra possui erros no RHS.");
            return false;
        }
        
        this.RHS.printGraph();
        
        if (NAC.isEmpty())
            System.out.println("\t\t\tEste grafo não possui NACs.");
        else{
            System.out.println("\t\t\tImprimindo NACs...");
            for (Graph nac: NAC){
                nac.printGraph();
            }
        }
        return true;
    }
}
