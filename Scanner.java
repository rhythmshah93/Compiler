import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

/**
 * This is the main class for the Scanner
 */

/**
 * @author Danny Reinheimer
 *
 */
public class Scanner {
	
	private String fileName;
	private Vector<String> MetaStatements;
	
	public Scanner(String fileName) {
		this.fileName = fileName;
		MetaStatements = new Vector<String>();
	}

	public Vector<Pair<TokenNames, String>> runScanner() {
		// checks to see if we are given any arguments
//		if(args.length < 1) {
//			System.out.println("Please provide an input file to process");
//			System.exit(0);
//		}
		
		//String fileName = args[0];
		Scan scan = new Scan(fileName);
		Vector<Pair<TokenNames,String>> outputTokens = new Vector<Pair<TokenNames,String>>();
		Pair<TokenNames,String> tokenPair;
		
		
		// get the name of the file minus the dot 
//			int pos = fileName.lastIndexOf(".");
//			String newFileName = fileName.substring(0, pos) + "_gen.c";
//			PrintWriter writer = new PrintWriter(newFileName,"UTF-8");
		
		// keep getting the next token until we get a null
		while((tokenPair = scan.getNextToken()) != null) {
			if(tokenPair.getKey() != TokenNames.Space && tokenPair.getKey() != TokenNames.MetaStatements) {
				outputTokens.addElement(tokenPair);
			}
			if(tokenPair.getKey() == TokenNames.MetaStatements)
				MetaStatements.add(tokenPair.getValue());
		}
		tokenPair = new Pair(TokenNames.eof, "");
		outputTokens.add(tokenPair);
		
		return outputTokens;
	}

	public void printMetaStatements(PrintWriter writer) throws FileNotFoundException
	{
		for(int i=0; i<MetaStatements.size(); i++)
		{
			writer.print(MetaStatements.elementAt(i));
//			System.out.println(MetaStatements.elementAt(i));
		}
	}
}
