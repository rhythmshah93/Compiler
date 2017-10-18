import java.io.PrintWriter;
import java.util.Vector;
import java.util.HashMap;
import java.util.Objects;

/**
 * 
 * @author Kannan
 *
 */
public class Node {
	private boolean leaf;	//indicates whether the node is a leaf node or has children
	private String value;	//in case of a leaf node, contains value to be printed.
							//in case of datadecl node, contains the function name
	private Vector<Node> children; //list of child nodes
	private boolean datadecl;	// indicates whether this is a datadecl node
	int index;					//used only in case of expression nodes, contains the index of the local array containing the value
	boolean local;				//used only in case of id nodes, indicates whether id belongs to local or global array
	private Vector<Integer> indexList;	//used only in case of expr list, contains indexes of parameters
	private boolean funccall;
	private int func_id;
	private String function_name;
	private Vector<Integer> paramList = new Vector<Integer>();

	/**
	 * Constructor
	 */
	public Node()
	{
		leaf = true;
		value = "";
		children = new Vector<Node>();
		datadecl = false;
		index = -1;
		indexList = new Vector<Integer>();
		local = true;
	}
	
	/**
	 * Set node as a leaf node
	 * @param value
	 */
	public void setTerminalValue(String value)
	{
		leaf = true;
		this.value = value;
	}

	public void setFuncCall(String name, int id, Vector<Integer> list1)
	{
		funccall = true;
		function_name = name;
		func_id = id;
		paramList = list1;
	}
	public void setFunctionId(String value, int id)
	{
		leaf = true;
		// funccall = true;
		this.value = value;
		func_id = id;
	}
	
	/**
	 * Set node as a data declaration node
	 * @param value = function Name
	 */
	public void setDataDeclValue(String value)
	{
		leaf = false;
		datadecl = true;
		this.value = value;
	}
	
	/**
	 * Set as a non-leaf node
	 */
	public void setAsIntermediate()
	{
		leaf = false;
	}
	
	/**
	 * Add child to list
	 * @param n <-child
	 * @return
	 */
	public boolean addChild(Node n)
	{
		if(n == null)
			return false;
		setAsIntermediate();
		children.add(n);
		return true;
	}
	
	/**
	 * Add parameter index to list
	 * @param i <- index of local array containing parameter value
	 */
	public void addToIndexList(int i)
	{
		indexList.add(i);
	}
	
	/**
	 * @return List of local array indexes containing paramter values
	 */
	public Vector<Integer> getIndexList(){
		return indexList;
	}
	
	
	public void concatIndexList(Vector<Integer> vec){
		for(int i = 0; i< vec.size(); i++)
		{
			addToIndexList(vec.elementAt(i));
		}
	}
	
	/**
	 * Prints the node to file
	 * @param pw -> printWriter to write to file
	 * @param vHMap -> Hashmap containing ids
	 */
	public void print(PrintWriter pw, VariableHashmap vHMap)
	{
		//leaf node
		if(leaf == true)
		{
			//print value
			if(value.contains("\n"))
				pw.print(value);
			else if (!value.equals(""))
			pw.print(value + " ");
		}
		//data declaration node
		else if(datadecl == true)
		{
			//get total number of variables for function
			int num = vHMap.getNumVariables(value);
			if(num > 0){
				if(value.equals("global"))
				{
					//int global[num];
					pw.print("int global["+num+"];\n");
				}
				else
				{
					//int local[num];
					pw.print("int local["+num+"];\n");
					Vector<String> paramsList = vHMap.getParameterList(value);
					for(int i= 0; i<paramsList.size(); i++)
					{
						int index = vHMap.get(value, paramsList.elementAt(i));
						pw.print("local[" + index +"] = " + paramsList.elementAt(i)+";\n");
					}
				}
			}
		}
		else
		{
			//non-leaf node
			for(int i=0; i< children.size(); i++)
			{
				//print children
				children.elementAt(i).print(pw, vHMap);
			}
		}
	}

