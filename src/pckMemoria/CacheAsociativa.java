package pckMemoria;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import componentes.Tabla;

import general.Global;
import general.Global.TiposReemplazo;
import general.MemoryException;

/*
 * 1024 entradas, 8 vías, 4 palabras por línea:
 * (1024 / 8) * 4 = 128 * 4 = 512 palabras totales
 * 512 palabras * 32 bits = 2048Bytes = 2KB
 */
public class CacheAsociativa implements Cache
{
	// Cada una de las vías se implementa como una caché directa.
	// De modo que el diseño es bastante simple.
	private CacheDirecta vias[];
	
	private int entradas;
	private int palabras_linea;
	private int bytes_palabra;
	public PoliticaReemplazo politica;
	
	private Tabla interfaz;
	
	// En caché directa se recomienda usar tamaños de potencias de 2^x.
	// En caché asociativa la división entradas/vías DEBE dar exacto (no decimales).
	// También se recomienda que entradas sea potencia de 2 (y divisible entre vías).
	public CacheAsociativa(int _entradas, int _palabras_linea, int _vias, TiposReemplazo _Tpolitica) throws MemoryException
	{
		this (_entradas, _palabras_linea, _vias, _Tpolitica, 4);
	}
	
	public CacheAsociativa(int _entradas, int _palabras_linea, int _vias, TiposReemplazo _Tpolitica, int _bytes_palabra) throws MemoryException
	{
		if (_vias < 1 || _bytes_palabra < 1)
			throw new MemoryException("Error en inicialización de caché.");
		
		entradas = _entradas / _vias;
		palabras_linea = _palabras_linea;
		bytes_palabra = _bytes_palabra;
		
		if (entradas <= 0 || _entradas % _vias != 0 || _palabras_linea < 1)
			throw new MemoryException("Error en inicialización de caché.");
		
		// Creamos el array de vías
		vias = new CacheDirecta[_vias];
		
		// Creamos las vías
		for (int i = 0; i < _vias; i++)
			vias[i] = new CacheDirecta(entradas, palabras_linea, bytes_palabra);
		
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
	
	// Invalida una línea.
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
	
	// Invalida una página.
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
		
		// Nunca deberíamos llegar aquí...
		throw new MemoryException("Escritura de línea imposible en dirección 0x" + Integer.toHexString(direccion) +
				" pos " + buscarPosicion(direccion));
	}
	
	// Actualizar una línea existente.
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
	
