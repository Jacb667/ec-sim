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
		ADD("RRR"),				// Suma 2 registros
		ADDI("RRC"),			// Suma registro con inmediate
		SUB("RRR"),				// Resta 2 registros
		SUBI("RRC"),			// Resta registro con inmediate
		LW("RCR"),				// Carga word en registro
		SW("RCR"),				// Guarda word de registro a memoria
		AND("RRR"),				// AND alu
		ANDI("RRC"),			// AND alu inmediate
		OR("RRR"),				// OR alu
		ORI("RRC"),				// OR alu inmediate
		XOR("RRR"),				// XOR alu
		XORI("RRC"),			// XOR alu inmediate
		NOR("RRR"),				// NOR alu
		NORI("RRC"),			// NOR alu inmediate
		//MULT,					// Multiplicacion alu (de momento no)
		//DIV,					// Division alu (de momento no)
		SLT("RRR"),				// Guarda 1 si menor que
		SLTI("RRC"),			// Guarda 1 si menor que inmediate
		SLL("RRC"),				// Shift left
		SRL("RRC"),				// Shift right
		BEQ("RRE"),				// Salta si iguales
		BNE("RRE"),				// Salta si distintos
		J("E","C"),				// Salto
		JR("R"),				// Vuelve a registro ($31)
		JAL("RE", "RC"),		// Salta y guarda PC+4 en registro ($31)
		NOP(""),				// No operation (stall)
		END("");				// Final del programa
		

		/*
		 * Formatos de instrucci�n:
		 * (Cada instrucci�n s�lo soporta un formato).
		 * 		
		 * 		RRR - 3 registros (destino, origen1, origen2).
		 * 		RRC - 2 registros y constante.
		 * 		E	- 1 etiqueta (salto J).
		 * 		C	- Constante para direcci�n de memoria.
		 * 		R	- Registro.
		 * 		RRE	- 2 registros y etiqueta (saltos condicionales).
		 * 		RCR	- Registro, constante y registro (los 2 �ltimos para calcular memoria).
		 * 		RE	- Registro y etiqueta.
		 * 		RC	- Registro y constante (para salto a direcci�n de memoria).
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
}

