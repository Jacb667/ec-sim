/*
 * NO FUNCIONA
 */
package Pruebas;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import componentes.Tabla;
import componentes.VentanaLimitada;
import componentes.VentanaOculta;

import general.Global.TiposReemplazo;
import general.MemoryException;
import pckCpu.CpuMonociclo;
import pckCpu.CpuException;
import pckCpu.Decoder;
import pckCpu.Instruccion;
import pckMemoria.Cache;
import pckMemoria.CacheAsociativa;
import pckMemoria.CacheDirecta;
import pckMemoria.JerarquiaMemoria;
import pckMemoria.MemoriaPrincipal;
import pckMemoria.TablaPaginas;
import pckMemoria.Tlb;

public class TestCpu {
	
	final int palabras_linea = 4;
	
	private JFrame[] framesMemoria;
	private Tabla[] tablasMemoria;
	
	private MemoriaPrincipal memoria;
	private JerarquiaMemoria jmem;
	private JerarquiaMemoria jmem2;  // Jerarquía de instrucciones.
	private Cache[] caches;
	private Cache[] cache2;  // Caché de instrucciones (por si se separan).
	private TablaPaginas tablaPags;
	private Tlb tlb1;
	private Tlb tlb2;
	
	private CpuMonociclo cpu;
	private int direccion_inst = 0;
	
	// CPU
	final String archivo = "Prueba.txt";
	final boolean segmentado = false;
	
	// Niveles de caché
	final int niveles_cache = 2;
	final int[] entradas_caches = new int[]{4,8,8};
	final int[] vias_caches = new int[]{1,4,4};
	
	// Páginas y memoria
	final int entradas_pagina = 16;
	final int max_entrada = 512;	// Última entrada permitida
	final int max_ent_mem = 128;	// Última entrada en memoria (tamaño de memoria)
	final int entradas_tlb = 4;
	final int vias_tlb = 1;
	
	public TestCpu()
	{
		// Leo el código.
		if (!Decoder.decodificarArchivo(archivo))
		{
			System.err.println("Error al decodificar el archivo.");
			return;
		}
		
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
			
			// Guardo un 1 en todas las posiciones entre 0 y 1000.
			for (int i = 0; i <= 1000; i+=4)
				tablaPags.inicializarDatoMemoriaVirtual(i, i);
			
			// Guardo las instrucciones en memoria.
			for (Instruccion inst : Decoder.getInstrucciones())
			{
				if (inst.esDireccionVirtual())
				{
					System.out.println("++ Instruccion " + inst);
					System.out.println("++ Dirección V " + (inst.getDireccion()));
					tablaPags.inicializarDatoMemoriaVirtual(inst.getDireccion(), inst.codificarBinario());
				}
				else
				{
					System.out.println("++ Instruccion " + inst);
					System.out.println("++ Dirección V " + (direccion_inst + inst.getDireccion()));
					tablaPags.inicializarDatoMemoriaVirtual(direccion_inst + inst.getDireccion(), inst.codificarBinario());
				}
			}
			
			// Asignamos PC a la primera instrucción.
			if (Decoder.getPrimeraInstruccion().esDireccionVirtual())
				cpu.setPC(Decoder.getPrimeraInstruccion().getDireccion());
			else
				cpu.setPC(direccion_inst + Decoder.getPrimeraInstruccion().getDireccion());
			
			// Una vez tenemos el código guardado en memoria, comenzamos la ejecución.
			cpu.ejecutarCodigo();
		}
		catch (MemoryException e)
		{
			System.err.println(e);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// Inicializa la Cpu.
	private void inicializarCpu()
	{
		// Calculo la dirección de memoria para instrucciones.
		int num_instrucciones = Decoder.getInstrucciones().size();
		int paginas_instrucciones = (int) Math.ceil(num_instrucciones / entradas_pagina);
		int primera_pag_inst = tablaPags.getNumeroPaginas()-1 - paginas_instrucciones;
		direccion_inst = primera_pag_inst * tablaPags.getEntradasPagina() * 4;
					
		cpu = new CpuMonociclo(jmem, null, direccion_inst);
	}
	
	// Inicializa la Jerarquía de Memoria.
	private void inicializarMemoria() throws MemoryException, CpuException
	{
		caches = new Cache[niveles_cache];
		
		for (int i = 0; i < niveles_cache; i++)
		{
			if (vias_caches[i] > 1)
				caches[i] = new CacheAsociativa(entradas_caches[i], palabras_linea, vias_caches[i], TiposReemplazo.LRU);
			else
				caches[i] = new CacheDirecta(entradas_caches[i], palabras_linea);
		}
		
		tlb1 = new Tlb(entradas_tlb);
		tlb2 = null;

		// Tabla de Páginas
		tablaPags = new TablaPaginas(entradas_pagina, palabras_linea, max_entrada, max_ent_mem, TiposReemplazo.LRU, tlb1, tlb2);
		
		// Memoria principal.
		memoria = new MemoriaPrincipal(tablaPags);
		
		// Inicializar la Jerarquía de Memoria.
		jmem = new JerarquiaMemoria(tablaPags, caches, memoria);
		jmem2 = null;
		
		tablaPags.setJerarquiaMemoria(jmem, jmem2);
	}
	
	// Inicializa la interfaz gráfica.
	private void inicializarInterfaz() throws Exception
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		tablasMemoria = new Tabla[1 + niveles_cache];
		framesMemoria = new JFrame[1 + niveles_cache];
		
		tablasMemoria[0] = new Tabla(memoria);
		framesMemoria[0] = new VentanaLimitada();
		JScrollPane jscroll1 = new JScrollPane(tablasMemoria[0], JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		framesMemoria[0].setTitle("Memoria");
		framesMemoria[0].setPreferredSize( new Dimension(245, 400) );
		framesMemoria[0].setMinimumSize(new Dimension(250, 400));
		framesMemoria[0].setMaximumSize(new Dimension(400, 2000));
		framesMemoria[0].add( jscroll1 );
		framesMemoria[0].pack();
		framesMemoria[0].addWindowListener(new VentanaOculta(framesMemoria[0]));
		framesMemoria[0].setVisible(true);
		memoria.setInterfaz(tablasMemoria[0]);
		
		for (int i = 1; i < 1 + niveles_cache; i++)
		{
			tablasMemoria[i] = new Tabla(caches[i-1]);
			if (vias_caches[i-1] > 1)
				tablasMemoria[i].setRenderTablaEnCelda();
			framesMemoria[i] = new VentanaLimitada();
			JScrollPane jscroll = new JScrollPane(tablasMemoria[i], JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			framesMemoria[i].setTitle("Cache L"+(i-1));
			framesMemoria[i].setPreferredSize( new Dimension(600, 200) );
			framesMemoria[i].setMinimumSize(new Dimension(500, 200));
			framesMemoria[i].setMaximumSize(new Dimension(2000, 2000));
			framesMemoria[i].add( jscroll );
			framesMemoria[i].pack();
			framesMemoria[i].addWindowListener(new VentanaOculta(framesMemoria[i]));
			framesMemoria[i].setVisible(true);
			caches[i-1].setInterfaz(tablasMemoria[i]);
		}
	}
}
