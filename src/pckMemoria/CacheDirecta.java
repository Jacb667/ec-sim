package pckMemoria;

import java.util.Arrays;

/* Tama�o:
 * 
 * Palabras de 4 bytes.
 * 1024 entradas = 1024 * 4 = 4KB (1 word por l�nea).
 * 
 * 32 entradas * 4 word por l�nea = 128 bits por l�nea = 4096Bytes = 4KB
 * 
 */

public class CacheDirecta implements InterfaceMemoria
{
	private int palabras_linea;
	private int entradas;
	
	private int bits_tag;
	private int bits_dir;
	private int bits_pal;
	
	private int[] tags;
	private boolean[] valid;
	private int[/*lineas*/][/*palabras*/] datos;
	
	public CacheDirecta(int _entradas, int _palabras_linea)
	{
		palabras_linea = _palabras_linea;
		entradas = _entradas;
		
		// Eliminar offset
		int bits_restantes = general.Constants.LONGITUD_BITS - 2;
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
		
		datos = new int[entradas][palabras_linea];
	}
	
	public boolean existeDato(int direccion)
	{
		int entrada = buscarPosicion(direccion);
		
		// Extraemos el tag
		int tag = direccion >> 2 >> bits_pal >> bits_dir;
		
		// Buscamos en el tag para ver si existe la palabra.
		if (tags[entrada] == tag)
			return true;
		
		return false;
	}
	
	// Busco la posici�n en el array (entry) del dato.
	public int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los �ltimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> 2 >> bits_pal;
		
		// Los siguientes bits son del �ndice.
		// La entrada ser� el m�dulo de 2^bits_dir
		return (int) (pos % Math.pow(2, bits_dir));
	}

	public int leerDato(int direccion)
	{
		
		return 0;
	}

	public void guardarDato(int direccion, int dato)
	{

		
	}

	public int[] leerLinea(int direccion, int tam_linea)
	{

		return null;
	}

	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < datos.length; i++)
		{
			// Direcci�n (hex) : Dato (dec)
			strB.append(String.format("0x%3S", Integer.toHexString(i << 2)).replace(" ", "0")).append(" : ").append(Arrays.toString(datos[i]));
			strB.append("\n");
		}
		
		return strB.toString();
	}

	public void guardarLinea(int direccion, int[] linea)
	{
		
	}

	@Override
	public String toString(boolean mostrarTodos) {
		// TODO Auto-generated method stub
		return null;
	}
}
