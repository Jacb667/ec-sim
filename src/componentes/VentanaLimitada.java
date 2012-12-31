package componentes;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

import javax.swing.JFrame;

public class VentanaLimitada extends JFrame {
	
	private static final long serialVersionUID = 1L;

	// Esta ventana está limitada a unas dimensiones mínimas y máximas.
	@Override
	public void paint(Graphics g)
	{
		Dimension d = getSize();
		Dimension m = getMaximumSize();
		boolean resize = d.width > m.width || d.height > m.height;
		d.width = Math.min(m.width, d.width);
		d.height = Math.min(m.height, d.height);
		if (resize)
		{
			Point p = getLocation();
			setVisible(false);
			setSize(d);
			setLocation(p);
			setVisible(true);
		}
		super.paint(g);
	}
}
