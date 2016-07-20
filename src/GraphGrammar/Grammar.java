package GraphGrammar;


import Tradutores.AGGToGraphGrammar;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
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
   HashSet <Rule> rules;   //Arraylist com as regras da gramática
   Graph host;          //Grafo host
   TypeGraph typeGraph;     //Grafo tipo
       
    public Grammar(String name){
        
        this.name = name;
        typeGraph = new TypeGraph();
        rules = new HashSet <>();
    }
     
    /**
     * Método de acesso ao grafo tipo
     * @return - grafo tipo.
     */
    public TypeGraph getTypeGraph(){
        return typeGraph;
    }
    
    /**
     * Método de acesso ao conjunto de regras
     * @return - retorna o hashset contendo o conjunto de regras
     */
    public HashSet<Rule> getRules(){
        return rules;
    }
    
     /**
     * Método de acesso ao grafo host
     * @return - grafo host.
     */
    public Graph getHost(){
        return host;
    }
    
    /**
     * Método de alteração do grafo host
     * @param host - grafo a ser inserido como grafo host da gramática
     */
    public void setHost(Graph host){
        this.host = host;
    }
    
   
    /**
     * Método de acesso ao nome da gramática
     * @return retorna string contendo o nome da gramática
     */
    public String getName(){
        return name;
    }
    
    /**
     * Adiciona uma regra a gramática
     * @param r - regra a ser adicionada
     */
    public void addRule(Rule r){
        rules.add(r);
    }
 
    public boolean printGrammar(){
        /* Testing */
        PrintStream realSystemOut = System.out; //salva systemOut original
        File file;
        file = new File("Grammar_" + this.name + ".log");
        try {           //Seta output para o arquivo
            PrintStream printStream = new PrintStream(new FileOutputStream(file));
            System.setOut(printStream);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AGGToGraphGrammar.class.getName()).log(Level.SEVERE, null, ex);
        }
        /* End of Testing */
        
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
                if(!r.printRule()){
                    System.setOut(realSystemOut);
                    return false;
                }
            }
        }
        
        System.setOut(realSystemOut);            
        return true;
    }
}
