
public class Registros {
	public final static int PRED=32;
	
	private int[] registros;
	
	public Registros(int tam)
	{
		registros=new int[tam];
	}
	public Registros()
	{
		this(PRED);
	}
	public int get(int n)
	{
		return registros[n];
	}
	public void mod(int n, int dat)
	{
		registros[n]=dat;
	}
}
