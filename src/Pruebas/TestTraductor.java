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
			TablaPaginas tablaPags = new TablaPaginas(1024, palabras_linea, 4294967295l, 10240, TiposReemplazo.RANDOM);
			System.out.println(tablaPags);
			
			// Direcciones entre 0 y 1024 -> Página 0
			
			System.out.println(tablaPags.traducirDireccion(4096));
		}
		catch (MemoryException e)
		{
			System.err.println(e);
			e.printStackTrace();
		}
	}

}
