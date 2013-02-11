package gui;
import general.*;
import general.Config.Conf_Type;
import general.Config.Conf_Type_c;
import general.Global.Funcion;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JOptionPane;

import cpu.ClasePrincipal;

public class Controlador implements ActionListener {
	
	private Vista v;
	private BufferedReader br;
	private FileReader fr;
	
	public ClasePrincipal claseP;
	
	public Controlador(Vista vista)
	{
		v=vista;
		Config.setCtr(this);
	}
	
	private final int limMemoriaMostrar = 16384;

	@Override
	public void actionPerformed(ActionEvent e)
	{
		String comando = e.getActionCommand();
		int error = 0;
		
        if(comando.equals(Global.VALC))
		{
			//COMPLETO(?)---TERMINAR PARA EJECUCION
			try
			{
				Config.ejecutando_codigo = true;
				Config.set(Conf_Type.TAMAÑO_PALABRA,v.getTamPal());
				Config.set(Conf_Type.NIVEL_JERARQUIAS_SEPARADAS,v.cacheSepNivel());
				Config.set(Conf_Type.ENTRADAS_PAGINA,v.getEntradasPagina());
				Config.set(Conf_Type.NUMERO_ENTRADAS_MEMORIA, v.getEntradasMemP());
				Config.set(Conf_Type.MAXIMA_ENTRADA_MEMORIA,v.getMaxEntradasVirt());
				Config.set(Conf_Type.TABLA_PAGINAS_ALOJADA,v.tablaPaginasAlojada());
				Config.set(Conf_Type.NIVEL_LOG, v.getNivelLog());
				error = 1;
				
				if (v.getEntradasMemP() > limMemoriaMostrar)
					JOptionPane.showMessageDialog( v, "Aviso, no se puede mostrar la memoria si tiene más de " + limMemoriaMostrar + " entradas (" + ((limMemoriaMostrar * v.getTamPal()) / 1024) + "KB).", "Advertencia", JOptionPane.WARNING_MESSAGE );	
				
				if(v.tlbDataCheck())
					Config.set(Conf_Type.TLB_DATOS, 1);
				else
					Config.set(Conf_Type.TLB_DATOS, 0);
				
				if(v.tlbInstCheck())
					Config.set(Conf_Type.TLB_INSTRUCCIONES, 1);
				else
					Config.set(Conf_Type.TLB_INSTRUCCIONES, 0);
				
				if(v.tlbDataCheck())
				{
					 Config.set(Conf_Type.TLB_DATOS_ENTRADAS, v.getTLBDNumEntradas());
					 Config.set(Conf_Type.TLB_DATOS_VIAS, v.getTLBDNumVias());
					 Config.set(Conf_Type_c.TLB_DATOS_POLITICA, v.getPRTLBD());
				}			 
				if(v.tlbInstCheck())
				{
					 Config.set(Conf_Type.TLB_INSTRUCCIONES_ENTRADAS, v.getTLBINumEntradas());
					 Config.set(Conf_Type.TLB_INSTRUCCIONES_VIAS, v.getTLBINumVias());
					 Config.set(Conf_Type_c.TLB_INSTRUCCIONES_POLITICA, v.getPRTLBI());
				}
				error = 2;

				Config.set(Conf_Type.NIVELES_CACHE_DATOS,v.getnvCacheD());
				Config.set(Conf_Type.NIVELES_CACHE_INSTRUCCIONES,v.getnvCacheI());
				Config.set(Conf_Type.TAMAÑO_LINEA, v.getTamLinea());
				Config.set(Conf_Type.CACHE1_DATOS_ENTRADAS,v.getCD1NEntradas());
				Config.set(Conf_Type.CACHE1_DATOS_VIAS,v.getCD1NVias());
				Config.set(Conf_Type_c.CACHE1_DATOS_POLITICA,v.getPRCD1());
				error = 3;
				
				if(v.getnvCacheD()>=2)
				{
					Config.set(Conf_Type.CACHE2_DATOS_ENTRADAS,v.getCD2NEntradas());
					Config.set(Conf_Type.CACHE2_DATOS_VIAS,v.getCD2NVias());
					Config.set(Conf_Type_c.CACHE2_DATOS_POLITICA,v.getPRCD2());
				}
				error = 4;
				
				if(v.getnvCacheD()==3)
				{
					Config.set(Conf_Type.CACHE3_DATOS_ENTRADAS,v.getCD3NEntradas());
					Config.set(Conf_Type.CACHE3_DATOS_VIAS,v.getCD3NVias());
					Config.set(Conf_Type_c.CACHE3_DATOS_POLITICA,v.getPRCD3());
				}
				error = 5;
			
				if(v.cacheSepNivel() > 1 && v.cacheSepNivel()>1)
				{
					Config.set(Conf_Type.CACHE1_INSTRUCCIONES_ENTRADAS, v.getCI1NEntradas());
					Config.set(Conf_Type.CACHE1_INSTRUCCIONES_VIAS,v.getCI1NVias());
					Config.set(Conf_Type_c.CACHE1_INSTRUCCIONES_POLITICA,v.getPRCI1());
				}
				error = 6;
				
				if(v.cacheSepNivel() > 2 && v.getnvCacheI()>=2)
				{
					Config.set(Conf_Type.CACHE2_INSTRUCCIONES_ENTRADAS, v.getCI2NEntradas());
					Config.set(Conf_Type.CACHE2_INSTRUCCIONES_VIAS,v.getCI2NVias());
					Config.set(Conf_Type_c.CACHE2_INSTRUCCIONES_POLITICA,v.getPRCI2());
				}
				error = 7;
				
				if(v.cacheSepNivel() > 3 && v.getnvCacheI()==3)
				{
					Config.set(Conf_Type.CACHE3_INSTRUCCIONES_ENTRADAS, v.getCI3NEntradas());
					Config.set(Conf_Type.CACHE3_INSTRUCCIONES_VIAS,v.getCI3NVias());
					Config.set(Conf_Type_c.CACHE3_INSTRUCCIONES_POLITICA,v.getPRCI3()); 
				}
				error = 8;

				Config.set(Conf_Type_c.ARCHIVO_CODIGO, v.getArchivoCodigo());
			 
				v.enabledEjecutarC();
				v.enabledConfig(false);
				
				claseP = new ClasePrincipal();
				claseP.setFuncion(Funcion.VALIDAR_CODIGO);
				Thread thread = new Thread(claseP);
				thread.start();
			}
			catch(NumberFormatException e1)
			{
				String mensaje = "";
				switch(error)
				{
					case 0:
						mensaje = "Configuración de memoria no válida.";
						break;
					case 1:
						mensaje = "Configuración de TLB no válida.";
						break;
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
						mensaje = "Configuración de Caché de Datos " + (error-2) + " no válida.";
						break;
					case 8:
						mensaje = "Error de fichero.";
						break;
				}
				
				JOptionPane.showMessageDialog( v, mensaje, "Error de formato", JOptionPane.ERROR_MESSAGE );
				e1.printStackTrace();
			}
		}
		else if(comando.equals(Global.VALT))
		{
			try
			{
				Config.ejecutando_codigo = true;
				Config.set(Conf_Type.TAMAÑO_PALABRA,v.getTamPal());
				Config.set(Conf_Type.NIVEL_JERARQUIAS_SEPARADAS,v.cacheSepNivel());
				Config.set(Conf_Type.ENTRADAS_PAGINA,v.getEntradasPagina());
				Config.set(Conf_Type.NUMERO_ENTRADAS_MEMORIA, v.getEntradasMemP());
				Config.set(Conf_Type.MAXIMA_ENTRADA_MEMORIA,v.getMaxEntradasVirt());
				Config.set(Conf_Type.TABLA_PAGINAS_ALOJADA,v.tablaPaginasAlojada());
				Config.set(Conf_Type.NIVEL_LOG, v.getNivelLog());
				error = 1;
				
				if(v.tlbDataCheck())
					Config.set(Conf_Type.TLB_DATOS, 1);
				else
					Config.set(Conf_Type.TLB_DATOS, 0);
				
				if(v.tlbInstCheck())
					Config.set(Conf_Type.TLB_INSTRUCCIONES, 1);
				else
					Config.set(Conf_Type.TLB_INSTRUCCIONES, 0);
				
				if(v.tlbDataCheck())
				{
					 Config.set(Conf_Type.TLB_DATOS_ENTRADAS, v.getTLBDNumEntradas());
					 Config.set(Conf_Type.TLB_DATOS_VIAS, v.getTLBDNumVias());
					 Config.set(Conf_Type_c.TLB_DATOS_POLITICA, v.getPRTLBD());
				}			 
				if(v.tlbInstCheck())
				{
					 Config.set(Conf_Type.TLB_INSTRUCCIONES_ENTRADAS, v.getTLBINumEntradas());
					 Config.set(Conf_Type.TLB_INSTRUCCIONES_VIAS, v.getTLBINumVias());
					 Config.set(Conf_Type_c.TLB_INSTRUCCIONES_POLITICA, v.getPRTLBI());
				}
				error = 2;

				Config.set(Conf_Type.NIVELES_CACHE_DATOS,v.getnvCacheD());
				Config.set(Conf_Type.NIVELES_CACHE_INSTRUCCIONES,v.getnvCacheI());
				Config.set(Conf_Type.TAMAÑO_LINEA, v.getTamLinea());
				Config.set(Conf_Type.CACHE1_DATOS_ENTRADAS,v.getCD1NEntradas());
				Config.set(Conf_Type.CACHE1_DATOS_VIAS,v.getCD1NVias());
				Config.set(Conf_Type_c.CACHE1_DATOS_POLITICA,v.getPRCD1());
				error = 3;
				
				if(v.getnvCacheD()>=2)
				{
					Config.set(Conf_Type.CACHE2_DATOS_ENTRADAS,v.getCD2NEntradas());
					Config.set(Conf_Type.CACHE2_DATOS_VIAS,v.getCD2NVias());
					Config.set(Conf_Type_c.CACHE2_DATOS_POLITICA,v.getPRCD2());
				}
				error = 4;
				
				if(v.getnvCacheD()==3)
				{
					Config.set(Conf_Type.CACHE3_DATOS_ENTRADAS,v.getCD3NEntradas());
					Config.set(Conf_Type.CACHE3_DATOS_VIAS,v.getCD3NVias());
					Config.set(Conf_Type_c.CACHE3_DATOS_POLITICA,v.getPRCD3());
				}
				error = 5;
			
				if(v.cacheSepNivel() > 1 && v.cacheSepNivel()>1)
				{
					Config.set(Conf_Type.CACHE1_INSTRUCCIONES_ENTRADAS, v.getCI1NEntradas());
					Config.set(Conf_Type.CACHE1_INSTRUCCIONES_VIAS,v.getCI1NVias());
					Config.set(Conf_Type_c.CACHE1_INSTRUCCIONES_POLITICA,v.getPRCI1());
				}
				error = 6;
				
				if(v.cacheSepNivel() > 2 && v.getnvCacheI()>=2)
				{
					Config.set(Conf_Type.CACHE2_INSTRUCCIONES_ENTRADAS, v.getCI2NEntradas());
					Config.set(Conf_Type.CACHE2_INSTRUCCIONES_VIAS,v.getCI2NVias());
					Config.set(Conf_Type_c.CACHE2_INSTRUCCIONES_POLITICA,v.getPRCI2());
				}
				error = 7;
				
				if(v.cacheSepNivel() > 3 && v.getnvCacheI()==3)
				{
					Config.set(Conf_Type.CACHE3_INSTRUCCIONES_ENTRADAS, v.getCI3NEntradas());
					Config.set(Conf_Type.CACHE3_INSTRUCCIONES_VIAS,v.getCI3NVias());
					Config.set(Conf_Type_c.CACHE3_INSTRUCCIONES_POLITICA,v.getPRCI3()); 
				}
				error = 8;

				Config.set(Conf_Type_c.ARCHIVO_CODIGO, v.getArchivoTraza());
			 
				v.enabledEjecutarT();
				v.enabledConfig(false);
				
				claseP = new ClasePrincipal();
				claseP.iniciarTraza();
				//claseP.validarCodigo();
			}
			catch(NumberFormatException e1)
			{
				String mensaje = "";
				switch(error)
				{
					case 0:
						mensaje = "Configuración de memoria no válida.";
						break;
					case 1:
						mensaje = "Configuración de TLB no válida.";
						break;
					case 2:
					case 3:
					case 4:
					case 5:
					case 6:
					case 7:
						mensaje = "Configuración de Caché de Datos " + (error-2) + " no válida.";
						break;
					case 8:
						mensaje = "Error de fichero.";
						break;
				}
				
				JOptionPane.showMessageDialog( v, mensaje, "Error de formato", JOptionPane.ERROR_MESSAGE );
				e1.printStackTrace();
			}
		}
		else if(comando.equals(Global.EJECUTART))
		{
			Config.ejecutando_codigo = false;
			//System.out.println("ENTRA2");
			if (claseP != null)
			{	
				String s=null;
				//System.out.println("ENTRA");
				try {
					s=bfOn();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				claseP.setTraza(s);
				try {
					claseP.ejecutarTraza();
					//v.resTraza(claseP.resTraza());
				} catch (MemoryException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			}
			v.enabledConfig(true);
		}
		else if(comando.equals(Global.EJECUTARC))
		{
			Config.ejecutando_codigo = true;
			if (claseP != null)
			{
				claseP.setFuncion(Funcion.EJECUTAR_CODIGO);
				Thread thread = new Thread(claseP);
				thread.start();
			}
			v.enabledConfig(true);
		}
		else if (comando.equals(Global.CICLO))
		{
			Config.ejecutando_codigo = true;
			if (claseP != null)
				claseP.ejecutarCicloCodigo();
			Config.getVista().setFinEjec();
		}
		// A PARTIR DE AQUI LOS BOTONES DE CACHES Y MEM-----------------------------------------------------------------------------------------------
		else if(comando.equals(Global.BCACHED1))
		{
			if (claseP != null && claseP.framesCache1[0] != null)
				claseP.framesCache1[0].setVisible(true);
		}
		else if(comando.equals(Global.BCACHED2))
		{
			if (claseP != null && claseP.framesCache1[1] != null)
				claseP.framesCache1[1].setVisible(true);
		}
		else if(comando.equals(Global.BCACHED3))
		{
			if (claseP != null && claseP.framesCache1[2] != null)
				claseP.framesCache1[2].setVisible(true);
		}
		else if(comando.equals(Global.BCACHEI1))
		{
			if (claseP != null && claseP.framesCache2[0] != null)
				claseP.framesCache2[0].setVisible(true);
		}
		else if(comando.equals(Global.BCACHEI2))
		{
			if (claseP != null && claseP.framesCache2[1] != null)
				claseP.framesCache2[1].setVisible(true);
		}
		else if(comando.equals(Global.BCACHEI3))
		{
			if (claseP != null && claseP.framesCache2[2] != null)
				claseP.framesCache2[2].setVisible(true);
		}
		else if(comando.equals(Global.BMEM))
		{
			if (claseP != null && claseP.frameMemoria != null)
				claseP.frameMemoria.setVisible(true);
		}
		else if(comando.equals(Global.DETENER))
		{
			if (claseP != null)
				claseP.detener();
		}
		
	}
	private String bfOn() throws IOException
	{
		StringBuilder sb = new StringBuilder("");
		fr=new FileReader(v.getArchivoTraza());
		br=new BufferedReader(fr);
		String aux=br.readLine();
		while(aux!=null)
		{
			sb.append(aux).append("\n");
			aux=br.readLine();
		}
		System.out.println(sb.toString());
		return sb.toString();
		
	}

}
