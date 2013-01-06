package Pruebas;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import componentes.Tabla;
import componentes.VentanaLimitada;
import componentes.VentanaOculta;

import general.Config;
import general.Config.Conf_Type;
import general.Global.TiposReemplazo;
import general.MemoryException;
import pckCpu.Cpu;
import pckCpu.Decoder;
import pckCpu.Instruccion;
import pckMemoria.Cache;
import pckMemoria.CacheAsociativa;
import pckMemoria.JerarquiaMemoria;
import pckMemoria.MemoriaPrincipal;

public class TestCpu {
	
	final int palabras_linea = 4;
	
	private JFrame frameMemoria;
	private JFrame frameCache1;
	
	private Tabla tablaMemoria;
	private Tabla tablaCache1;
	
	private MemoriaPrincipal memoria;
	private JerarquiaMemoria jmem;
	private JerarquiaMemoria jmem2;  // Jerarquía de instrucciones.
	private Cache[] caches;
	private Cache[] cache2;  // Caché de instrucciones (por si se separan).
	private Cpu cpu;
	
	public TestCpu()
	{
		// Asigno la primera posición de memoria. Por ejemplo la 800.
		Config.set(Conf_Type.INICIO_INSTRUCCIONES, 0x3A0);
		
		// Leo el código.
		if (!Decoder.decodificarArchivo("Prueba.txt"))
		{
			System.err.println("Error al decodificar el archivo.");
			return;
		}
		
		System.out.println("El archivo se ha procesado correctamente.");
		
		// Comprobamos que hay instrucciones.
		if (Decoder.getInstrucciones().size() == 0)
		{
			System.err.println("No se han encontrado instrucciones.");
			return;
		}
		
		try
		{
			inicializarMemoria();
			inicializarInterfaz();
			inicializarCpu();
			
			int direccion_base = Config.get(Conf_Type.INICIO_INSTRUCCIONES);
			
			// Guardo las instrucciones en memoria.
			System.out.println("Enviando instrucciones a memoria.");
			for (Instruccion inst : Decoder.getInstrucciones())
				memoria.guardarDato(direccion_base+inst.getDireccion(), inst.codificarBinario());
			
			// Asignamos PC a la primera instrucción.
			cpu.setPC(direccion_base+Decoder.getPrimeraInstruccion());
			
			// Una vez tenemos el código guardado en memoria, comenzamos la ejecución.
			cpu.ejecutarCodigoMonociclo();
		}
		catch (MemoryException e)
		{
			System.err.println(e);
		}
		catch (Exception e)
		{
			//e.printStackTrace();
		}
	}
	
	// Inicializa la Cpu.
	private void inicializarCpu()
	{
		cpu = new Cpu(jmem, null);
	}
	
	// Inicializa la Jerarquía de Memoria.
	private void inicializarMemoria() throws MemoryException
	{
		caches = new Cache[1];
		
		caches[0] = new CacheAsociativa(16,palabras_linea,4,TiposReemplazo.LRU);
		memoria = new MemoriaPrincipal(256, palabras_linea);
		
		jmem = new JerarquiaMemoria(caches, memoria);
	}
	
	// Inicializa la interfaz gráfica.
	private void inicializarInterfaz() throws Exception
	{
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
	}
}
