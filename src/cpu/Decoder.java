package cpu;

import general.Config;
import general.Global.Opcode;
import gui.Vista;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import javax.swing.JOptionPane;

public class Decoder
{
	private static SortedMap<String, Instruccion> etiquetas = new TreeMap<String, Instruccion>();
	private static List<Instruccion> instrucciones = new ArrayList<Instruccion>();
	private static int primera_instruccion = 0;
	private static int ultima_instruccion = 0;
	private static boolean tiene_trap = false;
	private static boolean tiene_direccion = false;
	private static int instrucciones_ant = 0;
	private static int primera_dir_v = 0;

	final static public String SEPARADORES_ETIQUETAS = ":";
	final static public String SEPARADORES_PARAMETROS = ",() \t";
	final static public String[] ETIQUETAS_INICIO = new String[]{"INICIO","START","ENTRY"};
	
	// Procesa un archivo de texto en c�digo ensamblador.
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
				String linea_l = linea.trim();
				if (linea_l.length() > 0)
				{
					// L�neas que se consideran comentarios.
					if (linea_l.charAt(0) != '#' && linea_l.charAt(0) != ';' && linea_l.charAt(0) != '/')
					{
						// L�nea que nos indica una direcci�n del c�digo.
						if (linea_l.charAt(0) == ':')
							obtenerDireccion(linea_l, i);
						else
							decodificarInstruccion(linea_l, i);
						
						if (!tiene_direccion)
							instrucciones_ant++;
					}
				}
				linea = br.readLine();
				i++;
			}
			
			// Una vez leido, validamos el c�digo.
			validarCodigo();
			return true;
		}
		catch (IOException e)
		{
			Vista v = Config.getVista();
			if (v == null)
				System.err.println("No se ha podido leer el fichero.");
			else
				JOptionPane.showMessageDialog( v, "No se ha podido leer el fichero.", "Error en la lectura del fichero", JOptionPane.ERROR_MESSAGE );
			clean();
			return false;
		}
		catch (CpuException e)
		{
			Vista v = Config.getVista();
			if (v == null)
				System.err.println(e);
			else
				JOptionPane.showMessageDialog( v, e.getMessage(), "Error en la lectura del fichero", JOptionPane.ERROR_MESSAGE );
			clean();
			e.printStackTrace();
			return false;
		}
	}
	
	// Modifica la direcci�n de instrucci�n.
	private static void obtenerDireccion(String s, int lin_fich) throws CpuException
	{
		try
		{
			ultima_instruccion = Integer.decode(s.substring(1));
			if (!tiene_direccion)
				primera_dir_v = ultima_instruccion;
			tiene_direccion = true;
		}
		catch (NumberFormatException e)
		{
			throw new CpuException("Error en formato de direcci�n en l�nea " + lin_fich);
		}
	}
	
	// Decodifica, crea y a�ade una instrucci�n.
	private static void decodificarInstruccion(String s, int lin_fich) throws CpuException
	{
		// La pasamos a may�sculas y eliminamos espacios en blanco al principio y final (trim).
		String cadena = s.toUpperCase().trim();
		
		// Comprobar si tiene etiqueta.
		StringTokenizer strEtiq = new StringTokenizer(cadena, SEPARADORES_ETIQUETAS);
		if (strEtiq.countTokens() > 2 || strEtiq.countTokens() == 0)
			throw new CpuException("Error en formato de instrucci�n en l�nea " + lin_fich);
		
		String etiqueta = null;
		// Si esto ocurre, tenemos una etiqueta.
		if (strEtiq.countTokens() == 2)
			etiqueta = strEtiq.nextToken();

		// En caso contrario tenemos una instrucci�n normal.
		Instruccion inst = new Instruccion(strEtiq.nextToken(), lin_fich, ultima_instruccion);
		
		if (tiene_direccion)
			inst.setDireccionVirtual();
		
		if (inst.getOpcode() == Opcode.TRAP)
			tiene_trap = true;

		ultima_instruccion += 4;
		
		if (etiqueta != null)
			a�adirEtiqueta(etiqueta, inst);
		
		// El tama�o de la lista es la posici�n donde se insertar�.
		instrucciones.add(instrucciones.size(), inst);
	}

	// A�ade una etiqueta y su correspondiente instrucci�n.
	private static void a�adirEtiqueta(String etiq, Instruccion inst) throws CpuException
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
	
	// Obtiene la direcci�n de la instrucci�n a la que apunta una etiqueta.
	private static int getPosicionEtiqueta(String etiq)
	{
		Instruccion inst = etiquetas.get(etiq);
		if (inst != null)
			return inst.getDireccion();
		
		return -1;
	}
	
	// Valida el c�digo ya decodificado.
	public static void validarCodigo() throws CpuException
	{
		if (!tiene_trap)
			throw new CpuException("No se ha encontrado ninguna instrucci�n TRAP para finalizar.");
		
		// Asignamos direcciones virtuales a todas las instrucciones en caso de que el usuario a�adiera alguna.
		if (tiene_direccion)
		{
			int primera_virtual = primera_dir_v - instrucciones_ant * 4;
			for (int i = 0; i < instrucciones_ant; i++)
			{
				int direccion = primera_virtual + i*4;
				instrucciones.get(i).setDireccionVirtual();
				instrucciones.get(i).setDireccion(direccion);
			}
		}
		
		// Busco direcciones repetidas (varias instrucciones con misma direcci�n).
		for (int i = 0; i < instrucciones.size(); i++)
		{
			Instruccion inst = instrucciones.get(i);
			int dir = inst.getDireccion();
			
			if (existeInstruccionDireccion(dir, i+1))
				throw new CpuException("Existen m�ltiples instrucciones para la direcci�n 0x" + Integer.toHexString(dir));
		}
		
		// Comprobamos todas las instrucciones buscando etiquetas de salto.
		// Para cada etiqueta, comprobamos si existe o no.
		// Adem�s, asignamos a la instrucci�n la direcci�n real de salto.
		for (Instruccion inst : instrucciones)
		{
			if (inst.esSalto() && inst.getOpcode() != Opcode.JR)
			{
				String etiq = inst.getEtiqueta();
				if (etiq != null)
				{
					int direccion = getPosicionEtiqueta(etiq);
					if (direccion == -1)
						throw new CpuException("Etiqueta no v�lida " + etiq + " en l�nea " + inst.getLinea());
		
					inst.setDireccionSalto(direccion);
				}
				// Compruebo si tiene direcci�n de salto.
				else
				{
					// Le sumo la primera instrucci�n (ya que es relativa a la primera).
					int direccion = inst.getDireccionSalto() + primera_instruccion;
					
					if (!existeInstruccionDireccion(direccion, 0))
						throw new CpuException("Direcci�n de salto no v�lida " + direccion + " en l�nea " + inst.getLinea());
					
					// Asignamos la nueva direcci�n de salto.
					inst.setDireccionSalto(direccion);
				}
			}
		}
	}
	
	// Devuelve la direcci�n de la primera instrucci�n.
	public static Instruccion getPrimeraInstruccion()
	{
		Instruccion inst = etiquetas.get(ETIQUETAS_INICIO[0]);
		
		if (inst == null)
			return instrucciones.get(0);
		
		return inst;
	}
	
	// Devuelve la lista de instrucciones.
	public static List<Instruccion> getInstrucciones()
	{
		return instrucciones;
	}
	
	// Devuelve informaci�n sobre las instrucciones encontradas (para debug).
	public static String getStringInfo()
	{
		StringBuilder strB = new StringBuilder("Encontradas " + instrucciones.size() + " instrucciones");
		strB.append("\n");
		for (Instruccion inst : instrucciones)
			strB.append(inst);
		
		return strB.toString();
	}
	
	private static boolean existeInstruccionDireccion(int dir, int comienzo)
	{
		for (int i = comienzo; i < instrucciones.size(); i++)
		{
			if (instrucciones.get(i).getDireccion() == dir)
				return true;
		}
		
		return false;
	}
	
	// Clean - Hace limpieza de esta clase. Se utiliza para borrar todo despu�s de un error.
	public static void clean()
	{
		etiquetas = new TreeMap<String, Instruccion>();
		instrucciones = new ArrayList<Instruccion>();
		primera_instruccion = 0;
		ultima_instruccion = 0;
		tiene_trap = false;
		tiene_direccion = false;
	}
}
