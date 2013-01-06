package pckCpu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Decoder
{
	private static SortedMap<String, Instruccion> etiquetas = new TreeMap<String, Instruccion>();
	private static List<Instruccion> instrucciones = new ArrayList<Instruccion>();
	private static int primera_instruccion = 0;
	private static int ultima_instruccion = 0;

	final static public String SEPARADORES_ETIQUETAS = ":";
	final static public String SEPARADORES_PARAMETROS = ",;() \t";
	final static public String[] ETIQUETAS_INICIO = new String[]{"INICIO","START","ENTRY"};
	
	// Procesa un archivo de texto en código ensamblador.
	public static boolean decodificarArchivo(String nombre)
	{
		clean();  // Hacemos limpieza siempre antes de abrir un nuevo archivo.
		try
		{
			FileReader fil = new FileReader(nombre);
			BufferedReader br = new BufferedReader(fil);
			
			int i = 1;
			String linea = br.readLine();
			while (linea != null)
			{
				// Las líneas que comienzan con el carácter # se consideran comentarios.
				if (linea.charAt(0) != '#')
					decodificarInstruccion(linea, i);
				linea = br.readLine();
				i++;
			}
			
			// Una vez leido, validamos el código.
			validarCodigo();
			return true;
		}
		catch (IOException e)
		{
			System.err.println("Error en la lectura del archivo.");
			clean();
			return false;
		}
		catch (CpuException e)
		{
			System.err.println(e);
			clean();
			return false;
		}
	}
	
	// Decodifica, crea y añade una instrucción.
	private static void decodificarInstruccion(String s, int lin_fich) throws CpuException
	{
		// La pasamos a mayúsculas y eliminamos espacios en blanco al principio y final (trim).
		String cadena = s.toUpperCase().trim();
		
		// Comprobar si tiene etiqueta.
		StringTokenizer strEtiq = new StringTokenizer(cadena, SEPARADORES_ETIQUETAS);
		if (strEtiq.countTokens() > 2 || strEtiq.countTokens() == 0)
			throw new CpuException("Error en formato de instrucción en línea " + lin_fich);
		
		String etiqueta = null;
		// Si esto ocurre, tenemos una etiqueta.
		if (strEtiq.countTokens() == 2)
			etiqueta = strEtiq.nextToken();

		// En caso contrario tenemos una instrucción normal.
		Instruccion inst = new Instruccion(strEtiq.nextToken(), lin_fich, ultima_instruccion);
		ultima_instruccion += 4;
		
		if (etiqueta != null)
			añadirEtiqueta(etiqueta, inst);
		
		// El tamaño de la lista es la posición donde se insertará.
		instrucciones.add(instrucciones.size(), inst);
	}

	// Añade una etiqueta y su correspondiente instrucción.
	private static void añadirEtiqueta(String etiq, Instruccion inst) throws CpuException
	{
		// Comprobamos si es una etiqueta de inicio.
		boolean es_inicio = false;
		for (int i = 0; i < ETIQUETAS_INICIO.length; i++)
		{
			if (etiq.equalsIgnoreCase(ETIQUETAS_INICIO[i]))
			{
				es_inicio = true;
				break;
			}
		}
		
		// Con esto igualamos las etiquetas de inicio a una sola.
		if (es_inicio)
			etiq = ETIQUETAS_INICIO[0];
		
		if (etiquetas.containsKey(etiq))
		{
			if (es_inicio)
				throw new CpuException("Etiqueta de inicio duplicada.");
			else
				throw new CpuException("Etiqueta " + etiq + " duplicada.");
		}

		etiquetas.put(etiq, inst);
	}
	
	// Obtiene la dirección de la instrucción a la que apunta una etiqueta.
	private static int getPosicionEtiqueta(String etiq)
	{
		Instruccion inst = etiquetas.get(etiq);
		if (inst != null)
			return inst.getDireccion();
		
		return -1;
	}
	
	// Valida el código ya decodificado.
	public static void validarCodigo() throws CpuException
	{
		// Comprobamos todas las instrucciones buscando etiquetas de salto.
		// Para cada etiqueta, comprobamos si existe o no.
		// Además, asignamos a la instrucción la dirección real de salto.
		for (Instruccion inst : instrucciones)
		{
			if (inst.esSalto())
			{
				String etiq = inst.getEtiqueta();
				if (etiq != null)
				{
					int direccion = getPosicionEtiqueta(etiq);
					if (direccion == -1)
						throw new CpuException("Etiqueta no válida " + etiq + " en línea " + inst.getLinea());
		
					inst.setDireccionSalto(direccion);
				}
				// Compruebo si tiene dirección de salto.
				else
				{
					// Le sumo la primera instrucción (ya que es relativa a la primera).
					int direccion = inst.getDireccionSalto() + primera_instruccion;
					
					if (direccion < primera_instruccion || direccion > ultima_instruccion)
						throw new CpuException("Dirección no válida " + direccion + " en línea " + inst.getLinea());
					
					// Asignamos la nueva dirección de salto.
					inst.setDireccionSalto(direccion);
				}
			}
		}
	}
	
	// Devuelve la dirección de la primera instrucción.
	public static int getPrimeraInstruccion()
	{
		int primera = getPosicionEtiqueta(ETIQUETAS_INICIO[0]);
		
		if (primera == -1)
			primera = 0;
		
		return primera;
	}
	
	// Devuelve la lista de instrucciones.
	public static List<Instruccion> getInstrucciones()
	{
		return instrucciones;
	}
	
	// Devuelve información sobre las instrucciones encontradas (para debug).
	public static String getStringInfo()
	{
		StringBuilder strB = new StringBuilder("Encontradas " + instrucciones.size() + " instrucciones");
		strB.append("\n");
		for (Instruccion inst : instrucciones)
			strB.append(inst);
		
		return strB.toString();
	}
	
	// Clean - Hace limpieza de esta clase. Se utiliza para borrar todo después de un error.
	public static void clean()
	{
		etiquetas = new TreeMap<String, Instruccion>();
		instrucciones = new ArrayList<Instruccion>();
		primera_instruccion = 0;
		ultima_instruccion = 0;
	}
}
