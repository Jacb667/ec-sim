package pckMemoria;

import general.Global.PoliticasReemplazo;

// Jerarquía de memoria
public class Jerarquia {
	
	private Cache caches[];
	private MemoriaPrincipal memoria;
	
	
	public Jerarquia(Cache[] _caches, MemoriaPrincipal _memoria)
	{
		System.arraycopy(_caches, 0, caches, 0, _caches.length);
		memoria = _memoria;
		
		
		
		
	}

}
