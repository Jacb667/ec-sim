package general;

import pckMemoria.*;

public class TestMemoria {
	
	public TestMemoria()
	{
		try
		{
			Cache[] caches = new Cache[2];
			
			// 2 niveles de cache directa
			caches[0] = new CacheDirecta(4,4);  // Caché de 4 entradas 4 palabras por línea.
			caches[1] = new CacheDirecta(8,4);  // Caché de 8 entradas 4 palabras por línea.
			
			// Memoria principal con 128 posiciones.
			MemoriaPrincipal memoria = new MemoriaPrincipal(128);
			
			JerarquiaMemoria jmem = new JerarquiaMemoria(caches, memoria);
			
			// Inicialización de la memoria para hacer pruebas.
			for (int i = 0; i < 128*4; i+=4)
				memoria.guardarDato(i, i);
			
			System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));
			
			System.out.println("Lectura 0x10: " + jmem.leerDato(0x10));
			
			System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));
			
			System.out.println("Guardo dato 1000 en 0x20");
			
			jmem.guardarDato(0x20, 1000);
			
			System.out.println("Cache L0:\n" + caches[0].toString());
			System.out.println("Cache L1:\n" + caches[1].toString());
			System.out.println("Memoria:\n" + memoria.toString(true));
			
		}
		catch (MemoryException e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
	}

}
