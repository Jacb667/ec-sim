package pckMemoria;

import general.Global.PoliticasReemplazo;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class CacheAsociativa implements Cache
{
	
	// Cada una de las vías se implementa como una caché directa.
	// De modo que el diseño es bastante simple.
	private CacheDirecta vias[];
	
	private int entradas;
	private int palabras_linea;
	private PoliticasReemplazo politica;
	
	// En caché directa se recomienda usar tamaños de potencias de 2^x.
	// En caché asociativa la división entradas/vías DEBE dar exacto (no decimales).
	// También se recomienda que entradas sea potencia de 2 (y divisible entre vías).
	// Estas comprobaciones deben ser echas antes de invocar a este constructor.
	public CacheAsociativa(int _entradas, int _palabras_linea, int _vias, PoliticasReemplazo _politica)
	{
		entradas = _entradas / _vias;
		palabras_linea = _palabras_linea;
		
		// Creamos el array de vías
		vias = new CacheDirecta[_vias];
		
		// Creamos las vías
		for (int i = 0; i < _vias; i++)
			vias[i] = new CacheDirecta(entradas, palabras_linea);
		
		politica = _politica;
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

	// Compruebo si isDirty en la vía que esté.
	public boolean lineaDirty(int direccion)
	{
		boolean isDirty = false;
		int i = 0;
		while (!isDirty && i < vias.length)
		{
			if (vias[i].existeDato(direccion))
				isDirty = vias[i].lineaDirty(direccion);
			i++;
		}
		
		return isDirty;
	}

	public int getTamanoLinea()
	{
		return palabras_linea;
	}

	// Si esto se ejecuta es porque sabemos que el dato está (en alguna vía).
	// Compruebo en qué vía está y leo el dato.
	public int consultarDato(int direccion)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
				return vias[i].consultarDato(direccion);
		}

		// Nunca deberíamos llegar aquí...
		return 0;
	}

	// Si esto se ejecuta es porque sabemos que el bloque del dato está (en alguna vía).
	// Compruebo en qué vía está y guardo el dato.
	public void modificarDato(int direccion, int dato)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
			{
				vias[i].modificarDato(direccion, dato);
				break;
			}
		}
	}

	// Leer una línea.
	public int[] leerLinea(int direccion)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
				return vias[i].leerLinea(direccion);
		}

		// Nunca deberíamos llegar aquí...
		return null;
	}

	// Guardar una línea.
	// Si ejecutamos este método es porque al menos existe una vía libre donde guardarlo.
	public void escribirLinea(int direccion, int[] linea)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].lineaLibre(direccion))
			{
				vias[i].escribirLinea(direccion, linea);
				break;
			}
		}
	}
	
	private int elegirLineaReemplazo(int direccion)
	{
		int res = 0;
		
		switch(politica)
		{
			// Reemplaza la línea que menos se usa recientemente.
			case LRU:
				
				
			// Reemplaza el bloque que se ha usado menos veces.
			case LFU:		
				
				
			// Reemplaza el primer bloque que entró.
			case FIFO:
				
				
			// Reemplaza el primer bloque que entró y no se ha usado.
			case SCHANC:

			
			// Reemplaza un bloque aleatorio.
			default:
				Random rand = new Random(new Date().getTime());
				res = rand.nextInt(vias.length);
		}
		
		return res;
	}
	
	// Reemplaza una línea por otra. Devuelve la línea anterior.
	// Usará la política de reemplazo para determinar qué línea se elimina.
	public int[] reemplazarLinea(int direccion, int[] linea)
	{
		int res[] = new int[palabras_linea];
		int via = elegirLineaReemplazo(direccion);
		
		res = vias[via].leerLinea(direccion);
		vias[via].escribirLinea(direccion, linea);
		
		return res;
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		
		for (int i = 0; i < vias.length; i++)
		{
			strB.append("-- Via ").append(i).append("\n\n");
			strB.append(vias[i].toString()).append("\n");
			strB.append("\n");
		}
		
		return strB.toString();
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
}
