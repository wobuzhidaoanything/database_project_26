package simpledb.storage;

import java.io.*;
import java.util.*;
import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see HeapPage#HeapPage
 * @author Sam Madden
 */

public class HeapFile implements DbFile {

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */

    // initialise attributes here
    private final File f;
    private final TupleDesc td;
    private final int tableId;

    public HeapFile(File f, TupleDesc td) {
        // some code goes here
        this.f = f;
        this.td = td;

        // its more efficient to just assign tableId in the constructor, and access it later
        // getAbsolutePath gives the absolute path to the heap file, hashcode gives the hashcode of that path
        this.tableId = f.getAbsoluteFile().hashCode();
    }

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.f;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
        return this.tableId;
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
        return this.td;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // some code goes here
        int pageNo = pid.getPageNumber();
        int pageSize = BufferPool.getPageSize();

        // check for illegal inputs
        if (pid.getTableId() != this.getId()) {
            throw new IllegalArgumentException("Illegal table");
        }
        if (pageNo < 0 || pageNo >= numPages()) {
            throw new IllegalArgumentException("Illegal page");
        }

        // fixed size of byte array assigned to data (local variable)
        byte[] data = new byte[pageSize];
        long offset = (long) pageNo * pageSize;

        // access by non sequential (we just count the offset of the page and jump to 
        // the specific page, instead of reading pages one by one)
        try {
            RandomAccessFile raf = new RandomAccessFile(f, "r");
            raf.seek(offset);
            raf.readFully(data);
            raf.close();
            return new HeapPage((HeapPageId) pid, data);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read page", e);
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        // typecast cause f.length() is a long value
        return (int) (f.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // Added: return an iterator that reads this heap file page by page
        return new AbstractDbFileIterator() {
            // pageNo tracks which page we should load next
            private int pageNo;

            // iterator for the tuples inside the current page
            private Iterator<Tuple> it;

            // open tracks whether the iterator is currently open or closed
            private boolean open;

            public void open() {
                // start from the first page and wait to load it until readNext()
                pageNo = 0;
                it = null;
                open = true;
            }

            protected Tuple readNext() throws DbException, TransactionAbortedException {
                if (!open) {
                    return null;
                }

                // keep going while the iterator is open
                while (true) {
                    // if the current page still has tuples, return the next one
                    if (it != null && it.hasNext()) {
                        return it.next();
                    }

                    // if all pages have been checked, there are no more tuples
                    if (pageNo >= numPages()) {
                        return null;
                    }

                    // load the next page through BufferPool, then use the page's own tuple iterator
                    HeapPageId pid = new HeapPageId(getId(), pageNo);
                    HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_ONLY);
                    it = page.iterator();
                    pageNo++;
                }
            }

            public void rewind() throws DbException, TransactionAbortedException {
                // reset back to the beginning
                close();
                open();
            }

            public void close() {
                super.close();
                // mark as closed and forget the current page iterator
                it = null;
                open = false;
            }
        };
    }

}
