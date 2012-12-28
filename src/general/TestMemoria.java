package general;

import interfazgrafica.TablaInterfaz;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import componentes.Tabla;

import pckMemoria.*;

public class TestMemoria {
	
	public TestMemoria()
	{
		try
		{
			Cache[] caches = new Cache[2];
			
			// 2 niveles de cache directa
			caches[0] = new CacheDirecta(4,4);  // Caché de 4 entradas 4 palabras por línea.
			caches[1] = new CacheDirecta(8,4);  // Caché de 8 entradas 4 palabras por línea.
			
			// Memoria principal con 128 posiciones.
			MemoriaPrincipal memoria = new MemoriaPrincipal(128);
			
			JerarquiaMemoria jmem = new JerarquiaMemoria(caches, memoria);
			
			// Inicialización de la memoria para hacer pruebas.
			for (int i = 0; i < 128*4; i+=4)
				memoria.guardarDato(i, i);
			
			System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));
			
			System.out.println("Lectura 0x10: " + jmem.leerDato(0x10));
			
			System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));
			
			System.out.println("Guardo dato 1000 en 0x20");
			
			jmem.guardarDato(0x20, 1000);
			
			System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));
			
			/*JFrame frame = new JFrame();
			frame.setTitle( "Memoria" );
			frame.setSize( 300, 200 );
			
			JPanel topPanel = new JPanel();
			topPanel.setLayout( new BorderLayout() );
			frame.getContentPane().add( topPanel );
			
			JTable table = memoria.crearJTable();
			
			JScrollPane scrollPane = new JScrollPane( table );
			topPanel.add( scrollPane, BorderLayout.CENTER );
			
			frame.setVisible(true);
			
			JFrame frame2 = new JFrame();
			frame2.setTitle( "Cache 0" );
			frame2.setSize( 300, 200 );
			
			JPanel topPanel2 = new JPanel();
			topPanel2.setLayout( new BorderLayout() );
			frame2.getContentPane().add( topPanel2 );
			
			JTable table2 = memoria.crearJTable();
			
			JScrollPane scrollPane2 = new JScrollPane( table2 );
			topPanel2.add( scrollPane2, BorderLayout.CENTER );
			
			frame2.setVisible(true);*/
			
			Tabla tabla1 = new Tabla(memoria.getDatos(), memoria.getColumnas());
			
			JFrame frame = new JFrame();
			frame.setTitle( "Memoria" );
			frame.setSize( 300, 200 );
			
			JPanel topPanel = new JPanel();
			topPanel.setLayout( new BorderLayout() );
			frame.getContentPane().add( topPanel );
			
			JScrollPane scrollPane = new JScrollPane( tabla1 );
			topPanel.add( scrollPane, BorderLayout.CENTER );
			
			frame.setVisible(true);
			
	
		}
		catch (MemoryException e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
	}

}
