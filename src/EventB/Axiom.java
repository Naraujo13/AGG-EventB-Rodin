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
    private String label;
    private String predicate;
    
    /**
     * Cria axioma com nome e predicado indicados
     * @param label - nome do axioma a ser criado.
     * @param predicate - predicado do axioma a ser criado.
     */
    public Axiom(String label, String predicate){
        this.label = label;
        this.predicate = predicate;
    }
    
    /**
     * Método de acesso ao label do axioma
     * @return retorna o label do axioma
     */
    public String getLabel(){
        return label;
    }

    public String getPredicate() {
        return predicate;
    }
}
