package pckMemoria;

import java.awt.Color;
import java.util.List;

import general.Log;
import general.Log.FlagsD;
import general.MemoryException;
import general.Log.Flags;

// Jerarquía de memoria
public class JerarquiaMemoria {
	
	private Cache caches[];
	private MemoriaPrincipal memoria;
	private int tam_linea;
	private boolean secundaria;
	
	private TablaPaginas tablaPags;
	
	public JerarquiaMemoria(TablaPaginas _tabla, Cache[] _caches, MemoriaPrincipal _memoria, boolean sec)
	{
		caches = new Cache[_caches.length];
		for (int i = 0; i < _caches.length; i++)
			caches[i] = _caches[i];
		memoria = _memoria;
		tam_linea = caches[0].getTamanoLinea();
		secundaria = sec;
		
		tablaPags = _tabla;
	}
	
	// Leer un dato.
	// Funciona con dirección VIRTUAL. Ya se encarga de traducirla.
	public int leerDato(int direccion_virtual) throws MemoryException
	{
		// Traducir la dirección.
		Direccion direccion = tablaPags.traducirDireccion(direccion_virtual, secundaria);
		
		Log.report(Flags.MEMORY_READ);
		
		// Busco en caché L1. Si no está, debemos traer la línea completa.
		Cache c = caches[0];
		
		if (c.existeDato(direccion.getReal()))
		{
			// Caché HIT L1
			Log.println(1, "CACHE HIT L1", Color.GREEN);
			if (secundaria)
				Log.report(FlagsD.CACHE_HIT_F, 0);
			else
				Log.report(FlagsD.CACHE_HIT, 0);
			
			int dato = c.consultarDato(direccion.getReal());
			Log.printDebug("Dato leído: " + dato);
			return dato;
		}
		else  // No existe el dato (MISS)
		{
			Log.println(1, "CACHE MISS L1", Color.RED);
			if (secundaria)
				Log.report(FlagsD.CACHE_MISS_F, 0);
			else
				Log.report(FlagsD.CACHE_MISS, 0);
			// Traemos la línea desde el siguiente nivel de caché.
			traerLinea(0, direccion);
			Log.println(2,"El bloque ya está disponible en L1 y se envía al procesador.");

			// El dato ya debería existir.
			if (!c.existeDato(direccion.getReal()))
				throw new MemoryException("No se ha podido localizar la dirección 0x" + direccion.getRealHex());
				
			// El dato ya está aquí. Lo devuelvo.
			int dato = c.consultarDato(direccion.getReal());
			Log.printDebug("Dato leído: " + dato);
			return dato;
		}
	}
	
