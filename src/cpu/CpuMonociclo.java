package cpu;

import general.Global.Opcode;
import general.Global;
import general.Log;
import general.MemoryException;

import java.awt.Color;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import memoria.JerarquiaMemoria;


public class CpuMonociclo {
	
	private int pc;
	private Alu alu;
	private JerarquiaMemoria jmem;
	private JerarquiaMemoria jinstr;
	private BancoRegistros registros;
	private int direccion_base;
	
	private boolean forzar_detencion;
	
	private SortedMap<Integer, Instruccion> instrucciones;
	
	public CpuMonociclo(JerarquiaMemoria j1, JerarquiaMemoria j2, int dir_base)
	{
		jmem = j1;
		jinstr = j2;
		alu = new Alu();
		registros = new BancoRegistros();
		instrucciones = new TreeMap<Integer, Instruccion>();
		direccion_base = dir_base;
		
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
	
	// Ejecuta el código (monociclo).
	public void ejecutarCodigo() throws MemoryException, CpuException
	{
		boolean ejecutando = true;
		
		while (!forzar_detencion && ejecutando)
		{
			ejecutando = ejecutarInstruccion();
			Global.sleep(5);  // Con esto evitamos que la interfaz se bloquee demasiado.
		}
		
		if (forzar_detencion)
			Log.println(1,"Programa detenido.\n\n", Color.RED, true);
		else
			Log.println(1,"Fin de programa.\n\n", Color.BLACK, true);
	}
	
	// Ejecutar siguiente instrucción (monociclo).
	public boolean ejecutarInstruccion() throws MemoryException, CpuException
	{
		/*
		 *  Etapa Fetch
		 */
		Instruccion inst = getInstruccion(getPC());
		
		// Fin del programa.
		if (inst.getOpcode() == Opcode.TRAP)
			return false;
		
		// Fetch memoria.
		if (jinstr != null)
		{
			Log.println(1, "Fetch 0x" + Integer.toHexString(getPC()), Color.BLACK, true);
			jinstr.leerDato(getPC());
		}
		else
		{
			Log.println(1, "Fetch 0x" + Integer.toHexString(getPC()), Color.BLACK, true);
			jmem.leerDato(getPC());
		}
		
		// PC+4
		incPC();
		Log.printDebug(inst.toString());
		
		/*
		 *  Etapa Decode
		 */
		
		// Leo los 2 registros de origen.
		int dato1 = registros.leerDato(inst.getOrigen1());
		int dato2 = registros.leerDato(inst.getOrigen2());
	
		/*
		 *  Etapa Execution
		 */
		
		int resultado = alu.ejecutar(inst, dato1, dato2);
		boolean[] flags = alu.getFlags();
		
		/*
		 *  Etapa Memory
		 */
		
		// Lee desde memoria.
		if (inst.getOpcode() == Opcode.LW)
		{
			Log.println(1, "Lectura de dirección virtual: 0x" + Integer.toHexString(resultado), Color.BLACK, true);
			resultado = jmem.leerDato(resultado);
		}
		// Guarda en memoria.
		else if (inst.getOpcode() == Opcode.SW)
		{
			Log.println(1, "Guardado de dato [" + dato1 + "] en dirección virtual: 0x" + Integer.toHexString(resultado), Color.BLACK, true);
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
				registros.guardarDato(31, getPC());
				if (inst.esDireccionVirtual())
				{
					Log.printDebug("Guardo PC+4 en $31 y modifico PC = " + inst.getDireccionSalto());
					setPC(inst.getDireccionSalto());
				}
				else
				{
					Log.printDebug("Guardo PC+4 en $31 y modifico PC = " + calcularSalto(inst.getDireccionSalto()));
					setPC(calcularSalto(inst.getDireccionSalto()));
				}
				break;
			// Si es un J, modifico PC
			case J:
				if (inst.esDireccionVirtual())
				{
					Log.printDebug("Modifico PC = " + inst.getDireccionSalto());
					setPC(inst.getDireccionSalto());
				}
				else
				{
					Log.printDebug("Modifico PC = " + calcularSalto(inst.getDireccionSalto()));
					setPC(calcularSalto(inst.getDireccionSalto()));
				}
				break;
			// Si es un JR, modifico PC
			case JR:
				Log.printDebug("Modifico PC = " + dato1);
				setPC(dato1);
				break;
			// Si es un BEQ, compruebo el flag zero de Alu antes de saltar.
			case BEQ:
				if (flags[0] == true)
				{
					if (inst.esDireccionVirtual())
					{
						Log.printDebug("Modifico PC = " + inst.getDireccionSalto());
						setPC(inst.getDireccionSalto());
					}
					else
					{
						Log.printDebug("Modifico PC = " + calcularSalto(inst.getDireccionSalto()));
						setPC(calcularSalto(inst.getDireccionSalto()));
					}
				}
				break;
			// Si es un BNE, compruebo el flag zero de Alu antes de saltar.
			case BNE:
				if (flags[0] == false)
				{
					if (inst.esDireccionVirtual())
					{
						Log.printDebug("Modifico PC = " + inst.getDireccionSalto());
						setPC(inst.getDireccionSalto());
					}
					else
					{
						Log.printDebug("Modifico PC = " + calcularSalto(inst.getDireccionSalto()));
						setPC(calcularSalto(inst.getDireccionSalto()));
					}
				}
				break;
		}
		
		//System.out.println(registros);
		return true;
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
	
	public void detener()
	{
		forzar_detencion = true;
	}
}
