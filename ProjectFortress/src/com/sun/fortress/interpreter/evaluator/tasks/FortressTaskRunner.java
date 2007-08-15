/********************************************************************************
    Copyright 2007 Sun Microsystems, Inc.,
    4150 Network Circle, Santa Clara, California 95054, U.S.A.
    All rights reserved.

    U.S. Government Rights - Commercial software.
    Government users are subject to the Sun Microsystems, Inc. standard
    license agreement and applicable provisions of the FAR and its supplements.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.

    Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
    trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
********************************************************************************/

package com.sun.fortress.interpreter.evaluator.tasks;

import jsr166y.forkjoin.*;


import com.sun.fortress.interpreter.evaluator.transactions.ContentionManager;
import com.sun.fortress.interpreter.evaluator.transactions.exceptions.AbortedException;
import com.sun.fortress.interpreter.evaluator.transactions.exceptions.PanicException;
import com.sun.fortress.interpreter.evaluator.transactions.exceptions.SnapshotException;
import com.sun.fortress.interpreter.evaluator.transactions.Transaction;

import java.util.concurrent.Callable;
import com.sun.fortress.interpreter.evaluator.FortressError;

public class FortressTaskRunner extends ForkJoinWorkerThread {
 /**
 * Contention manager class.
 */
    protected static Class contentionManagerClass;
/**
 * number of committed transactions for all threads
 */
    public static long totalCommitted = 0;
/**
 * total number of transactions for all threads
 */
    public static long totalTotal = 0;
/**
 * number of committed memory references for all threads
 */
    public static long totalCommittedMemRefs = 0;
/**
 * total number of memory references for all threads
 */
    public static long totalTotalMemRefs = 0;

    static ThreadLocal<ThreadState> _threadState = new ThreadLocal<ThreadState>() {
        protected synchronized ThreadState initialValue() {
        return new ThreadState();

	}
    };
    static ThreadLocal<Thread> _thread = new ThreadLocal<Thread>() {
        protected synchronized Thread initialValue() {
        return null;
        }
    };

    private static int MAX_NESTING_DEPTH = 100;

    private static Object lock = new Object();

    public volatile BaseTask currentTask;

    public BaseTask getCurrentTask() {return currentTask;}
    public void setCurrentTask(BaseTask task) {currentTask = task;}

    public FortressTaskRunner(FortressTaskRunnerGroup group) {
	super(group);
        try {
            Class managerClass = Class.forName("com.sun.fortress.interpreter.evaluator.transactions.manager.BackoffManager");
            setContentionManagerClass(managerClass);
        } catch (ClassNotFoundException ex) {
            System.out.println("UhOh Contention Manager not found");
            System.exit(0);
        }
    }


    /**
     * Establishes a contention manager.  You must call this method
     * before creating any <code>Thread</code>.
     *
     * @see com.sun.fortress.interpreter.evaluator.transactions.ContentionManager
     * @param theClass class of desired contention manager.
     */
    public static void setContentionManagerClass(Class theClass) {
	Class cm;
	try {
	    cm = Class.forName("com.sun.fortress.interpreter.evaluator.transactions.ContentionManager");
	} catch (ClassNotFoundException e) {
	    throw new PanicException(e);
	}
	try {
	    contentionManagerClass = theClass;
	} catch (Exception e) {
	    throw new PanicException("The class " + theClass
				     + " does not implement com.sun.fortress.interpreter.evaluator.transactions.ContentionManager");
	}
    }

    /**
     * Tests whether the current transaction can still commit.  Does not
     * actually end the transaction (either <code>commitTransaction</code> or
     * <code>abortTransaction</code> must still be called).  The contention
     * manager of the invoking thread is notified if the onValidate fails
     * because a <code>TMObject</code> opened for reading was invalidated.
     *
     * @return whether the current transaction may commit successfully.
     */
    static public boolean validate() {
	ThreadState threadState = _threadState.get();
	return threadState.validate();
    }

    /**
     * Gets the current transaction, if any, of the invoking <code>Thread</code>.
     *
     * @return the current thread's current transaction; <code>null</code> if
     *         there is no current transaction.
     */
    static public Transaction getTransaction() {
	return _threadState.get().transaction;
    }

    /**
     * Gets the contention manager of the invoking <code>Thread</code>.
     *
     * @return the invoking thread's contention manager
     */
    static public ContentionManager getContentionManager() {
	return _threadState.get().manager;
    }

