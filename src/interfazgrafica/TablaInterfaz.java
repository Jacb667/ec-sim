package interfazgrafica;

import javax.swing.JTable;

import pckMemoria.Cache;
import pckMemoria.MemoriaPrincipal;

public class TablaInterfaz extends JTable {
	
	private MemoriaPrincipal memoria;
	private Cache cache;
	private boolean t;
	
	public boolean isCellEditable(int rowIndex, int vColIndex)
	{
		return false;
	}
	
	public TablaInterfaz(MemoriaPrincipal mem)
	{
		
		setFillsViewportHeight(true);
		t = false;
		memoria = mem;
	}
	
	public TablaInterfaz(Cache c)
	{
		
		setFillsViewportHeight(true);
		t = true;
		cache = c;
	}
	
	public JTable crearTabla()
	{
		if (t == false)
			return crearTablaMemoria();
		
		return crearTablaCache();
	}
	
	private JTable crearTablaMemoria()
	{

		

	}
	
	private JTable crearTablaCache()
	{
		return null;
	}

}
