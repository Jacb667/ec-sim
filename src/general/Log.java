package general;

public class Log {
	
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
