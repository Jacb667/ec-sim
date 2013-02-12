package general;

import gui.Controlador;
import gui.Vista;

// Archivo global para configuraciones del programa.

public class Config {
	
	private static int enteros[] = new int[Conf_Type.END_CONFIG.index()];
	private static String cadenas[] = new String[Conf_Type_c.END_CONFIG.index()];
	private static Vista v;
	private static Controlador ctr;
	
	public static boolean ejecutando_codigo;
	
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
		ARCHIVO_MEMORIA(10),
		TP_POLITICA(11),
		END_CONFIG (12);
		
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
		NIVEL_JERARQUIAS_SEPARADAS(1),
		ENTRADAS_PAGINA(2),
		NUMERO_ENTRADAS_MEMORIA(3),
		MAXIMA_ENTRADA_MEMORIA(4),
		TLB_DATOS(5),
		TLB_INSTRUCCIONES(6),
		TLB_DATOS_ENTRADAS(7),
		TLB_DATOS_VIAS(8),
		TLB_INSTRUCCIONES_ENTRADAS(9),
		TLB_INSTRUCCIONES_VIAS(10),
		NIVELES_CACHE_DATOS(11),
		NIVELES_CACHE_INSTRUCCIONES(12),
		TAMAÑO_LINEA(13),
		CACHE1_DATOS_ENTRADAS(14),
		CACHE1_DATOS_VIAS(15),
		CACHE2_DATOS_ENTRADAS(16),
		CACHE2_DATOS_VIAS(17),
		CACHE3_DATOS_ENTRADAS(18),
		CACHE3_DATOS_VIAS(19),
		CACHE1_INSTRUCCIONES_ENTRADAS(20),
		CACHE1_INSTRUCCIONES_VIAS(21),
		CACHE2_INSTRUCCIONES_ENTRADAS(22),
		CACHE2_INSTRUCCIONES_VIAS(23),
		CACHE3_INSTRUCCIONES_ENTRADAS(24),
		CACHE3_INSTRUCCIONES_VIAS(25),
		TABLA_PAGINAS_ALOJADA(26),
		NIVEL_LOG(27),
		END_CONFIG (28);
		
		private int index;   

		Conf_Type(int index) {
	        this.index = index;
	    }

	    public int index() { 
	        return index; 
	    }
	}
	public static void setVista(Vista vi)
	{
		v=vi;
	}
	public static Vista getVista()
	{
		return v;
	}
	public static void setCtr(Controlador c)
	{
		ctr=c;
	}
	public static Controlador getCtr()
	{
		return ctr;
	}
}
