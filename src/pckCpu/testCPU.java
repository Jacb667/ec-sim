package pckCpu;

public class testCPU {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Cpu cpu=new Cpu();
		
		cpu.decloop("loop:add,$1,$2");
		cpu.decloop("sw $5,100($2)");
		cpu.decloop("sub,$6,100");
		cpu.decloop("j loop");

	}

}
