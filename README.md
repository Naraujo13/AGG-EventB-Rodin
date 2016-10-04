# Automatic Graph Grammar to Event-B Translator
Given a  graph grammar defined in AGG (.ggx) using a type graph, converts the file to a new one,  usable in Rodin,
containing the same definition represent in Event-B. This allows the use of semiautomatic provers in graph grammars with infinite states. It's important to make clear that auomatic provers exist, and operate directly over graph grammars, but to make their proofs they expand all the states of this grammar. Consequently it is impossible to use this automatic provers with grammar with infinite states or even with grammars that have a lot of states. This approach allows the use of semiautomatic provers with both of them, making the proofs without the need to expand all the states of the grammar. The formal base of this research is (CAVALHEIRO, 2010) PHD thesis availabe in the following link: www.lume.ufrgs.br/handle/10183/25516

This project consists of three phases:
      1. Parser 
      2. Translator 
      3. Files Creator

Phase 1- Parser: It reads the .ggx file and extracts the data, storing it in the data structure called Grammar defined in the package GraphGrammar. This structure can represent any strucutre that satisfies the follwoing conditions:
      1. It needs to be a typed grammar, that is, the grammar NEEDS to have a type graph.
      2. Only nodes can have attributes. This was defined during development due to the convention of modelling object as nodes, so it  wouldn't make sense to allow edges to have too. This can be changed in the future.
      3. Due to an AGG limitation, no user defined types are allowed yet. This can also be changed in the future.
This generic structure allows the use of different parsers, meaning that the editor can be changed later, without the need to change the other steps of the program. This phase is ready, with a parser implemented to the editor AGG, available in the following link:    http://www.user.tu-berlin.de/o.runge/agg/index.html
     
Phase 2 - Translator: This phase translates the structure called grammar to another strucut, containing the same grammar defined in the package Event-B. This structure can represent any structure that satisfies the same conditions stated in the previous steps. The use of generic structures as input and output allows this phase, the main one, to be independent of the other two, allowing the change of parser and tester. This phase is currently during late stages of development. The translator was defined as the implementation of the translation proposed in (CAVALHEIRO, 2010) as said above.

Phase 3 - Files Creator: This phase uses the generic Event-B structure created in phase 2 to create the files to be used in the semi-automatic prover. In this research the chosen tester was Rodin. This phase is currently during very early developmente stages.
This phase can only it needs to be changed to allow the use of different tester.


This work is currently during development at Universidade Federal de Pelotas, for the group "Métodos Formais e Fundamentos Matemáticos da Ciência da Computação", more information at: http://dgp.cnpq.br/dgp/espelhogrupo/9814311152255117#
