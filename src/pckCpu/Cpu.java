package pckCpu;

import general.Config;
import general.Config.Conf_Type;
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
	
	private SortedMap<Integer, Instruccion> instrucciones;
	
	public Cpu(JerarquiaMemoria j1, JerarquiaMemoria j2)
	{
		jmem = j1;
		jinstr = j2;
		alu = new Alu();
		registros = new BancoRegistros();
		instrucciones = new TreeMap<Integer, Instruccion>();
		
		List<Instruccion> lista = Decoder.getInstrucciones();
		int direccion_base = Config.get(Conf_Type.INICIO_INSTRUCCIONES);
		
		// Esto crea un mapa con la dirección real de la instrucción.
		for (Instruccion inst : lista)
			instrucciones.put(direccion_base+inst.getDireccion(), inst);
	}
	
	// Ejecuta el código (monociclo).
	public void ejecutarCodigoMonociclo() throws InterruptedException, MemoryException, CpuException
	{
		boolean terminado = false;
		
		while (!terminado)
		{
			ejecutarInstruccionMonociclo();
			//Thread.sleep(30000);
		}
	}
	
	// Ejecutar siguiente instrucción (monociclo).
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
				setPC(inst.getDireccionSalto());
				break;
			// Si es un J, modifico PC
			case J:
				System.out.println("Modifico PC.");
				setPC(inst.getDireccionSalto());
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
					setPC(inst.getDireccionSalto());
				}
				break;
			// Si es un BNE, compruebo el flag zero de Alu antes de saltar.
			case BNE:
				System.out.println("Compruebo si zero.");
				if (flags[0])
				{
					System.out.println("Modifico PC.");
					setPC(inst.getDireccionSalto());
				}
				break;
		}
			
		System.out.println(registros);
	}
	
	// Ejecuta el código.
	public void ejecutarCodigoSegmentado()
	{
		List<Instruccion> instrucciones = Decoder.getInstrucciones();
		
		
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
