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

	public enum Intrucciones
	{
		ADD,	// Suma 2 registros
		ADDI,	// Suma registro con inmediate
		SUB,	// Resta 2 registros
		SUBI,	// Resta registro con inmediate
		LW,		// Carga word en registro
		SW,		// Guarda word de registro a memoria
		AND,	// AND alu
		ANDI,	// AND alu inmediate
		OR,		// OR alu
		ORI,	// OR alu inmediate
		XOR,	// XOR alu
		XORI,	// XOR alu inmediate
		NOR,	// NOR alu
		NORI,	// NOR alu inmediate
		MULT,	// Multiplicacion alu
		DIV,	// Division alu
		SLT,	// Guarda 1 si menor que
		SLTI,	// Guarda 1 si menor que inmediate
		SLL,	// Shift left
		SRL,	// Shift right
		BEQ,	// Salta si iguales
		BNE,	// Salta si distintos
		J,		// Salta
		JR,		// Vuelve a registro ($31)
		JAL,	// Salta y guarda PC+4 en registro ($31)
		NOP,	// No operation (stall)
		END,	// Final del programa
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

