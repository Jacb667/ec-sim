package pckMemoria;

public class Direccion {
	
	// Dirección de memoria.
	private int virtual;
	private int real;
	private int pagina;
	
	public Direccion(int vi)
	{
		setVirtual(vi);
	}
	
	public Direccion(int vi, int re, int pa)
	{
		setVirtual(vi);
		setReal(re);
		setPagina(pa);
	}

	public int getVirtual()
	{
		return virtual;
	}
	
	public String getVirtualHex()
	{
		return Integer.toHexString(virtual);
	}

	public void setVirtual(int virtual)
	{
		this.virtual = virtual;
	}

	public int getReal()
	{
		return real;
	}
	
	public String getRealHex()
	{
		return Integer.toHexString(real);
	}

	public void setReal(int real)
	{
		this.real = real;
	}

	public int getPagina()
	{
		return pagina;
	}

	public void setPagina(int pagina)
	{
		this.pagina = pagina;
	}
}
