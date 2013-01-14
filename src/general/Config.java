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
		MEMORIAS_SEPARADAS (0),		// Bool: Indica si se separa la memoria de datos y instrucciones o no.
		POLITICA_REEMPLAZO (1),		// Int: Politica de reemplazo de datos en caché.
		TIPO_MOSTRAR (2),			// Int: Tipo de datos a mostrar (bin, oct, dec, hex).
		INICIO_INSTRUCCIONES (3),   // Int: Dirección de inicio de la memoria de instrucciones.
		NUMERO_PAGINAS (4),			// Int: Número de páginas de memoria.
		ENTRADAS_MEMORIA (5),		// Int: Número de entradas de la memoria principal (pd (2^32)/4 = 1073741824).
		
		
		
		
		END_CONFIG (6);
		
		private int index;   

		Conf_Type(int index) {
	        this.index = index;
	    }

	    public int index() { 
	        return index; 
	    }
	}
}
