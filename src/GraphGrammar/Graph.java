package GraphGrammar;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Graph {
    
    String type;
    List <Edge> graphEdges;
    List <Node> graphNodes;
    Map <String, String> morphism;
    
    /**
     * Construtor da classe grafo. Cria novo grafo baseado no tipo passado como
     * parâmetro.
     * @param type - tipo do grafo a ser criado 
     */
    public Graph (String type){
        this.type = type;
        graphEdges = new ArrayList <> ();
        graphNodes = new ArrayList <> ();
        morphism = new HashMap <> ();
    }
    
    /**
     * Função que adiciona novo nodo ao grafo atual.
     * @param newNode - nodo a ser adicionado ao arraylist
     */
    public void addNode(Node newNode){
        graphNodes.add(newNode);
    }
    
    /**
     * Função que adiciona nova aresta ao arraylist de arestas do grafo
     * @param newEdge aresta ser adicionada ao arraylist
     */
    public void addEdge(Edge newEdge){
        graphEdges.add(newEdge);
    }
    
    /**
     * Função que retorna o morfismo do grafo.
     * @return retorna hashmap com o morfismo do grafo (ID->ID)
     */
    public Map<String, String> getMorphism(){
        return morphism;
    }

    /**
     * Função que para um dado grafo, define seus nodos.
     * Faz a leitura do arquivo atual em scanne utilizando a variável tokenAtual,
     * extraindo as informações dos nodos do grafo e os inserindo no grafo atual.
     * @param tokenAtual - tokenAtualmente sendo analisado no arquivo
     * @param entrada - scanner do arquivo sendo lido no momento
     * @param attNames - hashmap com o nome dos atributos de um dado nodo
     * @param attTypes - hashmap com os tipos dos atributos de um dado nodo
    */
    public void defineGraphNodes(String tokenAtual,Scanner entrada, Map <String,String> attNames, Map <String, String> attTypes){
         //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
        String[] auxiliar3; //Auxiliar 3 para quebrar substrings
        
        Node newNode;
        String ID, nodeType, attributeID, atributeValue, atributeType;
        Attribute newAtt;
        //enquanto for nodo...
        while(tokenAtual.contains("Node") || tokenAtual.contains("Layout")){
                
            //caso não seja layout, atributo ou fechando seção, define o nodo
            if (!tokenAtual.contains("Layout") && !tokenAtual.contains("/Node") && !tokenAtual.contains("Attribute")){
                auxiliar = tokenAtual.split(" ");

                //ID do nodo
                auxiliar2 = auxiliar[1].split("\"");
                ID = auxiliar2[1];
 
                //Tipo do nodo
                auxiliar2 = auxiliar[2].split("\"");
                nodeType = auxiliar2[1];

                //Instancia novo nodo e insere no grafo
                newNode = new Node (nodeType, ID);
                this.addNode(newNode);
                //Itera para a próxima linha e testa se é atributo, caso seja define para este nodo
                tokenAtual = entrada.next();
                //Enquanto for um atributo, define o mesmo e adiciona ao arraylist do tipo do atributo na classe nodo de newNode
                while (tokenAtual.contains("Attribute")){
                    
                    //Pega ID do atributo
                    auxiliar = tokenAtual.split(" ");
                    if (auxiliar[1].contains("type="))
                        auxiliar2 = auxiliar[1].split("\"");
                    else if (auxiliar[2].contains("type="))
                        auxiliar2 = auxiliar[2].split("\"");
                    attributeID = auxiliar2[1];
                    
                    tokenAtual = entrada.next(); // Descarta "abertura" do atributo
                    tokenAtual = entrada.next(); // Descarta Value
                    
                    //Verifica tipo do argumento
                    
                    //Teste
                    atributeType = attTypes.get(attributeID);
                    auxiliar3 = tokenAtual.split(">");
                    atributeValue = auxiliar3[1];
                    newAtt = new Attribute(atributeType, attributeID, attNames.get(attributeID),atributeValue);
                    newNode.attributes.add(newAtt);
                    
                    while (!tokenAtual.contains("/Attribute")){
                    tokenAtual = entrada.next();
                    }
                    tokenAtual = entrada.next();
                    
                }
                    
            }
            else
                tokenAtual = entrada.next(); //itera para próxima linha
        }
         
        /*
        //Descarta opções de Layout
            while (!tokenAtual.contains("/Node")){
                tokenAtual = entrada.next(); 
            }
           
            //Descarta /Node
            tokenAtual = entrada.next();
         */
    }
    
     /**
     * Função que para o grafo atual faz a leitura do arquivo
     * salvo no scanner atual utilizando a variável tokenAtual, extraindo as 
     * informações das arestas do grafo e as inserindo no grafo passado como
     * parêmtro
     * @param tokenAtual - tokenAtualmente sendo analisado no arquivo
     * @param entrada - scanner do arquivo sendo lido no momento
    */
    public void defineGraphEdges(String tokenAtual,Scanner entrada){
         //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
        
         Edge newEdge;
        String edgeType, ID, source, target;
        //Enquanto for aresta...
        while (tokenAtual.contains("Edge") || tokenAtual.contains("Layout")){
            //Se não for layout ou fechamento de seção
            if (!tokenAtual.contains("Layout") && !tokenAtual.contains("/Edge")){
                auxiliar = tokenAtual.split(" ");

                //ID da aresta
                auxiliar2 = auxiliar[1].split("\"");
                ID = auxiliar2[1];

                //source
                auxiliar2 = auxiliar[2].split("\"");
                source = auxiliar2[1];

                //target
                auxiliar2 = auxiliar[3].split("\"");
                target = auxiliar2[1];

                //type
                auxiliar2 = auxiliar[4].split("\"");
                edgeType = auxiliar2[1];

                //Instancia nova aresta e insere no grafo
                newEdge = new Edge (edgeType, ID, source, target);
                this.addEdge(newEdge);
            }
            tokenAtual = entrada.next(); //itera para próxima linh
        }
    }
    
    /**
     * Função que define o morfismo de um grafo, sendo este um hash map. 
     * Em tal hashmap temos:
     * chave -> imagem
     * valor -> origem
     * @param tokenAtual - tokenAtualmente sendo analisado no arquivo
     * @param entrada - scanner do arquivo sendo lido no momento
     */
    public void defineMorphism (String tokenAtual,Scanner entrada){
        
         //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
        String[] auxiliar3; //Auxiliar 3 para quebrar substrings
        
        //Inserir Morfismo
        if (tokenAtual.contains("Morphism")){
            tokenAtual = entrada.next();
            while (tokenAtual.contains("Mapping")){
                auxiliar = tokenAtual.split(" ");
                auxiliar2 = auxiliar[1].split("\"");
                auxiliar3 = auxiliar[2].split("\"");
                this.getMorphism().put(auxiliar2[1], auxiliar3[1]);
                tokenAtual = entrada.next();
            }
            //Itera /Morphism
            if (tokenAtual.contains("/Morphism"))
                tokenAtual = entrada.next();
        }
     
    }
    
      public boolean printGraph(){
        if (this.type == null){
            System.out.println("\t\tErro: o grafo atual não possui tipo.");
            return false;
        }
        else
            System.out.println("\t\tTipo: " + this.type);
        
        if (this.graphNodes.isEmpty())
            System.out.println("\t\tO grafo atual não possui nodos.");
        else{
            for (Node node: graphNodes){
                if (!node.printNode())
                    return false;
            }
            if (this.graphEdges.isEmpty())
                System.out.println("\t\tO grafo atual não possui arestas.");
            else{
                for (Edge edge: graphEdges){
                    if(!edge.printEdge())
                        return false;
                }                
            }
        }
        //Imprimir morfismo do grafo.
        return true;
    }
}
