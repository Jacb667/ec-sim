package general;


// Operaciones generales
public class Op
{
	// Devuelve el n�mero de bits necesarios para representar el n�mero i
	public static int bitsNecesarios(int i)
	{
		// Convierto el n�mero a binario y cuento sus d�gitos
		String aux = Integer.toBinaryString(i);
		return aux.length();
	}
	
	// Devuelve el n�mero de bits necesarios para direccionar i posiciones
	public static int bitsDireccionar(int i)
	{
		// No es necesario direccionar 1 s�lo bit, ni menos de 1.
		if (i <= 1)
			return 0;

		// Si el n�mero es par le resto 1 para evitar l�mites.
		if (i % 2 == 0)
			i -= 1;
		
		String aux = Integer.toBinaryString(i);
		return aux.length();
	}
	
	
}
