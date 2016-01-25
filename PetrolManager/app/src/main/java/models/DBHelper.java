package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by Owner on 12/2/2015.
 */
public class DBHelper extends SQLiteOpenHelper {
    //database name
    public static final String DATABASE_NAME = "engen.db";
    //user Table
    public static final String userT = "user";
    public static final String uid = "uid";
    public static final String unm = "unm";
    public static final String pin = "pin";
    public static final String utype = "utype";
    public static final String ustatus = "ustatus";
    public static final String userTable = "create table user " +
            "(" + uid + " integer primary key AUTOINCREMENT, " + unm + " text," + pin + " text," + utype + " text," + ustatus + " text)";


    //queue table
    public static final String queueT = "tqueue";
    public static final String qId = "qid";
    public static final String tId = "tid";
    public static final String pMode = "pmode";
    public static final String request = "request";
    public static final String sum = "sum";
    public static final String qTime = "time";
    public static final String queueTable = "create table tqueue" +
            "(" + qId + " integer primary key AUTOINCREMENT, " + tId + " text, " + pMode + " text, " + request + " text,"+sum+" text, " + qTime + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    //Reconciliation table
    public static final String recT = "reconst";
    public static final String recId = "rid";
    public static final String tra_Id = "tid";
    public static final String descr = "descr";
    public static final String recTable = "create table reconst " +
            "(" + recId + " integer primary key AUTOINCREMENT, " + tra_Id + " text," + descr + " text)";


    //customer table
    public static final String customerT = "customer";
    public static final String custid = "custid";
    public static final String custtel = "custtel";
    public static final String custtin = "custtin";
    public static final String custname = "custname";
    public static final String time = "time";
    public static final String customerTable = "create table customer" +
            "(" + custid + " integer primary key AUTOINCREMENT, " + custname + " text, " + custtel + " text, " + custtin + " text, " + time + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

    //product table
    public static final String productT = "product";
    public static final String pid = "pid";
    public static final String pname = "pname";
    public static final String ppunity = "ppunity";
    public static final String productTable = "create table product" +
            "(" + pid + " integer primary key AUTOINCREMENT, " + pname + " text, " + ppunity + " text)";
    //transaction
    public static final String transactionT = "trans";
    public static final String tid = "tid";
    public static final String tpid = "pid";
    public static final String tcustid = "custid";
    public static final String tuid = "uid";
    public static final String quantity = "quantity";
    public static final String amount = "amount";
    public static final String tstatus = "status";
    public static final String ttime = "time";
    public static final String transactionTable = "create table trans" +
            "(" + tid + " integer primary key AUTOINCREMENT, " + tpid + " integer REFERENCES product(pid) ON UPDATE CASCADE ON DELETE CASCADE," + tcustid +
            " integer REFERENCES customer(custid) ON UPDATE CASCADE ON DELETE CASCADE, " + tuid
            + " integer REFERENCES customer(custid) ON UPDATE CASCADE ON DELETE CASCADE, " + quantity + " text, " + amount + " text, " + tstatus + " text," + ttime
            + " TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";
    // Logcat tag
    private static final String LOG = DBHelper.class.getName();
    private static final int DATABASE_VERSION = 1;
    private HashMap hp;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(queueTable);
        db.execSQL(recTable);

        //future use consept
        db.execSQL(userTable);
        db.execSQL(productTable);
        db.execSQL(customerTable);
        db.execSQL(transactionTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS tqueue");
        db.execSQL("DROP TABLE IF EXISTS reconst");

        //future use consept
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS product");
        db.execSQL("DROP TABLE IF EXISTS customer");
        db.execSQL("DROP TABLE IF EXISTS transaction");
        onCreate(db);
    }

