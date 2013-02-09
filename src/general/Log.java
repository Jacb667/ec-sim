package general;

import java.awt.Color;

import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import general.Config.Conf_Type;

public class Log {
	
	// Esta clase almacena y procesa información estadística del programa.
	public static int[] cache_hits = new int[3];
	public static int[] cache_misses = new int[3];
	public static int[] cache_conflicts = new int[3];
	
	public static int[] cache_hits_f = new int[3];			// Para Fetch
	public static int[] cache_misses_f = new int[3];		// Para Fetch
	public static int[] cache_conflicts_f = new int[3];		// Para Fetch
	
	public static int accesosBloques = 0;
	public static int lecturasBloques = 0;
	public static int escriturasBloques = 0;
	
	public static int accesosMemoria = 0;
	public static int lecturasMemoria = 0;
	public static int escriturasMemoria = 0;
	
	public static int accesosPagina = 0;
	public static int fallosPagina = 0;
	public static int aciertosPagina = 0;
	public static int conflictosPagina = 0;
	
	public static int accesosTlb = 0;
	public static int fallosTlb = 0;
	public static int aciertosTlb = 0;
	public static int conflictosTlb = 0;
	public static int accesosTlb_f = 0;		// Para Fetch
	public static int fallosTlb_f = 0;		// Para Fetch
	public static int aciertosTlb_f = 0;	// Para Fetch
	public static int conflictosTlb_f = 0;	// Para Fetch
	
	public static int accesosTablaPaginas = 0;
	
	// Flags por defecto para logs
	public enum Flags
	{
		MEMORY_WRITE,
		MEMORY_READ,

		BLOCK_READ,
		BLOCK_WRITE,

		PAGE_FAULT,
		PAGE_HIT,

		TLB_HIT,
		TLB_MISS,
		CONFLICT_TLB,
		
		TLB_HIT_F,
		TLB_MISS_F,
		CONFLICT_TLB_F,
		
		CONFLICT_PAGE,
		ACCESS_PT;
	}
	
	// Flags con campo Data adicional.
	public enum FlagsD
	{
		CACHE_HIT,
		CACHE_MISS,
		CONFLICT_CACHE,
		
		CACHE_HIT_F,
		CACHE_MISS_F,
		CONFLICT_CACHE_F,
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
	
