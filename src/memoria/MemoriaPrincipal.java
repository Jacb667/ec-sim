
package memoria;

import java.awt.Dimension;


import general.Global;
import gui.Tabla;

/* 
 * La memoria principal se compone de Páginas, con una capacidad de las páginas que caben en los marcos de la Tabla de Paginación.
 * Para acceder a un dato de la memoria, se buscará la página a la que pertenece.
 * Si la página ya está en un marco, se guarda directamente en la página correspondiente.
 * Si no está en un marco, será necesario traer la página a un marco cualquiera, siendo necesario reemplazar otra página si están todos ocupados.
 * En caso de eliminar una página de un marco, deberán eliminarse de las memorias Caché todas las entradas pertenecientes a esa página.
 */

public class MemoriaPrincipal
{
	private TablaPaginas tablaPags;
	private Tabla interfaz;
	private int entradas;
	private int bytes_palabra;
	
	// De momento no se usa. En un futuro podría usarse para controlar las peticiones de líneas.
	//private int palabras_linea;
	public MemoriaPrincipal(TablaPaginas tp)
	{
		this (tp, 4);
	}

	public MemoriaPrincipal(TablaPaginas tp, int _bytes_palabra)
	{
		tablaPags = tp;
		entradas = tablaPags.getMarcos().length * tablaPags.getEntradasPagina();
		bytes_palabra = _bytes_palabra;
	}
	
	// Selecciona una página a partir de la dirección física recibida.
	private Pagina seleccionarPagina(int direccion)
	{
		// Aquí no podemos usar seleccionarPagina, ya que sólo tenemos la dirección física.
		int marco = tablaPags.seleccionarMarco(direccion);
		Pagina pag = tablaPags.getMarcos()[marco];
		return pag;
	}
	
	// Comprueba si existe una dirección en memoria principal.
	public boolean existeDato(int direccion)
	{
		int marco = tablaPags.seleccionarMarco(direccion);
		if (marco >= tablaPags.getMarcos().length)
			return false;
		
		return true;
	}
	
	// Me envían la dirección física.
	public int leerDato(int direccion)
	{
		Pagina pag = seleccionarPagina(direccion);
		return pag.leerDato(direccion);
	}
	
	// Me envían la dirección física, elimino los 2 últimos bits y guardo la posición.
	public void guardarDato(int direccion, int dato)
	{
		Pagina pag = seleccionarPagina(direccion);
		pag.guardarDato(direccion, dato);
		
		// Actualizar interfaz gráfica.
		actualizarPaginaInterfaz(pag.getMarco());
	}
	
	// Dirección de inicio de un marco.
	private int getInicioMarco(int marco)
	{
		// Si los marcos son de 5 entradas (por ejemplo):
		// Marco 0 -> 0
		// Marco 1 -> 5
		// Marco 2 -> 10
		// etc...
		return marco * tablaPags.getEntradasPagina();
	}
	
	// Lee varias posiciones (tam_linea) a partir de una dirección
	// Se usa para enviar una línea completa a caché
	public int[] leerLinea(int direccion, int tam_linea)
	{
		Pagina pag = seleccionarPagina(direccion);
		return pag.leerLinea(direccion, tam_linea);
	}
	
	public void guardarLinea(int direccion, int[] linea) 
	{
		Pagina pag = seleccionarPagina(direccion);
		pag.guardarLinea(direccion, linea);
			
		// Actualizar interfaz gráfica.
		actualizarPaginaInterfaz(pag.getMarco());
	}
	
	// Temporal: De momento sale una lista con Dirección (hex) : Dato (dec)
	// Faltaría poner una opción para que muestre los datos en otros formatos (dec, bin, oct, hex).
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		for (Pagina pag : tablaPags.getMarcos())
		{
			if (pag != null)
			{
				strB.append("[" + pag.getMarco() + "]" + "Página " + pag.getId() + "\n");
				strB.append(pag);
			}
		}
		
