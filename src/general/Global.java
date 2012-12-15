package general;

// Esta es una clase Global, que puede ser accedida desde todo el proyecto.

public class Global {
	
	final static public int LONGITUD_BITS = 32;
	final static public int TAMAÑO_MEMORIA = 1024;
	final static public int TAMAÑO_BANCO = 32;

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
	
	public enum PoliticasReemplazo
	{
		LRU,		// Menos usado recientemente - Reemplaza el bloque que hace más tiempo que no se ha usado.
		LFU,		// Menos frecuencias de uso - Reemplaza el bloque que se ha usado menos veces.
		FIFO,		// Primero entrar, primero salir - Reemplaza el primer bloque que entró.
		AGING,		// Histórico de usos (32 bits).
		RANDOM		// Aleatorio - Elimina un bloque al azar.
	}
	
	
}

