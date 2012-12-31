package general;

import general.Global.TiposReemplazo;

import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
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
		try
		{
			Cache[] caches = new Cache[2];
			
			// 2 niveles de cache directa
			caches[0] = new CacheDirecta(4,4);  // Caché de 4 entradas 4 palabras por línea.
			caches[1] = new CacheAsociativa(16,4,8,TiposReemplazo.LRU);  // Caché de 8 entradas 4 palabras por línea.
			
			// Memoria principal con 128 posiciones.
			MemoriaPrincipal memoria = new MemoriaPrincipal(128);
			
			JerarquiaMemoria jmem = new JerarquiaMemoria(caches, memoria);
			
			// Inicialización de la memoria para hacer pruebas.
			for (int i = 0; i < 128*4; i+=4)
				memoria.guardarDato(i, i);
			
			/*System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));*/
			
			System.out.println("Lectura 0x10: " + jmem.leerDato(0x10));
			
			/*System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));*/
			
			System.out.println("Guardo dato 1000 en 0x20");
			
			jmem.guardarDato(0x20, 1000);
			
			//System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			//System.out.println("Memoria:\n" + memoria.toString(true));
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			tablaMemoria = new Tabla(memoria);
			frameMemoria = new VentanaLimitada();
			JScrollPane jscroll1 = new JScrollPane(tablaMemoria, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			frameMemoria.setTitle("Memoria");
			frameMemoria.setPreferredSize( new Dimension(245, 400) );
			frameMemoria.setMinimumSize(new Dimension(245, 400));
			frameMemoria.setMaximumSize(new Dimension(400, 2000));
			frameMemoria.add( jscroll1 );
			frameMemoria.pack();
			frameMemoria.addWindowListener(new VentanaOculta(frameMemoria));
			frameMemoria.setVisible(true);
			
			tablaCache1 = new Tabla(caches[0]);
			frameCache1 = new VentanaLimitada();
			JScrollPane jscroll2 = new JScrollPane(tablaCache1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			frameCache1.setTitle("Cache L0");
			frameCache1.setPreferredSize( new Dimension(400, 200) );
			frameCache1.setMinimumSize(new Dimension(400, 200));
			frameCache1.setMaximumSize(new Dimension(2000, 2000));
			frameCache1.add( jscroll2 );
			frameCache1.pack();
			frameCache1.addWindowListener(new VentanaOculta(frameCache1));
			frameCache1.setVisible(true);
			
			tablaCache2 = new Tabla(caches[1]);
			tablaCache2.setRenderTablaEnCelda();
			frameCache2 = new VentanaLimitada();
			JScrollPane jscroll3 = new JScrollPane(tablaCache2, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			frameCache2.setTitle("Cache L1");
			frameCache2.setPreferredSize( new Dimension(400, 200) );
			frameCache2.setMinimumSize(new Dimension(400, 200));
			frameCache2.setMaximumSize(new Dimension(2000, 2000));
			frameCache2.add( jscroll3 );
			frameCache2.pack();
			frameCache2.addWindowListener(new VentanaOculta(frameCache2));
			frameCache2.setVisible(true);
			
			//Thread.sleep(10000);
			//frame.setVisible(true);
			
			/*Random r = new Random();
			
			for (int i = 0; i < 9999999; i++)
			{
				for (int j = 0; j < 64; j++)
				{
					int n = r.nextInt(10);
					tabla1.setValueAt(String.valueOf(n), j, 1);
					tabla1.setValueAt(n != 0, j, 2);

					Thread.sleep(50);
				}
			}*/
		}
		/*catch (MemoryException e)
		{
			System.err.println(e);
			e.printStackTrace();
		}*/
		catch (Exception e)
		{
			//e.printStackTrace();
		}
	}

}
