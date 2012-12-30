package componentes;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class VentanaOculta extends WindowAdapter {
	
	// Nuestro propio sistema de cierre de ventanas.
	// Este se utilizará para cerrar los frames de memoria/caché
	// para que oculte la ventana y más adelante se pueda volver a abrir.
	
	private JFrame frameAsociado;
	
	public VentanaOculta(JFrame frame)
	{
		super();
		frameAsociado = frame;
	}
	
	@Override
	public void windowClosing(WindowEvent e)
	{
		// Oculta este frame.
		frameAsociado.setVisible(false);
	}
}
