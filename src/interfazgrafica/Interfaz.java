package interfazgrafica;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Manuel
 */
public class Interfaz {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Vista v=new Vista();
        JFrame ventana=new JFrame("Programa");
        Controlador ctr= new Controlador(v);
        v.controlador(ctr);
        ventana.setContentPane(v);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.pack();
        ventana.setVisible(true);
        
    }
}
