package pckMemoria;

import general.Global.TiposReemplazo;
import general.MemoryException;

public class Tlb {
	
	Cache tlb;
	
	// TLB Directa
	public Tlb(int entradas) throws MemoryException
	{
		try
		{
			tlb = new CacheDirecta(entradas, 1);
		}
		catch (MemoryException e)
		{
			throw new MemoryException("Error al inicializar la TLB.");
		}
	}
	
	// TLB Asociativa
	public Tlb(int entradas, int vias, TiposReemplazo tipo) throws MemoryException
	{
		try
		{
			tlb = new CacheAsociativa(entradas, 1, vias, tipo);
		}
		catch (MemoryException e)
		{
			throw new MemoryException("Error al inicializar la TLB.");
		}
	}
	
	// Existe?
	public boolean existePagina(int pagina)
	{
		if (tlb.existeDato(pagina << 2))
			return true;
		
		return false;
	}
	
	// Insertar entrada
	public void insertar(int pagina, int marco) throws MemoryException
	{
		// La "dirección" se desplaza 2 bits, ya que la caché lo desplaza 2.
		tlb.escribirLinea(pagina << 2, 0, new int[]{marco});
	}
	
	// Consultar entrada
	public int consultar(int pagina) throws MemoryException
	{
		// La "dirección" se desplaza 2 bits, ya que la caché lo desplaza 2.
		return tlb.consultarDato(pagina << 2);
	}
}
