package pckMemoria;

import java.util.List;

import general.Log;
import general.MemoryException;
import general.Log.Flags;

// Jerarquía de memoria
public class JerarquiaMemoria {
	
	private Cache caches[];
	private MemoriaPrincipal memoria;
	private int tam_linea;
	
	private TablaPaginas tablaPags;
	
	public JerarquiaMemoria(TablaPaginas _tabla, Cache[] _caches, MemoriaPrincipal _memoria)
	{
		caches = new Cache[_caches.length];
		for (int i = 0; i < _caches.length; i++)
			caches[i] = _caches[i];
		memoria = _memoria;
		tam_linea = caches[0].getTamanoLinea();
		
		tablaPags = _tabla;
		tablaPags.setJerarquiaMemoria(this);
	}
	
	// Leer un dato.
	// Funciona con dirección VIRTUAL. Ya se encarga de traducirla.
	public int leerDato(int direccion_virtual) throws MemoryException
	{
		// Traducir la dirección.
		Direccion direccion = tablaPags.traducirDireccion(direccion_virtual);
		
		Log.report(Flags.MEMORY_READ);
		Log.println(3, "Lectura en memoria 0x" + direccion.getRealHex());
		
		// Busco en caché L0. Si no está, debemos traer la línea completa.
		Cache c = caches[0];
		
		if (c.existeDato(direccion.getReal()))
		{
			// Caché HIT L0
			Log.println(2, "Dato encontrado en cache L0");
			Log.report(Flags.CACHE_HIT, 0);
			return c.consultarDato(direccion.getReal());
		}
		else  // No existe el dato (MISS)
		{
			Log.println(3, "El dato NO existe en cache L0");
			Log.report(Flags.CACHE_MISS, 0);
			// Traemos la línea desde el siguiente nivel de caché.
			traerLinea(0, direccion);

			// El dato ya debería existir.
			if (!c.existeDato(direccion.getReal()))
				throw new MemoryException("No se ha podido localizar la dirección 0x" + direccion.getRealHex());
				
			// El dato ya está aquí. Lo devuelvo.
			return c.consultarDato(direccion.getReal());
		}
	}
	
	// Guardar un dato.
	// Se utiliza la política Write-Back y Write-Allocate
	// Funciona con dirección VIRTUAL. Ya se encarga de traducirla.
	public void guardarDato(int direccion_virtual, int dato) throws MemoryException
	{
		// Traducir la dirección.
		Direccion direccion = tablaPags.traducirDireccion(direccion_virtual);
		
		Log.report(Flags.MEMORY_WRITE);
		Log.println(3, "Guardado en memoria 0x" + direccion.getRealHex());
		
		// Siempre escribimos en la cache L0.
		Cache c = caches[0];
		
		// Compruebo si existe el dato (HIT)
		if (c.existeDato(direccion.getReal()))
		{
			Log.println(3, "El dato existe en cache L0");
			Log.println(2, "Dato modificado en cache L0");
			Log.report(Flags.CACHE_HIT, 0);
			c.modificarDato(direccion.getReal(), direccion.getPagina(), dato);
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
			if (!c.existeDato(direccion.getReal()))
				throw new MemoryException("No se ha podido localizar la dirección 0x" + direccion.getRealHex());
				
			// El dato ya está aquí. Lo modifico.
			Log.println(2, "Dato modificado en cache L0");
			c.modificarDato(direccion.getReal(), direccion.getPagina(), dato);
			// TODO: Write-Through.
			return;
		}
	}

	// Mueve una línea hasta el nivel actual.
	private void traerLinea(int nivel_act, Direccion direccion) throws MemoryException
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
			if (!sig.existeDato(direccion.getReal()))
				throw new MemoryException("Fallo al traer desde memoria la línea 0x" + direccion.getRealHex());
			
			Log.report(Flags.BLOCK_READ);
			int[] linea = sig.leerLinea(direccion.getReal(), tam_linea);
			