	public static void generarEstadistica()
	{
		int nivel_compartido = Config.get(Conf_Type.NIVEL_JERARQUIAS_SEPARADAS);
		boolean j_separadas = nivel_compartido > 1;
		
		/*// Mayor que 1 significa que las jerarquías están separadas.
		if (Config.get(Conf_Type.NIVEL_JERARQUIAS_SEPARADAS) > 1)
		{
			println(1, "");
			println(1, "------------------");
			println(1, "Jerarquía de datos");
		}*/
		
		// Calculo las estadísticas de la caché de datos (o compartida).
		float ratio_l0 = (float)(Log.cache_hits[0]*100) / (float)(Log.accesosMemoria);
		float ratio_l1 = (float)(Log.cache_hits[1]*100) / (float)(Log.accesosMemoria-Log.cache_hits[0]);
		float ratio_l2 = (float)(Log.cache_hits[2]*100) / (float)(Log.accesosMemoria-Log.cache_hits[0]-Log.cache_hits[1]);
		
		// Estadísticas de memoria (siempre globales).
		println(1, "Accesos a memoria: " + Log.accesosMemoria + " (" + 
				Log.lecturasMemoria + " lecturas + " + Log.escriturasMemoria + " escrituras)");
		println(1, "Accesos a bloques: " + Log.accesosBloques + " (" + 
				Log.lecturasBloques + " leidos + " + Log.escriturasBloques + " escritos)");
		println(1, "Accesos a tabla de páginas: " + Log.accesosTablaPaginas);
		
		int nivel_datos = Config.get(Conf_Type.NIVELES_CACHE_DATOS);
		
		// TLB de Datos (o compartida).
		if (Config.get(Conf_Type.TLB_DATOS) == 1)
		{
			if (nivel_datos > 1)
				println(1, "DTLB -> " + Log.aciertosTlb + " Hits - " + Log.fallosTlb + " Miss (" + Log.conflictosTlb + ")");
			else
				println(1, "TLB -> " + Log.aciertosTlb + " Hits - " + Log.fallosTlb + " Miss (" + Log.conflictosTlb + ")");
		}
		
		// TLB de Instrucciones.
		if (Config.get(Conf_Type.TLB_INSTRUCCIONES) == 1)
		{
			println(1, "ITLB -> " + Log.aciertosTlb_f + " Hits - " + Log.fallosTlb_f + " Miss (" + Log.conflictosTlb_f + ")");
		}
		
		// Niveles de caché de Datos (o compartida).
		if (nivel_datos > 0)
		{
			if (nivel_compartido <= 1)
			{
				println(1, "Cache Compartida L1 -> " + Log.cache_hits[0] + " Hits - " + (Log.cache_misses[0]) + " Miss (" + Log.cache_conflicts[0] + ")");
				println(1, String.format("%.2f%%", ratio_l0));
			}
			else
			{
				println(1, "Cache Datos L1 -> " + Log.cache_hits[0] + " Hits - " + (Log.cache_misses[0]) + " Miss (" + Log.cache_conflicts[0] + ")");
				println(1, String.format("%.2f%%", ratio_l0));
			}
		}
		if (nivel_datos > 1)
		{
			if (nivel_compartido <= 2)
			{
				println(1, "Cache Compartida L2 -> " + Log.cache_hits[1] + " Hits - " + Log.cache_misses[1] + " Miss (" + Log.cache_conflicts[1] + ")");
				println(1, String.format("%.2f%%", ratio_l1));
			}
			else
			{
				println(1, "Cache Datos L2 -> " + Log.cache_hits[1] + " Hits - " + Log.cache_misses[1] + " Miss (" + Log.cache_conflicts[1] + ")");
				println(1, String.format("%.2f%%", ratio_l1));
			}
		}
		if (nivel_datos > 2)
		{
			if (nivel_compartido <= 3)
			{
				println(1, "Cache Compartida L3 -> " + Log.cache_hits[2] + " Hits - " + Log.cache_misses[2] + " Miss (" + Log.cache_conflicts[2] + ")");
				println(1, String.format("%.2f%%", ratio_l2));
			}
			else
			{
				println(1, "Cache Datos L3 -> " + Log.cache_hits[2] + " Hits - " + Log.cache_misses[2] + " Miss (" + Log.cache_conflicts[2] + ")");
				println(1, String.format("%.2f%%", ratio_l2));
			}
		}
		
		println(1, "Páginas -> " + Log.accesosPagina + " Accesos - " + Log.aciertosPagina + " Hits - " + Log.fallosPagina + " Faults (" + Log.conflictosPagina + ")");
		
		/*if (Config.get(Conf_Type.NIVEL_JERARQUIAS_SEPARADAS) > 1)
		{
			println(1, "");
			println(1, "--------------------------");
			println(1, "Jerarquía de instrucciones");
			
			
			ratio_l0 = (float)(Log.cache_hits1[0]*100) / (float)(Log.accesosMemoria1);
			ratio_l1 = (float)(Log.cache_hits1[1]*100) / (float)(Log.accesosMemoria1-Log.cache_hits1[0]);
			ratio_l2 = (float)(Log.cache_hits1[2]*100) / (float)(Log.accesosMemoria1-Log.cache_hits1[0]-Log.cache_hits1[1]);
			
			println(1,"Accesos a memoria: " + Log.accesosMemoria1 + " (" + 
					Log.lecturasMemoria1 + " lecturas + " + Log.escriturasMemoria1 + " escrituras)");
			println(1,"Accesos a bloques: " + Log.accesosBloques1 + " (" + 
					Log.lecturasBloques1 + " leidos + " + Log.escriturasBloques1 + " escritos)");
			
			nivel_datos = Config.get(Conf_Type.NIVELES_CACHE_INSTRUCCIONES);
			
			if (nivel_datos > 0)
			{
				println(1, "Cache L0 -> " + Log.cache_hits1[0] + " Hits - " + (Log.cache_misses1[0]) + " Miss (" + Log.cache_conflicts1[0] + ")");
				println(1, String.format("%.2f%%", ratio_l0));
			}
			if (nivel_datos > 1)
			{
				println(1, "Cache L1 -> " + Log.cache_hits1[1] + " Hits - " + Log.cache_misses1[1] + " Miss (" + Log.cache_conflicts1[1] + ")");
				println(1, String.format("%.2f%%", ratio_l1));
			}
			if (nivel_datos > 2)
			{
				println(1, "Cache L2 -> " + Log.cache_hits1[2] + " Hits - " + Log.cache_misses1[2] + " Miss (" + Log.cache_conflicts1[2] + ")");
				println(1, String.format("%.2f%%", ratio_l2));
			}
			
			println(1, "Páginas -> " + Log.accesosPagina1 + " Accesos - " + Log.aciertosPagina1 + " Hits - " + Log.fallosPagina1 + " Faults (" + Log.conflictosPagina1 + ")");
			
			if (Config.get(Conf_Type.TLB_INSTRUCCIONES) == 1)
				println(1, "ITLB -> " + Log.aciertosTlb1 + " Hits - " + Log.fallosTlb1 + " Miss (" + Log.conflictosTlb1 + ")");
		}*/
	}
	
