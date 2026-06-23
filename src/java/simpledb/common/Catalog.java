package simpledb.common;

import simpledb.common.Type;
import simpledb.storage.DbFile;
import simpledb.storage.HeapFile;
import simpledb.storage.TupleDesc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Catalog keeps track of all available tables in the database and their
 * associated schemas.
 * For now, this is a stub catalog that must be populated with tables by a
 * user program before it can be used -- eventually, this should be converted
 * to a catalog that reads a catalog table from disk.
 * 
 * @Threadsafe
 */
public class Catalog {

    // Added: Step 1 - small helper class bundling everything we need to know about one table
    private static class TableInfo {
        public final DbFile file;
        public final String name;
        public final String pkeyField;

        public TableInfo(DbFile file, String name, String pkeyField) {
            this.file = file;
            this.name = name;
            this.pkeyField = pkeyField;
        }
    }

    // Added: Step 1 - map from table id -> that table's info, keyed by DbFile.getId()
    private final Map<Integer, TableInfo> tables;

    /**
     * Constructor.
     * Creates a new, empty catalog.
     */
    public Catalog() {
        // Added: Step 2 - initialize the (empty) map of tables
        tables = new ConcurrentHashMap<>();
    }

    /**
     * Add a new table to the catalog.
     * This table's contents are stored in the specified DbFile.
     * If there exists a table with the same name or ID, replace that old table with this one. 
     * @param file the contents of the table to add;  file.getId() is the identfier of
     *    this file/tupledesc param for the calls getTupleDesc and getFile. 
     * @param name the name of the table -- may be an empty string.  May not be null.  
     * @param pkeyField the name of the primary key field
     */
    public void addTable(DbFile file, String name, String pkeyField) {
        // Added: Step 3 - remove any existing table with the same name (different id),
        // then store/overwrite the entry for this id
        Integer existingIdWithSameName = null;
        for (Map.Entry<Integer, TableInfo> entry : tables.entrySet()) {
            if (entry.getValue().name.equals(name)) {
                existingIdWithSameName = entry.getKey();
                break;
            }
        }
        if (existingIdWithSameName != null) {
            tables.remove(existingIdWithSameName);
        }
        tables.put(file.getId(), new TableInfo(file, name, pkeyField));
    }

    public void addTable(DbFile file, String name) {
        addTable(file, name, "");
    }

    /**
     * Add a new table to the catalog.
     * This table has tuples formatted using the specified TupleDesc and its
     * contents are stored in the specified DbFile.
     * @param file the contents of the table to add;  file.getId() is the identfier of
     *    this file/tupledesc param for the calls getTupleDesc and getFile
     */
    public void addTable(DbFile file) {
        addTable(file, (UUID.randomUUID()).toString());
    }

    /**
     * Return the id of the table with a specified name,
     * @throws NoSuchElementException if the table doesn't exist
     */
    public int getTableId(String name) throws NoSuchElementException {
        // Added: Step 4 - linear scan for a table whose name matches
        if (name != null) {
            for (Map.Entry<Integer, TableInfo> entry : tables.entrySet()) {
                if (name.equals(entry.getValue().name)) {
                    return entry.getKey();
                }
            }
        }
        throw new NoSuchElementException("No table with name: " + name);
    }

    /**
     * Returns the tuple descriptor (schema) of the specified table
     * @param tableid The id of the table, as specified by the DbFile.getId()
     *     function passed to addTable
     * @throws NoSuchElementException if the table doesn't exist
     */
    public TupleDesc getTupleDesc(int tableid) throws NoSuchElementException {
        // Added: Step 5 - look up the file by id, then delegate to its own getTupleDesc()
        TableInfo info = tables.get(tableid);
        if (info == null) {
            throw new NoSuchElementException("No table with id: " + tableid);
        }
        return info.file.getTupleDesc();
    }

    /**
     * Returns the DbFile that can be used to read the contents of the
     * specified table.
     * @param tableid The id of the table, as specified by the DbFile.getId()
     *     function passed to addTable
     */
    public DbFile getDatabaseFile(int tableid) throws NoSuchElementException {
        // Added: Step 6 - look up the file by id and return it directly
        TableInfo info = tables.get(tableid);
        if (info == null) {
            throw new NoSuchElementException("No table with id: " + tableid);
        }
        return info.file;
    }

    public String getPrimaryKey(int tableid) {
        // Added: Step 7 - look up the stored primary key for this id
        TableInfo info = tables.get(tableid);
        if (info == null) {
            throw new NoSuchElementException("No table with id: " + tableid);
        }
        return info.pkeyField;
    }

    public Iterator<Integer> tableIdIterator() {
        // Added: Step 8 - iterate over all known table ids
        return tables.keySet().iterator();
    }

    public String getTableName(int id) {
        // Added: Step 9 - look up the stored name for this id
        TableInfo info = tables.get(id);
        if (info == null) {
            throw new NoSuchElementException("No table with id: " + id);
        }
        return info.name;
    }
    
    /** Delete all tables from the catalog */
    public void clear() {
        // Added: Step 10 - wipe out all stored tables
        tables.clear();
    }
    
    /**
     * Reads the schema from a file and creates the appropriate tables in the database.
     * @param catalogFile
     */
    public void loadSchema(String catalogFile) {
        String line = "";
        String baseFolder=new File(new File(catalogFile).getAbsolutePath()).getParent();
        try {
            BufferedReader br = new BufferedReader(new FileReader(catalogFile));
            
            while ((line = br.readLine()) != null) {
                //assume line is of the format name (field type, field type, ...)
                String name = line.substring(0, line.indexOf("(")).trim();
                String fields = line.substring(line.indexOf("(") + 1, line.indexOf(")")).trim();
                String[] els = fields.split(",");
                ArrayList<String> names = new ArrayList<>();
                ArrayList<Type> types = new ArrayList<>();
                String primaryKey = "";
                for (String e : els) {
                    String[] els2 = e.trim().split(" ");
                    names.add(els2[0].trim());
                    if (els2[1].trim().equalsIgnoreCase("int"))
                        types.add(Type.INT_TYPE);
                    else if (els2[1].trim().equalsIgnoreCase("string"))
                        types.add(Type.STRING_TYPE);
                    else {
                        System.out.println("Unknown type " + els2[1]);
                        System.exit(0);
                    }
                    if (els2.length == 3) {
                        if (els2[2].trim().equals("pk"))
                            primaryKey = els2[0].trim();
                        else {
                            System.out.println("Unknown annotation " + els2[2]);
                            System.exit(0);
                        }
                    }
                }
                Type[] typeAr = types.toArray(new Type[0]);
                String[] namesAr = names.toArray(new String[0]);
                TupleDesc t = new TupleDesc(typeAr, namesAr);
                HeapFile tabHf = new HeapFile(new File(baseFolder+"/"+name + ".dat"), t);
                addTable(tabHf,name,primaryKey);
                System.out.println("Added table : " + name + " with schema " + t);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        } catch (IndexOutOfBoundsException e) {
            System.out.println ("Invalid catalog entry : " + line);
            System.exit(0);
        }
    }
}