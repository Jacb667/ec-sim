
package pckMemoria;

/* 
 * La memoria RAM se considera que no tiene v�as ni es asociativa.
 * Por lo tanto, conociendo el tama�o en bytes podemos determinar sus l�neas (de 1 palabra cada una).
 * 
 * La memoria en una memoria se almacena por palabras de 1 byte, nosotros almacenamos en campos INT de
 * 4 bytes, por lo que no necesitamos los 2 �ltimos bits de la direcci�n de memoria.
 */

public class MemoriaPrincipal
{
	private int entradas;
	private int[] mem;
	private boolean[] valid;
	
	public MemoriaPrincipal(int _tama�o, boolean b)
	{
		// Si b me env�an bytes de la memoria
		if (b)
		{
			// Dividimos entre 32 para conocer el n�mero de entradas.
			entradas = _tama�o / 32;
			mem = new int[entradas];
			valid = new boolean[entradas];
		}
		// En caso contrario me env�an las entradas (entradas * 32 = tama�o).
		else
		{
			entradas = _tama�o;
			mem = new int[entradas];
			valid = new boolean[entradas];
		}
	}
	
	// Compruebo si la direcci�n es v�lida.
	public boolean existeDato(int direccion)
	{
		direccion = direccion >> 2;
		if (direccion < 0 || direccion > entradas)
			return false;
		
		return true;
	}
	
	// Me env�an la direcci�n f�sica, elimino los 2 �ltimos bits y leo la posici�n.
	public int leerDato(int direccion)
	{
		return mem[direccion >> 2];
	}
	
	// Me env�an la direcci�n f�sica, elimino los 2 �ltimos bits y guardo la posici�n.
	public void guardarDato(int direccion, int dato)
	{
		mem[direccion >> 2] = dato;
		valid[direccion >> 2] = true;
	}
	
	// Direcci�n f�sica, hay que eliminar los 2 �ltimos bits.
	private int getInicioBloque(int direccion, int tam_linea)
	{
		return (int) Math.floor((direccion >> 2) / tam_linea);
	}
	
	// Lee varias posiciones (tam_linea) a partir de una direcci�n
	// Se usa para enviar una l�nea completa a cach�
	public int[] leerLinea(int direccion, int tam_linea)
	{
		int[] res =  new int[tam_linea];
		int direccion_inicio = getInicioBloque(direccion, tam_linea) * tam_linea;
		
		//System.out.print("Dir: 0x" + Integer.toHexString(direccion_inicio<<2) + " Bl: " + getInicioBloque(direccion, tam_linea));
		
		for (int i = 0; i < tam_linea; i++)
			res[i] = mem[direccion_inicio + i];
		
		return res;
	}
	
	public void guardarLinea(int direccion, int[] linea) 
	{
		int tam_linea = linea.length;
		int direccion_inicio = getInicioBloque(direccion, tam_linea) * tam_linea;
		
		for (int i = 0; i < tam_linea; i++)
		{
			mem[direccion_inicio + i] = linea[i];
			valid[direccion_inicio + i] = true;
		}
	}
	
	// Temporal: De momento sale una lista con Direcci�n (hex) : Dato (dec)
	// Faltar�a poner una opci�n para que muestre los datos en otros formatos (dec, bin, oct, hex).
	public String toString(boolean mostrarTodos)
	{
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < entradas; i++)
		{
			if (mostrarTodos || valid[i])
			{
				// Direcci�n (hex) : Dato (dec)
				strB.append(String.format("0x%2S", Integer.toHexString(i << 2)).replace(" ", "0")).append(" : ").append(mem[i]).append("\n");
			}
		}
		
		return strB.toString();
	}

	public boolean isValid(int direccion)
	{
		return valid[direccion >> 2];
	}
}
