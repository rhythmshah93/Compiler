# Compiler
I have used the code generator given by the TA. The current code stores the various functions and the statements in each function in a map. It also stores each function call as we have to resolve each function call. The current code tracks the times it needs to resolve the call. As the code for each function is stored, we just need to iterate through this map and resolve the function calls in it as well.



-----------------------------------

Unzip the tar file to your preferred location. 
You will find a list of .java files containing the source code in the src directory for the CodeGenerator application.

Compiling the parser 
---------------------
1) Open the command prompt and navigate to the directory with the .java files 
2) Now run the following command 
javac *.java
3) The .class files will be generated in this directory
If you want to generate the .class files in another location,
you can use javac -d <directory_name> *.java 
Please ensure that the directory_name exists

Running the parser
-------------------
1) Open the command prompt and navigate to the directory with compiled .class files
2) Now run the following command
java -cp . CodeGenerator <input_file_path>
The input_file_path is the passed as an argument
3) The ouput can be seen in out.c

Intra-Function Code Generator Functionality
---------------------
The code generator expects a file as an input. 
It scans through this file using a scanner to generate tokens.
These tokens are then used to evaluate the input based on the parser grammar that was provided.
As the tokens are parsed, for every rule a tree is created with the left side of the rule as root and right side as its children.
So, using depth first search on the tree, the entire code can be written.

In order to only have global or local int array declaration, the data_decl and dat_decl_z nodes were made silent and 
all the IDs found on parsing data_decl and data_decl_z were added to the a global hashmap with function name and id name as the key.
We automatically allot an array index to IDs hashed in the order they were seen.
The IDs found later in the other rules are then replaced by local[array index]

After this, i made modifications for if statement, while statement, continue, break nodes to satisfy the requirement.

<if statement> has been modified to
<if statement> --> if left_parenthesis <condition expression> right_expression
	  goto iflabel semicolon
	  goto restlabel2 semicolon
	  iflabel colon semicolon
	  <block statements>
	  restlabel colon semicolon

<while statement> has been modified to
<while statement> --> whileIfLabel colon semicolon
 						<if statement> left_parenthesis <condition expression> right_parenthesis
	  						goto whileBlockLabel semicolon
	  						goto whileRestLabel semicolon
	  						whileBlockLabel colon semicolon
	  						<block statements>
	  						goto whileIfLabel semicolon
	  						whileRestLabel colon semicolon
							
<break statement> has been modified to
<break statement> ---> goto whileRestLabel semicolon

<continue statement> has been modified to
<continue statement> ---> goto whileIfLabel semicolon

All nodes like <expression> which need to be evaluated return the index of the local array where the evaluated value is stored.
The parent nodes first add the evaluated node and then use the index to use the expression in the rule
For eg.
in
<return statement Z> --> <expression> semicolon
The return_statement_z first adds expression as a child and stores the index. 
Then the nodes with value "return", "local[index]" and semicolon are added as a children.

For nodes like <expression> with rules of type 
<expression> --> <term> <expression prime>
<expression prime> --> <addop> <term> <expression prime> | empty 
The term node is sent as prev node to expression_prime.
It adds the prev and term node as its children and uses the array index with value of prev and term 
to creates a new node with "local[index] =", "local[prev.index]", addop, "local[term.index};",  as children. 
This newly created node is then passed as parameter to the next expression_prime function.
The node returned from expression_prime is also added as a child and the parent nodes index is set to index.

For nodes like <expr list>,
the same process is followed but here an indexList is kept per node which will then be iterated to get all 
the values of parameters passed to the function.