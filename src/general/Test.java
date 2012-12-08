package general;

import java.util.Arrays;

import pckMemoria.Cache;
import pckMemoria.CacheAsociativa;
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
		MemoriaPrincipal m1 = new MemoriaPrincipal(32);
		MemoriaPrincipal m2 = new MemoriaPrincipal(16);
		
		for (int i = 0; i < 128; i+=4)
			m1.guardarDato(i, i * 2);
		
		System.out.println(m1.toString(true));
		
		//System.out.println("Leer 0x40,2: " + Arrays.toString(m1.leerLinea(0x40, 2)));
		
		m2.guardarLinea(0, m1.leerLinea(0x40, 12));
		
		System.out.println(m2.toString(true));
		
		Cache c = new CacheDirecta(16, 4);
		for (int i = 0; i < 16*4*4; i+=4)
			c.guardarDato(i, i * 2, false);
		
		System.out.println(c.toString());
		
		System.out.println(c.leerDato(0x04));
		
		c.guardarLinea(0x40, m1.leerLinea(0x20, 4), true);
		
		System.out.println(c.toString());
		
		System.out.println("Dato 0x14: " + c.estaLibre(0x14) + c.existeDato(0x14) + c.isDirty(0x14));
		if (c.existeDato(0x14))
			System.out.println(c.leerDato(0x14));
		System.out.println("Dato 0x4C: " + c.estaLibre(0x4C) + c.existeDato(0x4C) + c.isDirty(0x4C));
		if (c.existeDato(0x4C))
			System.out.println(c.leerDato(0x4C));
		System.out.println("Dato 0xE0: " + c.estaLibre(0xE0) + c.existeDato(0xE0) + c.isDirty(0xE0));
		if (c.existeDato(0xE0))
			System.out.println(c.leerDato(0xE0));
		
		/*System.out.println("Cache Asociativa: \n");
		Cache c = new CacheAsociativa(32, 4, 4);
		System.out.println(c.existeDato(0x20));
		
		for (int i = 0x24; i < 0x40; i+=4)
			c.guardarDato(i, i*2, false);
		
		c.guardarDato(0x20, 76, true);
		
		System.out.println(c.toString());*/
		
		//System.out.println("Leer: " + Arrays.toString(c.leerLinea(0xFF)));
	}
}
