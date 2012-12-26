package pckMemoria;

import general.Global.TiposReemplazo;
import general.MemoryException;


public class CacheAsociativa implements Cache
{
	// Cada una de las vías se implementa como una caché directa.
	// De modo que el diseño es bastante simple.
	private CacheDirecta vias[];
	
	private int entradas;
	private int palabras_linea;
	public PoliticaReemplazo politica;
	
	// En caché directa se recomienda usar tamaños de potencias de 2^x.
	// En caché asociativa la división entradas/vías DEBE dar exacto (no decimales).
	// También se recomienda que entradas sea potencia de 2 (y divisible entre vías).
	// Estas comprobaciones deben ser echas antes de invocar a este constructor.
	public CacheAsociativa(int _entradas, int _palabras_linea, int _vias, TiposReemplazo _Tpolitica)
	{
		entradas = _entradas / _vias;
		palabras_linea = _palabras_linea;
		
		// Creamos el array de vías
		vias = new CacheDirecta[_vias];
		
		// Creamos las vías
		for (int i = 0; i < _vias; i++)
			vias[i] = new CacheDirecta(entradas, palabras_linea);
		
		politica = new PoliticaReemplazo(_Tpolitica, entradas, vias.length);
	}
	
	// Para saber si un dato está, comprobamos todas las vías.
	public boolean existeDato(int direccion)
	{
		boolean res = false;
		int via = 0;
		while (!res && via < vias.length)
		{
			res = vias[via].existeDato(direccion);
			via++;
		}
		
		return res;
	}

	// Si esto se ejecuta es porque sabemos que el dato está (en alguna vía).
	// Compruebo en qué vía está y leo el dato.
	public int consultarDato(int direccion) throws MemoryException
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
			{
				politica.accesoLinea(buscarPosicion(direccion), i);
				return vias[i].consultarDato(direccion);
			}
		}

		// Nunca deberíamos llegar aquí...
		throw new MemoryException("Consulta de dato no existente en dirección 0x" + Integer.toHexString(direccion));
	}

	// Si esto se ejecuta es porque sabemos que el bloque del dato está (en alguna vía).
	// Compruebo en qué vía está y guardo el dato.
	public void modificarDato(int direccion, int dato) throws MemoryException
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
			{
				politica.accesoLinea(buscarPosicion(direccion), i);
				vias[i].modificarDato(direccion, dato);
				return;
			}
		}
		
		// Nunca deberíamos llegar aquí...
		throw new MemoryException("Modificación de dato no existente en dirección 0x" + Integer.toHexString(direccion));
	}
	
	// Leer una línea.
	public int[] leerLinea(int direccion) throws MemoryException
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
				return vias[i].leerLinea(direccion);
		}

		// Nunca deberíamos llegar aquí...
		throw new MemoryException("Lectura de línea no existente en dirección 0x" + Integer.toHexString(direccion));
	}

	// Guardar una línea.
	// Si ejecutamos este método es porque al menos existe una vía libre donde guardarlo.
	public void escribirLinea(int direccion, int[] linea) throws MemoryException
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].lineaLibre(direccion))
			{
				vias[i].escribirLinea(direccion, linea);
				politica.nuevaLinea(buscarPosicion(direccion), i);
				return;
			}
		}
		
		// Nunca deberíamos llegar aquí...
		throw new MemoryException("Escritura de línea imposible en dirección 0x" + Integer.toHexString(direccion));
	}
	
	// Actualizar una línea existente.
	public void actualizarLinea(int direccion, int[] linea)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
			{
				politica.accesoLinea(buscarPosicion(direccion), i);
				vias[i].escribirLinea(direccion, linea);
				return;
			}
		}
	}
	
	// Reemplaza una línea por otra. Devuelve la línea anterior.
	// Usará la política de reemplazo para determinar qué línea se elimina.
	public LineaReemplazo reemplazarLinea(int direccion, int[] linea) throws MemoryException
	{
		int via = politica.elegirViaReemplazo(buscarPosicion(direccion));
		
		// Reemplazamos. Devolverá null si la línea no estaba sucia.
		LineaReemplazo res = vias[via].reemplazarLinea(direccion, linea);
		
		politica.nuevaLinea(buscarPosicion(direccion), via);
		
		return res;
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		
		for (int i = 0; i < vias.length; i++)
		{
			strB.append("-- Via ").append(i).append("\n");
			strB.append(vias[i].toString()).append("\n");
		}
		
		return strB.toString();
	}
	
	public int getTamanoLinea()
	{
		return palabras_linea;
	}

	// Me determina si una dirección está libre o no.
	// Si está libre significa que puedo escribir, en caso contrario
	// tendré que reemplazar antes de escribir.
	public boolean lineaLibre(int direccion)
	{
		boolean res = false;
		int i = 0;
		while (!res && i < vias.length)
			res = vias[i].lineaLibre(direccion);

		return res;
	}
	
	// Busco la posición en el array (entry) del dato.
	private int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los últimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> 2 >> general.Op.bitsDireccionar(palabras_linea);;
		
		// Los siguientes bits son del índice.
		// La entrada será el módulo del número de entradas.
		return (int) (pos % entradas);
	}
}

