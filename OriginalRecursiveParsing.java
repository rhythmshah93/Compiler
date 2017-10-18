import java.io.PrintWriter;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements the recursive decent parser 
 */

/**
 * @author Kannan
 * @author Danny Reinheimer
 *
 */
public class OriginalRecursiveParsing {

	private static int numVariables; // Keeps track of the number of variables
	private static int numFunctions; // Keeps track of the number of functions
	private static int numStatements; // Keeps track of the number of statements
	private static Vector<Pair<TokenNames, String>> inputTokens; // Stores the
																	// set of
																	// input
																	// tokens
	private static TokenNames currentToken; // shows what the current token
											// removed from the stack was for
											// debug purposes
	private static String currFunction;
	private static Pair<TokenNames, String> currentPair;
	private static VariableHashmap vHMap;
	private static String temp;
	private int labelIndex;
	private int whileIfLabel;
	private int whileRestLabel;
	public HashMap<String,Node> func_stmts;

	/**
	 * Constructor initializes the fields and get the list of input tokens
	 * 
	 * @param inputTokens1
	 */
	public OriginalRecursiveParsing(Vector<Pair<TokenNames, String>> inputTokens1) {
		numFunctions = 0;
		numVariables = 0;
		numStatements = 0;
		inputTokens = inputTokens1;
		currentToken = TokenNames.None;
		currFunction = "global"; //contains the name of the function being parsed or global
		vHMap = new VariableHashmap(); //id hashmap
		currentPair = new Pair<TokenNames, String>(TokenNames.None, "");
		temp = "";
		labelIndex = 0;	//counter for labels
		whileIfLabel = -1; //current if label for a while statement
		whileRestLabel = -1; //current rest label for a while statement
		func_stmts = new HashMap<String,Node>();
	}

	public VariableHashmap getVariableHashmap()
	{
		return vHMap;
	}

	/**
	 * initialized the parsing, converts the code into a tree and prints out the results when finished
	 */
	public void parse(PrintWriter pw) {
		Node progNode = new Node();
		//Node to declare global int array
		progNode.addChild(createDataDeclNode("global"));
		//Program node
		progNode.addChild(program());
		if (inputTokens.firstElement().getKey() == TokenNames.eof) {
			System.out.println("Pass variable " + numVariables + " function " + numFunctions + " statement " + numStatements);
			//Print the constructed tree
			progNode.print(pw, vHMap);
			// pw.print("\n\n\nchanged output \n\n\n");
			// progNode.print1(pw,vHMap);
			// printing the statements of each function
			// pw.print("Iterating through the hasmap of function statements\n");
			// for(Map.Entry<String,Node> entry : func_stmts.entrySet())
			// {
			// 	String key = entry.getKey();
			// 	Node tmpNode = entry.getValue();
			// 	tmpNode.print(pw,vHMap);
			// }
			// pw.print("finished iterating through the functions\n");
		} else {
			System.out.println("error");
		}
	}

