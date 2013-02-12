
package cpu;


import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import memoria.Cache;
import memoria.CacheAsociativa;
import memoria.CacheDirecta;
import memoria.DecoderRam;
import memoria.JerarquiaMemoria;
import memoria.MemoriaPrincipal;
import memoria.TablaPaginas;
import memoria.Tlb;
import cpu.CpuMonociclo;
import cpu.Decoder;
import cpu.Instruccion;

import general.Config.Conf_Type;
import general.Config.Conf_Type_c;
import general.Global.Funcion;
import general.Global.TiposReemplazo;
import general.Config;
import general.CpuException;
import general.Log;
import general.MemoryException;
import gui.Tabla;
import gui.VentanaLimitada;
import gui.VentanaOculta;
import gui.Vista;

public class ClasePrincipal implements Runnable {
	
	private int bytes_palabra;
	private int palabras_linea;
	
	private int nivelJerarquiasSeparadas;
	
	public JFrame frameMemoria;
	public Tabla tablaMemoria;
	
	public JFrame[] framesCache1;
	public Tabla[] tablasCache1;
	
	public JFrame[] framesCache2;
	public Tabla[] tablasCache2;
	
	private MemoriaPrincipal memoria;
	private JerarquiaMemoria jmem;
	private JerarquiaMemoria jmem2;  // Jerarquía de instrucciones.
	private Cache[] caches1;
	private Cache[] caches2;  // Caché de instrucciones (por si se separan).
	private TablaPaginas tablaPags;
	private Tlb tlb1;
	private Tlb tlb2;
	
	private CpuMonociclo cpu;
	private int direccion_inst = 0;
	private int direccion_tablPags = 0;
	private TiposReemplazo politica_tp;
	
	private int niveles_cache1;
	private int niveles_cache2;
	private int entradas_caches1[];
	private int entradas_caches2[];
	private int vias_caches1[];
	private int vias_caches2[];
	private TiposReemplazo politicas_caches1[];
	private TiposReemplazo politicas_caches2[];
	
	private boolean tlb_datos;
	private boolean tlb_inst;
	
	private boolean tp_alojada;
	
	private Funcion funcion;
	private boolean detenido;
	
	// CPU
	private String archivo_cpu;
	private String archivo_traza;
	private String archivo_memoria;
	//private boolean segmentado = false;
	
	// Páginas y memoria
	private int entradas_pagina;
	private int max_entrada;
	private int max_ent_mem;

	private int tlb1_entradas;
	private int tlb1_vias;
	private int tlb2_entradas;
	private int tlb2_vias;
	private TiposReemplazo tlb1_politica;
	private TiposReemplazo tlb2_politica;
	//Traza
	private String t;
	private Traza traza;
	
	public ClasePrincipal()
	{
		leerConfig();
	}
	
