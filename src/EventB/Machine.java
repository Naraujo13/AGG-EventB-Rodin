/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Classe que representa uma máquina na notação eventB.
 * @author Nícolas Oreques de Araujo
 */
public class Machine {
    private String name;
    
    private ArrayList <Context> sees;
    private ArrayList <Machine> refinement;
    private ArrayList <Variable> variables;
    private HashMap <String, Invariant> invariants;
    private ArrayList <Event> events;

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

    public String getName() {
        return name;
    }

    public ArrayList<Context> getSees() {
        return sees;
    }

    public ArrayList<Machine> getRefinement() {
        return refinement;
    }

    public ArrayList<Variable> getVariables() {
        return variables;
    }

    public HashMap<String, Invariant> getInvariants() {
        return invariants;
    }

    public ArrayList<Event> getEvents() {
        return events;
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

    public boolean logMachine(String path){


        FileWriter copy = null;
        try{

            //Open file
            final FileWriter machineFile = new FileWriter( path + "/" + name + "_Machine.txt");
            copy = machineFile;
            //Log
            System.out.println("\t\tTraduzindo Machine " + name + "...");


            /* -- Start -- */
            machineFile.write("machine ");
            machineFile.write(name + "\n\n\n");

            /* -- Refines -- */
            System.out.println("\t\tTraduzindo Refines " + name + "...");
            if (!refinement.isEmpty()){
                machineFile.write(" refines");
                refinement.forEach(m->{
                    try {
                        machineFile.write(" " + m.getName());
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                });
                machineFile.write(" ");
            }

            /* -- Sees -- */
            System.out.println("\t\tTraduzindo Sees " + name + "...");
            if (!sees.isEmpty()){
                machineFile.write((" sees"));
                sees.forEach(c->{
                    try{
                        machineFile.write(" " + c.getName());
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                });
            }


            machineFile.write("\n\n\n");


            /* -- Variables -- */
            //Log
            System.out.println("\t\t\tTraduzindo Variables... ");

            machineFile.write("variables");

            //Writes each variable name
            variables.forEach(v->{
                try {
                    machineFile.write(" "  + v.getName());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });
            machineFile.write("\n\n");


            /* -- Invariants -- */
            //Log
            System.out.println("\t\t\tTraduzindo Invariants...");

            machineFile.write("invariants\n");

            //Writes each constant name
            invariants.forEach((l, i)->{
                try {
                    machineFile.write("  @" + l + " " + i.getPredicate() + "\n\n");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });
            machineFile.write("\n\n");


            /* -- Events -- */
            //Log
            System.out.println("\t\t\tTraduzindo Events...");

            machineFile.write("events\n");

            //Writes each Event
            events.forEach((e)->{
                try {
                    //Start
                    machineFile.write("  event " + e.getName());

                    //Extends
                    if (e.getExtend() != null)
                        machineFile.write(" extends " + e.getExtend().getName() + "\n");

                    //Any
                    if (!e.getParameters().isEmpty())
                        machineFile.write("    any\n");

                    //Write each parameter
                    e.getParameters().forEach((p)->{
                        try{
                            machineFile.write("      " + p + "\n");
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                    });

                    //Where
                    if (!e.getGuards().isEmpty())
                        machineFile.write("    where\n");

                    //Write each guard
                    e.getGuards().forEach((l, p)->{
                        try{
                            machineFile.write("      @" + l + " " + p + "\n");
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                    });

                    //Then
                    if (!e.getActs().isEmpty())
                        machineFile.write("    then\n");

                    //Write each act
                    e.getActs().forEach((l, p)->{
                        try{
                            machineFile.write("      @" + l + " " + p + "\n");
                        }
                        catch (Exception ex){
                            ex.printStackTrace();
                        }
                    });

                    //End
                    machineFile.write("  end\n\n");
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            });
            machineFile.write("\n");


            /* -- End -- */
            machineFile.write("end\n");
            //Log
            System.out.println("\t\t--- FIM ---");

        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            try {
                if (copy != null)
                    copy.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return true;
    }
}
