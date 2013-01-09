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
	
	// Esta clase funciona �nicamente con direcciones REALES.
	private SortedMap<Integer, Pagina> paginas;
	private Pagina[] marcos;
	
	private int entradas_pagina;
	private int tam_pagina;
	private int entrada_maxima;
	private int num_paginas;
	private int num_marcos;
	private int palabras_linea;
	
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
	public TablaPaginas(int ent_pag, int pal_linea, int max_ent_mem, int ent_mem_princ, TiposReemplazo _Tpolitica)
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
		tam_pagina = entradas_pagina * 4;
		
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
		System.out.println("Creando p�gina id " + id);
		Pagina nueva = new Pagina(entradas_pagina, palabras_linea, id);
		paginas.put(id, nueva);
		return nueva;
	}
	
	// Calcular id de p�gina
	public int calcularId(int direccion)
	{
		return (int) Math.floor(direccion / tam_pagina);
	}
	
	// Traduce una direcci�n virtual a una f�sica.
	// Si no le corresponde una direcci�n f�sica deber� poner la p�gina en un marco.
	public int traducirDireccion(int direccion) throws MemoryException
	{
		if ((direccion >> 2) >= entrada_maxima)
			throw new MemoryException("La direcci�n sobrepasa el l�mite de direccionamiento.");
			
		// Primero buscamos en qu� p�gina debe estar la direcci�n.
		Pagina pag = seleccionarPagina(direccion);
		
		// Ya tenemos la p�gina correspondiente, comprobamos si est� en un marco.
		if (pag.getMarco() != -1)
		{
			// La p�gina est� en un marco, podemos traducir la direcci�n.
			int offset = (int) Math.floor(direccion % tam_pagina);
			int marco = pag.getMarco();
			int res = (marco << Global.bitsDireccionar(tam_pagina)) + offset;
			return res;
		}
		else
		{
			// Tenemos que guardar la p�gina en un marco.
			int marco = buscarMarcoLibre();
			if (marco != -1)
			{
				marcos[marco] = pag;
				pag.asignarMarco(marco);
				int offset = (int) Math.floor(direccion % tam_pagina);
				int res = (marco << Global.bitsDireccionar(tam_pagina)) + offset;
				System.out.println("offset " + offset);
				System.out.println("Asignada a marco " + marco);
				return res;
			}
			else
			{
				marco = liberarMarco();
				marcos[marco] = pag;
				pag.asignarMarco(marco);
				int offset = (int) Math.floor(direccion % tam_pagina);
				int res = (marco << Global.bitsDireccionar(tam_pagina)) + offset;
				return res;
			}
		}
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
		{
			return -1;
		}
	}
	
	// Libera un marco (seg�n pol�tica de reemplazo).
	private int liberarMarco()
	{
		return -1;
	}
	
	// Selecciona la p�gina en la que se encuentra la direcci�n REAL.
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
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder("P�ginas en marco: ");
		for (int i = 0; i < marcos.length; i++)
			strB.append(marcos[i] + ",");
		
		strB.append("\n");
		strB.append("N�mero de p�ginas en total: " + paginas.size());
		
		return strB.toString();
	}

}
