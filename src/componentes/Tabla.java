package componentes;

import java.awt.Dimension;

import general.Global;

import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import pckMemoria.Cache;
import pckMemoria.MemoriaPrincipal;

@SuppressWarnings("serial")
public class Tabla extends JTable {
	
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
		// Bloqueo la redimensi�n de las columnas.
		tama�oColumnas(this, memoria.getTama�os());
	}
	
	public Tabla(Cache cache)
	{
		super(cache.getDatos(), cache.getColumnas());
		init();
		// Bloqueo la redimensi�n de las columnas.
		tama�oColumnas(this, cache.getTama�os());
	}
	
	// Inicializar
	private void init()
	{
		//setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		setTableHeader(createDefaultTableHeader());
		getTableHeader().setReorderingAllowed(false);
		setOpaque(false);

		// Desactivamos la selecci�n de filas y columnas.
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
	// no aparece el scroll horizontal siempre que deber�a.
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
	
	// Inicializa el tama�o de las columnas dependiendo del tipo de dato.
	public static void tama�oColumnas(JTable tabla, Dimension[] tama�os)
	{
		for (int i = 0; i < tama�os.length; i++)
		{
			TableColumn columna = tabla.getColumnModel().getColumn(i);
			columna.setMinWidth(tama�os[i].width);
			columna.setPreferredWidth(tama�os[i].width);
			if (tama�os[i].height != 0)
				columna.setMaxWidth(tama�os[i].height);
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
