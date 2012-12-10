package general;

public class Alu {
	
	private boolean Fzero;
	private boolean Foverflow;
	
	public Alu()
	{
		Fzero= false;
		Foverflow=false;
	}
	
	public boolean getZero()
	{
		return Fzero;
	}
	public boolean getFoverflow()
	{
		return Foverflow;
	}
	private void isOverFlow(int x)
	{
		boolean es=false;
		//Completar
		Foverflow=es;
		
	}
	private void isZero(int x)
	{
		if(x==0)
		{
			Fzero=true;
		}
		
	}
	
	public int suma(int a, int b)
	{
		int res=a+b;
		//Completar
		return res;
	}
	public int resta(int a, int b)
	{
		int res=a-b;
		//Completar
		return res;
	}
	public int mult(int a, int b)
	{
		int res=a*b;
		//Completar
		return res;
	}
	public int div(int a, int b)
	{
		if(b==0)
		{
			//Completar
		}
		int res=a/b;
		//Completar
		return res;
	}

}
