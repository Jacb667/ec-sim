package componentes;

import java.awt.Color;

import javax.swing.CellRendererPane;
import javax.swing.JTable;

public class Tabla extends JTable {
	
	private static final Color ColorA = new Color(241, 245, 250);
	private static final Color ColorB = new Color(0xd9d9d9);
	
	//private static final CellRendererPane CELL_RENDER_PANE = new CellRendererPane();
	
	// Nuestra propia JTable, ya que necesito realizar algunas modificaciones.
	public Tabla(Object[][] data, Object[] columns)
	{
		super(data, columns);
		init();
	}
	
	private void init()
	{
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setTableHeader(createDefaultTableHeader());
		getTableHeader().setReorderingAllowed(false);
		setOpaque(false);
		//setIntercellSpacing(new Dimension(0, 0));
		// turn off grid painting as we'll handle this manually in order to paint
		// grid lines over the entire viewport.
		//setShowGrid(true);
	}
	
	// Ninguna celda es editable por el usuario.
	public boolean isCellEditable(int rowIndex, int vColIndex)
	{ 
		return false;
	}

}
