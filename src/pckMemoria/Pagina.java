
package pckMemoria;

import java.awt.Dimension;

import componentes.Tabla;

import general.Global;
import general.MemoryException;

/* 
 * La memoria RAM se considera que no tiene vías ni es asociativa.
 * Por lo tanto, conociendo el tamaño en bytes podemos determinar sus líneas (de 1 palabra cada una).
 * 
 * La memoria en una memoria se almacena por palabras de 1 byte, nosotros almacenamos en campos INT de
 * 4 bytes, por lo que no necesitamos los 2 últimos bits de la dirección de memoria.
 */

/*
 * Página. La memoria RAM estará formada por varias de estas páginas.
 */

public class Pagina
{
	private int entradas;
	private int[] mem;
	private boolean[] valid;
	private Tabla interfaz;
	private int id;
	private int marco;
	
	// De momento no se usa. En un futuro podría usarse para controlar las peticiones de líneas.
	//private int palabras_linea;
	
	public Pagina(int _entradas, int _palabras_linea, int _id) throws MemoryException
	{
		if (_entradas < 1 || _palabras_linea < 1 || _entradas % _palabras_linea != 0)
			throw new MemoryException("Error en inicialización de memoria.");
			
		// Entradas debe ser divisible entre palabras_linea.
		entradas = _entradas;
		mem = new int[entradas];
		valid = new boolean[entradas];
		marco = -1;
		id = _id;
	}
	
	public Pagina(int _entradas, int _palabras_linea) throws MemoryException
	{
		if (_entradas < 1 || _palabras_linea < 1 || _entradas % _palabras_linea != 0)
			throw new MemoryException("Error en inicialización de memoria.");
			
		// Entradas debe ser divisible entre palabras_linea.
		entradas = _entradas;
		mem = new int[entradas];
		valid = new boolean[entradas];
		marco = -1;
		id = -1;
	}
	
	public int getId() { return id; }
	public void asignarMarco(int m) { marco = m; }
	public int getMarco() { return marco; }
	
	// Me aseguro de que la dirección REAL que llega, es el offset sin la página.
	public int leerDato(int direccion)
	{
		int entrada = direccion >> 2;
		entrada = (int) Math.floor(entrada % entradas);
		return mem[entrada];
	}
	
	// Me envían la dirección física, elimino los 2 últimos bits y guardo la posición.
	public void guardarDato(int direccion, int dato)
	{
		int entrada = direccion >> 2;
		entrada = (int) Math.floor(entrada % entradas);
		
		mem[entrada] = dato;
		valid[entrada] = true;
		
		// Actualizar interfaz gráfica.
		if (interfaz != null)
		{
			interfaz.setValueAt(String.valueOf(dato), direccion >> 2, 1);
			interfaz.setValueAt(true, direccion >> 2, 2);
		}
	}
	
	// Entrada de la página.
	private int getInicioBloque(int entrada, int tam_linea)
	{
		return (int) Math.floor(entrada / tam_linea);
	}
	
	// Lee varias posiciones (tam_linea) a partir de una dirección
	// Se usa para enviar una línea completa a caché
	public int[] leerLinea(int direccion, int tam_linea)
	{
		int[] res = new int[tam_linea];
		
		int entrada = direccion >> 2;
		entrada = (int) Math.floor(entrada % entradas);
		
		int direccion_inicio = getInicioBloque(entrada, tam_linea) * tam_linea;
		
		//System.out.print("Dir: 0x" + Integer.toHexString(direccion_inicio<<2) + " Bl: " + getInicioBloque(direccion, tam_linea));
		
		for (int i = 0; i < tam_linea; i++)
			res[i] = mem[direccion_inicio + i];
		
		return res;
	}
	
	public void guardarLinea(int direccion, int[] linea) 
	{
		int tam_linea = linea.length;
		int entrada = direccion >> 2;
		entrada = (int) Math.floor(entrada % entradas);
		int direccion_inicio = getInicioBloque(entrada, tam_linea) * tam_linea;
		
		for (int i = 0; i < tam_linea; i++)
		{
			mem[direccion_inicio + i] = linea[i];
			valid[direccion_inicio + i] = true;
			
			// Actualizar interfaz gráfica.
			if (interfaz != null)
			{
				interfaz.setValueAt(String.valueOf(linea[i]), direccion_inicio + i, 1);
				interfaz.setValueAt(true, direccion_inicio + i, 2);
			}
		}
	}
	
	// Temporal: De momento sale una lista con Dirección (hex) : Dato (dec)
	// Faltaría poner una opción para que muestre los datos en otros formatos (dec, bin, oct, hex).
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < entradas; i++)
		{
			// Dirección (hex) : Dato (dec)
			strB.append(String.format("0x%2S", Integer.toHexString(i << 2)).replace(" ", "0")).append(" : ").append(mem[i]).append("\n");
		}
		
		return strB.toString();
	}

	public boolean estaLibre(int direccion)
	{
		return !valid[direccion >> 2];
	}
	
	
	/*
	 *  Funciones para JTable (interfaz gráfica).
	 */
	public String[] getColumnas()
	{
		return new String[]{"Dirección", "Dato", "Válida"};
	}
	
	public Dimension[] getTamaños()
	{
		Dimension[] dim = new Dimension[3];
		
		dim[0] = new Dimension(Global.TAMAÑO_CELDA_NORMAL,0);
		dim[1] = new Dimension(Global.TAMAÑO_CELDA_NORMAL,0);
		dim[2] = new Dimension(Global.TAMAÑO_CELDA_BOOLEAN, Global.TAMAÑO_CELDA_BOOLEAN*2);
		
		return dim;
	}
	
	public Object[][] getDatos()
	{
		Object[][] datos = new Object[entradas][3];
		
		for (int i = 0; i < entradas; i++)
		{
			// Dirección, dato, valido
			int direccion = i << 2;
			Object[] linea = {String.format("0x%4S", Integer.toHexString(direccion)).replace(" ", "0"), String.valueOf(mem[i]), new Boolean(valid[i])};
			datos[i] = linea;
		}
		
		return datos;
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
