package pckCpu;

public class BancoRegistros {
	
	private int datos[];
	
	
	public BancoRegistros()
	{
		// 32 registros, desde 0 hasta 31
		datos = new int[general.Constants.TAMAÑO_BANCO];
		datos[0] = 0;
	}
	
	public int leerDato(int direccion)
	{
		return datos[direccion];
	}
	
	public void guardarDato(int direccion, int dato)
	{
		if (direccion == 0)
			return;
		
		datos[direccion] = dato;
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < datos.length; i++)
		{
			// Dirección (hex) : Dato (dec)
			strB.append(String.format("0x%2S", Integer.toHexString(i << 2)).replace(" ", "0")).append(" : ").append(datos[i]).append("\n");
		}
		
		return strB.toString();
	}

}
