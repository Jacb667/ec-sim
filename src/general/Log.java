package general;

public class Log {
	
	// Esta clase almacena y procesa información estadística del programa.
	public static int[] cache_hits = new int[3];
	public static int[] cache_misses = new int[3];
	public static int accesosBloques = 0;
	public static int lecturasBloques = 0;
	public static int escriturasBloques = 0;
	public static int accesosMemoria = 0;
	public static int lecturasMemoria = 0;
	public static int escriturasMemoria = 0;
	
	// Flags por defecto para logs
	public enum Flags
	{
		CACHE_HIT,
		CACHE_MISS,
		MEMORY_WRITE,
		MEMORY_READ,
		BLOCK_READ,
		BLOCK_WRITE,
	}
	
	// 0 = no mostrar nada.
	// 1 = mostrar sólo resultados.
	// 2 = mostrar inicio de operaciones, hits, miss, etc.
	// 3 = más detallado, muestra traza completa de operaciones.
	// 4 = muestra la traza completa de todo.
	private static int nivel = 4;

	public static int getNivel()
	{
		return nivel;
	}

	public static void setNivel(int nivel)
	{
		Log.nivel = nivel;
	}
	
	public static void report(Flags f, int data)
	{
		if (f == Flags.CACHE_HIT)
			cache_hits[data]++;
		else if (f == Flags.CACHE_MISS)
			cache_misses[data]++;
	}
	
	public static void report(Flags f)
	{
		if (f == Flags.MEMORY_READ)
		{
			lecturasMemoria++;
			accesosMemoria++;
		}
		else if (f == Flags.MEMORY_WRITE)
		{
			escriturasMemoria++;
			accesosMemoria++;
		}
		else if (f == Flags.BLOCK_READ)
		{
			lecturasBloques++;
			accesosBloques++;
		}
		else if (f == Flags.BLOCK_WRITE)
		{
			escriturasBloques++;
			accesosBloques++;
		}
	}
	
	public static void println(int n, String s)
	{
		// Filtramos el nivel de log.
		if (n <= nivel)
		{
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			// TODO: De momento hacemos una llamada a System.out.
			System.out.println(s);
		}
	}
	
	public static void print(int n, String s)
	{
		// Filtramos el nivel de log.
		if (n <= nivel)
		{
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			// TODO: De momento hacemos una llamada a System.out.
			System.out.print(s);
		}
	}
	
	public static void errorln(int n, String s)
	{
		// Filtramos el nivel de log.
		if (n <= nivel)
		{
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			// TODO: De momento hacemos una llamada a System.out.
			System.out.println(s);
		}
	}
	
	public static void error(int n, String s)
	{
		// Filtramos el nivel de log.
		if (n <= nivel)
		{
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			// TODO: De momento hacemos una llamada a System.out.
			System.out.print(s);
		}
	}
	
	public static void correctln(int n, String s)
	{
		// Filtramos el nivel de log.
		if (n <= nivel)
		{
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			// TODO: De momento hacemos una llamada a System.out.
			System.out.println(s);
		}
	}
	
	public static void correct(int n, String s)
	{
		// Filtramos el nivel de log.
		if (n <= nivel)
		{
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			// TODO: De momento hacemos una llamada a System.out.
			System.out.print(s);
		}
	}
}