		return strB.toString();
	}

	public boolean estaLibre(int direccion)
	{
		Pagina pag = seleccionarPagina(direccion);
		return pag.estaLibre(direccion);
	}
	
	
	/*
	 *  Funciones para JTable (interfaz gráfica).
	 */
	public String[] getColumnas()
	{
		return new String[]{"ID Página", "Dirección Real", "Dirección Virtual", "Dato"/*, "Válida"*/};
	}
	
	public Dimension[] getTamaños()
	{
		Dimension[] dim = new Dimension[4];
		
		dim[0] = new Dimension(Global.TAMAÑO_CELDA_NORMAL,Global.TAMAÑO_CELDA_NORMAL);
		dim[1] = new Dimension(Global.TAMAÑO_CELDA_NORMAL,0);
		dim[2] = new Dimension(Global.TAMAÑO_CELDA_NORMAL,0);
		dim[3] = new Dimension(Global.TAMAÑO_CELDA_NORMAL,0);
		//dim[4] = new Dimension(Global.TAMAÑO_CELDA_BOOLEAN, Global.TAMAÑO_CELDA_BOOLEAN*2);
		
		return dim;
	}
	
	public Object[][] getDatos()
	{
		Object[][] datos = new Object[entradas][4];
		
		int entrada = 0;
		for (int pag = 0; pag < tablaPags.getMarcos().length; pag++)
		{
			// Creo una página "vacía".
			for (int i = 0; i < tablaPags.getEntradasPagina(); i++)
			{
				// Página, dirección, dato, valido
				Object[] linea = {"", "", "", ""/*, new Boolean(false)*/};
			
				datos[entrada] = linea;
				entrada++;
			}
		}
		
		return datos;
	}
	
	public void actualizarPaginaInterfaz(int marco)
	{
		if (interfaz != null)
		{
			Pagina pag = tablaPags.getMarcos()[marco];
			int posicion_inicio = getInicioMarco(marco);
			if (pag != null)
			{
				Object[][] datos_pag = pag.getDatos();
				// Recorro la página, añadiendo los datos a nuestro Array.
				for (int i = 0; i < datos_pag.length; i++)
				{
					int pos = posicion_inicio + i;
					int direccion_r = getDireccionFisica(pos << Global.bitsDireccionar(bytes_palabra), pag.getMarco());
					int direccion_v = getDireccionVirtual(pos << Global.bitsDireccionar(bytes_palabra), pag.getId());

					interfaz.setValueAt(String.valueOf(pag.getId()), pos, 0);  // Página
					interfaz.setValueAt(String.format("0x%4S", Integer.toHexString(direccion_r)).replace(" ", "0"), pos, 1);  // Dirección
					interfaz.setValueAt(String.format("0x%4S", Integer.toHexString(direccion_v)).replace(" ", "0"), pos, 2);  // Virtual
					interfaz.setValueAt(String.valueOf(datos_pag[i][1]), pos, 3);  // Dato
					//interfaz.setValueAt(new Boolean(Boolean.valueOf(String.valueOf(datos_pag[i][2]))), pos, 4);  // Válido
				}
			}
		}
	}
	
	private int getDireccionVirtual(int direccion, int id)
	{
		int offset = (int) Math.floor(direccion % tablaPags.getTamañoPagina());
		int res = (id << Global.bitsDireccionar(tablaPags.getTamañoPagina())) + offset;
		return res;
	}
	
	private int getDireccionFisica(int direccion, int marco)
	{
		int offset = (int) Math.floor(direccion % tablaPags.getTamañoPagina());
		int res = (marco << Global.bitsDireccionar(tablaPags.getTamañoPagina())) + offset;
		return res;
	}
	
	public Tabla getInterfaz()
	{
		return interfaz;
	}
	
	public void setInterfaz(Tabla intf)
	{
		interfaz = intf;
	}
}
