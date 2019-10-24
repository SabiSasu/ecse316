package ecse429;

public class Controller {
	private Program program;
	private String filepath;
	private Vector[] vectors;
	
	public Controller() {
		
	}
	
	public Program setProgram(String file) {
		return null;
		
	}
	
	public Program getProgram() {
		return program;
		
	}
	
	public void setFilepath(String filepath) {
		
	}
	
	public void generateMutants() {
		//generate mutants and set them to 
		
		//maybe return file to them display it?
	}
	
	public Mutant[] getMutants() {
		return program.getMutants();
	}
	
	public Vector[] getVectors() {
		return vectors;
	}
	
	public void setVectors(String file) {
		//generate vectors and put them in controller
	}
	
	public String runMutant(Mutant mutant, Vector vector) {
		return null;
	}
	
	public String runProgram(Program program, Vector vector) {
		return null;
	}
	
	//compile before running
	private void compileFile() {
		
	}
	
}
