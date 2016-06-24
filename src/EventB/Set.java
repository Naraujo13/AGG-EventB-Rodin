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
public class Set {
    String name;
    String ID;
    
    public Set (String name, String ID){
        this.name = name;
        this.ID = ID;
    }
    
    /**
     * Função de acesso ao nome do set
     * @return retorna nome do set
     */
    public String getName(){
        return name;
    }
    
    
}
