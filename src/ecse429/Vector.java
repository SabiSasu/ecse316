package ecse429;

public class Vector {
	private int[] inputs;
	private String string;
	
	public Vector(String input) {
		//parse input and put it in inputs
		this.string = input;
	}

	public int[] getInputs() {
		return inputs;
	}

	public void setInputs(int[] inputs) {
		this.inputs = inputs;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

}
