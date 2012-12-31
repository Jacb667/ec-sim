package pckMemoria;

import general.Global;
import general.MemoryException;

import java.awt.Dimension;
import java.util.Arrays;

/* Tama�o:
 * 
 * Palabras de 4 bytes.
 * 1024 entradas = 1024 * 4 = 4KB (1 word por l�nea).
 * 
 * 32 entradas * 4 word por l�nea = 128 bits por l�nea = 4096Bytes = 4KB
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
			throw new MemoryException("Error en inicializaci�n de cach�.");
		
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
	
	// Me determina si la l�nea en esa direcci�n est� "sucia", es decir,
	// si se debe enviar a memoria principal antes de escribir en ella.
	private boolean lineaDirty(int direccion)
	{
		return dirty[buscarPosicion(direccion)];
	}
	
	// Esta funci�n me determina si la posici�n est� libre o no.
	// Es decir, si se puede almacenar una l�nea sin "pisar" otra.
	public boolean lineaLibre(int direccion)
	{
		return !valid[buscarPosicion(direccion)];
	}

	// Me determina si el dato existe o no. Para que exista el dato se debe
	// comprobar el TAG, ya que en una misma posici�n puede haber distintas l�neas.
	public boolean existeDato(int direccion)
	{
		int entrada = buscarPosicion(direccion);
		
		// Buscamos en el tag para ver si existe la palabra.
		if (valid[entrada] && tags[entrada] == extraerTag(direccion))
			return true;
		
		return false;
	}
	
	// Leer el dato. Este m�todo se ejecuta despu�s de "existeDato".
	// Es decir, si ejecutamos este m�todo es porque ya sabemos que el dato existe y se puede leer.
	public int consultarDato(int direccion)
	{
		int pos = buscarPosicion(direccion);
		int pal = posicionPalabra(direccion);
		
		return datos[pos][pal];
	}

	// Guardamos el dato en su posici�n.
	// Si se llama a este m�todo es porque la l�nea correspondiente ya est� cargada en esta cach�.
	public void modificarDato(int direccion, int dato)
	{
		int pos = buscarPosicion(direccion);
		int pal = posicionPalabra(direccion);
		
		tags[pos] = extraerTag(direccion);
		datos[pos][pal] = dato;
		valid[pos] = true;
		dirty[pos] = true;
	}

	// Leer l�nea que contiene la direcci�n especificada
	// Este m�todo se ejecuta despu�s de "existeDato".
	// Es decir, si ejecutamos este m�todo es porque ya sabemos que el dato existe y se puede leer.
	public int[] leerLinea(int direccion)
	{
		int[] res = new int[palabras_linea];
		int direccion_inicio = buscarPosicion(direccion);
		
		for (int i = 0; i < palabras_linea; i++)
			res[i] = datos[direccion_inicio][i];
		
		return res;
	}

	// Escribe una l�nea en la cach�.
	// Este m�todo guarda el dato en la l�nea especificada, no comprueba si ya estaba ocupado.
	public void escribirLinea(int direccion, int[] linea)
	{
		int direccion_inicio = buscarPosicion(direccion);
		
		tags[direccion_inicio] = extraerTag(direccion);
		for (int i = 0; i < palabras_linea; i++)
			datos[direccion_inicio][i] = linea[i];
		
		dirty[direccion_inicio] = false;
		valid[direccion_inicio] = true;
	}
	
	// Actualiza una l�nea existente.
	public void actualizarLinea(int direccion, int[] linea)
	{
		escribirLinea(direccion, linea);
	}
	
	// Reemplaza una l�nea en la cach� por otra.
	// Si la l�nea estaba "sucia", la devuelve para poder enviarla a otro nivel.
	public LineaReemplazo reemplazarLinea(int direccion, int[] linea)
	{
		LineaReemplazo res = null;
		
		// La devolvemos solamente si est� "sucia".
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
	
	// Busco la posici�n de la palabra en la l�nea
	private int posicionPalabra(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		int pos = direccion >> 2;
		
		// Los siguientes bits son de la palabra.
		// La entrada ser� el m�dulo de palabras po l�nea.
		return (int) (pos % palabras_linea);
	}

	// Busco la posici�n en el array (entry) del dato.
	private int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los �ltimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> 2 >> bits_pal;
		
		// Los siguientes bits son del �ndice.
		// La entrada ser� el m�dulo del n�mero de entradas.
		return (int) (pos % entradas);
	}
	
	// Me devuelve la direcci�n de la l�nea guardada donde ir�a direcci�n.
	private int getDireccionGuardado(int direccion)
	{
		int posicion = buscarPosicion(direccion);
		
		// Cogemos el TAG guardado.
		int dir = tags[posicion];
		
		// bits_dir y sumamos la posici�n.
		dir = dir << bits_dir;
		dir += posicion;
		
		// A�adimos bits de palabra y offset.
		dir = dir << bits_pal << 2;

		return dir;
	}
	
	// Extrae el tag de una direcci�n.
	private int extraerTag(int direccion)
	{
		return direccion >> 2 >> bits_pal >> bits_dir;
	}
	
	
	/*
	 *  Funciones para JTable (interfaz gr�fica).
	 */
	public String[] getColumnas()
	{
		int tama�o = 4 + palabras_linea;
		String[] columnas = new String[tama�o];
		columnas[0] = "L�nea";
		columnas[1] = "Tag";
		columnas[tama�o-2] = "V�lida";
		columnas[tama�o-1] = "Dirty";
		for (int i = 0; i < palabras_linea; i++)
			columnas[i+2] = "Palabra " + String.valueOf(i);
		
		return columnas;
	}
	
	public Object[][] getDatos()
	{
		int tama�o = 4 + palabras_linea;
		Object[][] res = new Object[entradas][tama�o];
		
		for (int i = 0; i < entradas; i++)
		{
			int direccion = i << 2 << bits_pal;
			
			Object[] linea = new Object[tama�o];
			
			linea[0] = String.format("0x%4S", Integer.toHexString(direccion)).replace(" ", "0");
			linea[1] = String.valueOf(tags[i]);
			linea[tama�o-1] = new Boolean(dirty[i]);
			linea[tama�o-2] = new Boolean(valid[i]);
			
			for (int j = 0; j < palabras_linea; j++)
				linea[j+2] = datos[i][j];

			res[(int) Math.floor(i)] = linea;
		}
		
		return res;
	}
	
	public Dimension[] getTama�os()
	{
		int tama�o = 4 + palabras_linea;
		Dimension[] dim = new Dimension[tama�o];
		
		for (int i = 0; i < tama�o-2; i++)
			dim[i] = new Dimension(Global.TAMA�O_CELDA_NORMAL, 0);
		
		dim[tama�o-1] = new Dimension(Global.TAMA�O_CELDA_BOOLEAN, Global.TAMA�O_CELDA_BOOLEAN*2);
		dim[tama�o-2] = new Dimension(Global.TAMA�O_CELDA_BOOLEAN, Global.TAMA�O_CELDA_BOOLEAN*2);
		
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

