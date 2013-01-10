package Pruebas;

import general.Log;
import general.MemoryException;
import general.Global.TiposReemplazo;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import componentes.Tabla;
import componentes.VentanaLimitada;
import componentes.VentanaOculta;

import pckMemoria.*;

public class TestMemoria2 {
	
	public static JFrame frameMemoria;
	public static JFrame frameCache1;
	public static JFrame frameCache2;
	public static JFrame frameCache3;
	
	public static Tabla tablaMemoria;
	public static Tabla tablaCache1;
	public static Tabla tablaCache2;
	public static Tabla tablaCache3;
	
	public TestMemoria2()
	{
		final int palabras_linea = 8;
		
		try
		{
			Cache[] caches = new Cache[3];
			
			// 2 niveles de cache directa
			caches[0] = new CacheDirecta(16,palabras_linea);  // Caché de 4 entradas 4 palabras por línea.
			caches[1] = new CacheAsociativa(64,palabras_linea,8,TiposReemplazo.LRU);  // Caché de 8 entradas 4 palabras por línea.
			caches[2] = new CacheAsociativa(128,palabras_linea,16,TiposReemplazo.LRU);
			
			// Memoria principal con 128 posiciones.
			MemoriaPrincipal memoria = new MemoriaPrincipal(2048, palabras_linea);
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			tablaMemoria = new Tabla(memoria);
			frameMemoria = new VentanaLimitada();
			JScrollPane jscroll1 = new JScrollPane(tablaMemoria, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			frameMemoria.setTitle("Memoria");
			frameMemoria.setPreferredSize( new Dimension(245, 400) );
			frameMemoria.setMinimumSize(new Dimension(250, 400));
			frameMemoria.setMaximumSize(new Dimension(400, 2000));
			frameMemoria.add( jscroll1 );
			frameMemoria.pack();
			frameMemoria.addWindowListener(new VentanaOculta(frameMemoria));
			frameMemoria.setVisible(true);
			memoria.setInterfaz(tablaMemoria);
			
			tablaCache1 = new Tabla(caches[0]);
			frameCache1 = new VentanaLimitada();
			JScrollPane jscroll2 = new JScrollPane(tablaCache1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			frameCache1.setTitle("Cache L0");
			frameCache1.setPreferredSize( new Dimension(600, 200) );
			frameCache1.setMinimumSize(new Dimension(500, 200));
			frameCache1.setMaximumSize(new Dimension(2000, 2000));
			frameCache1.add( jscroll2 );
			frameCache1.pack();
			frameCache1.addWindowListener(new VentanaOculta(frameCache1));
			frameCache1.setVisible(true);
			caches[0].setInterfaz(tablaCache1);
			
			tablaCache2 = new Tabla(caches[1]);
			tablaCache2.setRenderTablaEnCelda();
			frameCache2 = new VentanaLimitada();
			JScrollPane jscroll3 = new JScrollPane(tablaCache2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			frameCache2.setTitle("Cache L1");
			frameCache2.setPreferredSize( new Dimension(800, 500) );
			frameCache2.setMinimumSize(new Dimension(500, 300));
			frameCache2.setMaximumSize(new Dimension(2000, 2000));
			frameCache2.add( jscroll3 );
			frameCache2.pack();
			frameCache2.addWindowListener(new VentanaOculta(frameCache2));
			frameCache2.setVisible(true);
			caches[1].setInterfaz(tablaCache2);
			
			tablaCache3 = new Tabla(caches[2]);
			tablaCache3.setRenderTablaEnCelda();
			frameCache3 = new VentanaLimitada();
			JScrollPane jscroll4 = new JScrollPane(tablaCache3, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			frameCache3.setTitle("Cache L2");
			frameCache3.setPreferredSize( new Dimension(800, 500) );
			frameCache3.setMinimumSize(new Dimension(500, 300));
			frameCache3.setMaximumSize(new Dimension(2000, 2000));
			frameCache3.add( jscroll4 );
			frameCache3.pack();
			frameCache3.addWindowListener(new VentanaOculta(frameCache3));
			frameCache3.setVisible(true);
			caches[2].setInterfaz(tablaCache3);
			
			JerarquiaMemoria jmem = new JerarquiaMemoria(caches, memoria);
			
			// Inicialización de la memoria para hacer pruebas.
			for (int i = 0; i < 2048*4; i+=4)
				memoria.guardarDato(i, i);
			
			/*System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));*/
			
			//System.out.println("Lectura 0x10: " + jmem.leerDato(0x10));
			
			/*System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));*/
			
			//System.out.println("Guardo dato 1000 en 0x20");
			
			//jmem.guardarDato(0x20, 1000);
			
			//System.out.println("Cache L0:\n" + caches[0].toString());
			//System.out.println("Cache L1:\n" + caches[1].toString());
			//System.out.println("Memoria:\n" + memoria.toString(true));
			
			/*Object pr = tablaCache2.getValueAt(0, 1);
			if (pr.getClass().isArray())
			{
				Object[] passed = (Object[])pr;
				System.out.println(Arrays.toString(passed));
			}
			else
			{
				System.out.println(pr);
			}
			*/

			//frame.setVisible(true);
			
			Random r = new Random();
			
			int[] temp = new int[2048];
			
			for (int i = 0; i < 2048; i++)
				temp[i] = i*4;
			
			System.out.println(Arrays.toString(temp));
			
			int validos = 0;
			int incorrectos = 0;
			
			Thread.sleep(1000);
			
			for (int i = 0; i < 1000; i++)
			{
				int dir = r.nextInt(2048*4);
				int dato = r.nextInt(1000000);
				
				System.out.println("Guardo dato " + dato + " en dirección 0x" + Integer.toHexString(dir));
				jmem.guardarDato(dir, dato);
				temp[dir >> 2] = dato;
				
				System.out.println("-------------");
				
				//Thread.sleep(1000);
				
				int dir2 = r.nextInt(2048*4);
				
				System.out.print("Leo dirección 0x" + Integer.toHexString(dir2) + "  ");
				int dato2 = jmem.leerDato(dir2);
				System.out.println(dato2);
				if (temp[dir2 >> 2] == dato2)
					validos++;
				else
					incorrectos++;
				
				System.out.println("-------------");
				
				//Thread.sleep(1000);
			}
			
			System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));
			
			float ratio_l0 = (float)(Log.cache_hits[0]*100) / (float)(Log.accesosMemoria);
			float ratio_l1 = (float)(Log.cache_hits[1]*100) / (float)(Log.accesosMemoria-Log.cache_hits[0]);
			float ratio_l2 = (float)(Log.cache_hits[2]*100) / (float)(Log.accesosMemoria-Log.cache_hits[0]-Log.cache_hits[1]);
			
			System.out.println("Correctos " + validos + "/" + (validos+incorrectos));
			System.out.println("Accesos a memoria: " + Log.accesosMemoria + " (" + 
					Log.lecturasMemoria + " lecturas + " + Log.escriturasMemoria + " escrituras)");
			System.out.println("Accesos a bloques: " + Log.accesosBloques + " (" + 
					Log.lecturasBloques + " leidos + " + Log.escriturasBloques + " escritos)");
			System.out.printf("Cache hits: %s - Cache misses: %s \n", Arrays.toString(Log.cache_hits), Arrays.toString(Log.cache_misses));
			System.out.printf("Ratio acierto: [%.2f%%, %.2f%%, %.2f%%] \n", ratio_l0, ratio_l1, ratio_l2);
			
			System.out.println(Arrays.toString(temp));
			
			Thread.sleep(5000);
			
			List<LineaReemplazo> lineasR = caches[0].invalidarPagina(0);
			for (LineaReemplazo lin : lineasR)
				jmem.actualizarLinea(lin, 0);
			
			System.out.println(caches[0]);
		}
		catch (MemoryException e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
