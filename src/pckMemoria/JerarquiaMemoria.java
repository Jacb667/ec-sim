package pckMemoria;

import general.Global.TiposReemplazo;
import general.Log;
import general.MemoryException;

// Jerarquía de memoria
public class JerarquiaMemoria {
	
	private Cache caches[];
	private MemoriaPrincipal memoria;
	private int tam_linea;
	
	
	public JerarquiaMemoria(Cache[] _caches, MemoriaPrincipal _memoria)
	{
		System.arraycopy(_caches, 0, caches, 0, _caches.length);
		memoria = _memoria;
		tam_linea = caches[0].getTamanoLinea();
	}
	
	// Leer un dato.
	public int leerDato(int direccion) throws MemoryException
	{
		Log.println(3, "Lectura de dirección en memoria 0x" + Integer.toHexString(direccion));
		
		// Busco en caché L0
		Cache c = caches[0];
		
		if (c.existeDato(direccion))
		{
			Log.println(2, "Dato encontrado en cache L0");
			return c.consultarDato(direccion);
		}
		
		// Si no está el dato, tenemos que traerlo desde donde esté a L0 (nivel a nivel).
		// Buscamos en L1
		if (caches[1].existeDato(direccion))
		{
			Log.println(2, "Dato encontrado en cache L0");
			
		}
		
		// Buscamos en cada una de las cachés a ver si está el dato.
		for (int i = 0; i < caches.length; i++)
		{
			if (caches[i].existeDato(direccion))
			{
				Log.println(2, "Dato encontrado en cache L" + (i+1));
				return caches[i].consultarDato(direccion);
			}
		}
		
		// Si no está, buscamos en la memoria principal (que debería estar siempre).
		if (memoria.existeDato(direccion))
		{
			Log.println(2, "Lectura desde memoria principal");
			return memoria.leerDato(direccion);
		}
		
		throw new MemoryException("No se ha podido localizar la dirección 0x" + Integer.toHexString(direccion));
	}
	
	// Guardar un dato.
	// Se utiliza la política Write-Back y Write-Allocate
	public void guardarDato(int direccion, int dato) throws MemoryException
	{
		Log.println(3, "Guardado de dirección en memoria 0x" + Integer.toHexString(direccion));
		
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
			// Comprobamos si hay una línea libre para traerla a caché.
			if (c.lineaLibre(direccion))
			{
				Log.println(3, "Existen posiciones libres para traer la línea");
				// Traemos la línea desde el siguiente nivel de caché.
				traerLinea(0, direccion);
				
				// El dato ya debería existir.
				if (!c.existeDato(direccion))
					throw new MemoryException("Dato inexistente en cache L0");
				
				// El dato ya está aquí. Lo modifico.
				Log.println(2, "Dato modificado en cache L0");
				c.modificarDato(direccion, dato);
				// TODO: Write-Through.
				return;
			}
			else  // No hay línea libre (debemos reemplazar).
			{
				Log.println(3, "NO existen posiciones libres para traer la línea");
				// Traemos la línea desde el siguiente nivel de caché.
				traerLinea(0, direccion);
				
				// El dato ya debería existir.
				if (!c.existeDato(direccion))
					throw new MemoryException("Dato inexistente en cache L0");
				
				// El dato ya está aquí. Lo modifico.
				Log.println(2, "Dato modificado en cache L0");
				c.modificarDato(direccion, dato);
				// TODO: Write-Through.
				return;
			}
		}
	}

	// Mueve una línea hasta el nivel actual.
	private void traerLinea(int nivel, int direccion) throws MemoryException
	{
		int nivel_sig = nivel+1;
		
		if (nivel_sig <= 0 || nivel_sig > caches.length)
			throw new MemoryException("Fallo en nivel de jerarquía de cache. Acceso a L" + nivel_sig);
		
		// La búsqueda es recursiva. Si no existe en L0, se trae desde L1.
		// Si no existe en L1, se trae desde el siguiente, etc.
		// Siguiente nivel es memoria.
		if (nivel_sig == caches.length)
		{
			MemoriaPrincipal sig = memoria;
			Cache act = caches[nivel];
			
			// Esto siempre debería ocurrir...
			if (sig.existeDato(direccion))
			{
				int[] linea = sig.leerLinea(direccion, tam_linea);
				
				// Si hay hueco en la caché donde almacenar (nivel actual).
				if (act.lineaLibre(direccion))
				{
					act.escribirLinea(direccion, linea);
					return;
				}
				else  // Si no hay hueco.
				{
					LineaReemplazo linR = act.reemplazarLinea(direccion, linea);
					
					// Si era dirty, tenemos que enviarla al siguiente nivel.
					if (linR != null)
					{
						Log.println(2, "Se actualiza la línea reemplazada \"dirty\" en los demás niveles");
						actualizarLinea(linR, nivel);
					}
					return;
				}
			}
			else
				throw new MemoryException("Fallo al traer desde memoria la línea 0x" + Integer.toHexString(direccion));
		}
		else
		{
			Cache sig = caches[nivel+1];  // De donde leo el dato.
			Cache act = caches[nivel];  // A donde traigo el dato.
			
			// Llamada recursiva para traer a los demás niveles.
			if (!sig.existeDato(direccion))
				traerLinea(nivel+1, direccion);
			
			// Una vez hemos llegado aquí, el dato debe existir en el nivel anterior.
			if (sig.existeDato(direccion))
			{
				int[] linea = sig.leerLinea(direccion);
				
				// Si hay hueco en la caché donde almacenar (nivel actual).
				if (act.lineaLibre(direccion))
				{
					act.escribirLinea(direccion, linea);
					return;
				}
				else  // Si no hay hueco.
				{
					LineaReemplazo linR = act.reemplazarLinea(direccion, linea);
					
					// Si era dirty, tenemos que enviarla al siguiente nivel.
					if (linR != null)
					{
						Log.println(2, "Se actualiza la línea reemplazada \"dirty\" en los demás niveles");
						actualizarLinea(linR, nivel);
					}
					
					return;
				}
			}
			else
				throw new MemoryException("Fallo al traer desde cache la línea 0x" + Integer.toHexString(direccion));
		}
	}
	
	
	// Actualizar línea desde el nivel actual.
	// Actualiza todos los niveles superiores.
	private void actualizarLinea(LineaReemplazo linR, int nivel) throws MemoryException
	{
		int nivel_sig = nivel+1;
		
		if (nivel_sig <= 0 || nivel_sig > caches.length)
			throw new MemoryException("Fallo en nivel de jerarquía de cache. Acceso a L" + nivel_sig);
		
		int direccion = linR.getDireccion();
		int[] linea = linR.getLinea();
		
		for (int i = nivel_sig; i < caches.length; i++)
		{
			Log.println(3, "Actualizo en cache L" + i + " la dirección 0x" + Integer.toHexString(direccion));
			caches[i].actualizarLinea(direccion, linea);
		}
		
		Log.println(3, "Actualizo en memoria la dirección 0x" + Integer.toHexString(direccion));
		memoria.guardarLinea(direccion, linea);
	}

}
