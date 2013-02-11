package memoria;

import java.util.Arrays;

public class LineaReemplazo {
	
	private int[] lin;
	private int dir;
	private int pag;
	private boolean dirty;
	
	// Clase auxiliar para devolver l�neas con la direcci�n.
	public LineaReemplazo(int direccion, int pagina, int[] linea, boolean dirty)
	{
		lin = linea;
		dir = direccion;
		pag = pagina;
	}

	public int[] getLinea()
	{
		return lin;
	}

	public void setLinea(int[] linea)
	{
		lin = linea;
	}

	public int getDireccion()
	{
		return dir;
	}

	public void setDireccion(int direccion)
	{
		dir = direccion;
	}
	
	public String toString()
	{
		return "Dir: " + dir + " - " + Arrays.toString(lin);
	}

	public int getPagina()
	{
		return pag;
	}

	public void setPagina(int pagina)
	{
		pag = pagina;
	}

	public boolean isDirty()
	{
		return dirty;
	}
}
