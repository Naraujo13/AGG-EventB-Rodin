/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package EventB;

import Tradutores.AGGToGraphGrammar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  Classe que representa um projeto em EventB, contendo dois hashsets representando conjuntos de contextos e máquinas, além de uma
 *  string representando seu nome.
 * @author Nícolas Oreques de Araujo
 */
public class Project {
    private String name;
    private LinkedHashSet<Context> contexts;
    private LinkedHashSet<Machine> machines;
    
    public Project(String name){
        this.name = name;
        contexts = new LinkedHashSet<>();
        machines = new LinkedHashSet<>();
    }
    
    /**
     * Método para adição de contextos a um projeto eventB
     * @param c - contexto a ser adicionado
     */
    public void addContext(Context c){
        contexts.add(c);
    }
    
    /**
     * Método para adição de máquinas a um projeto eventB
     * @param m - máquina a ser adicionada
     */
    public void addMachine(Machine m){
        machines.add(m);
    }

    public boolean logProject(String logPath, String step2Path){

         /* Testing */
        PrintStream realSystemOut = System.out; //salva systemOut original
        File file;
        file = new File(logPath + "/Project_" + this.name + ".log");
        try {           //Seta output para o arquivo
            PrintStream printStream = new PrintStream(new FileOutputStream(file));
            System.setOut(printStream);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AGGToGraphGrammar.class.getName()).log(Level.SEVERE, null, ex);
        }
        /* End of Testing */

        System.out.println("Imprimindo Projeto " + this.name + "...");

        /* Contexts*/
        if (contexts.isEmpty()){
            System.out.println("\tWarning: este projeto não possui contextos.");
        }
        else{
            System.out.println("\tImprimindo contextos...");
            contexts.forEach(c->{
                if(!c.logContext(step2Path))
                    System.out.println("\t\tContexto " + c.getName() + " falhou.");
            });
        }

        /*Machines*/
        if (machines.isEmpty()){
            System.out.println("\tWarning: este projeto não possui máquinas.");
        }
        else{
            System.out.println("\tImprimindo máquinas: ");
            machines.forEach(m->{
                if (!m.logMachine(step2Path))
                    System.out.println("\t\tMáquina " + m.getName() + " falhou.");
            });
        }

        System.setOut(realSystemOut);
        return true;
    }

}
