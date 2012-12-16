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
		// Logaritmo de i en base 2. Redondeado siempre hacia arriba (si
		// sobrepasa una única posición, ya es necesario un bit más para
		// direccionar).
		return (int) Math.ceil((Math.log(i) / Math.log(2)));
	}
	
	
}
