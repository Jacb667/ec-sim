package cpu;

import general.Config;
import general.Log;
import general.MemoryException;

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
	StringBuilder stb=new StringBuilder();
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
		Log.generarEstadistica();
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
		if(jins==null)
		{
			Direccion d=jmem.simularLeerDato(dirInst);
			stb.append("FETCH--\t");
			generarEstadistica(d);
		}
		else
		{
			Direccion d=jins.simularLeerDato(dirInst);
			stb.append("FETCH--\t");
			generarEstadistica(d);
		}
		
	}

	private void generarEstadistica(Direccion d) {
		
		if(ultAT!=Log.aciertosTlb)
		{
			ultAT=Log.aciertosTlb;
			stb.append("TLB: HIT, ");
			
		}
		else
		{
			stb.append("TLB: MISS, ");
		}
		if(ultAP!=Log.aciertosPagina)
		{
			ultAP=Log.aciertosPagina;
			stb.append("PAGE: "+d.getPagina()+" HIT, Direccion Fisica: "+d.getReal()+" ");
		}
		else
		{
			stb.append("PAGE: "+d.getPagina()+" FAULT, Direccion Fisica: "+d.getReal()+" ");
		}
		
		for(int i=0;i<jmem.getNivelesCache();i++)
		{
			if(ultAC[i]!=Log.cache_hits[i])
			{
				stb.append("Cache "+i+": HIT ");
				ultAC[i]=Log.cache_hits[i];
				break;
			}
			else
			{
				stb.append("Cache "+i+": MISS ");
			}
		}
		
	
		stb.append(" Linea: "+contLines+"\n");
	}
	public void ejecutarLineaRW(int dirMem, String s) throws MemoryException
	{
		
		if(s.equals("W"))
		{
			stb.append("W--\t");
			Direccion d=jmem.simularGuardarDato(dirMem, dirMem);
			generarEstadistica(d);
		}
		else if(s.equals("R"))
		{
			stb.append("R--\t");
			Direccion d=jmem.simularLeerDato(dirMem);
			generarEstadistica(d);
		}
		else
		{
			JOptionPane.showMessageDialog( Config.getVista(), "Parametro invalido: "+s+" En linea: "+contLines, "Parametro Invalido", JOptionPane.ERROR_MESSAGE );
		}
	}
	
	public void addToResult(String s)
	{
		
		stb.append(s).append("\n");
	}
	public String getResult()
	{
		return stb.toString();
	}
}
