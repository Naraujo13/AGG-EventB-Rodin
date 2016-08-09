/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Classe que representa uma máquina na notação eventB.
 * @author Nícolas Oreques de Araujo
 */
public class Machine {
    String name;
    
    List <Context> sees;
    List <Machine> refinement;
    List <Variable> variables;
    HashMap <String, Invariant> invariants;
    List <Event> events;
    
    /**
     * Cria uma máquina com dado nome. Requer um contexto (pesquisar para ter certeza de obrigatoriedade).
     * @param name - nome da máquina a ser criada
     * @param context - nome do contexto ao qual a máquina está relacionada.
     */
    public Machine (String name, Context context){
        this.name = name;
        sees = new ArrayList <>();
        sees.add(context);
        refinement = new ArrayList <>();
        variables = new ArrayList <>();
        invariants = new HashMap <>();
        events =  new ArrayList <>();
    }
    
    /**
     * Método de adição de variáveis
     * @param v - variável a ser adicionada
     */
    public void addVariable(Variable v){
        variables.add(v);
    }
    
    /**
     * Método para adição de invariantes.
     * @param name - nome a ser associado com a invariante
     * @param inv - invariante a ser adicionada
     */
    public void addInvariant(String name, Invariant inv){
        invariants.put(name, inv);
    }
    
    /**
     * Método para adição de eventos
     * @param e - evento a ser adicionado
     */
    public void addEvent(Event e){
        events.add(e);
    }
}
