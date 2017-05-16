import java.io.*;
import java.awt.*;
import java.util.*;
import java.util.regex.*;

public class SimpleComputer{

	private static final String INPUT = "input.txt";
	//private static final String OUTPUT = "output.txt";

	//STATES
	private static final String FETCH = "F";
	private static final String DECODE = "D";
	private static final String EXECUTE = "E";
	private static final String MEM_ACCESS = "MA";
	private static final String WRITE_BACK = "WB";
	private static final String DONE = "X";
	private static final String STALL = "S";

	HashMap<String, Integer> registers = new HashMap<String, Integer>();
	HashMap<Integer, Operation> ops = new HashMap<Integer, Operation>();

	// Other Registers
	int pc;
	int mar;
	int mbr;
	int of=0;
	int nf=0;
	int zf=0;

	int clkCycle;
	int last_addr;

	//regular expressions
    String instregex = "(LOAD|ADD|SUB|CMP)";
    String registerregex = "(R1|R2|R3|R4|R5|R6|R7|R8|R9|R10|R11|R12|R13|R14|R15|R16|R17|R18|R19|R20|R21|R22|R23|R24|R25|R26|R27|R28|R29|R30|R31|R32)";
    String numregex = "[0-9][0-9]*";

    //compiles the regular expression into a pattern
    Pattern instpattern = Pattern.compile(instregex, Pattern.CASE_INSENSITIVE);
    Pattern registerpattern = Pattern.compile(registerregex, Pattern.CASE_INSENSITIVE);
    Pattern numpattern = Pattern.compile(numregex, Pattern.CASE_INSENSITIVE);

