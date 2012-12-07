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
		
		/*System.out.println("Leer 0x00,1: " + Arrays.toString(m1.leerLinea(0x00, 1)));
		System.out.println("Leer 0x10,1: " + Arrays.toString(m1.leerLinea(0x10, 1)));
		System.out.println("Leer 0x51,4: " + Arrays.toString(m1.leerLinea(0x51, 4)));
		System.out.println("Leer 0x52,4: " + Arrays.toString(m1.leerLinea(0x52, 4)));
		System.out.println("Leer 0x53,4: " + Arrays.toString(m1.leerLinea(0x53, 4)));
		System.out.println("Leer 0x54,4: " + Arrays.toString(m1.leerLinea(0x54, 4)));
		System.out.println("Leer 0x55,4: " + Arrays.toString(m1.leerLinea(0x55, 4)));
		System.out.println("Leer 0x56,4: " + Arrays.toString(m1.leerLinea(0x56, 4)));
		System.out.println("Leer 0x40,2: " + Arrays.toString(m1.leerLinea(0x40, 2)));
		System.out.println("Leer 0x40,12: " + Arrays.toString(m1.leerLinea(0x40, 12)));*/
		
		m2.guardarLinea(0, m1.leerLinea(0x40, 12));
		
		System.out.println(m2.toString(true));
		
		
		Cache c = new CacheDirecta(16, 4);
		for (int i = 0; i < 16*4*4; i+=4)
		{
			c.guardarDato(i, i * 2);
		}
		
		System.out.println(c.toString());
		
		System.out.println(c.leerDato(24));
		
		System.out.println("Leer 30,8: " + Arrays.toString(c.leerLinea(30, 8)));
	}
}
