package general;


// Operaciones generales
public class Op
{
	// Devuelve el número de bits necesarios para representar el número i
	public static int bitsNecesarios(int i)
	{
		// Convierto el número a binario y cuento sus dígitos
		String aux = Integer.toBinaryString(i);
		return aux.length();
	}
	
	// Devuelve el número de bits necesarios para direccionar i posiciones
	public static int bitsDireccionar(int i)
	{
		// No es necesario direccionar 1 sólo bit, ni menos de 1.
		if (i <= 1)
			return 0;

		// Si el número es par le resto 1 para evitar límites.
		if (i % 2 == 0)
			i -= 1;
		
		String aux = Integer.toBinaryString(i);
		return aux.length();
	}
	
	
}
