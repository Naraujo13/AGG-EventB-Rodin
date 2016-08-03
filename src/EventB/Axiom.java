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
public class Axiom {
    String name;
    String predicate;
    
    /**
     * Cria axioma com nome e predicado indicados
     * @param name - nome do axioma a ser criado.
     * @param predicate - predicado do axioma a ser criado.
     */
    public Axiom(String name, String predicate){
        this.name = name;
        this.predicate = predicate;
    }
    
    /**
     * Método de acesso ao nome do axioma
     * @return retorna o nome do axioma
     */
    public String getName(){
        return name;
    }
    
}