    // closing database
    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }

    //Action by each Entity

    // ------------------------ "USER" table methods ----------------//

    /**
     * Creating a user
     */
    public long createUser(User user, long[] user_ids) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(unm, user.getUnm());
        values.put(pin, user.getPin());
        values.put(utype, user.getUtype());
        values.put(ustatus, user.getUstatus());

        // insert row
        long user_id = db.insert(userT, null, values);

        return user_id;
    }

    /**
     * get single user
     */
    public User getSingleUser(long u_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + userT + " WHERE "
                + uid + " = " + u_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        User user = new User();
        user.setUid(String.valueOf(c.getInt(c.getColumnIndex(uid))));
        user.setUnm(c.getString(c.getColumnIndex(unm)));
        user.setPin(c.getString(c.getColumnIndex(pin)));
        user.setUtype(c.getString(c.getColumnIndex(utype)));
        user.setUstatus(c.getString(c.getColumnIndex(ustatus)));

        return user;
    }

    /**
     * get single user by pin
     */
    public User getSingleUserPin(long u_pin) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + userT + " WHERE "
                + pin + " = " + u_pin;

        User user = new User();

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null){
            if (c.moveToFirst()) {
                do {
                    user.setUid(String.valueOf(c.getInt(c.getColumnIndex(uid))));
                    user.setUnm(c.getString(c.getColumnIndex(unm)));
                    user.setPin(c.getString(c.getColumnIndex(pin)));
                    user.setUtype(c.getString(c.getColumnIndex(utype)));
                    user.setUstatus(c.getString(c.getColumnIndex(ustatus)));
                } while (c.moveToNext());
        }else{
                user.setUstatus("User not Found");
            }
        }else{
            user.setUstatus("Data not Allowed");
        }

        return user;
    }

    /**
     * getting all Users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<User>();
        String selectQuery = "SELECT  * FROM " + userT;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                User user = new User();
                user.setUid(String.valueOf(c.getInt(c.getColumnIndex(uid))));
                user.setUnm(c.getString(c.getColumnIndex(unm)));
                user.setPin(c.getString(c.getColumnIndex(pin)));
                user.setUtype(c.getString(c.getColumnIndex(utype)));
                user.setUstatus(c.getString(c.getColumnIndex(ustatus)));

                // adding user to list
                users.add(user);
                c.moveToNext();
            }
        }

        return users;
    }

    /**
     * getting user count
     */
    public int getUserCount() {
        String countQuery = "SELECT  * FROM " + userT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Updating a user
     */
    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(unm, user.getUnm());
        values.put(pin, user.getPin());
        values.put(utype, user.getUnm());
        values.put(ustatus, user.getUstatus());

        // updating row
        return db.update(userT, values, uid + " = ?",
                new String[]{String.valueOf(user.getUid())});
    }

    /**
     * Deleting a user
     */
    public void deleteUser(long u_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(userT, uid + " = ?",
                new String[]{String.valueOf(u_id)});
    }


    // ------------------------ "CUSTOMER" table methods ----------------//

    /**
     * Creating a user
     */
    public long createCustomer(Customer cust, long[] cust_ids) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(custname, cust.getCustname());
        values.put(custtel, cust.getCusttel());
        values.put(custtin, cust.getCusttin());

        // insert row
        long cust_id = db.insert(customerT, null, values);

        return cust_id;
    }

    /**
     * get single customer
     */
    public Customer getSingleCustomer(long cust_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + customerT + " WHERE "
                + custid + " = " + cust_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Customer cust = new Customer();
        cust.setCustid(String.valueOf(c.getInt(c.getColumnIndex(custid))));
        cust.setCustname(c.getString(c.getColumnIndex(custname)));
        cust.setCusttel(c.getString(c.getColumnIndex(custtel)));
        cust.setCusttin(c.getString(c.getColumnIndex(custtin)));
        cust.setTime(c.getString(c.getColumnIndex(time)));

        return cust;
    }

    /**
     * getting all Customer
     */
    public List<Customer> getAllCustomer() {
        List<Customer> custs = new ArrayList<Customer>();
        String selectQuery = "SELECT  * FROM " + customerT;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Customer cust = new Customer();
                cust.setCustid(String.valueOf(c.getInt(c.getColumnIndex(custid))));
                cust.setCustname(c.getString(c.getColumnIndex(custname)));
                cust.setCusttel(c.getString(c.getColumnIndex(custtel)));
                cust.setCusttin(c.getString(c.getColumnIndex(custtin)));
                cust.setTime(c.getString(c.getColumnIndex(time)));

                // adding customer to list
                custs.add(cust);
            } while (c.moveToNext());
        }

        return custs;
    }

    /**
     * getting customer count
     */
    public int getCustomerCount() {
        String countQuery = "SELECT  * FROM " + customerT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Updating a Customer
     */
    public int updateCustomer(Customer cust) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(custid, cust.getCustid());
        values.put(custname, cust.getCustname());
        values.put(custtel, cust.getCusttel());
        values.put(custtin, cust.getCusttin());

        // updating row
        return db.update(customerT, values, custid + " = ?",
                new String[]{String.valueOf(cust.getCustid())});
    }

    /**
     * Deleting a customer
     */
    public void deleteCustomer(long cust_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(customerT, custid + " = ?",
                new String[]{String.valueOf(cust_id)});
    }

    // ------------------------ "PRODUCT" table methods ----------------//

    /**
     * Creating a Product
     */
    public long createProduct(Product prod, long[] prod_ids) {
        SQLiteDatabase db = this.getWritableDatabase();


        ContentValues values = new ContentValues();
        values.put(pname, prod.getPname());
        values.put(ppunity, prod.getPpunity());

        // insert row
        long cust_id = db.insert(customerT, null, values);

        return cust_id;
    }

    /**
     * get single product
     */
    public Product getSingleProduct(long p_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + productT + " WHERE "
                + pid + " = " + p_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Product prod = new Product();
        prod.setPid(String.valueOf(c.getInt(c.getColumnIndex(pid))));
        prod.setPname(c.getString(c.getColumnIndex(pname)));
        prod.setPpunity(c.getString(c.getColumnIndex(ppunity)));

        return prod;
    }

    /**
     * getting all Product
     */
    public List<Product> getAllProduct() {
        List<Product> prods = new ArrayList<Product>();
        String selectQuery = "SELECT  * FROM " + productT;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Product prod = new Product();
                prod.setPid(String.valueOf(c.getInt(c.getColumnIndex(pid))));
                prod.setPname(c.getString(c.getColumnIndex(pname)));
                prod.setPpunity(c.getString(c.getColumnIndex(ppunity)));

                // adding Product to list
                prods.add(prod);
            } while (c.moveToNext());
        }

        return prods;
    }

    /**
     * getting Product count
     */
    public int getProductCount() {
        String countQuery = "SELECT  * FROM " + productT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Updating a Product
     */
    public int updateProduct(Product prod) {
        SQLiteDatabase db = this.getWritableDatabase();

        String t = null;

        ContentValues values = new ContentValues();
        values.put(pid, prod.getPid());
        values.put(pname, prod.getPname());
        values.put(ppunity, prod.getPpunity());

        // updating row
        return db.update(productT, values, pid + " = ?",
                new String[]{String.valueOf(prod.getPid())});
    }

    /**
     * Deleting a Product
     */
    public void deleteProduct(long p_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(productT, pid + " = ?",
                new String[]{String.valueOf(p_id)});
    }

    // ------------------------ "TRANSACTION" table methods ----------------//

    /**
     * Creating a transaction
     */
    public long createTransaction(Transaction trans, long[] trans_ids) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(tid, trans.getTid());
        values.put(tpid, trans.getTpid());
        values.put(tcustid, trans.getTcustid());
        values.put(tuid, trans.getTuid());
        values.put(quantity, trans.getQuantity());
        values.put(amount, trans.getAmount());
        values.put(tstatus, trans.getTstatus());

        // insert row
        long tr_id = db.insert(transactionT, null, values);

        return tr_id;
    }

    /**
     * getting all Transaction
     */
    public List<Transaction> getAllTransaction() {
        List<Transaction> transs = new ArrayList<Transaction>();
        String selectQuery = "SELECT  * FROM " + transactionT;

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Transaction trans = new Transaction();
                trans.setTid(String.valueOf(c.getInt(c.getColumnIndex(tid))));
                trans.setTpid(String.valueOf(c.getInt(c.getColumnIndex(tpid))));
                trans.setTcustid(String.valueOf(c.getInt(c.getColumnIndex(tcustid))));
                trans.setTuid(String.valueOf(c.getInt(c.getColumnIndex(tuid))));
                trans.setQuantity(c.getString(c.getColumnIndex(quantity)));
                trans.setAmount(c.getString(c.getColumnIndex(amount)));
                trans.setTstatus(c.getString(c.getColumnIndex(tstatus)));
                trans.setTtime(c.getString(c.getColumnIndex(ttime)));

                // adding transaction to list
                transs.add(trans);
            } while (c.moveToNext());
        }

        return transs;
    }

    /**
     * getting transaction count
     */
    public int getTransctionCount() {
        String countQuery = "SELECT  * FROM " + transactionT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Updating a Transaction
     */
    public int updateTransaction(Transaction trans) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(tid, trans.getTid());
        values.put(tpid, trans.getTpid());
        values.put(tcustid, trans.getTcustid());
        values.put(tuid, trans.getTuid());
        values.put(quantity, trans.getQuantity());
        values.put(amount, trans.getAmount());
        values.put(tstatus, trans.getTstatus());

        // updating row
        return db.update(transactionT, values, tid + " = ?",
                new String[]{String.valueOf(trans.getTid())});
    }

    /**
     * Deleting a transaction
     */
    public void deleteTransaction(long t_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(transactionT, tid + " = ?",
                new String[]{String.valueOf(t_id)});
    }


    // ------------------------ "QUEUE" table methods ----------------//

    /**
     * Creating a queue
     */
    public long createQueue(TQueue queue) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(tId, queue.gettId());
        values.put(pMode, queue.getpMode());
        values.put(request, queue.getRequest());
        values.put(sum, queue.getSum());


        // insert row
        long q_id = db.insert(queueT, null, values);

        return q_id;
    }

    /**
     * get single Queue
     */
    public TQueue getSingleQueue(long t_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + queueT + " WHERE "
                + tId + " = " + t_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        TQueue queue=new TQueue();
try{
    if (c != null)
        c.moveToFirst();

    queue.setqId(String.valueOf(c.getInt(c.getColumnIndex(qId))));
    queue.settId(String.valueOf(c.getInt(c.getColumnIndex(tId))));
    queue.setpMode(c.getString(c.getColumnIndex(pMode)));
    queue.setRequest(c.getString(c.getColumnIndex(request)));
    queue.setSum(String.valueOf(c.getInt(c.getColumnIndex(sum))));
    queue.setqTime(c.getString(c.getColumnIndex(qTime)));

    return queue;
}catch (CursorIndexOutOfBoundsException e){
    e.printStackTrace();
    return null;
}
    }


    /**
     * getting all TQueue
     */
    public List<TQueue> getAllQueue() {
        List<TQueue> tq = new ArrayList<TQueue>();
        String selectQuery = "SELECT  * FROM " + queueT+" ORDER BY "+tId+" DESC";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                TQueue queue=new TQueue();

                queue.setqId(String.valueOf(c.getInt(c.getColumnIndex(qId))));
                queue.settId(String.valueOf(c.getInt(c.getColumnIndex(tId))));
                queue.setpMode(c.getString(c.getColumnIndex(pMode)));
                queue.setRequest(c.getString(c.getColumnIndex(request)));
                queue.setSum(String.valueOf(c.getInt(c.getColumnIndex(sum))));
                queue.setqTime(c.getString(c.getColumnIndex(qTime)));

                // adding Queue to list
                tq.add(queue);
                c.moveToNext();
            }
        }

        return tq;
    }

    /**
     * getting queueu count
     */
    public int getQueueCount() {
        String countQuery = "SELECT  * FROM " + queueT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    /**
     * Updating a Queue
     */
    public int updateQueue(TQueue queue) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(tId, queue.gettId());
        values.put(pMode, queue.getpMode());
        values.put(request, queue.getRequest());
        values.put(sum, queue.getSum());

        // updating row
        return db.update(queueT, values, tId + " = ?",
                new String[]{String.valueOf(queue.gettId())});
    }

    /**
     * Deleting a queue
     */
    public void deletequeue(long t_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(queueT, tId + " = ?",
                new String[]{String.valueOf(t_id)});
    }

    /**
     * Truncate a queue
     */
    public void truncateQueue() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(queueT, null,null);
    }


    // ------------------------ "RECONCILIATION" table methods ----------------//

    /**
     * Creating a Reconciliation
     */
    public long createRec(Reconciliation rec) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(tra_Id, rec.getTra_Id());
        values.put(descr, rec.getDes());


        // insert row
        long rec_id = db.insert(recT, null, values);

        return rec_id;
    }


    /**
     * getting reconciliation list
     */
    public List<Reconciliation> getAllRec() {
        List<Reconciliation> recs = new ArrayList<Reconciliation>();
        String selectQuery = "SELECT * FROM " + recT+" ORDER BY "+recId+" DESC";

        Log.e(LOG, selectQuery);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            while(c.isAfterLast() == false){
                Reconciliation rec=new Reconciliation();

                rec.setRecId(String.valueOf(c.getInt(c.getColumnIndex(recId))));
                rec.setTra_Id(String.valueOf(c.getInt(c.getColumnIndex(tra_Id))));
                rec.setDes(c.getString(c.getColumnIndex(descr)));

                // adding Reconciliation to list
                recs.add(rec);
                c.moveToNext();
            }
        }

        return recs;
    }

    /**
     * get single Queue
     */
    public Reconciliation getSingleRec(long rec_id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + recT + " WHERE "
                + recId + " = " + rec_id;

        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        if (c != null)
            c.moveToFirst();

        Reconciliation rec=new Reconciliation();

        rec.setRecId(String.valueOf(c.getInt(c.getColumnIndex(recId))));
        rec.setTra_Id(String.valueOf(c.getInt(c.getColumnIndex(tra_Id))));
        rec.setDes(c.getString(c.getColumnIndex(descr)));

        return rec;
    }

    /**
     * getting Reconciliation count
     */
    public int getRecCount() {
        String countQuery = "SELECT * FROM " + recT;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }


    /**
     * Deleting a reconciliation
     */
    public void deleteRec(long rec_id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(recT, tra_Id + " = ?",
                new String[]{String.valueOf(rec_id)});
    }

    /**
     * Truncate a reconciliation
     */
    public void truncateRec() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(recT, null,null);
    }

}

