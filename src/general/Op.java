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
		// Logaritmo de i en base 2. Redondeado siempre hacia arriba (si
		// sobrepasa una �nica posici�n, ya es necesario un bit m�s para
		// direccionar).
		return (int) Math.ceil((Math.log(i) / Math.log(2)));
	}
	
	
}
