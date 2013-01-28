package pckCpu;

import general.MemoryException;

public interface Cpu {
	
	public void ejecutarCodigo() throws MemoryException, CpuException;
	public void setPC(int dir);
	public int getPC();

}
