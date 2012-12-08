package pckMemoria;

public class CacheAsociativa implements Cache
{
	
	// Cada una de las v�as se implementa como una cach� directa.
	// De modo que el dise�o es bastante simple.
	private CacheDirecta vias[];
	
	private int entradas;
	private int palabras_linea;
	
	// En cach� directa se recomienda usar tama�os de potencias de 2^x.
	// En cach� asociativa la divisi�n entradas/v�as DEBE dar exacto (no decimales).
	// Tambi�n se recomienda que entradas sea potencia de 2 (y divisible entre v�as).
	// Estas comprobaciones deben ser echas antes de invocar a este constructor.
	public CacheAsociativa(int _entradas, int _palabras_linea, int _vias)
	{
		entradas = _entradas / _vias;
		palabras_linea = _palabras_linea;
		
		// Creamos el array de v�as
		vias = new CacheDirecta[_vias];
		
		// Creamos las v�as
		for (int i = 0; i < _vias; i++)
		{
			vias[i] = new CacheDirecta(entradas, palabras_linea);
		}
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

	// Compruebo si isDirty en la v�a que est�.
	public boolean isDirty(int direccion)
	{
		boolean isDirty = false;
		int i = 0;
		while (!isDirty && i < vias.length)
		{
			if (vias[i].existeDato(direccion))
				isDirty = vias[i].isDirty(direccion);
			i++;
		}
		
		return isDirty;
	}

	public int getTamanoLinea()
	{
		return palabras_linea;
	}

	// Si esto se ejecuta es porque sabemos que el dato est� (en alguna v�a).
	// Compruebo en qu� v�a est� y leo el dato.
	public int leerDato(int direccion)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
				return vias[i].leerDato(direccion);
			i++;
		}

		// Nunca deber�amos llegar aqu�...
		return 0;
	}

	// Si esto se ejecuta es porque sabemos que el bloque del dato est� (en alguna v�a).
	// Compruebo en qu� v�a est� y guardo el dato.
	public void guardarDato(int direccion, int dato, boolean setDirty)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
			{
				vias[i].guardarDato(direccion, dato, setDirty);
				break;
			}
			i++;
		}
	}

	// Leer una l�nea.
	public int[] leerLinea(int direccion)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
				return vias[i].leerLinea(direccion);
			i++;
		}

		// Nunca deber�amos llegar aqu�...
		return null;
	}

	// Guardar una l�nea.
	public void guardarLinea(int direccion, int[] linea, boolean setDirty)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
			{
				vias[i].guardarLinea(direccion, linea, setDirty);
				break;
			}
			i++;
		}
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		
		for (int i = 0; i < vias.length; i++)
		{
			strB.append("-- Via ").append(i).append("\n\n");
			strB.append(vias[i].toString()).append("\n");
			strB.append("\n");
		}
		
		return strB.toString();
	}

	@Override
	public boolean estaLibre(int direccion) {
		// TODO Auto-generated method stub
		return false;
	}

}
