package GraphGrammar;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    
    private String name;
    private Graph RHS;
    private Graph LHS;
    private LinkedHashSet<Graph> NAC;
    
    public Rule (String name, Graph RHS, Graph LHS){
        this.name = name;
        this.RHS = RHS;
        this.LHS = LHS;
        NAC = new LinkedHashSet<>();
    }
   
    /**
     * Método de acesso ao nome da regra.
     * @return retorna string representando nome da regra
     */
    public String getName(){
        return name;
    }
    
    /**
     * Método de acesso ao grafo representando o lado esquerdo da regra
     * @return - retorna grafo que represneta lado esquerdo da rega
     */
    public Graph getLHS(){
        return LHS;
    }
    
    /**
     * Método de acesso ao grafo represnetando o lado direito da regra
     * @return - retorna grafo represnetando o lado direito da regra
     */
    public Graph getRHS(){
        return RHS;
    }
    
    /**
     * Método de acesso ao hashset representando o conjunto de NACs da regra.
     * @return - retorna hashset representando o conjunto de NACs da regra.
     */
    public HashSet<Graph> getNACs(){
        return NAC;
    }
    
    /**
     * Função que insere uma condição de aplicação no conjunto das condições de
     * aplicação desta regra.
     * @param NAC - NAC a ser inserida na regra.
     */
    public void insertNAC(Graph NAC){
        this.NAC.add(NAC);
    }
    
    /**
     * Método que imprime as informações da regra corrente. Usado para criação
     * do log.
     * @return retorna true ou false, indicado sucesso ou falha, respectivamente. 
     */
    boolean printRule(){
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
