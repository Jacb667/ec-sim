package pckMemoria;

import general.Log;
import general.MemoryException;
import general.Log.Flags;

// Jerarquía de memoria
public class JerarquiaMemoria {
	
	private Cache caches[];
	private MemoriaPrincipal memoria;
	private int tam_linea;
	
	public JerarquiaMemoria(TablaPaginas _tabla, Cache[] _caches, MemoriaPrincipal _memoria)
	{
		caches = new Cache[_caches.length];
		for (int i = 0; i < _caches.length; i++)
			caches[i] = _caches[i];
		memoria = _memoria;
		tam_linea = caches[0].getTamanoLinea();
	}
	
	// Leer un dato.
	public int leerDato(int direccion) throws MemoryException
	{
		Log.report(Flags.MEMORY_READ);
		Log.println(3, "Lectura en memoria 0x" + Integer.toHexString(direccion));
		
		// Busco en caché L0. Si no está, debemos traer la línea completa.
		Cache c = caches[0];
		
		if (c.existeDato(direccion))
		{
			// Caché HIT L0
			Log.println(2, "Dato encontrado en cache L0");
			Log.report(Flags.CACHE_HIT, 0);
			return c.consultarDato(direccion);
		}
		else  // No existe el dato (MISS)
		{
			Log.println(3, "El dato NO existe en cache L0");
			Log.report(Flags.CACHE_MISS, 0);
			// Traemos la línea desde el siguiente nivel de caché.
			traerLinea(0, direccion);

			// El dato ya debería existir.
			if (!c.existeDato(direccion))
				throw new MemoryException("No se ha podido localizar la dirección 0x" + Integer.toHexString(direccion));
				
			// El dato ya está aquí. Lo devuelvo.
			return c.consultarDato(direccion);
		}
	}
	
	// Guardar un dato.
	// Se utiliza la política Write-Back y Write-Allocate
	public void guardarDato(int direccion, int dato) throws MemoryException
	{
		Log.report(Flags.MEMORY_WRITE);
		Log.println(3, "Guardado en memoria 0x" + Integer.toHexString(direccion));
		
		// Siempre escribimos en la cache L0.
		Cache c = caches[0];
		
		// Compruebo si existe el dato (HIT)
		if (c.existeDato(direccion))
		{
			Log.println(3, "El dato existe en cache L0");
			Log.println(2, "Dato modificado en cache L0");
			Log.report(Flags.CACHE_HIT, 0);
			c.modificarDato(direccion, dato);
			// TODO: Write-Through.
			return;
		}
		else  // No existe el dato (MISS)
		{
			Log.println(3, "El dato NO existe en cache L0");
			Log.report(Flags.CACHE_MISS, 0);
			// Traemos la línea desde el siguiente nivel de caché.
			traerLinea(0, direccion);
				
			// El dato ya debería existir.
			if (!c.existeDato(direccion))
				throw new MemoryException("No se ha podido localizar la dirección 0x" + Integer.toHexString(direccion));
				
			// El dato ya está aquí. Lo modifico.
			Log.println(2, "Dato modificado en cache L0");
			c.modificarDato(direccion, dato);
			// TODO: Write-Through.
			return;
		}
	}

