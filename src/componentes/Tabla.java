package componentes;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
public class Tabla extends JTable {
	
	// Nuestra propia JTable, ya que necesito realizar algunas modificaciones.
	public Tabla(Object[][] data, Object[] columns)
	{
		super(data, columns);
		init();
	}
	
	private void init()
	{
		//setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		setTableHeader(createDefaultTableHeader());
		getTableHeader().setReorderingAllowed(false);
		setOpaque(false);
		
		// Bloqueo la redimensión de las columnas.
		tamañoColumna(0, 100);
		tamañoColumna(1, 100);
		tamañoColumna(2, 50);
		
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
	
	// Esta sirve para que salga el Checkbox en la columna booleana.
	public Class getColumnClass(int c)
	{
        return getValueAt(0, c).getClass();
    }
	
	private void tamañoColumna(int c, int tam)
	{
		TableColumn columna = getColumnModel().getColumn(c);
		columna.setMinWidth(tam);
		//columna.setMaxWidth(tam);
		columna.setPreferredWidth(tam);
	}

}
