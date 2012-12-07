package pckMemoria;

public interface InterfaceMemoria {
	
	public boolean existeDato(int direccion);
	public int leerDato(int direccion);
	public void guardarDato(int direccion, int dato);
	public int[] leerLinea(int direccion, int tam_linea);
	public void guardarLinea(int direccion, int[] linea);
	public String toString(boolean mostrarTodos);

}
