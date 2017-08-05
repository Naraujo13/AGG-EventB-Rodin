/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tradutores;

import EventB.Set;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Nícolas Oreques de Araujo
 */
public class GraphGrammarToEventB {
    
    private LinkedHashMap<String, Node> forbiddenVertices = new LinkedHashMap<>();
    private LinkedHashMap<String, Edge> forbiddenEdges = new LinkedHashMap<>();
    private StringBuilder stringBuilder = new StringBuilder(1024);

    /**
     * Função que realiza a tradução de uma gramática de grafos para um projeto em notação event-B
     * @param p - projeto a ser criado
     * @param g - gramática a ser traduzida
     * @param DPO
     * @return - sucesso ou fracasso
     */
    private boolean translate(Project p, Grammar g, boolean DPO) {

        //Cria contexto
        Context c = new Context(g.getName() + Integer.toString(p.getContexts().size() + 1) + "ctx");

        //Cria machine
        Machine m = new Machine(g.getName() + Integer.toString(p.getMachines().size() + 1) + "mch", c);

        /* -- Tradução do Grafo Tipo -- */
        if (!typeGraphTranslation(c, g)) {
            return false;
        }

        /* -- Tradução de LHS e NACs, define também LHS para estratégia SPO -- */
       if (!rulePatternTranslation(c, g, DPO))
           return false;

        /* -- Tradução do Grafo Estado -- */
        if (!stateGraphTranslation(m, g))
            return false;

        /* -- Tradução da Aplicação de Regras para abordagem escolhida -- */
        if (!DPO) {
            //SPO Approach
            if (!ruleApplication(c, m, g))
                return false;
        }
        else{
            //DPO Approach
            if (!DPOApplication(m, g))
                return false;
        }

        /* -- Adiciona Elementos Traduzidos ao Projeto -- */
        p.addContext(c);
        p.addMachine(m);

        /* -- Refinamento -- */

        //Cria contexto
        Context cR = new Context(g.getName() + Integer.toString(p.getContexts().size() + 1) + "ctx");
        cR.addExtend(c);

        //Cria machine
        Machine mR = new Machine(g.getName() + Integer.toString(p.getMachines().size() + 1) + "mch", cR);
        mR.addRefinement(m);

        if (!attributedTypeGraphTranslation(cR, g))
            return false;

        if (!stateGraphAttributesTranslation(mR, g))
            return false;

        if (!attributedRuleTranslation(g, cR, mR))
            return false;

        /* -- Adiciona Novos elementos -- */
        p.addContext(cR);
        p.addMachine(mR);

        return true;
        
    }

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