	// Reemplaza una línea por otra. Devuelve la línea anterior.
	// Usará la política de reemplazo para determinar qué línea se elimina.
	public LineaReemplazo reemplazarLinea(int direccion, int pagina, int[] linea) throws MemoryException
	{
		int pos = buscarPosicion(direccion);
		int via = politica.elegirViaReemplazo(pos);
		
		// Reemplazamos. Devolverá null si la línea no estaba sucia.
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

	// Me determina si una dirección está libre o no.
	// Si está libre significa que puedo escribir, en caso contrario
	// tendré que reemplazar antes de escribir.
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
	
	// Busco la posición en el array (entry) del dato.
	private int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los últimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> Global.bitsDireccionar(bytes_palabra) >> Global.bitsDireccionar(palabras_linea);;
		
		// Los siguientes bits son del índice.
		// La entrada será el módulo del número de entradas.
		return (int) (pos % entradas);
	}
	
	
	/*
	 *  Funciones para JTable (interfaz gráfica).
	 */
	public String[] getColumnas()
	{
		int tamaño = 4 + palabras_linea;
		String[] columnas = new String[tamaño];
		columnas[0] = "Conjunto";
		columnas[1] = "Tag";
		columnas[tamaño-2] = "Válida";
		columnas[tamaño-1] = "Dirty";
		for (int i = 0; i < palabras_linea; i++)
			columnas[i+2] = "Palabra " + String.valueOf(i);
		
		return columnas;
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
	
	public Object[][] getDatos()
	{
		int tamaño = 4 + palabras_linea;
		Object[][] res = new Object[entradas][tamaño];
		
		// Leemos la primera columna, que es la de las direcciones (sólo una)
		Object[][] primera = vias[0].getDatos();
		for (int lin = 0; lin < entradas; lin++)
			res[lin][0] = primera[lin][0];
		
		for (int lin = 0; lin < entradas; lin++)
		{
			// Recorro palabras de cada línea
			for (int campo = 0; campo < tamaño-1; campo++)
			{
				Object[] dato;
				// Tamaño-1 es el final.
				// Tamaño-2 es dirty.
				// Tamaño-3 es valid.
				if (campo < tamaño-3)
				{
					dato = new String[vias.length];
					// Recorro cada una de las cachés para montar el array de cada campo.
					for (int via = 0; via < vias.length; via++)
						dato[via] = String.valueOf(vias[via].getDato(lin, campo));
				}
				else
				{
					dato = new Boolean[vias.length];
					// Recorro cada una de las cachés para montar el array de cada campo.
					for (int via = 0; via < vias.length; via++)
						dato[via] = Boolean.valueOf(String.valueOf(vias[via].getDato(lin, campo)));
				}
				res[lin][campo+1] = dato;
			}
		}
		
		return res;
	}
	
	// Actualiza un dato en la interfaz gráfica.
	private void actualizarDatoInterfaz(int dato, int via, int pos, int pal)
	{
		int tamaño = 4 + palabras_linea;
		// Modificamos el tag.
		Object A_tag = interfaz.getValueAt(pos, 1);
		if (A_tag.getClass().isArray())
		{
			// Cada posición es una vía.
			String[] actual = (String[])A_tag;
			actual[via] = String.valueOf(vias[via].getTagGuardado(pos));
			interfaz.setValueAt(actual, pos, 1);
		}
		
		// Modificamos un dato.
		Object A_dato = interfaz.getValueAt(pos, pal+2);
		if (A_dato.getClass().isArray())
		{
			// Cada posición es una vía.
			String[] actual = (String[])A_dato;
			actual[via] = String.valueOf(dato);
			interfaz.setValueAt(actual, pos, pal+2);
		}
		
		// Modificamos el estado de valid.
		Object A_valid = interfaz.getValueAt(pos, tamaño-2);
		if (A_valid.getClass().isArray())
		{
			// Cada posición es una vía.
			Boolean[] actual = (Boolean[])A_valid;
			actual[via] = new Boolean(true);
			interfaz.setValueAt(actual, pos, tamaño-2);
		}
		
		// Modificamos el estado de dirty.
		Object A_dirty = interfaz.getValueAt(pos, tamaño-1);
		if (A_dirty.getClass().isArray())
		{
			// Cada posición es una vía.
			Boolean[] actual = (Boolean[])A_dirty;
			actual[via] = new Boolean(true);
			interfaz.setValueAt(actual, pos, tamaño-1);
		}
	}
	
	// Actualizar una línea en la interfaz.
	private void actualizarLineaInterfaz(int[] linea, int via, int pos)
	{
		int tamaño = 4 + palabras_linea;
		// Modificamos el tag.
		Object A_tag = interfaz.getValueAt(pos, 1);
		if (A_tag.getClass().isArray())
		{
			// Cada posición es una vía.
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
				// Cada posición es una vía.
				String[] actual = (String[])A_dato;
				actual[via] = String.valueOf(linea[pal]);
				interfaz.setValueAt(actual, pos, pal+2);
			}
		}
		
		// Modificamos el estado de valid.
		Object A_valid = interfaz.getValueAt(pos, tamaño-2);
		if (A_valid.getClass().isArray())
		{
			// Cada posición es una vía.
			Boolean[] actual = (Boolean[])A_valid;
			actual[via] = new Boolean(true);
			interfaz.setValueAt(actual, pos, tamaño-2);
		}
		
		// Modificamos el estado de dirty.
		Object A_dirty = interfaz.getValueAt(pos, tamaño-1);
		if (A_dirty.getClass().isArray())
		{
			// Cada posición es una vía.
			Boolean[] actual = (Boolean[])A_dirty;
			actual[via] = new Boolean(false);
			interfaz.setValueAt(actual, pos, tamaño-1);
		}
	}
	
	private void invalidarLineaInterfaz(int via, int pos)
	{
		int tamaño = 4 + palabras_linea;
		// Modificamos el estado de valid.
		Object A_valid = interfaz.getValueAt(pos, tamaño-2);
		if (A_valid.getClass().isArray())
		{
			// Cada posición es una vía.
			Boolean[] actual = (Boolean[])A_valid;
			actual[via] = new Boolean(false);
			interfaz.setValueAt(actual, pos, tamaño-2);
		}
		
		// Modificamos el estado de dirty.
		Object A_dirty = interfaz.getValueAt(pos, tamaño-1);
		if (A_dirty.getClass().isArray())
		{
			// Cada posición es una vía.
			Boolean[] actual = (Boolean[])A_dirty;
			actual[via] = new Boolean(false);
			interfaz.setValueAt(actual, pos, tamaño-1);
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

