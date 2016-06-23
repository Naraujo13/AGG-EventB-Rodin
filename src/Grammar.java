
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author nicol
 */
public class Grammar {
    
   
    //Atributos da gramática
   List <Rule> rules;   //Arraylist com as regras da gramática
   Graph host;          //Grafo host
   TypeGraph typeGraph;     //Grafo tipo
   
   
   
    //Auxiliares
    String tokenAtual;      //token sendo analisado
    Scanner entrada;        //scanner usado na leitura do arquivo
    
    public Grammar(){
        typeGraph = new TypeGraph();
        rules = new ArrayList <>();
    }

    public void reader(String arquivo ){
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
                        //Insere atributo no nodo
                        newNodeType.attributes.add(newAttType);
                    }
                   //Insere nodo no grafo de tadução
                    typeGraph.addTranslNode(auxiliar2[1], auxiliar3[1]); //associa ID ao tipo para tradução (adiconará internamente no grafo tipo a coleção de nodos, pois esta esta associada aos valoresdo hashmap)
                    typeGraph.allowedNodes.add(newNodeType);
                }
                tokenAtual = entrada.next();
            }            
        }
        //Definição de Arestas do Grafo Tipo
        EdgeType newEdgeType;
        while(tokenAtual.contains("Edge")){
            //Extrai tipo da aresta e salva como ID
            auxiliar = tokenAtual.split(" ");
            auxiliar2 = auxiliar[4].split("\"");
            newEdgeType = new EdgeType(auxiliar2[1]);
            
            //Extrai Source
            auxiliar2 = auxiliar[2].split("\"");
            newEdgeType.addSource(typeGraph.translationNodes.get((auxiliar2[1])));
            
            //Extrai Target
            auxiliar2 = auxiliar[3].split("\"");
            newEdgeType.addTarget(typeGraph.translate(auxiliar2[1]));
            
            //Adiciona Aresta no ArrayList do Grafo Tipo
            typeGraph.allowedEdges.add(newEdgeType);
            
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
        host = new Graph("HOST");
        tokenAtual = entrada.next();
        //Nodos do HOST
        defineGraphNodes(host, attNames, attTypes);     //Funcionando até aqui
        
        //Arestas do HOST
        defineGraphEdges(host);
        
       tokenAtual = entrada.next(); //Descarta /Graph do HOST
       //FIM DO HOST
       
       //REGRAS
       //Mais vantajoso ID LHS -> ID RHS ou ID RHS -> ID LHS ? Até o momento RHS ou NAC -> LHS
       defineRules(attNames, attTypes);
       
       // /GraphTrasnformationSystem
       if (tokenAtual.contains("/GraphTransformationSystem"))
           tokenAtual = entrada.next();
       
       //FIM DO ARQUIVO ABAIXO
       
       //Itera /Document
         // if (tokenAtual.contains("/Document"))
          // tokenAtual = entrada.next();
        //Fecha o Sacnner
        entrada.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Grammar.class.getName()).log(Level.SEVERE, null, ex);
        }    
       
    }
    
    
    /**
     * Função que define as Regras de uma Gramática (RHS, LHS e NACs)
     * @param attNames map contendo os nomes dos atributos associado ao seu ID
     * @param attTypes map contendo os tipos dos atributos associados ao seu ID
     */
    public void defineRules(Map <String, String> attNames, Map <String, String> attTypes){
        //Definição de uma Regra...
       Graph RHS = null, LHS = null;
       Rule rule = null;
       String name;
       
        //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
        String[] auxiliar3; //Auxiliar 3 para quebrar substrings
       
       while (tokenAtual.contains("Rule")){
           
           //Pega nome da regra.
           auxiliar = tokenAtual.split(" ");
           auxiliar2 = auxiliar[3].split("\"");
           name = auxiliar2[1];
           
           
           while (!tokenAtual.contains("Graph")){
            tokenAtual = entrada.next();
           }
           
                //Define LHS
            if (tokenAtual.contains("LHS")){
                tokenAtual = entrada.next();
                LHS = new Graph ("LHS");
               //Define Nodos
               defineGraphNodes(LHS, attNames, attTypes);
               //DEfine arestas
               defineGraphEdges(LHS);
               //Descarta /Graph
               tokenAtual = entrada.next(); 
            }
            
            while (!tokenAtual.contains("Graph")){
                tokenAtual = entrada.next();
            }
            
           //Define RHS
           if (tokenAtual.contains("RHS")){
               tokenAtual = entrada.next();
               RHS = new Graph ("RHS");
               //Define Nodos
               defineGraphNodes(RHS, attNames, attTypes);
               //DEfine arestas
               defineGraphEdges(RHS);
               //Descarta /Graph
               tokenAtual = entrada.next(); 
           }
      
            
            //Cria regra e insere RHS e LHS definidos acima
            if (RHS != null && LHS != null)
                rule = new Rule(name, RHS, LHS);
            else
                System.out.println("Erro ao definir uma regra");
            
            //Define Morfismo de RHS -> LHS
            defineMorphism(RHS);
                                               
            //Condições de Aplicação
            defineApplicationConditions(rule, attNames, attTypes);
            
            //Itera Layer, Prioridade e /Rule
            while (!tokenAtual.contains("/Rule"))
                tokenAtual = entrada.next();
            tokenAtual = entrada.next();
            
            //Adiciona regra no Araylist de regras;
            if (rule != null)
                rules.add(rule);
            else
                System.out.println("Erro ao criar Regra.");
            
             //Funcionando até aqui para 1 regra, testar com mais
       }
       //FIM DEFINIÇÂO DE REGRA
    }
    
    
    /**
     * Função chamada pela função defineRules() para definir as NACs
     * @param rule - regra que terá suas regras de aplicação definidas
     * @param attNames - map contendo os nomes dos atributos associados ao seu ID
     * @param attTypes - map contendo os tipos dos atributos associados ao seu ID
     */
    public void defineApplicationConditions(Rule rule, Map <String,String> attNames, Map <String, String> attTypes){
        
            Graph NAC;
        
             //Condições de Aplicação
            if (tokenAtual.contains("ApplCondition")){
                tokenAtual = entrada.next();
                
                //NAC
                while (tokenAtual.contains("NAC")){ //cada iteração do while é uma NAC
                    tokenAtual = entrada.next();
                    
                    NAC = new Graph("NAC");
                    //Itera pós ID-Nome e etc do NAC
                    tokenAtual = entrada.next();
                    //Define Nodos
                    defineGraphNodes(NAC,attNames, attTypes);
                    //Define Arestas
                    defineGraphEdges(NAC);
                    
                    //Descarta /Graph
                   tokenAtual = entrada.next();
                   
                   //Inserir Morfismo de LHS -> NAC
                   defineMorphism(NAC);
                   
                   //Itera /NAC
                   if (tokenAtual.contains("/NAC"))
                       tokenAtual = entrada.next();
                   
                   //Adiciona NAC no ArrayList
                   rule.insertNAC(NAC);
                }
                
                //Itera /ApplCondition
                if (tokenAtual.contains("/ApplCondition"))
                    tokenAtual = entrada.next();           
            }
    }
    
    
    /**
     * Função que define o morfismo de um grafo, sendo este um hash map. 
     * Em tal hashmap temos:
     * chave -> imagem
     * valor -> origem
     * @param graph - grafo a ter o morfismo definido
     */
    public void defineMorphism (Graph graph){
        
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
                graph.getMorphism().put(auxiliar2[1], auxiliar3[1]);
                tokenAtual = entrada.next();
            }
            //Itera /Morphism
            if (tokenAtual.contains("/Morphism"))
                tokenAtual = entrada.next();
        }
     
    }
    
    

    /**
     * Função que define nodos do dado grafo tipo, extraindo-os do arquivo corrente.
     * @param graph  Grafo tipo cujos nodos serão definidos
     */
    public void defineTypeGraphEdges(TypeGraph graph){
        EdgeType newEdgeType;
        while(tokenAtual.contains("Edge")){
            
             //Vetores de String Auxiliares para quebrar comandos
            String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
            String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
            String[] auxiliar3; //Auxiliar 3 para quebrar substrings
            
            //Extrai tipo da aresta e salva como ID
            auxiliar = tokenAtual.split(" ");
            auxiliar2 = auxiliar[4].split("\"");
            newEdgeType = new EdgeType(auxiliar2[1]);
            
            //Extrai Source
            auxiliar2 = auxiliar[2].split("\"");
            newEdgeType.addSource(typeGraph.translate((auxiliar2[1])));
            
            //Extrai Target
            auxiliar2 = auxiliar[3].split("\"");
            newEdgeType.addTarget(typeGraph.translate(auxiliar2[1]));
            
            //Descarta opções de Layout
            while (!tokenAtual.contains("/Edge")){
                tokenAtual = entrada.next(); 
            }
            //Descarta /Edge
            tokenAtual = entrada.next();
        }
    }
    
    /**
     * Função que recebe como parêmtro um grafo e faz a leitura do arquivo
     * salvo no scanner atual utilizando a variável tokenAtual, extraindo as 
     * informações dos nodos do grafo e os inserindo no grafo passado como
     * parêmtro
     * @param graph - grafo cujos nodos serão extraídas do arquivo
    */
    public void defineGraphNodes(Graph graph, Map <String,String> attNames, Map <String, String> attTypes){
         //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
        String[] auxiliar3; //Auxiliar 3 para quebrar substrings
        
        Node newNode;
        String ID, type, attributeID, atributeValue, atributeType;
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
                type = auxiliar2[1];

                //Instancia novo nodo e insere no grafo
                newNode = new Node (type, ID);
                graph.addNode(newNode);
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
     * Função que recebe como parêmtro um grafo e faz a leitura do arquivo
     * salvo no scanner atual utilizando a variável tokenAtual, extraindo as 
     * informações das arestas do grafo e as inserindo no grafo passado como
     * parêmtro
     * @param graph - grafo cujas arestas serão extraídas do arquivo
    */
    public void defineGraphEdges(Graph graph){
         //Vetores de String Auxiliares para quebrar comandos
        String[] auxiliar; //Declara vector que servirá como auxiliar ao quebrar o comando
        String[] auxiliar2; //Auxiliar 2 Para quebrar substrings;
        String[] auxiliar3; //Auxiliar 3 para quebrar substrings
        
         Edge newEdge;
        String type, ID, source, target;
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
                type = auxiliar2[1];

                //Instancia nova aresta e insere no grafo
                newEdge = new Edge (type, ID, source, target);
                graph.addEdge(newEdge);
            }
            tokenAtual = entrada.next(); //itera para próxima linh
        }
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
       String arquivo =  "PacmanAtributo.ggx";
        
       Grammar test = new Grammar();
       test.reader(arquivo);
       

       
       System.out.println("Finished!");
    }
}
