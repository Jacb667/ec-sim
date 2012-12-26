package pckMemoria;

import general.Global.TiposReemplazo;
import general.MemoryException;


public class CacheAsociativa implements Cache
{
	// Cada una de las v�as se implementa como una cach� directa.
	// De modo que el dise�o es bastante simple.
	private CacheDirecta vias[];
	
	private int entradas;
	private int palabras_linea;
	public PoliticaReemplazo politica;
	
	// En cach� directa se recomienda usar tama�os de potencias de 2^x.
	// En cach� asociativa la divisi�n entradas/v�as DEBE dar exacto (no decimales).
	// Tambi�n se recomienda que entradas sea potencia de 2 (y divisible entre v�as).
	// Estas comprobaciones deben ser echas antes de invocar a este constructor.
	public CacheAsociativa(int _entradas, int _palabras_linea, int _vias, TiposReemplazo _Tpolitica)
	{
		entradas = _entradas / _vias;
		palabras_linea = _palabras_linea;
		
		// Creamos el array de v�as
		vias = new CacheDirecta[_vias];
		
		// Creamos las v�as
		for (int i = 0; i < _vias; i++)
			vias[i] = new CacheDirecta(entradas, palabras_linea);
		
		politica = new PoliticaReemplazo(_Tpolitica, entradas, vias.length);
	}
	
	// Para saber si un dato est�, comprobamos todas las v�as.
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

	// Si esto se ejecuta es porque sabemos que el dato est� (en alguna v�a).
	// Compruebo en qu� v�a est� y leo el dato.
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

		// Nunca deber�amos llegar aqu�...
		throw new MemoryException("Consulta de dato no existente en direcci�n 0x" + Integer.toHexString(direccion));
	}

	// Si esto se ejecuta es porque sabemos que el bloque del dato est� (en alguna v�a).
	// Compruebo en qu� v�a est� y guardo el dato.
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
		
		// Nunca deber�amos llegar aqu�...
		throw new MemoryException("Modificaci�n de dato no existente en direcci�n 0x" + Integer.toHexString(direccion));
	}
	
	// Leer una l�nea.
	public int[] leerLinea(int direccion) throws MemoryException
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
				return vias[i].leerLinea(direccion);
		}

		// Nunca deber�amos llegar aqu�...
		throw new MemoryException("Lectura de l�nea no existente en direcci�n 0x" + Integer.toHexString(direccion));
	}

	// Guardar una l�nea.
	// Si ejecutamos este m�todo es porque al menos existe una v�a libre donde guardarlo.
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
		
		// Nunca deber�amos llegar aqu�...
		throw new MemoryException("Escritura de l�nea imposible en direcci�n 0x" + Integer.toHexString(direccion));
	}
	
	// Actualizar una l�nea existente.
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
	
	// Reemplaza una l�nea por otra. Devuelve la l�nea anterior.
	// Usar� la pol�tica de reemplazo para determinar qu� l�nea se elimina.
	public LineaReemplazo reemplazarLinea(int direccion, int[] linea) throws MemoryException
	{
		int via = politica.elegirViaReemplazo(buscarPosicion(direccion));
		
		// Reemplazamos. Devolver� null si la l�nea no estaba sucia.
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

	// Me determina si una direcci�n est� libre o no.
	// Si est� libre significa que puedo escribir, en caso contrario
	// tendr� que reemplazar antes de escribir.
	public boolean lineaLibre(int direccion)
	{
		boolean res = false;
		int i = 0;
		while (!res && i < vias.length)
			res = vias[i].lineaLibre(direccion);

		return res;
	}
	
	// Busco la posici�n en el array (entry) del dato.
	private int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los �ltimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> 2 >> general.Op.bitsDireccionar(palabras_linea);;
		
		// Los siguientes bits son del �ndice.
		// La entrada ser� el m�dulo del n�mero de entradas.
		return (int) (pos % entradas);
	}
}

