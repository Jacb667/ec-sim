
package memoria;

import java.awt.Dimension;


import general.Global;
import gui.Tabla;

/* 
 * La memoria principal se compone de P�ginas, con una capacidad de las p�ginas que caben en los marcos de la Tabla de Paginaci�n.
 * Para acceder a un dato de la memoria, se buscar� la p�gina a la que pertenece.
 * Si la p�gina ya est� en un marco, se guarda directamente en la p�gina correspondiente.
 * Si no est� en un marco, ser� necesario traer la p�gina a un marco cualquiera, siendo necesario reemplazar otra p�gina si est�n todos ocupados.
 * En caso de eliminar una p�gina de un marco, deber�n eliminarse de las memorias Cach� todas las entradas pertenecientes a esa p�gina.
 */

public class MemoriaPrincipal
{
	private TablaPaginas tablaPags;
	private Tabla interfaz;
	private int entradas;
	private int bytes_palabra;
	
	// De momento no se usa. En un futuro podr�a usarse para controlar las peticiones de l�neas.
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
	
	// Selecciona una p�gina a partir de la direcci�n f�sica recibida.
	private Pagina seleccionarPagina(int direccion)
	{
		// Aqu� no podemos usar seleccionarPagina, ya que s�lo tenemos la direcci�n f�sica.
		int marco = tablaPags.seleccionarMarco(direccion);
		Pagina pag = tablaPags.getMarcos()[marco];
		return pag;
	}
	
	// Comprueba si existe una direcci�n en memoria principal.
	public boolean existeDato(int direccion)
	{
		int marco = tablaPags.seleccionarMarco(direccion);
		if (marco >= tablaPags.getMarcos().length)
			return false;
		
		return true;
	}
	
	// Me env�an la direcci�n f�sica.
	public int leerDato(int direccion)
	{
		Pagina pag = seleccionarPagina(direccion);
		return pag.leerDato(direccion);
	}
	
	// Me env�an la direcci�n f�sica, elimino los 2 �ltimos bits y guardo la posici�n.
	public void guardarDato(int direccion, int dato)
	{
		Pagina pag = seleccionarPagina(direccion);
		pag.guardarDato(direccion, dato);
		
		// Actualizar interfaz gr�fica.
		actualizarPaginaInterfaz(pag.getMarco());
	}
	
	// Direcci�n de inicio de un marco.
	private int getInicioMarco(int marco)
	{
		// Si los marcos son de 5 entradas (por ejemplo):
		// Marco 0 -> 0
		// Marco 1 -> 5
		// Marco 2 -> 10
		// etc...
		return marco * tablaPags.getEntradasPagina();
	}
	
	// Lee varias posiciones (tam_linea) a partir de una direcci�n
	// Se usa para enviar una l�nea completa a cach�
	public int[] leerLinea(int direccion, int tam_linea)
	{
		Pagina pag = seleccionarPagina(direccion);
		return pag.leerLinea(direccion, tam_linea);
	}
	
	public void guardarLinea(int direccion, int[] linea) 
	{
		Pagina pag = seleccionarPagina(direccion);
		pag.guardarLinea(direccion, linea);
			
		// Actualizar interfaz gr�fica.
		actualizarPaginaInterfaz(pag.getMarco());
	}
	
	// Temporal: De momento sale una lista con Direcci�n (hex) : Dato (dec)
	// Faltar�a poner una opci�n para que muestre los datos en otros formatos (dec, bin, oct, hex).
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		for (Pagina pag : tablaPags.getMarcos())
		{
			if (pag != null)
			{
				strB.append("[" + pag.getMarco() + "]" + "P�gina " + pag.getId() + "\n");
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
	 *  Funciones para JTable (interfaz gr�fica).
	 */
	public String[] getColumnas()
	{
		return new String[]{"ID P�gina", "Direcci�n Real", "Direcci�n Virtual", "Dato"/*, "V�lida"*/};
	}
	
	public Dimension[] getTama�os()
	{
		Dimension[] dim = new Dimension[4];
		
		dim[0] = new Dimension(Global.TAMA�O_CELDA_NORMAL,Global.TAMA�O_CELDA_NORMAL);
		dim[1] = new Dimension(Global.TAMA�O_CELDA_NORMAL,0);
		dim[2] = new Dimension(Global.TAMA�O_CELDA_NORMAL,0);
		dim[3] = new Dimension(Global.TAMA�O_CELDA_NORMAL,0);
		//dim[4] = new Dimension(Global.TAMA�O_CELDA_BOOLEAN, Global.TAMA�O_CELDA_BOOLEAN*2);
		
		return dim;
	}
	
	public Object[][] getDatos()
	{
		Object[][] datos = new Object[entradas][4];
		
		int entrada = 0;
		for (int pag = 0; pag < tablaPags.getMarcos().length; pag++)
		{
			// Creo una p�gina "vac�a".
			for (int i = 0; i < tablaPags.getEntradasPagina(); i++)
			{
				// P�gina, direcci�n, dato, valido
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
				// Recorro la p�gina, a�adiendo los datos a nuestro Array.
				for (int i = 0; i < datos_pag.length; i++)
				{
					int pos = posicion_inicio + i;
					int direccion_r = getDireccionFisica(pos << Global.bitsDireccionar(bytes_palabra), pag.getMarco());
					int direccion_v = getDireccionVirtual(pos << Global.bitsDireccionar(bytes_palabra), pag.getId());

					interfaz.setValueAt(String.valueOf(pag.getId()), pos, 0);  // P�gina
					interfaz.setValueAt(String.format("0x%4S", Integer.toHexString(direccion_r)).replace(" ", "0"), pos, 1);  // Direcci�n
					interfaz.setValueAt(String.format("0x%4S", Integer.toHexString(direccion_v)).replace(" ", "0"), pos, 2);  // Virtual
					interfaz.setValueAt(String.valueOf(datos_pag[i][1]), pos, 3);  // Dato
					//interfaz.setValueAt(new Boolean(Boolean.valueOf(String.valueOf(datos_pag[i][2]))), pos, 4);  // V�lido
				}
			}
		}
	}
	
	private int getDireccionVirtual(int direccion, int id)
	{
		int offset = (int) Math.floor(direccion % tablaPags.getTama�oPagina());
		int res = (id << Global.bitsDireccionar(tablaPags.getTama�oPagina())) + offset;
		return res;
	}
	
	private int getDireccionFisica(int direccion, int marco)
	{
		int offset = (int) Math.floor(direccion % tablaPags.getTama�oPagina());
		int res = (marco << Global.bitsDireccionar(tablaPags.getTama�oPagina())) + offset;
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
