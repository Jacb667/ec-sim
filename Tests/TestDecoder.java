package Pruebas;

import pckCpu.Decoder;

public class TestDecoder {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Procesa el archivo y comprueba si todo es correcto.
		// Crea las instrucciones y deja el c�digo listo para ejecutar.
		if (Decoder.decodificarArchivo("Prueba.txt"))
			System.out.println("El c�digo es v�lido");
		
		//System.out.println(Decoder.getStringInfo());
	}
}
