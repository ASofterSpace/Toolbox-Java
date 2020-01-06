/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.io.RecordKind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


/**
 * Generic record used to exchange data
 * Used as basis for Record, XML, INI file entries, etc...
 */
public class Record {

	protected RecordKind kind;

	protected Map<String, Record> objContents;
	protected List<Record> arrContents;
	protected Object simpleContents;


	/**
	 * Create an empty Record object
	 */
	public static Record emptyObject() {
		return new Record();
	}

	/**
	 * Create a null Record
	 */
	public static Record nullRecord() {
		Record result = new Record();
		result.kind = RecordKind.NULL;
		return result;
	}

	/**
	 * Create an empty Record array
	 */
	public static Record emptyArray() {
		Record result = new Record();
		result.makeArray();
		return result;
	}

	/**
	 * Create an empty Record object
	 */
	public Record() {

		kind = RecordKind.OBJECT;

		objContents = new TreeMap<String, Record>();
	}

	/**
	 * Create a record object based on another generic record quickly,
	 * by making a shallow copy, NOT a deep one!
	 * (To create a deep copy, use the createDeepCopy() method)
	 */
	public Record(Record other) {

		this.kind = other.kind;

		switch (this.kind) {

			case STRING:
			case BOOLEAN:
			case NUMBER:
				this.simpleContents = other.simpleContents;
				break;

			case ARRAY:
				this.arrContents = other.arrContents;
				break;

			case OBJECT:
				this.objContents = other.objContents;
				break;
		}
	}

	/**
	* Create a Record object based on a string value
	*/
	public Record(String strValue) {

		kind = RecordKind.STRING;

		simpleContents = strValue;
	}

	/**
	* Create a Record object based on an integer value
	*/
	public Record(Integer intValue) {

		kind = RecordKind.NUMBER;

		simpleContents = intValue;
	}

	/**
	* Create a Record object based on a long value
	*/
	public Record(Long longValue) {

		kind = RecordKind.NUMBER;

		simpleContents = longValue;
	}

	/**
	* Create a Record object based on a double value
	*/
	public Record(Double doubleValue) {

		kind = RecordKind.NUMBER;

		simpleContents = doubleValue;
	}

	/**
	* Create a Record object based on a boolean value
	*/
	public Record(Boolean boolValue) {

		kind = RecordKind.BOOLEAN;

		simpleContents = boolValue;
	}

	public void convertTo(RecordKind newKind) {

		// TODO :: add more cases
		switch (kind) {
			case STRING:
				switch (newKind) {
					case BOOLEAN:
						if ("true".equals(simpleContents)) {
							simpleContents = true;
						} else {
							simpleContents = false;
						}
				}
		}

		kind = newKind;
	}

	/**
	 * Tells us whether this Record object is empty or not
	 */
	public boolean isEmpty() {

		switch (kind) {

			case BOOLEAN:
			case NUMBER:
				return simpleContents == null;

			case STRING:
				if (simpleContents == null) {
					return true;
				}
				return "".equals(simpleContents.toString());

			case ARRAY:
				return arrContents.size() < 1;

			case OBJECT:
				return objContents.size() < 1;

			default:
				return true;
		}
	}

	/**
	 * Create a record object based on this one quickly, by making
	 * a shallow copy
	 * (To create a deep copy, use the createShallowCopy() method)
	 */
	public Record createShallowCopy() {
		return new Record(this);
	}

	/**
	 * Create a record object based on this one slowly, by making
	 * a deep copy that has all independent data
	 * (To create a quick shallow copy, use the createShallowCopy() method)
	 */
	public Record createDeepCopy() {

		// create shallow copy to start with...
		Record result = createShallowCopy();

		// ... then actually make it a deep array copy...
		if (this.kind == RecordKind.ARRAY) {
			result.arrContents = new ArrayList<Record>();
			for (Record entry : this.arrContents) {
				result.arrContents.add(entry.createDeepCopy());
			}
		}

		// ... and/or a deep object copy...
		if (this.kind == RecordKind.OBJECT) {
			result.objContents = new TreeMap<String, Record>();
			for (Map.Entry<String, Record> entry : this.objContents.entrySet()) {
				String key = entry.getKey();
				Record value = entry.getValue();
				result.objContents.put(key, value.createDeepCopy());
			}
		}

		// ... and finally return it
		return result;
	}

