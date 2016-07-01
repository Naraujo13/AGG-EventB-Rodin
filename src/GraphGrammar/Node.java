package GraphGrammar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Nícolas Oreques de Araujo
 * Esta classe representa uma instancia de nodo no grafo host.
 * Ela herda da classe NodeType (nodos do grafo tipo) e difere da mesma pois
 * armazena o ID do nodo.
 */
public class Node extends NodeType{
    
    String ID;      //ID que identifica esta instância de nodo

    /*Construtor que recebe como parâmetro o tipo do nodo e sua identificação única*/
    public Node(String type, String ID){
        super(type);
        this.ID = ID;
    }

    /**
     * Método que insere atributo na lista do nodo
     * @param type - tipo do atributo a ser inserido
     * @param name - nome do atributo a ser inserido
     * @param ID - id do atributo a ser inserido
     * @param value - valor do atributo a ser inserido
     */
    public void insertAttribute(String type, String name, String ID, String value){
        Attribute newAtt = new Attribute(type, ID, name, value);
        attributes.add(newAtt);
    }
    
    /**
     * Método que imprime as informações do nodo corrente.
     * @return - retorna true se conseguiu imprimir as informações corretamete, false caso tenha encontrado algum erro.
     */
    public boolean printNode(){
        if (this.type == null){
            System.out.println("\t\t\tErro. O nodo atual não possui tipo.");
            return false;
        }
        System.out.println("\t\t\tType: " + this.type);
        
        if (this.ID == null){
            System.out.println("\t\t\tErro. O nodo atual não possui ID.");
            return false;
        }
        System.out.println("\t\t\tID: " + this.ID);
        
        if (this.attributes.isEmpty())
            System.out.println("\t\t\tO nodo atual não possui atributos.");
        else{
            System.out.println("\t\t\tAtributos:");
            int i=0;
            for (AttributeType att: this.attributes){
                i++;
                System.out.println("\t\t\t\tAtributo " + i + ":");
                System.out.println("\t\t\t\t\tID: " + att.getID());
                System.out.println("\t\t\t\t\tTipo: " + att.getType());
                System.out.println("\t\t\t\t\tValor: " + att.getValue());
            }
        }        
        return true;
    }
    
}

