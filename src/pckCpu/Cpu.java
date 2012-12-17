package pckCpu;
import java.util.*;

public class Cpu {
	private SortedMap<String,Integer> map=new TreeMap<String,Integer>();
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
		StringTokenizer str= new StringTokenizer(s,", ;");
		int num=str.countTokens();
		if(num==3)
		{
			String func=str.nextToken();
			String reg1=str.nextToken();
			String elem2=str.nextToken();
			if(isReg(elem2))
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
			}
		}
		else if(num==2)
		{
			String func=str.nextToken();
			String jum=str.nextToken();
			isNum(jum);
			System.out.println("Jump toNum "+isNum(jum));
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
}
