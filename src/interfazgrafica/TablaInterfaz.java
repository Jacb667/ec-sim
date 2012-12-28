package interfazgrafica;

import javax.swing.JTable;

import pckMemoria.Cache;
import pckMemoria.MemoriaPrincipal;

public class TablaInterfaz {
	
	private MemoriaPrincipal memoria;
	private Cache cache;
	private boolean t;
	
	public TablaInterfaz(MemoriaPrincipal mem)
	{
		t = false;
		memoria = mem;
	}
	
	public TablaInterfaz(Cache c)
	{
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
		String[] columnas = memoria.getColumnas();
		String[][] datos = memoria.getDatos();
		
		JTable jt = new JTable(datos, columnas) {
			public boolean isCellEditable(int rowIndex, int vColIndex) { return false; }
			};
		jt.setFillsViewportHeight(true);
		
		jt.setRowSelectionAllowed(false);
		jt.setCellSelectionEnabled(false);
		jt.setColumnSelectionAllowed(false);
		
		return jt;
	}
	
	private JTable crearTablaCache()
	{
		return null;
	}

}
