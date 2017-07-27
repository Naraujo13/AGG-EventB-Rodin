/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Classe que representa um contexto na notação EventB.
 * @author Nícolas Oreques de Araujo
 */
public class Context {

    private String name;
    private HashSet <Context> extend;
    private HashSet <Set> sets;
    private HashSet <Constant> constants;
    private HashMap <String, Axiom> axioms;
  
    /* -- Auxiliares -- */
    private int axiomLabelCount;
    
    /**
     * Função que cria um contexto.
     * @param name - nome do contexto a ser criado.
     */
    public Context(String name){
        this.name = name;
        axiomLabelCount = 0;
        extend = new HashSet<>();
        sets = new HashSet <> ();
        constants = new HashSet <> ();
        axioms = new HashMap <> ();
    }

    public String getName() {
        return name;
    }

    public HashSet<Context> getExtend() {
        return extend;
    }

    public HashSet<Set> getSets() {
        return sets;
    }

    public HashSet<Constant> getConstants() {
        return constants;
    }

    public HashMap<String, Axiom> getAxioms() {
        return axioms;
    }

    /**
     * Função que adiciona um set ao arraylist de sets.
     * @param set - set a ser adicionado
     */
    public void addSet (Set set){
        sets.add(set);
    }
    
      /**
     * Função que adiciona um constant ao arraylist de constantes.
     * @param constant - constant a ser adicionado
     */
    public void addConstant (Constant constant){
        constants.add(constant);
    }
    
      /**
     * Função que adiciona um axioma ao arraylist de axiomas.
     * @param axiom - axioma a ser adicionado
     */
    public void addAxiom (Axiom axiom){
        if (axioms.containsKey(axiom.getLabel()))
            axioms.put(axiom.getLabel() + Integer.toString(axiomLabelCount), axiom);
        else
            axioms.put(axiom.getLabel(), axiom);
        axiomLabelCount++;
    }

    /**
     * Função de acesso ao contador usado para criação das labels dos axiomas.
     * @return retorna inteiro informando quantos axiomas foram criados.
     */
    public int getAxiomLabelCount(){
        return axiomLabelCount;
    }

    public boolean logContext(String path){

        FileWriter copy = null;
        try{

            //Open file
            final FileWriter contextFile = new FileWriter( path + "/" + name + "_Context.txt");
            copy = contextFile;
            //Log
            System.out.println("\t\tTraduzindo Contexto " + name + "...");


            /* -- Start -- */
            contextFile.write("context ");
            contextFile.write(name);

            /* -- Extends -- */
            System.out.println("\t\tTraduzindo Extends " + name + "...");
            if (!extend.isEmpty()){
                contextFile.write(" extends");
                extend.forEach(c->{
                    try {
                        contextFile.write(" " + c.getName());
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }
            contextFile.write("\n\n\n");


            /* -- Sets -- */
            //Log
            System.out.println("\t\t\tTraduzindo sets... ");

            contextFile.write("sets");

            //Writes each set name
            sets.forEach(s->{
                try {
                    contextFile.write("\n  "  + s.getName());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });
            contextFile.write("\n\n");


            /* -- Constats -- */
            //Log
            System.out.println("\t\t\tTraduzindo constantes...");

            contextFile.write("constants");

            //Writes each constant name
            constants.forEach(c->{
                try {
                    contextFile.write("\n  " + c.getName());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });
            contextFile.write("\n\n");


            /* -- Axioms -- */
            //Log
            System.out.println("\t\t\tTraduzindo Axiomas...");

            contextFile.write("axioms\n");

            //Writes each Axiom label and predicate
            axioms.forEach((l, p)->{
                try {
                    contextFile.write("  @"  + l + " " + p.getPredicate() + "\n\n");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });
            contextFile.write("\n");


            /* -- End -- */
            contextFile.write("end\n");
            //Log
            System.out.println("\t\t--- FIM ---");

        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                if (copy != null)
                    copy.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return true;
    }
}

