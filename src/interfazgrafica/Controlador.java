package interfazgrafica;
import general.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;

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
			v.nvCacheData(v.getnvCache());
		}
		else if(comando.equals(Global.TLBDATA))
		{
			v.enabledTLBData(v.tlbDataCheck());
		}
		else if(comando.equals(Global.TLBINST))
		{
			v.enabledTLBInst(v.tlbInstCheck());
		}
		else if(comando.equals(Global.JSEP))
		{
				v.enabledJSEP(v.jsepCheck());
		}
		else if(comando.equals(Global.CBNCACHEI))
		{
			v.nvCacheInst(v.getnvCacheI());
		}
		else if(comando.equals(Global.CARGARC))
		{
			JFileChooser chooser= new JFileChooser();
			FileNameExtensionFilter filtro = new FileNameExtensionFilter("" +
					"*.txt","txt");
			FileNameExtensionFilter filtro2 = new FileNameExtensionFilter("*.asm","asm");
			chooser.setFileFilter(filtro2);
			chooser.setFileFilter(filtro);
			int returnVal=chooser.showOpenDialog(v);
			if(returnVal==JFileChooser.APPROVE_OPTION)
			{
				System.out.println(chooser.getSelectedFile().toString());
				
			}
		}
		else if(comando.equals(Global.CARGART))
		{
			JFileChooser chooser= new JFileChooser();
			FileNameExtensionFilter filtro = new FileNameExtensionFilter("" +
					"*.txt","txt");
			FileNameExtensionFilter filtro2 = new FileNameExtensionFilter("*.asm","asm");
			chooser.setFileFilter(filtro2);
			chooser.setFileFilter(filtro);
			int returnVal=chooser.showOpenDialog(v);
			if(returnVal==JFileChooser.APPROVE_OPTION)
			{
				System.out.println(chooser.getSelectedFile().toString());
				
			}
		}
		
	}

}
