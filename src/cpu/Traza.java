package cpu;

import general.Config;
import general.Log;
import general.MemoryException;

import java.awt.Color;
import java.util.*;

import javax.swing.JOptionPane;

import memoria.Direccion;
import memoria.JerarquiaMemoria;


public class Traza {
	int TAM=3;

	int contLines=0;
	int dirInst;
	int dirMem;
	String func;
	
	int ultAP=0;
	int ultAT=0;
	int[] ultAC= new int[3];
	
	JerarquiaMemoria jmem, jins;
	public Traza(JerarquiaMemoria j1, JerarquiaMemoria j2)
	{
		jmem=j1;
		jins=j2;
	}

	public void readLines(String s) throws MemoryException//desde un string
	{
		StringTokenizer str = new StringTokenizer(s,"'\n'");
		while(str.hasMoreTokens())
		{
			contLines++;
			decLine(str.nextToken());
			
		}
	}
	
	public void decLine(String s) throws MemoryException
	{
		StringTokenizer str=new StringTokenizer(s,",.; ");
		int strcount=str.countTokens();
		if(strcount==2)
		{
			try
			{
				dirInst=Integer.parseInt(str.nextToken());
				func=str.nextToken().toUpperCase();
				if((dirInst<0)||(dirMem<0))
				{
					if(Config.getVista()!=null)
					{
						JOptionPane.showMessageDialog( Config.getVista(), "Error en la linea "+contLines+", no se aceptan numeros negativos", "Error ", JOptionPane.ERROR_MESSAGE );
					}
					else
					{
						throw new MemoryException("Error de formato en la linea "+contLines);
					}
				}
				ejecutarLineaF(dirInst);
			}
			catch (NumberFormatException e)
			{
				if(Config.getVista()!=null)
				{
					JOptionPane.showMessageDialog( Config.getVista(), "Error de formato en la linea "+contLines, "Error de fomato tam 2", JOptionPane.ERROR_MESSAGE );
				}
				else
				{
					throw new MemoryException("Error de formato en la linea "+contLines);
				}
			}
		}
		else if(strcount==3)
		{
			try
			{
				dirInst=Integer.parseInt(str.nextToken());
				func=str.nextToken().toUpperCase();
				dirMem=Integer.parseInt(str.nextToken());
				if((dirInst<0)||(dirMem<0))
				{
					if(Config.getVista()!=null)
					{
						JOptionPane.showMessageDialog( Config.getVista(), "Error en la linea "+contLines+", no se aceptan numeros negativos", "Error ", JOptionPane.ERROR_MESSAGE );
					}
					else
					{
						throw new MemoryException("Error de formato en la linea "+contLines);
					}
				}
				ejecutarLineaF(dirInst);
				ejecutarLineaRW(dirMem, func);
			}
			catch (NumberFormatException e)
			{
				if(Config.getVista()!=null)
				{
					JOptionPane.showMessageDialog( Config.getVista(), "Error de formato en la linea "+contLines, "Error de fomato tam3", JOptionPane.ERROR_MESSAGE );
				}
				else
				{
					throw new MemoryException("Error de formato en la linea "+contLines);
				}
			}
		}
		else
		{
			if(Config.getVista()!=null)
			{
				JOptionPane.showMessageDialog( Config.getVista(), "Error de formato en la linea "+contLines, "Error de fomato "+strcount, JOptionPane.ERROR_MESSAGE );
			}
			else
			{
				throw new MemoryException("Error de formato en la linea "+contLines);
			}
		}
	}
	
	public void ejecutarLineaF(int dirInst) throws MemoryException
	{
		Log.println(1, "Fetch 0x" + Integer.toHexString(dirInst), Color.BLACK, true);
		if(jins==null)
		{
			Direccion d=jmem.simularLeerDato(dirInst);
		}
		else
		{
			Direccion d=jins.simularLeerDato(dirInst);
		}
	}

	public void ejecutarLineaRW(int dirMem, String s) throws MemoryException
	{
		if(s.equals("W"))
		{
			Log.println(1, "Lectura de dirección virtual: 0x" + Integer.toHexString(dirMem), Color.BLACK, true);
			Direccion d=jmem.simularGuardarDato(dirMem, dirMem);
		}
		else if(s.equals("R"))
		{
			Log.println(1, "Guardado de dato en dirección virtual: 0x" + Integer.toHexString(dirMem), Color.BLACK, true);
			Direccion d=jmem.simularLeerDato(dirMem);
		}
		else
		{
			JOptionPane.showMessageDialog( Config.getVista(), "Parametro invalido: "+s+" En linea: "+contLines, "Parametro Invalido", JOptionPane.ERROR_MESSAGE );
		}
	}
}