	/*public void print1(PrintWriter pw, VariableHashmap vHMap)
	{
		//leaf node
		if(funccall == true)
			pw.write("need to resolve function. Name is "+function_name+"\n");
		if(leaf == true)
		{
			// pw.print("start printing for a leaf node\n");
			//print value
			if(value.contains("local"))
			{
				String newstr = value.replaceAll("local","mem");
				if(newstr.contains("\n"))
					pw.print(newstr);
				else if (!newstr.equals(""))
					pw.print(newstr + " ");
			}
			else
				if(value.contains("\n"))
					pw.print(value);
				else if (!value.equals(""))
					pw.print(value + " ");
			// pw.print("end printing for a leaf node\n");
		}
		//data declaration node
		else if(datadecl == true)
		{
			//get total number of variables for function
			// pw.print("start printing for a datadecl node\n");
			int num = vHMap.getNumVariables(value);
			if(num > 0){
				if(value.equals("global"))
				{
					//int global[num];
					pw.print("int global["+num+"];\n");
				}
				else
				{
					//int local[num];
					pw.print("int local["+num+"];\n");
					Vector<String> paramsList = vHMap.getParameterList(value);
					for(int i= 0; i<paramsList.size(); i++)
					{
						int index = vHMap.get(value, paramsList.elementAt(i));
						pw.print("local[" + index +"] = " + paramsList.elementAt(i)+";\n");
					}
				}
			}
			// pw.print("end printing for a datadecl node\n");
		}
		else
		{
			// pw.print("start printing for a child node\n");
			//non-leaf node
			for(int i=0; i< children.size(); i++)
			{
				//print children
				children.elementAt(i).print1(pw, vHMap);
			}
			// pw.print("end printing for a child node\n");
		}
	}
*/
	public void print2(PrintWriter pw, VariableHashmap vHMap, HashMap<Integer,Vector<Integer>> func_call_map)
	{
		int label_count = 1;
		if(funccall == true)
		{
			resolve_func_call(pw,vHMap,function_name,label_count,func_id,func_call_map);
			pw.write("label_"+label_count+":;\n");
		}
		if(leaf == true)
		{
			// pw.print("start printing for a leaf node\n");
			//print value
			if(value.contains("local"))
			{
				String newstr = value.replaceAll("local","mem");
				if(newstr.contains("\n"))
					pw.print(newstr);
				else if (!newstr.equals(""))
					pw.print(newstr + " ");
			}
			else
				if(value.contains("\n"))
					pw.print(value);
				else if (!value.equals(""))
					pw.print(value + " ");
			// pw.print("end printing for a leaf node\n");
		}
		//data declaration node
		else if(datadecl == true)
		{
			//get total number of variables for function
			// pw.print("start printing for a datadecl node\n");
			int num = vHMap.getNumVariables(value);
			if(num > 0){
				if(value.equals("global"))
				{
					//int global[num];
					// pw.print("int global["+num+"];\n");
				}
				else
				{
					//int local[num];
					// pw.print("int local["+num+"];\n");
					Vector<String> paramsList = vHMap.getParameterList(value);
					// System.out.println("value is "+value);
					// if(!Objects.equals(value,"main"))
					for(int i= 0; i<paramsList.size(); i++)
					{
						// int index = vHMap.get(value, paramList.elementAt(i));
						pw.print("mem[base+" + i+"] = mem[base-4-" + (paramsList.size()+i)+"];\n");
					}
				}
			}
			// pw.print("end printing for a datadecl node\n");
		}
		else
		{
			// pw.print("start printing for a child node\n");
			//non-leaf node
			for(int i=0; i< children.size(); i++)
			{
				//print children
				children.elementAt(i).print2(pw, vHMap,func_call_map);
			}
			// pw.print("end printing for a child node\n");
		}
	}

	public void resolve_func_call(PrintWriter pw, VariableHashmap vHMap, String func_name, int label,int func_id, HashMap<Integer,Vector<Integer>> func_call_map)
	{
		Vector<String> parameter_list = vHMap.getParameterList(func_name);
		int nop = parameter_list.size();
		// System.out.println("NoP for "+func_name+" is "+ String.valueOf(nop));

		for(int x=0; x<nop;x++ )
		{
			pw.print("mem[top+"+x+"] = mem[base+"+paramList.elementAt(x)+"];\n");
		}
		pw.print("mem[top+"+nop+"]=base;\n");
		pw.print("mem[top+"+(nop+1)+"]=top;\n");
		// pw.print("mem[top+"+(nop+2)+"]=top;\n");
		pw.print("mem[top+"+(nop+3)+"]="+label+";\n");
		pw.print("base = top + "+(nop+4)+";\n");
		int locals = vHMap.getNumVariables(func_name);
		pw.print("top = base + "+locals+";\n");
		pw.print("goto "+func_name+"Func;\n");
	} 

	public void epilogue_for_func(PrintWriter pw,int returnvar_index)
	{
		pw.print("top = mem[base - 3];\n");
		pw.print("mem[base - 2] = mem[base+"+returnvar_index+"];\n");
		pw.print("jumpReg = mem[base-1];\n");
		pw.print("base = mem[base-4];\n");
		pw.print("goto jumptable;\n");
	}
}
