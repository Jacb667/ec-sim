/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;

import interfazgrafica.Controlador;
import interfazgrafica.Vista;

import javax.swing.JFrame;


/**
 *
 * @author José Andrés
 */
public class Aplicacion extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
	
    public static void main(String args[]) {
    	 Vista v=new Vista();
         JFrame ventana=new JFrame("Programa");
         Controlador ctr= new Controlador(v);
         v.controlador(ctr);
         ventana.setContentPane(v);
         ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         ventana.pack();
         ventana.setVisible(true);
         
        
        // Main
        //new Test();
    	//new TestMemoria();
    	//new TestMemoria2();
    	new TestCpu();
    	//new TestTraza();
    	//new TestTraductor();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
