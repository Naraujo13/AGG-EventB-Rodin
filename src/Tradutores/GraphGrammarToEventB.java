/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tradutores;

import GraphGrammar.EdgeType;
import GraphGrammar.NodeType;
import EventB.*;
import GraphGrammar.Attribute;
import GraphGrammar.AttributeType;
import GraphGrammar.Edge;
import GraphGrammar.Grammar;
import GraphGrammar.Graph;
import GraphGrammar.Node;
import GraphGrammar.Rule;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Nícolas Oreques de Araujo
 */
public class GraphGrammarToEventB {
    
    private HashMap<String, Node> forbiddenVertices = new HashMap<>();
    private HashMap<String, Edge> forbiddenEdges = new HashMap<>();
    private StringBuilder stringBuilder = new StringBuilder(1024);

    /**
     * Função que realiza a tradução de uma gramática de grafos para um projeto em notação event-B
     * @param p - projeto a ser criado
     * @param g - gramática a ser traduzida
     * @return - sucesso ou fracasso
     */
    private boolean translator(Project p, Grammar g) {

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
     * @param g - gramática que está sendo traduzida
     * @return - retorna true ou false indicando se a operação foi bem sucedida
     *         ou não
     */
    private boolean typeGraphTranslation(Context context, Grammar g) {

        /*
         * -- Instanciações e adições ao contexto --
         */
        //Sets
        context.addSet(new Set("VertT"));
        context.addSet(new Set("EdgeT"));
        //Constants
        context.addConstant(new Constant("SourceT"));
        context.addConstant(new Constant("TargetT"));

        /*
         * -- Traduz os tipos de nodos e arestas definidos no grafo tipo --
         */
         /*
         * Define constantes para representar tipos de nodos e arestas definidos
         * no grafo tipo
         */
        //Cria uma constante para cada tipo de nodo e adiciona estas constantes ao contexto
        for (NodeType nodeType : g.getTypeGraph().getAllowedNodes())
            context.addConstant(new Constant(nodeType.getType()));
     
        //Cria uma constante para cada tipo de aresta e adiciona estas constantes ao contexto
        for (EdgeType EdgeType : g.getTypeGraph().getAllowedEdges())
            context.addConstant(new Constant(EdgeType.getType()));
        
        /*
         * -- Axioms --
         */
        String name, predicate;

        //Define axiomas que representam tipagem das funções Source e Target
        name = "axm_srcTtype";
        predicate = "SourceT : EdgeT --> VertT";
        context.addAxiom(new Axiom(name, predicate));
        name = "axm_tgtTtype";
        predicate = "TargetT : EdgeT --> VertT";
        context.addAxiom(new Axiom(name, predicate));

        //Define axiomas para representar os tipos de VertT e EdgeT
        //VertT
        name = "axm_VertT";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(VertT");
        
        for (NodeType nodeType : g.getTypeGraph().getAllowedNodes()) {
            stringBuilder.append(", {").append(nodeType.getType()).append("}");
        }
        stringBuilder.append(")");
        context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        //EdgeT
        name = "axm_EdgeT";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(EdgeT");
        
        for (EdgeType et : g.getTypeGraph().getAllowedEdges()) {
            stringBuilder.append(", {").append(et.getType()).append("}");
        }
        stringBuilder.append(")");
        context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        //Define axiomas para representar funções Source e Target
        //Source
        name = "axm_srcTdef";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(SourceT");
        //Itera para cada tipo de aresta
        for (EdgeType et : g.getTypeGraph().getAllowedEdges()) {
            //Itera para cada tipo de nodo possível daquela aresta, concatenando o mapeamento de cada uma
            for (String Source : et.getSource()) {
               stringBuilder.append(", {").append(et.getType()).append("|->").append(Source).append("}");
            }
        }
        stringBuilder.append(")");
        context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        //Target
        name = "axm_tgtTdef";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(TargetT");
        //Itera para cada tipo de aresta
        for (EdgeType EdgeType : g.getTypeGraph().getAllowedEdges()) {
            //Itera para cada tipo de nodo possível daquela aresta, concatenando o mapeamento de cada uma
            for (String Target : EdgeType.getTarget()) {
               stringBuilder.append(", {").append(EdgeType.getType()).append("|->").append(Target).append("}");
            }
        }
        stringBuilder.append(")");
        context.addAxiom(new Axiom(name, stringBuilder.substring(0)));
        
        return true;
    }

    //Revisado
    /**
     * DEFINITION 16
     * Método que realiza a tradução e criação do grafo estado e o insere em uma
     * máquina.
     * @param m - máquina a ser criada
     * @param c - contexto a ser criada
     * @param g - gramática a ser traduzida
     * @return - sucesso ou fracasso da tradução
     */
    private boolean stateGraphTranslation(Machine m, Context c, Grammar g) {
        /*
         * -- Adiciona variáveis do grafo estado --
         */
        //Vértices
        m.addVariable(new Variable("VertG"));
        //Arestas
        m.addVariable(new Variable("vdgeG"));
        //Fonte
        m.addVariable(new Variable("SourceG"));
        //Destino
        m.addVariable(new Variable("TargetG"));
        //Tipagem de vértices
        m.addVariable(new Variable("tG_V"));
        //Tipagem de Arestas
        m.addVariable(new Variable("tG_E"));

        /*
         * -- Adiciona invariantes do grafo estado --
         */
        //Auxiliares
        String name, predicate;

        //Invariante para Vertices pertencerem a partes dos naturais
        name = "inv_VertG";
        predicate = "VertG : POW(NAT)";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para arestas pertencerem a partes dos naturais
        name = "inv_EdgeG";
        predicate = "EdgeG : POW(NAT)";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir domínio da fonte de aresta para nodo (EdgeG->VertG)
        name = "inv_SourceG";
        predicate = "SourceG : EdgeG --> VertG";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir domínio da destino de aresta para nodo (EdgeG->VertG)
        name = "inv_TargetG";
        predicate = "TargetG : EdgeG --> VertG";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir tipagem de vértices de Vertice do estado para Vertice do tipo (VertG->VertT)
        name = "inv_tGv";
        predicate = "tGv : VertG --> VertT";
        m.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir tipagem de arestas de aresta do estado para aresta do tipo (EdgeG->EdgeT)
        name = "inv_tGe";
        predicate = "tGe : EdgeG --> EdgeT";
        m.addInvariant(name, new Invariant(name, predicate));

        /*
         * -- Adiciona eventos --
         */
        //Evento de inicialização do grafo estado
        Event initialisation = new Event("INITIALISATION");
        String aux[];
        int flag;

        //Act para inicialização de nodos
        name = "act_VertG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("VertG = {");
        flag = 0;
        for (Node n : g.getHost().getNodes()) {            
            aux = n.getID().split("I");
            if (flag == 0) {
                stringBuilder.append(aux[1]);
            }
            else {
                stringBuilder.append(", ").append(aux[1]);
            }
            flag = 1;
        }
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para inicialização das arestas
        name = "act_EdgeG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("EdgeG = {");
        flag = 0;
        for (Edge e : g.getHost().getEdges()) {            
            aux = e.getID().split("I");
            if (flag != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(aux[1]);
            flag = 1;
        }
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para definir função Source
        name = "apublicct_srcG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("SourceG = {");
        flag = 0;
        for (Edge e : g.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(aux[1]).append("|->").append(e.getSource());
            flag = 1;
        }        
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para definir função Target
        name = "act_tgtG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("TargetG = {");
        flag = 0;
        for (Edge e : g.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(aux[1]).append("|->").append(e.getTarget());
            flag = 1;
        }        
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para tipagem dos nodos
        name = "apublicct_tG_V";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("tG_V = {");
        flag = 0;
        for (Node n : g.getHost().getNodes()) {
            aux = n.getID().split("I");
            if (flag != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(aux[1]).append("|->").append(n.getType());
            flag = 1;
        }        
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para tipagem das arestas
        name = "act_tG_E";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("tG_E = {");
        flag = 0;
        for (Edge e : g.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0) {
                stringBuilder.append(", ");
            }
            stringBuilder.append(aux[1]).append("|->").append(e.getType());
            flag = 1;
        }        
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Adiciona evento
        m.addEvent(initialisation);
        return true;
    }

    /**
     * DEFINITION 17,
     * Função que realiza a tradução de um conjunto de regras de uma gramática
     * de grafos para a notação EventB.
     * 100% Revised and working
     * @param context - contexto ao qual as regras devem ser inseridas
     * @param g       - gramática cujas regras serão traduzidas (fonte)
     * @return - retorna true ou false, indicando sucesso ou falha na tradução
     */
    private boolean rulePatternTranslation(Context context, Grammar g) {

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
            Set nodesL, EdgesL;
            nodesL = new Set("Vert" + r.getName());
            EdgesL = new Set("Edge" + r.getName());
            context.addSet(nodesL);
            context.addSet(EdgesL);

            //Define uma constante para cada nodo no LHS da regra
            for (Node n : r.getLHS().getNodes()) {
                context.addConstant(new Constant(r.getName() + n.getID()));
                
            }

            //Define uma constante para cada aresta no LHS da regra
            for (Edge e : r.getLHS().getEdges()) {
                context.addConstant(new Constant(r.getName() + e.getID()));
            }

            //Define constantes para funções Source e Target
            context.addConstant(new Constant("Source" + r.getName()));
            context.addConstant(new Constant("Target" + r.getName()));

            //Define duas constantes para represnetar a tipagem de arestas e nodos
            //no LHS.
            context.addConstant(new Constant("t" + r.getName() + "V"));
            context.addConstant(new Constant("t" + r.getName() + "E"));

            /*
             * -- Axiomas para vértices e arestas --
             */
            //Auxiliares
            String name, predicate;

            //Define axiomas que representam a tipagem dos vértices e arestas
            name = "axm_t" + r.getName() + "V";
            predicate = "t" + r.getName() + "V : Vert" + r.getName() + " --> VertT";
            context.addAxiom(new Axiom(name, predicate));
            
            name = "axm_t" + r.getName() + "E";
            predicate = "t" + r.getName() + "E : Edge" + r.getName() + " --> EdgeT";
            context.addAxiom(new Axiom(name, predicate));

            //Define axiomas para definição das funções de tipagem
            //Nodos
            name = "axm_t" + r.getName() + "V_def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(t").append(r.getName()).append("V");
            for (Node n : r.getLHS().getNodes()) {
                stringBuilder.append(", {").append(r.getName()).append(n.getID()).append(" |-> ").append(n.getType()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Arestas
            name = "axm_t" + r.getName() + "E_def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(t").append(r.getName()).append("E");
            for (Edge e : r.getLHS().getEdges()) {                
                stringBuilder.append(", {").append(r.getName()).append(e.getID()).append(" |-> ").append(e.getType()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Define NACs da regra
            if (!NACTranslation(context, g, r))
                return false;
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
    private boolean NACTranslation(Context c, Grammar g, Rule r) {

        //Montar conjunto NAC (proibidos)
        //Vertices
        HashSet<String> VertNAC = new HashSet<>();
        //Arestas
        HashSet<String> EdgeNAC = new HashSet<>();

        //Contador de controle das NACs
        int cont = 0;

        //NACV - Forbidden Vertices = NACv - (NACv intersecçao LHS)
        for (Graph NAC : r.getNACs()) {
            //Limpa auxiliares
            forbiddenVertices.clear();
            VertNAC.clear();
            forbiddenEdges.clear();
            EdgeNAC.clear();

            //Monta VertNAC e forbiddenVertices
            for (Node n : NAC.getNodes()) {
                String temp = NAC.getMorphism().get(n.getID());
                if (temp != null) {
                    VertNAC.add(temp);
                }
                else {
                    VertNAC.add(n.getID());
                    forbiddenVertices.put(n.getID(), n);
                }
            }

            //Monta EdgeNAC e forbiddenEdges
            //Monta VertNAC e forbiddenVertices
            for (Edge e : NAC.getEdges()) {
                String temp = NAC.getMorphism().get(e.getID());
                if (temp != null) {
                    EdgeNAC.add(temp);
                }
                else {
                    EdgeNAC.add(e.getID());
                    forbiddenEdges.put(e.getID(), e);
                }
            }

            /* -- Sets -- */
            c.addSet(new Set("Vert" + r.getName() + "NAC" + cont));
            c.addSet(new Set("Edge" + r.getName() + "NAC" + cont));

           /* -- Constantes -- */
            //VertLNACj
            for (Node n : forbiddenVertices.values()) {
                c.addConstant(new Constant(n.getID()));
                
            }
            //EdgeLNACj
            for (Edge e : forbiddenEdges.values()) {
                c.addConstant(new Constant(e.getID()));
            }

            //SourceLNACj
            c.addConstant(new Constant("Source" + r.getName() + "NAC" + cont));

            //TargetLNACj
            c.addConstant(new Constant("Target" + r.getName() + "NAC" + cont));

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
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(").append(r.getName()).append(cont).append("V");
            for (Node n : NAC.getNodes()) {
                String temp = NAC.getMorphism().get(n.getID());
                if (temp != null)
                    stringBuilder.append(", {").append(n.getID()).append(" |-> ").append(n.getType()).append("}");
            }
            stringBuilder.append(")");
            c.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //axm_ljE
            name = "axm_" + r.getName() + cont + "E";
            predicate = r.getName() + cont + "E : Edge" + r.getName() + " --> Vert" + r.getName() + "NAC" + cont;
            c.addAxiom(new Axiom(name, predicate));

            //axm_ljE_def
            name = "axm_" + r.getName() + cont + "E_def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(").append(r.getName()).append(cont).append("E");
            for (Edge e : NAC.getEdges()) {
                String temp = NAC.getMorphism().get(e.getID());
                if (temp != null)
                    stringBuilder.append(", {").append(e.getID()).append(" |-> ").append(e.getType()).append("}");
            }
            stringBuilder.append(")");
            c.addAxiom(new Axiom(name, stringBuilder.substring(0)));
            
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
    private boolean DPOApplication(Context c, Machine m, Grammar g) {
        
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

        //Preserved Edges ids
        HashSet<String> preservedEdges;
        //Preserved Edges reference
        HashSet<Edge> preservedEdgesRef;
        //Created Edges ids
        HashSet<String> createdEdges;
        //Reference to created Edges
        HashSet<Edge> createdEdgesRef;
        //Deleted Edges ids
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
            String[] aux;
            int flag = 0;
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("DelV := mV[");
            for (String n : deletedNodes) {
                aux = n.split("I");
                if (flag != 0) {
                    stringBuilder.append(", ");
                }
                else {
                    flag = 1;
                }
                stringBuilder.append("{").append(aux[1]).append("}");
            }
            stringBuilder.append("]");
            ruleEvent.addGuard(name, stringBuilder.substring(0));
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
            stringBuilder.delete(0, stringBuilder.length());
            name = "grd_DelE";
            stringBuilder.append("DelE := mE[");
            flag = 0;
            for (String e : deletedEdges) {
                aux = e.split("I");
                if (flag != 0) {
                    stringBuilder.append(", ");
                }
                else {
                    flag = 1;
                }
                stringBuilder.append("{").append(aux[1]).append("}");
            }
            stringBuilder.append("]");
            ruleEvent.addGuard(name, stringBuilder.substring(0));
            /*
             * -- Fim Arestas Excluídas --
             */

            /*
             * -- Arestas Pendentes --
             */
            //grd_Dang
            //Arestas pendentes
            name = "grd_Dang";
            predicate = "Dangling = dom((Source |> DelV) \\/ (TargetG |> DelV))\\DelE";
            ruleEvent.addGuard(name, predicate);
            /*
             * -- Fim Arestas Pendentes --
             */

            /*
             * -- Novos Vértices --
             */
            //grd_new_v
            //Guarda para novos Vertices pertencerem ao dominio
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
             * Source e Target --
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
            predicate = "!e.e : Edge" + r.getName() + "=> mV(Source" + r.getName() + "(e)) = SourceG(mE(e)) and mV(Target" + r.getName() + "(e)) = TargetG(mE(e))";
            ruleEvent.addGuard(name, predicate);
            /*
             * -- Fim Compatibilidade de tipos de arestas, vértice e das funçoes
             * -- Source e Target --
             */

            /* ---------------------- *
             * -- Theoretical NACs -- *
             * ---------------------- */
            //Definir conjunto NAC e NACid
            if (!setTheoreticalNACs(ruleEvent, r)) {
                return false;
            }

            /* ------------------- *
             * -- Passo 3: THEN -- *
             * ------------------- */
                       
            /* --- Act_V --- */
            flag = 0;
            name = "act_V";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("VertG := (VertG\\DelV)\\/{");
            for (String n : createdNodes) {
                if (flag == 0)
                    flag = 1;
                else
                    stringBuilder.append(", ");
                stringBuilder.append("new_").append(n);
            }
            stringBuilder.append("}");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            /* --- Act_E --- */
            flag = 0;
            name = "act_E";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("EdgeG := (EdgeG\\DelE)\\/{");
            for (String e : createdEdges) {
                if (flag == 0)
                    flag = 1;
                else
                    stringBuilder.append(", ");
                stringBuilder.append("new_").append(e);
            }
            stringBuilder.append("}");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            //Needs further testing
            /* --- Act_src --- */
            name = "act_src";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("SourceG := (DelE <<| SourceG) \\/ \n{\n");
            flag = 0;
            for (Edge e : createdEdgesRef) {               
                //Testa se nodo fonte da aresta criada é também um nodo criado
                if (createdNodes.contains(e.getSource())) {
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                    stringBuilder.append("new_").append(e.getID()).append(" |-> ").append(e.getSource());
                }
                //Testa se nodo fonte é um preservado
                else if (preservedNodes.contains(r.getRHS().getMorphism().get(e.getSource()))) 
                {
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                    stringBuilder.append("new_").append(e.getID()).append(" |-> ").append(r.getRHS().getMorphism().get(e.getSource()));
                }
            }
            stringBuilder.append("\n}\n");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            //Needs further testing
            /* --- Act_tgt --- */
            name = "act_tgt";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("TargetG := (DelE <<| TargetG) \\/ \n{\n");
            flag = 0;
            for (Edge e : createdEdgesRef) {
                //Testa se nodo destino da nova aresta é um novo nodo
                if (createdNodes.contains(e.getTarget())) {
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                   stringBuilder.append("new_").append(e.getID()).append(" |-> ").append(e.getTarget());
                }
                //Testa se nodo destino é um preservado
                else if (preservedNodes.contains(r.getRHS().getMorphism().get(e.getTarget())))
                {
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                   stringBuilder.append("new_").append(e.getID()).append(" |-> ").append(r.getRHS().getMorphism().get(e.getTarget()));
                }                
            }
            stringBuilder.append("\n}");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            //Needs further testing
            /* --- Act_tV --- */
            name = "act_tV";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("tGV := (DelV <<| tGV \\/ \n{\n");
            flag = 0;
            for (Node n : createdNodesRef) {
                if (flag == 0) 
                    flag = 1;
                else
                    stringBuilder.append(", ");
                stringBuilder.append("new_").append(n.getID()).append(" |-> ").append(n.getType());
            }
            stringBuilder.append("\n}");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            //Needs further testing
            /* --- Act_tE --- */
            name = "act_tE";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("tGE := (DelE <<| tGE \\/ \n{\n");
            flag = 0;
            for (Edge e : createdEdgesRef) {
                if (flag == 0) {
                    flag = 1;
                }
                else {
                    stringBuilder.append(", ");
                }
                stringBuilder.append("new_").append(e.getID()).append(" |-> ").append(e.getType());
            }
            stringBuilder.append("\n}");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */
            
            //Add the event with all the defined guards and acts
            m.addEvent(ruleEvent);
        }
        return true;
    }

    /**
     * Done
     * Theoretical NACs - segundo definition 20
     *
     * @param r - regra para qual serão definidas as theoretical NACs
     * @return true || false de acordo com sucesso || insucesso da definição
     */
    private boolean setTheoreticalNACs(Event ruleEvent, Rule r) {
        String name;
        for (Graph NAC : r.getNACs()) {
            name = "grd_NAC" + r.getName() + NAC.getNACindex();
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("not(#");

            StringBuilder nodeSetString = new StringBuilder(1024);
            StringBuilder EdgeSetString = new StringBuilder(1024);
            int flag = 0;
            for (Node n : forbiddenVertices.values()) {
                if (flag == 0) {
                    flag = 1;
                }
                else {
                    nodeSetString.append(", ");
                }
                nodeSetString.append(n.getID());
            }
            flag = 0;
            for (Edge e : forbiddenEdges.values()) {
                if (flag == 0) {
                    flag = 1;
                }
                else {
                    nodeSetString.append(", ");
                }
                EdgeSetString.append(e.getID());
            }
            stringBuilder.append(nodeSetString.substring(0)).append(", ").append(EdgeSetString.substring(0)).append(".")
                    .append("{").append(nodeSetString.substring(0)).append("} <: VertG \\ mE [Vert").append(r.getName()).append("] and\n")
                    .append("{").append(EdgeSetString.substring(0)).append("} <: EdgeG \\ mE [Edge").append(r.getName()).append("] and\n");

            //Guarda que garante unicidade do ID dos vértices
            for (Node n1 : forbiddenVertices.values()) {
                for (Node n2 : forbiddenVertices.values()) {
                    flag = 1;
                    stringBuilder.append(n1.getID()).append("/=").append(n2.getID()).append(" and ");
                }
            }
            stringBuilder.append("\n");

            //Guarda que garante unicidade do ID das arestas
            for (Edge e1 : forbiddenEdges.values()) {
                for (Edge e2 : forbiddenEdges.values()) {
                    stringBuilder.append(e1.getID()).append("/=").append(e2.getID()).append(" and ");
                }
            }
            stringBuilder.append("\n");

            //Needs testing
            //Tipagem de vértices proibidos
            for (Node n1 : forbiddenVertices.values()) {
                String ID = null;
                ID = NAC.getMorphism().get(n1.getID());
                if (ID != null) {
                    stringBuilder.append("tGV(").append(n1.getID()).append(") = ").append(ID).append(" and ");
                }
            }
            stringBuilder.append("\n");

            //Needs testing
            //Tipagem de arestas proibidas
            flag = 0;
            for (Edge e1 : forbiddenEdges.values()) {
                String ID = null;
                ID = NAC.getMorphism().get(e1.getID());
                if (ID != null) {
                    stringBuilder.append("tGE(").append(e1.getID()).append(") = ").append(ID);
                    /* -- Source -- */
                    //if SourceLNACj(e) == v and v : NACjV
                    Node Source = forbiddenVertices.get(e1.getSource());
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(" and\n");
                    if (Source != null)
                        stringBuilder.append("SourceG(").append(e1.getID()).append(") = ").append(Source.getID());
                    else{
                        stringBuilder.append("SourceG(").append(e1.getID()).append(") = mV(").append(e1.getSource()).append(")");
                    }
                    /* -- Target -- */
                    Node Target = forbiddenVertices.get(e1.getTarget());
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(" and\n");
                    if (Source != null)
                        stringBuilder.append("TargetG(").append(e1.getID()).append(") = ").append(Target.getID());
                    else{
                        stringBuilder.append("TargetG(").append(e1.getID()).append(") = mV(").append(e1.getTarget()).append(")");
                    }
                }
            }
            stringBuilder.append(") or\n");

            //Needs testing
            /* -- Unicidade do match -- */
            ArrayList<Node> nodeList;
            nodeList = new ArrayList<>(forbiddenVertices.values());
            flag = 0;
            stringBuilder.append("(");
            for (int i = 0; i < nodeList.size()-1; i++){
                for (int j = i + 1; j < nodeList.size(); j++){
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(" or\n");
                    stringBuilder.append("mV(").append(nodeList.get(i).getID()).append(") /= mV(").append(nodeList.get(j)).append(")");
                }
            }
            stringBuilder.append(")");
            /* ------------------------ */

            //Needs testing
            /* -- Unicidade do match -- */
            ArrayList<Edge> EdgeList;
            EdgeList = new ArrayList<>(forbiddenEdges.values());
            flag = 0;
            stringBuilder.append("(");
            for (int i = 0; i < EdgeList.size(); i++){
                for (int j = i + 1; j < EdgeList.size(); j++){
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(" or\n");
                    stringBuilder.append("mE(").append(EdgeList.get(i).getID()).append(") /= mE(").append(EdgeList.get(j)).append(")");
                }
            }
            stringBuilder.append(")");
            /* ------------------------ */
            ruleEvent.addAct(name, stringBuilder.substring(0));
        }
        return true;
    }

    /* ----- Refinamento ----- */

    //Needs revision
    /**
     * DEFINITION 33
     * Função que realiza a tradução dos atributos de um grafo tipo
     * @param context - contexto sendo criado
     * @param g - gramática sendo traduzida
     * @return sucesso ou fracasso
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
       /* --------------- */

        /* --- Axioms --- */

        String name, predicate;

        /* -- Axm_AttrT -- */
        name = "axm_AttrT";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(AttrT");
        int flag = 0;
        for (NodeType n: attNodes.values()){
            stringBuilder.append(", {").append(n.getType()).append("}");
        }
        stringBuilder.append(")");
        context.addAxiom(new Axiom(name, stringBuilder.substring(0)));
        /* --------------- */

        /* --- Unicidade de Id dos Nodos com Atributos --- */
        ArrayList<NodeType> attNodesList = new ArrayList<>(attNodes.values());
        for (int i = 0; i < attNodes.size()-1; i++){
            for (int j = i + 1; j < attNodes.size(); j++){
                name = "axm_attrTDiff" + attNodesList.get(i).getType() + attNodesList.get(j).getType();
                context.addAxiom(
                        new Axiom(name,
                                (new StringBuilder())
                                        .append(attNodesList.get(i).getType())
                                        .append(" /= ")
                                        .append(attNodesList.get(j).getType())
                                        .substring(0)));
            }
        }
        /* --------------- */

        context.addAxiom(new Axiom("axm_attrvT", "attrvT : AttrT --> VertT"));

        /* --- axm_attrvT/def --- */
        name = "axm_attrvTdef";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(attrvT");
        for (int i = 0; i < attNodes.size(); i++){
            stringBuilder.append(", {").append(attNodes.get(i).getType()).append("}");
        }
        stringBuilder.append(")");
        context.addAxiom(new Axiom(name, stringBuilder.substring(0)));
        /* ---------------------- */

        return true;
    }

    /**
     * DEFINITION 34
     * Método que realiza a tradução e criação dos atributos de um grafo estado.
     * @param m - máquina a ser criada
     * @param c - contexto a ser criado
     * @param g - gramática a ser traduzida
     * @return - sucesso ou fracasse
     */
    private boolean stateGraphAttributesTranslation(Machine m, Context c, Grammar g){

        HashMap <String, Node> attNodes = g.getHost().getAttNodes();


        /* --- Variables --- */
        m.addVariable(new Variable("AttrG"));
        m.addVariable(new Variable("attrvG"));
        m.addVariable(new Variable("tGA"));
        for (Node n: attNodes.values()){
            for (AttributeType at: n.getAttributes()){
                m.addVariable(new Variable("valG" + at.getName()));
            }
        }
        /* ----------------- */

        /* --- Invariants --- */
        String name;
        String predicate;

        name = "inv_AttrG";
        predicate = "AttrG : POW(NAT)";
        m.addInvariant(name, new Invariant(name, predicate));

        name = "inv_attrvG";
        predicate = "attrvG : AttrG --> VertG";
        m.addInvariant(name, new Invariant(name, predicate));

        name = "inv_tgA";
        predicate = "tgA : AttrG --> AttrT";
        m.addInvariant(name, new Invariant(name, predicate));

        /* -- Cada atributo possue apenas 1 valor associado -- */
        ArrayList<Attribute> atts = new ArrayList<>();
        for (Node n: attNodes.values()){
            for (AttributeType at: n.getAttributes()){
                if (at instanceof Attribute){
                    atts.add((Attribute) at);
                }
            }
        }
        for (Attribute at1: atts){
            for (Attribute at2: atts){
                if (at1 != at2){
                    name = "inv_Diff" + at1.getID() + at2.getID();
                    predicate = "dom(valG" + at1.getID() + ") INTER dom(valG" + at2.getID() + ") = {}";
                    m.addInvariant(name, new Invariant(name, predicate));
                }
            }
        }
        /* --------------------------------------------------- */
        for (Attribute a: atts){
            name = "inv_type" + a.getID();
            predicate =  "!a . a : AttrG & a : dom(tgA |> {" + a.getID() + "}) => a : dom(valG" + a.getID() + ")";
            m.addInvariant(name, new Invariant(name, predicate));
        }
        /* ----------------- */

        /* --- Events --- */
        Event initialisation = new Event("INITIALISATION");

        name = "actAttrG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("AttrG := {");
        for (Attribute a: atts){
            stringBuilder.append(a.getID());
        }
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        name = "act_attrvG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("attrvG := {");
        for (Attribute a: atts){
            stringBuilder.append(a.getID());
        }
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));
        /* -------------- */

        return true;
    }

    /**
     * DEFINITION 35
     * Método que realiza a tradução de uma regra com atributos
     * @param m - máquina a ser criada
     * @param extendedContext - contexto a ser criado
     * @param r - regra a ser traduzida
     * @return - sucesso ou fracasso
     */
    private boolean attributedRuleTranslation(Machine m, Grammar g, Context extendedContext, Rule r, Event ruleEvent) {

        boolean attLHS = false, attRHS = false, attNACs = false;

        ArrayList<Graph> attributedNACsSet = new ArrayList<>();


        if (!r.getLHS().getAttNodes().isEmpty())
            attLHS = true;
        if (!r.getRHS().getAttNodes().isEmpty())
            attRHS = true;

        //Tests NACs for attributes
        for (Graph nac : r.getNACs()) {
            if (!nac.getAttNodes().isEmpty()) {
                attNACs = true;
                attributedNACsSet.add(nac);
            }
        }

        if (!attLHS && !attRHS && !attNACs) //There is no attribute on this rule
            return false;

        //There is an attribute on this rule

        /* -- Context -- */
        String name;
        // --- Sets
        extendedContext.addSet(new Set("AttrL"));

        // --- Axioms
        //Define Attr set for the rule
        name = "axm_attr" + r.getName();
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(Attr").append(r.getName());
        if (attLHS) {
            for (Node n : r.getLHS().getAttNodes().values())
                stringBuilder.append(",").append(n.getID());
        }
        if (attRHS) {
            for (Node n : r.getRHS().getAttNodes().values())
                stringBuilder.append(",").append(n.getID());
        }
        if (attNACs) {
            if (!attributedNACsSet.isEmpty()) {
                for (Graph nac : attributedNACsSet) {
                    for (Node n : nac.getAttNodes().values()) {
                        stringBuilder.append(",").append(n.getID());
                    }
                }
            }
        }
        extendedContext.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        //Define Attr domain and image
        name = "axm_attrv" + r.getName();
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder
                .append("attrv").append(r.getName())
                .append(" : Attr").append(r.getName())
                .append(" --> Vert").append(r.getName());
        extendedContext.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        //Define Attr domain and image
        name = "axm_attrv" + r.getName() + "def";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(attrv").append(r.getName());
        if (attLHS) {
            for (Node n : r.getLHS().getAttNodes().values())
                stringBuilder.append(",").append(n.getID());
        }
        if (attRHS) {
            for (Node n : r.getRHS().getAttNodes().values())
                stringBuilder.append(",").append(n.getID());
        }
        if (attNACs) {
            if (!attributedNACsSet.isEmpty()) {
                for (Graph nac : attributedNACsSet) {
                    for (Node n : nac.getAttNodes().values()) {
                        stringBuilder.append(",").append(n.getID());
                    }
                }
            }
        }
        extendedContext.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        //Define axm_t para tipagem
        stringBuilder.delete(0, stringBuilder.length());
        name = "axm_t" + r.getName() + "A";
        stringBuilder
                .append("t")
                .append(r.getName())
                .append("A : Attr")
                .append(r.getName())
                .append(" --> AttrT");
        extendedContext.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        //Define axm_tdef para tipagem
        name = "axm_t" + r.getName() + "Adef";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder
                .append("partition(t")
                .append(r.getName())
                .append("A");
        if (attLHS) {
            for (Node n : r.getLHS().getAttNodes().values())
                stringBuilder.append(",").append(n.getType());
        }
        if (attRHS) {
            for (Node n : r.getRHS().getAttNodes().values())
                stringBuilder.append(",").append(n.getType());
        }
        if (attNACs) {
            if (!attributedNACsSet.isEmpty()) {
                for (Graph nac : attributedNACsSet) {
                    for (Node n : nac.getAttNodes().values()) {
                        stringBuilder.append(",").append(n.getType());
                    }
                }
            }
        }
        extendedContext.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        /* -- Machine -- */
        // --- Preparation
        HashMap<String, AttributeType> LHSAttributes = new HashMap<>();
        HashMap<String, AttributeType> RHSAttributes = new HashMap<>();
        HashMap<String, AttributeType> NACAttributes = new HashMap<>();
        HashMap<String, AttributeType> RuleNewA = new HashMap<>();
        HashMap<String, AttributeType> RuleDelA = new HashMap<>();
        HashMap<String, AttributeType> forbiddenAttributes = new HashMap<>();


        //Constroi conjunto de atributos no lado esquerdo
        if (attLHS) {
            for (Node n : r.getLHS().getAttNodes().values())
                //AddAll Map Equivalent
                LHSAttributes.putAll(n.getAttributes()
                        .stream()
                        .collect(Collectors.toMap(
                                AttributeType::getID,
                                Function.identity())
                        )
                );
        }

        //Constroi conjunto de atributos no lado direito
        if (attRHS) {
            for (Node n : r.getRHS().getAttNodes().values())
                RHSAttributes.putAll(n.getAttributes()
                        .stream()
                        .collect(Collectors.toMap(
                                AttributeType::getID,
                                Function.identity())
                        )
                );
        }

        //Constroi conjunto de atributos de uma NAC
        for (Graph nac : attributedNACsSet) {
            for (Node n : nac.getAttNodes().values())
                NACAttributes.putAll(n.getAttributes()
                        .stream()
                        .collect(Collectors.toMap(
                                AttributeType::getID,
                                Function.identity())
                        )
                );
        }

        //Constroi conjunto de atributos deletados
        if (attLHS) {
            RuleDelA.putAll(LHSAttributes);
            RuleDelA.entrySet().removeAll(RHSAttributes.entrySet());
        }

        //Constroi conjunto de novos atributos
        if (attRHS) {
            RuleNewA.putAll(RHSAttributes);
            RuleNewA.entrySet().removeAll(LHSAttributes.entrySet());
        }

        //Constrói conjuntos de atributos proibidos de serem identificados (NACat - LHSat)
        if (attNACs) {
            forbiddenAttributes.putAll(NACAttributes);
            if (attLHS)
                forbiddenAttributes.entrySet().removeAll(LHSAttributes.entrySet());
        }

        // creates event to extend it
        Event ruleExtendEvent = new Event(r.getName());
        ruleExtendEvent.setExtend(ruleEvent);

        // --- ANY - Parameters
        ruleExtendEvent.addParameter("mA");
        ruleExtendEvent.addParameter("DelA");
        ruleExtendEvent.addParameter("DanglingA");
        for (AttributeType a : LHSAttributes.values()) {
            ruleExtendEvent.addParameter("new_" + a.getID());
        }
        for (AttributeType a : RHSAttributes.values()) {
            ruleExtendEvent.addParameter("new_" + a.getID());
        }
        for (AttributeType a : NACAttributes.values()) {
            ruleExtendEvent.addParameter("new_" + a.getID());
        }


        // --- WHERE - Guards
        //Match Attribute Edges - total function mapping attributed Edges
        name = "grd_mA";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder
                .append("mA : Attr")
                .append(r.getName())
                .append(" --> AttrG");
        ruleExtendEvent.addGuard(name, stringBuilder.substring(0));

        //Set of deleted attributes of G
        name = "grd_DelA";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("DelA := mA[");
        int flag = 0;
        for (AttributeType a : RuleDelA.values()) {
            if (flag == 0)
                flag = 1;
            else
                stringBuilder.append(", ");
            stringBuilder.append(a.getID());
        }
        stringBuilder.append("]");
        ruleExtendEvent.addGuard(name, stringBuilder.substring(0));

        //Dangling Attributes of G
        name = "grd_DangA";
        stringBuilder.delete(0, stringBuilder.length());
        //TODO: Revisar símbolos ASCII do EventB para expressão abaixo
        stringBuilder.append("DanglingA = dom((attrvG |> DelV) \\ DelA");
        ruleExtendEvent.addGuard(name, stringBuilder.substring(0));

        //Fresh ids of Attribute Edges
        for (AttributeType a : RuleNewA.values())
            //TODO: Revisar símbolos ASCII do EventB para expressão abaixo
            ruleExtendEvent.addGuard("grd_new_a" + a.getID(), "new_a" + a.getID() + " : NAT \\ AttrG");

        //Unicidade de ids para novos atributos
        for (int i = 0; i < RuleNewA.size() - 1; i++) {
            for (int j = i + 1; j < RuleNewA.size(); j++) {
                //TODO: Revisar símbolos ASCII do EventB para expressão abaixo
                ruleExtendEvent.addGuard(
                        "grd_diff_a" + RuleNewA.get(i).getID() + "a" + RuleNewA.get(j).getID(),
                        "new_a" + RuleNewA.get(i).getID() + " != new_a" + RuleNewA.get(j).getID());
            }
        }

        for (AttributeType at : LHSAttributes.values()) {
            //Compatibilidade entre atributos da regra e do grafo estado
            //TODO: revisar se é com atributos ou com os nodos atrelados
            ruleExtendEvent.addGuard(
                    "grd_attrv_" + at.getID(),
                    "mV(attrv" + r.getName() + "(" + at.getID() + ")) = attrvG(" + at.getID());

            //Compatibilidade de tipo entre atributos das regras e do grafo estado
            ruleExtendEvent.addGuard(
                    "grd_tA" + at.getID(),
                    "t" + r.getName() + "A(" + at.getID() + ") = tGA(mA(" + at.getID() + ")");
            //Compatibilidade de valores entre grafo tipo e variáveis
            //TODO: terminar implementação da guarda abaixo
            //ruleExtendEvent.addGuard("grd_val" + at.getID(), );
        }

        for (Graph NAC: attributedNACsSet){
            name = "grd_NAC" + r.getName() + NAC.getNACindex();

            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("{");
            flag = 0;
            for (Node n: NAC.getAttNodes().values()){
                for (AttributeType at: n.getAttributes()){
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                    stringBuilder.append(at.getID());
                }
            }
            stringBuilder.append("} ");

            //TODO: revisar símbolos ASCII do EventB para expressão abaixo
            stringBuilder
                    .append("C= AttrG \\ mA[Attr")
                    .append(r.getName())
                    .append("] AND\n");
            for (AttributeType at: forbiddenAttributes.values()){
                AttributeType ta = NACAttributes.get(at.getID());
                if (ta != null) {

                    stringBuilder
                            .append("tGA(").append(at.getID()).append(") = ")
                            .append(ta.getID()).append(" AND\n");

                    stringBuilder.append("valG").append(ta.getID()).append("(").append(at.getID()).append(") = ")
                            .append("valLNACjtLANACj").append(" AND\n");    //TODO: REVIEW VAL FUNCTION IN THIS EXPRESSION

                    //TODO: continue translation of attributed rule (def 35)
                }


            }

        }




        return true;
    }
    /**
     * Main para testes de conversão de Agg para GG e de GG para EventB
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String fullPath = "tests/R2C/R2C";
        String name = fullPath.split("/")[fullPath.split("/").length-1];


        /* -- Creates Directories -- */
        //Base
        File baseDir = new File(fullPath);
        baseDir.mkdirs();
        //Log
        File logDir = new File(fullPath + "/log");
        logDir.mkdirs();
        //Step2
        File rodinDir = new File(fullPath + "/out");
        rodinDir.mkdirs();


        /* -- Step1 - AGG to GG translation -- */
        //Creates Translator and Grammar
        AGGToGraphGrammar agg = new AGGToGraphGrammar();
        Grammar test = new Grammar(name);
        //Translates
        agg.aggReader(fullPath + ".ggx", test);
        //Logs
        test.printGrammar(logDir.getPath());

        /* -- Step 2 - GG to EventB translation -- */
        //Creates Translator and project
        GraphGrammarToEventB eventB = new GraphGrammarToEventB();
        Project newProject = new Project(name);
        //Translates
        eventB.translator(newProject, test);
        //Logs
        newProject.logProject(logDir.getPath(), rodinDir.getPath());

        System.out.println("Finished!");
    }
}
