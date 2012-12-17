package pckMemoria;

import general.Global.TiposReemplazo;

// Jerarquía de memoria
public class JerarquiaMemoria {
	
	private Cache caches[];
	private MemoriaPrincipal memoria;
	
	
	public JerarquiaMemoria(Cache[] _caches, MemoriaPrincipal _memoria)
	{
		System.arraycopy(_caches, 0, caches, 0, _caches.length);
		memoria = _memoria;
	}
	
	
	// Leer un dato.
	public int leerDato(int direccion)
	{
		
	}
	

}
