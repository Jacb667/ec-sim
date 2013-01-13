package general;

public class TestTraza {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Traza t=new Traza();
		//Numero maximo de para instruccion de memoria 2147483647"
		String s=new String("-2147483647,r\n34,W,56\n34,45,r,2");
		
		t.readLines(s);
		
	}

}
