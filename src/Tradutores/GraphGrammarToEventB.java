/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tradutores;
import GraphGrammar.EdgeType;
import GraphGrammar.NodeType;
import EventB.*;
import GraphGrammar.Edge;
import GraphGrammar.Grammar;
import GraphGrammar.Graph;
import GraphGrammar.Node;
import GraphGrammar.Rule;
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
        
       if(!rulesTranslation(context, g))
           return false;
       
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
       
       p.addContext(context);
       return true;
       
    }
    
    /**
     * Função que realiza a tradução de um conjunto de regras de uma gramática
     * de grafos para a notação EventB
     * @param context - contexto ao qual as regras devem ser inseridas
     * @param g - gramática cujas regras serão traduzidas (fonte)
     * @return - retorna true ou false, indicando sucesso ou falha na tradução
     */
    public boolean rulesTranslation(Context context, Grammar g){
        
        
        //For para todas as regras
        
        /* ----------------------------------------------------------------*
         *  A tradução de cada regra foi dividida em duas etapas:        --*
         * (1) tradução do LHS da regra;                                 --*
         * (2) tradução dos NACs da regra.                               --*
         * ----------------------------------------------------------------*/
         for (Rule r: g.getRules()){
            /* -------------------*
             * -- Passo 1: LHS ---* 
             * -------------------*/
             
            //Define 2 sets para representar conjunto de vértices e de arestas
            Set nodesL, edgesL;
            nodesL = new Set("vertL" + r.getName());
            edgesL = new Set("edgeL" + r.getName());
            context.addSet(nodesL);
            context.addSet(edgesL);
                     
            Constant nodeConstant, edgeConstant;
            //Define uma constante para cada nodo no LHS da regra
            for (Node n: r.getLHS().getNodes()){
                nodeConstant = new Constant(r.getName() + "L" + n.getType());
                context.addConstant(nodeConstant);
                
            }
            
            //Define uma constante para cada aresta no LHS da regra
            for (Edge e: r.getLHS().getEdges()){
                edgeConstant = new Constant(r.getName() + "L" + e.getType());
                context.addConstant(edgeConstant);
            }
            
            //Define constantes para funções source e target
            Constant sourceL, targetL;
            sourceL = new Constant("sourceL" + r.getName());
            targetL = new Constant("targetL" + r.getName());
            context.addConstant(sourceL);
            context.addConstant(targetL);
            
            //Define duas constantes para represnetar a tipagem de arestas e nodos
            //no LHS.
            Constant tLv, tLe;
            tLv = new Constant("t" + r.getName() + "v");
            tLe = new Constant("t" + r.getName() + "e");
            context.addConstant(tLv);
            context.addConstant(tLe);
            
            String name, predicate;
            //Define axiomas para conjuntos de vértices e de arestas
            Axiom axmVertL, axmEdgeL;
            
            name = "axm_VertL" + r.getName();
            predicate = "partition(" + nodesL.getName();
            for (Node n: r.getLHS().getNodes()){
                predicate = predicate + ", {" + n.getType() + "}";
            }            
            predicate = predicate + ")";
            axmVertL = new Axiom(name, predicate);
            
            name = "axm_EdgeL" + r.getName();
            predicate = "partition(" + edgesL.getName();
            for (Edge e: r.getLHS().getEdges()){
                predicate = predicate + ", {" + e.getType() + "}";
            }            
            predicate = predicate + ")";
            axmEdgeL = new Axiom(name, predicate);
            
            context.addAxiom(axmVertL);
            context.addAxiom(axmEdgeL);
            
            //Define axiomas para representar tipagem de funções source e target
            Axiom axmSrcType, axmTgtType;
            
            name = "axm_srcL" + r.getName() + "type";
            predicate = "sourceL" + r.getName() + " : edgeL" + r.getName() + " --> " + "vertL" + r.getName();
            axmSrcType = new Axiom (name, predicate);
            
            name = "axm_tgtL" + r.getName() + "type";
            predicate = "targetL" + r.getName() + " : edgeL" + r.getName() + " --> " + "vertL" + r.getName();
            axmTgtType = new Axiom (name, predicate);
            
            context.addAxiom(axmSrcType);
            context.addAxiom(axmTgtType);

            //Define axiomas que representam a definição das funções source e target
            Axiom axmSrcDef, axmTgtDef;
            //source
            name = "axm_srcL" + r.getName() + "def";
            predicate = "partition(sourceL" + r.getName();
            //Itera para cada tipo de aresta
            for (Edge e: r.getLHS().getEdges()){
                    predicate = predicate + ", {" + e.getType() + "|->" + e.getSource() +"}";
            }
            predicate = predicate + ")";
            axmSrcDef = new Axiom(name, predicate);
            context.addAxiom(axmSrcDef);
            
            //target
            name = "axm_tgtL" + r.getName() + "def";
            predicate = "partition(targetL" + r.getName();
            //Itera para cada tipo de aresta
            for (Edge e: r.getLHS().getEdges()){
                    predicate = predicate + ", {" + e.getType() + "|->" + e.getTarget() +"}";
            }
            predicate = predicate + ")";
            axmTgtDef = new Axiom(name, predicate);
            context.addAxiom(axmTgtDef);
            
             String predicateAux;         
            //Define axiomas que representam a tipagem dos vértices e arestas
            Axiom tLvert, tLedg;
            
            name = "axm_tL" + r.getName() + "_V";
            predicate = "tL" + r.getName() + " : " + nodesL.getName() + " --> vertT";
            tLvert = new Axiom(name, predicate);
            
            name = "axm_tL" + r.getName() + "_E";
            predicate = "tL" + r.getName() + " : " + edgesL.getName() + " --> edgeT";
            tLedg = new Axiom(name, predicate);
            
            context.addAxiom(tLvert);
            context.addAxiom(tLedg);
            
            //Define axiomas para definição das funções de tipagem
            Axiom tLvDef, tLeDef;
            
            //Nodos
            name = "axm_tL" + r.getName() + "_V_def";
            predicate = "partition(tL" + r.getName() + "_V";
            
            for(Node n: r.getLHS().getNodes()){  
                 predicate = predicate + ", {" + n.getID() + " |-> " + n.getType() + "}";
            }
            predicate = predicate + ")";
            tLvDef = new Axiom(name, predicate);
            
            //Arestas
            name = "axm_tL" + r.getName() + "_E_def";
            predicate = "partition(tL" + r.getName() + "_E";
            
            for(Edge e: r.getLHS().getEdges()){  
                 predicate = predicate + ", {" + e.getID() + " |-> " + e.getType() + "}";
            }
            predicate = predicate + ")";
            tLeDef = new Axiom(name, predicate);
            
            context.addAxiom(tLvDef);
            context.addAxiom(tLeDef);
            
            if (!NACTranslation(context, g, r))
                return false;
        }
        return true;
    }
    
    /**
     * Método que realiza a tradução das NACs de uma regra
     * @param c - contexto ao qual serão inseridas NACs
     * @param g - gramática fonte
     * @param r - regra da qual serão traduzidas as NACs
     * @return - retorna true ou false, indicando sucesso ou falha do método
     */
    public boolean NACTranslation(Context c, Grammar g, Rule r){
        int count = 0;
        Set nacVert, nacEdge;
        Constant source, target, nodeConstant, edgeConstant;
        Constant tLVNAC, tLENAC, lV, lE; //Morfismos
        Axiom axmlV, axmlVDef, axmlE, axmlEDef;
        String name, predicate;
        for (Graph nac: r.getNACs()){
           
            //Cria dois sets, para arestas e vértices desta nac
            nacVert = new Set("Vert" + r.getName() + "NAC" + Integer.toString(count));
            nacEdge = new Set("Edge" + r.getName() + "NAC" + Integer.toString(count));
            c.addSet(nacVert);
            c.addSet(nacEdge);
            
            //Cria duas constantes, para representar source e target desta NAC
            source= new Constant("source"+ r.getName() +"NAC" + Integer.toString(count));
            target= new Constant("target"+ r.getName() +"NAC" + Integer.toString(count));
            c.addConstant(source);
            c.addConstant(target);
            
            //Cria uma constante para cada tipo de vértice na NAC
            for (Node n: nac.getNodes()){
                nodeConstant = new Constant(n.getID());
                c.addConstant(nodeConstant);
            }
            
            //Cria uma constante para cada tipo de aresta
            for (Edge e: nac.getEdges()){
                edgeConstant = new Constant(e.getID());
                c.addConstant(edgeConstant);
            }
            
            //Cria duas constantes para representar o morfismo de vértices e arestas da NAC
            tLVNAC = new Constant("t"+ r.getName() +"VNAC" + Integer.toString(count));
            tLENAC = new Constant("t"+ r.getName() +"ENAC" + Integer.toString(count));
            c.addConstant(tLVNAC);
            c.addConstant(tLENAC);
            
            //Cria morfismo de vértices e arestas em relação ao LHS
            lV = new Constant(r.getName() + "NAC" + Integer.toString(count) + "V");
            lE = new Constant(r.getName() + "NAC" + Integer.toString(count) + "E");
            c.addConstant(lV);
            c.addConstant(lE);
            
          //Axiomas para inicialização de ljV e ljE
            
            //Axiomas do morfismo de vértices da NAC com LHS
            
            name = "axm_"+ r.getName() + Integer.toString(count) + "V";
            predicate = r.getName() + Integer.toString(count) + "V : Vert"+ r.getName() +" --> Vert"+ r.getName() +"Nac" + Integer.toString(count);
            axmlV = new Axiom(name, predicate);
            
            name = "axm_"+ r.getName() + Integer.toString(count) + "V_def";
            predicate = "partition(" + r.getName() + Integer.toString(count) + "V";
            for(Node n: nac.getNodes()){  
                predicate = predicate + ", {" + n.getID() + " |-> " + n.getType() + "}";
            }
            predicate = predicate + ")";
            axmlVDef = new Axiom(name, predicate);
            
            c.addAxiom(axmlV);
            c.addAxiom(axmlVDef);
            
            //Axiomas do morfismo de arestas da NAC com LHS
            
            name = "axm_"+ r.getName() + Integer.toString(count) + "E";
            predicate = r.getName() + Integer.toString(count) + "E : Edge"+ r.getName() +" --> Edge"+ r.getName() +"Nac" + Integer.toString(count);
            axmlE = new Axiom(name, predicate);
            
            name = "axm_"+ r.getName() + Integer.toString(count) + "E_def";
            predicate = "partition(" + r.getName() + Integer.toString(count) + "E";
            for(Edge e: nac.getEdges()){  
                predicate = predicate + ", {" + e.getID() + " |-> " + e.getType() + "}";
            }
            predicate = predicate + ")";
            axmlEDef = new Axiom(name, predicate);
            
            c.addAxiom(axmlE);
            c.addAxiom(axmlEDef);
        
            count++;
        }   
        
        
        
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