	private void validarCodigo()
	{
		// Leo el código.
		if (!Decoder.decodificarArchivo(archivo_cpu))
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
			tablaPags.generarPaginasTablas(direccion_tablPags);
			
			// Guardo las instrucciones en memoria.
			for (Instruccion inst : Decoder.getInstrucciones())
			{
				if (inst.esDireccionVirtual())
				{
					Log.println(3, inst.getDireccion() + " : " + inst, Color.BLACK, false);
					tablaPags.inicializarDatoMemoriaVirtual(inst.getDireccion(), inst.codificarBinario());
				}
				else
				{
					Log.println(3, (direccion_inst + inst.getDireccion()) + " : " + inst, Color.BLACK, false);
					tablaPags.inicializarDatoMemoriaVirtual(direccion_inst + inst.getDireccion(), inst.codificarBinario());
				}
			}
			
			if (archivo_memoria != null && !archivo_memoria.equals(""))
			{
				DecoderRam dram = new DecoderRam(tablaPags);
				dram.decodeFile(archivo_memoria);
			}
			
			// Asignamos PC a la primera instrucción.
			if (Decoder.getPrimeraInstruccion().esDireccionVirtual())
				cpu.setPC(Decoder.getPrimeraInstruccion().getDireccion());
			else
				cpu.setPC(direccion_inst + Decoder.getPrimeraInstruccion().getDireccion());
		}
		catch (MemoryException e)
		{
			Vista v = Config.getVista();
			if (v == null)
				System.err.println(e);
			else
				JOptionPane.showMessageDialog( v, e, "Error en la lectura del fichero", JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
		catch (Exception e)
		{
			Vista v = Config.getVista();
			if (v != null)
				JOptionPane.showMessageDialog( v, e, "Se ha producido una excepción", JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
	}
	
	private void ejecutarCodigo()
	{
		// Una vez tenemos el código guardado en memoria, comenzamos la ejecución.
		try
		{
			cpu.ejecutarCodigo();
		}
		catch (MemoryException | CpuException e)
		{
			Vista v = Config.getVista();
			if (v == null)
				System.err.println(e);
			else
				JOptionPane.showMessageDialog( v, e, "Se ha producido un error", JOptionPane.ERROR_MESSAGE );
		}
		
		if (!detenido)
			Log.generarEstadistica();
		
		detenido = false;
	}
	
	public void ejecutarCicloCodigo()
	{
		try
		{
			boolean ejecutando = cpu.ejecutarInstruccion();
			if (!ejecutando)
			{
				Log.println(1,"Fin de programa.\n\n", Color.BLACK, true);
				Log.generarEstadistica();
				
				Config.getVista().enabledConfig(true);
				Config.getVista().enabledEjecutarC(false);
			}
		}
		catch (MemoryException | CpuException e)
		{
			Vista v = Config.getVista();
			if (v == null)
				System.err.println(e);
			else
				JOptionPane.showMessageDialog( v, e, "Se ha producido una excepción", JOptionPane.ERROR_MESSAGE );
		}
	}
	
	
	// Aquí el ejecutar traza
	public void ejecutarTraza() throws MemoryException
	{
		//ejecutar(archivo_traza);
		traza.readLines(t);
		
		Log.println(1, "Fin de programa\n\n", Color.BLACK, true);
		Log.generarEstadistica();
	}
	
	public void setTraza(String s)
	{
		t=s;
	}

	public void iniciarTraza()
	{
		try
		{
			tp_alojada = false;
			
			inicializarMemoria();
			inicializarInterfaz();
			inicializarCpu();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		traza=new Traza(jmem,jmem2);
	}
	
	// Leer configuración.
	private void leerConfig()
	{
		Log.setNivel(Config.get(Conf_Type.NIVEL_LOG));
		
		bytes_palabra = Config.get(Conf_Type.TAMAÑO_PALABRA);
		palabras_linea = Config.get(Conf_Type.TAMAÑO_LINEA);
		
		nivelJerarquiasSeparadas = Config.get(Conf_Type.NIVEL_JERARQUIAS_SEPARADAS);
		
		entradas_pagina = Config.get(Conf_Type.ENTRADAS_PAGINA);
		max_ent_mem = Config.get(Conf_Type.NUMERO_ENTRADAS_MEMORIA);
		max_entrada = Config.get(Conf_Type.MAXIMA_ENTRADA_MEMORIA);
		
		archivo_cpu = Config.get(Conf_Type_c.ARCHIVO_CODIGO);
		archivo_traza = Config.get(Conf_Type_c.ARCHIVO_TRAZA);
		archivo_memoria = Config.get(Conf_Type_c.ARCHIVO_MEMORIA);
		
		tp_alojada = Config.get(Conf_Type.TABLA_PAGINAS_ALOJADA) == 1 ? true : false;
		politica_tp = TiposReemplazo.valueOf(Config.get(Conf_Type_c.TP_POLITICA));
		
		// Niveles de caché
		niveles_cache1 = Config.get(Conf_Type.NIVELES_CACHE_DATOS);
		niveles_cache2 =  Config.get(Conf_Type.NIVELES_CACHE_INSTRUCCIONES);
		
		entradas_caches1 = new int[]{Config.get(Conf_Type.CACHE1_DATOS_ENTRADAS),Config.get(Conf_Type.CACHE2_DATOS_ENTRADAS),Config.get(Conf_Type.CACHE3_DATOS_ENTRADAS)};
		vias_caches1 = new int[]{Config.get(Conf_Type.CACHE1_DATOS_VIAS),Config.get(Conf_Type.CACHE2_DATOS_VIAS),Config.get(Conf_Type.CACHE3_DATOS_VIAS)};	
		entradas_caches2 = new int[]{Config.get(Conf_Type.CACHE1_INSTRUCCIONES_ENTRADAS),Config.get(Conf_Type.CACHE2_INSTRUCCIONES_ENTRADAS),Config.get(Conf_Type.CACHE3_INSTRUCCIONES_ENTRADAS)};
		vias_caches2 = new int[]{Config.get(Conf_Type.CACHE1_INSTRUCCIONES_VIAS),Config.get(Conf_Type.CACHE2_INSTRUCCIONES_VIAS),Config.get(Conf_Type.CACHE3_INSTRUCCIONES_VIAS)};


		politicas_caches1 = new TiposReemplazo[niveles_cache1];
		if (niveles_cache1 > 0 && Config.get(Conf_Type_c.CACHE1_DATOS_POLITICA) != null)
			politicas_caches1[0] = TiposReemplazo.valueOf(Config.get(Conf_Type_c.CACHE1_DATOS_POLITICA));
		if (niveles_cache1 > 1 && Config.get(Conf_Type_c.CACHE2_DATOS_POLITICA) != null)
			politicas_caches1[1] = TiposReemplazo.valueOf(Config.get(Conf_Type_c.CACHE2_DATOS_POLITICA));
		if (niveles_cache1 > 2 && Config.get(Conf_Type_c.CACHE3_DATOS_POLITICA) != null)
			politicas_caches1[2] = TiposReemplazo.valueOf(Config.get(Conf_Type_c.CACHE3_DATOS_POLITICA));
		
		if (nivelJerarquiasSeparadas > 1)
		{
			politicas_caches2 = new TiposReemplazo[niveles_cache2];
			if (niveles_cache2 > 0 && Config.get(Conf_Type_c.CACHE1_INSTRUCCIONES_POLITICA) != null)
				politicas_caches2[0] = TiposReemplazo.valueOf(Config.get(Conf_Type_c.CACHE1_INSTRUCCIONES_POLITICA));
			if (niveles_cache2 > 1 && Config.get(Conf_Type_c.CACHE2_INSTRUCCIONES_POLITICA) != null)
				politicas_caches2[1] = TiposReemplazo.valueOf(Config.get(Conf_Type_c.CACHE2_INSTRUCCIONES_POLITICA));
			if (niveles_cache2 > 2 && Config.get(Conf_Type_c.CACHE3_INSTRUCCIONES_POLITICA) != null)
				politicas_caches2[2] = TiposReemplazo.valueOf(Config.get(Conf_Type_c.CACHE3_INSTRUCCIONES_POLITICA));
		}

		tlb_datos = Config.get(Conf_Type.TLB_DATOS) == 1 ? true : false;
		tlb_inst = Config.get(Conf_Type.TLB_INSTRUCCIONES) == 1 ? true : false;
		
		tlb1_entradas = Config.get(Conf_Type.TLB_DATOS_ENTRADAS);
		tlb1_vias = Config.get(Conf_Type.TLB_DATOS_VIAS);
		
		if (tlb_datos)
			tlb1_politica = TiposReemplazo.valueOf(Config.get(Conf_Type_c.TLB_DATOS_POLITICA));
		
		tlb2_entradas = Config.get(Conf_Type.TLB_INSTRUCCIONES_ENTRADAS);
		tlb2_vias = Config.get(Conf_Type.TLB_INSTRUCCIONES_VIAS);
		
		if (tlb_inst)
			tlb2_politica = TiposReemplazo.valueOf(Config.get(Conf_Type_c.TLB_INSTRUCCIONES_POLITICA));
	}
	
	// Inicializa la Cpu.
	private void inicializarCpu() throws CpuException
	{
		// Calculo la dirección de memoria para instrucciones.
		int num_instrucciones = Decoder.getInstrucciones().size();
		int paginas_instrucciones = (int) Math.ceil(((float)num_instrucciones / (float)entradas_pagina));
		int num_paginas = max_entrada / entradas_pagina;
		int paginas_tablaPags = (int) Math.ceil(((float)num_paginas / (float)entradas_pagina));
		
		if (tablaPags.getNumeroPaginas() == 1)
		{
			int mitad = entradas_pagina / 2;
			if (mitad < num_instrucciones)
				throw new CpuException("No hay memoria suficiente para este código.");
			
			direccion_inst = mitad;
		}
		else
		{
			int primera_pag_tabla = tablaPags.getNumeroPaginas();
			
			if (tp_alojada)
			{
				primera_pag_tabla = tablaPags.getNumeroPaginas() - paginas_tablaPags;
				direccion_tablPags = primera_pag_tabla * tablaPags.getEntradasPagina() * 4;
			}
			
			int primera_pag_inst = primera_pag_tabla - paginas_instrucciones;
			direccion_inst = primera_pag_inst * tablaPags.getEntradasPagina() * 4;
			
			Log.printDebug("Dirección Tabla Páginas: " + direccion_tablPags);
			Log.printDebug("Dirección Primera Instrucción: " + direccion_inst);
		}
		
		cpu = new CpuMonociclo(jmem, jmem2, direccion_inst);
	}
	
	// Inicializa la Jerarquía de Memoria.
	private void inicializarMemoria() throws MemoryException, CpuException
	{
		// Inicializo la jerarquía de datos.
		caches1 = new Cache[niveles_cache1];
		for (int i = 0; i < niveles_cache1; i++)
		{
			if (vias_caches1[i] > 1)
				caches1[i] = new CacheAsociativa(entradas_caches1[i], palabras_linea, vias_caches1[i], politicas_caches1[i], bytes_palabra);
			else
				caches1[i] = new CacheDirecta(entradas_caches1[i], palabras_linea, bytes_palabra);
		}

		// Si hay alguna separación inicializo las de instrucciones.
		if (nivelJerarquiasSeparadas != 1)
		{
			caches2 = new Cache[niveles_cache2];
			
			if (nivelJerarquiasSeparadas < 4)
			{
				if (nivelJerarquiasSeparadas == 2)
				{
					// Si a partir de la 2 son compartidas, creo la primera.
					if (vias_caches2[0] > 1)
						caches2[0] = new CacheAsociativa(entradas_caches2[0], palabras_linea, vias_caches2[0], politicas_caches2[0], bytes_palabra);
					else
						caches2[0] = new CacheDirecta(entradas_caches2[0], palabras_linea, bytes_palabra);
					
					// Las otras 2 son las mismas que las de datos:
					if (niveles_cache2 > 1)
						caches2[1] = caches1[1];
					if (niveles_cache2 > 2)
					caches2[2] = caches1[2];
				}
				else
				{
					// Sólo la 3 es compartida, creo la 1 y la 2
					if (vias_caches2[0] > 1)
						caches2[0] = new CacheAsociativa(entradas_caches2[0], palabras_linea, vias_caches2[0], politicas_caches2[0], bytes_palabra);
					else
						caches2[0] = new CacheDirecta(entradas_caches2[0], palabras_linea, bytes_palabra);
					
					if (niveles_cache2 > 1)
					{
						if (vias_caches2[1] > 1)
							caches2[1] = new CacheAsociativa(entradas_caches2[1], palabras_linea, vias_caches2[1], politicas_caches2[1], bytes_palabra);
						else
							caches2[1] = new CacheDirecta(entradas_caches2[1], palabras_linea, bytes_palabra);
					}
					
					// La última es la misma que la de datos.
					if (niveles_cache2 > 2)
						caches2[2] = caches1[2];
				}
			}
			else
			{
				// Todas separadas.
				for (int i = 0; i < niveles_cache2; i++)
				{
					if (vias_caches2[i] > 1)
						caches2[i] = new CacheAsociativa(entradas_caches2[i], palabras_linea, vias_caches2[i], politicas_caches2[i], bytes_palabra);
					else
						caches2[i] = new CacheDirecta(entradas_caches2[i], palabras_linea, bytes_palabra);
				}
			}
		}

		if (tlb_datos)
		{
			if (tlb1_vias > 1)
				tlb1 = new Tlb(tlb1_entradas, tlb1_vias, tlb1_politica);
			else
				tlb1 = new Tlb(tlb1_entradas);
		}
		if (tlb_inst)
		{
			if (tlb2_vias > 1)
				tlb2 = new Tlb(tlb2_entradas, tlb2_vias, tlb2_politica);
			else
				tlb2 = new Tlb(tlb2_entradas);
		}

		// Tabla de Páginas
		tablaPags = new TablaPaginas(entradas_pagina, palabras_linea, max_entrada, max_ent_mem, politica_tp, tlb1, tlb2, tp_alojada);
		
		// Memoria principal.
		memoria = new MemoriaPrincipal(tablaPags);
		
		// Inicializar la Jerarquía de Memoria.
		jmem = new JerarquiaMemoria(tablaPags, caches1, memoria, false);
		if (nivelJerarquiasSeparadas > 1)
			jmem2 = new JerarquiaMemoria(tablaPags, caches2, memoria, true);
		
		tablaPags.setJerarquiaMemoria(jmem, jmem2);
	}
	
	// Inicializa la interfaz gráfica.
	private void inicializarInterfaz() throws Exception
	{
		//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		if (max_ent_mem > 16384)
			Config.getVista().mostrarBotonMemoria(false);
		else
		{
			Config.getVista().mostrarBotonMemoria(true);
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
			frameMemoria.setVisible(false);
			memoria.setInterfaz(tablaMemoria);
		}
		
		
		tablasCache1 = new Tabla[niveles_cache1];
		framesCache1 = new JFrame[niveles_cache1];

		for (int i = 0; i < niveles_cache1; i++)
		{
			tablasCache1[i] = new Tabla(caches1[i]);
			if (vias_caches1[i] > 1)
				tablasCache1[i].setRenderTablaEnCelda();
			framesCache1[i] = new VentanaLimitada();
			JScrollPane jscroll = new JScrollPane(tablasCache1[i], JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
			framesCache1[i].setTitle("Cache L"+(i+1));
			framesCache1[i].setPreferredSize(new Dimension(600, 200));
			framesCache1[i].setMinimumSize(new Dimension(500, 200));
			framesCache1[i].setMaximumSize(new Dimension(2000, 2000));
			framesCache1[i].add( jscroll );
			framesCache1[i].pack();
			framesCache1[i].addWindowListener(new VentanaOculta(framesCache1[i]));
			framesCache1[i].setVisible(false);
			caches1[i].setInterfaz(tablasCache1[i]);
		}
		if (nivelJerarquiasSeparadas > 1)
		{
			int limite2 = niveles_cache2;
			
			// Si hay jerarquías compartidas, la caché de instrucciones tendrá menos niveles de los que nos envía la Config.
			if (nivelJerarquiasSeparadas < 4)
			{
				if (nivelJerarquiasSeparadas == 1)
					limite2 = 0;
				else if (nivelJerarquiasSeparadas == 2)
					limite2 = 1;
				else if (nivelJerarquiasSeparadas == 3)
					limite2 = Math.min(niveles_cache1, 2);
			}
			
			tablasCache2 = new Tabla[limite2];
			framesCache2 = new JFrame[limite2];
			
			for (int i = 0; i < limite2; i++)
			{
				tablasCache2[i] = new Tabla(caches2[i]);
				if (vias_caches2[i] > 1)
					tablasCache2[i].setRenderTablaEnCelda();
				framesCache2[i] = new VentanaLimitada();
				JScrollPane jscroll = new JScrollPane(tablasCache2[i], JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
				framesCache2[i].setTitle("Cache Instrucciones L"+(i+1));
				framesCache2[i].setPreferredSize(new Dimension(600, 200));
				framesCache2[i].setMinimumSize(new Dimension(500, 200));
				framesCache2[i].setMaximumSize(new Dimension(2000, 2000));
				framesCache2[i].add( jscroll );
				framesCache2[i].pack();
				framesCache2[i].addWindowListener(new VentanaOculta(framesCache2[i]));
				framesCache2[i].setVisible(false);
				caches2[i].setInterfaz(tablasCache2[i]);
			}
		}
	}

	@Override
	public void run()
	{
		switch(funcion)
		{
			case VALIDAR_CODIGO:
				validarCodigo();
				break;
			case EJECUTAR_CODIGO:
				ejecutarCodigo();
				
				Config.getVista().enabledConfig(true);
				Config.getVista().enabledEjecutarC(false);
				break;
			case VALIDAR_TRAZA:
				
				break;
			case EJECUTAR_TRAZA:
				
				break;
		}
	}

	public Funcion getFuncion()
	{
		return funcion;
	}

	public void setFuncion(Funcion funcion)
	{
		this.funcion = funcion;
	}
	
	public void detener()
	{
		detenido = true;
		cpu.detener();
	}
}
