/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import java.util.ArrayList;
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
    List <Invariant> invariants;
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
        invariants = new ArrayList <>();
        events =  new ArrayList <>();
    }
    
}
