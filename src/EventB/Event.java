/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import java.util.HashMap;

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Event {
    
    String name;
    HashMap<String,String> acts;
    
    public Event(String name){
        this.name = name;
        acts = new HashMap<>();
    }
    
    public void addAct(String name, String predicate){
        acts.put(name, predicate);
    }
    
}
