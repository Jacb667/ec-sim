package general;

import java.util.Arrays;

import pckMemoria.Cache;
import pckMemoria.CacheDirecta;
import pckMemoria.MemoriaPrincipal;

public class Test {
	
	public Test()
	{
	    int num = 182669919;
		int num2 = 0xFFFFFFFF;
		int num3 = 0b111111111111111;
		int num4 = 32768;
		System.out.println(Integer.toHexString(num));
		System.out.printf("0x%hh \n", num);
		
		System.out.println(Integer.toBinaryString(num2));
		
		System.out.println(num2);
		
		System.out.println(num3);
		
		System.out.println("StringFormat: " + String.format("%16s", Integer.toBinaryString(num3)).replace(" ", "0"));
		
		System.out.println("Bits: " + general.Op.bitsNecesarios(num));
		System.out.println("Bits dir: " + general.Op.bitsDireccionar(num4));
		
		// Memoria
		System.out.println("--- Memoria --- \n");
		MemoriaPrincipal m1 = new MemoriaPrincipal(1024, true);
		MemoriaPrincipal m2 = new MemoriaPrincipal(512, true);
		
		for (int i = 0; i < 128; i+=4)
			m1.guardarDato(i, i * 2);
		
		System.out.println(m1.toString(true));
		
		//System.out.println("Leer 0x40,2: " + Arrays.toString(m1.leerLinea(0x40, 2)));
		
		m2.guardarLinea(0, m1.leerLinea(0x40, 12));
		
		System.out.println(m2.toString(true));
		
		Cache c = new CacheDirecta(9, 1);
		for (int i = 0; i < 9*4; i+=4)
			c.guardarDato(i, i * 2, false);
		
		System.out.println(c.toString());
		
		System.out.println(c.leerDato(0x04));
		
		c.guardarLinea(64, m1.leerLinea(0x50, 1), false);
		
		System.out.println(c.toString());
		
		//System.out.println("Leer: " + Arrays.toString(c.leerLinea(0xFF)));
	}
}
