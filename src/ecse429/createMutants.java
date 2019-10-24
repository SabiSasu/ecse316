package ecse429;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class createMutants {

	public static void main(String[] args) throws IOException {
		File program = new File("src//ecse429//program.txt");
		BufferedReader reader = new BufferedReader(new FileReader(program));
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("src//ecse429//mutants.txt")));
		
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
							System.out.println(operator[j]);
							String newLine = getNewLine(line, operator[j], operators[k]);
							//String newLine = line.replace(operator, operators[j]);
							String fileLine = String.format("  %-3d| %-30s| %-30s \n", lineNumb, line.trim(), newLine.trim()); 
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
		System.out.println(line.substring(0, Integer.parseInt(pos)));
		System.out.println(operators);
		System.out.println(line.substring(Integer.parseInt(pos)+1, line.length()));
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
			System.out.println(found[i] + ", ");
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
