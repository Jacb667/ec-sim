package pckCpu;

public class BancoRegistros {
	
	private int datos[];
	
	
	public BancoRegistros()
	{
		// 32 registros, desde 0 hasta 31
		datos = new int[general.Global.TAMAÑO_BANCO];
		datos[0] = 0;
	}
	
	public int leerDato(int direccion)
	{
		return datos[direccion];
	}
	
	public void guardarDato(int direccion, int dato) throws CpuException
	{
		if (direccion == 0)
			throw new CpuException("Intento de escritura en registro $0.");

		datos[direccion] = dato;
	}
	
	public String toString()
	{
		StringBuilder strB = new StringBuilder();
		for (int i = 0; i < datos.length; i++)
		{
			// Dirección (hex) : Dato (dec)
			strB.append(i);
			strB.append(" : ");
			strB.append(datos[i]);
			strB.append("\n");
		}
		
		return strB.toString();
	}

}
