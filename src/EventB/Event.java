/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Nícolas Oreques de Araujo
 */
public class Event {
    
    private String name; // -- Nome do evento
    private Event extend;    // -- Evento que será extendido, caso exista
    private ArrayList<String> parameters; // -- Lista de Parâmetros do Evento
    private HashMap<String,String> acts;    // -- Lista de Acts do Evento (Label -> Act)
    private HashMap<String,String> guards;  // -- Lista de Guardas do Evento (Label -> Guarda)
    
    
    public Event(String name){
        this.name = name;
        parameters = new ArrayList<>();
        acts = new HashMap<>();
        guards = new HashMap<>();
        extend = null;
    }

    /**
     * Método para quando um evento extende outro
     * @param event - evento a ser extendido
     */
    public void setExtend(Event event){
        extend = event;
    }

    /**
     * Método de adição de ações ao evento.
     * @param name - label da ação a ser adicionada
     * @param predicate - predicado da açaõ a ser adicinada
     */
    public void addAct(String name, String predicate){
        acts.put(name, predicate);
    }
    
    /**
     * Método para adição de parâmetros ao evento.
     * @param parameter - parâmetro a ser adicionado.
     */
    public void addParameter(String parameter){
        parameters.add(parameter);
    }
    
    /**
     * Método para adição de guardas ao evento
     * @param name - label da guarda
     * @param predicate - predicado da guarda
     */
    public void addGuard(String name, String predicate){
        guards.put(name, predicate);
    }

    public String getName() {
        return name;
    }

    /**
     * Método para acesso de evento que o atual estende
      * @return - retorna evento estendido
     */
    public Event getExtend() {
        return extend;
    }

    /**
     * Método de acesso aos parâmetros do evento
     * @return - retorna ArrayList com os parâmetros do evento
     */
    public ArrayList<String> getParameters() {
        return parameters;
    }

    /**
     * Método de acesso as acts do evento
     * @return - retorna HashMap contendo os acts do evento (Label -> Act)
     */
    public HashMap<String, String> getActs() {
        return acts;
    }

    /**
     * Método de acesso as guardas do evento
     * @return - retorna HashMap contendo as guardas do evento (Label -> Guarda)
     */
    public HashMap<String, String> getGuards() {
        return guards;
    }



}
