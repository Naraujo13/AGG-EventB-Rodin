/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tradutores;

import GraphGrammar.Attribute;
import GraphGrammar.AttributeType;
import GraphGrammar.Edge;
import GraphGrammar.EdgeType;
import GraphGrammar.Grammar;
import GraphGrammar.Graph;
import GraphGrammar.Node;
import GraphGrammar.NodeType;
import GraphGrammar.Rule;
import GraphGrammar.TypeGraph;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nicol
 */
public class AGGToGraphGrammar {
   
        //Auxiliares
        private String tokenAtual;      //token sendo analisado
        private Scanner entrada;        //scanner usado na leitura do arquivo
        private boolean existsFlag;
    
    void aggReader(String arquivo, Grammar grammar){
        //Hash Maps utilizados para definir nodos do grafo tipo com arestas
        Map <String, String> attNames;  //Associa ID ao nome lendo rótulos
        Map <String, String> attTypes; //Associa ID ao tipo lendo rótulos
        Map <String, String> nodeAtt;   //Hash map com ID do nodo e ID do att
        attNames = new HashMap <>();
        attTypes = new HashMap<>();
        nodeAtt = new HashMap<>();    
        
        String attName, attID, attType, rotuloID;
        //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
        String[] auxiliar3; //Auxiliar 3 para quebrar substrings
       
       try {
             entrada = new Scanner(new FileReader(arquivo)).useDelimiter("<");  //le e usa > como delimitador
             
        //Definição de Nodes ou Edges - inútil, pois é definido novamente no grafo tipo
        tokenAtual = entrada.next();
        while(!tokenAtual.contains("kind=\"TG\"")){ //itera até achar o início do grafo tipo
            if (tokenAtual.contains("/NodeType"))
                tokenAtual = entrada.next();
            if (tokenAtual.contains("NodeType")){   //se for nodo
                
                //Pega ID do nodo
                auxiliar = tokenAtual.split(" ");
                auxiliar2 = auxiliar[1].split("\"");
                rotuloID = auxiliar2[1];
                
                tokenAtual = entrada.next();
                if (!tokenAtual.contains("kind=\"TG\"")){ //e se tem atributo, salva o nome do atributo e seu ID tipo
                    if (tokenAtual.contains("AttrType")){
                        auxiliar = tokenAtual.split(" ");
                        //salva ID
                        auxiliar2 = auxiliar[1].split("\"");
                        attID = auxiliar2[1];
                        //salva nome
                        auxiliar2 = auxiliar[2].split("\"");
                        attName = auxiliar2[1];
                        
                        //salva Tipo
                        auxiliar2 = auxiliar[3].split("\"");
                        attType = auxiliar2[1];
                        
                        //adiciona no hashmap
                        attNames.put(attID, attName);
                        attTypes.put(attID, attType);
                        nodeAtt.put(rotuloID, attID);
                    }               
                }
            }
            if (!tokenAtual.contains("kind=\"TG\""))
                tokenAtual = entrada.next();                    //itera para o próximo comando
        }
        //Definição de Grafo Tipo, se existir
        //Definição de nodos e
        //Define HashMap que converte para os "Ids Tipo" (confusão do AGG)
        Map <String, String> types = new HashMap<>();
        NodeType newNodeType;
        String nodeTypeID;
        AttributeType newAttType;
        if (tokenAtual.contains("kind=\"TG\"")){
            tokenAtual = entrada.next();
            //Nodos
            while (tokenAtual.contains("Node ID") || tokenAtual.contains("Layout") ||tokenAtual.contains("/Node")){        //cria hashmap para os tipos dentro do grafo tipo
                if (!tokenAtual.contains("Layout") && !tokenAtual.contains("/Node")){
                    auxiliar = tokenAtual.split(" ");       //dá split
                    auxiliar2 = auxiliar[1].split("\"");     //coloca ID no auxiliar 2
                    auxiliar3 = auxiliar[2].split("\"");      //coloca tipo no auxiliar 3
                    nodeTypeID = auxiliar3[1];
                    //Cria nodo passando tipo
                    newNodeType = new NodeType(nodeTypeID);
                    //Se nodo possuia atributos no rótulo
                    if (nodeAtt.containsKey(auxiliar3[1])){
                        //Crianova instancia de atributo contendo o tipo
                        newAttType = new AttributeType(attTypes.get(nodeAtt.get(auxiliar3[1])));
                        System.out.println(auxiliar3[1] + "|" + nodeAtt.get(auxiliar3[1]));
                        //Insere atributo no nodo
                        newNodeType.addAttribute(newAttType);
                    }
                   //Insere nodo no grafo de tadução
                    grammar.getTypeGraph().addTranslNode(auxiliar2[1], auxiliar3[1]); //associa ID ao tipo para tradução (adiconará internamente no grafo tipo a coleção de nodos, pois esta esta associada aos valoresdo hashmap)
                    grammar.getTypeGraph().addAllowedNode(newNodeType);
                }
                tokenAtual = entrada.next();
            }            
        }

        //Arestas
        EdgeType newEdgeType;
        while(tokenAtual.contains("Edge")){

            //Quebra atributos
            auxiliar = tokenAtual
                    .replaceAll("<Edge", "")
                    .replaceAll(">", "")
                    .replaceAll("\"", "")
                    .replaceAll("\n", "")
                    .split(" ");

            String edgeType = "";
            String edgeSource = "";
            String edgeTarget = "";

            for (String att : auxiliar){
                if (att.contains("source") && !att.contains("max") && !att.contains("min"))
                    edgeSource = grammar.getTypeGraph().getTranslationNodes().get(att.replaceAll("source=", ""));
                else if (att.contains("target") && !att.contains("max") && !att.contains("min"))
                    edgeTarget = grammar.getTypeGraph().getTranslationNodes().get(att.replaceAll("target=", ""));
                else if (att.contains("type"))
                    edgeType = att.replaceAll("type=", "");
            }


            //Cria objeto
            final String finalEdgeType = edgeType;
            final String finalEdgeSource = edgeSource;
            final String finalEdgeTarget = edgeTarget;
            existsFlag = false;
            grammar.getTypeGraph().getAllowedEdges().forEach(e->{
                if (e.getType().equals(finalEdgeType)){
                    e.addSource(finalEdgeSource);
                    e.addTarget(finalEdgeTarget);
                    edgeLambdaCallback(true);
                }
            });
            if (!existsFlag) {
                newEdgeType = new EdgeType(edgeType);
                //Adiciona fonte
                if (edgeSource != null && !edgeSource.equals(""))
                    newEdgeType.addSource(edgeSource);
                //Adiciona destino
                if (edgeTarget != null && !edgeTarget.equals(""))
                    newEdgeType.addTarget(edgeTarget);

                grammar.getTypeGraph().addAllowedEdge(newEdgeType);
            }
            
            //Descarta opções de Layout
            while(!tokenAtual.contains("/Edge")){
                tokenAtual = entrada.next(); 
            }
            tokenAtual = entrada.next();
        }
        
        //FIM GRAFO TIPO
        if (tokenAtual.contains("/Graph")){
            tokenAtual = entrada.next();
        }
        if (tokenAtual.contains("/Types")){
            tokenAtual = entrada.next();
        }
        
        //INICIO GRAFO HOST
        Graph newHost = new Graph("HOST");
        tokenAtual = entrada.next();
        //Nodos do HOST
        defineGraphNodes(newHost, attNames, attTypes);
        
        //Arestas do HOST
        defineGraphEdges(grammar, newHost);

        if (tokenAtual.contains("/Graph") && entrada.hasNext() )
            tokenAtual = entrada.next(); //Descarta /Graph do HOST

       //FIM DO HOST
       grammar.setHost(newHost);

       //Descarta Hosts Extras
       while (tokenAtual.contains("Graph")){
           while (!tokenAtual.contains("/Graph") && entrada.hasNext())
               tokenAtual = entrada.next();
           if (entrada.hasNext())
               tokenAtual = entrada.next();
       }

       //Descarta Constraints
       while (tokenAtual.contains("Constraints")){
           while (!tokenAtual.contains("/Constraints") && entrada.hasNext())
               tokenAtual = entrada.next();
           if (entrada.hasNext())
               tokenAtual = entrada.next();
       }

       //REGRAS
       //Mais vantajoso ID LHS -> ID RHS ou ID RHS -> ID LHS ? Até o momento RHS ou NAC -> LHS
       defineRules(grammar, attNames, attTypes);
       
       // /GraphTrasnformationSystem
       if (tokenAtual.contains("/GraphTransformationSystem"))
           tokenAtual = entrada.next();
       else
           System.out.println("Erro em graph transformationSystem.\n");
       
       //FIM DO ARQUIVO ABAIXO
       
       //Fecha o Scanner
        entrada.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Grammar.class.getName()).log(Level.SEVERE, null, ex);
        }    
        
    }
    
    /**
     * Função que define nodos do grafo tipo atual, extraindo-os do arquivo corrente.
     * @param tg - grafo tipo que será definido
     */
    private void defineTypeGraphEdges(TypeGraph tg){
        EdgeType newEdgeType;
        while(tokenAtual.contains("Edge")){
            
             //Vetores de String Auxiliares para quebrar comandos
            String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
            String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
            
            //Extrai tipo da aresta e salva como ID
            auxiliar = tokenAtual.split(" ");
            auxiliar2 = auxiliar[4].split("\"");
            newEdgeType = new EdgeType(auxiliar2[1]);
            
            //Extrai Source
            auxiliar2 = auxiliar[2].split("\"");
            newEdgeType.addSource(tg.translate((auxiliar2[1])));
            
            //Extrai Target
            auxiliar2 = auxiliar[3].split("\"");
            newEdgeType.addTarget(tg.translate(auxiliar2[1]));
            
            //Descarta opções de Layout
            while (!tokenAtual.contains("/Edge")){
                tokenAtual = entrada.next(); 
            }
            //Descarta /Edge
            tokenAtual = entrada.next();
        }
    }  

    private void edgeLambdaCallback(boolean exists){
        this.existsFlag = exists;
    }
    
    /**
     * Função que para um dado grafo, define seus nodos.
     * Faz a leitura do arquivo atual em scanne utilizando a variável tokenAtual,
     * extraindo as informações dos nodos do grafo e os inserindo no grafo atual.
     * @param g - grafo cujos nodos serão definidos
     * @param attNames - hashmap com o nome dos atributos de um dado nodo
     * @param attTypes - hashmap com os tipos dos atributos de um dado nodo
    */
    private void defineGraphNodes(Graph g, Map <String,String> attNames, Map <String, String> attTypes){
         //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
        String[] auxiliar3; //Auxiliar 3 para quebrar substrings
        
        Node newNode;
        String ID, nodeType, attributeID, atributeValue, atributeType;
        Attribute newAtt;
        //enquanto for nodo...
        if (tokenAtual.contains("Graph") && entrada.hasNext())
            tokenAtual = entrada.next();
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
                    newNode.getAttributes().add(newAtt);
                    
                    while (!tokenAtual.contains("/Attribute")){
                    tokenAtual = entrada.next();
                    }
                    tokenAtual = entrada.next();
                    
                }
                g.addNode(newNode);
                    
            }
            else
                tokenAtual = entrada.next(); //itera para próxima linha
        }
    }
    
         /**
     * Função que para o grafo atual faz a leitura do arquivo
     * salvo no scanner atual utilizando a variável tokenAtual, extraindo as 
     * informações das arestas do grafo e as inserindo no grafo passado como
     * parêmtro
          * @param grammar
          */
    private void defineGraphEdges(Grammar grammar, Graph graph){
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
                graph.addEdge(newEdge);
            }
            tokenAtual = entrada.next(); //itera para próxima linh
        }
    }
    
    /**
     * Função que define o morfismo de um grafo, sendo este um hash map. 
     * Em tal hashmap temos:
     * chave -> imagem
     * valor -> origem
     * @param g - grafo cujo morfismo será definido
     */
    private void defineGraphMorphism (Graph g){
        
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
                g.getMorphism().put(auxiliar2[1], auxiliar3[1]);
                tokenAtual = entrada.next();
            }
            //Itera /Morphism
            if (tokenAtual.contains("/Morphism"))
                tokenAtual = entrada.next();
        }
     
    }
    
        /**
     * Função que define as Regras de uma Gramática (RHS, LHS e NACs)
     * @param grammar - gramática cujas regras serão definidas
     * @param attNames map contendo os nomes dos atributos associado ao seu ID
     * @param attTypes map contendo os tipos dos atributos associados ao seu ID
     */
    private void defineRules(Grammar grammar, Map <String, String> attNames, Map <String, String> attTypes){
        //Definição de uma Regra...
       Graph RHS = null, LHS = null;
       Rule rule = null;
       String ruleName;
       
        //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
       
       while (tokenAtual.contains("Rule") && !tokenAtual.contains("RuleSequence")){
           
           //Pega nome da regra.
           auxiliar = tokenAtual.split(" ");
           auxiliar2 = auxiliar[3].split("\"");
           ruleName = auxiliar2[1];
           
           
           while (!tokenAtual.contains("Graph")){
            tokenAtual = entrada.next();
           }
           
                //Define LHS
            if (tokenAtual.contains("LHS")){
                LHS = new Graph("LHS");
               if (!tokenAtual.contains("/")) {
                   tokenAtual = entrada.next();
                   //Define Nodos
                   defineGraphNodes(LHS, attNames, attTypes);
                   //Define arestas
                   defineGraphEdges(grammar, LHS);
               }
                //Descarta /Graph ou /
                tokenAtual = entrada.next();
            }
            
            while (!tokenAtual.contains("Graph")){
                tokenAtual = entrada.next();
            }
            
           //Define RHS
           if (tokenAtual.contains("RHS")){
               RHS = new Graph("RHS");
               if (!tokenAtual.contains("/")) {
                   tokenAtual = entrada.next();
                   //Define Nodos
                   defineGraphNodes(RHS, attNames, attTypes);
                   //DEfine arestas
                   defineGraphEdges(grammar, RHS);
               }
               //Descarta /Graph ou /
               tokenAtual = entrada.next(); 
           }
      
            
            //Cria regra e insere RHS e LHS definidos acima
            if (RHS != null && LHS != null)
                rule = new Rule(ruleName, RHS, LHS);
            else
                System.out.println("Erro ao definir uma regra");
            
            //Define Morfismo de RHS -> LHS
            if (RHS != null)
                defineGraphMorphism(RHS);
                                               
            //Condições de Aplicação
            if (rule != null)
                defineApplicationConditions(grammar, attNames, attTypes, rule);
            
            //Itera Layer, Prioridade e /Rule
            while (!tokenAtual.contains("/Rule"))
                tokenAtual = entrada.next();
            tokenAtual = entrada.next();
            
            //Adiciona regra no Araylist de regras;
            grammar.addRule(rule);

       }
       //FIM DEFINIÇÂO DE REGRA
    }
    
    /**
     * Função chamada pela função defineRules() para definir as NACs
     * @param grammar
     * @param attNames - map contendo os nomes dos atributos associados ao seu ID
     * @param attTypes - map contendo os tipos dos atributos associados ao seu ID
     * @param r - regra cujas condições de aplicação serão definidas
     */
    private void defineApplicationConditions(Grammar grammar, Map<String, String> attNames, Map<String, String> attTypes, Rule r){

            Graph newNAC;
            int NACindex = 1;
        
             //Condições de Aplicação
            if (tokenAtual.contains("ApplCondition")){
                tokenAtual = entrada.next();
                
                //NAC
                while (tokenAtual.contains("NAC")){ //cada iteração do while é uma NAC
                    tokenAtual = entrada.next();
                    
                    newNAC = new Graph("NAC", NACindex);
                    //Itera pós ID-Nome e etc do NAC
                    tokenAtual = entrada.next();
                    //Define Nodos
                    defineGraphNodes(newNAC, attNames, attTypes);
                    //Define Arestas
                    defineGraphEdges(grammar, newNAC);
                    
                    //Descarta /Graph
                   tokenAtual = entrada.next();
                   
                   //Inserir Morfismo de LHS -> NAC
                   defineGraphMorphism(newNAC);
                   
                   //Itera /NAC
                   if (tokenAtual.contains("/NAC"))
                       tokenAtual = entrada.next();
                   
                   //Adiciona NAC no ArrayList
                   r.insertNAC(newNAC);

                   NACindex++;
                }
                
                //Itera /ApplCondition
                if (tokenAtual.contains("/ApplCondition"))
                    tokenAtual = entrada.next();           
            }
    }

//    /** Main para testes de conversão do AGG
//    * @param args the command line arguments
//    */
//    public static void main(String[] args) {
//       String arquivo =  "PacmanAtributo.ggx";
//       AGGToGraphGrammar agg = new AGGToGraphGrammar();
//       Grammar test = new Grammar("PacmanAtributo");
//       agg.aggReader(arquivo, test);
//       test.printGrammar();
//       System.out.println("Finished!");
//    }
    
}
