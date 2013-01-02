package pckCpu;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class testDecoder {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		try
		{
			FileReader fil = new FileReader("Prueba.txt");
			BufferedReader br = new BufferedReader(fil);
			
			Decoder dec = new Decoder();
			
			int i = 1;
			String linea = br.readLine();
			while (linea != null)
			{
				// Las líneas que comienzan con el carácter # se consideran comentarios.
				if (linea.charAt(0) != '#')
					dec.decodificarInstruccion(linea, i);
				linea = br.readLine();
				i++;
			}
		}
		catch (IOException e)
		{
			System.err.println("Archivo no encontrado.");
		}
		catch (CpuException e)
		{
			System.err.println(e);
		}
	}
}
