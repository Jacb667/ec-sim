package general;

// Archivo global para configuraciones del programa.

public class Config {
	
	private static int c[] = new int[Conf_Type.END_CONFIG.index()];
	
	public static void set(Conf_Type config, int value)
	{
		c[config.index()] = value;
	}
	
	public static int get(Conf_Type config)
	{
		return c[config.index()];
	}
	
	public enum Conf_Type
	{
		
		
		END_CONFIG (6);
		
		private int index;   

		Conf_Type(int index) {
	        this.index = index;
	    }

	    public int index() { 
	        return index; 
	    }
	}
}
