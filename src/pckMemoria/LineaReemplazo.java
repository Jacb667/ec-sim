package pckMemoria;

import java.util.Arrays;

public class LineaReemplazo {
	
	private int[] lin;
	private int dir;
	
	// Clase auxiliar para devolver líneas con la dirección.
	public LineaReemplazo(int direccion, int[] linea)
	{
		lin = linea;
		dir = direccion;
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
}
