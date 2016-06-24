package AGG;

/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Attribute extends AttributeType {
    
    String name;
    String ID;
    String value;

    public Attribute(String type, String ID, String name, String value){
        super(type);
        this.name = name;
        this.ID = ID;
        this.value = value;
        
        
    } 
}