			// Si hay hueco en la caché donde almacenar (nivel actual).
			if (act.lineaLibre(direccion.getReal()))
			{
				Log.println(3, "Hay hueco libre en L" + nivel_act + " para traer el bloque");
				act.escribirLinea(direccion.getReal(), direccion.getPagina(), linea);
			}
			else  // Si no hay hueco.
			{
				LineaReemplazo linR = act.reemplazarLinea(direccion.getReal(), direccion.getPagina(), linea);
				
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
			if (!sig.existeDato(direccion.getReal()))
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
			if (!sig.existeDato(direccion.getReal()))
				throw new MemoryException("Fallo al traer desde cache la línea 0x" + direccion.getRealHex());
			
			int[] linea = sig.leerLinea(direccion.getReal());
			
			// Si hay hueco en la caché donde almacenar (nivel actual).
			if (act.lineaLibre(direccion.getReal()))
			{
				Log.println(3, "Hay hueco libre en L" + nivel_act + " para traer el bloque");
				act.escribirLinea(direccion.getReal(), direccion.getPagina(), linea);
			}
			else  // Si no hay hueco.
			{
				LineaReemplazo linR = act.reemplazarLinea(direccion.getReal(), direccion.getPagina(), linea);
				
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
		int pagina = linR.getPagina();
		int[] linea = linR.getLinea();
		
		for (int i = nivel_sig; i < caches.length; i++)
		{
			Log.println(3, "Actualizo en cache L" + i + " la dirección 0x" + Integer.toHexString(direccion));
			caches[i].actualizarLinea(direccion, pagina, linea);
		}
		
		Log.println(3, "Actualizo en memoria la dirección 0x" + Integer.toHexString(direccion));
		Log.report(Flags.BLOCK_WRITE);
		memoria.guardarLinea(direccion, linea);
	}
	
	// Invalidar una página en todos los niveles de caché.
	// Las líneas invalidadas y "dirty" se actualizan a los demás niveles.
	public void invalidarPagina(int pagina) throws MemoryException
	{
		for (int i = 0; i < caches.length; i++)
		{
			List<LineaReemplazo> lineasR1 = caches[i].invalidarPagina(pagina);
			for (LineaReemplazo lin : lineasR1)
				actualizarLinea(lin, i);
		}
	}
	
	public void actualizarMarcoInterfazMemoria(int marco)
	{
		memoria.actualizarPaginaInterfaz(marco);
	}
	
	/*
	 * Traza de memoria
	 */
	
	// Simulación de lectura de dato (para TRAZA)
	// Funciona con dirección VIRTUAL. Ya se encarga de traducirla.
	public Direccion simularLeerDato(int direccion_virtual) throws MemoryException
	{
		// Traducir la dirección.
		Direccion direccion = tablaPags.traducirDireccion(direccion_virtual);
		
		Log.report(Flags.MEMORY_READ);
		Log.println(3, "Lectura en memoria 0x" + direccion.getRealHex());
		
		// Busco en caché L0. Si no está, debemos traer la línea completa.
		Cache c = caches[0];
		
		if (c.existeDato(direccion.getReal()))
		{
			// Caché HIT L0
			Log.println(2, "Dato encontrado en cache L0");
			Log.report(Flags.CACHE_HIT, 0);
			c.consultarDato(direccion.getReal());
		}
		else  // No existe el dato (MISS)
		{
			Log.println(3, "El dato NO existe en cache L0");
			Log.report(Flags.CACHE_MISS, 0);
			// Traemos la línea desde el siguiente nivel de caché.
			traerLinea(0, direccion);

			// El dato ya debería existir.
			if (!c.existeDato(direccion.getReal()))
				throw new MemoryException("No se ha podido localizar la dirección 0x" + direccion.getRealHex());
				
			// El dato ya está aquí. Lo devuelvo.
			c.consultarDato(direccion.getReal());
		}
		
		// Devuelvo la dirección para la traza.
		return direccion;
	}
	
	// Simulación de guardado de dato (para TRAZA)
	// Se utiliza la política Write-Back y Write-Allocate
	// Funciona con dirección VIRTUAL. Ya se encarga de traducirla.
	public Direccion simularGuardarDato(int direccion_virtual, int dato) throws MemoryException
	{
		// Traducir la dirección.
		Direccion direccion = tablaPags.traducirDireccion(direccion_virtual);
		
		Log.report(Flags.MEMORY_WRITE);
		Log.println(3, "Guardado en memoria 0x" + direccion.getRealHex());
		
		// Siempre escribimos en la cache L0.
		Cache c = caches[0];
		
		// Compruebo si existe el dato (HIT)
		if (c.existeDato(direccion.getReal()))
		{
			Log.println(3, "El dato existe en cache L0");
			Log.println(2, "Dato modificado en cache L0");
			Log.report(Flags.CACHE_HIT, 0);
			c.modificarDato(direccion.getReal(), direccion.getPagina(), dato);
			// TODO: Write-Through.
		}
		else  // No existe el dato (MISS)
		{
			Log.println(3, "El dato NO existe en cache L0");
			Log.report(Flags.CACHE_MISS, 0);
			// Traemos la línea desde el siguiente nivel de caché.
			traerLinea(0, direccion);
				
			// El dato ya debería existir.
			if (!c.existeDato(direccion.getReal()))
				throw new MemoryException("No se ha podido localizar la dirección 0x" + direccion.getRealHex());
				
			// El dato ya está aquí. Lo modifico.
			Log.println(2, "Dato modificado en cache L0");
			c.modificarDato(direccion.getReal(), direccion.getPagina(), dato);
			// TODO: Write-Through.
		}
		
		// Devuelvo la dirección para la traza.
		return direccion;
	}
}
