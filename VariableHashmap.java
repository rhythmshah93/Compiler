import java.util.HashMap;
import java.util.Vector;

/**
 * 
 * @author Kannan
 *
 */
public class VariableHashmap {
	
	// Variable Hashmap
	// Hashmap mapping variable name
	// and function name to local
	// array(global if function
	// name=global) index
	public static HashMap<HKey, Integer> vHMap;
	
	// Function Hashmap
	// Hashmap mapping function name
	// to number of variables
	public static HashMap<String, Integer> fHMap;
	
	// Parameter hashmap
	// Hashmap mapping
	// function name to list
	// of parameter names
	public static HashMap<String, Vector<String>> pHMap;

	public VariableHashmap() {
		vHMap = new HashMap<HKey, Integer>();
		fHMap = new HashMap<String, Integer>(); 
		pHMap = new HashMap<String, Vector<String>>();
	}

	/**
	 * Adding a new function, id to variable hashmap
	 * 
	 * @param fName
	 *            = functionName
	 * @param iName
	 *            = idName
	 */
	public void put(String fName, String iName) {
		int index = 0;
		// get current total number of local array elements
		if (fHMap.containsKey(fName)) {
			index = fHMap.get(fName);
		}
		// add id to variable hashmap
		HKey k = new HKey(fName, iName);
		vHMap.put(k, index);
		// increment number of local array elements
		// add to function hashmap
		index++;
		fHMap.remove(fName);
		fHMap.put(fName, index);

	}

	/**
	 * Get the local array index for function,id
	 * @param fName = functionName
	 * @param iName = idName
	 * @return array index
	 */
	public int get(String fName, String iName) {
		HKey k = new HKey(fName, iName);
		return vHMap.get(k);
	}

	/**
	 * Checks if id has been hashed
	 * @param fName
	 * @param iName
	 * @return boolean
	 */
	public boolean containsKey(String fName, String iName) {
		HKey k = new HKey(fName, iName);
		return vHMap.containsKey(k);
	}

	/**
	 * Get variable count for a function
	 * @param fName = function name
	 * @return variable count
	 */
	public int getNumVariables(String fName) {
		if (fHMap.containsKey(fName))
			return fHMap.get(fName);
		else
			return 0;
	}

	/**
	 * Increment variable count
	 * @param fname
	 * @param num = value to be incremented by
	 */
	public void incrementNumVariables(String fname, int num) {
		int index = 0;
		if (fHMap.containsKey(fname)) {
			index = fHMap.get(fname);
		}
		fHMap.remove(fname);
		fHMap.put(fname, index + num);
	}

	/**
	 * 
	 * @param fName = function name
	 * @return Parameter list
	 */
	public Vector<String> getParameterList(String fName) {
		if (pHMap.containsKey(fName))
			return pHMap.get(fName);
		else
			return new Vector<String>();
	}

	/**
	 * Add parameter to list
	 * @param fName = function name
	 * @param iName = parameter name
	 */
	public void putParameter(String fName, String iName) {
		Vector<String> paramList;
		if (pHMap.containsKey(fName))
			paramList = pHMap.get(fName);
		else
			paramList = new Vector<String>();
		paramList.add(iName);
		pHMap.remove(fName);
		pHMap.put(fName, paramList);
		put(fName, iName);
	}

}

/**
 * Key for variable Hashmap
 * @author Kannan
 *
 */
class HKey {
	String fName, iName;

	public HKey(String fName, String iName) {
		this.fName = fName;
		this.iName = iName;
	}

	@Override
	public boolean equals(Object o) {
		HKey n = (HKey) o;
		if (n.fName.equals(this.fName) && n.iName.equals(this.iName))
			return true;
		return false;
	}

	@Override
	public int hashCode() {
		return fName.hashCode() + iName.hashCode();
	}
}
