package Pruebas;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import componentes.Tabla;
import componentes.VentanaLimitada;
import componentes.VentanaOculta;

import pckMemoria.MemoriaPrincipal;
import pckMemoria.Pagina;
import pckMemoria.TablaPaginas;
import general.Global.TiposReemplazo;
import general.MemoryException;

public class TestTraductor {
	
	final int palabras_linea = 4;
	
	public TestTraductor()
	{
		final int entradas_pagina = 8;	// 32 direcciones
		final int max_entrada = 2048;	// 8192 direcciones
		final int max_ent_mem = 128;	// 512 direcciones
		
		try
		{
			//TablaPaginas tablaPags = new TablaPaginas(4096, palabras_linea, 4294967295l, 40960, TiposReemplazo.RANDOM);
			TablaPaginas tablaPags = new TablaPaginas(entradas_pagina, palabras_linea, max_entrada, max_ent_mem, TiposReemplazo.RANDOM);
			System.out.println(tablaPags);
			
			// Direcciones entre 0 y 1024 -> Página 0
			
			//System.out.println("2047 traducida como: 0x" + Integer.toHexString(tablaPags.traducirDireccion(2047)));
			
			MemoriaPrincipal memoria = new MemoriaPrincipal(tablaPags);
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			Tabla tablaMemoria = new Tabla(memoria);
			JFrame frameMemoria = new VentanaLimitada();
			JScrollPane jscroll1 = new JScrollPane(tablaMemoria, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			frameMemoria.setTitle("Memoria");
			frameMemoria.setPreferredSize( new Dimension(245, 400) );
			frameMemoria.setMinimumSize(new Dimension(250, 400));
			frameMemoria.setMaximumSize(new Dimension(400, 2000));
			frameMemoria.add( jscroll1 );
			frameMemoria.pack();
			frameMemoria.addWindowListener(new VentanaOculta(frameMemoria));
			frameMemoria.setVisible(true);
			memoria.setInterfaz(tablaMemoria);
			
			Thread.sleep(10000);
			
			
			memoria.guardarDato(tablaPags.traducirDireccion(0x100), 100);
			memoria.guardarDato(tablaPags.traducirDireccion(0x200), 200);
			memoria.guardarDato(tablaPags.traducirDireccion(0x300), 300);
			memoria.guardarDato(tablaPags.traducirDireccion(0x400), 400);
			memoria.guardarDato(tablaPags.traducirDireccion(0x500), 500);
			memoria.guardarDato(tablaPags.traducirDireccion(0x600), 600);
			memoria.guardarDato(tablaPags.traducirDireccion(0x700), 700);
			memoria.guardarDato(tablaPags.traducirDireccion(0x800), 800);
			memoria.guardarDato(tablaPags.traducirDireccion(0x900), 900);
			memoria.guardarDato(tablaPags.traducirDireccion(0x1000), 1000);

			
			System.out.println("0x100 traducida como: " + tablaPags.traducirDireccion(0x100));
			System.out.println("0x200 traducida como: " + tablaPags.traducirDireccion(0x200));
			System.out.println("0x300 traducida como: " + tablaPags.traducirDireccion(0x300));
			System.out.println("0x400 traducida como: " + tablaPags.traducirDireccion(0x400));
			System.out.println("0x500 traducida como: " + tablaPags.traducirDireccion(0x500));
			System.out.println("0x600 traducida como: " + tablaPags.traducirDireccion(0x600));
			System.out.println("0x700 traducida como: " + tablaPags.traducirDireccion(0x700));
			System.out.println("0x800 traducida como: " + tablaPags.traducirDireccion(0x800));
			System.out.println("0x900 traducida como: " + tablaPags.traducirDireccion(0x900));
			System.out.println("0x1000 traducida como: " + tablaPags.traducirDireccion(0x1000));
			
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x100)));
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x200)));
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x300)));
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x400)));
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x500)));
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x600)));
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x700)));
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x800)));
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x900)));
			System.out.println(memoria.leerDato(tablaPags.traducirDireccion(0x1000)));
			
			System.out.println(memoria);
		}
		catch (MemoryException e)
		{
			System.err.println(e);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
