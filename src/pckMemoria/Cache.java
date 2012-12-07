package pckMemoria;

public interface Cache {
	
	// IMPORTANTE! leerDato, guardarDato, leerLinea y guardarLinea NO comprueban si el dato est� en cach�.
	// Es necesario que la jerarqu�a de memoria compruebe antes si el dato est� en esta cach� o no.
	// En caso de que no est�, la jerarqu�a se encargar� de traerlo desde la memoria principal.
	
	// Comprobaciones
	public boolean existeDato(int direccion);
	public boolean isDirty(int direccion);
	
	// Tama�o de l�nea.
	public int getTamanoLinea();
	
	// Operaciones para datos individuales.
	public int leerDato(int direccion);
	public void guardarDato(int direccion, int dato, boolean setDirty);
	
	// Operaciones para l�neas (cache)
	public int[] leerLinea(int direccion);
	public void guardarLinea(int direccion, int[] linea, boolean setDirty);
}