	@Override
	public String toString() {

		return toString(null);
	}

	/**
	 * Stores this Record object in a string
	 * @param compressed  whether to store this object compressed (true, default) or
	 *					uncompressed (false) - in which case it will be easier to
	 *					read by humans, but take up more space
	 */
	public String toString(Boolean compressed) {

		if (compressed == null) {
			compressed = true;
		}

		return toString(this, compressed, "");
	}

	/**
	 * In classes extending record, override this one to specify
	 * how the data should be converted to a textual representation
	 */
	protected String toString(Record item, boolean compressed, String linePrefix) {

		switch (item.kind) {

			case STRING:
				return "String Record";

			case BOOLEAN:
				return "Boolean Record";

			case NUMBER:
				return "Number Record";

			case ARRAY:
				return "Array Record";

			case OBJECT:
				return "Object Record";

			default:
				return "Null Record";
		}
	}

	/**
	 * Returns the value of this Record object as string, without any surrounding " or somesuch;
	 * meant to be used when you know that your Record object is of the simple type String, and
	 * you want to get that String
	 */
	public String asString() {

		if (simpleContents == null) {
			return null;
		}

		return simpleContents.toString();
	}

	/**
	 * Returns true if the given key exists in this record object,
	 * and false otherwise (as well as when this record is no object)
	 */
	public boolean contains(String key) {
		if (objContents == null) {
			return false;
		}
		return objContents.containsKey(key);
	}

	/**
	 * Get the kind of Record object this is
	 * @return the kind
	 */
	public RecordKind getKind() {
		return kind;
	}

	/**
	 * Get the set of all defined keys in this Record object
	 * @return the set of defined keys
	 */
	public Set<String> getKeys() {
		if (objContents == null) {
			return new HashSet<>();
		}
		return objContents.keySet();
	}

	/**
	 * Get a list of all values stored in this Record object
	 * (if it is an array, then getting all values in the array
	 * in order, and if it is an object, then getting all values
	 * belonging to keys of the object in some order)
	 * @return a list of all values
	 */
	public List<Record> getValues() {

		if (kind == RecordKind.ARRAY) {
			if (arrContents != null) {
				return arrContents;
			}
		}

		ArrayList<Record> result = new ArrayList<>();

		if (kind == RecordKind.OBJECT) {
			if (objContents != null) {
				for (Record val : objContents.values()) {
					result.add(val);
				}
			}
		}

		return result;
	}

	public Map<String, Record> getValueMap() {

		if (objContents == null) {
			return new TreeMap<String, Record>();
		}

		return objContents;
	}

	/**
	 * Get the Record-value corresponding to a specific key
	 * in a Record object or to a specific index in a Record
	 * array
	 * @param key the key to search for
	 * @return the Record object
	 */
	public Record get(Object key) {

		if (key == null) {
			return null;
		}

		if ((key instanceof Integer) && (arrContents != null)) {
			return arrContents.get((Integer) key);
		}

		if (objContents == null) {
			return null;
		}
		return objContents.get(key.toString());
	}

	/**
	 * Get the length of the array represented by this Record
	 * array
	 * @return the length of the Record array
	 */
	public int getLength() {
		return arrContents.size();
	}

	/**
	 * Get a list of Record values corresponding to a Record array
	 * stored in a particular key of a Record object
	 * @param key the key to search for
	 * @return the Record array stored in the key
	 */
	public List<Record> getArray(Object key) {

		Record result = get(key);

		if ((result == null) || (result.arrContents == null)) {
			return new ArrayList<>();
		}

		return result.arrContents;
	}

