/*
 * Copyright (c) 2019.
 */

package com.hzeng.markdownxserver.file;

import com.hzeng.markdownxserver.config.Global;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hzeng
 * @email hzeng1998@gmail.com
 * @date 2019/1/2 20:11
 */
public class ChangeSet implements Comparable {

    private ArrayList<Operation> operations;

    private int fileVerstion;

    private String user;

    public String getFileID() {
        return fileID;
    }

    public ChangeSet(ArrayList<Operation> operations, int fileVerstion, String user, String fileID) {
        this.operations = operations;
        this.fileVerstion = fileVerstion;
        this.user = user;
        this.fileID = fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    private String fileID;

    public ArrayList<Operation> getOperations() {
        return operations;
    }

    public void setOperations(ArrayList<Operation> operations) {
        this.operations = operations;
    }

    public int getFileVerstion() {
        return fileVerstion;
    }

    public void setFileVerstion(int fileVerstion) {
        this.fileVerstion = fileVerstion;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangeSet)) return false;
        ChangeSet changeSet = (ChangeSet) o;
        return getFileVerstion() == changeSet.getFileVerstion() &&
                Objects.equals(getOperations(), changeSet.getOperations()) &&
                getUser().equals(changeSet.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOperations(), getFileVerstion(), getUser());
    }

    /**
     * Compares this object with the specified object for order.  Returns a
     * negative integer, zero, or a positive integer as this object is less
     * than, equal to, or greater than the specified object.
     *
     * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
     * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
     * <tt>y.compareTo(x)</tt> throws an exception.)
     *
     * <p>The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     *
     * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
     * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
     * all <tt>z</tt>.
     *
     * <p>It is strongly recommended, but <i>not</i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
     * class that implements the <tt>Comparable</tt> interface and violates
     * this condition should clearly indicate this fact.  The recommended
     * language is "Note: this class has a natural ordering that is
     * inconsistent with equals."
     *
     * <p>In the foregoing description, the notation
     * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
     * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
     * <tt>0</tt>, or <tt>1</tt> according to whether the value of
     * <i>expression</i> is negative, zero or positive.
     *
     * @param changeSet the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(Object changeSet) {

        if (fileID.compareTo(((ChangeSet) changeSet).fileID) > 0) {
            return 1;
        }
        if (fileID.compareTo(((ChangeSet) changeSet).fileID) < 0) {
            return -1;
        }
        return fileVerstion - ((ChangeSet) changeSet).fileVerstion;
    }

    public void insertChangeSet() {

        int index;
        ArrayList<ChangeSet> fileChanges = Global.getChangeSetMap().get(fileID);
        for (index = fileChanges.size() - 1; index >= 0; index--) {
            if (compareTo(fileChanges.get(index)) > 0)
                break;
        }

        fileChanges.add(index + 1, this);
    }

    private void transformation(ChangeSet beforeChange) {

        int operationLen = operations.size();
        int beforeChangeLen = beforeChange.operations.size();

        int beforePos = 0, pos = 0;
        int count = 0; // change number
        int indexChange = 0, index = 0;

        while (indexChange < beforeChangeLen && index < operationLen) {

            Operation beforeOperation = beforeChange.operations.get(indexChange);
            Operation operation = operations.get(index);

            if (beforeOperation.getMethod() == Method.DELETE) {
                count = -(beforeOperation.getValue().length());
                beforePos += beforeOperation.getValue().length();
            }

            if (beforeOperation.getMethod() == Method.INSERT) {
                count = (beforeOperation.getValue().length());
            }

            if (beforeOperation.getMethod() == Method.RETAIN) {
                beforePos += Integer.valueOf(beforeOperation.getValue());
            }

            if (operation.getMethod() == Method.DELETE) {
                pos += operation.getValue().length();
            }

            if (operation.getMethod() == Method.RETAIN) {
                pos += Integer.valueOf(operation.getValue());
                if (pos >= beforePos) {
                    operation.setValue(String.valueOf(Integer.valueOf(operation.getValue()) + count));
                    indexChange++;
                    count = 0;
                } else {
                    index++;
                }
            }

            if (operation.getMethod() == Method.DELETE) {
                pos += operation.getValue().length();
                index++;
            }

            if (operation.getMethod() == Method.INSERT) {
                index++;
            }
        }
    }

    public void transformitter() {

        ArrayList<ChangeSet> fileChanges = Global.getChangeSetMap().get(fileID);

        for (int index = fileChanges.size() - 1; index >= 0; index--) {
            if (fileChanges.get(index).fileVerstion >= fileVerstion) {
                transformation(fileChanges.get(index));
            } else {
                break;
            }
        }
    }
/*
    public ChangeSet(String string) {

        Pattern pattern = Pattern.compile("Operation\\{method=(.*)?, value='(.*)?'}");
        Matcher matcher = pattern.matcher(string);

        operations = new ArrayList<>();

        while (matcher.find()) {

            operations.add(new Operation());
            matcher.group(1);
        }
    }
    */
}
