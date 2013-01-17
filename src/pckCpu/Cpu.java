package pckCpu;

import general.Global;
import general.Global.Opcode;
import general.MemoryException;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import pckMemoria.JerarquiaMemoria;

public class Cpu {
	
	private int pc;
	private Alu alu;
	private JerarquiaMemoria jmem;
	private JerarquiaMemoria jinstr;
	private BancoRegistros registros;
	private int direccion_base;
	
	private SortedMap<Integer, Instruccion> instrucciones;
	
	public Cpu(JerarquiaMemoria j1, JerarquiaMemoria j2, int dir_base)
	{
		jmem = j1;
		jinstr = j2;
		alu = new Alu();
		registros = new BancoRegistros();
		instrucciones = new TreeMap<Integer, Instruccion>();
		direccion_base = dir_base;
		
		List<Instruccion> lista = Decoder.getInstrucciones();
		
		// Esto crea un mapa con la direcci�n real de la instrucci�n.
		for (Instruccion inst : lista)
			instrucciones.put(direccion_base + inst.getDireccion(), inst);
	}
	
	// Ejecuta el c�digo (monociclo).
	public void ejecutarCodigoMonociclo() throws MemoryException, CpuException
	{
		boolean terminado = false;
		
		while (!terminado)
		{
			ejecutarInstruccionMonociclo();
			Global.sleep(30000);
		}
	}
	
	// Ejecutar siguiente instrucci�n (monociclo).
	public void ejecutarInstruccionMonociclo() throws MemoryException, CpuException
	{
		System.out.println("Fetch " + getPC());
		/*
		 *  Etapa Fetch
		 */
		Instruccion inst = getInstruccion(getPC());
		if (jinstr != null)
			jinstr.leerDato(getPC());
		else
			jmem.leerDato(getPC());
		
		// PC+4
		incPC();
		
		/*
		 *  Etapa Decode
		 */
		System.out.println("Decode " + inst);
		
		// Leo los 2 registros de origen.
		int dato1 = registros.leerDato(inst.getOrigen1());
		int dato2 = registros.leerDato(inst.getOrigen2());
		
		System.out.println("Dato1 " + dato1);
		System.out.println("Dato2 " + dato2);
		
		/*
		 *  Etapa Execution
		 */
		
		int resultado = alu.ejecutar(inst, dato1, dato2);
		boolean[] flags = alu.getFlags();
		
		System.out.println("resultado alu " + resultado);
		
		/*
		 *  Etapa Memory
		 */
		
		// Lee desde memoria.
		if (inst.getOpcode() == Opcode.LW)
		{
			resultado = jmem.leerDato(resultado);
			System.out.println("resultado memoria " + resultado);
		}
		// Guarda en memoria.
		else if (inst.getOpcode() == Opcode.SW)
		{
			System.out.println("guardo en memoria");
			jmem.guardarDato(resultado, dato1);
		}
		
		
		/*
		 *  Etapa Writeback
		 */
		
		// No hace escritura si es SW o un salto.
		if (inst.getOpcode() != Opcode.SW && !Opcode.esSalto(inst.getOpcode()))
			registros.guardarDato(inst.getDestino(), resultado);
		
		/*
		 * Saltos
		 */
		
		// Comprobamos saltos.
		
		switch (inst.getOpcode())
		{
			// Si es un JAL, guarda PC+4 en $31
			case JAL:
				System.out.println("Guardo PC+4 y modifico PC.");
				registros.guardarDato(31, getPC()+4);
				setPC(calcularSalto(inst.getDireccionSalto()));
				break;
			// Si es un J, modifico PC
			case J:
				System.out.println("Modifico PC.");
				setPC(calcularSalto(inst.getDireccionSalto()));
				break;
			// Si es un JR, modifico PC
			case JR:
				System.out.println("Modifico PC.");
				setPC(dato1);
				break;
			// Si es un BEQ, compruebo el flag zero de Alu antes de saltar.
			case BEQ:
				System.out.println("Compruebo si zero.");
				if (flags[0])
				{
					System.out.println("Modifico PC.");
					setPC(calcularSalto(inst.getDireccionSalto()));
				}
				break;
			// Si es un BNE, compruebo el flag zero de Alu antes de saltar.
			case BNE:
				System.out.println("Compruebo si zero.");
				if (flags[0])
				{
					System.out.println("Modifico PC.");
					setPC(calcularSalto(inst.getDireccionSalto()));
				}
				break;
		}
			
		System.out.println(registros);
	}
	
	// Ejecuta el c�digo.
	public void ejecutarCodigoSegmentado()
	{
		List<Instruccion> lista = Decoder.getInstrucciones();
		
		
	}
	
	// Calcula la direcci�n de salto.
	public int calcularSalto(int dir_relativa)
	{
		return direccion_base + dir_relativa;
	}
	
	// Establece el valor de PC.
	public void setPC(int dir) { pc = dir; }
	public int getPC() { return pc; }
	private void incPC() { pc += 4; }
	
	private Instruccion getInstruccion(int dir)
	{
		return instrucciones.get(dir);
	}
}
