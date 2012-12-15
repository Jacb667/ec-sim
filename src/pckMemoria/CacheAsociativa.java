package pckMemoria;

import general.Global.PoliticasReemplazo;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class CacheAsociativa implements Cache
{
	// Cada una de las v�as se implementa como una cach� directa.
	// De modo que el dise�o es bastante simple.
	private CacheDirecta vias[];
	
	private int entradas;
	private int palabras_linea;
	private Politica politica;
	
	// En cach� directa se recomienda usar tama�os de potencias de 2^x.
	// En cach� asociativa la divisi�n entradas/v�as DEBE dar exacto (no decimales).
	// Tambi�n se recomienda que entradas sea potencia de 2 (y divisible entre v�as).
	// Estas comprobaciones deben ser echas antes de invocar a este constructor.
	public CacheAsociativa(int _entradas, int _palabras_linea, int _vias, PoliticasReemplazo _Tpolitica)
	{
		entradas = _entradas / _vias;
		palabras_linea = _palabras_linea;
		
		// Creamos el array de v�as
		vias = new CacheDirecta[_vias];
		
		// Creamos las v�as
		for (int i = 0; i < _vias; i++)
			vias[i] = new CacheDirecta(entradas, palabras_linea);
		
		politica = new Politica(_Tpolitica, _entradas, _vias);
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
	public boolean lineaDirty(int direccion)
	{
		boolean isDirty = false;
		int i = 0;
		while (!isDirty && i < vias.length)
		{
			if (vias[i].existeDato(direccion))
				isDirty = vias[i].lineaDirty(direccion);
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
	public int consultarDato(int direccion)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
				return vias[i].consultarDato(direccion);
		}

		// Nunca deber�amos llegar aqu�...
		return 0;
	}

	// Si esto se ejecuta es porque sabemos que el bloque del dato est� (en alguna v�a).
	// Compruebo en qu� v�a est� y guardo el dato.
	public void modificarDato(int direccion, int dato)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
			{
				vias[i].modificarDato(direccion, dato);
				break;
			}
		}
	}

	// Leer una l�nea.
	public int[] leerLinea(int direccion)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].existeDato(direccion))
				return vias[i].leerLinea(direccion);
		}

		// Nunca deber�amos llegar aqu�...
		return null;
	}

	// Guardar una l�nea.
	// Si ejecutamos este m�todo es porque al menos existe una v�a libre donde guardarlo.
	public void escribirLinea(int direccion, int[] linea)
	{
		for (int i = 0; i < vias.length; i++)
		{
			if (vias[i].lineaLibre(direccion))
			{
				vias[i].escribirLinea(direccion, linea);
				break;
			}
		}
	}
	
	// Reemplaza una l�nea por otra. Devuelve la l�nea anterior.
	// Usar� la pol�tica de reemplazo para determinar qu� l�nea se elimina.
	public int[] reemplazarLinea(int direccion, int[] linea)
	{
		int res[] = new int[palabras_linea];
		int via = politica.elegirViaReemplazo(buscarPosicion(direccion));
		
		res = vias[via].leerLinea(direccion);
		vias[via].escribirLinea(direccion, linea);
		
		politica.nuevaLinea(buscarPosicion(direccion), via);
		
		return res;
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

	// Me determina si una direcci�n est� libre o no.
	// Si est� libre significa que puedo escribir, en caso contrario
	// tendr� que reemplazar antes de escribir.
	public boolean lineaLibre(int direccion)
	{
		boolean res = false;
		int i = 0;
		while (!res && i < vias.length)
			res = vias[i].lineaLibre(direccion);

		return res;
	}
	
	// Busco la posici�n en el array (entry) del dato.
	private int buscarPosicion(int direccion)
	{
		// Primero hay que ignorar los 2 bits de offset:
		// Los �ltimos bits del final son para seleccionar palabra, los ignoramos:
		int pos = direccion >> 2 >> general.Op.bitsDireccionar(palabras_linea);;
		
		// Los siguientes bits son del �ndice.
		// La entrada ser� el m�dulo del n�mero de entradas.
		return (int) (pos % entradas);
	}
}

class Politica {
	
	private int entradas;
	private int vias;
	private int[][] datos_reemplazo;
	private PoliticasReemplazo tipo;
	
	public Politica(PoliticasReemplazo _tipo, int _entradas, int _vias)
	{
		tipo = _tipo;
		vias = _vias;
		entradas = _entradas;
		
		datos_reemplazo = new int[entradas][vias];
	}
	
	// Actualizar pol�tica cuando se inserta una nueva l�nea en una v�a.
	public void nuevaLinea(int entrada, int via)
	{
		switch(tipo)
		{
			// Los dem�s quedan invariables. A esta le asigno el tiempo.
			case LRU:
				datos_reemplazo[entrada][via] = (int)(System.currentTimeMillis());
				break;

			// Incrementa todos en 1 y a�ade esta con valor 0.
			case LFU:
			case FIFO:
				for (int i=0; i < vias; i++)
					datos_reemplazo[entrada][i]++;
				datos_reemplazo[entrada][via] = 0;
				break;
				
			// Cuenta como acceso.
			case AGING:
				for (int i=0; i < vias; i++)
					datos_reemplazo[entrada][i] /= 10;
				datos_reemplazo[entrada][via] += 10000000;
		}
	}
	
	// Elegir la v�a que se reemplazar�.
	public int elegirViaReemplazo(int entrada)
	{
		int res = 0;
		
		switch(tipo)
		{
			// Valor m�s bajo (m�s antiguo).
			case LRU:
			case AGING:
				for (int i = 0; i < vias; i++)
				{
					if (datos_reemplazo[entrada][i] < datos_reemplazo[entrada][res])
						res = i;
				}
				break;
			
			// Valor m�s alto.
			case LFU:
			case FIFO:
				for (int i = 0; i < vias; i++)
				{
					if (datos_reemplazo[entrada][i] > datos_reemplazo[entrada][res])
						res = i;
				}
				break;
				
			// Aleatorio.
			default:
				Random rand = new Random(new Date().getTime());
				res = rand.nextInt(vias);

		}
		
		return res;
	}
	
	/*switch(tipo)
	{
		// Reemplaza la l�nea que menos se usa recientemente.
		case LRU:
			
			
		// Reemplaza el bloque que se ha usado menos veces.
		case LFU:		
			
			
		// Reemplaza el primer bloque que entr�.
		case FIFO:
			
			
		// Reemplaza el primer bloque que entr� y no se ha usado.
		case SCHANC:

		
		// Reemplaza un bloque aleatorio.
		default:
			Random rand = new Random(new Date().getTime());
			res = rand.nextInt(vias.length);
	}*/
	
}

