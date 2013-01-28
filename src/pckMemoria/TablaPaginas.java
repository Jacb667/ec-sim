package pckMemoria;

import general.Global;
import general.Global.TiposReemplazo;
import general.Log;
import general.Log.Flags;
import general.MemoryException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

public class TablaPaginas {
	
	// Esta clase funciona únicamente con direcciones REALES.
	private SortedMap<Integer, Pagina> paginas;
	private Pagina[] marcos;
	
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
	public TablaPaginas(int ent_pag, int pal_linea, int max_ent_mem, int ent_mem_princ, TiposReemplazo _Tpolitica, Tlb tlb1, Tlb tlb2)
	{
		this (ent_pag, pal_linea, max_ent_mem, ent_mem_princ, 4, _Tpolitica, tlb1, tlb2);
	}
	
	public TablaPaginas(int ent_pag, int pal_linea, int max_ent_mem, int ent_mem_princ, int _bytes_palabra, TiposReemplazo _Tpolitica, Tlb tlb1, Tlb tlb2)
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
		
		tlb_datos = tlb1;
		tlb_inst = tlb2;
		
		System.out.println("entrada_maxima: " + entrada_maxima);
		System.out.println("tam_pagina: " + ent_pag);
		System.out.println("num_paginas: " + num_paginas);
		System.out.println("num_marcos: " + num_marcos);
		
		System.out.println("Máxima dirección virtual: " + ((entrada_maxima << 2)-1));
		System.out.println("Máxima dirección física: " + (((num_paginas) << Global.bitsDireccionar(tam_pagina))-1));
		System.out.println("Máxima dirección memoria: " + (((num_marcos) << Global.bitsDireccionar(tam_pagina))-1));
		
		// La política sólo tendrá 1 entrada, con el número de marcos que hay.
		politica = new PoliticaReemplazo(_Tpolitica, 1, marcos.length);
	}
	
	// Crea una página nueva.
	private Pagina crearPagina(int direccion) throws MemoryException
	{
		int id = calcularId(direccion);
		Log.println(3, "Creando página " + id);
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
		if ((direccion >> Global.bitsDireccionar(bytes_palabra)) >= entrada_maxima)
			throw new MemoryException("La dirección 0x" + Integer.toHexString(direccion) + " sobrepasa el límite de direccionamiento.");
		
		// Primero buscamos en qué página debe estar la dirección.
		Pagina pag = seleccionarPagina(direccion);
		
		// Ya tenemos la página correspondiente, comprobamos si está en un marco.
		if (pag.getMarco() != -1)
		{
			// Comprobamos si está en la TLB (en realidad no se utiliza para nada, sólo para ver si es HIT o MISS)
			if (tlb_datos != null)
			{
				if (tlb_datos.existePagina(pag.getId()))
				{
					Log.report(Flags.TLB_HIT, secundaria);
					Log.println(2,"TLB HIT");
				}
				else
				{
					// MISS, la guardamos en TLB
					tlb_datos.insertar(pag.getId(), pag.getMarco());
					Log.report(Flags.TLB_MISS, secundaria);
					Log.println(2,"TLB MISS");
				}
			}
			
			// Si estamos aquí es porque se ha encontrado la página asociada a un marco.
			// OJO! Puede ser PAGE HIT y TLB MISS.
			Log.report(Flags.PAGE_HIT, secundaria);
			Log.println(2,"PAGE HIT");
			
			// La página está en un marco, podemos traducir la dirección.
			//Log.println(3, "La página ya está en el marco " + pag.getMarco());
			return calcularDireccion(direccion, pag.getMarco());
		}
		else
		{
			// MISS, la guardamos en TLB
			if (tlb_datos != null)
			{
				tlb_datos.insertar(pag.getId(), pag.getMarco());
				Log.report(Flags.TLB_MISS, secundaria);
				Log.println(2,"TLB MISS");
			}
			// Es PAGE FAULT
			Log.report(Flags.PAGE_FAULT, secundaria);
			Log.println(2,"PAGE FAULT");
			// Tenemos que guardar la página en un marco.
			int marco = buscarMarcoLibre();
			if (marco != -1)
			{
				asignarPaginaMarco(pag, marco);
				Log.println(3, "Página asignada al marco " + marco);
				return calcularDireccion(direccion, marco);
			}
			else
			{
				marco = liberarMarco();
				asignarPaginaMarco(pag, marco);
				Log.println(3, "Se ha reemplazado la página del marco " + marco);
				return calcularDireccion(direccion, marco);
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
		for (int i = 0; i < marcos.length; i++)
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
		if (jerarquia2 != null)
			jerarquia2.actualizarMarcoInterfazMemoria(marco);
	}
	
	// Libera un marco (según política de reemplazo).
	private int liberarMarco() throws MemoryException
	{
		int marco_libre = politica.elegirViaReemplazo(0);
		
		int id = marcos[marco_libre].getId();
		
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
