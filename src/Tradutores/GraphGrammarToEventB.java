/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tradutores;

import GraphGrammar.EdgeType;
import GraphGrammar.NodeType;
import EventB.*;
import GraphGrammar.AttributeType;
import GraphGrammar.Edge;
import GraphGrammar.Grammar;
import GraphGrammar.Graph;
import GraphGrammar.Node;
import GraphGrammar.Rule;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class GraphGrammarToEventB {
    
    HashMap<String, Node> forbiddenVertices = new HashMap<>();
    HashMap<String, Edge> forbiddenEdges = new HashMap<>();
    
    public boolean translator(Project p, Grammar g) {

        //Cria contexto
        Context c = new Context(g.getName() + "ctx");

        /*
         * --- Grafo Tipo ---
         */
        //Cria Sets para representar o conjunto de vértices e conjunto de arestas do grafo tipo
        if (!typeGraphTranslation(c, g)) {
            return false;
        }
        
        if (!rulePatternTranslation(c, g)) {
            return false;
        }
        
        Machine m = new Machine(g.getName() + "mch", c);
        
        if (!stateGraphTranslation(m, c, g)) {
            return false;
        }
        
        if (!DPOApplication(c, m, g)) {
            return false;
        }
        
        p.addContext(c);
        p.addMachine(m);
        return true;
        
    }

    //Revisado
    /**
     * DEFINITION 15
     * Função que realiza a tradução do grafo tipo.
     *
     * @param context - contexto ao qual serão inseridos os elementos eventB
     * @param g       - gramática que está sendo traduzida
     * @return - retorna true ou false indicando se a operação foi bem sucedida
     *         ou não
     */
    public boolean typeGraphTranslation(Context context, Grammar g) {

        /*
         * -- Instanciações e adições ao contexto --
         */
        //Sets
        context.addSet(new Set("vertT"));
        context.addSet(new Set("edgeT"));
        //Constants
        context.addConstant(new Constant("sourceT"));
        context.addConstant(new Constant("targetT"));

        /*
         * -- Traduz os tipos de nodos e arestas definidos no grafo tipo --
         */
         /*
         * Define constantes para representar tipos de nodos e arestas definidos
         * no grafo tipo
         */
        //Cria uma constante para cada tipo de nodo e adiciona estas constantes ao contexto
        for (NodeType nodeType : g.getTypeGraph().getAllowedNodes()) {
            context.addConstant(new Constant(nodeType.getType()));
        }
        //Cria uma constante para cada tipo de aresta e adiciona estas constantes ao contexto
        for (EdgeType edgeType : g.getTypeGraph().getAllowedEdges()) {
            context.addConstant(new Constant(edgeType.getType()));
        }

        /*
         * -- Axioms --
         */
        String name, predicate;

        //Define axiomas que representam tipagem das funções source e target
        name = "axm_srcTtype";
        predicate = "sourceT : edgeT --> vertT";
        context.addAxiom(new Axiom(name, predicate));
        name = "axm_tgtTtype";
        predicate = "targetT : edgeT --> vertT";
        context.addAxiom(new Axiom(name, predicate));

        //Define axiomas para representar os tipos de vertT e edgeT
        //vertT
        name = "axm_vertT";
        predicate = "partition(vertT";
        
        for (NodeType nodeType : g.getTypeGraph().getAllowedNodes()) {
            predicate = predicate + ", {" + nodeType.getType() + "}";
        }
        predicate = predicate + ")";
        context.addAxiom(new Axiom(name, predicate));

        //edgeT
        name = "axm_edgeT";
        predicate = "partition(edgeT";
        
        for (EdgeType edgeType : g.getTypeGraph().getAllowedEdges()) {
            predicate = predicate + ", {" + edgeType.getType() + "}";
        }
        predicate = predicate + ")";
        context.addAxiom(new Axiom(name, predicate));

        //Define axiomas para representar funções source e target
        //source
        name = "axm_srcTdef";
        predicate = "partition(sourceT";
        //Itera para cada tipo de aresta
        for (EdgeType edgeType : g.getTypeGraph().getAllowedEdges()) {
            //Itera para cada tipo de nodo possível daquela aresta, concatenando o mapeamento de cada uma
            for (String source : edgeType.getSource()) {
                predicate += ", {" + edgeType.getType() + "|->" + source + "}";
            }
        }
        predicate += ")";
        context.addAxiom(new Axiom(name, predicate));

        //target
        name = "axm_tgtTdef";
        predicate = "partition(targetT";
        //Itera para cada tipo de aresta
        for (EdgeType edgeType : g.getTypeGraph().getAllowedEdges()) {
            //Itera para cada tipo de nodo possível daquela aresta, concatenando o mapeamento de cada uma
            for (String target : edgeType.getTarget()) {
                predicate += ", {" + edgeType.getType() + "|->" + target + "}";
            }
        }
        predicate += ")";
        context.addAxiom(new Axiom(name, predicate));
        
        return true;
    }
    
    /**
     * DEFINITION 33
     * Função que realiza a tradução dos atributos de um grafo tipo
     * @param context
     * @param g
     * @return 
     */
    public boolean attributedTypeGraphTranslation(Context context, Grammar g) {
        HashMap <String, NodeType> attNodes = g.getTypeGraph().getAttNodes();
        if (attNodes.isEmpty())
            return false;
        
        /* -- Sets -- */
        context.addSet(new Set("AttrT"));
        context.addSet(new Set("DataType"));
       /* ---------- */
       
       /* -- Constants -- */
       context.addConstant(new Constant("attrvT"));
       context.addConstant(new Constant("valT"));
       /* --------------- */
       
       /* -- s : S ???? -- */
       
       /* -------------------------------------------------------------- *
        * Needs Set with all elements with atributes.                    *
        * Most eficient way: previously define the set in  the parser,   *
        * without adding complexity.                                     *
        * -------------------------------------------------------------- */
       for (NodeType nt: attNodes.values()){
           for (AttributeType at: nt.getAttributes()){
               context.addConstant(new Constant("at" + at.getID()));
           }
       }
              
        return true;
    }

    //Revisado
    /**
     * DEFINITION 16
     * Método que realiza a tradução e criação do grafo estado e o insere em uma
     * máquina.
     *
     * @param m
     * @param c
     * @param g
     * @return
     */
    public boolean stateGraphTranslation(Machine m, Context c, Grammar g) {

        /*
         * -- Adiciona variáveis do grafo estado --
         */
        //Vértices
        m.addVariable(new Variable("vertG"));
        //Arestas
        m.addVariable(new Variable("vdgeG"));
        //Fonte
        m.addVariable(new Variable("sourceG"));
        //Destino
        m.addVariable(new Variable("targetG"));
        //Tipagem de vértices
        m.addVariable(new Variable("tG_V"));
        //Tipagem de Arestas
        m.addVariable(new Variable("tG_E"));

        /*
         * -- Adiciona invariantes do grafo estado --
         */
        //Auxiliares
        String name, predicate;

        //Invariante para vertices pertencerem a partes dos naturais
        name = "inv_vertG";
        predicate = "vertG : POW(NAT)";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para arestas pertencerem a partes dos naturais
        name = "inv_edgeG";
        predicate = "edgeG : POW(NAT)";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir domínio da fonte de aresta para nodo (EdgeG->VertG)
        name = "inv_sourceG";
        predicate = "sourceG : edgeG --> vertG";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir domínio da destino de aresta para nodo (EdgeG->VertG)
        name = "inv_targetG";
        predicate = "targetG : edgeG --> vertG";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir tipagem de vértices de vertice do estado para vertice do tipo (vertG->vertT)
        name = "inv_tGv";
        predicate = "tGv : vertG --> vertT";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir tipagem de arestas de aresta do estado para aresta do tipo (edgeG->edgeT)
        name = "inv_tGe";
        predicate = "tGe : edgeG --> edgeT";
        m.addInvariant(name, new Invariant(name, predicate));

        /*
         * -- Adiciona eventos --
         */
        //Evento de inicialização do grafo estado
        Event initialisation = new Event("INITIALISATION");
        String aux[];
        int flag;

        //Act para inicialização de nodos
        name = "act_vertG";
        predicate = "vertG = {";
        flag = 0;
        for (Node n : g.getHost().getNodes()) {            
            aux = n.getID().split("I");
            if (flag == 0) {
                predicate = predicate + aux[1];
            }
            else {
                predicate = predicate + ", " + aux[1];
            }
            flag = 1;
        }
        predicate += "}";
        initialisation.addAct(name, predicate);

        //Act para inicialização das arestas
        name = "act_edgeG";
        predicate = "edgeG = {";
        flag = 0;
        for (Edge e : g.getHost().getEdges()) {            
            aux = e.getID().split("I");
            if (flag != 0) {
                predicate = predicate + ", ";
            }
            predicate = predicate + aux[1];
            flag = 1;
        }
        predicate += "}";
        initialisation.addAct(name, predicate);

        //Act para definir função source
        name = "act_srcG";
        predicate = "sourceG = {";
        flag = 0;
        for (Edge e : g.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0) {
                predicate = predicate + ", ";
            }
            predicate = predicate + aux[1] + "|->" + e.getSource();
            flag = 1;
        }        
        predicate += "}";
        initialisation.addAct(name, predicate);

        //Act para definir função target
        name = "act_tgtG";
        predicate = "targetG = {";
        flag = 0;
        for (Edge e : g.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0) {
                predicate = predicate + ", ";
            }
            predicate += aux[1] + "|->" + e.getTarget();
            flag = 1;
        }        
        predicate += "}";
        initialisation.addAct(name, predicate);

        //Act para tipagem dos nodos
        name = "act_tG_V";
        predicate = "tG_V = {";
        flag = 0;
        for (Node n : g.getHost().getNodes()) {
            aux = n.getID().split("I");
            if (flag != 0) {
                predicate += ", ";
            }
            predicate += aux[1] + "|->" + n.getType();
            flag = 1;
        }        
        predicate += "}";
        initialisation.addAct(name, predicate);

        //Act para tipagem das arestas
        name = "act_tG_E";
        predicate = "tG_E = {";
        flag = 0;
        for (Edge e : g.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0) {
                predicate += ", ";
            }
            predicate += aux[1] + "|->" + e.getType();
            flag = 1;
        }        
        predicate += "}";
        initialisation.addAct(name, predicate);

        //Adiciona evento
        m.addEvent(initialisation);
        return true;
    }

    //Revisado
    /**
     * DEFINITION 17,
     * Função que realiza a tradução de um conjunto de regras de uma gramática
     * de grafos para a notação EventB
     *
     * @param context - contexto ao qual as regras devem ser inseridas
     * @param g       - gramática cujas regras serão traduzidas (fonte)
     * @return - retorna true ou false, indicando sucesso ou falha na tradução
     */
    public boolean rulePatternTranslation(Context context, Grammar g) {

        /*
         * ----------------------------------------------------------------*
         * A tradução de cada regra foi dividida em duas etapas: --*
         * (1) tradução do LHS da regra (Definição 17 do paper); --*
         * (2) tradução da aplicação da regra DPO (Definição 22); --*
         * (3) tradução dos NACs da regra (definição 18); --*
         * (4) tradução das NACs como guarda (definição 20). --*
         * ----------------------------------------------------------------
         */
        //Itera conjunto de regras
        for (Rule r : g.getRules()) {
            /*
             * -------------------*
             * -- Passo 1: LHS ---*
             * -------------------
             */

            //Define 2 sets para representar conjunto de vértices e de arestas
            Set nodesL, edgesL;
            nodesL = new Set("vert" + r.getName());
            edgesL = new Set("edge" + r.getName());
            context.addSet(nodesL);
            context.addSet(edgesL);

            //Define uma constante para cada nodo no LHS da regra
            for (Node n : r.getLHS().getNodes()) {
                context.addConstant(new Constant(r.getName() + "L" + n.getType()));
                
            }

            //Define uma constante para cada aresta no LHS da regra
            for (Edge e : r.getLHS().getEdges()) {
                context.addConstant(new Constant(r.getName() + "L" + e.getType()));
            }

            //Define constantes para funções source e target
            context.addConstant(new Constant("sourceL" + r.getName()));
            context.addConstant(new Constant("targetL" + r.getName()));

            //Define duas constantes para represnetar a tipagem de arestas e nodos
            //no LHS.
            context.addConstant(new Constant("t" + r.getName() + "v"));
            context.addConstant(new Constant("t" + r.getName() + "e"));

            /*
             * -- Axiomas para vértices e arestas --
             */
            //Auxiliares
            String name, predicate;

            //Define axiomas que representam a tipagem dos vértices e arestas
            name = "axm_t" + r.getName() + "V";
            predicate = "t" + r.getName() + "V : vert" + nodesL.getName() + " --> vertT";
            context.addAxiom(new Axiom(name, predicate));
            
            name = "axm_t" + r.getName() + "E";
            predicate = "t" + r.getName() + "E : edge" + edgesL.getName() + " --> edgeT";
            context.addAxiom(new Axiom(name, predicate));

            //Define axiomas para definição das funções de tipagem
            //Nodos
            name = "axm_t" + r.getName() + "V_def";
            predicate = "partition(t" + r.getName() + "V";
            for (Node n : r.getLHS().getNodes()) {                
                predicate = predicate + ", {" + n.getID() + " |-> " + n.getType() + "}";
            }
            predicate = predicate + ")";
            context.addAxiom(new Axiom(name, predicate));

            //Arestas
            name = "axm_t" + r.getName() + "E_def";
            predicate = "partition(t" + r.getName() + "E";            
            for (Edge e : r.getLHS().getEdges()) {                
                predicate = predicate + ", {" + e.getID() + " |-> " + e.getType() + "}";
            }
            predicate = predicate + ")";            
            context.addAxiom(new Axiom(name, predicate));

            //Define NACs da regra
            if (!NACTranslation(context, g, r)) {
                return false;
            }
        }
        return true;
    }

    //Revisado
    /**
     * DEFINIÇÃO 18
     * Método que realiza a tradução das NACs de uma regra
     *
     * @param c - contexto ao qual serão inseridas NACs
     * @param g - gramática fonte
     * @param r - regra da qual serão traduzidas as NACs
     * @return - retorna true ou false, indicando sucesso ou falha do método
     */
    public boolean NACTranslation(Context c, Grammar g, Rule r) {

        //Montar conjunto NAC (proibidos)
        //Vertices
        HashSet<String> vertNAC = new HashSet<>();
        //Arestas
        HashSet<String> edgeNAC = new HashSet<>();

        //Contador de controle das NACs
        int cont = 0;

        //NACV - Forbidden vertices = NACv - (NACv intersecçao LHS)
        for (Graph NAC : r.getNACs()) {
            //Limpa auxiliares
            forbiddenVertices.clear();
            vertNAC.clear();
            forbiddenEdges.clear();
            edgeNAC.clear();

            //Monta vertNAC e forbiddenVertices
            for (Node n : NAC.getNodes()) {
                String temp = NAC.getMorphism().get(n.getID());
                if (temp != null) {
                    vertNAC.add(temp);
                }
                else {
                    vertNAC.add(n.getID());
                    forbiddenVertices.put(n.getID(), n);
                }
            }

            //Monta edgeNAC e forbiddenEdges
            //Monta vertNAC e forbiddenVertices
            for (Edge e : NAC.getEdges()) {
                String temp = NAC.getMorphism().get(e.getID());
                if (temp != null) {
                    edgeNAC.add(temp);
                }
                else {
                    edgeNAC.add(e.getID());
                    forbiddenEdges.put(e.getID(), e);
                }
            }

            //TODO
            //Montar forbidden identification, mais complexo
            //SETS
            c.addSet(new Set("Vert" + r.getName() + "NAC" + cont));
            c.addSet(new Set("Edge" + r.getName() + "NAC" + cont));

            //CONSTANTS
            //VertLNACj
            for (Node n : forbiddenVertices.values()) {
                c.addConstant(new Constant(n.getID()));
                
            }
            //EdgeLNACj
            for (Edge e : forbiddenEdges.values()) {
                c.addConstant(new Constant(e.getID()));
            }

            //sourceLNACj
            c.addConstant(new Constant("source" + r.getName() + "NAC" + cont));

            //targetLNACj
            c.addConstant(new Constant("target" + r.getName() + "NAC" + cont));

            //tLVNACj- tipagem nodos NAC->Tipo
            c.addConstant(new Constant("t" + r.getName() + "VNAC" + cont));

            //tLENACj - tipagem arestas NAC->Tipo
            c.addConstant(new Constant("t" + r.getName() + "ENAC" + cont));

            //ljV - morfismo nodos NAC->LHS
            c.addConstant(new Constant(r.getName() + cont + "V"));

            //ljE - morfismo arestas NAC->LHS
            c.addConstant(new Constant(r.getName() + cont + "E"));

            //AXIOMAS
            String name, predicate;

            //axm_ljV
            name = "axm_" + r.getName() + cont + "V";
            predicate = r.getName() + cont + "V : Vert" + r.getName() + " --> Vert" + r.getName() + "NAC" + cont;
            c.addAxiom(new Axiom(name, predicate));

            //axm_ljV_def
            name = "axm_" + r.getName() + cont + "V_def";
            predicate = "partition(" + r.getName() + cont + "V";
            for (Node n : NAC.getNodes()) {
                String temp = NAC.getMorphism().get(n.getID());
                if (temp != null) {
                    predicate = predicate + ", {" + n.getID() + " |-> " + n.getType() + "}";
                }                
            }
            predicate = predicate + ")";
            c.addAxiom(new Axiom(name, predicate));

            //axm_ljE
            name = "axm_" + r.getName() + cont + "E";
            predicate = r.getName() + cont + "E : Edge" + r.getName() + " --> Vert" + r.getName() + "NAC" + cont;
            c.addAxiom(new Axiom(name, predicate));

            //axm_ljE_def
            name = "axm_" + r.getName() + cont + "E_def";
            predicate = "partition(" + r.getName() + cont + "E";
            for (Edge e : NAC.getEdges()) {
                String temp = NAC.getMorphism().get(e.getID());
                if (temp != null) {
                    predicate = predicate + ", {" + e.getID() + " |-> " + e.getType() + "}";
                }                
            }
            predicate = predicate + ")";
            c.addAxiom(new Axiom(name, predicate));
            
            cont++;            
        }        
        return true;
    }

    //REVISAR
    /**
     * DEFINITIION 22
     * Método que define a aplicação de regras. Cria os eventos e outros ele-
     * mentos necessários para aplicação das regras
     *
     * @param c - contexto fonte
     * @param m - máquina destino
     * @param g - gramática fonte
     * @return - true/false indicando sucesso/falha na operação de tradução
     */
    public boolean DPOApplication(Context c, Machine m, Grammar g) {
        
        Event ruleEvent;
        String name, predicate;
        //Preserved nodes ids
        HashSet<String> preservedNodes;
        //Created nodes ids
        HashSet<String> createdNodes;
        //Reference to created Nodes
        HashSet<Node> createdNodesRef;
        //Deleted nodes ids
        HashSet<String> deletedNodes;

        //Preserved edges ids
        HashSet<String> preservedEdges;
        //Preserved edges reference
        HashSet<Edge> preservedEdgesRef;
        //Created edges ids
        HashSet<String> createdEdges;
        //Reference to created edges
        HashSet<Edge> createdEdgesRef;
        //Deleted edges ids
        HashSet<String> deletedEdges;

        //Itera entre todas as regras
        for (Rule r : g.getRules()) {
            
            ruleEvent = new Event(r.getName());

            /*
             * -- Criação de hashsets auxiliares --
             */
            preservedNodes = new HashSet<>();
            createdNodes = new HashSet<>();
            createdNodesRef = new HashSet<>();
            deletedNodes = new HashSet<>();
            preservedEdges = new HashSet<>();
            preservedEdgesRef = new HashSet<>();
            createdEdges = new HashSet<>();
            createdEdgesRef = new HashSet<>();
            deletedEdges = new HashSet<>();

            //Cria set com nodos preservados e criados
            for (Node n : r.getRHS().getNodes()) {
                String temp = r.getRHS().getMorphism().get(n.getID());
                if (temp != null) {
                    preservedNodes.add(temp);
                }
                else {
                    createdNodes.add(n.getID());
                    createdNodesRef.add(n);
                }
            }
            //Cria set com arestas preservadas e criadas
            for (Edge e : r.getRHS().getEdges()) {
                String temp = r.getRHS().getMorphism().get(e.getID());
                if (temp != null) {
                    preservedEdgesRef.add(e);
                    preservedEdges.add(temp);
                }
                else {
                    createdEdgesRef.add(e);
                    createdEdges.add(e.getID());
                }
            }

            /*
             * -------------------*
             * -- Passo 1: ANY ---*
             * -------------------
             */
 /*
             * -- Parâmetros --
             */
            ruleEvent.addParameter("mV");
            ruleEvent.addParameter("mE");
            ruleEvent.addParameter("DelV");
            ruleEvent.addParameter("PreservV");
            ruleEvent.addParameter("DelE");
            ruleEvent.addParameter("Dangling");

            /*
             * -- Listas Pre-Fixadas --
             */
            //Nodos
            for (String n : createdNodes) {
                ruleEvent.addParameter("newV_" + n);
            }
            //Arestas
            for (String e : createdEdges) {
                ruleEvent.addParameter("newE_" + e);
            }
            /*
             * -------------------*
             * --- FIM PASSO 1 ---*
             * -------------------
             */

 /*
             * -------------------*
             * -- Passo 2: WHERE -*
             * -------------------
             */
            //Função total mapeando os vértices
            name = "grd_mV";
            predicate = "mV : Vert" + r.getName() + " --> VertG";
            ruleEvent.addGuard(name, predicate);

            //Função total mapeando as arestas
            name = "grd_mE";
            predicate = "mE : Edge" + r.getName() + " --> EdgeG";
            ruleEvent.addGuard(name, predicate);

            /*
             * -- Vértices Excluídos --
             */
            //Define o set de vértices excluídos
            for (Node n : r.getLHS().getNodes()) {
                deletedNodes.add(n.getID());
            }
            deletedNodes.removeAll(r.getRHS().getMorphism().values());

            //Define guarda
            name = "grd_DelV";
            predicate = "DelV := mV[";
            String[] aux;
            int flag = 0;
            for (String n : deletedNodes) {
                aux = n.split("I");
                if (flag != 0) {
                    predicate += ", ";
                }
                else {
                    flag = 1;
                }
                predicate = predicate + "{" + aux[1] + "}";
            }
            predicate += "]";
            ruleEvent.addGuard(name, predicate);
            /*
             * -- Fim Vértices Excluídos --
             */

 /*
             * -- Vértices Preservados --
             */
            //Define conjunto de vértices preservados
            name = "grd_PreV";
            predicate = "PreservV := VertG \\ DelV";
            ruleEvent.addGuard(name, predicate);
            /*
             * -- Fim Vértices Preservados --
             */

 /*
             * -- Arestas Excluídas --
             */
            //Define o set de arestas excluídas
            for (Edge e : r.getLHS().getEdges()) {
                deletedEdges.add(e.getID());
            }
            deletedEdges.removeAll(r.getRHS().getMorphism().values());
            
            name = "grd_DelE";
            predicate = "DelE := mE[";
            flag = 0;
            for (String e : deletedEdges) {
                aux = e.split("I");
                if (flag != 0) {
                    predicate += ", ";
                }
                else {
                    flag = 1;
                }
                predicate += "{" + aux[1] + "}";
            }
            predicate += "]";
            ruleEvent.addGuard(name, predicate);
            /*
             * -- Fim Arestas Excluídas --
             */

 /*
             * -- Arestas Pendentes --
             */
            //grd_Dang
            //Arestas pendentes
            name = "grd_Dang";
            predicate = "Dangling = dom((source |> DelV) \\/ (targetG |> DelV))\\DelE";
            ruleEvent.addGuard(name, predicate);
            /*
             * -- Fim Arestas Pendentes --
             */

 /*
             * -- Novos Vértices --
             */
            //grd_new_v
            //Guarda para novos vertices pertencerem ao dominio 
            for (String n : createdNodes) {
                name = "grd_new_v" + n;
                predicate = "new_v" + n + ": NAT \\ VertG";
                ruleEvent.addGuard(name, predicate);
            }
            /*
             * -- Fim Novos Vértices --
             */

 /*
             * -- Novas Arestas --
             */
            //grd_new_e
            //Guarda para novas arestas pertencerem ao dominio
            for (String e : createdEdges) {
                name = "grd_new_e" + e;
                predicate = "new_e" + e + ": NAT \\ EdgeG";
                ruleEvent.addGuard(name, predicate);
            }
            /*
             * -- Fim Novos Vértices --
             */

 /*
             * -- Unicidade dos IDs de Novos Vértices --
             */
            //grd_diffvivj
            for (String vi : createdNodes) {
                for (String vj : createdNodes) {
                    if (!vi.equals(vj)) {
                        name = "grd_diff" + vi + vj;
                        predicate = "new_" + vi + " /= " + "new_" + vj;
                        ruleEvent.addGuard(name, predicate);
                    }                    
                }
            }
            /*
             * -- Fim Unicidade dos IDs de Novos Vértices --
             */

 /*
             * -- Unicidade dos IDs de Novas Arestas --
             */
            //grd_diffeiej
            for (String ei : createdEdges) {
                for (String ej : createdEdges) {
                    if (!ei.equals(ej)) {
                        name = "grd_diff" + ei + ej;
                        predicate = "new_" + ei + " /= " + "new_" + ej;
                        ruleEvent.addGuard(name, predicate);
                    }                    
                }
            }
            /*
             * -- Fim Unicidade dos IDs de Novas Arestas --
             */

 /*
             * -- Compatibilidade de tipos de arestas, vértice e das funçoes
             * source e target --
             */
            //grd_tV
            name = "grd_tV";
            predicate = "!v.v : Vert" + r.getName() + "=> t" + r.getName() + "V(v) = tGV(mv(v))";
            ruleEvent.addGuard(name, predicate);

            //grd_tE
            name = "grd_tE";
            predicate = "!e.e : Edge" + r.getName() + "=> t" + r.getName() + "E(e) = tGE(mE(e))";
            ruleEvent.addGuard(name, predicate);

            //grd_srctgt
            name = "grd_srctgt";
            predicate = "!e.e : Edge" + r.getName() + "=> mV(source" + r.getName() + "(e)) = sourceG(mE(e)) and mV(target" + r.getName() + "(e)) = targetG(mE(e))";
            ruleEvent.addGuard(name, predicate);
            /*
             * -- Fim Compatibilidade de tipos de arestas, vértice e das funçoes
             * source e target --
             */

            /* ---------------------- *
             * -- Theoretical NACs -- *
             * ---------------------- */
            //Definir conjunto NAC e NACid
            if (!setTheoreticalNACs(r)) {
                return false;
            }

            /* ------------------- *
             * -- Passo 3: THEN -- *
             * ------------------- */
                       
            /* --- Act_V --- */
            flag = 0;
            name = "act_V";
            predicate = "VertG := (VertG\\DelV)\\/{";
            for (String n : createdNodes) {
                if (flag == 0)
                    flag = 1;
                else
                    predicate += ", ";
                predicate += "new_" + n;
            }
            predicate += "}";
            ruleEvent.addAct(name, predicate);
            /* ------------- */

             /* --- Act_E --- */
            flag = 0;
            name = "act_E";
            predicate = "EdgeG := (EdgeG\\DelE)\\/{";
            for (String e : createdEdges) {
                if (flag == 0)
                    flag = 1;
                else
                    predicate += ", ";
                predicate += "new_" + e;
            }
            predicate += "}";
            ruleEvent.addAct(name, predicate);
             /* ------------- */

            //Needs further testing
            /* --- Act_src --- */
            name = "act_src";
            predicate = "sourceG := (DelE <<| sourceG) \\/ \n{\n";
            flag = 0;
            for (Edge e : createdEdgesRef) {               
                //Testa se nodo fonte da aresta criada é também um nodo criado
                if (createdNodes.contains(e.getSource())) {
                    if (flag == 0)
                        flag = 1;
                    else
                        predicate += ", ";
                    predicate = predicate + "new_" + e.getID() + " |-> " + e.getSource();
                }
                //Testa se nodo fonte é um preservado
                else if (preservedNodes.contains(r.getRHS().getMorphism().get(e.getSource()))) 
                {
                    if (flag == 0)
                        flag = 1;
                    else
                        predicate += ", ";
                    predicate = predicate + "new_" + e.getID() + " |-> " + r.getRHS().getMorphism().get(e.getSource());
                }                
            }
            predicate += "\n}\n";
            ruleEvent.addAct(name, predicate);
            /* ------------- */

               //Needs further testing
            /* --- Act_tgt --- */
            name = "act_tgt";
            predicate = "targetG := (DelE <<| targetG) \\/ \n{\n";
            flag = 0;
            for (Edge e : createdEdgesRef) {
                //Testa se nodo destino da nova aresta é um novo nodo
                if (createdNodes.contains(e.getTarget())) {
                    if (flag == 0)
                        flag = 1;
                    else
                        predicate += ", ";
                    predicate = predicate + "new_" + e.getID() + " |-> " + e.getTarget();
                }
                //Testa se nodo destino é um preservado
                else if (preservedNodes.contains(r.getRHS().getMorphism().get(e.getTarget())))
                {
                    if (flag == 0)
                        flag = 1;
                    else
                        predicate += ", ";
                    predicate = predicate + "new_" + e.getID() + " |-> " + r.getRHS().getMorphism().get(e.getTarget());
                }                
            }
            predicate += "\n}";
            ruleEvent.addAct(name, predicate);
            /* ------------- */

            //Needs further testing
            /* --- Act_tV --- */
            name = "act_tV";
            predicate = "tGV := (DelV <<| tGV \\/ \n{\n";
            flag = 0;
            for (Node n : createdNodesRef) {
                if (flag == 0) 
                    flag = 1;
                else 
                    predicate += ", ";
                predicate = "new_" + n.getID() + " |-> " + n.getType();
            }
            predicate += "\n}";                                                                                                                                                                                                                                                                                                                                                                                                
            ruleEvent.addAct(name, predicate);
            /* ------------- */

              //Needs further testing
            /* --- Act_tE --- */
            name = "act_tE";
            predicate = "tGE := (DelE <<| tGE \\/ \n{\n";
            flag = 0;
            for (Edge e : createdEdgesRef) {
                if (flag == 0) {
                    flag = 1;
                }
                else {
                    predicate += ", ";
                }
                predicate = "new_" + e.getID() + " |-> " + e.getType();
            }
            predicate += "\n}";
            ruleEvent.addAct(name, predicate);
            /* ------------- */
            
            //Add the event with all the defined guards and acts
            m.addEvent(ruleEvent);
        }
        return true;
    }

    /**
     * Done - Needs Testing
     * Theoretical NACs - segundo definition 20
     *
     * @param r
     * @return true || false de acordo com sucesso || insucesso da definição
     */
    public boolean setTheoreticalNACs(Rule r) {
        String name, predicate;
        for (Graph NAC : r.getNACs()) {
            name = "grd_NAC" + r.getName();
            predicate = "not(#";
            
            String nodeSetString = "", edgeSetString = "";
            int flag = 0;
            for (Node n : forbiddenVertices.values()) {
                if (flag == 0) {
                    flag = 1;
                }
                else {
                    nodeSetString += ", ";
                }
                nodeSetString += n.getID();
            }
            flag = 0;
            for (Edge e : forbiddenEdges.values()) {
                if (flag == 0) {
                    flag = 1;
                }
                else {
                    nodeSetString += ", ";
                }
                edgeSetString += e.getID();
            }
            
            predicate += nodeSetString + ", " + edgeSetString + ".";  
            predicate += "{" + nodeSetString + "} <: VertG \\ mE [Vert" + r.getName() + "] and\n";
            predicate += "{" + edgeSetString + "} <: EdgeG \\ mE [Edge" + r.getName() + "] and\n";
            
            //Guarda que garante unicidade do ID dos vértices
            for (Node n1 : forbiddenVertices.values()) {
                for (Node n2 : forbiddenVertices.values()) {
                    flag = 1;
                    predicate += n1.getID() + "/=" + n2.getID() + " and ";
                }
            }
            predicate += "\n";
                        
            //Guarda que garante unicidade do ID das arestas
            for (Edge e1 : forbiddenEdges.values()) {
                for (Edge e2 : forbiddenEdges.values()) {
                    predicate += e1.getID() + "/=" + e2.getID() + " and ";
                }
            }
            predicate += "\n";
           
             //Needs testing
            //Tipagem de vértices proibidos
            for (Node n1 : forbiddenVertices.values()) {
                String ID = null;
                ID = NAC.getMorphism().get(n1.getID());
                if (ID != null) {
                    predicate += "tGV(" + n1.getID() + ") = " + ID + " and ";
                }
            }
            predicate += "\n";
            
            //Needs testing
            //Tipagem de arestas proibidas
            Graph LHS = r.getLHS();
            flag = 0;
            for (Edge e1 : forbiddenEdges.values()) {
                String ID = null;
                ID = NAC.getMorphism().get(e1.getID());
                if (ID != null) {
                    predicate += "tGE(" + e1.getID() + ") = " + ID;
                    /* -- Source -- */
                    //if sourceLNACj(e) == v and v : NACjV
                    Node source = forbiddenVertices.get(e1.getSource());
                    if (flag == 0)
                        flag = 1;
                    else
                        predicate += " and\n";
                    if (source != null)
                        predicate += "sourceG(" + e1.getID() + ") = " + source.getID();
                    else{
                        predicate += "sourceG(" + e1.getID() + ") = mV(" + e1.getSource() + ")";
                    }
                     /* ------------ */
                     /* -- Target -- */
                    Node target = forbiddenVertices.get(e1.getTarget());
                    if (flag == 0)
                        flag = 1;
                    else
                        predicate += " and\n";
                    if (source != null)
                        predicate += "targetG(" + e1.getID() + ") = " + target.getID();
                    else{
                        predicate += "targetG(" + e1.getID() + ") = mV(" + e1.getTarget() + ")";
                    }
                      /* ------------ */
                }               
            }            
            predicate += ") or\n";
            
            //Needs testing
            /* -- Unicidade do match -- */
            ArrayList<Node> nodeList;
            nodeList = (ArrayList) forbiddenVertices.values();
            flag = 0;
            predicate += "(";
            for (int i = 0; i < nodeList.size()-1; i++){
                for (int j = i + 1; j < nodeList.size(); j++){
                    if (flag == 0)
                        flag = 1;
                    else
                        predicate += " or\n";
                    predicate += "mV(" + nodeList.get(i).getID() + ") /= mV(" + nodeList.get(j) + ")"; 
                }
            }
            predicate += ")";
             /* ------------------------ */
             
             //Needs testing
            /* -- Unicidade do match -- */
            ArrayList<Edge> edgeList;
            edgeList = (ArrayList) forbiddenEdges.values();
            flag = 0;
            predicate += "(";
            for (int i = 0; i < edgeList.size(); i++){
                for (int j = i + 1; j < edgeList.size(); j++){
                    if (flag == 0)
                        flag = 1;
                    else
                        predicate += " or\n";
                    predicate += "mE(" + edgeList.get(i).getID() + ") /= mE(" + edgeList.get(j) + ")"; 
                }
            }
            predicate += ")";
             /* ------------------------ */
            
        }
        return true;
    }

    /* ----- Refinamento ----- */
    
    
    /**
     * Main para testes de conversão de Agg para GG e de GG para EventB
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        /**
         * -- Step1 - AGG to GG translation --
         */
        String arquivo = "pacman.ggx";
        AGGToGraphGrammar agg = new AGGToGraphGrammar();
        Grammar test = new Grammar("pacman");
        agg.aggReader(arquivo, test);
        test.printGrammar();

        /*
         * -- Step 2 - GG to EventB translation --
         */
        GraphGrammarToEventB eventB = new GraphGrammarToEventB();
        Project newProject = new Project("pacman");
        eventB.translator(newProject, test);        
        System.out.println("Finished!");
    }
}
