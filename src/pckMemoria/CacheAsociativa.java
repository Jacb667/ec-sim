package pckMemoria;

public class CacheAsociativa implements Cache
{

	@Override
	public boolean existeDato(int direccion) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDirty(int direccion) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isValid(int direccion) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getTamanoLinea() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int leerDato(int direccion) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void guardarDato(int direccion, int dato) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] leerLinea(int direccion) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void guardarLinea(int direccion, int[] linea) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString(boolean mostrarTodos) {
		// TODO Auto-generated method stub
		return null;
	}

}
