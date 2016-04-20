# AGG-Rodin
Given a  graph grammar defined in AGG (.ggx) using a type graph, converts the file to a new one,  usable in Rodin,
containing the same definition.
It reads the .ggx file and extracts the data found, storing it in the data structure called Grammar.
Grammar is a data structure containing a reference to a TypeGraph, a reference to a Graph called host, and ArrayList of Rules.
A Graph contains an ArrayList of Nodes, an ArrayList of Edges and an HashMap representing the possible Morphism.
An TypeGraph contains an ArrayList of TypeNodes, an ArrayList of TypeEdges and a HashMap called translation.
This HashMaps is used internally to translate some of the nodes IDs, since the AGG makes a confusion in the .ggx associating 
two IDS to the type graph nodes and edges, and referencing both of them sometimes. This HashMap solves this by translating them
to one ID called type.

Work in progress...

Given the current state of development, the following conditions must be applied:
The graph definided in AGG HAS to be defined by a type graph.
It can contain as many rules as wanted, but cant have an Constraint, atomic or not.
