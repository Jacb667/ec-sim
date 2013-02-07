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
	private boolean d_virtual;

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
			
			// Con que cumpla uno de los 2 ya se considera válida.
			if (!decodificar(formato1, strT1) && !decodificar(formato2, strT2))
				throw new CpuException("Error en formato de instrucción " + opcode + " en línea " + lin_fich);
			
			verificarInstruccion();
		}
		catch (IllegalArgumentException e)
		{
			throw new CpuException("Instrucción " + decode + " no soportada  en línea " + lin_fich);
		}
	}
	
	// Verificar si la instrucción intenta una operación no permitida.
	private void verificarInstruccion() throws CpuException
	{
		// Compruebo que los registros de lectura estén entre 0 y 31.
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
		// Sólo si es una instrucción de tipo J, la dirección de salto puede ser de 26 bits.
		if (opcode == Opcode.J)
		{
			if (d_salto < -33554432 || d_salto > 33554431)
				throw new CpuException("La dirección de salto no puede superar los 26 bits de longitud.");
		}
		else
		{
			if (d_salto < -32768 || d_salto > 32767)
				throw new CpuException("La dirección de salto no puede superar los 16 bits de longitud.");
		}
		
		// La constante es un campo de 16 bits. (-32768 a 32767)
		if (constante < -32768 || constante > 32767)
			throw new CpuException("La constante no puede superar los 16 bits de longitud.");
		
		if (Opcode.esDesplazamiento(opcode))
			if (constante < 0 || constante > 31)
				throw new CpuException("No se puede realizar un desplazamiento de " + constante + " bits.");
	}

	// Comprueba si cumple el formato y decodifica la instrucción.
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
			// Obtenemos carácter a carácter el formato de la instrucción.
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
						v[i] = Integer.decode(token);
						break;
					case 'E':
						if (token == null || Global.esNumero(token))
							return false;
						etiq = token;
						break;
					default:
						return false;
				}
				
				// Si todo ha ido bien, pasamos cada parámetro a su lugar correcto.
				switch(formato)
				{
					case "DRR":  // Aritméticas (2 registros).
						destino = v[0];
						origen1 = v[1];
						origen2 = v[2];
						modifica = true;
						break;
					case "DRC":  // Aritméticas (inmediatas).
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
					case "R":  // JR
						origen1 = v[0];
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
	
	// Devuelve true si la instrucción es un salto.
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
	public boolean esDireccionVirtual() { return d_virtual; }
	public boolean modificaDestino() { return modifica; }

	public void setDireccion(int dir)
	{
		direccion = dir;
	}
	
	public void setDireccionSalto(int pos)
	{
		d_salto = pos;
	}
	
	public void setDireccionVirtual()
	{
		d_virtual = true;
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder("[" + opcode);
		strB.append(" D" + destino);
		strB.append(" $" + origen1);
		strB.append(" $" + origen2);
		strB.append(" I" + constante);
		if (etiqueta != null)
			strB.append(" " + etiqueta);
		if (Opcode.esSalto(opcode))
			strB.append(" " + d_salto);
		strB.append("]");
		
		return strB.toString();
	}
	
	// Codifica la instrucción a binario (para guardar en memoria).
	public int codificarBinario()
	{
		int resultado = opcode.getCodigo();
		if (opcode.formatoInst() == 'R')
		{
			resultado = resultado << 5;
			resultado += origen1;
			resultado = resultado << 5;
			resultado += origen2;
			resultado = resultado << 5;
			resultado += destino;
			resultado = resultado << 5;
			if (Opcode.esDesplazamiento(opcode))
				resultado += constante;  // Sólo en desplazamientos.
			else
				resultado += 0;
			resultado = resultado << 6;
			resultado += opcode.getFuncion();
		}
		else if (opcode.formatoInst() == 'I')
		{
			resultado = resultado << 5;
			resultado += origen1;
			resultado = resultado << 5;
			resultado += origen2;
			resultado = resultado << 16;
			resultado += constante;
		}
		else
		{
			resultado = resultado << 26;
			resultado += d_salto;
		}
		
		return resultado;
	}

}
