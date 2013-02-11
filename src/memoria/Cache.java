package memoria;

import java.awt.Dimension;
import java.util.List;


import general.MemoryException;
import gui.Tabla;

public interface Cache {
	
	// IMPORTANTE! leerDato, guardarDato, leerLinea y guardarLinea NO comprueban si el dato est� en cach�.
	// Es necesario que la jerarqu�a de memoria compruebe antes si el dato est� en esta cach� o no.
	// En caso de que no est�, la jerarqu�a se encargar� de traerlo desde la memoria principal.
	
	// Comprobaciones
	public boolean existeDato(int direccion);
	//public boolean lineaDirty(int direccion) throws MemoryException;
	public boolean lineaLibre(int direccion);
	
	// Tama�o de l�nea.
	public int getTamanoLinea();
	
	// Invalida una l�nea.
	public void invalidarLinea(int direccion);
	public List<LineaReemplazo> invalidarPagina(int pagina_id);
	
	// Operaciones para datos individuales.
	public int consultarDato(int direccion) throws MemoryException;
	public void modificarDato(int direccion, int pagina, int dato) throws MemoryException;
	
	// Operaciones para l�neas (cache)
	public int[] leerLinea(int direccion) throws MemoryException;
	public void escribirLinea(int direccion, int pagina, int[] linea) throws MemoryException;
	public void actualizarLinea(int direccion, int pagina, int[] linea);
	public LineaReemplazo reemplazarLinea(int direccion, int pagina, int[] linea) throws MemoryException;
	
	// Funciones para JTable (interfaz gr�fica)
	public String[] getColumnas();
	public Object[][] getDatos();
	public Dimension[] getTama�os();
	
	public Tabla getInterfaz();
	public void setInterfaz(Tabla interfaz);
}
