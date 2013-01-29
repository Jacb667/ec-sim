/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;

import java.awt.Dimension;

import interfazgrafica.Controlador;
import interfazgrafica.Vista;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


/**
 *
 * @author José Andrés Cordero
 * @author Manuel Álvarez
 */
public class Aplicacion extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
	
    public static void main(String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
    {
    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	Vista v = new Vista();
        JFrame ventana = new JFrame("Simulador MIPS v1.1");
        ventana.setMinimumSize(new Dimension(660,405));
        ventana.setPreferredSize(new Dimension(660,405));
        Controlador ctr = new Controlador(v);
        v.controlador(ctr);
        ventana.setContentPane(v);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.pack();
        ventana.setVisible(true);
        
        // Main
        //new Test();
    	//new TestMemoria();
    	//new TestMemoria2();
    	//new TestCpu();
    	//new TestTraza();
    	//new TestTraductor();
    }
}
