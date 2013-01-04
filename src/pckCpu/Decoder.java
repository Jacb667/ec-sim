package pckCpu;
import java.util.*;

public class Decoder
{
	private SortedMap<String, Instruccion> etiquetas;
	private List<Instruccion> instrucciones;
	private int primera_instruccion;
	private int ultima_instruccion;

	final static public String SEPARADORES_ETIQUETAS = ":";
	final static public String SEPARADORES_PARAMETROS = ",;() \t";
	
	public Decoder(int pos_mem)
	{
		instrucciones = new ArrayList<Instruccion>();
		etiquetas = new TreeMap<String, Instruccion>();
		primera_instruccion = pos_mem;
		ultima_instruccion = pos_mem;
	}
	
	public void decodificarInstruccion(String s, int lin_fich) throws CpuException
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
	public void añadirEtiqueta(String etiq, Instruccion inst)
	{
		etiquetas.put(etiq, inst);
	}
	
	// Obtiene la dirección de la instrucción a la que apunta una etiqueta.
	private int getPosicionEtiqueta(String etiq)
	{
		Instruccion inst = etiquetas.get(etiq);
		if (inst != null)
			return inst.getDireccion();
		
		return -1;
	}
	
	// Valida el código ya decodificado.
	public void validarCodigo() throws CpuException
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
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder("Encontradas " + instrucciones.size() + " instrucciones");
		strB.append("\n");
		for (Instruccion inst : instrucciones)
		{
			strB.append(inst);
		}
		
		return strB.toString();
	}
}
