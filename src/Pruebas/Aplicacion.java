/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Pruebas;

import java.awt.Dimension;

import general.Config;
import general.Config.Conf_Type;
import general.Config.Conf_Type_c;
import general.Global.TiposReemplazo;
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
	
	/*private static Controlador controlador;
	private static Vista vista;*/
	
    public static void main(String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
    {
    	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    	/*vista = new Vista();
        JFrame ventana = new JFrame("Simulador MIPS v1.1");
        ventana.setMinimumSize(new Dimension(660,405));
        ventana.setPreferredSize(new Dimension(660,405));
        controlador = new Controlador(vista);
        vista.controlador(controlador);
        ventana.setContentPane(vista);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.pack();
        ventana.setVisible(true);*/
    	
    	
    	Config.set(Conf_Type.TAMAÑO_PALABRA, 4);
		Config.set(Conf_Type.TAMAÑO_LINEA, 4);
		
		Config.set(Conf_Type.JERARQUIAS_SEPARADAS, 0);
		//segmentado = Config.get(Conf_Type.SEGMENTADO) == 1 ? true : false;
		
		Config.set(Conf_Type.ENTRADAS_PAGINA, 32);
		Config.set(Conf_Type.NUMERO_ENTRADAS_MEMORIA, 512);
		Config.set(Conf_Type.MAXIMA_ENTRADA_MEMORIA, 1024);
		
		Config.set(Conf_Type_c.ARCHIVO_CODIGO, "Prueba.txt");
		Config.set(Conf_Type_c.ARCHIVO_TRAZA, "");
		
		// Niveles de caché
		Config.set(Conf_Type.NIVELES_CACHE_DATOS, 2);
		Config.set(Conf_Type.NIVELES_CACHE_INSTRUCCIONES, 1);
		
		Config.set(Conf_Type.CACHE1_DATOS_ENTRADAS, 8);
		Config.set(Conf_Type.CACHE2_DATOS_ENTRADAS, 32);
		Config.set(Conf_Type.CACHE3_DATOS_ENTRADAS, 64);
		Config.set(Conf_Type.CACHE1_DATOS_VIAS, 1);
		Config.set(Conf_Type.CACHE2_DATOS_VIAS, 4);
		Config.set(Conf_Type.CACHE3_DATOS_VIAS, 4);	
		Config.set(Conf_Type.CACHE1_INSTRUCCIONES_ENTRADAS, 16);
		Config.set(Conf_Type.CACHE2_INSTRUCCIONES_ENTRADAS, 16);
		Config.set(Conf_Type.CACHE3_INSTRUCCIONES_ENTRADAS, 16);
		Config.set(Conf_Type.CACHE1_INSTRUCCIONES_VIAS, 1);
		Config.set(Conf_Type.CACHE2_INSTRUCCIONES_VIAS, 1);
		Config.set(Conf_Type.CACHE3_INSTRUCCIONES_VIAS, 1);

		Config.set(Conf_Type_c.CACHE1_DATOS_POLITICA, "LRU");
		Config.set(Conf_Type_c.CACHE2_DATOS_POLITICA, "LRU");
		Config.set(Conf_Type_c.CACHE3_DATOS_POLITICA, "LRU");
		
		Config.set(Conf_Type_c.CACHE1_INSTRUCCIONES_POLITICA, "LRU");
		Config.set(Conf_Type_c.CACHE2_INSTRUCCIONES_POLITICA, "LRU");
		Config.set(Conf_Type_c.CACHE3_INSTRUCCIONES_POLITICA, "LRU");

		Config.set(Conf_Type.TLB_DATOS, 1);
		Config.set(Conf_Type.TLB_INSTRUCCIONES, 0);
		
		Config.set(Conf_Type.TLB_DATOS_ENTRADAS, 16);
		Config.set(Conf_Type.TLB_DATOS_VIAS, 2);
		
		Config.set(Conf_Type_c.TLB_DATOS_POLITICA, "LRU");
		
		Config.set(Conf_Type.TLB_INSTRUCCIONES_ENTRADAS, 8);
		Config.set(Conf_Type.TLB_INSTRUCCIONES_VIAS, 1);
		
		Config.set(Conf_Type_c.TLB_INSTRUCCIONES_POLITICA, "LRU");

    	new TestCpu();
    }
}
