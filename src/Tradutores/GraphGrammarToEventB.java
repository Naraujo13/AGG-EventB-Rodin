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

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class GraphGrammarToEventB {
    
    public void translator(Grammar g){
       
        //Cria contexto
       Context context = new Context (g.getName() + "ctx");
       
       /* --- Grafo Tipo --- */
       //Cria Sets para representar o conjunto de vértices e conjunto de arestas do grafo tipo
       Set nodeSet, edgeSet;
       nodeSet = new Set("vertT");
       edgeSet = new Set("edgeT");
       context.addSet(nodeSet);
       context.addSet(edgeSet);
       
       /** --- Regras --- */
       //LHS
       nodeSet = new Set ("vertL1");
       context.addSet(nodeSet);
       edgeSet = new Set("edgeL1");
       context.addSet(edgeSet);
       //RHS
       nodeSet = new Set ("vertR1"); 
       context.addSet(nodeSet); 
       edgeSet = new Set("edgeT");
       context.addSet(edgeSet);
       
       /** -- Tipos definidos -- **/
       //Lê tipos no grafo tipo e define uma constante para cada
       
       //Cria constantes que representam funções source e target
       
       //Define axiomas das funções source e target
       
       //Define os axiomas que definem os tipos (Sets) ???
       
       //Define
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
}
