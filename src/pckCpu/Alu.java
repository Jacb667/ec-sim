package pckCpu;

public class Alu
{
	boolean flags[];
	
	public Alu()
	{
		// 2 flags, cero y overflow.
		flags = new boolean[2];
	}
	
	// Ejecuta una instrucción (etapa Alu).
	public int ejecutar(Instruccion inst, int dato1, int dato2)
	{
		long a = dato1;
		long b = dato2;
		long r_long = 0;
		
		int constante = inst.getConstante();
		int res = 0;
		
		switch (inst.getOpcode())
		{
			case ADD:
				// Sumamos y comprobamos overflow.
				r_long = a + b;
				if (r_long > Integer.MAX_VALUE || r_long < Integer.MIN_VALUE)
					flags[1] = true;
				res = (int)r_long;
				break;
			case ADDU:
				// No lanza overflow.
				res = dato1 + dato2;
				break;
			case ADDI:
				// Sumamos y comprobamos overflow.
				r_long = a + constante;
				if (r_long > Integer.MAX_VALUE || r_long < Integer.MIN_VALUE)
					flags[1] = true;
				res = (int)r_long;
				break;
			case ADDIU:
				// No lanza overflow.
				res = dato1 + constante;
				break;
			case SUB:
				// Restamos y comprobamos overflow.
				r_long = a - b;
				if (r_long > Integer.MAX_VALUE || r_long < Integer.MIN_VALUE)
					flags[1] = true;
				res = (int)r_long;
				break;
			case SUBU:
				// No lanza overflow.
				res = dato1 - dato2;
				break;
			case LW:
			case SW:
				// Calcula la dirección.
				res = constante + dato2;
				break;
			case AND:
				res = dato1 & dato2;
				break;
			case ANDI:
				res = dato1 & constante;
				break;
			case OR:
				res = dato1 | dato2;
				break;
			case ORI:
				res = dato1 | constante;
				break;
			case XOR:
				res = dato1 ^ dato2;
				break;
			case XORI:
				res = dato1 ^ constante;
			case NOR:
				res = ~(dato1 | dato2);
				break;
			case NORI:
				res = ~(dato1 | constante);
				break;
			case SLT:
				if (dato1 < dato2)
					res = 1;
				else
					res = 0;
				break;
			case SLTI:
				if (dato1 < constante)
					res = 1;
				else
					res = 0;
				break;
			case SLTU:
				if (Math.abs(dato1) < Math.abs(dato2))
					res = 1;
				else
					res = 0;
				break;
			case SLL:
				res = dato1 << constante;
				break;
			case SRL:
				res = dato1 >>> constante;
				break;
			case SRA:
				res = dato1 >> constante;
				break;
			case BEQ:
				// Si son iguales devuelve 0.
				if (dato1 == dato2)
					res = 0;
				else
					res = 1;
				break;
			case BNE:
				// Si son distintos devuelve 0.
				if (dato1 != dato2)
					res = 0;
				else
					res = 1;
				break;
			case J:
			case JR:
			case JAL:
				// No utilizan la ALU.
				break;
		}
		
		
		// Flag zero.
		if (res == 0)
			flags[0] = true;
		
		return res;
	}
	
	public boolean[] getFlags() { return flags; }
}
