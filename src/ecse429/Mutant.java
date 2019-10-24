package ecse429;

public class Mutant {
	private int line;
	private String string;
	private String result;
	private String program;
	private static int id;
	
	public Mutant(int line, String string, String program) {
		this.line = line;
		this.string = string;
		this.program = program;
		setId(getId() + 1);
	}

	public int getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getProgram() {
		return program;
	}

	public void setProgram(String program) {
		this.program = program;
	}



	public static int getId() {
		return id;
	}



	public static void setId(int id) {
		Mutant.id = id;
	}
}
