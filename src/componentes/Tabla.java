package componentes;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import pckMemoria.Cache;
import pckMemoria.MemoriaPrincipal;

@SuppressWarnings("serial")
public class Tabla extends JTable {
	
	private final int TAMAÑO_DEFECTO = 60;
	private final int TAMAÑO_BOOLEAN = 40;
	
	// Nuestra propia JTable, ya que necesito realizar algunas modificaciones.
	public Tabla(Object[][] data, Object[] columns)
	{
		super(data, columns);
		init();
	}
	
	public Tabla(MemoriaPrincipal memoria)
	{
		super(memoria.getDatos(), memoria.getColumnas());
		init();
	}
	
	public Tabla(Cache cache)
	{
		super(cache.getDatos(), cache.getColumnas());
		init();
	}
	
	// Inicializar
	private void init()
	{
		//setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		setTableHeader(createDefaultTableHeader());
		getTableHeader().setReorderingAllowed(false);
		setOpaque(false);
		
		// Bloqueo la redimensión de las columnas.
		tamañoColumnas();
		
		// Desactivamos la selección de filas y columnas.
		setRowSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setColumnSelectionAllowed(false);
	}
	
	// Ninguna celda es editable por el usuario.
	public boolean isCellEditable(int rowIndex, int vColIndex)
	{ 
		return false;
	}
	
	// Esta clase sirve para solucionar un bug en las JTable, que
	// no aparece el scroll horizontal siempre que debería.
	public boolean getScrollableTracksViewportWidth()
	{
		if (autoResizeMode != AUTO_RESIZE_OFF)
		{
			if (getParent() instanceof JViewport)
				return (((JViewport)getParent()).getWidth() > getPreferredSize().width);
		}
		return false;
	}
	
	// Esta sirve para que salga el Checkbox en la columna booleana.
	public Class<? extends Object> getColumnClass(int c)
	{
		Object valor = getValueAt(0, c);
		if (valor != null)
			return valor.getClass();
		else
			return new String().getClass();
    }
	
	// Inicializa el tamaño de las columnas dependiendo del tipo de dato.
	private void tamañoColumnas()
	{
		for (int i = 0; i < getColumnCount(); i++)
		{
			TableColumn columna = getColumnModel().getColumn(i);
			if (getColumnClass(i) == Boolean.class)
			{
				columna.setMinWidth(TAMAÑO_BOOLEAN);
				columna.setMaxWidth(TAMAÑO_BOOLEAN*2);
				columna.setPreferredWidth(TAMAÑO_BOOLEAN);
			}
			else
			{
				columna.setMinWidth(TAMAÑO_DEFECTO);
				columna.setPreferredWidth(TAMAÑO_DEFECTO);
			}
		}
	}
	
	public void setRenderTablaEnCelda()
	{
		TableCellRenderer jTableCellRenderer = new RenderTablaEnCelda();
		
		TableColumnModel tcm = getColumnModel();
		for(int it = 0; it < tcm.getColumnCount(); it++)
		{
			tcm.getColumn(it).setCellRenderer(jTableCellRenderer);
		}
	}
}
