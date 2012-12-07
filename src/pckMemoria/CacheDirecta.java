package pckMemoria;

import java.util.Arrays;

/* Tamaño:
 * 
 * Palabras de 4 bytes.
 * 1024 entradas = 1024 * 4 = 4KB (1 word por línea).
 * 
 * 32 entradas * 4 word por línea = 128 bits por línea = 4096Bytes = 4KB
 * 
 */

public class CacheDirecta implements Cache
{
	private int palabras_linea;
	private int entradas;

	private int bits_tag;
	private int bits_dir;
	private int bits_pal;

	private int[] tags;
	private boolean[] valid;
	private boolean[] dirty;
	private int[/*lineas*/][/*palabras*/] datos;

	public CacheDirecta(int _entradas, int _palabras_linea)
	{
		palabras_linea = _palabras_linea;
		entradas = _entradas;
		
		// Eliminar offset
		int bits_restantes = general.Global.LONGITUD_BITS - 2;
		if (palabras_linea > 1)
		{
			bits_pal = general.Op.bitsDireccionar(palabras_linea);
			// Eliminar bits palabra.
			bits_restantes -= bits_pal;
		}
		// Direccionar entradas
		bits_dir = general.Op.bitsDireccionar(entradas);
		bits_restantes -= bits_dir;
		
		// Tag
		bits_tag = bits_restantes;
		
		tags = new int[entradas];
		valid = new boolean[entradas];
		dirty = new boolean[entradas];
		
		datos = new int[entradas][palabras_linea];
	}
	
	// Comprobaciones
	public int getTamanoLinea() { return palabras_linea; }
	public boolean isDirty(int direccion)
	{
		int entrada = buscarPosicion(direccion);
		
		// Extraemos el tag
		int tag = direccion >> 2 >> bits_pal >> bits_dir;
		
		// Buscamos en el tag para ver si existe la palabra.
		if (dirty[entrada] && tags[entrada] == tag)
			return true;
		
		return false;
	}

	public boolean existeDato(int direccion)
	{
		int entrada = buscarPosicion(direccion);
		
		// Extraemos el tag
		int tag = direccion >> 2 >> bits_pal >> bits_dir;
		
		// Buscamos en el tag para ver si existe la palabra.
		if (valid[entrada] && tags[entrada] == tag)
			return true;
		
		return false;
	}
	
	// Leer el dato en dirección
	// Este método se ejecuta después de "existeDato".
	// Es decir, si ejecutamos este método es porque ya sabemos que el dato existe y se puede leer.
	public int leerDato(int direccion)
	{
		int pos = buscarPosicion(direccion);
		int pal = posicionPalabra(direccion);
		
		return datos[pos][pal];
	}

	// Guardamos el dato en su posición.
	// Si se llama a este método es porque la línea correspondiente ya está cargada en esta caché.
	public void guardarDato(int direccion, int dato, boolean setDirty)
	{
		int pos = buscarPosicion(direccion);
		int pal = posicionPalabra(direccion);
		
		datos[pos][pal] = dato;
		
		if (setDirty)
			dirty[pos] = true;
	}

	// Leer línea que contiene la dirección especificada
	// Este método se ejecuta después de "existeDato".
	// Es decir, si ejecutamos este método es porque ya sabemos que el dato existe y se puede leer.
	public int[] leerLinea(int direccion)
	{
		int[] res =  new int[palabras_linea];
		int direccion_inicio = buscarPosicion(direccion);
		
		for (int i = 0; i < palabras_linea; i++)
		{
			res[i] = datos[direccion_inicio][i];
		}
		
		return res;
	}

	// Guardamos el dato en su posición.
	// Este método guarda el dato en la línea especificada, no comprueba si ya estaba ocupado.
	public void guardarLinea(int direccion, int[] linea, boolean setDirty)
	{
		int tam_linea = linea.length;
		int direccion_inicio = buscarPosicion(direccion);
		
		if (setDirty)
			dirty[direccion_inicio] = true;
		valid[direccion_inicio] = true;
		
		for (int i = 0; i < palabras_linea; i++)
		{
			datos[direccion_inicio][i] = linea[i];
		}
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < datos.length; i++)
		{
			// Dirección (hex) : Dato (dec)
			strB.append(String.format("0x%3S", Integer.toHexString(i << 2 << bits_pal)).replace(" ", "0")).append(" : ").append(Arrays.toString(datos[i]));
			strB.append("\n");
		}
		
		return strB.toString();
	}
	
	// Busco la posición de la palabra en la línea
	private int posicionPalabra(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		int pos = direccion >> 2;
			
		// Los siguientes bits son de la palabra.
		// La entrada será el módulo de 2^bits_pal
		return (int) (pos % palabras_linea);
	}

	// Busco la posición en el array (entry) del dato.
	private int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los últimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> 2 >> bits_pal;
		
		// Los siguientes bits son del índice.
		// La entrada será el módulo de 2^bits_dir
		return (int) (pos % entradas);
	}
}