	public static void report(FlagsD flag, int data)
	{
		switch(flag)
		{
			case CACHE_HIT:
				cache_hits[data]++;
				break;
			case CACHE_MISS:
				cache_misses[data]++;
				break;
			case CONFLICT_CACHE:
				cache_conflicts[data]++;
				break;
			case CACHE_HIT_F:
				cache_hits_f[data]++;
				break;
			case CACHE_MISS_F:
				cache_misses_f[data]++;
				break;
			case CONFLICT_CACHE_F:
				cache_conflicts_f[data]++;
				break;
		}
	}
	
	public static void report(Flags flag)
	{
		switch(flag)
		{
			case MEMORY_READ:
				lecturasMemoria++;
				accesosMemoria++;
				break;
			case MEMORY_WRITE:
				escriturasMemoria++;
				accesosMemoria++;
				break;
			case BLOCK_READ:
				lecturasBloques++;
				accesosBloques++;
				break;
			case BLOCK_WRITE:
				escriturasBloques++;
				accesosBloques++;
				break;
			case PAGE_FAULT:
				fallosPagina++;
				accesosPagina++;
				break;
			case PAGE_HIT:
				aciertosPagina++;
				accesosPagina++;
				break;
			case TLB_MISS:
				fallosTlb++;
				accesosTlb++;
				break;
			case TLB_HIT:
				aciertosTlb++;
				accesosTlb++;
				break;
			case CONFLICT_TLB:
				conflictosTlb++;
				break;
			case TLB_MISS_F:
				fallosTlb_f++;
				accesosTlb_f++;
				break;
			case TLB_HIT_F:
				aciertosTlb_f++;
				accesosTlb_f++;
				break;
			case CONFLICT_TLB_F:
				conflictosTlb_f++;
				break;
			case CONFLICT_PAGE:
				conflictosPagina++;
				break;
			case ACCESS_PT:
				accesosTablaPaginas++;
		}
	}
	
	public static void println(int n, String s)
	{
		// Filtramos el nivel de log.
		if (n <= nivel)
		{
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			if(Config.getVista()!=null)
			{
				if (Config.ejecutando_codigo)
					Config.getVista().resEjec(s+"\n", null);
				else
					Config.getVista().resTraza(s+"\n", null);
			}
			System.out.println(s);
		}
	}
	
	public static void println(int n, String s, Color color, boolean negrita)
	{
		// Filtramos el nivel de log.
		if (n <= nivel)
		{
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			if(Config.getVista()!=null)
			{
				SimpleAttributeSet aset = new SimpleAttributeSet(); 
				StyleConstants.setForeground(aset, color);
				StyleConstants.setBold(aset, negrita);
		        
				if (Config.ejecutando_codigo)
					Config.getVista().resEjec(s+"\n", aset);
				else
					Config.getVista().resTraza(s+"\n", aset);
			}
			System.out.println(s);
		}
	}
	
	public static void print(int n, String s)
	{
		// Filtramos el nivel de log.
		if (n <= nivel)
		{
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			if(Config.getVista()!=null)
			{
				if (Config.ejecutando_codigo)
					Config.getVista().resEjec(s, null);
				else
					Config.getVista().resTraza(s, null);
			}
			System.out.println(s);
		}
	}
	
	public static void printSeparador(int n)
	{
		if (n <= nivel)
			// Llamamos al controlador para que muestre el mensaje en el lugar adecuado.
			if(Config.getVista()!=null)
			{
				if (Config.ejecutando_codigo)
					Config.getVista().resEjec("----------------------------------------\n", null);
				else
					Config.getVista().resTraza("----------------------------------------\n", null);
			}
			System.out.println("----------------------------------------\n");
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
