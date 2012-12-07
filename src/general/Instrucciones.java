package general;

public enum Instrucciones {
	
	ADD,	// Suma 2 registros
	ADDI,	// Suma registro con inmediate
	SUB,	// Resta 2 registros
	SUBI,	// Resta registro con inmediate
	LW,		// Carga word en registro
	SW,		// Guarda word de registro a memoria
	AND,	// AND alu
	OR,		// OR alu
	XOR,	// XOR alu
	NOR,	// NOR alu
	SLT,	// Guarda 1 si menor que
	SLTI,	// Guarda 1 si menor que inmediate
	SLL,	// Shift left
	SRL,	// Shift right
	BEQ,	// Salta si iguales
	BNE,	// Salta si distintos
	J,		// Salta
	JR,		// Vuelve a registro ($31)
	JAL,	// Salta y guarda PC+4 en registro ($31)
}
