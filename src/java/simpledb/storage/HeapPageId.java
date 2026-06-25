package simpledb.storage;

/** Unique identifier for HeapPage objects. */
public class HeapPageId implements PageId {

    /**
     * Constructor. Create a page id structure for a specific page of a
     * specific table.
     *
     * @param tableId The table that is being referenced
     * @param pgNo The page number in that table.
     */

    // set some private final fields to store the tableId and page number first
    private final int tableId;
    private final int pgNo; 

    public HeapPageId(int tableId, int pgNo) {
        this.tableId = tableId;
        this.pgNo = pgNo;
    }

    /** @return the table associated with this PageId */
    public int getTableId() {
        return this.tableId;
    }

    /**
     * @return the page number in the table getTableId() associated with
     *   this PageId
     */
    public int getPageNumber() {
        return this.pgNo;
    }

    /**
     * @return a hash code for this page, represented by a combination of
     *   the table number and the page number (needed if a PageId is used as a
     *   key in a hash table in the BufferPool, for example.)
     * @see BufferPool
     */
    public int hashCode() {
        return Integer.hashCode(tableId) * 31 + Integer.hashCode(pgNo);
    }

    /**
     * Compares one PageId to another.
     *
     * @param o The object to compare against (must be a PageId)
     * @return true if the objects are equal (e.g., page numbers and table
     *   ids are the same)
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        // we can't call equals here cause it'll be an infinite loop, so we use instanceof and cast to check if o is a HeapPageId and then compare the fields
        if (!(o instanceof HeapPageId)) {
            return false;
        }
        HeapPageId other = (HeapPageId) o;
        return this.tableId == other.tableId && this.pgNo == other.pgNo;
    }

    /**
     *  Return a representation of this object as an array of
     *  integers, for writing to disk.  Size of returned array must contain
     *  number of integers that corresponds to number of args to one of the
     *  constructors.
     */
    public int[] serialize() {
        int[] data = new int[2]; 
        // fix the array to 2 items. according to PageId interface, 
        // we need to return an array of integers that contains the tableId and the page number

        data[0] = getTableId();
        data[1] = getPageNumber();

        return data;
    }

}
