package pckMemoria;

import general.Global;
import general.MemoryException;

import java.awt.Dimension;
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

	private int bits_dir;
	private int bits_pal;

	private int[] tags;
	private boolean[] valid;
	private boolean[] dirty;
	private int[/*lineas*/][/*palabras*/] datos;

	public CacheDirecta(int _entradas, int _palabras_linea) throws MemoryException
	{
		if (_entradas < 1 || _palabras_linea < 1)
			throw new MemoryException("Error en inicialización de caché.");
		
		palabras_linea = _palabras_linea;
		entradas = _entradas;
		
		// Bits direccionamiento.
		if (palabras_linea > 1)
		{
			bits_pal = general.Op.bitsDireccionar(palabras_linea);
			// Eliminar bits palabra.
		}
		// Direccionar entradas
		bits_dir = general.Op.bitsDireccionar(entradas);
		
		tags = new int[entradas];
		valid = new boolean[entradas];
		dirty = new boolean[entradas];
		
		datos = new int[entradas][palabras_linea];
	}
	
	// Comprobaciones
	public int getTamanoLinea() { return palabras_linea; }
	
	// Me determina si la línea en esa dirección está "sucia", es decir,
	// si se debe enviar a memoria principal antes de escribir en ella.
	private boolean lineaDirty(int direccion)
	{
		return dirty[buscarPosicion(direccion)];
	}
	
	// Esta función me determina si la posición está libre o no.
	// Es decir, si se puede almacenar una línea sin "pisar" otra.
	public boolean lineaLibre(int direccion)
	{
		return !valid[buscarPosicion(direccion)];
	}

	// Me determina si el dato existe o no. Para que exista el dato se debe
	// comprobar el TAG, ya que en una misma posición puede haber distintas líneas.
	public boolean existeDato(int direccion)
	{
		int entrada = buscarPosicion(direccion);
		
		// Buscamos en el tag para ver si existe la palabra.
		if (valid[entrada] && tags[entrada] == extraerTag(direccion))
			return true;
		
		return false;
	}
	
	// Leer el dato. Este método se ejecuta después de "existeDato".
	// Es decir, si ejecutamos este método es porque ya sabemos que el dato existe y se puede leer.
	public int consultarDato(int direccion)
	{
		int pos = buscarPosicion(direccion);
		int pal = posicionPalabra(direccion);
		
		return datos[pos][pal];
	}

	// Guardamos el dato en su posición.
	// Si se llama a este método es porque la línea correspondiente ya está cargada en esta caché.
	public void modificarDato(int direccion, int dato)
	{
		int pos = buscarPosicion(direccion);
		int pal = posicionPalabra(direccion);
		
		tags[pos] = extraerTag(direccion);
		datos[pos][pal] = dato;
		valid[pos] = true;
		dirty[pos] = true;
	}

	// Leer línea que contiene la dirección especificada
	// Este método se ejecuta después de "existeDato".
	// Es decir, si ejecutamos este método es porque ya sabemos que el dato existe y se puede leer.
	public int[] leerLinea(int direccion)
	{
		int[] res = new int[palabras_linea];
		int direccion_inicio = buscarPosicion(direccion);
		
		for (int i = 0; i < palabras_linea; i++)
			res[i] = datos[direccion_inicio][i];
		
		return res;
	}

	// Escribe una línea en la caché.
	// Este método guarda el dato en la línea especificada, no comprueba si ya estaba ocupado.
	public void escribirLinea(int direccion, int[] linea)
	{
		int direccion_inicio = buscarPosicion(direccion);
		
		tags[direccion_inicio] = extraerTag(direccion);
		for (int i = 0; i < palabras_linea; i++)
			datos[direccion_inicio][i] = linea[i];
		
		dirty[direccion_inicio] = false;
		valid[direccion_inicio] = true;
	}
	
	// Actualiza una línea existente.
	public void actualizarLinea(int direccion, int[] linea)
	{
		escribirLinea(direccion, linea);
	}
	
	// Reemplaza una línea en la caché por otra.
	// Si la línea estaba "sucia", la devuelve para poder enviarla a otro nivel.
	public LineaReemplazo reemplazarLinea(int direccion, int[] linea)
	{
		LineaReemplazo res = null;
		
		// La devolvemos solamente si está "sucia".
		if (lineaDirty(direccion))
			res = new LineaReemplazo(getDireccionGuardado(direccion), leerLinea(direccion));
		
		escribirLinea(direccion, linea);
		
		return res;
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < datos.length; i++)
		{
			strB.append(String.format("0x%3S", Integer.toHexString(i << 2 << bits_pal)).replace(" ", "0"));
			strB.append(" -> ").append(Integer.toHexString(tags[i])).append(" : ").append(Arrays.toString(datos[i]));
			strB.append(" ").append(valid[i]).append(" ").append(dirty[i]);
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
		// La entrada será el módulo de palabras po línea.
		return (int) (pos % palabras_linea);
	}

	// Busco la posición en el array (entry) del dato.
	private int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los últimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> 2 >> bits_pal;
		
		// Los siguientes bits son del índice.
		// La entrada será el módulo del número de entradas.
		return (int) (pos % entradas);
	}
	
	// Me devuelve la dirección de la línea guardada donde iría dirección.
	private int getDireccionGuardado(int direccion)
	{
		int posicion = buscarPosicion(direccion);
		
		// Cogemos el TAG guardado.
		int dir = tags[posicion];
		
		// bits_dir y sumamos la posición.
		dir = dir << bits_dir;
		dir += posicion;
		
		// Añadimos bits de palabra y offset.
		dir = dir << bits_pal << 2;

		return dir;
	}
	
	// Extrae el tag de una dirección.
	private int extraerTag(int direccion)
	{
		return direccion >> 2 >> bits_pal >> bits_dir;
	}
	
	
	/*
	 *  Funciones para JTable (interfaz gráfica).
	 */
	public String[] getColumnas()
	{
		int tamaño = 4 + palabras_linea;
		String[] columnas = new String[tamaño];
		columnas[0] = "Línea";
		columnas[1] = "Tag";
		columnas[tamaño-2] = "Válida";
		columnas[tamaño-1] = "Dirty";
		for (int i = 0; i < palabras_linea; i++)
			columnas[i+2] = "Palabra " + String.valueOf(i);
		
		return columnas;
	}
	
	public Object[][] getDatos()
	{
		int tamaño = 4 + palabras_linea;
		Object[][] res = new Object[entradas][tamaño];
		
		for (int i = 0; i < entradas; i++)
		{
			int direccion = i << 2 << bits_pal;
			
			Object[] linea = new Object[tamaño];
			
			linea[0] = String.format("0x%4S", Integer.toHexString(direccion)).replace(" ", "0");
			linea[1] = String.valueOf(tags[i]);
			linea[tamaño-1] = new Boolean(dirty[i]);
			linea[tamaño-2] = new Boolean(valid[i]);
			
			for (int j = 0; j < palabras_linea; j++)
				linea[j+2] = datos[i][j];

			res[(int) Math.floor(i)] = linea;
		}
		
		return res;
	}
	
	public Dimension[] getTamaños()
	{
		int tamaño = 4 + palabras_linea;
		Dimension[] dim = new Dimension[tamaño];
		
		for (int i = 0; i < tamaño-2; i++)
			dim[i] = new Dimension(Global.TAMAÑO_CELDA_NORMAL, 0);
		
		dim[tamaño-1] = new Dimension(Global.TAMAÑO_CELDA_BOOLEAN, Global.TAMAÑO_CELDA_BOOLEAN*2);
		dim[tamaño-2] = new Dimension(Global.TAMAÑO_CELDA_BOOLEAN, Global.TAMAÑO_CELDA_BOOLEAN*2);
		
		return dim;
	}
	
	public Object getDato(int linea, int posicion)
	{
		if (posicion == 0)  // Tag
			return tags[linea];
		else if (posicion > 0 && posicion <= palabras_linea)  // Palabra
			return datos[linea][posicion-1];
		else if (posicion == palabras_linea+1)  // Valid
			return valid[linea];
		else
			return dirty[linea];
	}
}

