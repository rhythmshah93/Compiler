import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * implements the main function that gets the file name and calls the scanner and parser 
 */

/**
 * @author Danny Reinheimer
 *
 */
public class CodeGenerator {

	
	/**
	 * starting point for the program
	 * @param args The file name to read in and parse
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException {
		// checks to see if we are given any arguments
		if(args.length < 1) {
			System.out.println("Please provide an input file to process");
			System.exit(0);
		}
		Vector<Pair<TokenNames,String>> scannedTokens = new Vector<Pair<TokenNames,String>>();
		Vector<Pair<TokenNames,String>> scannedTokens1 = new Vector<Pair<TokenNames,String>>();
		// run initialize and run the scanner
		Scanner scanner = new Scanner(args[0]);
		scannedTokens = scanner.runScanner();
		scannedTokens1 = scanner.runScanner();
		// initialize and run the parser
		OriginalRecursiveParsing RP = new OriginalRecursiveParsing(scannedTokens);
		String fileName = "temp.c";
		PrintWriter pw = new PrintWriter(new File(fileName));
		scanner.printMetaStatements(pw);
		RP.parse(pw);
		VariableHashmap map = RP.getVariableHashmap();
		RecursiveParsing RP1 = new RecursiveParsing(scannedTokens1,map);
		String fileName1 = "out.c";
		PrintWriter pw1 = new PrintWriter(new File(fileName1));
		scanner.printMetaStatements(pw1);
		RP1.parse(pw1);
		pw.close();
		pw1.close();
		System.out.println("The generated output file is "+fileName);

	}
	
	
	
	

}
