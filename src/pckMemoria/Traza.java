package pckMemoria;

import general.Log;
import general.MemoryException;

import java.util.*;

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
					throw new MemoryException("Error en la linea "+contLines+ " no se aceptan numeros negativos");
				}
				ejecutarLineaF(dirInst);
			}
			catch (NumberFormatException e)
			{
				throw new MemoryException("Error de formato en la linea "+contLines);
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
					throw new MemoryException("Error en la linea "+contLines);
				}
				ejecutarLineaF(dirInst);
				ejecutarLineaRW(dirMem, func);
			}
			catch (NumberFormatException e)
			{
				throw new MemoryException("Error de formato en la linea "+contLines);
			}
		}
		else
		{
			throw new MemoryException("Error en la linea "+contLines +" formato no aceptado");
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
			throw new MemoryException("Parametro invalido: "+s+" En linea: "+contLines);
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