	/**
	 * Get a list of strings corresponding to a Record array
	 * filled with strings stored in a particular key of a
	 * Record object (entries that are not strings will be ignored!)
	 * @param key the key to search for
	 * @return the list of strings stored in the array at the key
	 */
	public List<String> getArrayAsStringList(Object key) {

		Record result = get(key);

		List<String> resultList = new ArrayList<>();

		if ((result == null) || (result.arrContents == null)) {
			return resultList;
		}

		for (Record entry : result.arrContents) {
			if (entry != null) {
				if (entry.kind == RecordKind.STRING) {
					resultList.add(entry.simpleContents.toString());
				}
			}
		}

		return resultList;
	}

	/**
	 * Gets a string value stored in a key (which will
	 * just return a string as string if the value is
	 * already a string, but if the value is a complex
	 * Record object, then it will be coaxed into a string,
	 * which does not necessarily mean that it is a string
	 * that makes much sense - beware!)
	 * @param key the key to search for
	 * @return the Record object - hopefully a plain string - as string
	 */
	public String getString(Object key) {
		Record result = get(key);

		if (result == null) {
			return null;
		}

		switch (result.kind) {

			case NULL:
				return null;

			// in case of a string, return the contained string WITHOUT
			// enclosing ""-signs
			case STRING:
			case BOOLEAN:
			case NUMBER:
				return result.simpleContents.toString();

			// in case of an array or object or whatever, return the default serialization
			default:
				return result.toString();
		}
	}

	/**
	 * Gets a boolean value stored in a key of a Record object
	 * @param key  the key to be searched for
	 * @return the boolean value stored in the key
	 */
	public Boolean getBoolean(Object key) {

		return getBoolean(key, null);
	}

	/**
	 * Gets a boolean value stored in a key of a Record object
	 * @param key  the key to be searched for
	 * @param defaultValue  the default value to be returned if none is set
	 * @return the boolean value stored in the key
	 */
	public Boolean getBoolean(Object key, Boolean defaultValue) {

		Record result = get(key);

		if (result == null) {
			return defaultValue;
		}

		if (result.kind == RecordKind.BOOLEAN) {
			return (Boolean) result.simpleContents;
		}

		return defaultValue;
	}

	/**
	 * Gets an int value stored in a key of a Record object
	 * @param key  the key to be searched for
	 * @return the integer value stored in the key
	 */
	public Integer getInteger(Object key) {

		Record result = get(key);

		if (result == null) {
			return null;
		}

		if (result.kind == RecordKind.NUMBER) {
			if (result.simpleContents instanceof Long) {
				return (Integer) (int) (long) ((Long) result.simpleContents);
			}
			if (result.simpleContents instanceof Integer) {
				return (Integer) result.simpleContents;
			}
			if (result.simpleContents instanceof Double) {
				return (Integer) (int) Math.round((Double) result.simpleContents);
			}
		}

		if (result.kind == RecordKind.BOOLEAN) {
			if ((Boolean) result.simpleContents) {
				return 1;
			} else {
				return 0;
			}
		}

		if (result.kind == RecordKind.NULL) {
			return null;
		}

		return 0;
	}

	/**
	 * Gets an long value stored in a key of a Record object
	 * @param key  the key to be searched for
	 * @return the integer value stored in the key
	 */
	public Long getLong(Object key) {

		Record result = get(key);

		if (result == null) {
			return null;
		}

		if (result.kind == RecordKind.NUMBER) {
			if (result.simpleContents instanceof Long) {
				return (Long) result.simpleContents;
			}
			if (result.simpleContents instanceof Integer) {
				return (Long) (long) (int) (Integer) result.simpleContents;
			}
			if (result.simpleContents instanceof Double) {
				return (Long) Math.round((Double) result.simpleContents);
			}
		}

		if (result.kind == RecordKind.BOOLEAN) {
			if ((Boolean) result.simpleContents) {
				return 1L;
			} else {
				return 0L;
			}
		}

		if (result.kind == RecordKind.NULL) {
			return null;
		}

		return 0L;
	}

