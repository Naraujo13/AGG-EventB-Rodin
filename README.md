# Automatic Graph Grammar to Event-B Translator
Given a  graph grammar defined in AGG (.ggx) using a type graph, converts the file to a new one,  usable in Rodin,
containing the same definition represent in Event-B. This allows the use of semiautomatic provers in graph grammars with
infinite states. It's important to make clear that auomatic provers exist, and operate directly over graph grammars, but 
to make their proofs they expand all the states of this grammar. Consequently it is impossible to use this automatic 
provers with grammar with infinite states or even with grammars that have a lot of states. This approach allows the use 
of semiautomatic provers with both of them, making the proofs without the need to expand all the states of the grammar. 
The formal base of this research is (CAVALHEIRO, 2010) PHD thesis and further developed in (CAVALHEIRO, 2017), available
in the following links:

(1) www.lume.ufrgs.br/handle/10183/25516;
(2)www.sciencedirect.com/science/article/pii/S0304397517303419a;

This project consists of three phases:
      1. Parser 
      2. Translator 
      3. Files Creator

Phase 1- Parser: It reads the .ggx file and extracts the data, storing it in the data structure called Grammar defined 
in the package GraphGrammar. This structure can represent any strucutre that satisfies the follwoing conditions:
      1. It needs to be a typed grammar, that is, the grammar NEEDS to have a type graph.
      2. Only nodes can have attributes. This was defined during development due to the convention of modelling object 
      as nodes, so it  wouldn't make sense to allow edges to have too. This can be changed in the future.
      3. Due to an AGG limitation, no user defined types are allowed yet. This can also be changed in the future.
This generic structure allows the use of different parsers, meaning that the editor can be changed later, without the 
need to change the other steps of the program. This phase is ready, with a parser implemented to the editor AGG, 
available in the following link:    http://www.user.tu-berlin.de/o.runge/agg/index.html
     
Phase 2 - Translator: This phase translates the structure called grammar to another strucut, containing the same grammar
defined in the package Event-B. This structure can represent any structure that satisfies the same conditions stated in 
the previous steps. The use of generic structures as input and output allows this phase, the main one, to be independent 
of the other two, allowing the change of parser and tester. This phase is currently during late stages of development. 
The translator was defined as the implementation of the translation defined in (CAVALHEIRO, 2017) as said above.

Phase 3 - Files Creator: This phase uses the generic Event-B structure created in phase 2 to create the files to be used 
in the semi-automatic prover. In this research the chosen tester was Rodin. The files are generated in a txt format, with
the correct Rodin Syntax, being ready to be used alongside Camile plugin.
This phase can only it needs to be changed to allow the use of different tester.

This was done at Universidade Federal de Pelotas, for the group "Métodos Formais e 
Fundamentos Matemáticos da Ciência da Computação", more information at: 
http://dgp.cnpq.br/dgp/espelhogrupo/9814311152255117#

# Dependencies:

- AGG - http://www.user.tu-berlin.de/o.runge/agg/   
- Rodin - http://www.event-b.org/install.html 
- Camille Editor plugin - http://wiki.event-b.org/index.php/Camille_Editor

# Usage:

Download the release version (https://github.com/Naraujo13/Graph-Grammar-Translator/releases), that comes with a JAR artifact and a tests folder. 
Then execute the JAR passing the path to the file as argument like below:

java -jar translator.jar fileRelativePath/fileName.ggx

This will generate two folders. One named "log", containing logs of the parsing of the graph grammar. The second, named
"out", containing the already translated machines and contexts in txt format. To use them, simply create the file in
normally in Rodin with the corresponding name and, using Camille plugin, paste the txt content into the created file.
Simply save it and you're ready to go. Repeat this process for every generated context and machine file.



