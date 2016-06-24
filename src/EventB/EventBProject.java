/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class EventBProject {
    String name;
    List <Context> contexts;
    List <Machine> machines;
    
    /* --- Auxiliares --- */
   
    
    public void EventBProject(String name){
        this.name = name;
        contexts = new ArrayList<>();
        machines = new ArrayList<>();
    }
}
