package pckCpu;
import java.util.*;

public class Decoder
{
	private SortedMap<String,Integer> map=new TreeMap<String,Integer>();
	public void readLine(String s)
	{
		StringTokenizer str=new StringTokenizer(s,"'\n'");
		while (str.hasMoreTokens())
		{
			decloop(str.nextToken());
		}
	}
	public void decloop(String s)
	{
		StringTokenizer str =new StringTokenizer(s,":");
		String func=str.nextToken();
		if(str.hasMoreTokens())
		{
			String op=str.nextToken();
			System.out.println(func+": "+op+" ...");
			dec(op);
		}
		else
		{
			dec(func);
		}
	}
	public void dec(String s)
	{
		System.out.println(s);
		StringTokenizer str= new StringTokenizer(s,", ; '\n'");
		int num=str.countTokens();
		if(num==3)
		{
			String func=str.nextToken();
			String reg1=str.nextToken();
			String elem2=str.nextToken();
			
			if(isReg(reg1))
			{
				decFunc(func, reg1,elem2);
			}
			else
			{
				System.out.println("ERROR reg1, no es un registro");
			}
			
			
			
		}
		else if(num==2)
		{
			//FUNCIONES J,JR,JAL
			String func=str.nextToken();
			String jum=str.nextToken();
			System.out.println("Jump toNum "+isNum(jum));
		}
		else if(num==4)
		{
			String func=str.nextToken();
			String reg1=str.nextToken();
			String reg2=str.nextToken();
			String to=str.nextToken();
			if((isReg(reg1))&&(isReg(reg2)))
			{
				//FUNCION BEQ, BNE,
			}
		}
		
		
	}
	public void putMap(String s, int i)
	{
		map.put(s, i);
	}
	public SortedMap<String,Integer> getMap()
	{
		return map;
	}
	public boolean isReg(String s)
	{
		boolean reg=false;
		if(s.charAt(0)=='$')
		{
			reg=true;
		}
		return reg;
	}
	public boolean isConst(String s)
	{
		boolean is=false;
		StringTokenizer str=new StringTokenizer(s,"()");
		if(str.countTokens()==1)
		{
			is=true;
		}
		
		return is;
	}
	public boolean isNum(String s)
	{
		boolean num=true;
		try
		{
			int x=Integer.parseInt(s);
		}
		catch (NumberFormatException e)
		{
			num=false;
		}
		
		return num;
	}

	public void decFunc(String func, String reg1, String elem2)
	{
		func=func.toUpperCase();
		switch(func)
		{
			case"ADD": System.out.println("Operacion suma");
				if(isReg(elem2))
				{
					
					System.out.println("elem2 es reg");
				}
				else if(isConst(elem2))
				{
					System.out.println("error, add solo acepta dos registros no constantes");
				}
				else
				{
					System.out.println("error, add solo acepta dos registros no direccion de memoria");
				}
			break;
			case "ADDI": System.out.println("Suma inmediata");
				if(isReg(elem2))
				{
					
					System.out.println("error addi necesita una constante no reg.");
				}
				else if(isConst(elem2))
				{
					System.out.println("elem2 es const");
				}
				else
				{
					System.out.println("error addi necesita una constante no dir.mem.");
				}
			break;
			case "SUB": System.out.println("resta");
				if(isReg(elem2))
				{
					
					System.out.println("elem2 es reg");
				}
				else if(isConst(elem2))
				{
					System.out.println("error, sub solo acepta dos registros no constantes");
				}
				else
				{
					System.out.println("error, add solo acepta dos registros no direccion de memoria");
				}
			break;
			case "SUBI": System.out.println("resta inmediata");
				if(isReg(elem2))
				{
					
					System.out.println("error subi necesita una constante no reg.");
				}
				else if(isConst(elem2))
				{
					System.out.println("elem2 es const");
				}
				else
				{
					System.out.println("error subi necesita una constante no dir.mem.");
				}
			break;
			case "LW": System.out.println("Cargar dato");
				if(isReg(elem2))
				{
					
					System.out.println("error en lw, elm2=reg");
				}
				else if(isConst(elem2))
				{
					System.out.println("error en lw, elem2=const");
				}
				else
				{
					System.out.println("elem2 es direccion memoria");
				}
			break;
			case "SW": System.out.println("Guardar dato");
				if(isReg(elem2))
				{
					
					System.out.println("error en  sw elem2= reg");
				}
				else if(isConst(elem2))
				{
					System.out.println("error en sw elem2 es const");
				}
				else
				{
					System.out.println("elem2 es direccion memoria");
				}
			break;
		}
		/*if(isReg(elem2))
		{
			
			System.out.println("elem2 es reg");
		}
		else if(isConst(elem2))
		{
			System.out.println("elem2 es const");
		}
		else
		{
			System.out.println("elem2 es direccion memoria");
		}*/
	}
}