	// Guardar un dato.
	// Se utiliza la política Write-Back y Write-Allocate
	// Funciona con dirección VIRTUAL. Ya se encarga de traducirla.
	public void guardarDato(int direccion_virtual, int dato) throws MemoryException
	{
		// Traducir la dirección.
		Direccion direccion = tablaPags.traducirDireccion(direccion_virtual, secundaria);
		
		Log.report(Flags.MEMORY_WRITE);
		
		// Siempre escribimos en la cache L1.
		Cache c = caches[0];
		
		// Compruebo si existe el dato (HIT)
		if (c.existeDato(direccion.getReal()))
		{
			Log.println(1, "CACHE HIT L1", Color.GREEN);
			if (secundaria)
				Log.report(FlagsD.CACHE_HIT_F, 0);
			else
				Log.report(FlagsD.CACHE_HIT, 0);
			
			Log.printDebug("Dato: " + dato + " modificado en: " + direccion.getRealHex());
			c.modificarDato(direccion.getReal(), direccion.getPagina(), dato);
			// TODO: Write-Through.
			return;
		}
		else  // No existe el dato (MISS)
		{
			Log.println(1, "CACHE MISS L1", Color.GREEN);
			if (secundaria)
				Log.report(FlagsD.CACHE_MISS_F, 0);
			else
				Log.report(FlagsD.CACHE_MISS, 0);
			// Traemos la línea desde el siguiente nivel de caché.
			traerLinea(0, direccion);
				
			// El dato ya debería existir.
			if (!c.existeDato(direccion.getReal()))
				throw new MemoryException("No se ha podido localizar la dirección 0x" + direccion.getRealHex());
				
			// El dato ya está aquí. Lo modifico.
			Log.printDebug("Dato: " + dato + " modificado en: " + direccion.getRealHex());
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
		
		// La búsqueda es recursiva. Si no existe en L1, se trae desde L2.
		// Si no existe en L2, se trae desde el siguiente, etc.
		// Siguiente nivel es memoria.
		if (nivel_sig == caches.length)
		{
			Log.println(2, "Se accede a memoria para traer el bloque a L" + (nivel_act+1));
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
				Log.println(2, "Hay un conjunto libre en L" + (nivel_act+1) + " para traer el bloque");
				act.escribirLinea(direccion.getReal(), direccion.getPagina(), linea);
			}
			else  // Si no hay hueco.
			{
				LineaReemplazo linR = act.reemplazarLinea(direccion.getReal(), direccion.getPagina(), linea);
				Log.println(2, "No hay conjunto libre para traer el bloque, reemplazo 0x" + Integer.toHexString(linR.getDireccion()));
				
				if (secundaria)
					Log.report(FlagsD.CONFLICT_CACHE_F, nivel_act);
				else
					Log.report(FlagsD.CONFLICT_CACHE, nivel_act);
				
				// Si era dirty, tenemos que enviarla al siguiente nivel.
				if (linR != null)
				{
					Log.println(2, "El bloque reemplazado era \"dirty\", se escribe en los siguientes niveles.");
					actualizarLinea(linR, nivel_act);
				}
			}
		}
		else
		{
			Log.println(2, "Se accede a L" + nivel_sig + " para traer el bloque a L" + (nivel_act+1));
			Cache sig = caches[nivel_sig];  // De donde leo el dato.
			Cache act = caches[nivel_act];  // A donde traigo el dato.
			
			// Llamada recursiva para traer a los demás niveles.
			if (!sig.existeDato(direccion.getReal()))
			{
				Log.println(2, "CACHE MISS L" + (nivel_sig+1));
				if (secundaria)
					Log.report(FlagsD.CACHE_MISS_F, nivel_sig);
				else
					Log.report(FlagsD.CACHE_MISS, nivel_sig);
				traerLinea(nivel_sig, direccion);
			}
			else
			{
				Log.println(2, "CACHE HIT L" + (nivel_sig+1));
				if (secundaria)
					Log.report(FlagsD.CACHE_HIT_F, nivel_sig);
				else
					Log.report(FlagsD.CACHE_HIT, nivel_sig);
			}
			
			// Una vez hemos llegado aquí, el dato debe existir en el nivel anterior.
			if (!sig.existeDato(direccion.getReal()))
				throw new MemoryException("Fallo al traer desde cache la línea 0x" + direccion.getRealHex());
			
			int[] linea = sig.leerLinea(direccion.getReal());
			
			// Si hay hueco en la caché donde almacenar (nivel actual).
			if (act.lineaLibre(direccion.getReal()))
			{
				Log.println(2, "Hay hueco libre en L" + (nivel_act+1) + " para traer el bloque");
				act.escribirLinea(direccion.getReal(), direccion.getPagina(), linea);
			}
			else  // Si no hay hueco.
			{
				if (secundaria)
					Log.report(FlagsD.CONFLICT_CACHE_F, nivel_act);
				else
					Log.report(FlagsD.CONFLICT_CACHE, nivel_act);
				
				LineaReemplazo linR = act.reemplazarLinea(direccion.getReal(), direccion.getPagina(), linea);
				Log.println(2, "No hay hueco libre para traer el bloque, reemplazo 0x" + Integer.toHexString(linR.getDireccion()));
				
				// Si era dirty, tenemos que enviarla al siguiente nivel.
				if (linR != null)
				{
					Log.println(2, "El bloque reemplazado era \"dirty\", se escribe en los siguientes niveles.");
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
			throw new MemoryException("Acceso a L" + (nivel_sig+1) + " en jerarquía de memoria");
		
		int direccion = linR.getDireccion();
		int pagina = linR.getPagina();
		int[] linea = linR.getLinea();
		
		for (int i = nivel_sig; i < caches.length; i++)
		{
			Log.printDebug("Actualizo en cache L" + (i+1) + " la dirección 0x" + Integer.toHexString(direccion));
			caches[i].actualizarLinea(direccion, pagina, linea);
		}
		
		Log.printDebug("Actualizo en memoria la dirección 0x" + Integer.toHexString(direccion));
		Log.report(Flags.BLOCK_WRITE);
		memoria.guardarLinea(direccion, linea);
	}
	
	// Invalidar una página en todos los niveles de caché.
	// Las líneas invalidadas y "dirty" se actualizan a los demás niveles.
	public void invalidarPagina(int pagina) throws MemoryException
	{
		Log.printDebug("Invalidando en cache la página " + pagina);
		for (int i = 0; i < caches.length; i++)
		{
			List<LineaReemplazo> lineasR1 = caches[i].invalidarPagina(pagina);
			for (LineaReemplazo lin : lineasR1)
				actualizarLinea(lin, i);
		}
	}
	
	// Devuelve los niveles de caché.
	public int getNivelesCache()
	{
		return caches.length;
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
		Direccion direccion = tablaPags.traducirDireccion(direccion_virtual, secundaria);
		
		Log.report(Flags.MEMORY_READ);
		
		// Busco en caché L1. Si no está, debemos traer la línea completa.
		Cache c = caches[0];
		
		if (c.existeDato(direccion.getReal()))
		{
			// Caché HIT L1
			Log.println(1, "CACHE HIT L1", Color.GREEN);
			if (secundaria)
				Log.report(FlagsD.CACHE_HIT_F, 0);
			else
				Log.report(FlagsD.CACHE_HIT, 0);
			
			c.consultarDato(direccion.getReal());
		}
		else  // No existe el dato (MISS)
		{
			Log.println(1, "CACHE MISS L1", Color.RED);
			if (secundaria)
				Log.report(FlagsD.CACHE_MISS_F, 0);
			else
				Log.report(FlagsD.CACHE_MISS, 0);
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
		Direccion direccion = tablaPags.traducirDireccion(direccion_virtual, secundaria);
		
		Log.report(Flags.MEMORY_WRITE);
		
		// Siempre escribimos en la cache L1.
		Cache c = caches[0];
		
		// Compruebo si existe el dato (HIT)
		if (c.existeDato(direccion.getReal()))
		{
			Log.println(1, "CACHE HIT L1", Color.GREEN);
			if (secundaria)
				Log.report(FlagsD.CACHE_HIT_F, 0);
			else
				Log.report(FlagsD.CACHE_HIT, 0);
			
			c.modificarDato(direccion.getReal(), direccion.getPagina(), dato);
		}
		else  // No existe el dato (MISS)
		{
			Log.println(1, "CACHE MISS L1", Color.GREEN);
			if (secundaria)
				Log.report(FlagsD.CACHE_MISS_F, 0);
			else
				Log.report(FlagsD.CACHE_MISS, 0);
			// Traemos la línea desde el siguiente nivel de caché.
			traerLinea(0, direccion);
				
			// El dato ya debería existir.
			if (!c.existeDato(direccion.getReal()))
				throw new MemoryException("No se ha podido localizar la dirección 0x" + direccion.getRealHex());
				
			// El dato ya está aquí. Lo modifico.
			c.modificarDato(direccion.getReal(), direccion.getPagina(), dato);
		}
		
		// Devuelvo la dirección para la traza.
		return direccion;
	}
}
