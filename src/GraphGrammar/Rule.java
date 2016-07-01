package GraphGrammar;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

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

    /**
     * Função chamada pela função defineRules() para definir as NACs
     * @param rule - regra que terá suas regras de aplicação definidas
     * @param attNames - map contendo os nomes dos atributos associados ao seu ID
     * @param attTypes - map contendo os tipos dos atributos associados ao seu ID
     */
    public void defineApplicationConditions(String tokenAtual,Scanner entrada, Map <String,String> attNames, Map <String, String> attTypes){
        
            Graph newNAC;
        
             //Condições de Aplicação
            if (tokenAtual.contains("ApplCondition")){
                tokenAtual = entrada.next();
                
                //NAC
                while (tokenAtual.contains("NAC")){ //cada iteração do while é uma NAC
                    tokenAtual = entrada.next();
                    
                    newNAC = new Graph("NAC");
                    //Itera pós ID-Nome e etc do NAC
                    tokenAtual = entrada.next();
                    //Define Nodos
                    newNAC.defineGraphNodes(tokenAtual, entrada, attNames, attTypes);
                    //Define Arestas
                    newNAC.defineGraphEdges(tokenAtual, entrada);
                    
                    //Descarta /Graph
                   tokenAtual = entrada.next();
                   
                   //Inserir Morfismo de LHS -> NAC
                   newNAC.defineMorphism(tokenAtual, entrada);
                   
                   //Itera /NAC
                   if (tokenAtual.contains("/NAC"))
                       tokenAtual = entrada.next();
                   
                   //Adiciona NAC no ArrayList
                   this.insertNAC(newNAC);
                }
                
                //Itera /ApplCondition
                if (tokenAtual.contains("/ApplCondition"))
                    tokenAtual = entrada.next();           
            }
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