    /**
     * Execute a transaction
     * @param xaction execute this object's <CODE>call()</CODE> method.
     * @return result of <CODE>call()</CODE> method
     */
    public static <T> T doIt(Callable<T> xaction) {
	ThreadState threadState = _threadState.get();
	ContentionManager manager = threadState.manager;
	T result = null;
	try {
	    while (true) {
		threadState.beginTransaction();
		try {
		    result = xaction.call();
		} catch (AbortedException e) {
		} catch (SnapshotException e) {
		    threadState.abortTransaction();
		} catch (FortressError e) {
 		    e.printStackTrace(System.out);
		    throw new PanicException("Unhandled exception " + e);
		} catch (Exception e) {
		    e.printStackTrace();
		    throw new PanicException("Unhandled exception " + e);
		} catch (Error e) {
		    e.printStackTrace();
		    throw new PanicException("Error " + e);
		}
                threadState.totalMemRefs += threadState.transaction.memRefs;
		if (threadState.commitTransaction()) {
		    threadState.committedMemRefs += threadState.transaction.memRefs;
		    return result;
		}
		threadState.transaction.attempts++;
		// transaction aborted
	    }
	} finally {
	    threadState.transaction = null;
	}
    }
    /**
     * Execute transaction
     * @param xaction call this object's <CODE>run()</CODE> method
     */
    public static void doIt(final Runnable xaction) {
	doIt(new Callable<Boolean>() {
		 public Boolean call() {
		     xaction.run();
		     return false;
		 };
	     });
    }

    /**
     * number of transactions committed by this thread
     * @return number of transactions committed by this thread
     */
    public static long getCommitted() {
	return totalCommitted;
    }

    /**
     * umber of transactions aborted by this thread
     * @return number of aborted transactions
     */
    public static long getAborted() {
	return totalTotal -  totalCommitted;
    }

    /**
     * number of transactions executed by this thread
     * @return number of transactions
     */
    public static long getTotal() {
	return totalTotal;
    }

    /**
     * Register a method to be called every time this thread validates any transaction.
     * @param c abort if this object's <CODE>call()</CODE> method returns false
     */
    public static void onValidate(Callable<Boolean> c) {
	_threadState.get().onValidate.add(c);
    }
    /**
     * Register a method to be called every time the current transaction is validated.
     * @param c abort if this object's <CODE>call()</CODE> method returns false
     */
    public static void onValidateOnce(Callable<Boolean> c) {
	_threadState.get().onValidateOnce.add(c);
    }
    /**
     * Register a method to be called every time this thread begins a transaction.
     * @param r call this object's <CODE>run()</CODE> method
     */
    public static void onBegin(Runnable r) {
	_threadState.get().onBegin.add(r);
    }
    /**
     * Register a method to be called the next time this thread begins a transaction.
     * @param r call this object's <CODE>run()</CODE> method
     */
    public static void onBeginOnce(Runnable r) {
	_threadState.get().onBeginOnce.add(r);
    }
    /**
     * Register a method to be called every time this thread commits a transaction.
     * @param r call this object's <CODE>run()</CODE> method
     */
    public static void onCommit(Runnable r) {
	_threadState.get().onCommit.add(r);
    }
    /**
     * Register a method to be called once if the current transaction commits.
     * @param r call this object's <CODE>run()</CODE> method
     */
    public static void onCommitOnce(Runnable r) {
	_threadState.get().onCommitOnce.add(r);
    }
    /**
     * Register a method to be called every time this thread aborts a transaction.
     * @param r call this objec't <CODE>run()</CODE> method
     */
    public static void onAbort(Runnable r) {
	_threadState.get().onAbort.add(r);
    }
    /**
     * Register a method to be called once if the current transaction aborts.
     * @param r call this object's <CODE>run()</CODE> method
     */
    public static void onAbortOnce(Runnable r) {
	_threadState.get().onAbortOnce.add(r);
    }
    /**
     * get thread ID for debugging
     * @return unique id
     */
    public static int getID() {
	return _threadState.get().hashCode();
    }

    /**
     * reset thread statistics
     */
    public static void clear() {
	totalTotal = 0;
	totalCommitted = 0;
	totalCommittedMemRefs = 0;
	totalTotalMemRefs = 0;
    }

}
