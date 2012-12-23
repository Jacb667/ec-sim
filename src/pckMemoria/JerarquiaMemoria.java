package pckMemoria;

import general.Global.TiposReemplazo;
import general.Log;
import general.MemoryException;

// Jerarqu�a de memoria
public class JerarquiaMemoria {
	
	private Cache caches[];
	private MemoriaPrincipal memoria;
	
	
	public JerarquiaMemoria(Cache[] _caches, MemoriaPrincipal _memoria)
	{
		System.arraycopy(_caches, 0, caches, 0, _caches.length);
		memoria = _memoria;
	}
	
	// Leer un dato.
	public int leerDato(int direccion) throws MemoryException
	{
		Log.println(3, "Lectura de direcci�n en memoria 0x" + Integer.toHexString(direccion));
		
		// Busco en cach� L0
		Cache c = caches[0];
		
		if (c.existeDato(direccion))
		{
			Log.println(2, "Dato encontrado en cache L0");
			return c.consultarDato(direccion);
		}
		
		// Si no est� el dato, tenemos que traerlo desde donde est� a L0 (nivel a nivel).
		// Buscamos en L1
		if (caches[1].existeDato(direccion))
		{
			Log.println(2, "Dato encontrado en cache L0");
			
		}
		
		// Buscamos en cada una de las cach�s a ver si est� el dato.
		for (int i = 0; i < caches.length; i++)
		{
			if (caches[i].existeDato(direccion))
			{
				Log.println(2, "Dato encontrado en cache L" + (i+1));
				return caches[i].consultarDato(direccion);
			}
		}
		
		// Si no est�, buscamos en la memoria principal (que deber�a estar siempre).
		if (memoria.existeDato(direccion))
		{
			Log.println(2, "Lectura desde memoria principal");
			return memoria.leerDato(direccion);
		}
		
		throw new MemoryException("No se ha podido localizar la direcci�n 0x" + Integer.toHexString(direccion));
	}
	
	// Guardar un dato.
	// Se utiliza la pol�tica Write-Back y Write-Allocate
	public void guardarDato(int direccion, int dato) throws MemoryException
	{
		Log.println(3, "Guardado de direcci�n en memoria 0x" + Integer.toHexString(direccion));
		
		// Siempre escribimos en la cache L0.
		Cache c = caches[0];
		
		// Compruebo si existe el dato (HIT)
		if (c.existeDato(direccion))
		{
			Log.println(3, "El dato existe en cache L0");
			Log.println(2, "Dato modificado en cache L0");
			c.modificarDato(direccion, dato);
			// TODO: Write-Through.
			return;
		}
		else  // No existe el dato (MISS)
		{
			Log.println(3, "El dato NO existe en cache L0");
			// Comprobamos si hay una l�nea libre para traerla a cach�.
			if (c.lineaLibre(direccion))
			{
				Log.println(3, "Existen posiciones libres para traer la l�nea");
				// Traemos la l�nea desde el siguiente nivel de cach�.
				c.escribirLinea(direccion, traerLinea(direccion, c));
				
				// El dato ya est� aqu�. Lo modifico.
				Log.println(2, "Dato modificado en cache L0");
				c.modificarDato(direccion, dato);
				// TODO: Write-Through.
				return;
			}
			else  // No hay l�nea libre (debemos reemplazar).
			{
				Log.println(3, "NO existen posiciones libres para traer la l�nea");
				// Traemos la l�nea desde el siguiente nivel de cach�.
				LineaReemplazo linR = c.reemplazarLinea(direccion, traerLinea(direccion, c));
				
				// Si era dirty, tenemos que enviarla al siguiente nivel.
				if (linR != null)
				{
					Log.println(2, "Se actualiza la l�nea reemplazada \"dirty\" en los dem�s niveles");
					actualizarLinea(linR);
				}
				
				// El dato ya est� aqu�. Lo modifico.
				Log.println(2, "Dato modificado en cache L0");
				c.modificarDato(direccion, dato);
				// TODO: Write-Through.
				return;
			}
		}
	}

	// Trae una l�nea desde el siguiente nivel de cach� donde se encuentre.
	// Si no est� en otro nivel superior, la trae desde RAM.
	private int[] traerLinea(int direccion, Cache c) throws MemoryException
	{
		for (int j = 1; j < caches.length; j++)
		{
			if (caches[j].existeDato(direccion))
			{
				Log.println(2, "Traigo l�nea desde cache L" + (j+1));
				return caches[j].leerLinea(direccion);
			}
		}
		
		// Si aun as� no existe (no se ha podido traer) leemos desde RAM.
		Log.println(2, "Traigo l�nea desde memoria principal");
		if (memoria.existeDato(direccion))
			return memoria.leerLinea(direccion, c.getTamanoLinea());
		
		throw new MemoryException("No se ha encontrado la direcci�n 0x" + Integer.toHexString(direccion));
	}
	
	// Actualiza una l�nea "sucia" en los siguientes niveles de cach�.
	private void actualizarLinea(LineaReemplazo lineaR) throws MemoryException
	{
		int direccion = lineaR.getDireccion();
		int[] linea = lineaR.getLinea();
		
		Log.println(3, "Se actualiza la l�nea 0x" + Integer.toHexString(direccion));
		
		for (int j = 1; j < caches.length; j++)
		{
			if (caches[j].existeDato(direccion))
			{
				Log.println(3, "Actualizo cache L" + (j+1));
				caches[j].escribirLinea(direccion, linea);
			}
		}
		
		// Si aun as� no existe (no se ha podido traer) leemos desde RAM.
		if (memoria.existeDato(direccion))
		{
			Log.println(3, "Actualizo memoria principal");
			memoria.guardarLinea(direccion, linea);
		}
		
		throw new MemoryException("No se ha encontrado la direcci�n 0x" + Integer.toHexString(direccion));
	}
	
	
	
	
	// Mueve una l�nea hasta el nivel actual.
	private void traerLineaACache(int nivel)
	{
		
	}
	
	
	// Actualizar l�nea
	private void actualizarLineaCache(int nivel, int[] linea)
	{
		
	}

}
