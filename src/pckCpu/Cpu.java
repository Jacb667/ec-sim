package pckCpu;

import general.Config;
import general.Config.Conf_Type;

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
		
		for (Instruccion inst : lista)
			instrucciones.put(direccion_base+inst.getDireccion(), inst);
		
		System.out.println(instrucciones.toString());
	}
	
	// Ejecuta el código (monociclo).
	public void ejecutarCodigoMonociclo()
	{
		
		
		
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
	
	
	
	
	
	
}
