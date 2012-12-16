package pckMemoria;

import general.MemoryException;

public interface Cache {
	
	// IMPORTANTE! leerDato, guardarDato, leerLinea y guardarLinea NO comprueban si el dato est� en cach�.
	// Es necesario que la jerarqu�a de memoria compruebe antes si el dato est� en esta cach� o no.
	// En caso de que no est�, la jerarqu�a se encargar� de traerlo desde la memoria principal.
	
	// Comprobaciones
	public boolean existeDato(int direccion);
	public boolean lineaDirty(int direccion) throws MemoryException;
	public boolean lineaLibre(int direccion);
	
	// Tama�o de l�nea.
	public int getTamanoLinea();
	
	// Operaciones para datos individuales.
	public int consultarDato(int direccion) throws MemoryException;
	public void modificarDato(int direccion, int dato) throws MemoryException;
	
	// Operaciones para l�neas (cache)
	public int[] leerLinea(int direccion) throws MemoryException;
	public void escribirLinea(int direccion, int[] linea) throws MemoryException;
	public int[] reemplazarLinea(int direccion, int[] linea) throws MemoryException;
}
