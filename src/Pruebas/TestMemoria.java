package Pruebas;

import general.Log;
import general.MemoryException;
import general.Global.TiposReemplazo;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import componentes.Tabla;
import componentes.VentanaLimitada;
import componentes.VentanaOculta;

import pckMemoria.*;

public class TestMemoria {
	
	public static JFrame frameMemoria;
	public static JFrame frameCache1;
	public static JFrame frameCache2;
	public static JFrame frameCache3;
	
	public static Tabla tablaMemoria;
	public static Tabla tablaCache1;
	public static Tabla tablaCache2;
	public static Tabla tablaCache3;
	
	public TestMemoria()
	{
		final int palabras_linea = 4;
		final int entradas_cache1 = 8;
		final int entradas_cache2 = 16;
		final int entradas_cache3 = 32;
		final int vias_cache1 = 1;
		final int vias_cache2 = 8;
		final int vias_cache3 = 16;
		final int entradas_pagina = 128;	// 32 direcciones
		final int max_entrada = 16384;	// Última entrada permitida
		final int max_ent_mem = 1024;	// Última entrada en memoria (tamaño de memoria)
		
		try
		{
			Cache[] caches = new Cache[3];
			
			// 2 niveles de cache directa
			caches[0] = new CacheDirecta(entradas_cache1,palabras_linea);
			caches[1] = new CacheAsociativa(entradas_cache2,palabras_linea,vias_cache2,TiposReemplazo.LRU);
			caches[2] = new CacheAsociativa(entradas_cache3,palabras_linea,vias_cache3,TiposReemplazo.RANDOM);
			
			// Tabla de Páginas
			TablaPaginas tablaPags = new TablaPaginas(entradas_pagina, palabras_linea, max_entrada, max_ent_mem, TiposReemplazo.LRU);
			
			// Memoria principal con 128 posiciones.
			MemoriaPrincipal memoria = new MemoriaPrincipal(tablaPags);
			
			// Inicializar la Jerarquía de Memoria.
			JerarquiaMemoria jmem = new JerarquiaMemoria(tablaPags, caches, memoria);
			
			// Inicializar interfaz de memoria.
			inicializarInterfazMemoria(caches, memoria);
			
			// Inicialización de la memoria para hacer pruebas.
			for (int i = 0; i < max_entrada*4; i+=4)
				memoria.guardarDato(tablaPags.traducirDireccion(i).getReal(), i);
			
			Random r = new Random();
			
			System.out.println(tablaPags);
			
			Thread.sleep(2000);
			
			/*System.out.println("Leo 0-" + jmem.leerDato(0));
			System.out.println("Leo 32-" + jmem.leerDato(32));
			System.out.println("Leo 64-" + jmem.leerDato(64));
			System.out.println("Leo 96-" + jmem.leerDato(96));
			System.out.println("Leo 128-" + jmem.leerDato(128));
			System.out.println("Leo 160-" + jmem.leerDato(160));*/
			
			// Lanzo 1000 pruebas de lectura.
			
			int correctos = 0;
			int realizados = 10000;
			
			for (int i = 0; i < realizados; i++)
			{
				int dir = r.nextInt(max_entrada*4);
				int dato = jmem.leerDato(dir);
				
				// Para eliminar offset.
				dir = dir >> 2;
				dir = dir << 2;
				
				System.out.println(dir + " " + dato);
				
				if (dato == dir)
					correctos++;
			}
			
			System.out.println("Correctos: " + correctos + "/" + realizados);
			
			/*for (int i = 0; i < 10000; i++)
			{
				int dir = r.nextInt(max_entrada*4);
				int dato = r.nextInt(1000000);
				
				System.out.println("Guardo dato " + dato + " en dirección 0x" + Integer.toHexString(dir));
				jmem.guardarDato(dir, dato);
				
				System.out.println("-------------");
				
				//Thread.sleep(100);
				
				int dir2 = r.nextInt(max_entrada*4);
				
				System.out.print("Leo dirección 0x" + Integer.toHexString(dir2) + "  ");
				int dato2 = jmem.leerDato(dir2);
				System.out.println(dato2);
				
				System.out.println("-------------");
				
				//Thread.sleep(100);
			}*/
			
			float ratio_l0 = (float)(Log.cache_hits[0]*100) / (float)(Log.accesosMemoria);
			float ratio_l1 = (float)(Log.cache_hits[1]*100) / (float)(Log.accesosMemoria-Log.cache_hits[0]);
			float ratio_l2 = (float)(Log.cache_hits[2]*100) / (float)(Log.accesosMemoria-Log.cache_hits[0]-Log.cache_hits[1]);
			
			System.out.println("Accesos a memoria: " + Log.accesosMemoria + " (" + 
					Log.lecturasMemoria + " lecturas + " + Log.escriturasMemoria + " escrituras)");
			System.out.println("Accesos a bloques: " + Log.accesosBloques + " (" + 
					Log.lecturasBloques + " leidos + " + Log.escriturasBloques + " escritos)");
			System.out.printf("Cache hits: %s - Cache misses: %s \n", Arrays.toString(Log.cache_hits), Arrays.toString(Log.cache_misses));
			System.out.printf("Ratio acierto: [%.2f%%, %.2f%%, %.2f%%] \n", ratio_l0, ratio_l1, ratio_l2);
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

	private void inicializarInterfazMemoria(Cache[] caches, MemoriaPrincipal memoria) 
			throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		tablaMemoria = new Tabla(memoria);
		frameMemoria = new VentanaLimitada();
		JScrollPane jscroll1 = new JScrollPane(tablaMemoria, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frameMemoria.setTitle("Memoria");
		frameMemoria.setPreferredSize(new Dimension(350, 400) );
		frameMemoria.setMinimumSize(new Dimension(300, 400));
		frameMemoria.setMaximumSize(new Dimension(2000, 2000));
		frameMemoria.add( jscroll1 );
		frameMemoria.pack();
		frameMemoria.addWindowListener(new VentanaOculta(frameMemoria));
		frameMemoria.setVisible(true);
		memoria.setInterfaz(tablaMemoria);
		
		tablaCache1 = new Tabla(caches[0]);
		frameCache1 = new VentanaLimitada();
		JScrollPane jscroll2 = new JScrollPane(tablaCache1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		frameCache1.setTitle("Cache L0");
		frameCache1.setPreferredSize(new Dimension(600, 200) );
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
		frameCache2.setPreferredSize(new Dimension(800, 500) );
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
	}
}