    /**
     * DEFINITION 16
     * Método que realiza a tradução e criação do grafo estado e o insere em uma
     * máquina.
     * 100% Revised and working
     * @param machine - máquina a ser criada
     * @param grammar - gramática a ser traduzida
     * @return - sucesso ou fracasso da tradução
     */
    private boolean stateGraphTranslation(Machine machine, Grammar grammar) {
        /*
         * -- Adiciona variáveis do grafo estado --
         */
        //Vértices
        machine.addVariable(new Variable("VertG"));
        //Arestas
        machine.addVariable(new Variable("EdgeG"));
        //Fonte
        machine.addVariable(new Variable("SourceG"));
        //Destino
        machine.addVariable(new Variable("TargetG"));
        //Tipagem de vértices
        machine.addVariable(new Variable("tGV"));
        //Tipagem de Arestas
        machine.addVariable(new Variable("tGE"));

        /*
         * -- Adiciona invariantes do grafo estado --
         */
        //Auxiliares
        String name, predicate;

        //Invariante para Vertices pertencerem a partes dos naturais
        name = "inv_VertG";
        predicate = "VertG : POW(NAT)";
        machine.addInvariant(name, new Invariant(name, predicate));

        //Invariante para arestas pertencerem a partes dos naturais
        name = "inv_EdgeG";
        predicate = "EdgeG : POW(NAT)";
        machine.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir domínio da fonte de aresta para nodo (EdgeG->VertG)
        name = "inv_SourceG";
        predicate = "SourceG : EdgeG --> VertG";
        machine.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir domínio da destino de aresta para nodo (EdgeG->VertG)
        name = "inv_TargetG";
        predicate = "TargetG : EdgeG --> VertG";
        machine.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir tipagem de vértices de Vertice do estado para Vertice do tipo (VertG->VertT)
        name = "inv_tGV";
        predicate = "tGV : VertG --> VertT";
        machine.addInvariant(name, new Invariant(name, predicate));

        //Invariante para de definir tipagem de arestas de aresta do estado para aresta do tipo (EdgeG->EdgeT)
        name = "inv_tGE";
        predicate = "tGE : EdgeG --> EdgeT";
        machine.addInvariant(name, new Invariant(name, predicate));

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
        stringBuilder.append("VertG := {");
        flag = 0;
        for (Node n : grammar.getHost().getNodes()) {
            aux = n.getID().split("I");
            if (flag != 0)
                stringBuilder.append(", ");
            else
                flag = 1;
            stringBuilder.append(aux[1]);
        }
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para inicialização das arestas
        name = "act_EdgeG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("EdgeG := {");
        flag = 0;
        for (Edge e : grammar.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0)
                stringBuilder.append(", ");
            else
                flag = 1;
            stringBuilder.append(aux[1]);

        }
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para definir função Source
        name = "act_srcG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("SourceG := {");
        flag = 0;
        for (Edge e : grammar.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0)
                stringBuilder.append(", ");
            else
                flag = 1;
            stringBuilder.append(aux[1]).append("|->").append(e.getSource().replaceAll("I", ""));
        }        
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para definir função Target
        name = "act_tgtG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("TargetG := {");
        flag = 0;
        for (Edge e : grammar.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0)
                stringBuilder.append(", ");
            else
                flag = 1;
            stringBuilder.append(aux[1]).append("|->").append(e.getTarget().replaceAll("I", ""));
        }        
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para tipagem dos nodos
        name = "act_tGV";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("tGV := {");
        flag = 0;
        for (Node n : grammar.getHost().getNodes()) {
            aux = n.getID().split("I");
            if (flag != 0)
                stringBuilder.append(", ");
            else
                flag = 1;
            stringBuilder.append(aux[1]).append("|->").append(n.getType());
        }        
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Act para tipagem das arestas
        name = "act_tGE";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("tGE := {");
        flag = 0;
        for (Edge e : grammar.getHost().getEdges()) {
            aux = e.getID().split("I");
            if (flag != 0)
                stringBuilder.append(", ");
            else
                flag = 1;
            stringBuilder.append(aux[1]).append("|->").append(e.getType());
        }        
        stringBuilder.append("}");
        initialisation.addAct(name, stringBuilder.substring(0));

        //Adiciona evento
        machine.addEvent(initialisation);
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
    private boolean rulePatternTranslation(Context context, Grammar g, boolean DPO) {

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

            //Vert
            name = "axm_Vert" + r.getName();
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(Vert").append(r.getName());
            for (Node n : r.getLHS().getNodes()) {
                stringBuilder.append(", {").append(r.getName()).append(n.getID()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Edge
            name = "axm_Edge" + r.getName();
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(Edge").append(r.getName());
            for (Edge e : r.getLHS().getEdges()) {
                stringBuilder.append(", {").append(r.getName()).append(e.getID()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Source
            name = "axm_src" + r.getName();
            predicate = "Source" + r.getName() + " : " + "Edge" + r.getName() + " --> " + "Vert" + r.getName();
            context.addAxiom(new Axiom(name, predicate));

            //Target
            name = "axm_tgt" + r.getName();
            predicate = "Target" + r.getName() + " : " + "Edge" + r.getName() + " --> " + "Vert" + r.getName();
            context.addAxiom(new Axiom(name, predicate));

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

            if (!DPO) {
                //Define RHS da regra
                if (!RHSTranslation(context, r))
                    return false;
            }
            //Define NACs da regra
            if (!NACTranslation(context, r))
                return false;
        }
        return true;
    }

    public boolean RHSTranslation(Context context, Rule r){
            /* ----------------- *
             *   Passo 2: RHS    *
             * ----------------- */
            String RHSPrefix = r.getName() + "R";
            String name;
            String predicate;

            //Define 2 sets para representar conjunto de vértices e de arestas
            Set nodesR, EdgesR;
            nodesR = new Set("Vert" + RHSPrefix);
            EdgesR = new Set("Edge" + RHSPrefix);
            context.addSet(nodesR);
            context.addSet(EdgesR);

            //Define uma constante para cada nodo no LHS da regra
            for (Node n : r.getRHS().getNodes()) {
                context.addConstant(new Constant(RHSPrefix + n.getID()));

            }

            //Define uma constante para cada aresta no LHS da regra
            for (Edge e : r.getRHS().getEdges()) {
                context.addConstant(new Constant(RHSPrefix + e.getID()));
            }

            //Define constantes para funções Source e Target
            context.addConstant(new Constant("Source" + RHSPrefix));
            context.addConstant(new Constant("Target" + RHSPrefix));

            //Define duas constantes para represnetar a tipagem de arestas e nodos
            //no LHS.
            context.addConstant(new Constant("t" + RHSPrefix + "V"));
            context.addConstant(new Constant("t" + RHSPrefix + "E"));

            /*
             * -- Axiomas para vértices e arestas --
             */

            //Vert
            name = "axm_Vert" + RHSPrefix;
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(Vert").append(RHSPrefix);
            for (Node n : r.getRHS().getNodes()) {
                stringBuilder.append(", {").append(RHSPrefix).append(n.getID()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Edge
            name = "axm_Edge" + RHSPrefix;
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(Edge").append(RHSPrefix);
            for (Edge e : r.getRHS().getEdges()) {
                stringBuilder.append(", {").append(RHSPrefix).append(e.getID()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Source
            name = "axm_src" + RHSPrefix;
            predicate = "Source" + RHSPrefix + " : " + "Edge" + RHSPrefix + " --> " + "Vert" + RHSPrefix;
            context.addAxiom(new Axiom(name, predicate));

            //Target
            name = "axm_tgt" + RHSPrefix;
            predicate = "Target" + RHSPrefix + " : " + "Edge" + RHSPrefix + " --> " + "Vert" + RHSPrefix;
            context.addAxiom(new Axiom(name, predicate));

            //Define axiomas que representam a tipagem dos vértices e arestas
            name = "axm_t" + RHSPrefix + "V";
            predicate = "t" + RHSPrefix + "V : Vert" + RHSPrefix + " --> VertT";
            context.addAxiom(new Axiom(name, predicate));

            name = "axm_t" + RHSPrefix + "E";
            predicate = "t" + RHSPrefix + "E : Edge" + RHSPrefix + " --> EdgeT";
            context.addAxiom(new Axiom(name, predicate));

            //Define axiomas para definição das funções de tipagem
            //Nodos
            name = "axm_t" + RHSPrefix + "V_def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(t").append(RHSPrefix).append("V");
            for (Node n : r.getRHS().getNodes()) {
                stringBuilder.append(", {").append(RHSPrefix).append(n.getID()).append(" |-> ").append(n.getType()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Arestas
            name = "axm_t" + RHSPrefix + "E_def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(t").append(RHSPrefix).append("E");
            for (Edge e : r.getRHS().getEdges()) {
                stringBuilder.append(", {").append(RHSPrefix).append(e.getID()).append(" |-> ").append(e.getType()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));
        return true;
    }



    /**
     * DEFINIÇÃO 18
     * Método que realiza a tradução das NACs de uma regra
     * 100% Revised and working
     * @param context - contexto ao qual serão inseridas NACs
     * @param r - regra da qual serão traduzidas as NACs
     * @return - retorna true ou false, indicando sucesso ou falha do método
     */
    private boolean NACTranslation(Context context, Rule r) {

        //Montar conjunto NAC (proibidos)
        //Vertices
        HashSet<String> VertNAC = new HashSet<>();
        //Arestas
        HashSet<String> EdgeNAC = new HashSet<>();

        //Contador de controle das NACs
        int cont = 1;

        //NACV - Forbidden Vertices = NACv - (NACv intersecçao LHS)
        for (Graph NAC : r.getNACs()) {

            //Prefixo para cada NAC
            String NACPrefix = r.getName() + "NAC" + cont;

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
            context.addSet(new Set("Vert" + NACPrefix));
            context.addSet(new Set("Edge" + NACPrefix));

           /* -- Constantes -- */
            //VertLNACj
            for (Node n : NAC.getNodes()) {
                context.addConstant(new Constant(NACPrefix + n.getID()));
                
            }
            //EdgeLNACj
            for (Edge e : NAC.getEdges()) {
                context.addConstant(new Constant(NACPrefix + e.getID()));
            }

            //SourceLNACj
            context.addConstant(new Constant("Source" + NACPrefix));

            //TargetLNACj
            context.addConstant(new Constant("Target" + NACPrefix));

            //tLVNACj- tipagem nodos NAC->Tipo
            context.addConstant(new Constant("t" + NACPrefix + "V"));

            //tLENACj - tipagem arestas NAC->Tipo
            context.addConstant(new Constant("t" + NACPrefix + "E"));

            //ljV - morfismo nodos NAC->LHS
            context.addConstant(new Constant("l" + NACPrefix + "V"));

            //ljE - morfismo arestas NAC->LHS
            context.addConstant(new Constant("l" + NACPrefix + "E"));

            /* -- Axiomas -- */
            String name, predicate;

            //Vert
            name = "axm_Vert" + NACPrefix;
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(Vert").append(NACPrefix);
            for (Node n : NAC.getNodes()) {
                stringBuilder.append(", {").append(NACPrefix).append(n.getID()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Edge
            name = "axm_Edge" + NACPrefix;
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(Edge").append(NACPrefix);
            for (Edge e : NAC.getEdges()) {
                stringBuilder.append(", {").append(NACPrefix).append(e.getID()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Source
            name = "axm_src" + NACPrefix;
            predicate = "Source" + NACPrefix + " : " + "Edge" + NACPrefix + " --> " + "Vert" + NACPrefix;
            context.addAxiom(new Axiom(name, predicate));

            //Target
            name = "axm_tgt" + NACPrefix;
            predicate = "Target" + NACPrefix + " : " + "Edge" + NACPrefix + " --> " + "Vert" + NACPrefix;
            context.addAxiom(new Axiom(name, predicate));

            //tV
            name = "axm_t" + NACPrefix + "V";
            predicate = "t" + NACPrefix + "V : " + "Vert" + NACPrefix + " --> " + "VertT";
            context.addAxiom(new Axiom(name, predicate));

            //tV_def
            name = "axm_t" + NACPrefix + "V_def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(").append("t").append(NACPrefix).append("V");
            for (Node n : NAC.getNodes()) {
                stringBuilder.append(", {").append(NACPrefix).append(n.getID()).append(" |-> ").append(n.getType()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //tE
            name = "axm_t" + NACPrefix + "E";
            predicate = "t" + NACPrefix + "E : " + "Edge" + NACPrefix + " --> " + "EdgeT";
            context.addAxiom(new Axiom(name, predicate));

            //tE_def
            name = "axm_t" + NACPrefix + "E_def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(").append("t").append(NACPrefix).append("E");
            for (Edge e : NAC.getEdges()) {
                stringBuilder.append(", {").append(NACPrefix).append(e.getID()).append(" |-> ").append(e.getType()).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //axm_ljV
            name = "axm_l" + NACPrefix + "V";
            predicate = "l" + NACPrefix + "V : Vert" + NACPrefix + " --> Vert" + r.getName();
            context.addAxiom(new Axiom(name, predicate));

            //axm_ljV_def
            name = "axm_l" + NACPrefix + "V_def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(").append("l").append(NACPrefix).append("V");
            for (Node n : NAC.getNodes()) {
                String lID = NAC.getMorphism().get(n.getID());
                if (lID != null)
                    stringBuilder.append(", {").append(NACPrefix).append(n.getID()).append(" |-> ").append(r.getName()).append(lID).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //axm_ljE
            name = "axm_l" + NACPrefix + "E";
            predicate = "l" + NACPrefix + "E : Edge" + NACPrefix + " --> Edge" + r.getName();
            context.addAxiom(new Axiom(name, predicate));

            //axm_ljE_def
            name = "axm_l" + NACPrefix + "E_def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(").append("l").append(NACPrefix).append("E");
            for (Edge e : NAC.getEdges()) {
                String lID = NAC.getMorphism().get(e.getID());
                if (lID != null)
                    stringBuilder.append(", {").append(NACPrefix).append(e.getID()).append(" |-> ").append(r.getName()).append(lID).append("}");
            }
            stringBuilder.append(")");
            context.addAxiom(new Axiom(name, stringBuilder.substring(0)));
            
            cont++;
        }        
        return true;
    }

    private boolean ruleApplication(Context ctx, Machine machine, Grammar grammar) {

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
        for (Rule r : grammar.getRules()) {

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

            /* -------------------*
             * -- Passo 1: ANY ---*
             * -------------------*/

            /* -- Parâmetros  -- */
            ruleEvent.addParameter("mV");
            ruleEvent.addParameter("mE");
            ruleEvent.addParameter("DelV");
            ruleEvent.addParameter("PreservV");
            ruleEvent.addParameter("NewV");
            ruleEvent.addParameter("newV");
            ruleEvent.addParameter("DelE");
            ruleEvent.addParameter("Dangling");
            ruleEvent.addParameter("NewE");
            ruleEvent.addParameter("newE");

            /* -------------------*
             * --- FIM PASSO 1 ---*
             * -------------------*/

             /* -------------------*
             * -- Passo 2: WHERE -*
             * -------------------*/
            String RHSPrefix = r.getName() + "R";
            //Função total mapeando os vértices
            name = "grd_mV";
            predicate = "mV : Vert" + r.getName() + " --> VertG";
            ruleEvent.addGuard(name, predicate);

            //Função total mapeando as arestas
            name = "grd_mE";
            predicate = "mE : Edge" + r.getName() + " --> EdgeG";
            ruleEvent.addGuard(name, predicate);

            /* -- Vértices Excluídos -- */
            //Define o set de vértices excluídos
            for (Node n : r.getLHS().getNodes()) {
                deletedNodes.add(n.getID());
            }
            deletedNodes.removeAll(r.getRHS().getMorphism().values());

            name = "grd_DelV";
            int flag = 0;
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("DelV = mV[{");
            for (String n : deletedNodes) {
                if (flag != 0)
                    stringBuilder.append(", ");
                else
                    flag = 1;
                stringBuilder.append("{").append(r.getName()).append(n).append("}");
            }
            stringBuilder.append("}]");
            ruleEvent.addGuard(name, stringBuilder.substring(0));


            /* -- Vértices Preservados -- */
            //Define conjunto de vértices preservados
            name = "grd_PreV";
            predicate = "PreservV = VertG \\ DelV";
            ruleEvent.addGuard(name, predicate);

            /* -- Novos Vértices -- */
            name = "grd_NewV";
            predicate = "NewV <: NAT \\ VertG";
            ruleEvent.addGuard(name, predicate);

            /* -- Função Relacionando Novos Vértices com RHS -- */
            name = "grd_newV";
            flag = 0;
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("newV : NewV >->> {");
            for (String n : createdNodes) {
                if (flag != 0)
                    stringBuilder.append(", ");
                else
                    flag = 1;
                stringBuilder.append(RHSPrefix).append(n);
            }
            stringBuilder.append("}");
            ruleEvent.addGuard(name, stringBuilder.substring(0));


            /* -- Arestas Excluídas -- */
            //Define o set de arestas excluídas
            for (Edge e : r.getLHS().getEdges()) {
                deletedEdges.add(e.getID());
            }
            deletedEdges.removeAll(r.getRHS().getMorphism().values());
            stringBuilder.delete(0, stringBuilder.length());
            name = "grd_DelE";
            stringBuilder.append("DelE = mE[{");
            flag = 0;
            for (String e : deletedEdges) {
                if (flag != 0)
                    stringBuilder.append(", ");
                else
                    flag = 1;
                stringBuilder.append("{").append(r.getName()).append(e).append("}");
            }
            stringBuilder.append("}]");
            ruleEvent.addGuard(name, stringBuilder.substring(0));


            /* -- Arestas Penduradas -- */
            //grd_Dang
            //Arestas pendentes
            name = "grd_Dang";
            predicate = "Dangling = dom((SourceG |> DelV) \\/ (TargetG |> DelV))\\DelE";
            ruleEvent.addGuard(name, predicate);



            /* -- Novas Arestas -- */
            name = "grd_NewE";
            predicate = "NewE <: NAT \\ EdgeG";
            ruleEvent.addGuard(name, predicate);

            /* -- Função Relacionando Novas Arestas com RHS -- */
            name = "grd_newE";
            flag = 0;
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("newE : NewE >->> {e : {");
            for (String e : createdEdges) {
                if (flag != 0)
                    stringBuilder.append(", ");
                else
                    flag = 1;
                stringBuilder.append(RHSPrefix).append(e);
            }
            stringBuilder.append("} | ")
                    .append("(mV(Source").append(RHSPrefix).append("(e)) : PreservV or Source").append(RHSPrefix).append("(e) : newV[NewV]) &")
                    .append("(mV(Target").append(RHSPrefix).append("(e)) : PreservV or Target").append(RHSPrefix).append("(e) : newV[NewV])}");
            ruleEvent.addGuard(name, stringBuilder.substring(0));

            /* -- Tipagem Vértices -- */
            //grd_tV
            name = "grd_tV";
            predicate = "!v.v : Vert" + r.getName() + " => t" + r.getName() + "V(v) = tGV(mV(v))";
            ruleEvent.addGuard(name, predicate);

            /* -- Tipagem Arestas -- */
            //grd_tE
            name = "grd_tE";
            predicate = "!e.e : Edge" + r.getName() + " => t" + r.getName() + "E(e) = tGE(mE(e))";
            ruleEvent.addGuard(name, predicate);

            /* -- Tipagem Source e Target -- */
            //grd_srctgt
            name = "grd_srctgt";
            predicate = "!e.e : Edge" + r.getName() + " => mV(Source" + r.getName() + "(e)) = SourceG(mE(e)) & mV(Target" + r.getName() + "(e)) = TargetG(mE(e))";
            ruleEvent.addGuard(name, predicate);

            /* -- NACs Satisfaction -- */
            int count = 0;

            for (Graph NAC : r.getNACs()) {

                String NACPrefix = r.getName() + "NAC" + count;

                String forbiddenNodeList, forbiddenEdgeList;
                name = "grd_NAC" + count;

                //Nodelist
                stringBuilder.delete(0, stringBuilder.length());
                flag = 0;
                for (String n : forbiddenVertices.keySet()){
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                    stringBuilder.append(r.getName()).append(n);
                }
                forbiddenNodeList = stringBuilder.substring(0, stringBuilder.length());
                stringBuilder.delete(0, stringBuilder.length());

                //EdgeList
                stringBuilder.delete(0, stringBuilder.length());
                flag = 0;
                for (String e : forbiddenEdges.keySet()){
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                    stringBuilder.append(r.getName()).append(e);
                }
                forbiddenEdgeList = stringBuilder.substring(0, stringBuilder.length());
                stringBuilder.delete(0, stringBuilder.length());

                //Predicate
                stringBuilder.delete(0, stringBuilder.length());
                stringBuilder.append("not(#");
                if (forbiddenNodeList != null)
                    stringBuilder.append(forbiddenNodeList);
                if (forbiddenNodeList != null && forbiddenEdgeList != null)
                    stringBuilder.append(", ");
                if (forbiddenEdgeList != null)
                    stringBuilder.append(forbiddenEdgeList);
                stringBuilder.append(" . ");

                //Dominio dos nodos
                stringBuilder.append("{").append(forbiddenNodeList).append("}")
                        .append(" <: VertG \\ mV[Vert").append(r.getName()).append("] & ");
                //Dominio das Arestas
                stringBuilder.append("{").append(forbiddenEdgeList).append("}")
                        .append(" <: EdgeG \\ mE[Edge").append(r.getName()).append("] & ");

                //Unicidade de identificação dos nodos proibidos
                flag = 0;
                for (String node1 : forbiddenVertices.keySet()){
                    for(String node2 : forbiddenVertices.keySet()) {
                        if (!node1.equals(node2)) {
                            if (flag == 0)
                                flag = 1;
                            else
                                stringBuilder.append(" & ");
                            stringBuilder.append(node1).append(" /= ").append(node2);
                        }
                    }
                }

                //Unicidade de identificação das arestas proibidos
                flag = 0;
                for (String edge1 : forbiddenEdges.keySet()){
                    for(String edge2 : forbiddenEdges.keySet()) {
                        if (!edge1.equals(edge2)) {
                            if (flag == 0)
                                flag = 1;
                            else
                                stringBuilder.append(" & ");
                            stringBuilder.append(edge1).append(" /= ").append(edge2);
                        }
                    }
                }
                stringBuilder.append(" & ");

                //Tipagem de Vértices Proibidos
                for (Node n : forbiddenVertices.values()){
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(" & ");
                    stringBuilder.append("tGV(").append(n.getID()).append(") = ").append(n.getType());
                }


                for (Edge e : forbiddenEdges.values()){
                    //Tipagem de Arestas Proibidas
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(" & ");
                    stringBuilder.append("tGE(").append(e.getID()).append(") = ").append(e.getType());

                    //Tipagem de Source das Arestas Proibidos
                    boolean containsValue = NAC.getMorphism().containsValue(e.getSource());
                    if (containsValue) {
                        stringBuilder.append(" & ");
                        String source = NAC.getMorphism().get(e.getID());
                        stringBuilder.append("SourceG(").append(e.getID()).append(") = ").append(source);
                    }

                    //Tipagem de Target das Arestas Proibidos
                    containsValue = NAC.getMorphism().containsValue(e.getTarget());
                    if (containsValue) {
                        stringBuilder.append(" & ");
                        String target = NAC.getMorphism().get(e.getTarget());
                        stringBuilder.append("SourceG(").append(e.getID()).append(") = ").append(target);
                    }
                }
                stringBuilder.append(") or ");



                //Dominio das arestas
//                predicate = "not(#nV, nE . nV : Vert" + NACPrefix + " --> VertG & ";
//                predicate += "nE : Edge" + NACPrefix + " --> EdgeG & ";
//                predicate += "l" + NACPrefix + "V[Vert" + r.getName() + "] <<| Vert" + NACPrefix + "\\"
//                        + "l" + NACPrefix + "V[Vert" + r.getName() + "] >-> VertG \\ mV[Vert" + r.getName() + "] & ";
//                predicate += "(#v . v : Vert" + NACPrefix + "=> t" + NACPrefix + "V(v) = tGV(nV(v))) & ";
//                predicate += "(#e . e : Edge" + NACPrefix + "=> t" + NACPrefix + "E(e) = tGE(nE(e))) & ";
//                predicate += "(#e . e : Edge" + NACPrefix + "=> nV(Source" + NACPrefix + "(e)) = sourceG(nE(e)) & ";
//                predicate += "nV(Target" + NACPrefix + "(e)) = targetG(nE(e))) & ";
//                predicate += "nV circ l" + NACPrefix + "V = mV & circ l" + NACPrefix + "E = mE)";
                ruleEvent.addGuard(name, predicate);

                count++;
            }

            /* ------------------- *
             * -- Passo 3: THEN -- *
             * ------------------- */

            /* --- Act_V --- */
            name = "act_V";
            predicate =  "VertG := (VertG \\ DelV) \\/ NewV";
            ruleEvent.addAct(name, predicate);

            /* --- Act_E --- */
            name = "act_E";
            stringBuilder.delete(0, stringBuilder.length());
            predicate =  "EdgeG := (EdgeG \\ (DelE \\/ Dangling)) \\/ NewE";
            ruleEvent.addAct(name, predicate);

            /* --- Act_src --- */
            name = "act_src";
            predicate =  "SourceG := ((DelE \\/ Dangling) <<| SourceG) \\/ " +
                    "{ e |-> mV(Source" + RHSPrefix + "(newE(e))) | e : NewE & Source" + RHSPrefix + "(newE)) : PreservV} \\/ " +
                    "{ e |-> Source" + RHSPrefix + "(newE(e)) | e : NewE & Source" + RHSPrefix + "(newE(e)) : newV[NewV]}";
            ruleEvent.addAct(name, predicate);

            /* --- Act_tgt --- */
            name = "act_tgt";
            predicate =  "TargetG := ((DelE \\/ Dangling) <<| TargetG) \\/ " +
                    "{ e |-> mV(Target" + RHSPrefix + "(newE(e))) | e : NewE & Target" + RHSPrefix + "(newE)) : PreservV} \\/ " +
                    "{ e |-> Target" + RHSPrefix + "(newE(e)) | e : NewE & Target" + RHSPrefix + "(newE(e)) : newV[NewV]}";
            ruleEvent.addAct(name, predicate);

             /* --- Act_tV --- */
            name = "act_tV";
            predicate =  "tGV := (DelV <<| tGV) \\/ {v |-> t" + RHSPrefix + "V(newV[NewV]) | v : NewV}";
            ruleEvent.addAct(name, predicate);

            /* --- Act_tE --- */
            name = "act_tE";
            predicate =  "tGE := ((DelE \\/ Dangling) <<| tGE) \\/ {e |-> t" + RHSPrefix + "E(newE[NewE]) | e : NewE}";
            ruleEvent.addAct(name, predicate);

            //Add the event with all the defined guards and acts
            machine.addEvent(ruleEvent);
        }
        return true;
    }



    /**
     * DEFINITIION 22
     * 100% Revised and working
     * Método que define a aplicação de regras. Cria os eventos e outros ele-
     * mentos necessários para aplicação das regras
     * @param machine - máquina destino
     * @param grammar - gramátic a fonte
     * @return - true/false indicando sucesso/falha na operação de tradução
     */
    private boolean DPOApplication(Machine machine, Grammar grammar) {
        
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
        for (Rule r : grammar.getRules()) {
            
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

            /* -------------------*
             * -- Passo 1: ANY ---*
             * -------------------*/

            /* -- Parâmetros  -- */
            ruleEvent.addParameter("mV");
            ruleEvent.addParameter("mE");
            ruleEvent.addParameter("DelV");
            ruleEvent.addParameter("PreservV");
            ruleEvent.addParameter("DelE");
            ruleEvent.addParameter("Dangling");

            //Nodos
            for (String n : createdNodes) {
                ruleEvent.addParameter("newV_" + n);
            }
            //Arestas
            for (String e : createdEdges) {
                ruleEvent.addParameter("newE_" + e);
            }

            /* -------------------*
             * --- FIM PASSO 1 ---*
             * -------------------*/

             /* -------------------*
             * -- Passo 2: WHERE -*
             * -------------------*/
            //Função total mapeando os vértices
            name = "grd_mV";
            predicate = "mV : Vert" + r.getName() + " --> VertG";
            ruleEvent.addGuard(name, predicate);

            //Função total mapeando as arestas
            name = "grd_mE";
            predicate = "mE : Edge" + r.getName() + " --> EdgeG";
            ruleEvent.addGuard(name, predicate);

            /* -- Vértices Excluídos -- */
            //Define o set de vértices excluídos
            for (Node n : r.getLHS().getNodes()) {
                deletedNodes.add(n.getID());
            }
            deletedNodes.removeAll(r.getRHS().getMorphism().values());

            name = "grd_DelV";
            int flag = 0;
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("DelV = mV[{");
            for (String n : deletedNodes) {
                if (flag != 0)
                    stringBuilder.append(", ");
                else
                    flag = 1;
                stringBuilder.append(r.getName()).append(n);
            }
            stringBuilder.append("}]");
            ruleEvent.addGuard(name, stringBuilder.substring(0));


            /* -- Vértices Preservados -- */
            //Define conjunto de vértices preservados
            name = "grd_PreV";
            predicate = "PreservV = VertG \\ DelV";
            ruleEvent.addGuard(name, predicate);


            /* -- Arestas Excluídas -- */
            //Define o set de arestas excluídas
            for (Edge e : r.getLHS().getEdges()) {
                deletedEdges.add(e.getID());
            }
            deletedEdges.removeAll(r.getRHS().getMorphism().values());
            stringBuilder.delete(0, stringBuilder.length());
            name = "grd_DelE";
            stringBuilder.append("DelE = mE[{");
            flag = 0;
            for (String e : deletedEdges) {
                if (flag != 0)
                    stringBuilder.append(", ");
                else
                    flag = 1;
                stringBuilder.append(r.getName()).append(e);
            }
            stringBuilder.append("}]");
            ruleEvent.addGuard(name, stringBuilder.substring(0));


            /* -- Arestas Penduradas -- */
            //grd_Dang
            //Arestas pendentes
            name = "grd_Dang";
            predicate = "Dangling = dom((SourceG |> DelV) \\/ (TargetG |> DelV))\\DelE";
            ruleEvent.addGuard(name, predicate);


            //* -- Novos Vértices -- */
            //grd_new_v
            //Guarda para novos Vertices pertencerem ao dominio
            for (String n : createdNodes) {
                name = "grd_newV_" + n;
                predicate = "newV_" + n + " : NAT \\ VertG";
                ruleEvent.addGuard(name, predicate);
            }


            /* -- Novas Arestas -- */
            //grd_new_e
            //Guarda para novas arestas pertencerem ao dominio
            for (String e : createdEdges) {
                name = "grd_newE_" + e;
                predicate = "newE_" + e + " : NAT \\ EdgeG";
                ruleEvent.addGuard(name, predicate);
            }


            /* -- Unicidade de ID em Novos Vértices -- */
            //grd_diffvivj
            for (String vi : createdNodes) {
                for (String vj : createdNodes) {
                    if (!vi.equals(vj)) {
                        name = "grd_diff" + vi + vj;
                        predicate = "newV_" + vi + " /= " + "new_" + vj;
                        ruleEvent.addGuard(name, predicate);
                    }
                }
            }

           /* -- Unicidade de ID Arestas -- */
            //grd_diffeiej
            for (String ei : createdEdges) {
                for (String ej : createdEdges) {
                    if (!ei.equals(ej)) {
                        name = "grd_diff" + ei + ej;
                        predicate = "newE_" + ei + " /= " + "new_" + ej;
                        ruleEvent.addGuard(name, predicate);
                    }
                }
            }


            /* -- Tipagem Vértices -- */
            //grd_tV
            name = "grd_tV";
            predicate = "!v.v : Vert" + r.getName() + " => t" + r.getName() + "V(v) = tGV(mV(v))";
            ruleEvent.addGuard(name, predicate);

            /* -- Tipagem Arestas -- */
            //grd_tE
            name = "grd_tE";
            predicate = "!e.e : Edge" + r.getName() + " => t" + r.getName() + "E(e) = tGE(mE(e))";
            ruleEvent.addGuard(name, predicate);

            /* -- Tipagem Source e Target -- */
            //grd_srctgt
            name = "grd_srctgt";
            predicate = "!e.e : Edge" + r.getName() + " => mV(Source" + r.getName() + "(e)) = SourceG(mE(e)) & mV(Target" + r.getName() + "(e)) = TargetG(mE(e))";
            ruleEvent.addGuard(name, predicate);

            /* ---------------------- *
             * -- Theoretical NACs -- *
             * ---------------------- */
            //Definir conjunto NAC e NACid
            if (!setTheoreticalNACs(ruleEvent, r))
                return false;

            /* -- Condição de Identificação de Vértices 1 -- */
            if (!preservedNodes.isEmpty()) {
                name = "grd_Ident1V";
                stringBuilder.delete(0, stringBuilder.length());
                stringBuilder.append("DelV /\\ mV[{");
                preservedNodes.forEach((vertName) -> stringBuilder.append(r.getName()).append(vertName).append(", "));
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                stringBuilder.append("}] = {}");
                ruleEvent.addGuard(name, stringBuilder.substring(0, stringBuilder.length()));
            }

            /* -- Condição de Identificação de Vértices 2 -- */
            if (!deletedNodes.isEmpty()) {
                name = "grd_Ident2V";
                stringBuilder.delete(0, stringBuilder.length());
                stringBuilder.append("card(DelV) = card({");
                deletedNodes.forEach((vertName) -> stringBuilder.append(r.getName()).append(vertName).append(", "));
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                stringBuilder.append("})");
                ruleEvent.addGuard(name, stringBuilder.substring(0, stringBuilder.length()));
            }

            /* -- Condição de Identificação de Arestas 1 -- */
            if (!preservedEdges.isEmpty()) {
                name = "grd_Ident1E";
                stringBuilder.delete(0, stringBuilder.length());
                stringBuilder.append("DelE /\\ mE[{");
                preservedEdges.forEach((edgeName) -> stringBuilder.append(r.getName()).append(edgeName).append(", "));
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                stringBuilder.append("}] = {}");
                ruleEvent.addGuard(name, stringBuilder.substring(0, stringBuilder.length()));
            }

            /* -- Condição de Identificação de Arestas 2 -- */
            if (!deletedEdges.isEmpty()) {
                name = "grd_Ident2E";
                stringBuilder.delete(0, stringBuilder.length());
                stringBuilder.append("card(DelE) = card({");
                deletedEdges.forEach((edgeName) -> stringBuilder.append(r.getName()).append(edgeName).append(", "));
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
                stringBuilder.append("})");
                ruleEvent.addGuard(name, stringBuilder.substring(0, stringBuilder.length()));
            }

            /* -- Condição de Arestas Penduradas -- */
            predicate = "Dangling = {}";
            ruleEvent.addGuard(name, predicate);


            /* ------------------- *
             * -- Passo 3: THEN -- *
             * ------------------- */

            /* --- Act_V --- */
            flag = 0;
            name = "act_V";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("VertG := (VertG\\DelV)\\/ {");
            for (String n : createdNodes) {
                if (flag == 0)
                    flag = 1;
                else
                    stringBuilder.append(", ");
                stringBuilder.append("newV_").append(n);
            }
            stringBuilder.append("}");
            ruleEvent.addAct(name, stringBuilder.substring(0));

            /* --- Act_E --- */
            flag = 0;
            name = "act_E";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("EdgeG := (EdgeG\\DelE)\\/ {");
            for (String e : createdEdges) {
                if (flag == 0)
                    flag = 1;
                else
                    stringBuilder.append(", ");
                stringBuilder.append("newE_").append(e);
            }
            stringBuilder.append("} ");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            /* --- Act_src --- */
            name = "act_src";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("SourceG := (DelE <<| SourceG) \\/ {");
            flag = 0;
            for (Edge e : createdEdgesRef) {
                //Testa se nodo fonte da aresta criada é também um nodo criado
                if (createdNodes.contains(e.getSource())) {
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                    stringBuilder.append("newE_").append(e.getID()).append(" |-> newV_").append(e.getSource());
                }
                //Testa se nodo fonte é um preservado
                else if (preservedNodes.contains(r.getRHS().getMorphism().get(e.getSource())))
                {
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                    stringBuilder.append("newE_").append(e.getID()).append(" |-> ").append("mV(")
                            .append(r.getName()).append(r.getRHS().getMorphism().get(e.getSource())).append(")");
                }
            }
            stringBuilder.append("} ");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            /* --- Act_tgt --- */
            name = "act_tgt";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("TargetG := (DelE <<| TargetG) \\/ {");
            flag = 0;
            for (Edge e : createdEdgesRef) {
                //Testa se nodo destino da nova aresta é um novo nodo
                if (createdNodes.contains(e.getTarget())) {
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                   stringBuilder.append("newE_").append(e.getID()).append(" |-> newV_").append(e.getTarget());
                }
                //Testa se nodo destino é um preservado
                else if (preservedNodes.contains(r.getRHS().getMorphism().get(e.getTarget())))
                {
                    if (flag == 0)
                        flag = 1;
                    else
                        stringBuilder.append(", ");
                   stringBuilder.append("newE_").append(e.getID()).append(" |-> ").append("mV(")
                           .append(r.getName()).append(r.getRHS().getMorphism().get(e.getTarget())).append(")");
                }
            }
            stringBuilder.append("} ");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            //Needs further testing
            /* --- Act_tV --- */
            name = "act_tV";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("tGV := (DelV <<| tGV) \\/ {");
            flag = 0;
            for (Node n : createdNodesRef) {
                if (flag == 0)
                    flag = 1;
                else
                    stringBuilder.append(", ");
                stringBuilder.append("newV_").append(n.getID()).append(" |-> ").append(n.getType());
            }
            stringBuilder.append("} ");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            //Needs further testing
            /* --- Act_tE --- */
            name = "act_tE";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("tGE := (DelE <<| tGE) \\/ {");
            flag = 0;
            for (Edge e : createdEdgesRef) {
                if (flag == 0) {
                    flag = 1;
                }
                else {
                    stringBuilder.append(", ");
                }
                stringBuilder.append("newE_").append(e.getID()).append(" |-> ").append(e.getType());
            }
            stringBuilder.append("} ");
            ruleEvent.addAct(name, stringBuilder.substring(0));
            /* ------------- */

            //Add the event with all the defined guards and acts
            machine.addEvent(ruleEvent);
        }
        return true;
    }

    /**
     * Theoretical NACs - segundo definition 20
     * 100% Revised and working
     * @param r - regra para qual serão definidas as theoretical NACs
     * @return true || false de acordo com sucesso || insucesso da definição
     */
    private boolean setTheoreticalNACs(Event ruleEvent, Rule r) {
        String name;
        for (Graph NAC : r.getNACs()) {

            //Prefixo para cada NAC
            String NACPrefix = r.getName() + "NAC" + NAC.getNACindex();


            LinkedHashSet<String> forbiddenIdentificationVertices = new LinkedHashSet<>();
            LinkedHashSet<String> forbiddenIdentificationEdges = new LinkedHashSet<>();

            LinkedHashMap<String, String> forbiddenIdAux = new LinkedHashMap<>();  //Map de  LHSv -> NACv ou LHSe -> NACe

            forbiddenVertices.clear();
            forbiddenEdges.clear();

            //Prepara vértices proibidos
            for (Node NACv : NAC.getNodes()) {
                String LHSv = NAC.getMorphism().get(NACv.getID());
                if (LHSv == null)
                    forbiddenVertices.put(NACv.getID(), NACv);
                else{
                    //Prepara Vértices proibidos de identificação
                    if(forbiddenIdAux.containsKey(LHSv)) {
                        forbiddenIdAux.forEach((k, v)->{
                            if (k.equals(LHSv) &&  !v.equals(LHSv))
                                forbiddenIdentificationVertices.add("{" + v + "," + NACv + "}");
                        });
                    }
                    forbiddenIdAux.put(LHSv, NACv.getID());
                }
            }

            forbiddenIdAux.clear();
            //Prepara arestas proibidas
            for (Edge NACe : NAC.getEdges()) {
                String LHSe = NAC.getMorphism().get(NACe.getID());
                if (LHSe == null)
                    forbiddenEdges.put(NACe.getID(), NACe);
                else{
                    //Prepara Arestas proibidas de identificação
                    if(forbiddenIdAux.containsKey(LHSe)) {
                        forbiddenIdAux.forEach((k, v)->{
                            if (k.equals(LHSe) &&  !v.equals(LHSe))
                                forbiddenIdentificationEdges.add("{" + v + "," + NACe + "}");
                        });
                    }
                    forbiddenIdAux.put(LHSe, NACe.getID());
                }
            }

            if (forbiddenVertices.isEmpty() && forbiddenEdges.isEmpty())
                return true;

            //inicio de atualização de definição para utilização de asg
//            String NACPrefix = r.getName() + "NAC" + NAC.getNACindex();
//            /* -- Cria NACjV e NACjE -- */
//            ruleEvent.addParameter("forbidden" + NACPrefix + "V");    //Vertices proibidos
//            ruleEvent.addParameter("forbidden" + NACPrefix + "E");    //Arestas proibidas
//
//            /* -- Define NACjV e NACjE -- */
//            String predicate;
//            name = "grdNAC" +  r.getName() + NAC.getNACindex() + "V";
//            predicate = "Vert" + r.getName() + "NAC" + NAC.getNACindex() + " \\ " + "l" + NACPrefix + "V" ;
//            ruleEvent.addGuard(name, predicate);
//
//            name = "grdNAC" +  r.getName() + NAC.getNACindex() + "V";
//            predicate = "Vert" + r.getName() + "NAC" + NAC.getNACindex() + " \\ " + "l" + NACPrefix + "V" ;
//            ruleEvent.addGuard(name, predicate);



            /* -- grd_NAC -- */
            name = "grd_" + r.getName() + "NAC" + NAC.getNACindex();
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("not(# ");

            StringBuilder nodeSetString = new StringBuilder(1024);
            StringBuilder edgeSetString = new StringBuilder(1024);
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
                    edgeSetString.append(", ");
                }
                edgeSetString.append(e.getID());
            }

            if (nodeSetString.length() > 0) {
                stringBuilder.append(nodeSetString.substring(0));
                if (edgeSetString.length() > 0)
                    stringBuilder.append(", ");
            }

            if (edgeSetString.length() > 0)
                stringBuilder.append(edgeSetString.substring(0));

            stringBuilder.append(" . ");

            if(nodeSetString.length() > 0)
                    stringBuilder.append("{").append(nodeSetString.substring(0)).append("} <: VertG \\ mV [Vert").append(r.getName()).append("] & ");

            if (edgeSetString.length() > 0)
                    stringBuilder.append("{").append(edgeSetString.substring(0)).append("} <: EdgeG \\ mE [Edge").append(r.getName()).append("] & ");

            //Guarda que garante unicidade do ID dos vértices
            for (Node n1 : forbiddenVertices.values()) {
                for (Node n2 : forbiddenVertices.values()) {
                    if (!n1.getID().equals(n2.getID()))
                        stringBuilder.append(n1.getID()).append("/=").append(n2.getID()).append(" & ");
                }
            }

            //Guarda que garante unicidade do ID das arestas
            for (Edge e1 : forbiddenEdges.values()) {
                for (Edge e2 : forbiddenEdges.values()) {
                    if (!e1.getID().equals(e2.getID()))
                        stringBuilder.append(e1.getID()).append("/=").append(e2.getID()).append(" & ");
                }
            }

            //Tipagem de vértices proibidos
            for (Node n : forbiddenVertices.values()) {
                stringBuilder.append("(tGV(").append(n.getID()).append(") = ").append(n.getType()).append(") & ");
            }

            //Tipagem de arestas proibidas
            for (Edge e : forbiddenEdges.values()) {
                stringBuilder.append("(tGE(").append(e.getID()).append(") = ").append(e.getType()).append(") & ");
                /* -- Source -- */
                Node Source = forbiddenVertices.get(e.getSource());

                if (Source != null) //Se source também é um proibido
                    stringBuilder.append("(SourceG(").append(e.getID()).append(") = ").append(Source.getID()).append(") & ");
                else{ //Se source não é um proibido
                    String source = NAC.getMorphism().get(e.getSource());
                    stringBuilder.append("(SourceG(").append(e.getID()).append(") = mV(").append(r.getName()).append(source).append(")").append(") & ");

                }
                /* -- Target -- */
                Node Target = forbiddenVertices.get(e.getTarget());
                if (Target != null) //Se target também é um proibido
                    stringBuilder.append("(TargetG(").append(e.getID()).append(") = ").append(Target.getID()).append(") & ");
                else {    //Se target não é um proibido
                    String target = NAC.getMorphism().get(e.getTarget());
                    stringBuilder.append("(TargetG(").append(e.getID()).append(") = mV(").append(r.getName()).append(target).append(")) & ");
                }
            }
            stringBuilder.delete(stringBuilder.length()-3, stringBuilder.length());
            stringBuilder.append(") ");


            /* -- Unicidade do match -- */
            if (!forbiddenIdentificationVertices.isEmpty()) {
                flag = 0;
                stringBuilder.append(" or (");
                for (String n1 : forbiddenIdentificationVertices) {
                    for (String n2 : forbiddenIdentificationVertices) {
                        if (!n1.equals(n2)) {
                            if (flag == 0)
                                flag = 1;
                            else
                                stringBuilder.append(" or ");
                            stringBuilder.append("mV(").append(NACPrefix).append(n1)
                                    .append(") /= mV(")
                                    .append(NACPrefix).append(n2).append(")");
                        }
                    }
                }
                stringBuilder.append(")");
            }
            /* ------------------------ */


            /* -- Unicidade do match -- */
            if (!forbiddenIdentificationEdges.isEmpty()) {
                flag = 0;
                stringBuilder.append(" or (");
                for (String e1 : forbiddenIdentificationEdges) {
                    for (String e2 : forbiddenIdentificationEdges) {
                        if (!e1.equals(e2)) {
                            if (flag == 0)
                                flag = 1;
                            else
                                stringBuilder.append(" or ");
                            stringBuilder.append("mE(").append(NACPrefix).append(e1)
                                    .append(") /= mE(")
                                    .append(NACPrefix).append(e2).append(")");
                        }
                    }
                }
                stringBuilder.append(")");
            }
            /* ------------------------ */
            ruleEvent.addGuard(name, stringBuilder.substring(0));
        }
        return true;
    }

    /* ----- Refinamento ----- */


    /**
     * DEFINITION 32
     * Função que realiza a tradução dos atributos de um grafo tipo
     * 100% Revised and working
     * @param context - contexto sendo criado
     * @param g - gramática sendo traduzida
     * @return sucesso ou fracasso
     */
    private boolean attributedTypeGraphTranslation(Context context, Grammar g) {

        //Cria LinkedHashMap com atributos
        LinkedHashMap<String, AttributeType> attTypes = new LinkedHashMap<>();
        g.getTypeGraph().getAttNodes().values().forEach((nt)->
                nt.getAttributes().forEach((at)->
                        attTypes.put(at.getID(), at)));


        if (attTypes.isEmpty())
            return false;

        /* -- Sets -- */
        context.addSet(new Set("AttrT"));   //Conjunto com Tipos de Atributos
        context.addSet(new Set("DataType"));

       /* -- Constants -- */
        context.addConstant(new Constant("attrvT"));
        context.addConstant(new Constant("valT"));

       //Define Sort para tipos default
        context.addConstant(new Constant("NatSort"));
        context.addConstant(new Constant("BoolSort"));


       /* -- Attribute Types --*/
       attTypes.keySet().forEach((atTypeID) -> context.addConstant(new Constant("at" + atTypeID)));

        /* --- Axioms --- */
        String name, predicate;

        /* -- Axm_AttrT -- */
        name = "axm_AttrT";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(AttrT, ");
        attTypes.keySet().forEach((atTypeID)->
                stringBuilder.append("{").append("at").append(atTypeID).append("}, "));
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
        stringBuilder.append(")");
        context.addAxiom(new Axiom(name, stringBuilder.substring(0)));


        /* --- Unicidade de Tipos de Atributos --- */
        attTypes.keySet().forEach((atTypeID1)->{
            attTypes.keySet().forEach((atTypeId2)->{
                if (!atTypeID1.equals(atTypeId2))
                    context.addAxiom(new Axiom("axm_attrTDiff" + atTypeID1 + atTypeId2, atTypeID1 + " /= " + atTypeId2));
            });
        });

        /* -- Tipos de Atributos com Tipos Default -- */
        name = "axm_data";
        predicate = "partition(DataType, {NatSort}, {BoolSort})";
        context.addAxiom(new Axiom(name, predicate));

        name = "axm+dataDiffNatSortBoolSort";
        predicate = "NatSort /= BoolSort";
        context.addAxiom(new Axiom(name, predicate));

        /* -- attrvT dominio e definição -- */
        name = "axm_attrvT";
        predicate = "attrvT : AttrT --> VertT";
        context.addAxiom(new Axiom(name, predicate));

        name = "axm_attrvTdef";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(attrvT, ");
        g.getTypeGraph().getAttNodes().values().forEach((nt)->
                nt.getAttributes().forEach((at)->
                        stringBuilder
                                .append("{").append("at")
                                .append(at.getID()).append(" |-> ")
                                .append(nt.getType()).append("}, ")
                )
        );
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
        stringBuilder.append(")");
        context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        /* -- valT dominio e definição -- */
        name = "axm_valT";
        predicate = "valT : AttrT --> DataType";
        context.addAxiom(new Axiom(name, predicate));

        name = "axm_valTdef";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("partition(valT, ");
        attTypes.values().forEach((at)->{
            if (at.getType().equals("int"))
                stringBuilder
                        .append("{").append("at")
                        .append(at.getID()).append(" |-> ")
                        .append("NatSort").append("}, ");
            else if (at.getType().equals("boolean") || at.getType().equals("bool"))
                stringBuilder
                        .append("{").append("at")
                        .append(at.getID()).append(" |-> ")
                        .append("BoolSort").append("}, ");
        });
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
        stringBuilder.append(")");
        context.addAxiom(new Axiom(name, stringBuilder.substring(0)));

        return true;
    }

    /**
     * DEFINITION 3
     * Método que realiza a tradução e criação dos atributos de um grafo estado.
     * @param m - máquina a ser criada
     * @param g - gramática a ser traduzida
     * @return - sucesso ou fracasso
     */
    private boolean stateGraphAttributesTranslation(Machine m, Grammar g){

        //Cria LinkedHashMap com tipos de atributos
        LinkedHashMap<String, AttributeType> attTypes = new LinkedHashMap<>();
        g.getTypeGraph().getAttNodes().values().forEach((nt)->
                nt.getAttributes().forEach((at)->
                        attTypes.put(at.getID(), at)));

        //Cria LinkedHashMap com atributos
        LinkedHashMap<String, AttributeType> attG = new LinkedHashMap<>();
        g.getHost().getAttNodes().values().forEach((nt)->
                nt.getAttributes().forEach((a)->
                        attG.put(a.getName(), a)));

        if (attG.isEmpty())
            return false;


        /* --- Variables --- */
        m.addVariable(new Variable("AttrG"));   //Conjunto de instâncias de atributos
        m.addVariable(new Variable("attrvG"));  //Função mapeando atributos para nodos

        m.addVariable(new Variable("tGA"));     //Tipagem dos Atributos

        //Valor das instâncias de atributos
        attTypes.keySet().forEach((atTypeID)-> m.addVariable(new Variable("valGat" + atTypeID)));

        /* --- Invariants --- */
        String name;
        String predicate;

        //Domínio dos Atributos
        name = "inv_AttrG";
        predicate = "AttrG : POW(NAT)";
        m.addInvariant(name, new Invariant(name, predicate));

        //Dominio e Imagem da Função de Mapeamento dos Atributos para Nodos
        name = "inv_attrvG";
        predicate = "attrvG : AttrG --> VertG";
        m.addInvariant(name, new Invariant(name, predicate));

        //Domínio e Imagem da função de tipagem
        name = "inv_tGA";
        predicate = "tGA : AttrG --> AttrT";
        m.addInvariant(name, new Invariant(name, predicate));

        //Função para domíno dos valores das instâncias de atributos
        attTypes.values().forEach((at)->{
            String invName = "inv_valGat" + at.getID();
            String invPredicate = "valGat" + at.getID() + " : AttrG +-> ";
            if (at.getType().equals("int"))
                invPredicate += "INT";
            else if (at.getType().equals("boolean") || at.getType().equals("bool"))
                invPredicate += "BOOL";
            m.addInvariant(invName, new Invariant (invName, invPredicate));
        });

        //Cada atributo possui apenas um valor
        attTypes.keySet().forEach((atTypeID1) ->
                attTypes.keySet().forEach((atTypeID2) -> {
                    if (!atTypeID1.equals(atTypeID2)) {
                        String invName = "inv_Diffat" + atTypeID1 + "at" +  atTypeID2;
                        String invPredicate = "dom(valGat" + atTypeID1 + ") /\\ dom(valGat" + atTypeID2 + ") = {}";
                        m.addInvariant(invName, new Invariant(invName, invPredicate));
                    }
                }));


        //Todos os atributos possuem um valor
        attTypes.values().forEach((at)->{
            String invName = "inv_typeat" + at.getID();
            String invPredicate = "#a . a : AttrG & a : dom(tGA |> {at" + at.getID() + "}) => a : dom(valGat" + at.getID() + ")";
            m.addInvariant(invName, new Invariant(invName, invPredicate));
        });

        /* -- Event -- */
        Event initilisation = new Event("INITIALISATION");

        //THEN

        //Conjunto de valores
        name = "act_AttrG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("AttrG := {");
        attG.values().forEach((a) -> stringBuilder.append(a.getID().replaceAll("I", "")).append(", "));
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
        stringBuilder.append("}");
        initilisation.addAct(name, stringBuilder.substring(0, stringBuilder.length()));

        //Função de mapeamento de  conjunto de atributos para conjunto de valores
        name = "act_attrvG";
        stringBuilder.delete(0, stringBuilder.length());
        stringBuilder.append("attrvG := {");
        g.getHost().getAttNodes().values().forEach((n)->
                n.getAttributes().forEach((a)->
                        stringBuilder
                                .append(a.getID().replaceAll("I", ""))
                                .append(" |-> ")
                                .append(n.getID().replaceAll("I", ""))
                                .append(", ")
                )
        );
        stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());
        stringBuilder.append("}");
        initilisation.addAct(name, stringBuilder.substring(0, stringBuilder.length()));

        //act_valGta

        attTypes.values().forEach((at) -> {
            stringBuilder.delete(0, stringBuilder.length());
            String actName = "act_valGat" + at.getID();
            stringBuilder.append("valGat").append(at.getID()).append(" := {");

            attG.values().forEach((a)->{
                if (a.getID().equals(at.getID()))
                    stringBuilder.append(a.getID().replaceAll("I", "")).append(" |-> ").append(a.getValue()).append(", ");
            });

            stringBuilder.delete(stringBuilder.length()-2, stringBuilder.length());

            stringBuilder.append("}");
            initilisation.addAct(actName, stringBuilder.substring(0, stringBuilder.length()));
        });


        m.addEvent(initilisation);

        return true;
    }

    /**
     * DEFINITION 34
     * Método que realiza a tradução de uma regra com atributos
     * 100% Revised and working
     * @param m - máquina a ser criada
     * @param ctx - contexto a ser criada
     * @return - sucesso ou fracasso
     */
    private boolean attributedRuleTranslation(Grammar g, Context ctx, Machine m) {

        //Cria LinkedHashMap com tipos de atributos
        LinkedHashMap<String, AttributeType> attTypes = new LinkedHashMap<>();
        g.getTypeGraph().getAttNodes().values().forEach((nt)->
                nt.getAttributes().forEach((at)->
                        attTypes.put(at.getID(), at)));

        for (Rule r : g.getRules()) {

            //Cria LinkedHashMap com atributos do LHS
            LinkedHashMap<String, AttributeType> attLHS = new LinkedHashMap<>();
            r.getLHS().getAttNodes().values().forEach((nt) ->
                    nt.getAttributes().forEach((a) ->
                            attLHS.put(a.getName(), a)));

            String attLHSPrefix = "att" + r.getName();


        /* -- Context -- */
            String name, predicate;

            // --- Sets

            //Conjunto de Atributos no LHS
            ctx.addSet(new Set("Attr" + r.getName()));

            // --- Constants

            //Atributos no LHS
            attLHS.keySet().forEach((aLHS) -> ctx.addConstant(new Constant(attLHSPrefix + aLHS)));

            //Função para mapear atributos do LHS para nodos do LHS
            ctx.addConstant(new Constant("attrv" + r.getName()));

            //Função de Tipagem dos Atributos
            ctx.addConstant(new Constant("t" + r.getName() + "A"));

            // --- Axioms

            //Definição do Conjunto de Atributos de LHS
            name = "axm_Attr" + r.getName();
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(Attr").append(r.getName()).append(", ");
            attLHS.keySet().forEach((aLHS -> stringBuilder.append("{").append(attLHSPrefix).append(aLHS).append("}").append(", ")));
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            stringBuilder.append(")");
            ctx.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Definição do Domínio e Imagem da Função que mapeia atributo para vértice
            name = "axm_attrv" + r.getName();
            predicate = "attrv" + r.getName() + " : Attr" + r.getName() + " --> Vert" + r.getName();
            ctx.addAxiom(new Axiom(name, predicate));

            //Definição do Comportamento da Função que mapeia atributo para vértice
            name = "axm_attrv" + r.getName() + "def";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(attrv").append(r.getName()).append(", ");

            r.getLHS().getAttNodes().values().forEach((nt) ->
                    nt.getAttributes().forEach((at) ->
                            stringBuilder
                                    .append("{").append(attLHSPrefix)
                                    .append(at.getName()).append(" |-> ")
                                    .append(r.getName()).append(nt.getID()).append("}, ")
                    )
            );
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            stringBuilder.append(")");

            ctx.addAxiom(new Axiom(name, stringBuilder.substring(0)));

            //Definição do Domínio e Imagem de Função de Tipagem dos Atributos
            name = "axm_t" + r.getName() + "A";
            predicate = "t" + r.getName() + "A : Attr" + r.getName() + " --> AttrT";
            ctx.addAxiom(new Axiom(name, predicate));

            //Definição da Função de Tipagem de Atributos
            name = "axm_t" + r.getName() + "Adef";
            stringBuilder.delete(0, stringBuilder.length());
            stringBuilder.append("partition(t").append(r.getName()).append("A, ");
            attLHS.values().forEach((at) ->
                    stringBuilder.append("{").append(attLHSPrefix).append(at.getName())
                            .append(" |-> ")
                            .append("at").append(at.getID()).append("}, ")
            );
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            stringBuilder.append(")");
            ctx.addAxiom(new Axiom(name, stringBuilder.substring(0)));



            //TODO: REVIEW ALL CODE BELOW

//        //Define Attr domain & image
//        name = "axm_attrv" + r.getName();
//        stringBuilder.delete(0, stringBuilder.length());
//        stringBuilder
//                .append("attrv").append(r.getName())
//                .append(" : Attr").append(r.getName())
//                .append(" --> Vert").append(r.getName());
//        ctx.addAxiom(new Axiom(name, stringBuilder.substring(0)));
//
//        //Define Attr domain & image
//        name = "axm_attrv" + r.getName() + "def";
//        stringBuilder.delete(0, stringBuilder.length());
//        stringBuilder.append("partition(attrv").append(r.getName());
//        if (attLHS) {
//            for (Node n : r.getLHS().getAttNodes().values())
//                stringBuilder.append(",").append(n.getID());
//        }
//        if (attRHS) {
//            for (Node n : r.getRHS().getAttNodes().values())
//                stringBuilder.append(",").append(n.getID());
//        }
//        if (attNACs) {
//            if (!attributedNACsSet.isEmpty()) {
//                for (Graph nac : attributedNACsSet) {
//                    for (Node n : nac.getAttNodes().values()) {
//                        stringBuilder.append(",").append(n.getID());
//                    }
//                }
//            }
//        }
//        ctx.addAxiom(new Axiom(name, stringBuilder.substring(0)));
//
//        //Define axm_t para tipagem
//        stringBuilder.delete(0, stringBuilder.length());
//        name = "axm_t" + r.getName() + "A";
//        stringBuilder
//                .append("t")
//                .append(r.getName())
//                .append("A : Attr")
//                .append(r.getName())
//                .append(" --> AttrT");
//        ctx.addAxiom(new Axiom(name, stringBuilder.substring(0)));
//
//        //Define axm_tdef para tipagem
//        name = "axm_t" + r.getName() + "Adef";
//        stringBuilder.delete(0, stringBuilder.length());
//        stringBuilder
//                .append("partition(t")
//                .append(r.getName())
//                .append("A");
//        if (attLHS) {
//            for (Node n : r.getLHS().getAttNodes().values())
//                stringBuilder.append(",").append(n.getType());
//        }
//        if (attRHS) {
//            for (Node n : r.getRHS().getAttNodes().values())
//                stringBuilder.append(",").append(n.getType());
//        }
//        if (attNACs) {
//            if (!attributedNACsSet.isEmpty()) {
//                for (Graph nac : attributedNACsSet) {
//                    for (Node n : nac.getAttNodes().values()) {
//                        stringBuilder.append(",").append(n.getType());
//                    }
//                }
//            }
//        }
//        ctx.addAxiom(new Axiom(name, stringBuilder.substring(0)));
//
//        /* -- Machine -- */
//        // --- Preparation
//        HashMap<String, AttributeType> LHSAttributes = new HashMap<>();
//        HashMap<String, AttributeType> RHSAttributes = new HashMap<>();
//        HashMap<String, AttributeType> NACAttributes = new HashMap<>();
//        HashMap<String, AttributeType> RuleNewA = new HashMap<>();
//        HashMap<String, AttributeType> RuleDelA = new HashMap<>();
//        HashMap<String, AttributeType> forbiddenAttributes = new HashMap<>();
//
//
//        //Constroi conjunto de atributos no lado esquerdo
//        if (attLHS) {
//            for (Node n : r.getLHS().getAttNodes().values())
//                //AddAll Map Equivalent
//                LHSAttributes.putAll(n.getAttributes()
//                        .stream()
//                        .collect(Collectors.toMap(
//                                AttributeType::getID,
//                                Function.identity())
//                        )
//                );
//        }
//
//        //Constroi conjunto de atributos no lado direito
//        if (attRHS) {
//            for (Node n : r.getRHS().getAttNodes().values())
//                RHSAttributes.putAll(n.getAttributes()
//                        .stream()
//                        .collect(Collectors.toMap(
//                                AttributeType::getID,
//                                Function.identity())
//                        )
//                );
//        }
//
//        //Constroi conjunto de atributos de uma NAC
//        for (Graph nac : attributedNACsSet) {
//            for (Node n : nac.getAttNodes().values())
//                NACAttributes.putAll(n.getAttributes()
//                        .stream()
//                        .collect(Collectors.toMap(
//                                AttributeType::getID,
//                                Function.identity())
//                        )
//                );
//        }
//
//        //Constroi conjunto de atributos deletados
//        if (attLHS) {
//            RuleDelA.putAll(LHSAttributes);
//            RuleDelA.entrySet().removeAll(RHSAttributes.entrySet());
//        }
//
//        //Constroi conjunto de novos atributos
//        if (attRHS) {
//            RuleNewA.putAll(RHSAttributes);
//            RuleNewA.entrySet().removeAll(LHSAttributes.entrySet());
//        }
//
//        //Constrói conjuntos de atributos proibidos de serem identificados (NACat - LHSat)
//        if (attNACs) {
//            forbiddenAttributes.putAll(NACAttributes);
//            if (attLHS)
//                forbiddenAttributes.entrySet().removeAll(LHSAttributes.entrySet());
//        }
//
//        // creates event to extend it
//        Event ruleExtendEvent = new Event(r.getName());
//        ruleExtendEvent.setExtend(ruleEvent);
//
//        // --- ANY - Parameters
//        ruleExtendEvent.addParameter("mA");
//        ruleExtendEvent.addParameter("DelA");
//        ruleExtendEvent.addParameter("DanglingA");
//        for (AttributeType a : LHSAttributes.values()) {
//            ruleExtendEvent.addParameter("new_" + a.getID());
//        }
//        for (AttributeType a : RHSAttributes.values()) {
//            ruleExtendEvent.addParameter("new_" + a.getID());
//        }
//        for (AttributeType a : NACAttributes.values()) {
//            ruleExtendEvent.addParameter("new_" + a.getID());
//        }
//
//
//        // --- WHERE - Guards
//        //Match Attribute Edges - total function mapping attributed Edges
//        name = "grd_mA";
//        stringBuilder.delete(0, stringBuilder.length());
//        stringBuilder
//                .append("mA : Attr")
//                .append(r.getName())
//                .append(" --> AttrG");
//        ruleExtendEvent.addGuard(name, stringBuilder.substring(0));
//
//        //Set of deleted attributes of G
//        name = "grd_DelA";
//        stringBuilder.delete(0, stringBuilder.length());
//        stringBuilder.append("DelA := mA[");
//        int flag = 0;
//        for (AttributeType a : RuleDelA.values()) {
//            if (flag == 0)
//                flag = 1;
//            else
//                stringBuilder.append(", ");
//            stringBuilder.append(a.getID());
//        }
//        stringBuilder.append("]");
//        ruleExtendEvent.addGuard(name, stringBuilder.substring(0));
//
//        //Dangling Attributes of G
//        name = "grd_DangA";
//        stringBuilder.delete(0, stringBuilder.length());
//        //TODO: Revisar símbolos ASCII do EventB para expressão abaixo
//        stringBuilder.append("DanglingA = dom((attrvG |> DelV) \\ DelA");
//        ruleExtendEvent.addGuard(name, stringBuilder.substring(0));
//
//        //Fresh ids of Attribute Edges
//        for (AttributeType a : RuleNewA.values())
//            //TODO: Revisar símbolos ASCII do EventB para expressão abaixo
//            ruleExtendEvent.addGuard("grd_new_a" + a.getID(), "new_a" + a.getID() + " : NAT \\ AttrG");
//
//        //Unicidade de ids para novos atributos
//        for (int i = 0; i < RuleNewA.size() - 1; i++) {
//            for (int j = i + 1; j < RuleNewA.size(); j++) {
//                //TODO: Revisar símbolos ASCII do EventB para expressão abaixo
//                ruleExtendEvent.addGuard(
//                        "grd_diff_a" + RuleNewA.get(i).getID() + "a" + RuleNewA.get(j).getID(),
//                        "new_a" + RuleNewA.get(i).getID() + " != new_a" + RuleNewA.get(j).getID());
//            }
//        }
//
//        for (AttributeType at : LHSAttributes.values()) {
//            //Compatibilidade entre atributos da regra e do grafo estado
//            //TODO: revisar se é com atributos ou com os nodos atrelados
//            ruleExtendEvent.addGuard(
//                    "grd_attrv_" + at.getID(),
//                    "mV(attrv" + r.getName() + "(" + at.getID() + ")) = attrvG(" + at.getID());
//
//            //Compatibilidade de tipo entre atributos das regras e do grafo estado
//            ruleExtendEvent.addGuard(
//                    "grd_tA" + at.getID(),
//                    "t" + r.getName() + "A(" + at.getID() + ") = tGA(mA(" + at.getID() + ")");
//            //Compatibilidade de valores entre grafo tipo e variáveis
//            //TODO: terminar implementação da guarda abaixo
//            //ruleExtendEvent.addGuard("grd_val" + at.getID(), );
//        }
//
//        for (Graph NAC: attributedNACsSet){
//            name = "grd_NAC" + r.getName() + NAC.getNACindex();
//
//            stringBuilder.delete(0, stringBuilder.length());
//            stringBuilder.append("{");
//            flag = 0;
//            for (Node n: NAC.getAttNodes().values()){
//                for (AttributeType at: n.getAttributes()){
//                    if (flag == 0)
//                        flag = 1;
//                    else
//                        stringBuilder.append(", ");
//                    stringBuilder.append(at.getID());
//                }
//            }
//            stringBuilder.append("} ");
//
//            //TODO: revisar símbolos ASCII do EventB para expressão abaixo
//            stringBuilder
//                    .append("C= AttrG \\ mA[Attr")
//                    .append(r.getName())
//                    .append("] & ");
//            for (AttributeType at: forbiddenAttributes.values()){
//                AttributeType ta = NACAttributes.get(at.getID());
//                if (ta != null) {
//
//                    stringBuilder
//                            .append("tGA(").append(at.getID()).append(") = ")
//                            .append(ta.getID()).append(" & ");
//
//                    stringBuilder.append("valG").append(ta.getID()).append("(").append(at.getID()).append(") = ")
//                            .append("valLNACjtLANACj").append(" & ");    //TODO: REVIEW VAL FUNCTION IN THIS EXPRESSION
//
//                    //TODO: continue translation of attributed rule (def 35)
//                }
//
//
//            }
//
//        }
        }
        return true;
    }

    /**
     * Main para testes de conversão de Agg para GG e de GG para EventB
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String fullPath = "tests/pacmanAtributo/pacmanAtributo";
        //String fullPath = "tests/R2C/R2C";

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
        eventB.translate(newProject, test, true);
        //Logs
        newProject.logProject(logDir.getPath(), rodinDir.getPath());

        System.out.println("Finished!");
    }
}
