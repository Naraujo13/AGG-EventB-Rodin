/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um contexto na notação EventB.
 * @author Nícolas Oreques de Araujo
 */
public class Context {
    String name;
    
    List <Context> extend;
    List <Set> sets;
    List <Constant> constants;
    List <Axiom> axioms;
    
    /**
     * Função que cria um contexto.
     * @param name - nome do contexto a ser criado.
     */
    public Context(String name){
        this.name = name;
        sets = new ArrayList <> ();
        constants = new ArrayList <> ();
        axioms = new ArrayList <> ();
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
        axioms.add(axiom);
    }
}

