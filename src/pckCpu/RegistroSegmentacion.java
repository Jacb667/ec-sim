package pckCpu;

import general.Global.Etapa;

public class RegistroSegmentacion {
	
	private Instruccion instruccion;
	private int data1;
	private int data2;
	private int valor;
	private boolean calculado;
	private boolean[] flags;
	private Etapa etapa;
	
	public RegistroSegmentacion()
	{
		etapa = Etapa.FETCH;
	}

	public Instruccion getInstruccion() { return instruccion; }
	public int getData1() { return data1; }
	public int getData2() { return data2; }
	public int getDestino() { return instruccion.getDestino(); }
	public int getValor() { return valor; }
	public boolean[] getFlags() { return flags; }
	public boolean isCalculado() { return calculado; }

	public void setInstruccion(Instruccion instruccion) { this.instruccion = instruccion; }
	public void setData1(int data1) { this.data1 = data1; }
	public void setData2(int data2) { this.data2 = data2; }
	public void setValor(int valor) { this.valor = valor; }
	public void setFlags(boolean[] flags) { this.flags = flags; }
	public void setCalculado(boolean calculado) { this.calculado = calculado; }
	
	public Etapa getEtapa() { return etapa; }
	public void setEtapa(Etapa e) { etapa = e; }

}
