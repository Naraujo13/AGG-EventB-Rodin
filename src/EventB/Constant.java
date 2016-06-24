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
public class Constant {
    
    String name;

    public Constant(String name){
        this.name = name;
    }
    
    /**
     * Função de acesso ao nome da constantes
     * @return - retorna string com nome da constante
     */
    public String getName(){
        return name;
    }
}
