package memoria;

import general.Global;
import general.Global.TiposReemplazo;
import general.Log;
import general.Log.Flags;
import general.MemoryException;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class TablaPaginas {
	
	// Esta clase funciona únicamente con direcciones REALES.
	private SortedMap<Integer, Pagina> paginas;
	private Pagina[] marcos;
	private Pagina[] paginasTP;
	
	private int entradas_pagina;
	private int tam_pagina;
	private int entrada_maxima;
	private int num_paginas;
	private int num_marcos;
	private int palabras_linea;
	private int bytes_palabra;
	
	private JerarquiaMemoria jerarquia1;
	private JerarquiaMemoria jerarquia2;
	private Tlb tlb_datos;
	private Tlb tlb_inst;
	
	public PoliticaReemplazo politica;
	
	private int registroTablaPaginas;
	private boolean tablaPagsEnMemoria;
	
	// Esta clase contiene todas las páginas.
	// Cada página contendrá su porción de memoria y si está en ella o no.
	// Cada página se direcciona según dirección VIRTUAL.
	// Cada página tiene un identificador único por el que se identifica (nombre).
	// Si la página está en memoria principal debe estar contenida en un MARCO.
	// La TLB contiene los bits más significativos de la dirección virtual y el MARCO.
	
	/*
	 * Acceso:
	 * 
	 * 1 - Se descompone la dirección virtual. Si tenemos 2^32 direcciones posibles la dirección será:
	 *     20 bits para seleccionar página y 12 para seleccionar palabra dentro de la página.
	 * 2 - Se busca en TLB (si está pasamos al punto 6).
	 * 3 - Buscamos en la TablaPaginas (esta clase) si la página está en memoria principal 
	 *     (es decir, tiene MARCO asignado). Si esto ocurre pasamos al punto 5.
	 * 4 - Page Fault. Asignar un marco a la página.
	 * 5 - Actualizar TLB.
	 * 6 - Construir la dirección REAL usando la TLB y el offset de la dirección virtual.
	 */
	
	// Este constructor utiliza NUMERO DE ENTRADAS de cada tipo.
	public TablaPaginas(int ent_pag, int pal_linea, int max_ent_mem, int ent_mem_princ, TiposReemplazo _Tpolitica, Tlb tlb1, Tlb tlb2, boolean tp_alojada)
	{
		this (ent_pag, pal_linea, max_ent_mem, ent_mem_princ, 4, _Tpolitica, tlb1, tlb2, tp_alojada);
	}
	
	public TablaPaginas(int ent_pag, int pal_linea, int max_ent_mem, int ent_mem_princ, int _bytes_palabra, TiposReemplazo _Tpolitica, Tlb tlb1, Tlb tlb2, boolean tp_alojada)
	{
		// Max_memoria por defecto: 0xFFFFFFFF -> Max ent: 0xFFFFFFFF / 4
		// Memoria principal por defecto: 2048 (64KB).
		// Una página de 4KB tendrá tamaño 4096 / 4 = 1024.
		entrada_maxima = max_ent_mem;
		num_paginas = entrada_maxima / ent_pag;
		num_marcos = ent_mem_princ / ent_pag;
		marcos = new Pagina[num_marcos];
		paginas = new TreeMap<Integer, Pagina>();
		entradas_pagina = ent_pag;
		palabras_linea = pal_linea;
		tam_pagina = entradas_pagina * _bytes_palabra;
		bytes_palabra = _bytes_palabra;
		tablaPagsEnMemoria = tp_alojada;
		
		tlb_datos = tlb1;
		tlb_inst = tlb2;
		
		Log.printDebug("Información Debug");
		Log.printSeparador(3);
		
		Log.printDebug("Máxima entrada: " + entrada_maxima);
		Log.printDebug("Tamaño página: " + ent_pag);
		Log.printDebug("Número páginas: " + num_paginas);
		Log.printDebug("Número marcos: " + num_marcos);
		
		Log.printDebug("Máxima dirección virtual: " + ((entrada_maxima << 2)-1));
		Log.printDebug("Máxima dirección física: " + (((num_paginas) << Global.bitsDireccionar(tam_pagina))-1));
		Log.printDebug("Máxima dirección memoria: " + (((num_marcos) << Global.bitsDireccionar(tam_pagina))-1));
		
		Log.printSeparador(3);
		
		// La política sólo tendrá 1 entrada, con el número de marcos que hay.
		politica = new PoliticaReemplazo(_Tpolitica, 1, ultimoMarco());
	}
	
	public void generarPaginasTablas(int direccion) throws MemoryException
	{
		int numPags = (int) Math.ceil(num_paginas / entradas_pagina);
		paginasTP = new Pagina[numPags];
		
		int direccion_inicio = direccion;
		
		for (int i = 0; i < numPags; i++)
		{
			Pagina nueva = seleccionarPagina(direccion_inicio);
			paginasTP[i] = nueva;
			direccion_inicio += entradas_pagina * bytes_palabra;
		}
		
		int primer_marco = num_marcos - numPags;
		registroTablaPaginas = primer_marco * entradas_pagina * 4;
		
		for (int i = 0; i < numPags; i++)
			asignarPaginaMarco(paginasTP[i], primer_marco+i);
		
		actualizarPaginasTabla();
	}
	
	private void actualizarPaginasTabla()
	{
		for (int i = 0; i < num_paginas; i++)
		{
			int pagina = (int) Math.ceil(i / entradas_pagina);
			int dato = 0;
			if (paginas.get(i) != null && paginas.get(i).getMarco() != -1)
			{
				dato = paginas.get(i).getMarco();
				dato = dato | 0x40000000;
				if (paginas.get(i).esDirty())
					dato = dato | 0x20000000;
			}
			paginasTP[pagina].guardarLinea((i % entradas_pagina) * bytes_palabra, new int[]{dato});
		}
		
		for (int i = 0; i < paginasTP.length; i++)
			jerarquia1.actualizarMarcoInterfazMemoria(paginasTP[i].getMarco());
	}
	
	private void actualizarPaginasTabla(Pagina pag)
	{
		if (!tablaPagsEnMemoria)
			return;
		
		int pagId = pag.getId();
		int pagina = (int) Math.ceil(pagId / entradas_pagina);
		int dato = 0;
		if (pag != null && pag.getMarco() != -1)
		{
			dato = pag.getMarco();
			dato = dato | 0x40000000;
			if (pag.esDirty())
				dato = dato | 0x20000000;
		}
		
		int direcR = registroTablaPaginas + (pagId * bytes_palabra);
		int direc = (pagId % entradas_pagina) * bytes_palabra;
		Log.printDebug(String.format("Se actualiza la Tabla de Páginas -> Página: %d (0x%s) Marco: %d (%d).",pagId,Integer.toHexString(direcR),pag.getMarco(),dato));
		
		paginasTP[pagina].guardarLinea(direc, new int[]{dato});
		
		for (int i = 0; i < paginasTP.length; i++)
			jerarquia1.actualizarMarcoInterfazMemoria(paginasTP[i].getMarco());
	}
	
	// Devuelve el último marco usable por datos (elimina los marcos usados por la Tabla de Páginas.
	private int ultimoMarco()
	{
		if (paginasTP != null)
			return marcos.length - paginasTP.length;
		else
			return marcos.length;
	}
	
	// Crea una página nueva.
	private Pagina crearPagina(int direccion) throws MemoryException
	{
		int id = calcularId(direccion);
		Log.printDebug("Creando página " + id);
		Pagina nueva = new Pagina(entradas_pagina, palabras_linea, id);
		paginas.put(id, nueva);
		return nueva;
	}
	
	// Calcular id de página. La ID se calcula con direcciones VIRTUALES.
	public int calcularId(int direccion)
	{
		return (int) Math.floor(direccion / tam_pagina);
	}
	
	// Traduce una dirección virtual a una física.
	// Si no le corresponde una dirección física deberá poner la página en un marco.
	public Direccion traducirDireccion(int direccion, boolean secundaria) throws MemoryException
	{
		Log.printDebug("Se traduce la dirección " + direccion);
		
		if ((direccion >> Global.bitsDireccionar(bytes_palabra)) >= entrada_maxima)
			throw new MemoryException("La dirección 0x" + Integer.toHexString(direccion) + " sobrepasa el límite de direccionamiento.");
		
		// Primero buscamos en qué página debe estar la dirección.
		Pagina pag = seleccionarPagina(direccion);
		if (pag.getMarco() == -1)
			Log.printDebug("La página " + pag.getId() + " no tiene marco asignado.");
		else
			Log.printDebug("La página " + pag.getId() + " tiene asignada el marco " + pag.getMarco());
		
		// Ya tenemos la página correspondiente, comprobamos si está en un marco.
		if (pag.getMarco() != -1)
		{
			boolean tlb_hit = false;
			
			// Comprobamos la tabla de páginas.
			if (!tlb_hit)
			{
				Log.report(Flags.ACCESS_PT);
				
				if (tablaPagsEnMemoria)
				{
					int dirPag = registroTablaPaginas + pag.getId()*4;
					Log.printDebug("Puntero Tabla Páginas: " + registroTablaPaginas);
					Log.printDebug("Desplazamiento: " + pag.getId()*4);
					Log.println(2,"TP: Se accede a la dirección 0x" + Integer.toHexString(dirPag) + " para buscar el marco de la página.", Color.BLUE);
				}
				else
				{
					Log.println(2,"TP: Se accede a la tabla de páginas para buscar el marco de la página.", Color.BLUE);
				}
			}
			
			// Comprobamos si está en la TLB.
			if (!secundaria)
			{
				if (tlb_datos != null)
				{
					if (tlb_datos.existePagina(pag.getId()))
					{
						Log.report(Flags.TLB_HIT);
						Log.println(1,"TLB HIT", Color.GREEN, false);
						tlb_hit = true;
					}
					else
					{
						// MISS, la guardamos en TLB
						if (!tlb_datos.hayHueco(pag.getId()))
							Log.report(Flags.CONFLICT_TLB);
						tlb_datos.insertar(pag.getId(), pag.getMarco());
						Log.report(Flags.TLB_MISS);
						Log.println(1,"TLB MISS", Color.RED, false);
					}
				}
			}
			else
			{
				if (tlb_inst != null)
				{
					if (tlb_inst.existePagina(pag.getId()))
					{
						Log.report(Flags.TLB_HIT_F);
						Log.println(1,"ITLB HIT", Color.GREEN, false);
						tlb_hit = true;
					}
					else
					{
						// MISS, la guardamos en TLB
						if (!tlb_inst.hayHueco(pag.getId()))
							Log.report(Flags.CONFLICT_TLB_F);
						tlb_inst.insertar(pag.getId(), pag.getMarco());
						Log.report(Flags.TLB_MISS_F);
						Log.println(1,"ITLB MISS", Color.RED, false);
					}
				}
			}
			
			// Si estamos aquí es porque se ha encontrado la página asociada a un marco.
			// OJO! Puede ser PAGE HIT y TLB MISS.
			Log.report(Flags.PAGE_HIT);
			Log.println(1,"PAGE HIT", Color.GREEN);
			
			// La página está en un marco, podemos traducir la dirección.
			Log.println(2,"TP: Se ha encontrado la página en el marco " + pag.getMarco());
			Direccion dirR = calcularDireccion(direccion, pag.getMarco());
			Log.println(2,"TP: Se traduce la dirección como 0x" + dirR.getRealHex());
			return dirR;
		}
		else
		{
			// MISS, la guardamos en TLB
			if (!secundaria)
			{
				if (tlb_datos != null)
				{
					if (!tlb_datos.hayHueco(pag.getId()))
						Log.report(Flags.CONFLICT_TLB);
					tlb_datos.insertar(pag.getId(), pag.getMarco());
					Log.report(Flags.TLB_MISS);
					Log.println(1,"TLB MISS", Color.RED, false);
				}
			}
			else
			{
				if (tlb_inst != null)
				{
					if (!tlb_inst.hayHueco(pag.getId()))
						Log.report(Flags.CONFLICT_TLB_F);
					tlb_inst.insertar(pag.getId(), pag.getMarco());
					Log.report(Flags.TLB_MISS_F);
					Log.println(1,"TLB MISS", Color.RED, false);
				}
			}
			
			Log.report(Flags.ACCESS_PT);
			
			// Comprobamos la tabla de páginas.
			if (tablaPagsEnMemoria)
			{
				int dirPag = registroTablaPaginas + pag.getId()*4;
				Log.printDebug("Puntero Tabla Páginas: " + registroTablaPaginas);
				Log.printDebug("Desplazamiento: " + pag.getId()*4);
				Log.println(2,"TP: Se accede a la dirección 0x" + Integer.toHexString(dirPag) + " para buscar el marco de la página.", Color.BLUE);
			}
			else
			{
				Log.println(2,"TP: Se accede a la tabla de páginas para buscar el marco de la página.", Color.BLUE);
			}
			
			// Es PAGE FAULT
			Log.report(Flags.PAGE_FAULT);
			Log.println(1,"PAGE FAULT", Color.MAGENTA);
			// Tenemos que guardar la página en un marco.
			int marco = buscarMarcoLibre();
			if (marco != -1)
			{
				asignarPaginaMarco(pag, marco);
				actualizarPaginasTabla(pag);
				Log.println(2,"SO: Se carga la página desde memoria secundaria al marco " + marco);
				Direccion dirR = calcularDireccion(direccion, marco);
				Log.println(2,"TP: Se traduce la dirección como 0x" + dirR.getRealHex());
				return dirR;
			}
			else
			{
				marco = liberarMarco();
				asignarPaginaMarco(pag, marco);
				actualizarPaginasTabla(pag);
				Log.println(2,"SO: Se carga la página desde memoria secundaria al marco " + marco + ", reemplazando la página anterior.");
				Direccion dirR = calcularDireccion(direccion, marco);
				Log.println(2,"TP: Se traduce la dirección como 0x" + dirR.getRealHex());
				return dirR;
			}
		}
	}
	
	// Traducción.
	private Direccion calcularDireccion(int direccion, int marco)
	{
		int offset = (int) Math.floor(direccion % tam_pagina);
		int res = (marco << Global.bitsDireccionar(tam_pagina)) + offset;
		int id_pagina = calcularId(direccion);
		//Log.println(3, "Dirección 0x" + Integer.toHexString(direccion) + " traducida como 0x" + Integer.toHexString(res) + " [" + marco + "] " + offset);
		return new Direccion(direccion, res, id_pagina);
	}
	
	// Selecciona la página correspondiente a la dirección VIRTUAL.
	public Pagina seleccionarPagina(int direccion) throws MemoryException
	{
		// Buscamos la página.
		int id = calcularId(direccion);
		Pagina pag = paginas.get(id);
		
		// Si la página no existe, la creamos.
		if (pag == null)
			pag = crearPagina(direccion);
		
		return pag;
	}
	
	// Busca una página por ID (si no existe no la crea).
	public Pagina buscarPagina(int id) throws MemoryException
	{
		Pagina pag = paginas.get(id);
		return pag;
	}
	
	// Busca un marco libre donde insertar la página.
	private int buscarMarcoLibre()
	{
		// Creamos una lista con todos los marcos libres.
		List<Integer> lista = new ArrayList<Integer>();
		for (int i = 0; i < ultimoMarco(); i++)
		{
			if (marcos[i] == null)
				lista.add(i);
		}
		
		// Seleccionamos una posición aleatoriamente.
		if (lista.size() > 0)
		{
			Random r = new Random();
			return lista.get(r.nextInt(lista.size()));
		}
		else
			return -1;
	}
	
	private void asignarPaginaMarco(Pagina pag, int marco) throws MemoryException
	{
		if (marcos[marco] != null && marcos[marco].getMarco() != -1)
			throw new MemoryException("Página asignada a marco en asignación.");
		
		if (marcos[marco] != null)
			throw new MemoryException("Marco ocupado al asignar página.");
		
		marcos[marco] = pag;
		pag.asignarMarco(marco);
		politica.nuevaLinea(0, marco);
		
		// Actualizar la interfaz de memoria para este marco.
		jerarquia1.actualizarMarcoInterfazMemoria(marco);
	}
	
	// Libera un marco (según política de reemplazo).
	private int liberarMarco() throws MemoryException
	{
		int marco_libre = politica.elegirViaReemplazo(0);
		
		int id = marcos[marco_libre].getId();
		
		Log.report(Flags.CONFLICT_PAGE);
		if (marcos[marco_libre].esDirty())
		{
			marcos[marco_libre].escribirDisco();
			Log.println(2, "Se escribe la página " + id + " en disco.");
		}
		
		// Eliminamos todas las referencias a la página anterior en caché.
		jerarquia1.invalidarPagina(id);
		if (jerarquia2 != null)
			jerarquia2.invalidarPagina(id);
		
		// Eliminar referencias de la página anterior.
		marcos[marco_libre].asignarMarco(-1);
		marcos[marco_libre] = null;
		
		return marco_libre;
	}
	
	// Actualizar la política de reemplazo cuando se produce un acceso
	public void accesoMarco(int marco)
	{
		politica.accesoLinea(0, marco);
	}
	
	// Selecciona el marco a partir de la dirección REAL
	public int seleccionarMarco(int direccion)
	{
		int marco = direccion >> Global.bitsDireccionar(tam_pagina);
		return marco;
	}
	
	// Devuelve la lista de marcos.
	public Pagina[] getMarcos() { return marcos; }
	
	// Devuelve el tamaño de página.
	public int getTamañoPagina() { return tam_pagina; }
	
	// Devuelve el tamaño de página.
	public int getEntradasPagina() { return entradas_pagina; }
	
	// Devuelve el número de páginas.
	public int getNumeroPaginas() { return num_paginas; }
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder("Páginas en marco: \n");
		for (int i = 0; i < marcos.length; i++)
			strB.append("Marco [" + i + "]" + "\n" + marcos[i] + "\n");
		
		strB.append("\n");
		strB.append("Número de páginas en total: " + paginas.size());
		
		return strB.toString();
	}

	public void setJerarquiaMemoria(JerarquiaMemoria jmem1, JerarquiaMemoria jmem2)
	{
		jerarquia1 = jmem1;
		jerarquia2 = jmem2;
	}
	
	// Este método inicializar en memoria virtual el contenido de la RAM.
	// Se crea la página y se guarda el dato, pero sin asignarla a marco.
	// La dirección recibida es VIRTUAL.
	public void inicializarDatoMemoriaVirtual(int direccion_virtual, int dato) throws MemoryException
	{
		// Selecciono o creo la página si es necesario.
		Pagina pag = seleccionarPagina(direccion_virtual);
		if (pag.estaLibre(direccion_virtual))
			pag.guardarLinea(direccion_virtual, new int[]{dato});
		else
			throw new MemoryException("Se sobreescribe un dato en la dirección 0x" + Integer.toHexString(direccion_virtual));
	}
}
