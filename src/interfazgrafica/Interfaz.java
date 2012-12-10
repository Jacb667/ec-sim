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
        NewJPanel p=new NewJPanel();
        JFrame ventana=new JFrame("Programa");
        ventana.setContentPane(p);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.pack();
        ventana.setVisible(true);
        
    }
}
