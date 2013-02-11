package memoria;

public class Direccion {
	
	// Dirección de memoria.
	private int virtual;
	private int real;
	private int pagina;
	
	public Direccion(int vi, int re, int pa)
	{
		virtual = vi;
		real = re;
		pagina = pa;
	}

	public int getVirtual()
	{
		return virtual;
	}
	
	public String getVirtualHex()
	{
		return Integer.toHexString(virtual);
	}

	public int getReal()
	{
		return real;
	}
	
	public String getRealHex()
	{
		return Integer.toHexString(real);
	}

	public int getPagina()
	{
		return pagina;
	}
}
