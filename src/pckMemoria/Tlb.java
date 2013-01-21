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
			tlb = new CacheDirecta(entradas, 1, 1);
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
			tlb = new CacheAsociativa(entradas, 1, vias, tipo, 1);
		}
		catch (MemoryException e)
		{
			throw new MemoryException("Error al inicializar la TLB.");
		}
	}
	
	// Existe?
	public boolean existePagina(int pagina)
	{
		if (tlb.existeDato(pagina))
			return true;
		
		return false;
	}
	
	// Insertar entrada
	public void insertar(int pagina, int marco) throws MemoryException
	{
		tlb.escribirLinea(pagina, 0, new int[]{marco});
	}
	
	// Consultar entrada
	public int consultar(int pagina) throws MemoryException
	{
		return tlb.consultarDato(pagina);
	}
}
