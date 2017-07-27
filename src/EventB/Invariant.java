/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Invariant {
    private String label;
    private String predicate;
    
    public Invariant(String label, String predicate){
        this.label = label;
        this.predicate = predicate;
    }
    
    public String getLabel(){
        return label;
    }

    public String getPredicate() {
        return predicate;
    }
}
