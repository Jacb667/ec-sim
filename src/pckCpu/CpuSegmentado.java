package pckCpu;

import general.Global;
import general.Global.Opcode;
import general.MemoryException;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import pckMemoria.JerarquiaMemoria;

public class CpuSegmentado {
	
	private enum Etapa
	{
		FETCH,
		DECODE,
		EXECUTION,
		MEMORY,
		WRITEBACK,
	}
	
	private int pc;
	private Alu alu;
	private JerarquiaMemoria jmem;
	private JerarquiaMemoria jinstr;
	private BancoRegistros registros;
	private int direccion_base;
	private boolean cortocircuitos;
	private boolean fin_programa;
	private boolean stall;
	
	private RegistroSegmentacion[] registros_segmentacion;
	private RegistroSegmentacion[] registros_temporales;
	
	private SortedMap<Integer, Instruccion> instrucciones;
	
	public CpuSegmentado(JerarquiaMemoria j1, JerarquiaMemoria j2, int dir_base, boolean corts)
	{
		jmem = j1;
		jinstr = j2;
		alu = new Alu();
		registros = new BancoRegistros();
		instrucciones = new TreeMap<Integer, Instruccion>();
		direccion_base = dir_base;
		cortocircuitos = corts;
		
		fin_programa = false;
		stall = false;
		
		registros_segmentacion = new RegistroSegmentacion[5];
		registros_temporales = new RegistroSegmentacion[5];
		
		List<Instruccion> lista = Decoder.getInstrucciones();
		
		// Esto crea un mapa con la dirección real de la instrucción.
		for (Instruccion inst : lista)
		{
			if (inst.esDireccionVirtual())
				instrucciones.put(inst.getDireccion(), inst);
			else
				instrucciones.put(direccion_base + inst.getDireccion(), inst);
		}
	}
	
	// Ejecuta el código (segmentado).
	/*
	 * Tenemos 5 registros de segmentación. En cada paso ejecutamos una acción en cada instrucción, 
	 * enviando al siguiente registro el resultado:
	 * 		Registro 0 -> Siempre Fetch, que se almacena en Registro 1.
	 * 		Registro 1 -> Siempre Decode, que se almacena en Registro 2.
	 * 		Registro 2 -> Siempre Execution, que se almacena en Registro 3.
	 * 		Registro 3 -> Siempre Memory, que se almacena en Registro 4.
	 * 		Registro 4 -> Siempre WriteBack, no se almacena.
	 * 
	 * Para mantener la sincronización, se crean registros temporales, y después de la ejecución reemplazan
	 * al array de registros (como si fuera un ciclo de reloj).
	 */
	public void ejecutarCodigo() throws MemoryException, CpuException
	{
		boolean ejecutando = true;
		
		while (ejecutando)
		{
			ejecutarFetch();
			Global.sleep(10000);
			ejecutarDecode();
			Global.sleep(10000);
			ejecutarExecution();
			Global.sleep(10000);
			ejecutarMemory();
			Global.sleep(10000);
			ejecutarWriteback();
			Global.sleep(10000);
			
			sincronizarRegistros();
		}
		
		System.out.println("Fin de programa.");
	}
	
	private void sincronizarRegistros()
	{
		for (int i = 0; i < 5; i++)
			registros_segmentacion[i] = registros_temporales[i];
	}
	
	// FETCH
	// Lee PC y guarda en el primer registro el resultado del fetch.
	public void ejecutarFetch() throws MemoryException
	{
		System.out.println("Fetch " + getPC());
		
		Instruccion inst = getInstruccion(getPC());
		
		// Fin del programa.
		if (inst.getOpcode() == Opcode.TRAP)
			fin_programa = true;
	
		if (jinstr != null)
			jinstr.leerDato(getPC());
		else
			jmem.leerDato(getPC());
		
		// PC+4
		incPC();
		
		// Fetch siempre ocurre en el paso 0:
		registros_temporales[0] = new RegistroSegmentacion();
		registros_temporales[0].setInstruccion(inst);
	}
		