	/**
	 * Gets an double value stored in a key of a Record object
	 * @param key  the key to be searched for
	 * @return the integer value stored in the key
	 */
	public Double getDouble(Object key) {

		Record result = get(key);

		if (result == null) {
			return null;
		}

		if (result.kind == RecordKind.NUMBER) {
			if (result.simpleContents instanceof Long) {
				return (Double) (double) (long) (Long) result.simpleContents;
			}
			if (result.simpleContents instanceof Integer) {
				return (Double) (double) (int) (Integer) result.simpleContents;
			}
			if (result.simpleContents instanceof Double) {
				return (Double) result.simpleContents;
			}
		}

		if (result.kind == RecordKind.BOOLEAN) {
			if ((Boolean) result.simpleContents) {
				return 1.0;
			} else {
				return 0.0;
			}
		}

		if (result.kind == RecordKind.NULL) {
			return null;
		}

		return 0.0;
	}

	/**
	 * Removes a key (and its value, duh) from this object
	 * @param key .. the key to remove
	 */
	public void removeKey(Object key) {

		makeObject();

		if (key != null) {
			objContents.remove(key.toString());
		}
	}

	/**
	 * Removes the index-th entry from this array
	 * @param index .. the index of the entry which should be removed
	 */
	public void removeIndex(int index) {

		makeArray();

		arrContents.remove(index);
	}

	/**
	 * Removes all keys from this object except the ones given as arguments
	 * @param keys .. the keys to keep
	 */
	public void removeAllKeysExcept(String... keys) {

		makeObject();

		TreeMap<String, Record> newObjContents = new TreeMap<String, Record>();

		for (String key : keys) {
			if (objContents.containsKey(key)) {
				newObjContents.put(key, objContents.get(key));
			}
		}

		objContents = newObjContents;
	}

	/**
	 * Sets a key of the Record object to the Record value
	 * @param key
	 * @param value
	 */
	public void set(Object key, Object value) {

		if (key == null) {
			return;
		}

		makeObject();

		if (value == null) {
			objContents.put(key.toString(), Record.nullRecord());
		}

		if (value instanceof Record) {
			objContents.put(key.toString(), (Record) value);
			return;
		}

		if (value instanceof Boolean) {
			objContents.put(key.toString(), new Record((Boolean) value));
			return;
		}

		if (value instanceof String) {
			objContents.put(key.toString(), new Record((String) value));
			return;
		}

		if (value instanceof Integer) {
			objContents.put(key.toString(), new Record((Integer) value));
			return;
		}

		if (value instanceof Long) {
			objContents.put(key.toString(), new Record((Long) value));
			return;
		}

		if (value instanceof Double) {
			objContents.put(key.toString(), new Record((Double) value));
			return;
		}

		objContents.put(key.toString(), Record.nullRecord());
	}

	/**
	 * Sets a key to the string value
	 * @param key
	 * @param value
	 */
	public void setString(Object key, Object value) {

		if (value == null) {
			set(key, new Record(""));
		} else {
			set(key, new Record(value.toString()));
		}
	}

	/**
	 * Set a list of Record values corresponding to a Record array
	 * stored in a particular key of a Record object
	 * @param key the key to set
	 * @param values the Record array to be stored in the key
	 */
	public void setArray(Object key, List<Record> values) {

		Record valList = Record.emptyArray();

		valList.arrContents = values;

		set(key, valList);
	}

	/**
	 * We ensure that this is a valid object type
	 */
	public void makeObject() {

		kind = RecordKind.OBJECT;

		if (objContents == null) {
			objContents = new TreeMap<String, Record>();
		}
	}

	/**
	 * We ensure that this is a valid array type
	 */
	public void makeArray() {

		kind = RecordKind.ARRAY;

		if (arrContents == null) {
			arrContents = new ArrayList<Record>();
		}
	}

	public void reverse() {
		if (arrContents != null) {
			Collections.reverse(arrContents);
		}
	}

	/**
	 * Sets an index of the Record array to the Record value
	 * @param index
	 * @param value
	 */
	public void set(Integer index, Record value) {

		makeArray();

		arrContents.set(index, value);
	}

	/**
	 * Appends a Record value to the Record array
	 * @param value
	 */
	public void append(Record value) {

		makeArray();

		arrContents.add(value);
	}

}
