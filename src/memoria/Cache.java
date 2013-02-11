package memoria;

import java.awt.Dimension;
import java.util.List;


import general.MemoryException;
import gui.Tabla;

public interface Cache {
	
	// IMPORTANTE! leerDato, guardarDato, leerLinea y guardarLinea NO comprueban si el dato está en caché.
	// Es necesario que la jerarquía de memoria compruebe antes si el dato está en esta caché o no.
	// En caso de que no esté, la jerarquía se encargará de traerlo desde la memoria principal.
	
	// Comprobaciones
	public boolean existeDato(int direccion);
	//public boolean lineaDirty(int direccion) throws MemoryException;
	public boolean lineaLibre(int direccion);
	
	// Tamaño de línea.
	public int getTamanoLinea();
	
	// Invalida una línea.
	public void invalidarLinea(int direccion);
	public List<LineaReemplazo> invalidarPagina(int pagina_id);
	
	// Operaciones para datos individuales.
	public int consultarDato(int direccion) throws MemoryException;
	public void modificarDato(int direccion, int pagina, int dato) throws MemoryException;
	
	// Operaciones para líneas (cache)
	public int[] leerLinea(int direccion) throws MemoryException;
	public void escribirLinea(int direccion, int pagina, int[] linea) throws MemoryException;
	public void actualizarLinea(int direccion, int pagina, int[] linea);
	public LineaReemplazo reemplazarLinea(int direccion, int pagina, int[] linea) throws MemoryException;
	
	// Funciones para JTable (interfaz gráfica)
	public String[] getColumnas();
	public Object[][] getDatos();
	public Dimension[] getTamaños();
	
	public Tabla getInterfaz();
	public void setInterfaz(Tabla interfaz);
}
