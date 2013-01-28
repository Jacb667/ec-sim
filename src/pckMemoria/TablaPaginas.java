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
	
	// Esta clase funciona �nicamente con direcciones REALES.
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
	
	// Esta clase contiene todas las p�ginas.
	// Cada p�gina contendr� su porci�n de memoria y si est� en ella o no.
	// Cada p�gina se direcciona seg�n direcci�n VIRTUAL.
	// Cada p�gina tiene un identificador �nico por el que se identifica (nombre).
	// Si la p�gina est� en memoria principal debe estar contenida en un MARCO.
	// La TLB contiene los bits m�s significativos de la direcci�n virtual y el MARCO.
	
	/*
	 * Acceso:
	 * 
	 * 1 - Se descompone la direcci�n virtual. Si tenemos 2^32 direcciones posibles la direcci�n ser�:
	 *     20 bits para seleccionar p�gina y 12 para seleccionar palabra dentro de la p�gina.
	 * 2 - Se busca en TLB (si est� pasamos al punto 6).
	 * 3 - Buscamos en la TablaPaginas (esta clase) si la p�gina est� en memoria principal 
	 *     (es decir, tiene MARCO asignado). Si esto ocurre pasamos al punto 5.
	 * 4 - Page Fault. Asignar un marco a la p�gina.
	 * 5 - Actualizar TLB.
	 * 6 - Construir la direcci�n REAL usando la TLB y el offset de la direcci�n virtual.
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
		// Una p�gina de 4KB tendr� tama�o 4096 / 4 = 1024.
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
		
		System.out.println("M�xima direcci�n virtual: " + ((entrada_maxima << 2)-1));
		System.out.println("M�xima direcci�n f�sica: " + (((num_paginas) << Global.bitsDireccionar(tam_pagina))-1));
		System.out.println("M�xima direcci�n memoria: " + (((num_marcos) << Global.bitsDireccionar(tam_pagina))-1));
		
		// La pol�tica s�lo tendr� 1 entrada, con el n�mero de marcos que hay.
		politica = new PoliticaReemplazo(_Tpolitica, 1, marcos.length);
	}
	
	// Crea una p�gina nueva.
	private Pagina crearPagina(int direccion) throws MemoryException
	{
		int id = calcularId(direccion);
		Log.println(3, "Creando p�gina " + id);
		Pagina nueva = new Pagina(entradas_pagina, palabras_linea, id);
		paginas.put(id, nueva);
		return nueva;
	}
	
	// Calcular id de p�gina. La ID se calcula con direcciones VIRTUALES.
	public int calcularId(int direccion)
	{
		return (int) Math.floor(direccion / tam_pagina);
	}
	
	// Traduce una direcci�n virtual a una f�sica.
	// Si no le corresponde una direcci�n f�sica deber� poner la p�gina en un marco.
	public Direccion traducirDireccion(int direccion, boolean secundaria) throws MemoryException
	{
		if ((direccion >> Global.bitsDireccionar(bytes_palabra)) >= entrada_maxima)
			throw new MemoryException("La direcci�n 0x" + Integer.toHexString(direccion) + " sobrepasa el l�mite de direccionamiento.");
		
		// Primero buscamos en qu� p�gina debe estar la direcci�n.
		Pagina pag = seleccionarPagina(direccion);
		
		// Ya tenemos la p�gina correspondiente, comprobamos si est� en un marco.
		if (pag.getMarco() != -1)
		{
			// Comprobamos si est� en la TLB (en realidad no se utiliza para nada, s�lo para ver si es HIT o MISS)
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
			
			// Si estamos aqu� es porque se ha encontrado la p�gina asociada a un marco.
			// OJO! Puede ser PAGE HIT y TLB MISS.
			Log.report(Flags.PAGE_HIT, secundaria);
			Log.println(2,"PAGE HIT");
			
			// La p�gina est� en un marco, podemos traducir la direcci�n.
			//Log.println(3, "La p�gina ya est� en el marco " + pag.getMarco());
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
			// Tenemos que guardar la p�gina en un marco.
			int marco = buscarMarcoLibre();
			if (marco != -1)
			{
				asignarPaginaMarco(pag, marco);
				Log.println(3, "P�gina asignada al marco " + marco);
				return calcularDireccion(direccion, marco);
			}
			else
			{
				marco = liberarMarco();
				asignarPaginaMarco(pag, marco);
				Log.println(3, "Se ha reemplazado la p�gina del marco " + marco);
				return calcularDireccion(direccion, marco);
			}
		}
	}
	
	// Traducci�n.
	private Direccion calcularDireccion(int direccion, int marco)
	{
		int offset = (int) Math.floor(direccion % tam_pagina);
		int res = (marco << Global.bitsDireccionar(tam_pagina)) + offset;
		int id_pagina = calcularId(direccion);
		//Log.println(3, "Direcci�n 0x" + Integer.toHexString(direccion) + " traducida como 0x" + Integer.toHexString(res) + " [" + marco + "] " + offset);
		return new Direccion(direccion, res, id_pagina);
	}
	
	// Selecciona la p�gina correspondiente a la direcci�n VIRTUAL.
	public Pagina seleccionarPagina(int direccion) throws MemoryException
	{
		// Buscamos la p�gina.
		int id = calcularId(direccion);
		Pagina pag = paginas.get(id);
		
		// Si la p�gina no existe, la creamos.
		if (pag == null)
			pag = crearPagina(direccion);
		
		return pag;
	}
	
	// Busca una p�gina por ID (si no existe no la crea).
	public Pagina buscarPagina(int id) throws MemoryException
	{
		Pagina pag = paginas.get(id);
		return pag;
	}
	
	// Busca un marco libre donde insertar la p�gina.
	private int buscarMarcoLibre()
	{
		// Creamos una lista con todos los marcos libres.
		List<Integer> lista = new ArrayList<Integer>();
		for (int i = 0; i < marcos.length; i++)
		{
			if (marcos[i] == null)
				lista.add(i);
		}
		
		// Seleccionamos una posici�n aleatoriamente.
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
			throw new MemoryException("P�gina asignada a marco en asignaci�n.");
		
		if (marcos[marco] != null)
			throw new MemoryException("Marco ocupado al asignar p�gina.");
		
		marcos[marco] = pag;
		pag.asignarMarco(marco);
		politica.nuevaLinea(0, marco);
		
		// Actualizar la interfaz de memoria para este marco.
		jerarquia1.actualizarMarcoInterfazMemoria(marco);
		if (jerarquia2 != null)
			jerarquia2.actualizarMarcoInterfazMemoria(marco);
	}
	
	// Libera un marco (seg�n pol�tica de reemplazo).
	private int liberarMarco() throws MemoryException
	{
		int marco_libre = politica.elegirViaReemplazo(0);
		
		int id = marcos[marco_libre].getId();
		
		// Eliminamos todas las referencias a la p�gina anterior en cach�.
		jerarquia1.invalidarPagina(id);
		if (jerarquia2 != null)
			jerarquia2.invalidarPagina(id);
		
		// Eliminar referencias de la p�gina anterior.
		marcos[marco_libre].asignarMarco(-1);
		marcos[marco_libre] = null;
		
		return marco_libre;
	}
	
	// Actualizar la pol�tica de reemplazo cuando se produce un acceso
	public void accesoMarco(int marco)
	{
		politica.accesoLinea(0, marco);
	}
	
	// Selecciona el marco a partir de la direcci�n REAL
	public int seleccionarMarco(int direccion)
	{
		int marco = direccion >> Global.bitsDireccionar(tam_pagina);
		return marco;
	}
	
	// Devuelve la lista de marcos.
	public Pagina[] getMarcos() { return marcos; }
	
	// Devuelve el tama�o de p�gina.
	public int getTama�oPagina() { return tam_pagina; }
	
	// Devuelve el tama�o de p�gina.
	public int getEntradasPagina() { return entradas_pagina; }
	
	// Devuelve el n�mero de p�ginas.
	public int getNumeroPaginas() { return num_paginas; }
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder("P�ginas en marco: \n");
		for (int i = 0; i < marcos.length; i++)
			strB.append("Marco [" + i + "]" + "\n" + marcos[i] + "\n");
		
		strB.append("\n");
		strB.append("N�mero de p�ginas en total: " + paginas.size());
		
		return strB.toString();
	}

	public void setJerarquiaMemoria(JerarquiaMemoria jmem1, JerarquiaMemoria jmem2)
	{
		jerarquia1 = jmem1;
		jerarquia2 = jmem2;
	}
	
	// Este m�todo inicializar en memoria virtual el contenido de la RAM.
	// Se crea la p�gina y se guarda el dato, pero sin asignarla a marco.
	// La direcci�n recibida es VIRTUAL.
	public void inicializarDatoMemoriaVirtual(int direccion_virtual, int dato) throws MemoryException
	{
		// Selecciono o creo la p�gina si es necesario.
		Pagina pag = seleccionarPagina(direccion_virtual);
		if (pag.estaLibre(direccion_virtual))
			pag.guardarLinea(direccion_virtual, new int[]{dato});
		else
			throw new MemoryException("Se sobreescribe un dato en la direcci�n 0x" + Integer.toHexString(direccion_virtual));
	}
}
