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
    private String ID;
    private String name;
    private String type;
    
    public AttributeType(String ID, String name, String type){
        this.ID = ID;
        this.name = name;
        this.type = type;
    }
    
 public String getID(){return ID;}
 public String getValue(){return "";}
 public String getName(){return name;}
    /**
     * Método de acesso ao tipo do atributo.
     * @return - tipo do atributo em forma de string.
     */
    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }
}
