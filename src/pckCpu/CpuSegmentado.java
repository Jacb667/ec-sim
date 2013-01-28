package pckCpu;

import general.Global;
import general.Global.Etapa;
import general.Global.Opcode;
import general.MemoryException;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import pckMemoria.JerarquiaMemoria;

public class CpuSegmentado implements Cpu {
	
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
		
		int max = 1;
		
		while (ejecutando)
		{
			for(int i = 0; i < max; i++)
			{
				System.out.println(">>Ejecutando " + i);
				ejecutarPaso(i);
			}

			Global.sleep(10000);
			if (max < 5)
				max++;
			
			System.out.println(registros);
		}
		
		System.out.println("Fin de programa.");
	}
	
	private void ejecutarPaso(int i) throws MemoryException, CpuException
	{
		// Si es null, toca Fetch
		if (registros_segmentacion[i] == null)
		{
			if (stall)
				stall = false;
			else
				ejecutarFetch(i);
			
			return;
		}
		
		switch(registros_segmentacion[i].getEtapa())
		{
			case FETCH:
				ejecutarDecode(i);
				break;
			case DECODE:
				ejecutarExecution(i);
				break;
			case EXECUTION:
				comprobarSaltos(i);
				ejecutarMemory(i);
				break;
			case MEMORY:
				ejecutarWriteback(i);
				break;
		}
		
	}
	
	// FETCH
	// Lee PC y guarda en el primer registro el resultado del fetch.
	private void ejecutarFetch(int i) throws MemoryException
	{
		System.out.println("Fetch " + getPC());
		
		Instruccion inst = getInstruccion(getPC());
		System.out.println("Fetch " + inst);
		
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
		registros_segmentacion[i] = new RegistroSegmentacion();
		registros_segmentacion[i].setInstruccion(inst);
	}
		
	// DECODE
	// Lee la instrucción guardada en el registro 0 de antes y lo decodifica.
	private void ejecutarDecode(int i)
	{
		if (registros_segmentacion[i] == null)
			return;
Global.sleep(1000);
		// Decode siempre es la etapa 1.
		Instruccion inst = registros_segmentacion[i].getInstruccion();
System.out.println("Decode " + inst);
		System.out.println("Decode " + inst);
		
		// Leo los 2 registros de origen.
		int dato1 = registros.leerDato(inst.getOrigen1());
		int dato2 = registros.leerDato(inst.getOrigen2());
		
		// Se guarda en el registro de segmentación.
		registros_segmentacion[i].setData1(dato1);
		registros_segmentacion[i].setData2(dato2);
		registros_segmentacion[i].setEtapa(Etapa.DECODE);
	}
		
	// EXECUTION
	// Leo la instrucción del registro 1 y la ejecuto.
	private void ejecutarExecution(int i)
	{
		if (registros_segmentacion[i] == null)
			return;
Global.sleep(1000);
		// Execution siempre es la etapa 2.
		Instruccion inst = registros_segmentacion[i].getInstruccion();
System.out.println("Execution " + inst);
		int dato1 = registros_segmentacion[i].getData1();
		int dato2 = registros_segmentacion[i].getData2();
		
		int resultado = alu.ejecutar(inst, dato1, dato2);
		boolean[] flags = alu.getFlags();
		
		System.out.println("resultado alu " + resultado);
		
		// Se guarda en el registro 2.
		registros_segmentacion[i].setValor(resultado);
		registros_segmentacion[i].setFlags(flags);
		registros_segmentacion[i].setEtapa(Etapa.EXECUTION);
	}
	
	// MEMORY
	// Leo la instrucción del registro 2 y la ejecuto.
	private void ejecutarMemory(int i) throws MemoryException
	{
		if (registros_segmentacion[i] == null)
			return;
Global.sleep(1000);
		// Memory siempre es la etapa 3.
		Instruccion inst = registros_segmentacion[i].getInstruccion();
System.out.println("Memory " + inst);
		int resultado = registros_segmentacion[i].getValor();
		int dato1 = registros_segmentacion[i].getData1();
		
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
		registros_segmentacion[i].setValor(resultado);
		registros_segmentacion[i].setEtapa(Etapa.MEMORY);
	}
	
	// MEMORY
	// Leo la instrucción del registro 3 y la ejecuto.
	private void ejecutarWriteback(int i) throws CpuException
	{
		if (registros_segmentacion[i] == null)
			return;
Global.sleep(1000);
		// Writeback siempre es la etapa 4.
		Instruccion inst = registros_segmentacion[i].getInstruccion();
System.out.println("WriteBack " + inst);
		int resultado = registros_segmentacion[i].getValor();
		
		// No hace escritura si es SW o un salto.
		if (inst.getOpcode() != Opcode.SW && !Opcode.esSalto(inst.getOpcode()))
			registros.guardarDato(inst.getDestino(), resultado);
		
		registros_segmentacion[i] = null;
	}
	
	// Decodificación del salto.
	// Por defecto se ejecuta en MEMORY.
	// Para ejecutarla en EXECUTION hace falta hardware adicional (comparador).
	private void comprobarSaltos(int i) throws CpuException
	{
		if (registros_segmentacion[i] == null)
			return;
Global.sleep(1000);
		Instruccion inst = registros_segmentacion[i].getInstruccion();
System.out.println("Salto " + inst);
		int dato1 = registros_segmentacion[i].getData1();
		boolean[] flags = registros_segmentacion[i].getFlags();
		boolean flush = false;
		
		// Comprobamos saltos.
		switch (inst.getOpcode())
		{
			// Si es un JAL, guarda PC+4 en $31
			case JAL:
				flush = true;
				System.out.println("Guardo PC+4 y modifico PC.");
				registros.guardarDato(31, getPC()+4);
				if (inst.esDireccionVirtual())
					setPC(inst.getDireccionSalto());
				else
					setPC(calcularSalto(inst.getDireccionSalto()));
				break;
			// Si es un J, modifico PC
			case J:
				flush = true;
				System.out.println("Modifico PC.");
				if (inst.esDireccionVirtual())
					setPC(inst.getDireccionSalto());
				else
					setPC(calcularSalto(inst.getDireccionSalto()));
				break;
			// Si es un JR, modifico PC
			case JR:
				flush = true;
				System.out.println("Modifico PC.");
				setPC(dato1);
				break;
			// Si es un BEQ, compruebo el flag zero de Alu antes de saltar.
			case BEQ:
				System.out.println("Compruebo si zero.");
				if (flags[0] == true)
				{
					flush = true;
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
					flush = true;
					System.out.println("Modifico PC.");
					if (inst.esDireccionVirtual())
						setPC(inst.getDireccionSalto());
					else
						setPC(calcularSalto(inst.getDireccionSalto()));
				}
				break;
		}
		
		// Borra todas las instrucciones siguientes a ésta.
		if (flush == true)
		{
			for (int j = 0; j < 5; j++)
			{
				if (registros_segmentacion[j] != null)
				{
					switch(registros_segmentacion[j].getEtapa())
					{
						case FETCH:
						case DECODE:
						case EXECUTION:
							registros_segmentacion[j] = null;
						break;
					}
				}
			}
			flush = false;
		}
	}
	
	// Calcula la dirección de salto.
	private int calcularSalto(int dir_relativa)
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
