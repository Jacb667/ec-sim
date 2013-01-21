package pckMemoria;

import general.Global;
import general.MemoryException;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import componentes.Tabla;

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
	private int bytes_palabra;

	private int bits_dir;
	private int bits_pal;
	private int bits_byte;

	private int[] tags;
	private int[] paginas;  // Al igual que el TAG, guardamos la p�gina correspondiente.
	private boolean[] valid;
	private boolean[] dirty;
	private int[/*lineas*/][/*palabras*/] datos;
	
	private Tabla interfaz;
	
	public CacheDirecta(int _entradas, int _palabras_linea) throws MemoryException
	{
		this (_entradas, _palabras_linea, 4);
	}

	public CacheDirecta(int _entradas, int _palabras_linea, int _bytes_palabra) throws MemoryException
	{
		if (_entradas < 1 || _palabras_linea < 1 || _bytes_palabra < 1)
			throw new MemoryException("Error en inicializaci�n de cach�.");
		
		palabras_linea = _palabras_linea;
		entradas = _entradas;
		bytes_palabra = _bytes_palabra;
		
		// Bits byte (b).
		bits_byte = Global.bitsDireccionar(bytes_palabra);
		
		// Seleccionar palabra (w).
		bits_pal = Global.bitsDireccionar(palabras_linea);

		// Direccionar entradas (c).
		bits_dir = Global.bitsDireccionar(entradas);
		
		tags = new int[entradas];
		paginas = new int[entradas];
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
	
	// Invalida una l�nea.
	public void invalidarLinea(int direccion)
	{
		int pos = buscarPosicion(direccion);
		
		valid[pos] = false;
		dirty[pos] = false;
		
		// Actualizar interfaz gr�fica.
		if (interfaz != null)
		{
			int tama�o = 4 + palabras_linea;
			interfaz.setValueAt(false, pos, tama�o-2);  // Valid
			interfaz.setValueAt(false, pos, tama�o-1);  // Dirty
		}
	}
	
	// Invalidar p�gina. Devuelve una lista con las entradas eliminadas.
	public List<LineaReemplazo> invalidarPagina(int pagina_id)
	{
		List<LineaReemplazo> eliminadas = new ArrayList<LineaReemplazo>();
		for (int pos = 0; pos < datos.length; pos++)
		{
			if (paginas[pos] == pagina_id)
			{
				if (dirty[pos])
				{
					LineaReemplazo LinR = new LineaReemplazo(getDireccionGuardadaEntrada(pos), paginas[pos], datos[pos]);
					eliminadas.add(LinR);
				}
				
				valid[pos] = false;
				dirty[pos] = false;
				
				// Actualizar interfaz gr�fica.
				if (interfaz != null)
				{
					int tama�o = 4 + palabras_linea;
					interfaz.setValueAt(false, pos, tama�o-2);  // Valid
					interfaz.setValueAt(false, pos, tama�o-1);  // Dirty
				}
			}
		}
		
		return eliminadas;
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
	public void modificarDato(int direccion, int pagina, int dato)
	{
		int pos = buscarPosicion(direccion);
		int pal = posicionPalabra(direccion);
		int tag = extraerTag(direccion);
		
		tags[pos] = tag;
		datos[pos][pal] = dato;
		valid[pos] = true;
		dirty[pos] = true;
		paginas[pos] = pagina;
		
		// Actualizar interfaz gr�fica.
		if (interfaz != null)
		{
			int tama�o = 4 + palabras_linea;
			interfaz.setValueAt(String.valueOf(tag), pos, 1);  // Tag
			interfaz.setValueAt(String.valueOf(dato), pos, pal+2);  // Palabra
			interfaz.setValueAt(true, pos, tama�o-2);  // Valid
			interfaz.setValueAt(true, pos, tama�o-1);  // Dirty
		}
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
	public void escribirLinea(int direccion, int pagina, int[] linea)
	{
		int direccion_inicio = buscarPosicion(direccion);
		int tag = extraerTag(direccion);
		
		tags[direccion_inicio] = tag;
		for (int i = 0; i < palabras_linea; i++)
			datos[direccion_inicio][i] = linea[i];
		
		dirty[direccion_inicio] = false;
		valid[direccion_inicio] = true;
		paginas[direccion_inicio] = pagina;
		
		// Actualizar interfaz gr�fica.
		if (interfaz != null)
		{
			int tama�o = 4 + palabras_linea;
			for (int i = 0; i < palabras_linea; i++)
				interfaz.setValueAt(String.valueOf(linea[i]), direccion_inicio, i+2);  // Palabra
			
			interfaz.setValueAt(String.valueOf(tag), direccion_inicio, 1);  // Tag
			interfaz.setValueAt(true, direccion_inicio, tama�o-2);  // Valid
			interfaz.setValueAt(false, direccion_inicio, tama�o-1);  // Dirty
		}
	}
	
	// Actualiza una l�nea existente.
	public void actualizarLinea(int direccion, int pagina, int[] linea)
	{
		escribirLinea(direccion, pagina, linea);
	}
	
	// Reemplaza una l�nea en la cach� por otra.
	// Si la l�nea estaba "sucia", la devuelve para poder enviarla a otro nivel.
	public LineaReemplazo reemplazarLinea(int direccion, int pagina, int[] linea)
	{
		LineaReemplazo res = null;
		
		// La devolvemos solamente si est� "sucia".
		if (lineaDirty(direccion))
			res = new LineaReemplazo(getDireccionGuardada(direccion), paginas[buscarPosicion(direccion)], leerLinea(direccion));
		
		escribirLinea(direccion, pagina, linea);
		
		return res;
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < datos.length; i++)
		{
			strB.append(String.format("0x%3S", Integer.toHexString(i << bits_byte << bits_pal)).replace(" ", "0"));
			strB.append(" ("+paginas[i]+")");
			strB.append(" -> ").append(Integer.toHexString(tags[i])).append(" : ").append(Arrays.toString(datos[i]));
			strB.append(" ").append(valid[i]).append(" ").append(dirty[i]);
			strB.append("\n");
		}
		
		return strB.toString();
	}
	
	// Busco la posici�n de la palabra en la l�nea
	public int posicionPalabra(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		int pos = direccion >> bits_byte;
		
		// Los siguientes bits son de la palabra.
		// La entrada ser� el m�dulo de palabras po l�nea.
		return (int) (pos % palabras_linea);
	}

	// Busco la posici�n en el array (entry) del dato.
	private int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los �ltimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> bits_byte >> bits_pal;
		
		// Los siguientes bits son del �ndice.
		// La entrada ser� el m�dulo del n�mero de entradas.
		return (int) (pos % entradas);
	}
	
	// Me devuelve la direcci�n de la l�nea guardada donde ir�a direcci�n.
	private int getDireccionGuardada(int direccion)
	{
		int posicion = buscarPosicion(direccion);
		
		// Cogemos el TAG guardado.
		int dir = tags[posicion];
		
		// bits_dir y sumamos la posici�n.
		dir = dir << bits_dir;
		dir += posicion;
		
		// A�adimos bits de palabra y offset.
		dir = dir << bits_pal << bits_byte;

		return dir;
	}
	
	private int getDireccionGuardadaEntrada(int posicion)
	{
		// Cogemos el TAG guardado.
		int dir = tags[posicion];
		
		// bits_dir y sumamos la posici�n.
		dir = dir << bits_dir;
		dir += posicion;
		
		// A�adimos bits de palabra y offset.
		dir = dir << bits_pal << bits_byte;
		
		return dir;
	}
	
	public int getTagGuardado(int posicion)
	{
		return tags[posicion];
	}
	
	// Extrae el tag de una direcci�n.
	private int extraerTag(int direccion)
	{
		return direccion >> bits_byte >> bits_pal >> bits_dir;
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
			columnas[i+2] = "Palabra " + i;
		
		return columnas;
	}
	
	public Object[][] getDatos()
	{
		int tama�o = 4 + palabras_linea;
		Object[][] res = new Object[entradas][tama�o];
		
		for (int i = 0; i < entradas; i++)
		{
			int direccion = i << bits_byte << bits_pal;
			
			Object[] linea = new Object[tama�o];
			
			linea[0] = String.format("0x%4S", Integer.toHexString(direccion)).replace(" ", "0");
			linea[1] = String.valueOf(tags[i]);
			linea[tama�o-1] = new Boolean(dirty[i]);
			linea[tama�o-2] = new Boolean(valid[i]);
			
			for (int j = 0; j < palabras_linea; j++)
				linea[j+2] = String.valueOf(datos[i][j]);

			res[i] = linea;
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
			return String.valueOf(tags[linea]);
		else if (posicion > 0 && posicion <= palabras_linea)  // Palabra
			return String.valueOf(datos[linea][posicion-1]);
		else if (posicion == palabras_linea+1)  // Valid
			return new Boolean(valid[linea]);
		else
			return new Boolean(dirty[linea]);
	}

	public Tabla getInterfaz()
	{
		return interfaz;
	}

	public void setInterfaz(Tabla interfaz)
	{
		this.interfaz = interfaz;
	}
}

