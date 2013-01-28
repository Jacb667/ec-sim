package general;

// Archivo global para configuraciones del programa.

public class Config {
	
	private static int enteros[] = new int[Conf_Type.END_CONFIG.index()];
	private static String cadenas[] = new String[Conf_Type_c.END_CONFIG.index()];
	
	public static void set(Conf_Type config, int value)
	{
		enteros[config.index()] = value;
	}
	
	public static int get(Conf_Type config)
	{
		return enteros[config.index()];
	}
	
	public static void set(Conf_Type_c config, String value)
	{
		cadenas[config.index()] = value;
	}
	
	public static String get(Conf_Type_c config)
	{
		return cadenas[config.index()];
	}
	
	public enum Conf_Type_c
	{
		TLB_DATOS_POLITICA(0),
		TLB_INSTRUCCIONES_POLITICA(1),
		CACHE1_DATOS_POLITICA(2),
		CACHE2_DATOS_POLITICA(3),
		CACHE3_DATOS_POLITICA(4),
		CACHE1_INSTRUCCIONES_POLITICA(5),
		CACHE2_INSTRUCCIONES_POLITICA(6),
		CACHE3_INSTRUCCIONES_POLITICA(7),
		ARCHIVO_TRAZA(8),
		ARCHIVO_CODIGO(9),
		ARCHIVO_EXPORTAR(10),
		END_CONFIG (11);
		
		private int index;   

		Conf_Type_c(int index) {
	        this.index = index;
	    }

	    public int index() { 
	        return index; 
	    }
	}
	
	public enum Conf_Type
	{
		TAMAÑO_PALABRA(0),
		JERARQUIAS_SEPARADAS(1),
		SEGMENTADO(2),
		ENTRADAS_PAGINA(3),
		NUMERO_ENTRADAS_MEMORIA(4),
		MAXIMA_ENTRADA_MEMORIA(5),
		TLB_DATOS(6),
		TLB_INSTRUCCIONES(7),
		TLB_DATOS_ENTRADAS(8),
		TLB_DATOS_VIAS(9),
		TLB_INSTRUCCIONES_ENTRADAS(10),
		TLB_INSTRUCCIONES_VIAS(11),
		NIVELES_CACHE_DATOS(12),
		NIVELES_CACHE_INSTRUCCIONES(13),
		TAMAÑO_LINEA(14),
		CACHE1_DATOS_ENTRADAS(15),
		CACHE1_DATOS_VIAS(16),
		CACHE2_DATOS_ENTRADAS(17),
		CACHE2_DATOS_VIAS(18),
		CACHE3_DATOS_ENTRADAS(19),
		CACHE3_DATOS_VIAS(20),
		CACHE1_INSTRUCCIONES_ENTRADAS(21),
		CACHE1_INSTRUCCIONES_VIAS(22),
		CACHE2_INSTRUCCIONES_ENTRADAS(23),
		CACHE2_INSTRUCCIONES_VIAS(24),
		CACHE3_INSTRUCCIONES_ENTRADAS(25),
		CACHE3_INSTRUCCIONES_VIAS(26),
		END_CONFIG (27);
		
		private int index;   

		Conf_Type(int index) {
	        this.index = index;
	    }

	    public int index() { 
	        return index; 
	    }
	}
}
