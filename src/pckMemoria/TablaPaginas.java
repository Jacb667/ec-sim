package pckMemoria;

import general.Global;
import general.Global.TiposReemplazo;
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
	
	private int tamaño_pagina;
	private long memoria_maxima;
	private int num_paginas;
	private int num_marcos;
	private int palabras_linea;
	
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
	public TablaPaginas(int tam_pagina, int pal_linea, long max_memoria, int tam_mem_princ, TiposReemplazo _Tpolitica)
	{
		// Max_memoria por defecto: 0xFFFFFFFF
		// Memoria principal por defecto: 2048 (64KB).
		// Una página de 4KB tendrá tamaño 4096 / 4 = 1024.
		memoria_maxima = max_memoria / 4;
		num_paginas = (int) (memoria_maxima / tam_pagina);
		System.out.println("memoria_maxima: " + memoria_maxima);
		System.out.println("tam_pagina: " + tam_pagina);
		System.out.println("num_paginas: " + num_paginas);
		num_marcos = tam_mem_princ / tam_pagina;
		marcos = new Pagina[num_marcos];
		paginas = new TreeMap<Integer, Pagina>();
		tamaño_pagina = tam_pagina;
		palabras_linea = pal_linea;
		
		// La política sólo tendrá 1 entrada, con el número de marcos que hay.
		politica = new PoliticaReemplazo(_Tpolitica, 1, marcos.length);
	}
	
	// Crea una página nueva.
	public Pagina crearPagina(int direccion) throws MemoryException
	{
		int id = calcularId(direccion);
		System.out.println("Creando página id " + id);
		Pagina nueva = new Pagina(tamaño_pagina, palabras_linea, id);
		paginas.put(id, nueva);
		return nueva;
	}
	
	// Calcular id de página
	private int calcularId(int direccion)
	{
		// 1048575
		return (int) Math.floor((direccion >> 2) / tamaño_pagina);
	}
	
	// Traduce una dirección virtual a una física.
	// Si no le corresponde una dirección física deberá poner la página en un marco.
	public int traducirDireccion(int direccion) throws MemoryException
	{
		// Primero buscamos en qué página debe estar la dirección.
		Pagina pag = seleccionarPagina(direccion);
		
		// Ya tenemos la página correspondiente, comprobamos si está en un marco.
		if (pag.getMarco() != -1)
		{
			// La página está en un marco, podemos traducir la dirección.
			int offset = (int) Math.floor(direccion % tamaño_pagina);
			int marco = pag.getMarco();
			int res = marco << Global.bitsDireccionar(tamaño_pagina) + offset;
			return res;
		}
		else
		{
			// Tenemos que guardar la página en un marco.
			int marco = buscarMarcoLibre();
			if (marco != -1)
			{
				marcos[marco] = pag;
				pag.asignarMarco(marco);
				int offset = (int) Math.floor(direccion % tamaño_pagina);
				int res = marco << Global.bitsDireccionar(tamaño_pagina) + offset;
				System.out.println("offset " + offset);
				System.out.println("Asignada a marco " + marco);
				return res;
			}
			else
			{
				marco = liberarMarco();
				marcos[marco] = pag;
				pag.asignarMarco(marco);
				int offset = (int) Math.floor(direccion % tamaño_pagina);
				int res = marco << Global.bitsDireccionar(tamaño_pagina) + offset;
				return res;
			}
		}
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
		{
			return -1;
		}
	}
	
	// Libera un marco (según política de reemplazo).
	private int liberarMarco()
	{
		return -1;
	}
	
	// Selecciona la página en la que se encuentra la dirección REAL.
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
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder("Páginas en marco: ");
		for (int i = 0; i < marcos.length; i++)
			strB.append(marcos[i] + ",");
		
		strB.append("\n");
		strB.append("Número de páginas en total: " + paginas.size());
		
		return strB.toString();
	}

}
