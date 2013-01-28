package interfazgrafica;
import general.*;
import general.Config.Conf_Type;
import general.Config.Conf_Type_c;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FileChooserUI;

public class Controlador implements ActionListener {
	
	private Vista v;
	private String ArchivoTraza;
	private String ArchivoCode;
	
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
				ArchivoCode=chooser.getSelectedFile().toString();
				
			}
			//TIENE QUE ACABARSE
			
			v.enabledValidarC();
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
				ArchivoTraza=chooser.getSelectedFile().toString();
				
			}
			//TIENE QUE ACABARSE
			v.enabledValidarT();
		}
		else if(comando.equals(Global.VALC))
		{
			//COMPLETO(?)---TERMINAR PARA EJECUCION
			Config.set(Conf_Type.TAMAÑO_PALABRA,v.getTamPal());
			if(v.jsepCheck())
			{
				Config.set(Conf_Type.JERARQUIAS_SEPARADAS,1);
			}
			else
			{
				Config.set(Conf_Type.JERARQUIAS_SEPARADAS,0);
			}
			Config.set(Conf_Type.SEGMENTADO, v.getSegmentado());
			Config.set(Conf_Type.ENTRADAS_PAGINA,v.getEntradasPagina());
			Config.set(Conf_Type.NUMERO_ENTRADAS_MEMORIA, v.getNumEntradasMem());
			Config.set(Conf_Type.MAXIMA_ENTRADA_MEMORIA,v.getMaxNumEntradas());
			if(v.tlbDataCheck())
			{
				Config.set(Conf_Type.TLB_DATOS, 1);
			}
			else
			{
				Config.set(Conf_Type.TLB_DATOS, 0);
			}
			if(v.tlbInstCheck())
			{
				Config.set(Conf_Type.TLB_INSTRUCCIONES, 1);
			}
			else
			{
				Config.set(Conf_Type.TLB_INSTRUCCIONES, 0);
			}
			 Config.set(Conf_Type.TLB_DATOS_ENTRADAS, v.getTLBDNumEntradas());
			 Config.set(Conf_Type.TLB_DATOS_VIAS, v.getTLBDNumVias());
			 Config.set(Conf_Type_c.TLB_DATOS_POLITICA, v.getPRTLBD());
			 Config.set(Conf_Type.TLB_INSTRUCCIONES_ENTRADAS, v.getTLBINumEntradas());
			 Config.set(Conf_Type.TLB_INSTRUCCIONES_VIAS, v.getTLBINumVias());
			 Config.set(Conf_Type_c.TLB_INSTRUCCIONES_POLITICA, v.getPRTLBI());
			 Config.set(Conf_Type.NIVELES_CACHE_DATOS,v.getnvCache());
			 Config.set(Conf_Type.NIVELES_CACHE_INSTRUCCIONES,v.getnvCacheI());
			 Config.set(Conf_Type.TAMAÑO_LINEA, v.getTamLinea());
			 Config.set(Conf_Type.CACHE1_DATOS_ENTRADAS,v.getCD1NEntradas());
			 Config.set(Conf_Type.CACHE1_DATOS_VIAS,v.getCD1NVias());
			 Config.set(Conf_Type_c.CACHE1_DATOS_POLITICA,v.getPRCD1());
			 Config.set(Conf_Type.CACHE2_DATOS_ENTRADAS,v.getCD2NEntradas());
			 Config.set(Conf_Type.CACHE2_DATOS_VIAS,v.getCD2NVias());
			 Config.set(Conf_Type_c.CACHE2_DATOS_POLITICA,v.getPRCD2());
			 Config.set(Conf_Type.CACHE3_DATOS_ENTRADAS,v.getCD3NEntradas());
			 Config.set(Conf_Type.CACHE3_DATOS_VIAS,v.getCD3NVias());
			 Config.set(Conf_Type_c.CACHE3_DATOS_POLITICA,v.getPRCD3());
			 Config.set(Conf_Type.CACHE1_INSTRUCCIONES_ENTRADAS, v.getCI1NEntradas());
			 Config.set(Conf_Type.CACHE1_INSTRUCCIONES_VIAS,v.getCI1NVias());
			 Config.set(Conf_Type_c.CACHE1_INSTRUCCIONES_POLITICA,v.getPRCI1());
			 Config.set(Conf_Type.CACHE2_INSTRUCCIONES_ENTRADAS, v.getCI2NEntradas());
			 Config.set(Conf_Type.CACHE2_INSTRUCCIONES_VIAS,v.getCI2NVias());
			 Config.set(Conf_Type_c.CACHE2_INSTRUCCIONES_POLITICA,v.getPRCI2());
			 Config.set(Conf_Type.CACHE3_INSTRUCCIONES_ENTRADAS, v.getCI3NEntradas());
			 Config.set(Conf_Type.CACHE3_INSTRUCCIONES_VIAS,v.getCI3NVias());
			 Config.set(Conf_Type_c.CACHE3_INSTRUCCIONES_POLITICA,v.getPRCI3());
			 Config.set(Conf_Type_c.ARCHIVO_CODIGO,ArchivoCode);
			
			v.enabledEjecutarC();
		}
		else if(comando.equals(Global.VALT))
		{
			//COMPLETO(?)-----TERMINAR PARA EJECUCION
			Config.set(Conf_Type.TAMAÑO_PALABRA,v.getTamPal());
			if(v.jsepCheck())
			{
				Config.set(Conf_Type.JERARQUIAS_SEPARADAS,1);
			}
			else
			{
				Config.set(Conf_Type.JERARQUIAS_SEPARADAS,0);
			}
			Config.set(Conf_Type.SEGMENTADO, v.getSegmentado());
			Config.set(Conf_Type.ENTRADAS_PAGINA,v.getEntradasPagina());
			Config.set(Conf_Type.NUMERO_ENTRADAS_MEMORIA, v.getNumEntradasMem());
			Config.set(Conf_Type.MAXIMA_ENTRADA_MEMORIA,v.getMaxNumEntradas());
			if(v.tlbDataCheck())
			{
				Config.set(Conf_Type.TLB_DATOS, 1);
			}
			else
			{
				Config.set(Conf_Type.TLB_DATOS, 0);
			}
			if(v.tlbInstCheck())
			{
				Config.set(Conf_Type.TLB_INSTRUCCIONES, 1);
			}
			else
			{
				Config.set(Conf_Type.TLB_INSTRUCCIONES, 0);
			}
			 Config.set(Conf_Type.TLB_DATOS_ENTRADAS, v.getTLBDNumEntradas());
			 Config.set(Conf_Type.TLB_DATOS_VIAS, v.getTLBDNumVias());
			 Config.set(Conf_Type_c.TLB_DATOS_POLITICA, v.getPRTLBD());
			 Config.set(Conf_Type.TLB_INSTRUCCIONES_ENTRADAS, v.getTLBINumEntradas());
			 Config.set(Conf_Type.TLB_INSTRUCCIONES_VIAS, v.getTLBINumVias());
			 Config.set(Conf_Type_c.TLB_INSTRUCCIONES_POLITICA, v.getPRTLBI());
			 Config.set(Conf_Type.NIVELES_CACHE_DATOS,v.getnvCache());
			 Config.set(Conf_Type.NIVELES_CACHE_INSTRUCCIONES,v.getnvCacheI());
			 Config.set(Conf_Type.TAMAÑO_LINEA, v.getTamLinea());
			 Config.set(Conf_Type.CACHE1_DATOS_ENTRADAS,v.getCD1NEntradas());
			 Config.set(Conf_Type.CACHE1_DATOS_VIAS,v.getCD1NVias());
			 Config.set(Conf_Type_c.CACHE1_DATOS_POLITICA,v.getPRCD1());
			 Config.set(Conf_Type.CACHE2_DATOS_ENTRADAS,v.getCD2NEntradas());
			 Config.set(Conf_Type.CACHE2_DATOS_VIAS,v.getCD2NVias());
			 Config.set(Conf_Type_c.CACHE2_DATOS_POLITICA,v.getPRCD2());
			 Config.set(Conf_Type.CACHE3_DATOS_ENTRADAS,v.getCD3NEntradas());
			 Config.set(Conf_Type.CACHE3_DATOS_VIAS,v.getCD3NVias());
			 Config.set(Conf_Type_c.CACHE3_DATOS_POLITICA,v.getPRCD3());
			 Config.set(Conf_Type.CACHE1_INSTRUCCIONES_ENTRADAS, v.getCI1NEntradas());
			 Config.set(Conf_Type.CACHE1_INSTRUCCIONES_VIAS,v.getCI1NVias());
			 Config.set(Conf_Type_c.CACHE1_INSTRUCCIONES_POLITICA,v.getPRCI1());
			 Config.set(Conf_Type.CACHE2_INSTRUCCIONES_ENTRADAS, v.getCI2NEntradas());
			 Config.set(Conf_Type.CACHE2_INSTRUCCIONES_VIAS,v.getCI2NVias());
			 Config.set(Conf_Type_c.CACHE2_INSTRUCCIONES_POLITICA,v.getPRCI2());
			 Config.set(Conf_Type.CACHE3_INSTRUCCIONES_ENTRADAS, v.getCI3NEntradas());
			 Config.set(Conf_Type.CACHE3_INSTRUCCIONES_VIAS,v.getCI3NVias());
			 Config.set(Conf_Type_c.CACHE3_INSTRUCCIONES_POLITICA,v.getPRCI3());
			 Config.set(Conf_Type_c.ARCHIVO_TRAZA,ArchivoTraza);
			 
			 
			v.enabledEjecutarT();
		}
		// A PARTIR DE AQUI LOS BOTONES DE CACHES Y MEM-----------------------------------------------------------------------------------------------
		else if(comando.equals(Global.BCACHED1))
		{
			
		}
		else if(comando.equals(Global.BCACHED2))
		{
			
		}
		else if(comando.equals(Global.BCACHED3))
		{
			
		}
		else if(comando.equals(Global.BCACHEI1))
		{
			
		}
		else if(comando.equals(Global.BCACHEI2))
		{
			
		}
		else if(comando.equals(Global.BCACHEI3))
		{
			
		}
		else if(comando.equals(Global.BMEM))
		{
			
		}
		
	}

}
