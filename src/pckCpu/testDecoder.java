package pckCpu;

public class testDecoder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Decoder dec=new Decoder();
		
		dec.readLine("loop:add,$1,$2 \n sw $5,100($2) \n addi,$6,100 \n j loop \n subi $5,100($2)");
		//cpu.decloop("sw $5,100($2)");
		//cpu.decloop("sub,$6,100");
		//cpu.decloop("j loop");

	}

}
