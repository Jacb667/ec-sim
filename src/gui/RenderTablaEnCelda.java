package gui;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

public class RenderTablaEnCelda implements TableCellRenderer {
	
	/* Magic Happens */
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
	{
		if (value == null)
			return null;
		
		/* If what we're displaying isn't an array of values we
		return the normal renderer*/
		if (!value.getClass().isArray())
		{
			return table.getDefaultRenderer(
					value.getClass()).getTableCellRendererComponent(
							table, value, isSelected, hasFocus,row, column);
		}
		else
		{
			final Object[] passed = (Object[])value;

			// Obtenemos el tamaño actual de esta línea.
			int alturaNormal = table.getRowHeight();
			int alturaActual = table.getRowHeight(row);
			int alturaNueva = passed.length * alturaNormal;
			
			if (alturaActual < alturaNueva)
				table.setRowHeight(row, alturaNueva);
			
			/* We create the table that will hold the multivalue
			 *fields and that will be embedded in the main table */
			return new JTable( new AbstractTableModel() 
			{
				private static final long serialVersionUID = 1L;
				public int getColumnCount() { return 1; }
				public int getRowCount() { return passed.length; }
				public Object getValueAt(int rowIndex, int columnIndex) {
					return passed[rowIndex]; }
				public boolean isCellEditable(int row, int col){ return false; }
				public Class<? extends Object> getColumnClass(int c)
				{
					Object valor = getValueAt(0, c);
					if (valor != null)
						return valor.getClass();
					else
						return new String().getClass();
			    }
			});
		}
    }
}