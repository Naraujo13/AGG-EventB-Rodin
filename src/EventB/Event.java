/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Event {
    
    String name;
    ArrayList<String> parameters; 
    HashMap<String,String> acts;
    HashMap<String,String> guards;
    
    
    public Event(String name){
        this.name = name;
        parameters = new ArrayList<>();
        acts = new HashMap<>();
        guards = new HashMap<>();
    }
    
    /**
     * Método de adição de ações ao evento.
     * @param name - label da ação a ser adicionada
     * @param predicate - predicado da açaõ a ser adicinada
     */
    public void addAct(String name, String predicate){
        acts.put(name, predicate);
    }
    
    /**
     * Método para adição de parâmetros ao evento.
     * @param parameter - parâmetro a ser adicionado.
     */
    public void addParameter(String parameter){
        parameters.add(parameter);
    }
    
    /**
     * Método para adição de guardas ao evento
     * @param name - label da guarda
     * @param predicate - predicado da guarda
     */
    public void addGuard(String name, String predicate){
        guards.put(name, predicate);
    }
    
}
