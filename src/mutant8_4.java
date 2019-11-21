public class mutant8_4{	
  public static void main(String[] args) {
  int result = add(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    System.out.println(result);
  }
  public static int add(int a, int b){
	int sum = a + b + a; //*
	int sub = b - a;
	return sum;

}
}