	// Mueve una línea hasta el nivel actual.
	private void traerLinea(int nivel_act, int direccion) throws MemoryException
	{
		int nivel_sig = nivel_act+1;
		
		if (nivel_sig <= 0 || nivel_sig > caches.length)
			throw new MemoryException("Fallo en nivel de jerarquía de cache. Acceso a L" + nivel_sig);
		
		// La búsqueda es recursiva. Si no existe en L0, se trae desde L1.
		// Si no existe en L1, se trae desde el siguiente, etc.
		// Siguiente nivel es memoria.
		if (nivel_sig == caches.length)
		{
			Log.println(3, "Trayendo bloque desde memoria a L" + nivel_act);
			MemoriaPrincipal sig = memoria;
			Cache act = caches[nivel_act];
			
			// Esto siempre debería ocurrir...
			if (!sig.existeDato(direccion))
				throw new MemoryException("Fallo al traer desde memoria la línea 0x" + Integer.toHexString(direccion));
			
			Log.report(Flags.BLOCK_READ);
			int[] linea = sig.leerLinea(direccion, tam_linea);
			
			// Si hay hueco en la caché donde almacenar (nivel actual).
			if (act.lineaLibre(direccion))
			{
				Log.println(3, "Hay hueco libre en L" + nivel_act + " para traer el bloque");
				act.escribirLinea(direccion, linea);
			}
			else  // Si no hay hueco.
			{
				LineaReemplazo linR = act.reemplazarLinea(direccion, linea);
				
				// Si era dirty, tenemos que enviarla al siguiente nivel.
				if (linR != null)
				{
					Log.println(3, "El bloque reemplazado era \"dirty\", se actualiza en los demás niveles");
					actualizarLinea(linR, nivel_act);
				}
			}
		}
		else
		{
			Log.println(3, "Trayendo bloque desde L" + nivel_sig + " a L" + nivel_act);
			Cache sig = caches[nivel_sig];  // De donde leo el dato.
			Cache act = caches[nivel_act];  // A donde traigo el dato.
			
			// Llamada recursiva para traer a los demás niveles.
			if (!sig.existeDato(direccion))
			{
				traerLinea(nivel_sig, direccion);
				Log.report(Flags.CACHE_MISS, nivel_sig);
			}
			else
			{
				Log.errorln(2, "Dato encontrado en cache L" + nivel_sig);
				Log.report(Flags.CACHE_HIT, nivel_sig);
			}
			
			// Una vez hemos llegado aquí, el dato debe existir en el nivel anterior.
			if (!sig.existeDato(direccion))
				throw new MemoryException("Fallo al traer desde cache la línea 0x" + Integer.toHexString(direccion));
			
			int[] linea = sig.leerLinea(direccion);
			
			// Si hay hueco en la caché donde almacenar (nivel actual).
			if (act.lineaLibre(direccion))
			{
				Log.println(3, "Hay hueco libre en L" + nivel_act + " para traer el bloque");
				act.escribirLinea(direccion, linea);
			}
			else  // Si no hay hueco.
			{
				LineaReemplazo linR = act.reemplazarLinea(direccion, linea);
				
				// Si era dirty, tenemos que enviarla al siguiente nivel.
				if (linR != null)
				{
					Log.println(3, "El bloque reemplazado era \"dirty\", se actualiza en los demás niveles");
					actualizarLinea(linR, nivel_act);
				}
			}
		}
	}
	
	// Actualizar línea desde el nivel actual.
	// Actualiza todos los niveles superiores.
	// Añadir un boolean para tipo de política.
	private void actualizarLinea(LineaReemplazo linR, int nivel_act) throws MemoryException
	{
		int nivel_sig = nivel_act+1;
		
		if (nivel_sig <= 0 || nivel_sig > caches.length)
			throw new MemoryException("Acceso a L" + nivel_sig + " en jerarquía de memoria");
		
		int direccion = linR.getDireccion();
		int[] linea = linR.getLinea();
		
		for (int i = nivel_sig; i < caches.length; i++)
		{
			Log.println(3, "Actualizo en cache L" + i + " la dirección 0x" + Integer.toHexString(direccion));
			caches[i].actualizarLinea(direccion, linea);
		}
		
		Log.println(3, "Actualizo en memoria la dirección 0x" + Integer.toHexString(direccion));
		Log.report(Flags.BLOCK_WRITE);
		memoria.guardarLinea(direccion, linea);
	}
	
	public void invalidarPagina(int pagina)
	{
		for (int i = 0; i < caches.length; i++)
			caches[i].invalidarPagina(pagina);
	}
	
	public void actualizarMarcoInterfazMemoria(int marco)
	{
		memoria.actualizarPaginaInterfaz(marco);
	}
}
