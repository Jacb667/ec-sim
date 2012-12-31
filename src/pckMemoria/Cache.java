package pckMemoria;

import java.awt.Dimension;

import componentes.Tabla;

import general.MemoryException;

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
	
	// Operaciones para datos individuales.
	public int consultarDato(int direccion) throws MemoryException;
	public void modificarDato(int direccion, int dato) throws MemoryException;
	
	// Operaciones para líneas (cache)
	public int[] leerLinea(int direccion) throws MemoryException;
	public void escribirLinea(int direccion, int[] linea) throws MemoryException;
	public void actualizarLinea(int direccion, int[] linea);
	public LineaReemplazo reemplazarLinea(int direccion, int[] linea) throws MemoryException;
	
	// Funciones para JTable (interfaz gráfica)
	public String[] getColumnas();
	public Object[][] getDatos();
	public Dimension[] getTamaños();
	
	public Tabla getInterfaz();
	public void setInterfaz(Tabla interfaz);
}