	/**
	 * <program> --> <type name> ID <data decls> <func list> | empty
	 * 
	 * @return A node for <program> if passed or null if failed
	 */
	private Node program() {
		Node programNode = new Node();
		// check if we are at the eof
		if (inputTokens.firstElement().getKey() == TokenNames.eof) {
			return programNode;
		} else {
			Node tNode = type_name();
			if (tNode != null) {
				if (inputTokens.firstElement().getKey() == TokenNames.ID) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey(); // get the ID token
					temp = currentPair.getValue();
					if (inputTokens.elementAt(0).getKey() != TokenNames.left_parenthesis) {
						// A <id>
						//add data declaration to hashmap
						AddIdToHashMap(currFunction, temp);
					} else {
						//A function declaration
						programNode.addChild(tNode);
						programNode.addChild(createTerminal(temp));
					}
					if (programNode.addChild(data_decls())
							&& programNode.addChild(func_list(temp))) {
						// check to see if the remaining token is eof is so this
						// is a legal syntax
						if (inputTokens.firstElement().getKey() == TokenNames.eof) {
							return programNode;
						}
					}
				}
			}
		}
		return null;
	}

	/*
	 * Adds idName to the hashmap corresponding to functionName
	 */
	private void AddIdToHashMap(String functionName, String idName) {
		if (!vHMap.containsKey(functionName, idName))
			vHMap.put(functionName, idName);
	}

	/*
	 * @return A node for <id> with name value if passed or null if failed
	 */
	private Node createIdNode(String value) {
		Node idNode = new Node();
		int index;
		//checks whether <id> with value is present in current function'c hashmap 
		if (!currFunction.equals("global")
				&& vHMap.containsKey(currFunction, value)) {
			index = vHMap.get(currFunction, value);
			idNode.index = index;
			idNode.setTerminalValue("local[" + index + "]");
			return idNode;
		} else 
			//checks whether <id> with value is present in global hashmap
			if (vHMap.containsKey("global", value)) {
			index = vHMap.get("global", value);
			idNode.local = false;//id corresponds to global array
			idNode.index = index;
			idNode.setTerminalValue("global[" + index + "]");
			return idNode;
		}

		return null;
	}

	/**
	 * <func list> --> empty | left_parenthesis <parameter list>
	 * right_parenthesis <func Z> <func list Z>
	 * 
	 * @return A node for <func list> if passed or null if failed
	 */
	private Node func_list(String func_name) {
		Node fNode = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			fNode.addChild(createTerminal(currentPair.getValue()));
			currFunction = temp;
			if (fNode.addChild(parameter_list())) {
				if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					fNode.addChild(createTerminal(currentPair.getValue()));
					if (fNode.addChild(func_Z(func_name))) {
						if (fNode.addChild(func_list_Z()))
						{
							// func_stmts.put(temp,fNode);
							return fNode;
						}
						else
							return null;
					}
					return null;
				}
				return null;
			}
			return null;
		}
		return fNode;
	}

	/**
	 * @return a node with string value
	 */
	private Node createTerminal(String value) {
		Node child = new Node();
		child.setTerminalValue(value);
		return child;
	}

	/**
	 * <func Z> --> semicolon | left_brace <data decls Z> <statements>
	 * right_brace
	 * 
	 * @return a node for <func Z> if passed or null if failed
	 */
	private Node func_Z(String func_name) {
		Node f = new Node();
		// checks if the next token is a semicolon
		if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
			currentToken = inputTokens.remove(0).getKey(); 
			f.addChild(createSemiColonNode());
			return f;
		}

		if (inputTokens.firstElement().getKey() == TokenNames.left_brace) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			f.addChild(createTerminal(currentPair.getValue()+"\n"));
			if (data_decls_Z() != null) {
				//local array declaration
				f.addChild(createDataDeclNode(currFunction));
				if (f.addChild(statements())) {
					if (inputTokens.firstElement().getKey() == TokenNames.right_brace) {
						currentPair = inputTokens.remove(0);
						currentToken = currentPair.getKey();
						f.addChild(createTerminal(currentPair.getValue()+"\n"));
						// Count the number of function definitions
						numFunctions += 1;
						currFunction = "global";
						func_stmts.put(func_name,f);
						return f;
					}
					return null;
				}
				return null;
			}
			return null;
		}
		return null;
	}

	/**
	 * @param value = functionName
	 * @return a node for array declaration(local/global)
	 */
	private Node createDataDeclNode(String value) {
		Node d = new Node();
		d.setDataDeclValue(value);
		return d;
	}

	/**
	 * <func list Z> --> empty | <type name> ID left_parenthesis <parameter
	 * list> right_parenthesis <func Z> <func list Z>
	 * 
	 * @return a node for <func list Z> if passed or null if failed
	 */
	private Node func_list_Z() {
		Node fNode = new Node();
		if (fNode.addChild(type_name())) {
			if (inputTokens.firstElement().getKey() == TokenNames.ID) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
				fNode.addChild(createTerminal(currentPair.getValue()));
				temp = currentPair.getValue();
				if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					fNode.addChild(createTerminal(currentPair.getValue()));
					currFunction = temp;
					if (fNode.addChild(parameter_list())) {
						if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
							currentPair = inputTokens.remove(0);
							currentToken = currentPair.getKey();
							fNode.addChild(createTerminal(currentPair
									.getValue()));
							if (fNode.addChild(func_Z(currFunction))) {
								if (fNode.addChild(func_list_Z()))
									return fNode;
								else
									return null;
							}
						}
					}
				}
			}
			return null;
		}
		// return true for the empty rule
		return fNode;
	}

	/**
	 * <type name> --> int | void | binary | decimal
	 * 
	 * @return A node for <type name> if passed or null if failed
	 */
	private Node type_name() {
		Node tNode = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.Int
				|| inputTokens.firstElement().getKey() == TokenNames.Void
				|| inputTokens.firstElement().getKey() == TokenNames.binary
				|| inputTokens.firstElement().getKey() == TokenNames.decimal) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			tNode.setTerminalValue(currentPair.getValue());
			return tNode;
		}
		return null;
	}

	/**
	 * <parameter list> --> empty | void <parameter list Z> | <non-empty list>
	 * 
	 * @return a node for <parameter list> if passed or null if failed
	 */
	private Node parameter_list() {
		Node pNode = new Node();
		// void <parameter list Z>
		if (inputTokens.firstElement().getKey() == TokenNames.Void) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			pNode.addChild(createTerminal(currentPair.getValue()));
			if (pNode.addChild(parameter_list_Z()))
				return pNode;
			else
			return null;
		}
		// <non-empty list>
		else if (pNode.addChild(non_empty_list())) {
			return pNode;
		}
		// empty
		return pNode;
	}

	/**
	 * <parameter list Z> --> empty | ID <non-empty list prime>
	 * 
	 * @return a node for <parameter list Z> if passed or null if failed
	 */
	private Node parameter_list_Z() {
		Node p = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.ID) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			p.addChild(createTerminal(currentPair.getValue()));
			//paramters also need to be equated to array elements
			AddParameterToHashMap(currFunction, currentPair.getValue());
			if (p.addChild(non_empty_list_prime()))
				return p;
			else
			return null;
		}
		return p;
	}

	/**
	 * Adding the parameter to hashmap corresponding to function funcName
	 */
	public void AddParameterToHashMap(String funcName, String parameterName) {
		vHMap.putParameter(funcName, parameterName);
	}

	/**
	 * <non-empty list> --> int ID <non-empty list prime> | binary ID <non-empty
	 * list prime> | decimal ID <non-empty list prime>
	 * 
	 * @return a node for <non-empty list> if passed or null if failed
	 */
	private Node non_empty_list() {
		Node n = new Node();
		// check for int, binary, decimal
		if (inputTokens.firstElement().getKey() == TokenNames.Int
				|| inputTokens.firstElement().getKey() == TokenNames.binary
				|| inputTokens.firstElement().getKey() == TokenNames.decimal) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			n.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.ID) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
				n.addChild(createTerminal(currentPair.getValue()));
				//adding the parameter to hashmap
				AddParameterToHashMap(currFunction, currentPair.getValue());
				if (n.addChild(non_empty_list_prime()))
					return n;
				else
					return null;
			}
		}
		return null;
	}

	/**
	 * <non-empty list prime> --> comma <type name> ID <non-empty list prime> |
	 * empty
	 * 
	 * @return a node for <non-empty list prime> if passed or null if failed
	 */
	private Node non_empty_list_prime() {
		Node n = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.comma) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			n.addChild(createTerminal(currentPair.getValue()));
			if (n.addChild(type_name())) {
				if (inputTokens.firstElement().getKey() == TokenNames.ID) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					n.addChild(createTerminal(currentPair.getValue()));
					//adding parameter to hashmap
					AddParameterToHashMap(currFunction, currentPair.getValue());
					if (n.addChild(non_empty_list_prime()))
						return n;
					else
						return null;
				}
				return null;
			}
			return null;
		}
		return n;
	}

	/**
	 * <data decls> --> empty | <id list Z> semicolon <program> | <id list
	 * prime> semicolon <program>
	 * 
	 * @return a <data decls> node if passed or null if failed
	 */
	private Node data_decls() {
		Node dNode = new Node();
		if (id_list_Z() != null) {
			if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
				currentToken = inputTokens.remove(0).getKey();
				// dNode.addChild(createSemiColonNode());
				// count variable
				numVariables += 1;
				if (dNode.addChild(program())) // data_decls_Z();
					return dNode;
				else
					return null;
			}
			return null;
		}
		if (id_list_prime() != null) {
			if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
				currentToken = inputTokens.remove(0).getKey();
				// dNode.addChild(createSemiColonNode());
				// since we consume the first id before we get here count this
				// as a variable
				numVariables += 1;
				if (dNode.addChild(program()))
					return dNode;// data_decls_Z();
				else
					return null;
			}
			// return false;
		}
		return dNode;
	}

	/**
	 * @return A node for <semicolon>
	 */
	private Node createSemiColonNode() {
		Node child = new Node();
		child.setTerminalValue(";\n");
		return child;
	}

	/**
	 * <data decls Z> --> empty | int <id list> semicolon <data decls Z> | void
	 * <id list> semicolon <data decls Z> | binary <id list> semicolon <data
	 * decls Z> | decimal <id list> semicolon <data decls Z>
	 * 
	 * @return A Node for <data decls Z> if passed or null if failed
	 */
	private Node data_decls_Z() {
		Node dNode = new Node();
		if (type_name() != null) {
			if (id_list() != null) {
				if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
					currentToken = inputTokens.remove(0).getKey();
					if (data_decls_Z() != null)
						return dNode;
					else
						return null;
				}
				return null;
			}
			return null;
		}
		return dNode;
	}

	/**
	 * <id list> --> <id> <id list prime>
	 * 
	 * @return a node for <id list> if passed or null if failes
	 */
	private Node id_list() {
		Node n = new Node();
		if (n.addChild(id())) {
			if (n.addChild(id_list_prime()))
				return n;
			else
				return null;
		}
		return null;
	}

	/**
	 * <id list Z> --> left_bracket <expression> right_bracket <id list prime>
	 * 
	 * @return a node <id list z> if passed or null if failed
	 */
	private Node id_list_Z() {
		Node i = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.left_bracket) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			// i.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.NUMBER) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
				vHMap.incrementNumVariables(currFunction,
						Integer.parseInt(currentPair.getValue()) - 1);
				if (inputTokens.firstElement().getKey() == TokenNames.right_bracket) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					// i.addChild(createTerminal(currentPair.getValue()));
					if (i.addChild(id_list_prime()))
						return i;
					else
						return null;
				}
			}
		}
		return null;
	}

	/**
	 * <id list prime> --> comma <id> <id list prime> | empty
	 * 
	 * @return a node <id list prime> if passed or null if failed
	 */
	private Node id_list_prime() {
		Node i = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.comma) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			// i.addChild(createTerminal(currentPair.getValue()));
			if (id() != null) {
				if (i.addChild(id_list_prime()))
					return i;
				else
					return null;
			}
			return null;
		}
		return i;
	}

	/**
	 * <id> --> ID <id Z>
	 * 
	 * @return a node <id Z> if passed or null if failed
	 */
	private Node id() {
		Node i = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.ID) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			AddIdToHashMap(currFunction, currentPair.getValue());
			if (i.addChild(id_Z()))
				return i;
			else
				return null;
		}
		return null;
	}

	/**
	 * <id Z> --> left_bracket <expression> right_bracket | empty
	 * 
	 * @return a node <id Z> if passed or null if failed
	 */
	private Node id_Z() {
		Node i = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.left_bracket) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			// i.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.NUMBER) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
				vHMap.incrementNumVariables(currFunction,
						Integer.parseInt(currentPair.getValue()) - 1);
				if (inputTokens.firstElement().getKey() == TokenNames.right_bracket) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					// i.addChild(createTerminal(currentPair.getValue()));
					// count the number of variables
					numVariables += 1;
					return i;
				}
				return null;
			}
			return null;
		}
		// count the number of variables
		numVariables += 1;
		return i;
	}

	/**
	 * <block statements> --> left_brace <statements> right_brace
	 * 
	 * @return a node <block statements> if passed or null if failed
	 */
	private Node block_statements() {
		Node b = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.left_brace) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
		//	b.addChild(createTerminal(currentPair.getValue()));
			if (b.addChild(statements())) {
				if (inputTokens.firstElement().getKey() == TokenNames.right_brace) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
				//	b.addChild(createTerminal(currentPair.getValue()));
					return b;
				}
			}
		}
		return null;
	}

	/**
	 * <statements> --> empty | <statement> <statements>
	 * 
	 * @return a node if passed or null if failed
	 */
	private Node statements() {
		Node s = new Node();
		if (s.addChild(statement())) {
			numStatements += 1;
			if (s.addChild(statements()))
				return s;
			else
				return null;
		}
		return s;
	}

	/**
	 * <statement> --> ID <statement Z> | <if statement> | <while statement> |
	 * <return statement> | <break statement> | <continue statement> | read
	 * left_parenthesis ID right_parenthesis semicolon | write left_parenthesis
	 * <expression> right_parenthesis semicolon | print left_parenthesis STRING
	 * right_parenthesis semicolon
	 * 
	 * @return a boolean indicating if the rule passed or failed
	 */
	private Node statement() {
		Node s = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.ID) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			/*
			 * if (inputTokens.elementAt(0).getKey() ==
			 * TokenNames.left_parenthesis)
			 * s.addChild(createTerminal(currentPair.getValue())); else
			 * s.addChild(createIdNode(currentPair.getValue()));
			 */
			if (s.addChild(statement_Z(currentPair.getValue())))
				return s;
			else
				return null;
		}
		if (s.addChild(if_statement())) {
			return s;
		}
		if (s.addChild(while_statement())) {
			return s;
		}
		if (s.addChild(return_statement())) {
			return s;
		}
		if (s.addChild(break_statement())) {
			return s;
		}
		if (s.addChild(continue_statement())) {
			return s;
		}
		if (inputTokens.firstElement().getKey() == TokenNames.read) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			s.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
				s.addChild(createTerminal(currentPair.getValue()));
				if (inputTokens.firstElement().getKey() == TokenNames.ID) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					s.addChild(createIdNode(currentPair.getValue()));
					if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
						currentPair = inputTokens.remove(0);
						currentToken = currentPair.getKey();
						s.addChild(createTerminal(currentPair.getValue()));
						if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
							currentPair = inputTokens.remove(0);
							currentToken = currentPair.getKey();
							s.addChild(createSemiColonNode());
							return s;
						}
					}
				}
			}
			return null;
		}

		// write left_parenthesis <expression> right_parenthesis semicolon
		if (inputTokens.firstElement().getKey() == TokenNames.write) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			Node writeNode = createTerminal(currentPair.getValue());
			if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
				Node lp = createTerminal(currentPair.getValue());
				// s.addChild(createTerminal(currentPair.getValue()));
				Node expNode = expression();
				if (s.addChild(expNode)) {
					s.addChild(writeNode);
					s.addChild(lp);
					s.addChild(createTerminal("local[" + expNode.index + "]"));
					if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
						currentPair = inputTokens.remove(0);
						currentToken = currentPair.getKey();
						s.addChild(createTerminal(currentPair.getValue()));
						if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
							currentPair = inputTokens.remove(0);
							currentToken = currentPair.getKey();
							s.addChild(createSemiColonNode());
							return s;
						}
					}
				}
			}
			return null;
		}

		// print left_parenthesis STRING right_parenthesis semicolon
		if (inputTokens.firstElement().getKey() == TokenNames.print) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			s.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
				s.addChild(createTerminal(currentPair.getValue()));
				if (inputTokens.firstElement().getKey() == TokenNames.STRING) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					s.addChild(createTerminal(currentPair.getValue()));
					if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
						currentPair = inputTokens.remove(0);
						currentToken = currentPair.getKey();
						s.addChild(createTerminal(currentPair.getValue()));
						if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
							currentPair = inputTokens.remove(0);
							currentToken = currentPair.getKey();
							s.addChild(createSemiColonNode());
							return s;
						}
					}
				}
			}
			return null;
		}
		return null;
	}

	/**
	 * <statement Z> --> <assignment Z> | <func call>
	 * 
	 * @return a node if the rule passed or failed
	 */
	private Node statement_Z(String value) {
		Node s = new Node();
		if (s.addChild(assignment_Z(value))) {
			return s;
		} else if (s.addChild(func_call(value))) {
			return s;
		}
		return null;
	}

	/**
	 * <assignment Z> --> equal_sign <expression> semicolon | left_bracket
	 * <expression> right_bracket equal_sign <expression> semicolon
	 * @param value = name of left side id
	 * @return a node if rule passed or null if failed
	 */
	private Node assignment_Z(String value) {
		Node a = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.equal_sign) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			Node eNode = createTerminal(currentPair.getValue());
			Node expNode = expression();
			if (a.addChild(expNode)) {
				a.addChild(createIdNode(value));
				a.addChild(eNode);
				a.addChild(createTerminal("local[" + expNode.index + "]"));
				if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					a.addChild(createSemiColonNode());
					return a;
				}
			}
			return null;
		}
		if (inputTokens.firstElement().getKey() == TokenNames.left_bracket) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			Node expNode = expression();
			if (a.addChild(expNode)) {
				if (inputTokens.firstElement().getKey() == TokenNames.right_bracket) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					if (inputTokens.firstElement().getKey() == TokenNames.equal_sign) {
						currentPair = inputTokens.remove(0);
						currentToken = currentPair.getKey();
						// a.addChild(createTerminal(currentPair.getValue()));
						Node exp1Node = expression();
						if (a.addChild(exp1Node)) {
							if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
								currentPair = inputTokens.remove(0);
								currentToken = currentPair.getKey();
								/* get the base indes*/
								int idBaseIndex = vHMap
										.getNumVariables(currFunction);
								vHMap.incrementNumVariables(currFunction, 1);
								a.addChild(createTerminal("local["
										+ idBaseIndex + "] ="));
								Node id = createIdNode(value);
								int id_base = id.index;
								a.addChild(createTerminal(Integer
										.toString(id_base)));
								a.addChild(createSemiColonNode());
								/* get the array element index*/
								int arrayIndex = vHMap
										.getNumVariables(currFunction);
								vHMap.incrementNumVariables(currFunction, 1);
								a.addChild(createTerminal("local[" + arrayIndex
										+ "] ="));
								a.addChild(createTerminal("local["
										+ expNode.index + "] +"));
								a.addChild(createTerminal("local["
										+ idBaseIndex + "]"));
								a.addChild(createSemiColonNode());
								/* assign exp1 to array element*/
								if(id.local == true)
								a.addChild(createTerminal("local[local["
										+ arrayIndex + "]] = local["
										+ exp1Node.index + "]"));
								else
									a.addChild(createTerminal("global[local["
											+ arrayIndex + "]] = local["
											+ exp1Node.index + "]"));	
								a.addChild(createSemiColonNode());
								return a;
							}
						}
					}
				}
			}
			return null;
		}
		return null;
	}

	/**
	 * <func call> --> left_parenthesis <expr list> right_parenthesis semicolon
	 * 
	 * @return a node if rule passed or null if rule failed
	 */
	private Node func_call(String value) {
		Node f = new Node();
		Node new_f = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
	//		f.addChild(createTerminal(currentPair.getValue()));
			Node exprNode = expr_list();
			if (f.addChild(exprNode)) {
				if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
			//		f.addChild(createTerminal(currentPair.getValue()));
					if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
						currentToken = inputTokens.remove(0).getKey();
			/*			int index = vHMap.getNumVariables(currFunction);
						vHMap.incrementNumVariables(currFunction, 1);
						f.addChild(createTerminal("local[" + index + "] ="));*/
						/*call function using expression list as parameter*/
						f.addChild(createTerminal(value));
						f.addChild(createTerminal("("));
						Vector<Integer> paramList = exprNode.getIndexList();
						for(int i=0; i<paramList.size() - 1 ; i++)
						{
							f.addChild(createTerminal("local["+paramList.elementAt(i)+"],"));
						}
						if(paramList.size() > 0)
						{
							f.addChild(createTerminal("local["+paramList.elementAt(paramList.size() - 1)+"]"));
						}
						f.addChild(createTerminal(")"));
						f.addChild(createSemiColonNode());
						return f;
						// func_call_map.put(funcid_count,f);
						// new_f.addChild(createFuncNode(value,funcid_count));
						// funcid_count += 1;
						// return new_f;
					}
				}
			}
		}
		return null;
	}


	/**
	 * <expr list> --> empty | <non-empty expr list>
	 * 
	 * @return a node if rule passed or null if failed
	 */
	private Node expr_list() {
		Node e = new Node();
		Node neel = non_empty_expr_list();
		if (e.addChild(neel)) {
			/* indexList contains list of local array indexes with parameter values*/
			e.concatIndexList(neel.getIndexList());
			return e;
		}
		return e;
	}

	/**
	 * <non-empty expr list> --> <expression> <non-empty expr list prime>
	 * 
	 * @return a node if rule passed or null if failed
	 */
	private Node non_empty_expr_list() {
		Node n = new Node();
		Node exp = expression();
		if (n.addChild(exp)) {
			n.addToIndexList(exp.index);
			Node neelp = non_empty_expr_list_prime();
			if (n.addChild(neelp)){
				n.concatIndexList(neelp.getIndexList());
				return n;
			}
		}
		return null;
	}

	/**
	 * <non-empty expr list prime> --> comma <expression> <non-empty expr list
	 * prime> | empty
	 * 
	 * @return a node if rule passed or null if failed
	 */
	private Node non_empty_expr_list_prime() {
		Node n = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.comma) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			// n.addChild(createTerminal(currentPair.getValue()));
			Node exp = expression();
			if (n.addChild(exp)) {
				/* Add the value of expression to parameter list */
				n.addToIndexList(exp.index);
				Node neelp = non_empty_expr_list_prime();
				if (n.addChild(neelp)) {
					n.concatIndexList(neelp.getIndexList());
					return n;
				} else
					return null;
			}
			return null;
		}
		return n;
	}

	/**
	 * <if statement> --> if left_parenthesis <condition expression>
	 * right_parenthesis <block statements>
	 * modified to
	 * <if statement> --> if left_parenthesis <condition expression> right_expression
	 * goto iflabel semicolon
	 * goto restlabel2 semicolon
	 * iflabel colon semicolon
	 * <block statements>
	 * restlabel colon semicolon
	 * @return a node if rule passed or bull if failed
	 */
	private Node if_statement() {
		Node i = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.If) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			Node ifNode = createTerminal(currentPair.getValue());
		//	i.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
				 Node lp = createTerminal(currentPair.getValue());
		//		i.addChild(createTerminal(currentPair.getValue()));
				 Node condExp = condition_expression();
				if (i.addChild(condExp)) {
					if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
						currentPair = inputTokens.remove(0);
						currentToken = currentPair.getKey();
						i.addChild(ifNode);
						i.addChild(lp);
						i.addChild(createTerminal("local["+condExp.index+"]"));
						i.addChild(createTerminal(currentPair.getValue()));
						int iflabel = labelIndex;
						labelIndex++;
						i.addChild(createTerminal("goto c"+iflabel+";\n"));
						int restlabel = labelIndex;
						labelIndex++;
						i.addChild(createTerminal("goto c"+restlabel+";\n"));
						i.addChild(createTerminal("c"+iflabel+":;\n"));
						if (i.addChild(block_statements())){
							i.addChild(createTerminal("c"+restlabel+":;\n"));
							return i;
							}
						else return null;
					}
				}
			}
		}
		return null;
	}

	/**
	 * <condition expression> --> <condition> <condition expression Z>
	 * 
	 * @return a node if rule passed or null if failed
	 */
	private Node condition_expression() {
		Node c = new Node();
		Node cond = condition();
		if (cond!=null) {
			Node condExp = condition_expression_Z(cond);
			if (c.addChild(condExp)){
				c.index = condExp.index;
				return c;
			}
		}
		return null;
	}

	/**
	 * <condition expression Z> --> <condition op> <condition> | empty
	 * 
	 * @return a node if rule passed or null if failed
	 */
	private Node condition_expression_Z(Node prev) {
		Node c = new Node();
		Node condOpNode = condition_op();
		if (condOpNode != null) {
			Node condNode = condition();
			if (condNode != null)
			{
				c.addChild(prev);
				c.addChild(condNode);
				int index = vHMap.getNumVariables(currFunction);
				vHMap.incrementNumVariables(currFunction, 1);
				c.addChild(createTerminal("local[" + index + "] ="));
				c.addChild(createTerminal("local[" + prev.index + "]"));
				c.addChild(condOpNode);
				c.addChild(createTerminal("local[" + condNode.index + "]"));
				c.addChild(createSemiColonNode());
				c.index = index;
				return c;
			}
			else
				return null;
		}
		c.addChild(prev);
		c.index = prev.index;
		return c;
	}

	/**
	 * <condition op> --> double_end_sign | double_or_sign
	 * 
	 * @return a node if rule passed or null if failed
	 */
	private Node condition_op() {
		Node c = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.double_and_sign
				|| inputTokens.firstElement().getKey() == TokenNames.double_or_sign) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			c.addChild(createTerminal(currentPair.getValue()));
			return c;
		}
		return null;
	}

	/**
	 * <condition> --> <expression> <comparison op> <expression>
	 * 
	 * @return a node if rule passed or null if rule failed
	 */
	private Node condition() {
		Node c = new Node();
		Node exp = expression();
		if (c.addChild(exp)) {
			Node compOp = comparison_op();
			if (compOp != null) {
				Node exp2 = expression();
				if (c.addChild(exp2)){
					int index = vHMap.getNumVariables(currFunction);
					vHMap.incrementNumVariables(currFunction, 1);
					c.addChild(createTerminal("local[" + index + "] ="));
					c.addChild(createTerminal("local[" + exp.index + "]"));
					c.addChild(compOp);
					c.addChild(createTerminal("local[" + exp2.index + "]"));
					c.addChild(createSemiColonNode());
					c.index = index;
					return c;
					}
			}
		}
		return null;
	}

	/**
	 * <comparison op> --> == | != | > | >= | < | <=
	 * 
	 * @return a node if rule passed or null if failed
	 */
	private Node comparison_op() {
		Node c = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.doubleEqualSign
				|| inputTokens.firstElement().getKey() == TokenNames.notEqualSign
				|| inputTokens.firstElement().getKey() == TokenNames.greaterThenSign
				|| inputTokens.firstElement().getKey() == TokenNames.greaterThenOrEqualSign
				|| inputTokens.firstElement().getKey() == TokenNames.lessThenSign
				|| inputTokens.firstElement().getKey() == TokenNames.lessThenOrEqualSign) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			c.addChild(createTerminal(currentPair.getValue()));
			return c;
		}
		return null;
	}

	/**
	 * <while statement> --> while left_parenthesis <condition expression>
	 * right_parenthesis <block statements>
	 * modified to
	 * <while statement> --> whileIfLabel colon semicolon
	 * 						<if statement> left_parenthesis <condition expression> right_parenthesis
	 * 						goto whileBlockLabel semicolon
	 * 						goto whileRestLabel semicolon
	 * 						whileBlockLabel colon semicolon
	 * 						<block statements>
	 * 						goto whileIfLabel semicolon
	 * 						whileRestLabel colon semicolon
	 * @return
	 */
	private Node while_statement() {
		Node w = new Node();
		int prevWhileIfLabel = whileIfLabel;
		int prevWhileRestLabel = whileRestLabel;
		if (inputTokens.firstElement().getKey() == TokenNames.While) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
//			w.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
//				w.addChild(createTerminal(currentPair.getValue()));
				Node condExp = condition_expression();
				whileIfLabel = labelIndex;
				labelIndex++;
				w.addChild(createTerminal("c"+whileIfLabel+":;\n"));
				if (w.addChild(condExp)) {
					if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
						currentPair = inputTokens.remove(0);
						currentToken = currentPair.getKey();
						w.addChild(createTerminal("if(local["+condExp.index+"])"));
						int whileBlockLabel = labelIndex;
						labelIndex++;
						w.addChild(createTerminal("goto c"+whileBlockLabel+";\n"));
						whileRestLabel = labelIndex;
						labelIndex++;
						w.addChild(createTerminal("goto c"+whileRestLabel+";\n"));
					//	w.addChild(createTerminal(currentPair.getValue()));
						w.addChild(createTerminal("c"+whileBlockLabel+":;\n"));
						if (w.addChild(block_statements()))
							w.addChild(createTerminal("goto c"+whileIfLabel+";\n"));
							w.addChild(createTerminal("c"+whileRestLabel+":;\n"));
							whileIfLabel = prevWhileIfLabel;
							whileRestLabel = prevWhileRestLabel;
							return w;
					}
				}
			}
		}
		return null;
	}

	/**
	 * <return statement> --> return <return statement Z>
	 * 
	 * @return a node if passed or null if failed
	 */
	private Node return_statement() {
		Node r = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.Return) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			// r.addChild();
			if (r.addChild(return_statement_Z()))
				return r;

		}
		return null;
	}

	/**
	 * <return statement Z> --> <expression> semicolon | semicolon
	 * 
	 * @return a node if passed or null if failed
	 */
	private Node return_statement_Z() {
		Node r = new Node();
		Node exp = expression();
		if (exp != null) {
			r.addChild(exp);
			r.addChild(createTerminal("return"));
			r.addChild(createTerminal("local [" + exp.index + "]"));
			if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
				currentToken = inputTokens.remove(0).getKey();
				r.addChild(createSemiColonNode());
				return r;
			}
			return null;
		}
		if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
			currentToken = inputTokens.remove(0).getKey();
			r.addChild(createTerminal("return"));
			r.addChild(createSemiColonNode());
			return r;
		}
		return null;
	}

	/**
	 * <break statement> ---> break semicolon
	 * modified to 
	 * <break statement> ---> goto whileRestLabel semicolon
	 * @return a boolean
	 */
	private Node break_statement() {
		Node b = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.Break) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			b.addChild(createTerminal("goto c"+whileRestLabel));
	//		b.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
				currentToken = inputTokens.remove(0).getKey();
				b.addChild(createSemiColonNode());
				return b;
			}
		}
		return null;
	}

	/**
	 * <continue statement> ---> continue semicolon
	 * modified to 
	 * <continue statement> ---> goto whileIfLabel semicolon
	 * @return a node if rule passed or null if failed
	 */
	private Node continue_statement() {
		Node c = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.Continue) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			c.addChild(createTerminal("goto c"+whileIfLabel));
		//	c.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.semicolon) {
				currentToken = inputTokens.remove(0).getKey();
				c.addChild(createSemiColonNode());
				return c;
			}
		}
		return null;
	}

	/**
	 * <expression> --> <term> <expression prime>
	 * 
	 * @return a node if passed or null if failed
	 */
	private Node expression() {
		Node e = new Node();
		Node tNode = term();
		if (tNode != null) {
			Node expprime = expression_prime(tNode);
			if (expprime != null) {
				e.addChild(expprime);
				e.index = expprime.index;
				return e;
			}
		}
		return null;
	}

	/**
	 * <expression prime> --> <addop> <term> <expression prime> | empty
	 * @param prev is the previous node in the tree
	 * @return a node if passed or null if failed
	 */
	private Node expression_prime(Node prev) {
		Node e = new Node();
		Node opNode = addop();
		if (opNode != null) {
			Node tNode = term();
			if (tNode != null) {
				Node paramNode = new Node();
				paramNode.addChild(prev);
				paramNode.addChild(tNode);
				int index = vHMap.getNumVariables(currFunction);
				vHMap.incrementNumVariables(currFunction, 1);
				paramNode.addChild(createTerminal("local[" + index + "] ="));
				paramNode.addChild(createTerminal("local[" + prev.index + "]"));
				paramNode.addChild(opNode);
				paramNode.addChild(createTerminal("local[" + tNode.index
						+ "];\n"));
				paramNode.index = index;
				Node expprime = expression_prime(paramNode);
				if (e.addChild(expprime)) {
					e.index = expprime.index;
					return e;
				}
			}
			return null;
		}
		e.addChild(prev);
		e.index = prev.index;
		return e;
	}

	/**
	 * <addop> --> plus_sign | minus_sign
	 * 
	 * @return a node if passed or null if failed
	 */
	private Node addop() {
		Node a = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.plus_sign
				|| inputTokens.firstElement().getKey() == TokenNames.minus_sign) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			a.addChild(createTerminal(currentPair.getValue()));
			return a;
		}
		return null;
	}

	/**
	 * <term> --> <factor> <term prime>
	 * 
	 * @return a node if passed or null if failed
	 */
	private Node term() {
		Node t = new Node();
		Node factorNode = factor();
		if (factorNode != null) {
			Node tprimeNode = term_prime(factorNode);
			if (tprimeNode != null) {
				t.addChild(tprimeNode);
				t.index = tprimeNode.index;
				return t;
			}
		}
		return null;
	}

	/**
	 * <term prime> --> <mulop> <factor> <term prime> | empty
	 * @param prev is the presvios node in the tree
	 * @return node if passed or null if failed
	 */
	private Node term_prime(Node prev) {
		Node t = new Node();
		Node opNode = mulop();
		if (opNode != null) {
			Node facNode = factor();
			if (facNode != null) {
				Node paramNode = new Node();
				paramNode.addChild(prev);
				paramNode.addChild(facNode);
				int index = vHMap.getNumVariables(currFunction);
				vHMap.incrementNumVariables(currFunction, 1);
				paramNode.addChild(createTerminal("local[" + index + "] ="));
				paramNode.addChild(createTerminal("local[" + prev.index + "]"));
				paramNode.addChild(opNode);
				paramNode.addChild(createTerminal("local[" + facNode.index
						+ "];\n"));
				paramNode.index = index;
				Node tprime = term_prime(paramNode);
				if (t.addChild(tprime)) {
					t.index = tprime.index;
					return t;
				}
			}
			return null;
		}
		t.addChild(prev);
		t.index = prev.index;
		return t;
	}

	/**
	 * <mulop> --> star_sign | forward_slash
	 * 
	 * @return a boolean
	 */
	private Node mulop() {
		Node m = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.star_sign
				|| inputTokens.firstElement().getKey() == TokenNames.forward_slash) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			m.addChild(createTerminal(currentPair.getValue()));
			return m;
		}
		return null;
	}

	/**
	 * <factor> --> ID <factor Z> | NUMBER | minus_sign NUMBER |
	 * left_parenthesis <expression>right_parenthesis
	 * 
	 * @return
	 */
	private Node factor() {
		Node f = new Node();
		if (inputTokens.firstElement().getKey() == TokenNames.ID) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			Node fz = factor_Z(currentPair.getValue());
			if (fz != null) {
				f.addChild(fz);
				f.index = fz.index;
				return f;
			} else
				return null;
		}

		// NUMBER
		if (inputTokens.firstElement().getKey() == TokenNames.NUMBER) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			int index = vHMap.getNumVariables(currFunction);
			vHMap.incrementNumVariables(currFunction, 1);
			f.addChild(createTerminal("local[" + index + "] ="));
			f.addChild(createTerminal(currentPair.getValue()));
			f.addChild(createSemiColonNode());
			f.index = index;
			return f;
		}

		// minus_sign NUMBER
		if (inputTokens.firstElement().getKey() == TokenNames.minus_sign) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			int index = vHMap.getNumVariables(currFunction);
			vHMap.incrementNumVariables(currFunction, 1);
			f.addChild(createTerminal("local[" + index + "] ="));
			f.addChild(createTerminal(currentPair.getValue()));
			if (inputTokens.firstElement().getKey() == TokenNames.NUMBER) {
				currentPair = inputTokens.remove(0);
				currentToken = currentPair.getKey();
				f.addChild(createTerminal(currentPair.getValue()));
				f.addChild(createSemiColonNode());
				f.index = index;
				return f;
			}
			return null;
		}

		// left_parenthesis <expression>right_parenthesis
		if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			Node lp = createTerminal(currentPair.getValue());
			Node exp = expression();
			if (exp != null) {
				f.addChild(exp);
				int index = vHMap.getNumVariables(currFunction);
				vHMap.incrementNumVariables(currFunction, 1);
				f.addChild(createTerminal("local[" + index + "] ="));
				f.addChild(lp);
				f.addChild(createTerminal("local[" + exp.index + "]"));
				if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					f.addChild(createTerminal(currentPair.getValue()));
					f.addChild(createSemiColonNode());
					f.index = index;
					return f;
				}
			}
			return null;
		}
		return null;
	}

	/**
	 * <factor Z> --> left_bracket <expression> right_bracket | left_parenthesis
	 * <expr list> right_parenthesis | empty
	 * 
	 * @return
	 */
	private Node factor_Z(String value) {
		Node f = new Node();
		// left_bracket <expression> right_bracket
		if (inputTokens.firstElement().getKey() == TokenNames.left_bracket) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			Node lb = createTerminal(currentPair.getValue());
			Node exp = expression();
			if (exp != null) {
				f.addChild(exp);
				/* get the base index of array*/
				int idBaseIndex = vHMap.getNumVariables(currFunction);
				vHMap.incrementNumVariables(currFunction, 1);
				f.addChild(createTerminal("local[" + idBaseIndex + "] ="));
				Node id = createIdNode(value);
				int id_base = id.index;
				f.addChild(createTerminal(Integer.toString(id_base)));
				f.addChild(createSemiColonNode());
				/* get array element index */
				int arrayIndex = vHMap.getNumVariables(currFunction);
				vHMap.incrementNumVariables(currFunction, 1);
				f.addChild(createTerminal("local[" + arrayIndex + "] ="));
				f.addChild(createTerminal("local[" + exp.index + "] +"));
				f.addChild(createTerminal("local[" + idBaseIndex + "]"));
				f.addChild(createSemiColonNode());
				if (inputTokens.firstElement().getKey() == TokenNames.right_bracket) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					/* get element*/
					int elementIndex = vHMap.getNumVariables(currFunction);
					vHMap.incrementNumVariables(currFunction, 1);
					f.addChild(createTerminal("local[" + elementIndex + "] ="));
					if(id.local)
					f.addChild(createTerminal("local[local[" + arrayIndex
							+ "]]"));
					else f.addChild(createTerminal("global[local[" + arrayIndex
							+ "]]"));
					f.addChild(createSemiColonNode());
					f.index = elementIndex;
					return f;
				}
			}
			return null;
		}
		// left_parenthesis <expr list> right_parenthesis
		if (inputTokens.firstElement().getKey() == TokenNames.left_parenthesis) {
			currentPair = inputTokens.remove(0);
			currentToken = currentPair.getKey();
			// f.addChild(createTerminal(currentPair.getValue()));
			Node exprNode = expr_list();
			if (exprNode != null) {
				f.addChild(exprNode);
				if (inputTokens.firstElement().getKey() == TokenNames.right_parenthesis) {
					currentPair = inputTokens.remove(0);
					currentToken = currentPair.getKey();
					int index = vHMap.getNumVariables(currFunction);
					vHMap.incrementNumVariables(currFunction, 1);
					f.addChild(createTerminal("local[" + index + "] ="));
					f.addChild(createTerminal(value));
					f.addChild(createTerminal("("));
					//pass parameters to function */
					Vector<Integer> paramList = exprNode.getIndexList();
					for(int i=0; i<paramList.size() - 1 ; i++)
					{
						f.addChild(createTerminal("local["+paramList.elementAt(i)+"],"));
					}
					if(paramList.size() > 0)
					{
						f.addChild(createTerminal("local["+paramList.elementAt(paramList.size() - 1)+"]"));
					}
					f.addChild(createTerminal(")"));
					f.addChild(createSemiColonNode());
					f.index = index;
					return f;
				}
			}
			return null;
		}
		// empty
		int index = vHMap.getNumVariables(currFunction);
		vHMap.incrementNumVariables(currFunction, 1);
		f.addChild(createTerminal("local[" + index + "] ="));
		Node id = createIdNode(value);
		f.addChild(id);
		f.addChild(createSemiColonNode());
		f.index = index;
		return f;
	}

}
