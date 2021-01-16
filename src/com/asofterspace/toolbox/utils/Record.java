/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

	// the parent information only gets initialized when .linkDoubly() is called on the root element explicitly
	protected Record parent;
	protected String parentPathComponent;


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
	 * Create a record from basically anything,
	 * but NOT making a deep copy (so in case you
	 * hand this method a Record, it will simply
	 * be returned)
	 */
	@SuppressWarnings("rawtypes")
	public static Record fromAnything(Object recordOrWhatever) {

		if (recordOrWhatever == null) {
			return Record.nullRecord();
		}

		if (recordOrWhatever instanceof Record) {
			return (Record) recordOrWhatever;
		}

		if (recordOrWhatever instanceof Boolean) {
			return new Record((Boolean) recordOrWhatever);
		}

		if (recordOrWhatever instanceof String) {
			return new Record((String) recordOrWhatever);
		}

		if (recordOrWhatever instanceof Integer) {
			return new Record((Integer) recordOrWhatever);
		}

		if (recordOrWhatever instanceof Long) {
			return new Record((Long) recordOrWhatever);
		}

		if (recordOrWhatever instanceof Double) {
			return new Record((Double) recordOrWhatever);
		}

		if (recordOrWhatever instanceof Float) {
			return new Record((Float) recordOrWhatever);
		}

		if (recordOrWhatever instanceof List) {
			Record arrRecord = Record.emptyArray();
			List<Record> valList = new ArrayList<>();
			for (Object obj : (List) recordOrWhatever) {
				valList.add(fromAnything(obj));
			}
			arrRecord.arrContents = valList;
			return arrRecord;
		}

		if (recordOrWhatever instanceof Object[]) {
			Record arrRecord = Record.emptyArray();
			List<Record> valList = new ArrayList<>();
			for (Object obj : (Object[]) recordOrWhatever) {
				valList.add(fromAnything(obj));
			}
			arrRecord.arrContents = valList;
			return arrRecord;
		}

		if (recordOrWhatever instanceof Map) {
			Record mapRecord = Record.emptyObject();
			Map<String, Record> valMap = new HashMap<>();
			for (Object entryObj : ((Map) recordOrWhatever).entrySet()) {
				Map.Entry entry = (Map.Entry) entryObj;
				Object key = entry.getKey();
				Object value = entry.getValue();
				valMap.put(""+key, fromAnything(value));
			}
			mapRecord.objContents = valMap;
			return mapRecord;
		}

		if (recordOrWhatever instanceof Enum<?>) {
			return new Record(recordOrWhatever.toString());
		}

		if (recordOrWhatever instanceof Date) {
			return new Record(DateUtils.serializeDateTime((Date) recordOrWhatever));
		}

		return Record.nullRecord();
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
	* Create a Record object based on a float value
	*/
	public Record(Float floatValue) {

		kind = RecordKind.NUMBER;

		simpleContents = floatValue;
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

	/**
	 * Return all the children (including recursive ones) of this Record which represent such
	 * a key-value pair
	 */
	public List<Record> searchForKeyValue(String key, String value) {
		List<Record> results = new ArrayList<>();
		searchForKeyValueInternal(key, value, results);
		return results;
	}

	private void searchForKeyValueInternal(String key, String value, List<Record> results) {

		if (kind == RecordKind.ARRAY) {
			if (arrContents != null) {
				for (Record recChild : arrContents) {
					if (recChild != null) {
						recChild.searchForKeyValueInternal(key, value, results);
					}
				}
			}
		}

		if (kind == RecordKind.OBJECT) {
			if (objContents != null) {
				for (Map.Entry<String, Record> entry : objContents.entrySet()) {
					Record recChild = entry.getValue();
					if (recChild != null) {
						if (key.equals(entry.getKey())) {
							if (recChild.kind == RecordKind.STRING) {
								if (value.equals(recChild.simpleContents)) {
									results.add(recChild);
								}
							}
						}

						recChild.searchForKeyValueInternal(key, value, results);
					}
				}
			}
		}
	}

	/**
	 * Call this on the root of a Record structure to back-link all children (and
	 * recursive children, and so on) with links to their respective parents
	 * Later updates to the Record structure will not be reflected in the back
	 * links, so if you make changes, you have to call linkDoubly() again before
	 * calling methods that require linkDoubly() to have been called!
	 */
	public void linkDoubly() {

		// make this the root
		parent = null;
		parentPathComponent = "root";

		// then make us the root of all our children
		if (kind == RecordKind.ARRAY) {
			if (arrContents != null) {
				for (int i = 0; i < arrContents.size(); i++) {
					Record recChild = arrContents.get(i);
					if (recChild != null) {
						recChild.linkDoubly();
						recChild.parent = this;
						recChild.parentPathComponent = "[" + i + "]";
					}
				}
			}
		}
		if (kind == RecordKind.OBJECT) {
			if (objContents != null) {
				for (Map.Entry<String, Record> entry : objContents.entrySet()) {
					Record recChild = entry.getValue();
					if (recChild != null) {
						recChild.linkDoubly();
						recChild.parent = this;
						recChild.parentPathComponent = "." + entry.getKey();
					}
				}
			}
		}
	}

	/**
	 * Before calling getParent(), ensure that the Record structure you are looking at
	 * is doubly linked - otherwise, no parent information will be available!
	 * To ensure this, call .linkDoubly() on the root element of the structure you
	 * want to work with.
	 */
	public Record getParent() {
		return parent;
	}

	/**
	 * Before calling getPath(), ensure that the Record structure you are looking at
	 * is doubly linked - otherwise, no parent information will be available!
	 * To ensure this, call .linkDoubly() on the root element of the structure you
	 * want to work with.
	 */
	public String getPath() {
		StringBuilder result = new StringBuilder();
		Record curRec = this;
		while (curRec != null) {
			result.insert(0, curRec.parentPathComponent);
			curRec = curRec.getParent();
		}
		return result.toString();
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
	 * Returns the value of this Record object as Object - which could be a String, Integer, etc.
	 */
	public Object asObject() {

		return simpleContents;
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

	public List<Record> getValues(String key) {
		Record rec = get(key);
		if (rec == null) {
			return new ArrayList<>();
		}
		return rec.getValues();
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

		if (kind == RecordKind.STRING) {
			result.add(this);
		}

		return result;
	}

	public Map<String, Record> getValueMap(String key) {
		Record rec = get(key);
		if (rec == null) {
			return new TreeMap<String, Record>();
		}
		return rec.getValueMap();
	}

	public Map<String, Record> getValueMap() {

		if (objContents == null) {
			return new TreeMap<String, Record>();
		}

		return objContents;
	}

	public Map<String, Object> getObjectMap(String key) {
		Record rec = get(key);
		if (rec == null) {
			return new TreeMap<String, Object>();
		}
		return rec.getObjectMap();
	}

	public Map<String, Object> getObjectMap() {

		if (objContents == null) {
			return new TreeMap<String, Object>();
		}

		Map<String, Object> result = new HashMap<>();

		for (Map.Entry<String, Record> entry : objContents.entrySet()) {
			String key = entry.getKey();
			Record recValue = entry.getValue();
			if (recValue == null) {
				result.put(key, null);
			} else {
				result.put(key, recValue.asObject());
			}
		}

		return result;
	}

	/**
	 * Get the Record-value corresponding to a specific key
	 * in a Record object or to a specific index in a Record
	 * array
	 * @param key the key to search for
	 * @return the Record value
	 */
	public Record get(Object key) {

		// garbage in, garbage out
		if (key == null) {
			return null;
		}

		// array-access
		if (key instanceof Integer) {
			return get((int) key);
		}

		// are we an object?
		if (objContents == null) {
			return null;
		}

		// oh we are an object!
		return objContents.get(key.toString());
	}

	/**
	 * Get the Record-value corresponding to a specific index
	 * in a Record array
	 * @param key the key to search for
	 * @return the Record value
	 */
	public Record get(int key) {

		if (arrContents != null) {

			// regular array
			if (arrContents.size() > key) {
				return arrContents.get(key);
			}

		} else {

			// array-access for a single string, which we interpret as array with one entry
			if (kind == RecordKind.STRING) {
				if (key == 0) {
					return this;
				}
			}
		}

		return null;
	}

	/**
	 * Get the length of the array represented by this Record
	 * array
	 * @return the length of the Record array
	 */
	public int getLength() {
		if (arrContents == null) {
			return 0;
		}
		return arrContents.size();
	}

	/**
	 * Get the length of the array represented by this Record
	 * array
	 * @return the length of the Record array
	 */
	public int length() {
		return getLength();
	}

	/**
	 * Get the length of the array represented by this Record
	 * array
	 * @return the length of the Record array
	 */
	public int size() {
		return getLength();
	}

	/**
	 * Get a list of Record values corresponding to a Record array
	 * stored in a particular key of a Record object
	 * @param key the key to search for
	 * @return the Record array stored in the key
	 */
	public List<Record> getArray(Object key) {

		Record result = get(key);

		if (result == null) {
			return new ArrayList<>();
		}

		if (result.arrContents == null) {
			List<Record> resultList = new ArrayList<>();
			if (result.kind == RecordKind.STRING) {
				resultList.add(result);
			}
			return resultList;
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

		if (result == null) {
			return new ArrayList<>();
		}

		return result.getStringValues();
	}

	/**
	 * Get a list of strings corresponding to the contents of a Record array
	 * filled with strings
	 * @return the list of strings stored in the array
	 */
	public List<String> getStringValues() {

		List<String> resultList = new ArrayList<>();

		if (arrContents == null) {
			if (kind == RecordKind.STRING) {
				resultList.add(simpleContents.toString());
			}
			return resultList;
		}

		for (Record entry : arrContents) {
			if (entry != null) {
				if (entry.kind == RecordKind.STRING) {
					resultList.add(entry.simpleContents.toString());
				}
			}
		}

		return resultList;
	}

	/**
	 * Get a list of integers corresponding to a Record array
	 * filled with integers stored in a particular key of a
	 * Record object (entries that are not numbers will be ignored!)
	 * @param key the key to search for
	 * @return the list of integers stored in the array at the key
	 */
	public List<Integer> getArrayAsIntegerList(Object key) {

		Record result = get(key);

		List<Integer> resultList = new ArrayList<>();

		if (result == null) {
			return resultList;
		}

		if (result.arrContents == null) {
			Integer val = getIntegerFromRecord(result);
			if (val != null) {
				resultList.add(val);
			}
			return resultList;
		}

		for (Record entry : result.arrContents) {
			if (entry != null) {
				Integer val = getIntegerFromRecord(entry);
				if (val != null) {
					resultList.add(val);
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

	public String getString(Object key, String defaultValue) {

		String result = getString(key);

		if (result == null) {
			return defaultValue;
		}

		return result;
	}

	/**
	 * Gets a date value stored in a key
	 */
	public Date getDate(Object key) {

		String str = getString(key);

		if (str == null) {
			return null;
		}

		return DateUtils.parseDate(str);
	}

	/**
	 * Gets a date-time value stored in a key
	 */
	public Date getDateTime(Object key) {

		String str = getString(key);

		if (str == null) {
			return null;
		}

		return DateUtils.parseDateTime(str);
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

	public Integer getInteger(Object key, Integer defaultValue) {
		Integer result = getInteger(key);
		if (result == null) {
			return defaultValue;
		}
		return result;
	}

	/**
	 * Gets an int value stored in a key of a Record object
	 * @param key  the key to be searched for
	 * @return the integer value stored in the key
	 */
	public Integer getInteger(Object key) {

		Record result = get(key);

		return getIntegerFromRecord(result);
	}

	private Integer getIntegerFromRecord(Record result) {

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
			if (result.simpleContents instanceof Float) {
				return (Integer) Math.round((Float) result.simpleContents);
			}
		}

		if (result.kind == RecordKind.BOOLEAN) {
			if ((Boolean) result.simpleContents) {
				return 1;
			} else {
				return 0;
			}
		}

		if (result.kind == RecordKind.STRING) {
			return StrUtils.strToInt((String) result.simpleContents);
		}

		if (result.kind == RecordKind.NULL) {
			return null;
		}

		return null;
	}

	/**
	 * Retrieves the value that the key currently holds, increases it in the Record
	 * and returns the increased value (or null if there was none / it was not a number)
	 */
	public Integer inc(Object key) {

		Integer curVal = getInteger(key);

		if (curVal == null) {
			return null;
		}

		curVal += 1;

		set(key, curVal);

		return curVal;
	}

	/**
	 * Retrieves the value that the key currently holds, decreases it in the Record
	 * and returns the decreased value (or null if there was none / it was not a number)
	 */
	public Integer dec(Object key) {

		Integer curVal = getInteger(key);

		if (curVal == null) {
			return null;
		}

		curVal -= 1;

		set(key, curVal);

		return curVal;
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
			if (result.simpleContents instanceof Float) {
				return (Long) (long) Math.round((Float) result.simpleContents);
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
			if (result.simpleContents instanceof Float) {
				return (Double) (double) (float) (Float) result.simpleContents;
			}
		}

		if (result.kind == RecordKind.STRING) {
			if ("".equals((String) result.simpleContents)) {
				return null;
			}
			try {
				return Double.valueOf((String) result.simpleContents);
			} catch (NumberFormatException e2) {
				try {
					return Double.valueOf(((String) result.simpleContents).replaceAll(",", "."));
				} catch (NumberFormatException e3) {
					System.err.println("Cannot convert " + result.simpleContents + " to double...");
					return null;
				}
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
	public void remove(Object key) {

		if (key == null) {
			return;
		}

		if (key instanceof Integer) {
			remove((int) key);
			return;
		}

		makeObject();

		objContents.remove(key.toString());
	}

	/**
	 * Removes the index-th entry from this array
	 * @param index .. the index of the entry which should be removed
	 */
	public void remove(int index) {

		makeArray();

		arrContents.remove(index);
	}

	/**
	 * Removes all keys from this object except the ones given as arguments
	 * @param keys .. the keys to keep
	 */
	public void removeAllExcept(Object... keys) {

		makeObject();

		TreeMap<String, Record> newObjContents = new TreeMap<String, Record>();

		for (Object key : keys) {
			if (key == null) {
				continue;
			}
			String keyStr = key.toString();
			if (objContents.containsKey(keyStr)) {
				newObjContents.put(keyStr, objContents.get(keyStr));
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

		if (key instanceof Integer) {
			set((int) key, value);
			return;
		}

		makeObject();

		objContents.put(key.toString(), fromAnything(value));
	}

	/**
	 * Sets a key of the Record object to the Record value, or removes the key is null is passed in as value
	 * @param key
	 * @param value
	 */
	public void setOrRemove(Object key, Object value) {
		if (value == null) {
			remove(key);
		} else {
			set(key, value);
		}
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

		// actually, our set function has become so powerful it can just do this :)
		// set(key, values);

		// ... buuut for performance reasons we do not rely on it anyway ^^
		Record arrRecord = Record.emptyArray();

		arrRecord.arrContents = values;

		set(key, arrRecord);
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

		if (arrContents == null) {
			arrContents = new ArrayList<Record>();
			if (kind == RecordKind.STRING) {
				arrContents.add(new Record(this));
			}
		}

		kind = RecordKind.ARRAY;
	}

	public void reverse() {
		if (arrContents != null) {
			Collections.reverse(arrContents);
		}
	}

	/**
	 * Sets an index of the Record array to the value
	 * @param index
	 * @param value
	 */
	public void set(int index, Object value) {

		makeArray();

		// if our array only contains two entries, and now someone wants to set at index 5,
		// actually add the remaining ones as null in between!
		while (arrContents.size() <= index) {
			arrContents.add(null);
		}

		arrContents.set(index, fromAnything(value));
	}

	/**
	 * Appends any value to the Record array
	 * @param value
	 */
	public void append(Object value) {

		makeArray();

		arrContents.add(fromAnything(value));
	}

	/**
	 * Adds any value to the Record array
	 * @param value
	 */
	public void add(Object value) {
		append(value);
	}

	/**
	 * Pushes any value to the Record array
	 * @param value
	 */
	public void push(Object value) {
		append(value);
	}

}
