package pckMemoria;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import componentes.Tabla;

import general.Global;
import general.Global.TiposReemplazo;
import general.MemoryException;

/*
 * 1024 entradas, 8 v�as, 4 palabras por l�nea:
 * (1024 / 8) * 4 = 128 * 4 = 512 palabras totales
 * 512 palabras * 32 bits = 2048Bytes = 2KB
 */
public class CacheAsociativa implements Cache
{
	// Cada una de las v�as se implementa como una cach� directa.
	// De modo que el dise�o es bastante simple.
	private CacheDirecta vias[];
	
	private int entradas;
	private int palabras_linea;
	private int bytes_palabra;
	public PoliticaReemplazo politica;
	
	private Tabla interfaz;
	
	// En cach� directa se recomienda usar tama�os de potencias de 2^x.
	// En cach� asociativa la divisi�n entradas/v�as DEBE dar exacto (no decimales).
	// Tambi�n se recomienda que entradas sea potencia de 2 (y divisible entre v�as).
	public CacheAsociativa(int _entradas, int _palabras_linea, int _vias, TiposReemplazo _Tpolitica) throws MemoryException
	{
		this (_entradas, _palabras_linea, _vias, _Tpolitica, 4);
	}
	
	public CacheAsociativa(int _entradas, int _palabras_linea, int _vias, TiposReemplazo _Tpolitica, int _bytes_palabra) throws MemoryException
	{
		if (_vias < 1 || _bytes_palabra < 1)
			throw new MemoryException("Error en inicializaci�n de cach�.");
		
		entradas = _entradas / _vias;
		palabras_linea = _palabras_linea;
		bytes_palabra = _bytes_palabra;
		
		if (entradas <= 0 || _entradas % _vias != 0 || _palabras_linea < 1)
			throw new MemoryException("Error en inicializaci�n de cach�.");
		
		// Creamos el array de v�as
		vias = new CacheDirecta[_vias];
		
		// Creamos las v�as
		for (int i = 0; i < _vias; i++)
			vias[i] = new CacheDirecta(entradas, palabras_linea, bytes_palabra);
		
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
	
	// Invalida una l�nea.
	public void invalidarLinea(int direccion)
	{
		for (int via = 0; via < vias.length; via++)
		{
			if (vias[via].existeDato(direccion))
			{
				int pos = buscarPosicion(direccion);
				vias[via].invalidarLinea(direccion);
				
				if (interfaz != null)
					invalidarLineaInterfaz(via, pos);
				
				return;
			}
		}
	}
	
	// Invalida una p�gina.
	public List<LineaReemplazo> invalidarPagina(int pagina_id)
	{
		List<LineaReemplazo> linRes = new ArrayList<LineaReemplazo>();
		
		List<LineaReemplazo> eliminadas;
		for (int via = 0; via < vias.length; via++)
		{
			eliminadas = vias[via].invalidarPagina(pagina_id);
			linRes.addAll(eliminadas);
			
			// Actualizar interfaz.
			if (interfaz != null)
			{
				for (LineaReemplazo linR : eliminadas)
					invalidarLineaInterfaz(via, buscarPosicion(linR.getDireccion()));
			}
		}
		
		return linRes;
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
	public void modificarDato(int direccion, int pagina, int dato) throws MemoryException
	{
		for (int via = 0; via < vias.length; via++)
		{
			if (vias[via].existeDato(direccion))
			{
				int pos = buscarPosicion(direccion);
				int pal = vias[via].posicionPalabra(direccion);
				politica.accesoLinea(pos, via);
				vias[via].modificarDato(direccion, pagina, dato);
				
				if (interfaz != null)
					actualizarDatoInterfaz(dato, via, pos, pal);
				
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
	public void escribirLinea(int direccion, int pagina, int[] linea) throws MemoryException
	{
		for (int via = 0; via < vias.length; via++)
		{
			if (vias[via].lineaLibre(direccion))
			{
				int pos = buscarPosicion(direccion);
				vias[via].escribirLinea(direccion, pagina, linea);
				politica.nuevaLinea(pos, via);
				
				if (interfaz != null)
					actualizarLineaInterfaz(linea, via, pos);
				
				return;
			}
		}
		
		// Nunca deber�amos llegar aqu�...
		throw new MemoryException("Escritura de l�nea imposible en direcci�n 0x" + Integer.toHexString(direccion) +
				" pos " + buscarPosicion(direccion));
	}
	
	// Actualizar una l�nea existente.
	public void actualizarLinea(int direccion, int pagina, int[] linea)
	{
		for (int via = 0; via < vias.length; via++)
		{
			if (vias[via].existeDato(direccion))
			{
				int pos = buscarPosicion(direccion);
				politica.accesoLinea(pos, via);
				vias[via].escribirLinea(direccion, pagina, linea);
				
				if (interfaz != null)
					actualizarLineaInterfaz(linea, via, pos);
				
				return;
			}
		}
	}
	
	// Reemplaza una l�nea por otra. Devuelve la l�nea anterior.
	// Usar� la pol�tica de reemplazo para determinar qu� l�nea se elimina.
	public LineaReemplazo reemplazarLinea(int direccion, int pagina, int[] linea) throws MemoryException
	{
		int pos = buscarPosicion(direccion);
		int via = politica.elegirViaReemplazo(pos);
		
		// Reemplazamos. Devolver� null si la l�nea no estaba sucia.
		LineaReemplazo res = vias[via].reemplazarLinea(direccion, pagina, linea);
		
		politica.nuevaLinea(buscarPosicion(direccion), via);
		
		if (interfaz != null)
			actualizarLineaInterfaz(linea, via, pos);
		
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
		{
			res = vias[i].lineaLibre(direccion);
			i++;
		}

		return res;
	}
	
	// Busco la posici�n en el array (entry) del dato.
	private int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los �ltimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> Global.bitsDireccionar(bytes_palabra) >> Global.bitsDireccionar(palabras_linea);;
		
		// Los siguientes bits son del �ndice.
		// La entrada ser� el m�dulo del n�mero de entradas.
		return (int) (pos % entradas);
	}
	
	
	/*
	 *  Funciones para JTable (interfaz gr�fica).
	 */
	public String[] getColumnas()
	{
		int tama�o = 4 + palabras_linea;
		String[] columnas = new String[tama�o];
		columnas[0] = "Conjunto";
		columnas[1] = "Tag";
		columnas[tama�o-2] = "V�lida";
		columnas[tama�o-1] = "Dirty";
		for (int i = 0; i < palabras_linea; i++)
			columnas[i+2] = "Palabra " + String.valueOf(i);
		
		return columnas;
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
	
	public Object[][] getDatos()
	{
		int tama�o = 4 + palabras_linea;
		Object[][] res = new Object[entradas][tama�o];
		
		// Leemos la primera columna, que es la de las direcciones (s�lo una)
		Object[][] primera = vias[0].getDatos();
		for (int lin = 0; lin < entradas; lin++)
			res[lin][0] = primera[lin][0];
		
		for (int lin = 0; lin < entradas; lin++)
		{
			// Recorro palabras de cada l�nea
			for (int campo = 0; campo < tama�o-1; campo++)
			{
				Object[] dato;
				// Tama�o-1 es el final.
				// Tama�o-2 es dirty.
				// Tama�o-3 es valid.
				if (campo < tama�o-3)
				{
					dato = new String[vias.length];
					// Recorro cada una de las cach�s para montar el array de cada campo.
					for (int via = 0; via < vias.length; via++)
						dato[via] = String.valueOf(vias[via].getDato(lin, campo));
				}
				else
				{
					dato = new Boolean[vias.length];
					// Recorro cada una de las cach�s para montar el array de cada campo.
					for (int via = 0; via < vias.length; via++)
						dato[via] = Boolean.valueOf(String.valueOf(vias[via].getDato(lin, campo)));
				}
				res[lin][campo+1] = dato;
			}
		}
		
		return res;
	}
	
	// Actualiza un dato en la interfaz gr�fica.
	private void actualizarDatoInterfaz(int dato, int via, int pos, int pal)
	{
		int tama�o = 4 + palabras_linea;
		// Modificamos el tag.
		Object A_tag = interfaz.getValueAt(pos, 1);
		if (A_tag.getClass().isArray())
		{
			// Cada posici�n es una v�a.
			String[] actual = (String[])A_tag;
			actual[via] = String.valueOf(vias[via].getTagGuardado(pos));
			interfaz.setValueAt(actual, pos, 1);
		}
		
		// Modificamos un dato.
		Object A_dato = interfaz.getValueAt(pos, pal+2);
		if (A_dato.getClass().isArray())
		{
			// Cada posici�n es una v�a.
			String[] actual = (String[])A_dato;
			actual[via] = String.valueOf(dato);
			interfaz.setValueAt(actual, pos, pal+2);
		}
		
		// Modificamos el estado de valid.
		Object A_valid = interfaz.getValueAt(pos, tama�o-2);
		if (A_valid.getClass().isArray())
		{
			// Cada posici�n es una v�a.
			Boolean[] actual = (Boolean[])A_valid;
			actual[via] = new Boolean(true);
			interfaz.setValueAt(actual, pos, tama�o-2);
		}
		
		// Modificamos el estado de dirty.
		Object A_dirty = interfaz.getValueAt(pos, tama�o-1);
		if (A_dirty.getClass().isArray())
		{
			// Cada posici�n es una v�a.
			Boolean[] actual = (Boolean[])A_dirty;
			actual[via] = new Boolean(true);
			interfaz.setValueAt(actual, pos, tama�o-1);
		}
	}
	
	// Actualizar una l�nea en la interfaz.
	private void actualizarLineaInterfaz(int[] linea, int via, int pos)
	{
		int tama�o = 4 + palabras_linea;
		// Modificamos el tag.
		Object A_tag = interfaz.getValueAt(pos, 1);
		if (A_tag.getClass().isArray())
		{
			// Cada posici�n es una v�a.
			String[] actual = (String[])A_tag;
			actual[via] = String.valueOf(vias[via].getTagGuardado(pos));
			interfaz.setValueAt(actual, pos, 1);
		}

		// Modificamos los datos
		for (int pal = 0; pal < linea.length; pal++)
		{
			Object A_dato = interfaz.getValueAt(pos, pal+2);
			if (A_dato.getClass().isArray())
			{
				// Cada posici�n es una v�a.
				String[] actual = (String[])A_dato;
				actual[via] = String.valueOf(linea[pal]);
				interfaz.setValueAt(actual, pos, pal+2);
			}
		}
		
		// Modificamos el estado de valid.
		Object A_valid = interfaz.getValueAt(pos, tama�o-2);
		if (A_valid.getClass().isArray())
		{
			// Cada posici�n es una v�a.
			Boolean[] actual = (Boolean[])A_valid;
			actual[via] = new Boolean(true);
			interfaz.setValueAt(actual, pos, tama�o-2);
		}
		
		// Modificamos el estado de dirty.
		Object A_dirty = interfaz.getValueAt(pos, tama�o-1);
		if (A_dirty.getClass().isArray())
		{
			// Cada posici�n es una v�a.
			Boolean[] actual = (Boolean[])A_dirty;
			actual[via] = new Boolean(false);
			interfaz.setValueAt(actual, pos, tama�o-1);
		}
	}
	
	private void invalidarLineaInterfaz(int via, int pos)
	{
		int tama�o = 4 + palabras_linea;
		// Modificamos el estado de valid.
		Object A_valid = interfaz.getValueAt(pos, tama�o-2);
		if (A_valid.getClass().isArray())
		{
			// Cada posici�n es una v�a.
			Boolean[] actual = (Boolean[])A_valid;
			actual[via] = new Boolean(false);
			interfaz.setValueAt(actual, pos, tama�o-2);
		}
		
		// Modificamos el estado de dirty.
		Object A_dirty = interfaz.getValueAt(pos, tama�o-1);
		if (A_dirty.getClass().isArray())
		{
			// Cada posici�n es una v�a.
			Boolean[] actual = (Boolean[])A_dirty;
			actual[via] = new Boolean(false);
			interfaz.setValueAt(actual, pos, tama�o-1);
		}
	}

	public Tabla getInterfaz()
	{
		return interfaz;
	}

	public void setInterfaz(Tabla interfaz)
	{
		this.interfaz = interfaz;
		this.interfaz.setRenderTablaEnCelda();
	}
}

