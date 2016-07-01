package GraphGrammar;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Grammar {
    
   
    //Atributos da gramática
    String name;        //nome associado a gramática
   List <Rule> rules;   //Arraylist com as regras da gramática
   Graph host;          //Grafo host
   TypeGraph typeGraph;     //Grafo tipo
       
    public Grammar(String name){
        this.name = name;
        typeGraph = new TypeGraph();
        rules = new ArrayList <>();
    }
     
    /**
     * Método de acesso ao grafo tipo
     * @return - grafo tipo.
     */
    public TypeGraph getTypeGraph(){
        return typeGraph;
    }
    
     /**
     * Método de acesso ao grafo host
     * @return - grafo host.
     */
    public Graph getHost(){
        return host;
    }
    
    public void setHost(Graph host){
        this.host = host;
    }
    
    /**
     * Função que define as Regras de uma Gramática (RHS, LHS e NACs)
     * @param tokenAtual - tokenAtualmente sendo analisado no arquivo
     * @param entrada - scanner do arquivo sendo lido no momento
     * @param attNames map contendo os nomes dos atributos associado ao seu ID
     * @param attTypes map contendo os tipos dos atributos associados ao seu ID
     */
    public void defineRules(String tokenAtual,Scanner entrada, Map <String, String> attNames, Map <String, String> attTypes){
        //Definição de uma Regra...
       Graph RHS = null, LHS = null;
       Rule rule = null;
       String ruleName;
       
        //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
       
       while (tokenAtual.contains("Rule")){
           
           //Pega nome da regra.
           auxiliar = tokenAtual.split(" ");
           auxiliar2 = auxiliar[3].split("\"");
           ruleName = auxiliar2[1];
           
           
           while (!tokenAtual.contains("Graph")){
            tokenAtual = entrada.next();
           }
           
                //Define LHS
            if (tokenAtual.contains("LHS")){
                tokenAtual = entrada.next();
                LHS = new Graph ("LHS");
               //Define Nodos
               LHS.defineGraphNodes(tokenAtual, entrada, attNames, attTypes);
               //DEfine arestas
               LHS.defineGraphEdges(tokenAtual, entrada);
               //Descarta /Graph
               tokenAtual = entrada.next(); 
            }
            
            while (!tokenAtual.contains("Graph")){
                tokenAtual = entrada.next();
            }
            
           //Define RHS
           if (tokenAtual.contains("RHS")){
               tokenAtual = entrada.next();
               RHS = new Graph ("RHS");
               //Define Nodos
               RHS.defineGraphNodes(tokenAtual, entrada, attNames, attTypes);
               //DEfine arestas
               RHS.defineGraphEdges(tokenAtual, entrada);
               //Descarta /Graph
               tokenAtual = entrada.next(); 
           }
      
            
            //Cria regra e insere RHS e LHS definidos acima
            if (RHS != null && LHS != null)
                rule = new Rule(ruleName, RHS, LHS);
            else
                System.out.println("Erro ao definir uma regra");
            
            //Define Morfismo de RHS -> LHS
            if (RHS != null)
                RHS.defineMorphism(tokenAtual, entrada);
                                               
            //Condições de Aplicação
            if (rule != null)
                rule.defineApplicationConditions(tokenAtual, entrada, attNames, attTypes);
            
            //Itera Layer, Prioridade e /Rule
            while (!tokenAtual.contains("/Rule"))
                tokenAtual = entrada.next();
            tokenAtual = entrada.next();
            
            //Adiciona regra no Araylist de regras;
            rules.add(rule);
            
             //Funcionando até aqui para 1 regra, testar com mais
       }
       //FIM DEFINIÇÂO DE REGRA
    }
  
    public boolean printGrammar(){
        System.out.println("Imprimindo gramática " + this.name + "...");
        
        /* Grafo tipo*/
        if (typeGraph == null){
             System.out.println("\tErro: esta gramática não possui grafo tipo.");
        }
        else{
            System.out.println("\tGrafo tipo: ");
            
        }
        
        /*Grafo host*/
         if (host == null){
             System.out.println("\tEsta gramática não possui grafo host.");
        }
        else{
            System.out.println("\tGrafo host: ");
            this.host.printGraph();
        }
        
        /*Regras*/
        if (rules == null)
            System.out.println("\tEsta gramática não possui regras.");
        else{
            System.out.println("\tImprimindo as regras:");
            for (Rule r: rules){
                if(!r.printRule())
                    return false;
            }
        }
        return true;
    }
}
