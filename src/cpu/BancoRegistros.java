package cpu;

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
		
		for (int i = 0; i < datos.length;)
		{
			strB.append(String.format("%3d", i));
			strB.append(" : ");
			for (int j = 0; j < 8; j++)
			{
				strB.append(String.format("%8d", datos[i]));
				if (j != 7)
					strB.append(", ");
				i++;
			}
			strB.append("\n");
		}
		
		return strB.toString();
	}
}
