package ecse429;

public class Program {
	private int mutantsPlus;
	private int mutantsMinus;
	private int mutantsMult;
	private int mutantsDiv;
	String program;
	Mutant[] mutants;
	
	public Program(int plus, int minus, int mult, int div, String program, Mutant[] mutants) {
		this.mutantsPlus = plus;
		this.mutantsDiv = div;
		this.mutantsMinus = minus;
		this.mutantsMult = mult;
		this.program = program;
		this.mutants = mutants;
	}

	public int getMutantsPlus() {
		return mutantsPlus;
	}

	public void setMutantsPlus(int mutantsPlus) {
		this.mutantsPlus = mutantsPlus;
	}

	public int getMutantsMinus() {
		return mutantsMinus;
	}

	public void setMutantsMinus(int mutantsMinus) {
		this.mutantsMinus = mutantsMinus;
	}

	public int getMutantsMult() {
		return mutantsMult;
	}

	public void setMutantsMult(int mutantsMult) {
		this.mutantsMult = mutantsMult;
	}

	public int getMutantsDiv() {
		return mutantsDiv;
	}

	public void setMutantsDiv(int mutantsDiv) {
		this.mutantsDiv = mutantsDiv;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}

	public Mutant[] getMutants() {
		return mutants;
	}

	public void setMutants(Mutant[] mutants) {
		this.mutants = mutants;
	}
	
}
