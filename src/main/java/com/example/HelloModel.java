package com.example;

import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }



    public record ChatEntry(
            String sender,
            String message,
            LocalDate timeStamp){}


    private static final DateTimeFormatter TIME_FORMATTER=DateTimeFormatter.ofPattern("HH:mm");

    public ObservableList<ChatEntry> getChatHistory() {
    }

    ObservableList<ChatEntry> chatHistory=new ObservableList<String>() {
        @Override
        public void addListener(ListChangeListener<? super String> listChangeListener) {

        }

        @Override
        public void removeListener(ListChangeListener<? super String> listChangeListener) {

        }

        @Override
        public boolean addAll(String... strings) {
            return false;
        }

        @Override
        public boolean setAll(String... strings) {
            return false;
        }

        @Override
        public boolean setAll(Collection<? extends String> collection) {
            return false;
        }

        @Override
        public boolean removeAll(String... strings) {
            return false;
        }

        @Override
        public boolean retainAll(String... strings) {
            return false;
        }

        @Override
        public void remove(int i, int i1) {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<String> iterator() {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return null;
        }

        @Override
        public boolean add(String s) {
            return false;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends String> c) {
            return false;
        }

        @Override
        public boolean addAll(int index, Collection<? extends String> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public String get(int index) {
            return "";
        }

        @Override
        public String set(int index, String element) {
            return "";
        }

        @Override
        public void add(int index, String element) {

        }

        @Override
        public String remove(int index) {
            return "";
        }

        @Override
        public int indexOf(Object o) {
            return 0;
        }

        @Override
        public int lastIndexOf(Object o) {
            return 0;
        }

        @Override
        public ListIterator<String> listIterator() {
            return null;
        }

        @Override
        public ListIterator<String> listIterator(int index) {
            return null;
        }

        @Override
        public List<String> subList(int fromIndex, int toIndex) {
            return List.of();
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {

        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {

        }
    }

}
