package general;

import java.util.*;

public class Traza {

	int contLines=0;
	int dirInst;
	int dirMem;
	String func;
	String result="";

	public void readLines(String s)//desde un string
	{
		StringTokenizer str = new StringTokenizer(s,"'\n'");
		while(str.hasMoreTokens())
		{
			contLines++;
			decLine(str.nextToken());
			
		}
	}
	
	public void decLine(String s)
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
					System.out.println("Error en la linea "+contLines+ " no se aceptan numeros negativos");
				}
				System.out.println("es de dos " + func);
			}
			catch (NumberFormatException e)
			{
				System.out.println("Error de formato en la linea "+contLines);
			}
		}
		else if(strcount==3)
		{
			try
			{
				dirInst=Integer.parseInt(str.nextToken());
				func=str.nextToken().toUpperCase();
				dirMem=Integer.parseInt(str.nextToken());
				System.out.println("es de tres");
				if((dirInst<0)||(dirMem<0))
				{
					System.out.println("Error en la linea "+contLines);
				}
			}
			catch (NumberFormatException e)
			{
				System.out.println("Error de formato en la linea "+contLines);
			}
		}
		else
		{
			System.out.println("Error en la linea "+contLines +" formato no aceptado");
		}
	}
	
	public void addToResult(String s)
	{
		StringBuilder stb=new StringBuilder();
		stb.append(result).append(s).append("/n");
	}
	public String getResult()
	{
		return result;
	}
}
