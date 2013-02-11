package memoria;

import general.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

public class DecoderRam {
	
	private int dato;//dato a guardar
	private int direccion;//dirección de memoria a guardar
	private BufferedReader br;//buffer reader
	private FileReader fr;//File reader
	private MemoriaPrincipal memoria;
	public DecoderRam(MemoriaPrincipal mem)//hay que pasarle la memoria donde se quieren guardar los datos.
	{
		memoria=mem;
		direccion=0;
		dato=0;
	}
	
	public void decodeFile(File file) throws IOException
	{
		fr=new FileReader(file);
		br=new BufferedReader(fr);
		String linea=br.readLine();
		while(linea!=null)
		{
			decodeLine(linea);
			linea=br.readLine();
		}
	}
	public void decodeLine(String l)
	{
		
		StringTokenizer st= new StringTokenizer(l," ,.; '\n'");
		String s;
		while(st.hasMoreTokens())
		{
			s=st.nextToken();
			char x=s.charAt(0);
			if(":".equals(x))
			{
				//Coge la dirección
				StringTokenizer st2=new StringTokenizer(s,":");
				try
				{
					direccion=Integer.parseInt(st2.nextToken());//:Direccion
				}
				catch(NumberFormatException ex)
				{
					JOptionPane.showMessageDialog( Config.getVista(), "Dirección invalida", "Parametro Invalido", JOptionPane.ERROR_MESSAGE );
				}
			}
			else
			{
				//introducir el dato en la RAM;
				try
				{
					dato=Integer.parseInt(s);//S es un dato
					memoria.guardarDato(direccion, dato);
				}
				catch(NumberFormatException ex)
				{
					JOptionPane.showMessageDialog( Config.getVista(), "Dato invalido", "Parametro Invalido", JOptionPane.ERROR_MESSAGE );
				}
				direccion=direccion+4;//Pasa a la siguiente dirección;
			}
		}
		
	}

}
