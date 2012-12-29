package general;

import java.awt.Dimension;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import componentes.Tabla;
import componentes.WindowCloser;

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
			for (int i = 0; i < 128*2; i+=4)
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
			
			Tabla tabla1 = new Tabla(memoria.getDatos(), memoria.getColumnas());
			
			JFrame frame = new JFrame();
			frame.setTitle("Memoria");
			frame.setPreferredSize( new Dimension(280, 400) );
			frame.setMinimumSize(new Dimension(280, 400));
			//frame.setResizable(false);
			
			JScrollPane scrollPane = new JScrollPane( tabla1 );
			frame.add( scrollPane );
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			frame.pack();
			frame.addWindowListener(new WindowCloser(frame));
			frame.setVisible(true);
			
			Thread.sleep(10000);
			frame.setVisible(true);
			
			Random r = new Random();
			
			for (int i = 0; i < 9999999; i++)
			{
				for (int j = 0; j < 64; j++)
				{
					int n = r.nextInt(20);
					tabla1.setValueAt(String.valueOf(n), j, 1);
					tabla1.setValueAt(n != 0, j, 2);

					//Thread.sleep(50);
				}
			}
		}
		catch (MemoryException e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
