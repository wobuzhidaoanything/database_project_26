package simpledb.storage;

import simpledb.common.Type;

import java.io.Serializable;
import java.util.*;

/**
 * TupleDesc describes the schema of a tuple.
 */
public class TupleDesc implements Serializable {

    /**
     * A help class to facilitate organizing the information of each field
     * */
    public static class TDItem implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The type of the field
         * */
        public final Type fieldType;
        
        /**
         * The name of the field
         * */
        public final String fieldName;

        public TDItem(Type t, String n) {
            this.fieldName = n;
            this.fieldType = t;
        }

        public String toString() {
            return fieldName + "(" + fieldType + ")";
        }
    }
    //Added 1: field to store the list of TDItems
    //this is the actual list coloumns living inside the object
    private final List<TDItem> items;

    /**
     * @return
     *        An iterator which iterates over all the field TDItems
     *        that are included in this TupleDesc
     * */
    public Iterator<TDItem> iterator() {
        // Added 9: return an iterator over the tdItems list
        return items.iterator();
    }

    private static final long serialVersionUID = 1L;

    /**
     * Create a new TupleDesc with typeAr.length fields with fields of the
     * specified types, with associated named fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     * @param fieldAr
     *            array specifying the names of the fields. Note that names may
     *            be null.
     */
    public TupleDesc(Type[] typeAr, String[] fieldAr) {
        // some code goes here
        //Added 2 - build TDitems from typeAR and fieldAR in parallel
        //
        items = new ArrayList<>();
        for (int i = 0; i < typeAr.length; i++) {
            items.add(new TDItem(typeAr[i], fieldAr[i]));
        }
    }


    /**
     * Constructor. Create a new tuple desc with typeAr.length fields with
     * fields of the specified types, with anonymous (unnamed) fields.
     * 
     * @param typeAr
     *            array specifying the number of and types of fields in this
     *            TupleDesc. It must contain at least one entry.
     */
    public TupleDesc(Type[] typeAr) {
        // some code goes here
        //Added 2 - build TDitems from typeAR with unnamed fields
        items = new ArrayList<>();
        for (Type type : typeAr) {
            items.add(new TDItem(type, null));
        }
    }

    /**
     * @return the number of fields in this TupleDesc
     */
    public int numFields() {
        // some code goes here
        //Added 3: return the size of the items list
        return items.size();
    }

    /**
     * Gets the (possibly null) field name of the ith field of this TupleDesc.
     * 
     * @param i
     *            index of the field name to return. It must be a valid index.
     * @return the name of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public String getFieldName(int i) throws NoSuchElementException {
        // some code goes here
        //Added 3: return the field name of the ith TDItem
        if (i < 0 || i >= items.size()) {
            throw new NoSuchElementException("Invalid field index" + i);
        }
        return items.get(i).fieldName;
    }

    /**
     * Gets the type of the ith field of this TupleDesc.
     * 
     * @param i
     *            The index of the field to get the type of. It must be a valid
     *            index.
     * @return the type of the ith field
     * @throws NoSuchElementException
     *             if i is not a valid field reference.
     */
    public Type getFieldType(int i) throws NoSuchElementException {
        // some code goes here
        //Added 3: bound check and return the field type of the ith TDItem
        if (i < 0 || i >= items.size()) {
            throw new NoSuchElementException("Invalid field index" + i);
        }
        return items.get(i).fieldType;
    }

    /**
     * Find the index of the field with a given name.
     * 
     * @param name
     *            name of the field.
     * @return the index of the field that is first to have the given name.
     * @throws NoSuchElementException
     *             if no field with a matching name is found.
     */
    public int fieldNameToIndex(String name) throws NoSuchElementException {
        // some code goes here
        //Added 4: linear scan for first matching field name
        if (name != null) {
            for (int i = 0; i < items.size(); i++) {
                if (name.equals(items.get(i).fieldName)) {
                    return i;
                }
            }
        }
        throw new NoSuchElementException("No field with name: " + name);
    }

    /**
     * @return The size (in bytes) of tuples corresponding to this TupleDesc.
     *         Note that tuples from a given TupleDesc are of a fixed size.
     */
    public int getSize() {
        // some code goes here
        //Added 5: sum up the size of all field types
        //answers how many bytes does one full row of this schema take up on a disk
        int size = 0;
        for (TDItem item : items) {
            size += item.fieldType.getLen();
        }
        return size;
    }

    /**
     * Merge two TupleDescs into one, with td1.numFields + td2.numFields fields,
     * with the first td1.numFields coming from td1 and the remaining from td2.
     * 
     * @param td1
     *            The TupleDesc with the first fields of the new TupleDesc
     * @param td2
     *            The TupleDesc with the last fields of the TupleDesc
     * @return the new TupleDesc
     */
    public static TupleDesc merge(TupleDesc td1, TupleDesc td2) {
        // some code goes here
        //Added 6:: concatenate td1's fields followed by td2's fields
        //
        int n1 = td1.numFields();
        int n2 = td2.numFields();
        Type[] types = new Type[n1 + n2];
        String[] names = new String[n1 + n2];

        for (int i = 0; i < n1; i++) {
            types[i] = td1.getFieldType(i);
            names[i] = td1.getFieldName(i);
        }
        for (int i = 0; i < n2; i++) {
            types[n1 + i] = td2.getFieldType(i);
            names[n1 + i] = td2.getFieldName(i);
        }
        return new TupleDesc(types, names);
    }

    

    /**
     * Compares the specified object with this TupleDesc for equality. Two
     * TupleDescs are considered equal if they have the same number of items
     * and if the i-th type in this TupleDesc is equal to the i-th type in o
     * for every i.
     * 
     * @param o
     *            the Object to be compared for equality with this TupleDesc.
     * @return true if the object is equal to this TupleDesc.
     */

    public boolean equals(Object o) {
        // some code goes here
        //Added 7: equal if same number of fields and matching field types

        if (!(o instanceof TupleDesc)) {
            return false;
        }
        TupleDesc other = (TupleDesc) o;
        if (this.numFields() != other.numFields()) {
            return false;
        }
        for (int i = 0; i < this.numFields(); i++) {
            if (!this.getFieldType(i).equals(other.getFieldType(i))) {
                return false;
            }
        }
        return true;
    }
    
    public int hashCode() {
        // If you want to use TupleDesc as keys for HashMap, implement this so
        // that equal objects have equals hashCode() results
        //Added 7: hash code based on field types
        int hash = 1;
        for (TDItem item : items) {
            hash = 31 * hash + item.fieldType.hashCode();
        }
        return hash;
    }

    /**
     * Returns a String describing this descriptor. It should be of the form
     * "fieldType[0](fieldName[0]), ..., fieldType[M](fieldName[M])", although
     * the exact format does not matter.
     * 
     * @return String describing this descriptor.
     */
    public String toString() {
        // some code goes here
        // Added 8: join each TDitem's toString with "," seperator
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i).toString());
            if (i < items.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
