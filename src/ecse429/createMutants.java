package ecse429;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class createMutants {
	static int mutantsKilled = 0;
	public static void main(String[] args) throws Exception {
		//clean up src folder
		cleanSourceFolder();
		String programFile = "program";

		String source = "src//" + programFile + ".java";
		String destination =  "src//mutantList.txt";
		String vectorFile = "src//testVectors.txt";

		//generating mutants (assig 1)
		generateMutantFile(source, destination);

		//reading mutants and generating individual mutant files (assig 2)
		ArrayList<String> mutantList = new ArrayList<String>();
		mutantList = generateMutants(source, destination, "src//");
		
		//reading vector file
		ArrayList<String> vectors = getVectorsFile(vectorFile);

		//run tests with test vectors and write output to file
		TestMutants(vectors, mutantList, programFile);

	}

	private static void TestMutants(ArrayList<String> vectors, ArrayList<String> mutantList, String program) throws Exception {
		PrintStream out = new PrintStream(new FileOutputStream("src//mutant_testing.txt"));
		ArrayList<String> results = new ArrayList<String>();
		System.setOut(out);

		//Run main program
		System.out.println("Software Under Test:");
		System.out.println("**********");
		results = runProgram(vectors, program);
		System.out.println("\n");
		
		//Run tests
		System.out.println("Mutants:\n");
		
		for (String mutant: mutantList) {
			simpleRun(vectors, mutant, results);
			System.out.println("\n");
		}
		
		System.out.println("Total Mutants: " + mutantList.size());
		System.out.println("Total Mutants Killed: " + mutantsKilled);
		System.out.println("Mutants Killed Ratio: " + mutantsKilled/mutantList.size());
	}

	
	private static ArrayList<String> getVectorsFile(String vectorFile) throws IOException {
		ArrayList<String> output = new ArrayList<String>();
		File file = new File(vectorFile);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while((line = reader.readLine()) != null) {
			output.add(line);
		}
		return output;
	}

	private static void cleanSourceFolder() {
		//to do maybe?

	}

	private static void printLines(String cmd, InputStream ins) throws Exception {
		String line = null;
		BufferedReader in = new BufferedReader(
				new InputStreamReader(ins));
		while ((line = in.readLine()) != null) {
			System.out.println(cmd + " " + line);
		}
	}

	private static String runProcess(String command) throws Exception {
		Process pro = Runtime.getRuntime().exec(command);
		if(!command.contains("javac")) {
		printLines("Result: ", pro.getInputStream());
		printLines("", pro.getErrorStream());
		pro.waitFor();
		}
		return pro.getInputStream().toString();
	}

	private static void simpleRun(ArrayList<String> inputs, String mutant, ArrayList<String> results) throws Exception {
		System.out.println(mutant);
		System.out.println("**********");
		String output = runProcess("javac -cp src " + mutant);
		System.out.println("**********");
		Boolean mutantKilled = true;
		for (int i = 0; i < inputs.size(); i++) {
			try {
				System.out.println("Test: " + inputs.get(i));
				output = runProcess("java -cp src " +mutant+ " " + inputs.get(i));
				System.out.println("SUT Output: " + results.get(i));
				
				if(output.equalsIgnoreCase(results.get(i))) {
					System.out.println("Mutant is NOT killed");
					mutantKilled = false;
				}
				else {
					System.out.println("Mutant is killed");
				}
				System.out.println("**********");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if(mutantKilled) {
			mutantsKilled++;
		}
	}
	
	private static ArrayList<String> runProgram(ArrayList<String> inputs, String mutant) throws Exception {
		ArrayList<String> outputs = new ArrayList<String>();
		for (String input: inputs) {
			try {
				System.out.println("Test: " + input);
				String output = runProcess("java -cp src " +mutant+ " " + input);
				outputs.add(output);
				System.out.println("**********");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return outputs;
	}

	//Assig 2:
	private static ArrayList<String> generateMutants(String source, String dest, String folderName) throws IOException {
		ArrayList<String> mutantNames = new ArrayList<String>();
		int id = 0;
		String mutantName = "mutant";

		BufferedReader mutantFile = new BufferedReader(new FileReader(new File(dest)));

		//loop through lines in mutant file, iterate through program file to copy each line
		String lineM = "";
		String lineP = "";
		int lineNum = 0;
		boolean end = false;
		int previousLine = 0;
		while((lineM = mutantFile.readLine()) != null){
			lineNum++;

			if(lineM.contains("---") && lineNum >= 3)
				end = true;

			if(lineNum >= 3 && end == false) {
				lineM = lineM.trim();
				id++;

				BufferedReader programFile = new BufferedReader(new FileReader(new File(source)));

				String[] content = lineM.split(",");
				int mutantLine = Integer.parseInt(content[0].trim());
				if(previousLine != mutantLine) {
					id = 1;
					previousLine=mutantLine;
				}
				String mutantString = content[2].trim();
				String programString = content[1].trim();
				int lineCount = 0;
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File(folderName+mutantName+mutantLine+"_"+id+ ".java")));

				while((lineP = programFile.readLine()) != null){
					lineCount++;
					//we reached mutant, replace line
					if(lineCount == mutantLine) {
						writer.write(lineP.replace(programString, mutantString)+" //*\n");
					}
					//copy string to new file
					else {
						if(lineCount == 1) {
							writer.write("public class " + mutantName+mutantLine+"_"+id +"{");
							mutantNames.add(mutantName+mutantLine+"_"+id);
						}
						else
							writer.write(lineP+"\n");
					}	
				}
				programFile.close();
				writer.close();
			}
		}
		mutantFile.close();
		return mutantNames;
	}

	//Assig 1:
	private static void generateMutantFile(String source, String dest) throws IOException {
		File program = new File(source);
		BufferedReader reader = new BufferedReader(new FileReader(program));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(dest)));

		writer.write("Line | Original Code 	             | Mutant Code\n");
		writer.write("---------------------------------------------------------------------\n");

		int mutantsFound = 0;
		int lineNumb = 0;
		String line = "";
		String[] operators = {"*", "+", "-", "/"};
		int[] operatorsCount = {0, 0, 0, 0};
		int operatorPos;

		while((line = reader.readLine()) != null){
			line = line.trim();
			//increment line
			lineNumb++;
			String operator[] = findOperators(line, operators);
			//the line contains at least one operator, so we can make mutants
			if(operator != null) {
				//loop through all operators of the string
				for(int j = 0; j < operator.length; j+=2) {
					//loop through the array with 4 operators for comparison
					for(int k = 0; k < operators.length; k++) {
						if(!operator[j+1].equals(operators[k])) {
							mutantsFound++;
							operatorsCount[k]++;
							//get new line by swapping whatever exists at the character position by operators[k]
							//System.out.println(operator[j]);
							String newLine = getNewLine(line, operator[j], operators[k]);
							//String newLine = line.replace(operator, operators[j]);
							String fileLine = String.format("  %-3d, %-30s, %-30s \n", lineNumb, line.trim(), newLine.trim()); 
							//writer.newLine();
							writer.write(fileLine);
						}
					}
				}

			}

		}

		//writing to mutants file
		writer.write("---------------------------------------------------------------------\n");
		writer.newLine();
		writer.write("Total mutants '"+ operators[0]  +"': " + operatorsCount[0] + "\n");
		writer.write("Total mutants '"+ operators[1]  +"': " + operatorsCount[1] + "\n");
		writer.write("Total mutants '"+ operators[2]  +"': " + operatorsCount[2] + "\n");
		writer.write("Total mutants '"+ operators[3]  +"': " + operatorsCount[3] + "\n");
		writer.write("Total mutants: " + mutantsFound);
		writer.close();
		reader.close();


	}

	private static String getNewLine(String line, String pos, String operators) {
		//System.out.println(line.substring(0, Integer.parseInt(pos)));
		//System.out.println(operators);
		//System.out.println(line.substring(Integer.parseInt(pos)+1, line.length()));
		String result = line.substring(0, Integer.parseInt(pos)) + operators + line.substring(Integer.parseInt(pos)+1, line.length());
		return result;
	}

	private static String[] findOperators(String line, String[] operators) {
		//iterate through line and find operators and their positions
		String[] found = new String[100];
		int index = 0;
		for(int i = 0; i < line.length(); i++) {
			if(line.charAt(i) == '+') {
				found[index] = Integer.toString(i);
				found[index+1] = "+";
				index+=2;
			}
			else if(line.charAt(i) == '-') {
				found[index] = Integer.toString(i);
				found[index+1] = "-";
				index+=2;
			}
			else if(line.charAt(i) == '*') {
				found[index] = Integer.toString(i);
				found[index+1] = "*";
				index+=2;
			}
			else if(line.charAt(i) == '/') {
				found[index] = Integer.toString(i);
				found[index+1] = "/";
				index+=2;
			}
		}

		//copy array to a smaller array
		String[] result = new String[index];
		for(int i = 0; i < result.length; i++) {
			//System.out.println(found[i] + ", ");
			result[i] = found[i];
		}
		return result;
	}

	private static String lineContains(String line, String[] operators) {
		for(int i = 0; i < operators.length; i++) {
			if(line.contains(operators[i]))
				return operators[i];

		}
		return null;
	}

}
