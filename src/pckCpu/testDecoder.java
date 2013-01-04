package pckCpu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class testDecoder {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		final int pos_mem = 800;  // Posición de memoria donde inician las instrucciones.
		
		try
		{
			FileReader fil = new FileReader("Prueba.txt");
			BufferedReader br = new BufferedReader(fil);
			
			Decoder dec = new Decoder(pos_mem);
			
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
			
			// Una vez leido, validamos el código.
			dec.validarCodigo();
			System.out.println(dec.toString());
			System.out.println("El código es válido");
		}
		catch (IOException e)
		{
			System.err.println("Error en la lectura del archivo.");
		}
		catch (CpuException e)
		{
			System.err.println(e);
		}
	}
}
