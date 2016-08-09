/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import java.util.HashSet;

/**
 *  Classe que representa um projeto em EventB, contendo dois hashsets representando conjuntos de contextos e máquinas, além de uma
 *  string representando seu nome.
 * @author Nícolas Oreques de Araujo
 */
public class Project {
    String name;
    HashSet <Context> contexts;
    HashSet<Machine> machines;
    
    public Project(String name){
        this.name = name;
        contexts = new HashSet<>();
        machines = new HashSet<>();
    }
    
    /**
     * Método para adição de contextos a um projeto eventB
     * @param c - contexto a ser adicionado
     */
    public void addContext(Context c){
        contexts.add(c);
    }
    
    /**
     * Método para adição de máquinas a um projeto eventB
     * @param m - máquina a ser adicionada
     */
    public void addMachine(Machine m){
        machines.add(m);
    }
}
