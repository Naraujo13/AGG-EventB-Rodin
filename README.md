# AGG-Rodin
Given a  graph grammar defined in AGG (.ggx) using a type graph, converts the file to a new one,  usable in Rodin,
containing the same definition.
It reads the .ggx file and extracts the data, storing it in the data structure called Grammar defined in the package AGG (temporary name).
From this datastrucuture, it can convert it to a new data structere in EventB notation (in progress) using structers from the EventB package. Furthermore, the goal is to implement a third package that creates Rodin Files from this EventB data structure. The project was divided in differentes structres so it can be reused for other programs, aside Rodin and AGG.


Data Structures:
  -> Grammar is a data structure containing a reference to a TypeGraph, a reference to a Graph called host, and ArrayList of Rules.
  -> A Graph contains an ArrayList of Nodes, an ArrayList of Edges and an HashMap representing the possible Morphism.
  ->An TypeGraph contains an ArrayList of TypeNodes, an ArrayList of TypeEdges and a HashMap called translation.
    This HashMaps is used internally to translate some of the nodes IDs, since the AGG makes a confusion in the .ggx associating 
    two IDS to the type graph nodes and edges, and referencing both of them sometimes. This HashMap solves this by translating them
    to one ID called type.
Work in progress...

Given the current state of development, the following conditions must be applied:
The graph definided in AGG HAS to be defined by a type graph.
It can contain as many rules as wanted, but cant have an Constraint, wheter it being atomic or not.
There is support for attributes in nodes, although that isn't true for edges. It was a project decision since the research who aims to use the program does not use it, thogh it can be added as an extra after the end of the original project.
