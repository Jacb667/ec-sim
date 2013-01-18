package general;

// Esta es una clase Global, que puede ser accedida desde todo el proyecto.

public class Global {
	
	// Propiedades CPU
	final static public int LONGITUD_BITS = 32;
	final static public int TAMAÑO_MEMORIA = 1024;
	final static public int TAMAÑO_BANCO = 32;
	
	// Propiedades tablas interfaz
	final static public int TAMAÑO_CELDA_NORMAL = 60;
	final static public int TAMAÑO_CELDA_BOOLEAN = 40;

	
	final static public String CBNCACHE = "Niveles de cache";

	public enum Opcode
	{
		ADD   (	"DRR",		'R',	0,		0x20	),		// Suma 2 registros
		ADDU  (	"DRR",		'R',	0,		0x21	),		// Suma 2 registros (sin signo)
		ADDI  (	"DRC",		'I',	0x8,	0		),		// Suma registro con inmediate
		ADDIU (	"DRC",		'I',	0x9,	0		),		// Suma registro con inmediate (sin signo)
		SUB   (	"DRR",		'R',	0,		0x22	),		// Resta 2 registros
		SUBU  (	"DRR",		'R',	0,		0x23	),		// Resta 2 registros (sin signo)
		LW    (	"DCR",		'I',	0x23,	0		),		// Carga word en registro
		SW    (	"RCR",		'I',	0x2B,	0		),		// Guarda word de registro a memoria
		AND   (	"DRR",		'R',	0,		0x24	),		// AND alu
		ANDI  (	"DRC",		'I',	0xC,	0		),		// AND alu inmediate
		OR    (	"DRR",		'R',	0,		0x25	),		// OR alu
		ORI   (	"DRC",		'I',	0xD,	0		),		// OR alu inmediate
		XOR   (	"DRR",		'R',	0,		0x26	),		// XOR alu
		XORI  (	"DRC",		'I',	0xE,	0		),		// XOR alu inmediate
		NOR   (	"DRR",		'R',	0,		0x27	),		// NOR alu
		NORI  (	"DRC",		'I',	0xF,	0		),		// NOR alu inmediate
		SLT   (	"DRR",		'R',	0,		0x2A	),		// Guarda 1 si menor que
		SLTU  (	"DRR",		'R',	0,		0x2B	),		// Guarda 1 si menor que (sin signo)
		SLTI  (	"DRC",		'I',	0xA,	0		),		// Guarda 1 si menor que inmediate
		SLL   (	"DRC",		'R',	0,		0		),		// Shift left (<<)
		SRL   (	"DRC",		'R',	0,		0x2		),		// Shift right (>>>)
		SRA   (	"DRC",		'R',	0,		0x3		),		// Desplazamiento aritmético (>>)
		BEQ   (	"RRE","RRJ",'I',	0x4,	0		),		// Salta si iguales
		BNE   (	"RRE","RRJ",'I',	0x5,	0		),		// Salta si distintos
		J     (	"E","J",	'J',	0x2,	0		),		// Salto
		JR    (	"R",		'R',	0,		0x8		),		// Vuelve a registro ($31)
		JAL   (	"E", "J",	'J',	0x3,	0		),		// Salta y guarda PC+4 en registro ($31)
		TRAP  ( "",			'I',	0x0,	0x0		),		// Fin del programa
		;

		/*
		 * Formatos de instrucción:
		 * (Cada instrucción sólo soporta un formato).
		 * 		
		 * 		DRR - 3 registros (destino, origen1, origen2).
		 * 		DRC - 2 registros y constante.
		 * 		E	- 1 etiqueta (salto J).
		 * 		R	- Registro.
		 * 		J	- Constante es dirección de salto.
		 * 		RRE	- 2 registros y etiqueta (saltos condicionales).
		 * 		RRJ - 2 registros y constante (saltos condicionales).
		 * 		RCR	- Registro, constante y registro (los 2 últimos para calcular memoria).
		 * 		DCR	- Registro, constante y registro (los 2 últimos para calcular memoria).
		 * 		
		 */
		
		private String formato1;
		private String formato2;
		private int codigo;
		private int funcion;
		private char formatoInst;

		Opcode(String f1, char tipo, int op, int fun)
		{
			formato1 = f1;
			formato2 = null;
			formatoInst = tipo;
			codigo = op;
			funcion = fun;
		}

		Opcode(String f1, String f2, char tipo, int op, int fun)
		{
			formato1 = f1;
			formato2 = f2;
			formatoInst = tipo;
			codigo = op;
			funcion = fun;
		}

		public String getFormato1() { return formato1; }
		public String getFormato2() { return formato2; }
		public int getCodigo() { return codigo; }
		public int getFuncion() { return funcion; }
		public char formatoInst() { return formatoInst; }
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
		
		// Devuelve true si la instrucción es un salto.
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
		
		// Devuelve true si la instrucción es de memoria.
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
		
		// Devuelve true si es de tipo "sin signo"
		public static boolean esSinSigno(Opcode opcode)
		{
			switch(opcode)
			{
				case ADDU:
				case ADDIU:
				case SUBU:
				case SLTU:
					return true;
			}
			return false;
		}
		
		// Devuelve true si es un tipo de desplazamiento
		public static boolean esDesplazamiento(Opcode opcode)
		{
			switch(opcode)
			{
				case SLL:
				case SRL:
				case SRA:
					return true;
			}
			return false;
		}
	}
	
	public enum TiposReemplazo
	{
		LRU,		// Menos usado recientemente - Reemplaza el bloque que hace más tiempo que no se ha usado.
		LFU,		// Menos frecuencias de uso - Reemplaza el bloque que se ha usado menos veces.
		FIFO,		// Primero entrar, primero salir - Reemplaza el primer bloque que entró.
		AGING,		// Histórico de usos (32 bits).
		RANDOM		// Aleatorio - Elimina un bloque al azar.
	}
	
	// Devuelve el número de bits necesarios para representar el número i
	public static int bitsNecesarios(int i)
	{
		// Convierto el número a binario y cuento sus dígitos
		String aux = Integer.toBinaryString(i);
		return aux.length();
	}
	
	// Devuelve el número de bits necesarios para direccionar i posiciones
	public static int bitsDireccionar(int i)
	{
		// Logaritmo de i en base 2. Redondeado siempre hacia arriba (si
		// sobrepasa una única posición, ya es necesario un bit más para
		// direccionar).
		return (int) Math.ceil((Math.log(i) / Math.log(2)));
	}
	
	// Devuelve true/false si la cadena es numérica o no.
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
	
	// Sleep.
	public static void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}
}

