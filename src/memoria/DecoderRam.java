package memoria;

import general.Config;
import general.MemoryException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class DecoderRam {
	
	private TablaPaginas tablaPags;
	public DecoderRam(TablaPaginas tp)//hay que pasarle la memoria donde se quieren guardar los datos.
	{
		tablaPags = tp;
	}
	
	public void decodeFile(String file) throws IOException, MemoryException
	{
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String linea=br.readLine();
		int l = 1;
		while(linea != null)
		{
			decodeLine(linea,l);
			linea = br.readLine();
			l++;
		}
	}
	public void decodeLine(String l, int lineaN) throws MemoryException
	{
		StringTokenizer st = new StringTokenizer(l," ,.;");
		String s;
		int direccion = 0;
		while (st.hasMoreTokens())
		{
			s = st.nextToken();
			char x = s.charAt(0);
			if(x == ':')
			{
				//Coge la dirección
				try
				{
					direccion = Integer.decode(s.substring(1));
				}
				catch(NumberFormatException ex)
				{
					JOptionPane.showMessageDialog( Config.getVista(), "Dirección inválida en línea " + lineaN, "Carga de archivo de memoria", JOptionPane.ERROR_MESSAGE );
				}
			}
			else
			{
				//introducir el dato en la RAM;
				try
				{
					int dato = Integer.parseInt(s);//S es un dato
					tablaPags.inicializarDatoMemoriaVirtual(direccion, dato);
					System.out.println(direccion + " " + dato);
				}
				catch(NumberFormatException ex)
				{
					JOptionPane.showMessageDialog( Config.getVista(), "Dato inválido en línea " + lineaN, "Carga de archivo de memoria", JOptionPane.ERROR_MESSAGE );
				}
				direccion = direccion+4;//Pasa a la siguiente dirección;
			}
		}
		
	}

}