	// DECODE
	// Lee la instrucción guardada en el registro 0 de antes y lo decodifica.
	public void ejecutarDecode()
	{
		// Decode siempre es la etapa 1.
		Instruccion inst = registros_segmentacion[0].getInstruccion();
		
		System.out.println("Decode " + inst);
		
		// Leo los 2 registros de origen.
		int dato1 = registros.leerDato(inst.getOrigen1());
		int dato2 = registros.leerDato(inst.getOrigen2());
		
		// Se guarda en el registro 1.
		registros_temporales[1] = registros_segmentacion[0];
		registros_temporales[1].setData1(dato1);
		registros_temporales[1].setData2(dato2);
	}
		
	// EXECUTION
	// Leo la instrucción del registro 1 y la ejecuto.
	public void ejecutarExecution()
	{
		// Execution siempre es la etapa 2.
		Instruccion inst = registros_segmentacion[1].getInstruccion();
		int dato1 = registros_segmentacion[1].getData1();
		int dato2 = registros_segmentacion[1].getData2();
		
		int resultado = alu.ejecutar(inst, dato1, dato2);
		boolean[] flags = alu.getFlags();
		
		System.out.println("resultado alu " + resultado);
		
		// Se guarda en el registro 2.
		registros_temporales[2] = registros_segmentacion[1];
		registros_temporales[2].setValor(resultado);
		registros_temporales[2].setFlags(flags);
	}
	
	// MEMORY
	// Leo la instrucción del registro 2 y la ejecuto.
	public void ejecutarMemory() throws MemoryException
	{
		// Memory siempre es la etapa 3.
		Instruccion inst = registros_segmentacion[2].getInstruccion();
		int resultado = registros_segmentacion[2].getValor();
		int dato1 = registros_segmentacion[2].getData1();
		
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
		
		// Se guarda en el registro 3.
		registros_temporales[3] = registros_segmentacion[2];
		registros_temporales[3].setValor(resultado);
	}
	
	// MEMORY
	// Leo la instrucción del registro 3 y la ejecuto.
	public void ejecutarWriteback() throws CpuException
	{
		// Writeback siempre es la etapa 4.
		Instruccion inst = registros_segmentacion[3].getInstruccion();
		int resultado = registros_segmentacion[3].getValor();
		
		// No hace escritura si es SW o un salto.
		if (inst.getOpcode() != Opcode.SW && !Opcode.esSalto(inst.getOpcode()))
			registros.guardarDato(inst.getDestino(), resultado);
		
		// Se guarda en el registro 4.
		registros_temporales[4] = registros_segmentacion[3];
		registros_temporales[4].setValor(resultado);
	}
	
	// Decodificación del salto.
	// Por defecto se ejecuta en MEMORY.
	// Para ejecutarla en EXECUTION hace falta hardware adicional (comparador).
	public void comprobarSaltos() throws CpuException
	{
		Instruccion inst = registros_segmentacion[2].getInstruccion();
		int dato1 = registros_segmentacion[2].getData1();
		boolean[] flags = registros_segmentacion[2].getFlags();
		
		// Comprobamos saltos.
		switch (inst.getOpcode())
		{
			// Si es un JAL, guarda PC+4 en $31
			case JAL:
				System.out.println("Guardo PC+4 y modifico PC.");
				registros.guardarDato(31, getPC()+4);
				if (inst.esDireccionVirtual())
					setPC(inst.getDireccionSalto());
				else
					setPC(calcularSalto(inst.getDireccionSalto()));
				break;
			// Si es un J, modifico PC
			case J:
				System.out.println("Modifico PC.");
				if (inst.esDireccionVirtual())
					setPC(inst.getDireccionSalto());
				else
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
				if (flags[0] == true)
				{
					System.out.println("Modifico PC.");
					if (inst.esDireccionVirtual())
						setPC(inst.getDireccionSalto());
					else
						setPC(calcularSalto(inst.getDireccionSalto()));
				}
				break;
			// Si es un BNE, compruebo el flag zero de Alu antes de saltar.
			case BNE:
				System.out.println("Compruebo si zero.");
				if (flags[0] == false)
				{
					System.out.println("Modifico PC.");
					if (inst.esDireccionVirtual())
						setPC(inst.getDireccionSalto());
					else
						setPC(calcularSalto(inst.getDireccionSalto()));
				}
				break;
		}
		
		System.out.println(registros);
	}
	
	// Calcula la dirección de salto.
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
