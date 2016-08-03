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
        contexts = new HashSet<>();
    }
    
    /**
     * Método para adição de contextos a um projeto eventB
     * @param context 
     */
    public void addContext(Context context){
        contexts.add(context);
    }
}
