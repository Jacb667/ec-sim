package pckCpu;

import java.util.StringTokenizer;

import general.Global;
import general.Global.Opcode;

public class Instruccion {
	
	private int destino;
	private int origen1;
	private int origen2;
	private int constante;
	private String etiqueta;
	private Opcode opcode;
	private boolean modifica;
	private int linea;
	private int direccion;
	private int d_salto;

	public Instruccion(String dato, int lin_fich, int dir) throws CpuException
	{
		StringTokenizer strT1 = new StringTokenizer(dato, Decoder.SEPARADORES_PARAMETROS);
		StringTokenizer strT2 = new StringTokenizer(dato, Decoder.SEPARADORES_PARAMETROS);
		String decode = strT1.nextToken();
		strT2.nextToken();
		modifica = false;
		linea = lin_fich;
		direccion = dir;
		
		try
		{
			opcode = Opcode.valueOf(decode);
			String formato1 = opcode.getFormato1();
			String formato2 = opcode.getFormato2();
			
			// Con que cumpla uno de los 2 ya se considera v�lida.
			if (!decodificar(formato1, strT1) && !decodificar(formato2, strT2))
				throw new CpuException("Error en formato de instrucci�n " + opcode + " en l�nea " + lin_fich);
			
			verificarInstruccion();
		}
		catch (IllegalArgumentException e)
		{
			throw new CpuException("Instrucci�n no soportada en l�nea " + decode + " en l�nea " + lin_fich);
		}
	}
	
	// Verificar si la instrucci�n intenta una operaci�n no permitida.
	private void verificarInstruccion() throws CpuException
	{
		// Compruebo que los registros de lectura est�n entre 0 y 31.
		if (origen1 < 0 || origen1 > 31)
			throw new CpuException("No se puede acceder al registro " + origen1);
		
		if (origen2 < 0 || origen2 > 31)
			throw new CpuException("No se puede acceder al registro " + origen2);

		if (destino < 0 || destino > 31)
			throw new CpuException("No se puede acceder al registro " + destino);
		
		// El registro de destino (en caso de usarse) no puede ser 0
		if (modifica && destino == 0)
			throw new CpuException("No se puede escribir en el registro 0 reservado.");
		
		// La constante es un campo de 16 bits. (-32768 a 32767)
		if (constante < -32768 || constante > 32767)
			throw new CpuException("La constante no puede superar los 16 bits de longitud.");
		
		// La constante es un campo de 16 bits. (-32768 a 32767)
		if (constante < -32768 || constante > 32767)
			throw new CpuException("La constante no puede superar los 16 bits de longitud.");
	}

	// Comprueba si cumple el formato y decodifica la instrucci�n.
	private boolean decodificar(String formato, StringTokenizer strT)
	{
		if (formato == null)
			return false;
		
		if (strT.countTokens() != formato.length())
			return false;
		
		int[] v;
		if (formato.contains("E"))
			v = new int[formato.length()-1];
		else
			v = new int[formato.length()];
		String etiq = null;

		try
		{
			// Obtenemos car�cter a car�cter el formato de la instrucci�n.
			for(int i = 0; i < formato.length(); i++)
			{
				String token = strT.nextToken();
				switch(formato.charAt(i))
				{
					case 'R':
					case 'D':
						if (token.charAt(0) == '$' || token.charAt(0) == 'R')
							v[i] = Integer.parseInt(token.substring(1));
						else
							return false;
						break;
					case 'C':
					case 'J':
						v[i] = Integer.parseInt(token);
						break;
					case 'E':
						if (token == null || Global.esNumero(token))
							return false;
						etiq = token;
						break;
					default:
						return false;
				}
				
				// Si todo ha ido bien, pasamos cada par�metro a su lugar correcto.
				switch(formato)
				{
					case "DRR":  // Aritm�ticas (2 registros).
						destino = v[0];
						origen1 = v[1];
						origen2 = v[2];
						modifica = true;
						break;
					case "DRC":  // Aritm�ticas (inmediatas).
						destino = v[0];
						origen1 = v[1];
						constante = v[2];
						modifica = true;
						break;
					case "RRE":  // Saltos condicionales.
						origen1 = v[0];
						origen2 = v[1];
						etiqueta = etiq;
						break;
					case "RRJ":  // Saltos condicionales.
						origen1 = v[0];
						origen2 = v[1];
						d_salto = v[2];
						break;
					case "RCR":  // SW
						origen1 = v[0];
						constante = v[1];
						origen2 = v[2];
						break;
					case "DCR":  // LW
						destino = v[0];
						constante = v[1];
						origen2 = v[2];
						modifica = true;
						break;
					case "DE":  // JAL
						destino = v[0];
						etiqueta = etiq;
						modifica = true;
						break;
					case "DJ":  // JAL
						destino = v[0];
						d_salto = v[1];
						modifica = true;
						break;
					case "R":  // JR
						destino = v[0];
						break;
					case "E":  // J
						etiqueta = etiq;
						break;
					case "J":  // J
						d_salto = v[0];
						break;
					default:
						return false;
				}
			}
			
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
	
	// Devuelve true si la instrucci�n es un salto.
	public boolean esSalto()
	{
		return Opcode.esSalto(opcode);
	}
	
	// Devuelve la etiqueta (null si no se utiliza).
	public String getEtiqueta() { return etiqueta; }
	public int getOrigen1() { return origen1; }
	public int getOrigen2() { return origen2; }
	public int getDestino() { return destino; }
	public int getConstante() { return constante; }
	public Opcode getOpcode() { return opcode; }
	public int getLinea() { return linea; }
	public int getDireccion() { return direccion; }
	public int getDireccionSalto() { return d_salto; }
	
	public void setDireccionSalto(int pos)
	{
		d_salto = pos;
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder("Instrucci�n: " + opcode);
		strB.append("\n");
		strB.append("Instrucci�n: " + opcode);
		strB.append("\n");
		strB.append("Destino: " + destino);
		strB.append("\n");
		strB.append("Origen1: " + origen1);
		strB.append("\n");
		strB.append("Origen2: " + origen2);
		strB.append("\n");
		strB.append("Constante: " + constante);
		strB.append("\n");
		strB.append("Etiqueta: " + etiqueta);
		strB.append("\n");
		strB.append("Dir salto: " + d_salto);
		strB.append("\n");
		strB.append("--------------------");
		strB.append("\n");
		
		return strB.toString();
	}
	
	// Codifica la instrucci�n a binario (para guardar en memoria).
	public int codificarBinario()
	{
		
		return 0;
	}

}
