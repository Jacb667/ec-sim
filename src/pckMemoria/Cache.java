package pckMemoria;

public interface Cache {
	
	// IMPORTANTE! leerDato, guardarDato, leerLinea y guardarLinea NO comprueban si el dato está en caché.
	// Es necesario que la jerarquía de memoria compruebe antes si el dato está en esta caché o no.
	// En caso de que no esté, la jerarquía se encargará de traerlo desde la memoria principal.
	
	// Comprobaciones
	public boolean existeDato(int direccion);
	public boolean lineaDirty(int direccion);
	public boolean lineaLibre(int direccion);
	
	// Tamaño de línea.
	public int getTamanoLinea();
	
	// Operaciones para datos individuales.
	public int consultarDato(int direccion);
	public void modificarDato(int direccion, int dato);
	
	// Operaciones para líneas (cache)
	public int[] leerLinea(int direccion);
	public void escribirLinea(int direccion, int[] linea);
	public int[] reemplazarLinea(int direccion, int[] linea);
}
