package GraphGrammar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class AttributeType {
    private String type;
    
    public AttributeType(String type){
        this.type = type;
    }
    
 public String getID(){return "";}
 public String getValue(){return "";}
 public String getName(){return "";}
    /**
     * Método de acesso ao tipo do atributo.
     * @return - tipo do atributo em forma de string.
     */
    public String getType() {
        return type;
    }
}
