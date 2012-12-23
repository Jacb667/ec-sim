package general;

import pckMemoria.*;

public class TestMemoria {
	
	public TestMemoria()
	{
		try
		{
			Cache[] caches = new Cache[2];
			
			// 2 niveles de cache directa
			caches[0] = new CacheDirecta(4,2);
			caches[1] = new CacheDirecta(8,2);
			
			MemoriaPrincipal memoria = new MemoriaPrincipal(24);
			
			
			JerarquiaMemoria jmem = new JerarquiaMemoria(caches, memoria);
			
			jmem.guardarDato(direccion, dato);
			
		}
		catch (MemoryException e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
	}

}
