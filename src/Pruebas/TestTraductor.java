package Pruebas;

import pckMemoria.TablaPaginas;
import general.Global.TiposReemplazo;
import general.MemoryException;

public class TestTraductor {
	
	final int palabras_linea = 4;
	
	public TestTraductor()
	{
		try
		{
			//TablaPaginas tablaPags = new TablaPaginas(4096, palabras_linea, 4294967295l, 40960, TiposReemplazo.RANDOM);
			TablaPaginas tablaPags = new TablaPaginas(16, palabras_linea, 512, 64, TiposReemplazo.RANDOM);
			System.out.println(tablaPags);
			
			// Direcciones entre 0 y 1024 -> Página 0
			
			System.out.println("2047 traducida como: 0x" + Integer.toHexString(tablaPags.traducirDireccion(2047)));
			
		}
		catch (MemoryException e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
	}

}
