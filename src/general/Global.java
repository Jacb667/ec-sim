package general;

// Esta es una clase Global, que puede ser accedida desde todo el proyecto.

public class Global {
	
	// Propiedades CPU
	final static public int LONGITUD_BITS = 32;
	final static public int TAMA�O_MEMORIA = 1024;
	final static public int TAMA�O_BANCO = 32;
	
	// Propiedades tablas interfaz
	final static public int TAMA�O_CELDA_NORMAL = 60;
	final static public int TAMA�O_CELDA_BOOLEAN = 40;

	
	final static public String CBNCACHE = "Niveles de cache";

	public enum Opcode
	{
		ADD("DRR"),				// Suma 2 registros
		ADDI("DRC"),			// Suma registro con inmediate
		SUB("DRR"),				// Resta 2 registros
		SUBI("DRC"),			// Resta registro con inmediate
		LW("DCR"),				// Carga word en registro
		SW("RCR"),				// Guarda word de registro a memoria
		AND("DRR"),				// AND alu
		ANDI("DRC"),			// AND alu inmediate
		OR("DRR"),				// OR alu
		ORI("DRC"),				// OR alu inmediate
		XOR("DRR"),				// XOR alu
		XORI("DRC"),			// XOR alu inmediate
		NOR("DRR"),				// NOR alu
		NORI("DRC"),			// NOR alu inmediate
		SLT("DRR"),				// Guarda 1 si menor que
		SLTI("DRC"),			// Guarda 1 si menor que inmediate
		SLL("DRC"),				// Shift left
		SRL("DRC"),				// Shift right
		BEQ("RRE","RRJ"),		// Salta si iguales
		BNE("RRE","RRJ"),		// Salta si distintos
		J("E","J"),				// Salto
		JR("R"),				// Vuelve a registro ($31)
		JAL("DE", "DJ"),		// Salta y guarda PC+4 en registro ($31)
		NOP(""),				// No operation (stall)
		END("");				// Final del programa
		

		/*
		 * Formatos de instrucci�n:
		 * (Cada instrucci�n s�lo soporta un formato).
		 * 		
		 * 		DRR - 3 registros (destino, origen1, origen2).
		 * 		DRC - 2 registros y constante.
		 * 		E	- 1 etiqueta (salto J).
		 * 		R	- Registro.
		 * 		J	- Constante es direcci�n de salto.
		 * 		RRE	- 2 registros y etiqueta (saltos condicionales).
		 * 		RRJ - 2 registros y constante (saltos condicionales).
		 * 		RCR	- Registro, constante y registro (los 2 �ltimos para calcular memoria).
		 * 		DCR	- Registro, constante y registro (los 2 �ltimos para calcular memoria).
		 * 		DE	- Registro y etiqueta.
		 * 		DJ	- Registro y constante (para salto a direcci�n de memoria).
		 * 
		 * 		
		 */
		
		private String formato1;
		private String formato2;

		Opcode(String t1)
		{
			formato1 = t1;
			formato2 = null;
		}

		Opcode(String t1, String t2)
		{
			formato1 = t1;
			formato2 = t2;
		}

		public String getFormato1() { return formato1; }
		public String getFormato2() { return formato2; }
		public String toString() { return name(); }
		
		public static Opcode find(String name)
		{
		    for (Opcode op : Opcode.values())
		    {
		        if (name.equalsIgnoreCase(op.toString()))
		        	return op;
		    }
		    return null;
		}
		
		// Devuelve true si la instrucci�n es un salto.
		public static boolean esSalto(Opcode opcode)
		{
			switch(opcode)
			{
				case BEQ:
				case BNE:
				case J:
				case JR:
				case JAL:
					return true;
			}
			return false;
		}
		
		// Devuelve true si la instrucci�n es de memoria.
		public static boolean esMemoria(Opcode opcode)
		{
			switch(opcode)
			{
				case SW:
				case LW:
					return true;
			}
			return false;
		}
	}
	
	public enum TiposReemplazo
	{
		LRU,		// Menos usado recientemente - Reemplaza el bloque que hace m�s tiempo que no se ha usado.
		LFU,		// Menos frecuencias de uso - Reemplaza el bloque que se ha usado menos veces.
		FIFO,		// Primero entrar, primero salir - Reemplaza el primer bloque que entr�.
		AGING,		// Hist�rico de usos (32 bits).
		RANDOM		// Aleatorio - Elimina un bloque al azar.
	}
	
	// Devuelve el n�mero de bits necesarios para representar el n�mero i
	public static int bitsNecesarios(int i)
	{
		// Convierto el n�mero a binario y cuento sus d�gitos
		String aux = Integer.toBinaryString(i);
		return aux.length();
	}
	
	// Devuelve el n�mero de bits necesarios para direccionar i posiciones
	public static int bitsDireccionar(int i)
	{
		// Logaritmo de i en base 2. Redondeado siempre hacia arriba (si
		// sobrepasa una �nica posici�n, ya es necesario un bit m�s para
		// direccionar).
		return (int) Math.ceil((Math.log(i) / Math.log(2)));
	}
	
	// Devuelve true/false si la cadena es num�rica o no.
	public static boolean esNumero(String s)
	{
		if (s == null || s.isEmpty())
			return false;
		
		int i = 0;
		
		// Por si empieza en negativo.
		if (s.charAt(0) == '-')
		{
			if (s.length() > 1)
				i++;
			else
				return false;
		}
		
		// Recorremos a partir de i.
		for (; i < s.length(); i++)
		{
			if (!Character.isDigit(s.charAt(i)))
				return false;
		}
		
		return true;
	}
}

