import java.io.*;
import java.util.*;

public class VPL
{
  static String fileName;
  static Scanner keys;

  static int max;
  static int[] mem;
  static int ip, bp, sp, rv, hp, numPassed, gp;
  static int step;

  public static void main(String[] args) throws Exception {

    keys = new Scanner( System.in );

    if( args.length != 2 ) {
      System.out.println("Usage: java VPL <vpl program> <memory size>" );
      System.exit(1);
    }

    fileName = args[0];

    max = Integer.parseInt( args[1] );
    mem = new int[max];

    // load the program into the front part of
    // memory
    Scanner input = new Scanner( new File( fileName ) );
    String line;
    StringTokenizer st;
    int opcode;

    ArrayList<IntPair> labels, holes;
    labels = new ArrayList<IntPair>();
    holes = new ArrayList<IntPair>();
    int label;

    // load the code

    int k=0;
    while ( input.hasNextLine() ) {
      line = input.nextLine();
      System.out.println("parsing line [" + line + "]");
      if( line != null )
      {// extract any tokens
        st = new StringTokenizer( line );
        if( st.countTokens() > 0 )
        {// have a token, so must be an instruction (as opposed to empty line)

          opcode = Integer.parseInt(st.nextToken());

          // load the instruction into memory:

          if( opcode == labelCode )
          {// note index that comes where label would go
            label = Integer.parseInt(st.nextToken());
            labels.add( new IntPair( label, k ) );
          }
          else if( opcode == noopCode ){
          }
          else
          {// opcode actually gets stored
            mem[k] = opcode;  k++;
 
            if( opcode == callCode || opcode == jumpCode ||
                opcode == condJumpCode )
            {// note the hole immediately after the opcode to be filled in later
              label = Integer.parseInt( st.nextToken() );
              mem[k] = label;  holes.add( new IntPair( k, label ) );
              ++k;
            }

            // load correct number of arguments (following label, if any):
            for( int j=0; j<numArgs(opcode); ++j )
            {
              mem[k] = Integer.parseInt(st.nextToken());
              ++k;
            }

          }// not a label

        }// have a token, so must be an instruction
      }// have a line
    }// loop to load code
    
    //System.out.println("after first scan:");
    //showMem( 0, k-1 );

    // fill in all the holes:
    int index;
    for( int m=0; m<holes.size(); ++m )
    {
      label = holes.get(m).second;
      index = -1;
      for( int n=0; n<labels.size(); ++n )
        if( labels.get(n).first == label )
          index = labels.get(n).second;
      mem[ holes.get(m).first ] = index;
    }

    System.out.println("after replacing labels:");
    showMem( 0, k-1 );

    // initialize registers:
    bp = k;  sp = k+2;  ip = 0;  rv = -1;  hp = max;
    numPassed = 0;
    
    int codeEnd = bp-1;

    System.out.println("Code is " );
    showMem( 0, codeEnd );

    // start execution:
    boolean done = false;
    int op; //, a=0, b=0, c=0;
    // int actualNumArgs;

    // int step = 0;

    // int oldIp = 0;

    // repeatedly execute a single operation
    // *****************************************************************

    do {

/*    // show details of current step
      System.out.println("--------------------------");
      System.out.println("Step of execution with IP = " + ip + " opcode: " +
          mem[ip] + 
         " bp = " + bp + " sp = " + sp + " hp = " + hp + " rv = " + rv );
      System.out.println(" chunk of code: " +  mem[ip] + " " +
                            mem[ip+1] + " " + mem[ip+2] + " " + mem[ip+3] );
      System.out.println("--------------------------");
      System.out.println( " memory from " + (codeEnd+1) + " up: " );
      showMem( codeEnd+1, sp+3 );
      System.out.println("hit <enter> to go on" );
      keys.nextLine();
*/

      // oldIp = ip;

      
      // extract the args into a, b, c for convenience:
      // a = -1;  b = -2;  c = -3;

      // numArgs is wrong for these guys, need one more!
      // if( op == callCode || op == jumpCode ||
      //           op == condJumpCode )
      // {
      //   actualNumArgs = numArgs( op ) + 1;
      // }
      // else
      //   actualNumArgs = numArgs( op );

      // if( actualNumArgs == 1 )
      // {  a = mem[ ip ];  ip++;  }
      // else if( actualNumArgs == 2 )
      // {  a = mem[ ip ];  ip++;  b = mem[ ip ]; ip++; }
      // else if( actualNumArgs == 3 )
      // {  a = mem[ ip ];  ip++;  b = mem[ ip ]; ip++; c = mem[ ip ]; ip++; }
 
      // implement all operations here:
      // ********************************************

      // put your work right here!

      op = mem[ip]; ip++;

      if ( op == labelCode) {
        // do nothing - labels have been replaced with actual cell references
      } else if (op == callCode) {
        // Store current instruction pointer and base pointer after pushed-value-cells
        // Then set instruction pointer to the cell reference (that used to be a label)
        // Then move stack pointer past pushed values, ip and bp
        mem[sp] = ip+1; // set the cell at the SP address to the return IP
        mem[sp + 1] = bp; // set the cell at the SP+1 address to the return BP
        bp = sp; // set the BP to the beginning of the new stack frame
        sp += (2 + numPassed); // move the SP to the new 'end of stack frame'
        ip = mem[ip]; // set the IP to the referenced cell
        numPassed = 0; // reset numPassed for future calls
      } else if (op == passCode) {
        mem[sp + 2 + numPassed] = mem[bp + 2 + mem[ip]]; numPassed++; ip++;
      } else if (op == allocCode) {
        sp += mem[ip]; ip++; // increase sp by n for storing local variables
      } else if (op == returnCode) {
        rv = mem[bp + 2 + mem[ip]]; // store the return value in RV register
        ip = mem[bp]; // set the IP to the return IP value stored at the BP
        sp = bp; // restore the SP to the end of the previous stack frame
        bp = mem[bp + 1]; // set the BP to the return BP value stored at BP+1
      } else if (op == getRetvalCode) {
        mem[bp + 2 + mem[ip]] = rv; ip++;
      } else if (op == jumpCode) {
        ip = mem[ip];
      } else if (op == condJumpCode) {
        int jmpTo = mem[ip]; ip++;
        if (mem[bp + 2 + mem[ip]] != 0) ip = jmpTo; else ip++;
      } else if (op == addCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] + mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == subCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] - mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == multCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] * mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == divCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] / mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == remCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] % mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == equalCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] == mem[bp + 2 + mem[++ip]] ? 1 : 0; ip++;
      } else if (op == notEqualCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] != mem[bp + 2 + mem[++ip]] ? 1 : 0; ip++;
      } else if (op == lessCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] < mem[bp + 2 + mem[++ip]] ? 1 : 0; ip++;
      } else if (op == lessEqualCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] <= mem[bp + 2 + mem[++ip]] ? 1 : 0; ip++;
      } else if (op == andCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] & mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == orCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] | mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == notCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]] == 0 ? 1 : 0; ip++;
      } else if (op == oppCode) {
        mem[bp + 2 + mem[ip]] = -mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == litCode) {
        mem[bp + 2 + mem[ip]] = mem[++ip]; ip++; // Literal, so actual value stored in this memory cell
      } else if (op == copyCode) {
        mem[bp + 2 + mem[ip]] = mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == getCode) {
        mem[bp + 2 + mem[ip]] = mem[(mem[bp + 2 + mem[++ip]] + mem[bp + 2 + mem[++ip]])]; ip++; // get value in heap at two addresses added together
      } else if (op == putCode) {
        mem[(mem[bp + 2 + mem[ip]] + mem[bp + 2 + mem[++ip]])] = mem[bp + 2 + mem[++ip]]; ip++; // put value in cell at combined address location
      } else if (op == haltCode) {
        done = true;
      } else if (op == inputCode) {
        System.out.print("? ");
        mem[bp + 2 + mem[ip]] = keys.nextInt(); ip++;
      } else if (op == outputCode) {
        System.out.println(mem[bp + 2 + mem[ip]]); ip++;
      } else if (op == newlineCode) {
        System.out.println();
      } else if (op == symbolCode) {
        if (mem[bp + 2 + mem[ip]] >= 32 && mem[bp + 2 + mem[ip]] <= 126) {
          System.out.print((char)mem[bp + 2 + mem[ip]]);
        }
        ip++;
      } else if (op == newCode) {
        mem[bp + 2 + mem[ip]] = hp -= mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == allocGlobalCode) {
        sp = codeEnd + 1 + mem[ip];
        gp = codeEnd + 1;
        ip++;
      } else if (op == toGlobalCode) {
        mem[gp + mem[ip]] = mem[bp + 2 + mem[++ip]]; ip++;
      } else if (op == fromGlobalCode) {
        mem[bp + 2 + mem[ip]] = mem[gp + mem[++ip]]; ip++;
      }

      else
      {
        System.out.println("Fatal error: unknown opcode [" + op + "]" );
        System.exit(1);
      }
       
      // step++;

    } while( !done );
    

  }// main

  // use symbolic names for all opcodes:

  // op to produce comment
  private static final int noopCode = 0;

  // ops involved with registers
  private static final int labelCode = 1;
  private static final int callCode = 2;
  private static final int passCode = 3;
  private static final int allocCode = 4;
  private static final int returnCode = 5;  // return a means "return and put
           // copy of value stored in cell a in register rv
  private static final int getRetvalCode = 6;//op a means "copy rv into cell a"
  private static final int jumpCode = 7;
  private static final int condJumpCode = 8;

  // arithmetic ops
  private static final int addCode = 9;
  private static final int subCode = 10;
  private static final int multCode = 11;
  private static final int divCode = 12;
  private static final int remCode = 13;
  private static final int equalCode = 14;
  private static final int notEqualCode = 15;
  private static final int lessCode = 16;
  private static final int lessEqualCode = 17;
  private static final int andCode = 18;
  private static final int orCode = 19;
  private static final int notCode = 20;
  private static final int oppCode = 21;
  
  // ops involving transfer of data
  private static final int litCode = 22;  // litCode a b means "cell a gets b"
  private static final int copyCode = 23;// copy a b means "cell a gets cell b"
  private static final int getCode = 24; // op a b means "cell a gets
                                                // contents of cell whose 
                                                // index is stored in b"
  private static final int putCode = 25;  // op a b means "put contents
     // of cell b in cell whose offset is stored in cell a"

  // system-level ops:
  private static final int haltCode = 26;
  private static final int inputCode = 27;
  private static final int outputCode = 28;
  private static final int newlineCode = 29;
  private static final int symbolCode = 30;
  private static final int newCode = 31;
  
  // global variable ops:
  private static final int allocGlobalCode = 32;
  private static final int toGlobalCode = 33;
  private static final int fromGlobalCode = 34;

  // debug ops:
  private static final int debugCode = 35;

  // return the number of arguments after the opcode,
  // except ops that have a label return number of arguments
  // after the label, which always comes immediately after 
  // the opcode
  private static int numArgs( int opcode )
  {
    // highlight specially behaving operations
    if( opcode == labelCode ) return 1;  // not used
    else if( opcode == jumpCode ) return 0;  // jump label
    else if( opcode == condJumpCode ) return 1;  // condJump label expr
    else if( opcode == callCode ) return 0;  // call label

    // for all other ops, lump by count:

    else if( opcode==noopCode ||
             opcode==haltCode ||
             opcode==newlineCode ||
             opcode==debugCode
           ) 
      return 0;  // op

    else if( opcode==passCode || opcode==allocCode || 
             opcode==returnCode || opcode==getRetvalCode || 
             opcode==inputCode || 
             opcode==outputCode || opcode==symbolCode ||
             opcode==allocGlobalCode
           )  
      return 1;  // op arg1

    else if( opcode==notCode || opcode==oppCode || 
             opcode==litCode || opcode==copyCode || opcode==newCode ||
             opcode==toGlobalCode || opcode==fromGlobalCode

           ) 
      return 2;  // op arg1 arg2

    else if( opcode==addCode ||  opcode==subCode || opcode==multCode ||
             opcode==divCode ||  opcode==remCode || opcode==equalCode ||
             opcode==notEqualCode ||  opcode==lessCode || 
             opcode==lessEqualCode || opcode==andCode ||
             opcode==orCode || opcode==getCode || opcode==putCode
           )
      return 3;
   
    else
    {
      System.out.println("Fatal error: unknown opcode [" + opcode + "]" );
      System.exit(1);
      return -1;
    }

  }// numArgs

  private static void showMem( int a, int b )
  {
    for( int k=a; k<=b; ++k )
    {
      System.out.println( k + ": " + mem[k] );
    }
  }// showMem

}// VPL