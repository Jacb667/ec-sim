package interfazgrafica;
import general.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Controlador implements ActionListener {
	
	private Vista v;
	
	public Controlador(Vista vista)
	{
		v=vista;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String comando = e.getActionCommand();
		
		if(comando.equals(Global.CBNCACHE))
		{
			int x=Integer.parseInt(v.getnvCache());
			v.nvCache(x);
		}
		
	}

}
