package memoria;

import general.Global.TiposReemplazo;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

public class PoliticaReemplazo {
	
	private int entradas;
	private int vias;
	private long[][] datos_reemplazo;
	private TiposReemplazo tipo;
	
	public PoliticaReemplazo(TiposReemplazo _tipo, int _entradas, int _vias)
	{
		tipo = _tipo;
		vias = _vias;
		entradas = _entradas;
		
		datos_reemplazo = new long[entradas][vias];
	}
	
	// Actualizar política cuando se accede a una línea en una vía.
	public void accesoLinea(int entrada, int via)
	{
		switch(tipo)
		{
			// Los demás quedan invariables. A esta le asigno el tiempo.
			case LRU:
				datos_reemplazo[entrada][via] = System.currentTimeMillis();
				break;

			// Incrementa todos en 1 excepto esta.
			case LFU:
				for (int i=0; i < vias; i++)
					if (i != via)
					datos_reemplazo[entrada][i]++;
				break;
				
			// No hace nada, sólo importa el orden de entrada.
			case FIFO:
				break;
				
			// Acceso.
			case AGING:
				for (int i=0; i < vias; i++)
					datos_reemplazo[entrada][i] /= 10;
				datos_reemplazo[entrada][via] += 10000000;
				break;
		}
	}
	
	// Actualizar política cuando se inserta una nueva línea en una vía.
	public void nuevaLinea(int entrada, int via)
	{
		switch(tipo)
		{
			// Los demás quedan invariables. A esta le asigno el tiempo.
			case LRU:
				datos_reemplazo[entrada][via] = (int)(System.currentTimeMillis());
				break;

			// Incrementa todos en 1 y añade esta con valor 0.
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
				break;
		}
	}
	
	// Elegir la vía que se reemplazará.
	public int elegirViaReemplazo(int entrada)
	{
		int res = 0;
		
		switch(tipo)
		{
			// Valor más bajo (más antiguo).
			case LRU:
			case AGING:
				for (int i = 0; i < vias; i++)
				{
					if (datos_reemplazo[entrada][i] < datos_reemplazo[entrada][res])
						res = i;
				}
				break;
			
			// Valor más alto.
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
	
	// Imprimir política entera (para debug)
	public String toString()
	{
		StringBuilder strB = new StringBuilder("Politica " + tipo.toString() + "\n");
		for (int i = 0; i < datos_reemplazo.length; i++)
		{
			strB.append(Arrays.toString(datos_reemplazo[i]));
			strB.append("\n");
		}
		
		return strB.toString();
	}
	
}