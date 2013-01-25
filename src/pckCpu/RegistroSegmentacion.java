package pckCpu;

public class RegistroSegmentacion {
	
	private Instruccion instruccion;
	private int data1;
	private int data2;
	private int destino;
	private int valor;
	private int[] flags;
	
	public RegistroSegmentacion() { }

	public Instruccion getInstruccion() { return instruccion; }
	public int getData1() { return data1; }
	public int getData2() { return data2; }
	public int getDestino() { return destino; }
	public int getValor() { return valor; }
	public int[] getFlags() { return flags; }

	public void setInstruccion(Instruccion instruccion) { this.instruccion = instruccion; }
	public void setData1(int data1) { this.data1 = data1; }
	public void setData2(int data2) { this.data2 = data2; }
	public void setDestino(int destino) { this.destino = destino; }
	public void setValor(int valor) { this.valor = valor; }
	public void setFlags(int[] flags) { this.flags = flags; }

}
