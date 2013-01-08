package pckMemoria;

import pckCpu.CpuException;

public class Traductor {
	
	private static Cache itlb;
	private static Cache dtlb;
	
	// Esta clase traduce las direcciones lógicas a físicas.
	public static void inicializar(int tam_memoria, int tam_pagina, int num_marcos, Cache _itlb, Cache _dtlb) throws CpuException
	{
		if (tam_memoria % tam_pagina != 0)
			throw new CpuException("Tamaño incorrecto de página.");
		
		itlb = _itlb;
		dtlb = _dtlb;
		
		int num_pags = tam_memoria / tam_pagina;
	}
	
	

}