	public SimpleComputer() throws IOException{

		int i,j;
		int temp1=0;
		int temp2=0;

		clkCycle=1;

		String line;				//to reference one line at a time
		String[] array;				//temporary string holder

		try(BufferedReader br = new BufferedReader(new FileReader(INPUT))) {

			int temp_address=1;			//init temporary address to be placed as keys in the hashmap to get the instructions to be performed

			while((line = br.readLine()) != null) {					//read the input per line
				//splits the string according to the specified delimiter
                array = line.split("[, ]+");

                //tries to find whether the string inside the array is an instruction, register, or number
				Matcher inst = instpattern.matcher(array[0]);
				Matcher reg = registerpattern.matcher(array[1]);
				Matcher num = numpattern.matcher(array[2]);

				Operation op = new Operation();					//instantiate new operation

				if(array.length==3){
					if (inst.matches()) {
						if(array[0].equals("LOAD")){
							op.instruction=array[0];

							if(reg.matches()){
								op.operand1=array[1];
								if(num.matches()){
									op.operand2=array[2];
									op.state=FETCH;
									ops.put(temp_address,op);		//put it in the array of operations with temporary address, waiting to be fetched
									//System.out.println(ops.get(1).instruction + " " + ops.get(1).operand1 + " " + ops.get(1).operand2);
									last_addr=temp_address;
									temp_address++;	
								}
								else{
									System.out.println("Unknown operand encountered!");
								}
							}
							else{
								System.out.println("Unknown operand encountered!");
							}

						}
						else{
							op.instruction=array[0];

							if(reg.matches()){
								op.operand1=array[1];
								if(reg.matches()){
									op.operand2=array[2];
									ops.put(temp_address,op);		//put it in the array of operations with temporary address, waiting to be fetched
									op.state=FETCH;
									//System.out.println(ops.get(4).instruction + " " + ops.get(4).operand1 + " " + ops.get(4).operand2);
									last_addr=temp_address;
									temp_address++;	
								}
								else{
									System.out.println("Unknown operand encountered!");
								}
							}
							else{
								System.out.println("Unknown operand encountered!");
							}

						}
	                }
	                else{
	                	System.out.println("An unknown instruction has been detected!");
	                }
				}
				else{
					System.out.println("Invalid input read!");
				}

			}

			// ---------------------------------------- END OF READING FILE ----------------------------------------

			while(clkCycle<7){		//!!!!!!!!!!!!!!!!!!!! -------------------- while(ops.get(last_addr).state!=DONE)

				if(clkCycle==1){

					System.out.println("Operation: " + ops.get(clkCycle).instruction + " " + ops.get(clkCycle).operand1 + "," 
						+ ops.get(clkCycle).operand2 + "\nState: " + ops.get(clkCycle).state);
					System.out.println("Clock Cycle: " + clkCycle);
					ops.get(clkCycle).state=DECODE;
					temp1=clkCycle;					//temp1 will hold the leading instruction being executed
					clkCycle++;
					System.out.println();
				}
				else{

					if(ops.get(temp1).state==DONE){
						temp1++;
					}
					else{

						if(ops.get(temp1).state==DECODE){

							System.out.println("Operation: " + ops.get(clkCycle-1).instruction + " " + ops.get(clkCycle-1).operand1 + "," 
							+ ops.get(clkCycle-1).operand2 + "\nState: " + ops.get(clkCycle-1).state);
							System.out.println("Operation: " + ops.get(clkCycle).instruction + " " + ops.get(clkCycle).operand1 + "," 
							+ ops.get(clkCycle).operand2 + "\nState: " + ops.get(clkCycle).state);

							System.out.println("Clock Cycle: " + clkCycle);
							ops.get(clkCycle).state=DECODE;
							ops.get(clkCycle-1).state=EXECUTE;
							clkCycle++;
							System.out.println();
						}	

						else if(ops.get(temp1).state==EXECUTE){

							System.out.println("Operation: " + ops.get(clkCycle-2).instruction + " " + ops.get(clkCycle-2).operand1 + "," 
							+ ops.get(clkCycle-2).operand2 + "\nState: " + ops.get(clkCycle-2).state);
							System.out.println("Operation: " + ops.get(clkCycle-1).instruction + " " + ops.get(clkCycle-1).operand1 + "," 
							+ ops.get(clkCycle-1).operand2 + "\nState: " + ops.get(clkCycle-1).state);
							System.out.println("Operation: " + ops.get(clkCycle).instruction + " " + ops.get(clkCycle).operand1 + "," 
							+ ops.get(clkCycle).operand2 + "\nState: " + ops.get(clkCycle).state);

							//~EXECUTE the instruction with the execute state~
							if(ops.get(temp1).instruction.equals("LOAD")){
								execLoad(ops.get(temp1).operand1,ops.get(temp1).operand2);
							}

							System.out.println("Clock Cycle: " + clkCycle);
							ops.get(clkCycle).state=DECODE;
							ops.get(clkCycle-1).state=EXECUTE;
							ops.get(clkCycle-2).state=MEM_ACCESS;
							clkCycle++;
							System.out.println();
						}

						else if(ops.get(temp1).state==MEM_ACCESS){

							System.out.println("Operation: " + ops.get(clkCycle-3).instruction + " " + ops.get(clkCycle-3).operand1 + "," 
							+ ops.get(clkCycle-3).operand2 + "\nState: " + ops.get(clkCycle-3).state);
							System.out.println("Operation: " + ops.get(clkCycle-2).instruction + " " + ops.get(clkCycle-2).operand1 + "," 
							+ ops.get(clkCycle-2).operand2 + "\nState: " + ops.get(clkCycle-2).state);
							System.out.println("Operation: " + ops.get(clkCycle-1).instruction + " " + ops.get(clkCycle-1).operand1 + "," 
							+ ops.get(clkCycle-1).operand2 + "\nState: " + ops.get(clkCycle-1).state);
							System.out.println("Operation: " + ops.get(clkCycle).instruction + " " + ops.get(clkCycle).operand1 + "," 
							+ ops.get(clkCycle).operand2 + "\nState: " + ops.get(clkCycle).state);

							//~EXECUTE the instruction with the execute state~

							System.out.println("Clock Cycle: " + clkCycle);
							ops.get(clkCycle).state=DECODE;
							ops.get(clkCycle-1).state=EXECUTE;
							ops.get(clkCycle-2).state=MEM_ACCESS;
							ops.get(clkCycle-3).state=WRITE_BACK;
							clkCycle++;
							System.out.println();
						}

						else if(ops.get(temp1).state==WRITE_BACK){
							System.out.println("Operation: " + ops.get(clkCycle-4).instruction + " " + ops.get(clkCycle-4).operand1 + "," 
							+ ops.get(clkCycle-4).operand2 + "\nState: " + ops.get(clkCycle-4).state);
							System.out.println("Operation: " + ops.get(clkCycle-3).instruction + " " + ops.get(clkCycle-3).operand1 + "," 
							+ ops.get(clkCycle-3).operand2 + "\nState: " + ops.get(clkCycle-3).state);
							System.out.println("Operation: " + ops.get(clkCycle-2).instruction + " " + ops.get(clkCycle-2).operand1 + "," 
							+ ops.get(clkCycle-2).operand2 + "\nState: " + ops.get(clkCycle-2).state);
							System.out.println("Operation: " + ops.get(clkCycle-1).instruction + " " + ops.get(clkCycle-1).operand1 + "," 
							+ ops.get(clkCycle-1).operand2 + "\nState: " + ops.get(clkCycle-1).state);
							System.out.println("Operation: " + ops.get(clkCycle).instruction + " " + ops.get(clkCycle).operand1 + "," 
							+ ops.get(clkCycle).operand2 + "\nState: " + ops.get(clkCycle).state);

							//~EXECUTE the instruction with the execute state~

							System.out.println("Clock Cycle: " + clkCycle);
							ops.get(clkCycle).state=DECODE;
							ops.get(clkCycle-1).state=EXECUTE;
							ops.get(clkCycle-2).state=MEM_ACCESS;
							ops.get(clkCycle-3).state=WRITE_BACK;
							ops.get(clkCycle-4).state=DONE;
							clkCycle++;
							System.out.println();
						}

					}

				}

			}

		}catch(FileNotFoundException ex){
			System.out.println("Unable to open file '" + INPUT + "'");  
		}

	}

	public void execLoad(String register, String val){
		int value = Integer.parseInt(val);

		registers.put(register,value);

		System.out.println("Executing..");
		System.out.println(registers.get(register));

	}

	public void execAdd(String op1, String op2){
		
	}

	public void execSub(String op1, String op2){
		
	}

	public void execCmp(String op1, String op2){
		
	}

	public static void main(String[] args) throws IOException{
		SimpleComputer simpComp = new SimpleComputer();
	}

}