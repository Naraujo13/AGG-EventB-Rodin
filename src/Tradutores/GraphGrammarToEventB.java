/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tradutores;
import GraphGrammar.EdgeType;
import GraphGrammar.NodeType;
import EventB.*;
import GraphGrammar.Grammar;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class GraphGrammarToEventB {  
    
    public boolean translator(Project p, Grammar g){
       
        //Cria contexto
       Context context = new Context (g.getName() + "ctx");
       
       /* --- Grafo Tipo --- */
       //Cria Sets para representar o conjunto de vértices e conjunto de arestas do grafo tipo
       if(!typeGraphTranslation(context, g))
           return false;
       
       p.addContext(context);
       
       /** --- Regras --- *//*
       //LHS
       nodeSet = new Set ("vertL1");
       context.addSet(nodeSet);
       edgeSet = new Set("edgeL1");
       context.addSet(edgeSet);
       //RHS
       nodeSet = new Set ("vertR1"); 
       context.addSet(nodeSet); 
       edgeSet = new Set("edgeT");
       context.addSet(edgeSet);*/
       
       return true;
       
    }
    
    /**
     * Função que realiza a tradução do grafo tipo.
     * @param context - contexto ao qual serão inseridos os elementos eventB
     * @param g - gramática que está sendo traduzida 
     * @return - retorna true ou false indicando se a operação foi bem sucedida ou não
     */
    public boolean typeGraphTranslation(Context context, Grammar g){
       /* -- Declarações -- */
       //Sets
       Set nodeSet, edgeSet;
       //Constants
       Constant sourceT, targetT;
       
       /* -- Instanciações -- */
       //Sets
       nodeSet = new Set("vertT");
       edgeSet = new Set("edgeT");
       //Constants
       sourceT = new Constant("sourceT");
       targetT = new Constant("targetT");
       
       /* -- Adições ao contexto fornecido como parâmetro -- */
       //Sets
       context.addSet(nodeSet);
       context.addSet(edgeSet);
       //Constants
       context.addConstant(sourceT);
       context.addConstant(targetT);
       
       /* -- Traduz os tipos de nodos e arestas definidos no grafo tipo -- */
       List vertT, edgeT;
       vertT = new ArrayList <> ();
       edgeT = new ArrayList <> ();
       
       /* Define constantes para representar tipos de nodos e arestas definidos no grafo tipo */
         Constant auxConstant;
        //Cria uma constante para cada tipo de nodo e adiciona estas constantes ao contexto
        for (NodeType nodeType: g.getTypeGraph().getAllowedNodes()){
           auxConstant = new Constant(nodeType.getType());
           context.addConstant(auxConstant);
       }
       //Cria uma constante para cada tipo de aresta e adiciona estas constantes ao contexto
         for (EdgeType edgeType: g.getTypeGraph().getAllowedEdges()){
           auxConstant = new Constant(edgeType.getType());
           context.addConstant(auxConstant);
       }
      
       /* -- Axioms --*/
       String name, predicate;
       Axiom sourceTypeAxm, sourceDefAxm, targetTypeAxm, targetDefAxm, vertTAxm, edgeTAxm;
       
       //Define axiomas que representam tipagem das funções source e target
       name = "axm_srcTtype";
       predicate = "sourceT : EdgeT --> VertT";
       sourceTypeAxm = new Axiom(name, predicate);
       name = "axm_tgtTtype";
       predicate = "targetT : EdgeT --> vertT";
       targetTypeAxm = new Axiom(name, predicate);
       context.addAxiom(sourceTypeAxm);
       context.addAxiom(targetTypeAxm);
       
       //Define axiomas para representar os tipos de vertT e edgeT
       //vertT
       name = "axm_vertT";
       predicate = "partition(vertT,";
       
       for (NodeType nodeType: g.getTypeGraph().getAllowedNodes()){
           predicate = predicate + ", {" + nodeType.getType() + "}";
       }
       predicate = predicate + ")";
       
       vertTAxm = new Axiom (name, predicate);
       context.addAxiom(vertTAxm);
       
       //edgeT
       name = "axm_edgeT";
       predicate = "partition(edgeT,";
       
       for (EdgeType edgeType: g.getTypeGraph().getAllowedEdges()){
           predicate = predicate + ", {" + edgeType.getType() + "}";
       }
       predicate = predicate + ")";
       
       edgeTAxm = new Axiom (name, predicate);
       context.addAxiom(edgeTAxm);

        //Define axiomas para representar funções source e target
        String predicateAux;
        //source
        name = "axm_srcTdef";
        predicate = "partition(sourceT";
        //Itera para cada tipo de aresta
        for (EdgeType edgeType: g.getTypeGraph().getAllowedEdges()){
            predicateAux = "";
            //Itera para cada tipo de nodo possível daquela aresta, concatenando o mapeamento de cada uma
            for (String source: edgeType.getSource()){
                predicateAux = ", {" + edgeType.getType() + "|->" + source +"}";
            }
            predicate = predicate + predicateAux;
        }
        predicate = predicate + ")";
        sourceDefAxm = new Axiom(name, predicate);
        context.addAxiom(sourceDefAxm);
        
        //target
        name = "axm_tgtTdef";
        predicate = "partition(targetT";
        //Itera para cada tipo de aresta
        for (EdgeType edgeType: g.getTypeGraph().getAllowedEdges()){
            predicateAux = "";
            //Itera para cada tipo de nodo possível daquela aresta, concatenando o mapeamento de cada uma
            for (String target: edgeType.getTarget()){
                predicateAux = ", {" + edgeType.getType() + "|->" + target +"}";
            }
            predicate = predicate + predicateAux;
        }
        predicate = predicate + ")";
        targetDefAxm = new Axiom(name, predicate);
        context.addAxiom(targetDefAxm);
        
       return true;
    }
    
    
       /**
     * Cria um set para reprensentar um tipo do grafo tipo e o adiciona ao
     * contexto. Cria constantes associadas ao set criado
     * @param nodeType - tipo de nodo que irá virar set
     * @param context - contexto ao qual será adicionado o set craido.
     */
    public void createNodeType(NodeType nodeType, Context context){
        Set newSet;
        newSet = new Set("vertT" + nodeType.getType());
        context.addSet(newSet);
        
        Constant newConstant1 = new Constant(newSet.getName() + "1");
        context.addConstant(newConstant1);
        Constant newConstant2 = new Constant(newSet.getName() + "2");
        context.addConstant(newConstant2);
       
        Axiom newAxiom = new Axiom("axm" + context.getAxiomLabelCount(), "partition("+ newSet.getName() + ",{" + newConstant1.getName() + "}, {" + newConstant2.getName() + "})");
        context.addAxiom(newAxiom);
    }
    
    /**
     * Cria um set para representar o tipo de aresta do grafo tipo e o adiciona
     * ao contexto
     * @param edgeType - tipo  de aresta que irá virar set
     * @param context - contexto ao qual será adiciona o set criado.
     */
    public void createEdgeType(EdgeType edgeType, Set sourceNode, Set targetNode, Context context){
        Set newSet;
        newSet = new Set ("edgeT" + edgeType.getType());
        context.addSet(newSet);
        
        Constant newConstant1 = new Constant(newSet.getName() + "1");
        context.addConstant(newConstant1);
        Constant newConstant2 = new Constant(newSet.getName() + "2");
        context.addConstant(newConstant2);
        
        /* -- Creates axiom to represent the domain of the new type -- */
        Axiom newAxiom = new Axiom("axm" + context.getAxiomLabelCount(), "partition("+ newSet.getName() + ",{" + newConstant1.getName() + "}, {" + newConstant2.getName() + "})");
        context.addAxiom(newAxiom);
        
        /* -- Creates new Constants and axioms representing the source and target
         * -- functions for the new edge type created. */
        Constant newConstant3 = new Constant("sourceT" + newSet.getName());
        context.addConstant(newConstant3);
        Constant newConstant4 = new Constant("targetT" + newSet.getName());
        context.addConstant(newConstant4);
        
        /* -- Creates source function -- */
        newAxiom = new Axiom("axm" + context.getAxiomLabelCount(), newConstant3.getName() + " : " + newSet.getName() + " --> " + sourceNode.getName());
        context.addAxiom(newAxiom);
        newAxiom = new Axiom("axm" + context.getAxiomLabelCount(),"partition(" + newConstant3.getName() + ", {" + newConstant1.getName() + " |-> ");    //corrigir
        
        
        /** -- Creates target function -- */
        newAxiom = new Axiom("axm" + context.getAxiomLabelCount(), newConstant4.getName() + " : " + newSet.getName() + " --> " + targetNode.getName());
        context.addAxiom(newAxiom);    
    }
    
    
    /** Main para testes de conversão de Agg para GG  e de GG para EventB
    * @param args the command line arguments
    */
    public static void main(String[] args) {
       /**-- Step1 - AGG to GG translation --*/
       String arquivo =  "PacmanAtributo.ggx";
       AGGToGraphGrammar agg = new AGGToGraphGrammar();
       Grammar test = new Grammar("PacmanAtributo");
       agg.aggReader(arquivo, test);
       test.printGrammar();
       
       /*-- Step 2 - GG to EventB translation --*/
       GraphGrammarToEventB eventB = new GraphGrammarToEventB();
       Project newProject = new Project ("PacmanAtributo");
       eventB.translator(newProject, test);       
       System.out.println("Finished!");
    }
}
