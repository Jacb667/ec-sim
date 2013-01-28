package general;

// Archivo global para configuraciones del programa.

public class Config {
	
	private static int c[] = new int[Conf_Type.END_CONFIG.index()];
	
	public static void set(Conf_Type config, int value)
	{
		c[config.index()] = value;
	}
	
	public static int get(Conf_Type config)
	{
		return c[config.index()];
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
		TLB_DATOS_POLITICA(10),
		TLB_INSTRUCCIONES_ENTRADAS(11),
		TLB_INSTRUCCIONES_VIAS(12),
		TLB_INSTRUCCIONES_POLITICA(13),
		NIVELES_CACHE_DATOS(14),
		NIVELES_CACHE_INSTRUCCIONES(15),
		TAMAÑO_LINEA(16),
		CACHE1_DATOS_ENTRADAS(17),
		CACHE1_DATOS_VIAS(18),
		CACHE1_DATOS_POLITICA(19),
		CACHE2_DATOS_ENTRADAS(20),
		CACHE2_DATOS_VIAS(21),
		CACHE2_DATOS_POLITICA(22),
		CACHE3_DATOS_ENTRADAS(23),
		CACHE3_DATOS_VIAS(24),
		CACHE3_DATOS_POLITICA(25),
		CACHE1_INSTRUCCIONES_ENTRADAS(26),
		CACHE1_INSTRUCCIONES_VIAS(27),
		CACHE1_INSTRUCCIONES_POLITICA(28),
		CACHE2_INSTRUCCIONES_ENTRADAS(29),
		CACHE2_INSTRUCCIONES_VIAS(30),
		CACHE2_INSTRUCCIONES_POLITICA(31),
		CACHE3_INSTRUCCIONES_ENTRADAS(32),
		CACHE3_INSTRUCCIONES_VIAS(33),
		CACHE3_INSTRUCCIONES_POLITICA(34),
		ARCHIVO_TRAZA(35),
		ARCHIVO_CODIGO(36),
		ARCHIVO_EXPORTAR(37),
		END_CONFIG (38);
		
		private int index;   

		Conf_Type(int index) {
	        this.index = index;
	    }

	    public int index() { 
	        return index; 
	    }
	}
}
