package edu.cwu.catmap.core;

import java.util.Objects;

/**
 * ClassItem class to hold information about the classes to add to the recycler view
 */
public class ClassItem {
    private String name;
    private int color;

    public ClassItem(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object object) {
        //true if same object id
        if(this == object) return true;

        //false if null or different type
        if(!(object instanceof ClassItem)) return false;

        ClassItem classItem = (ClassItem) object;
        return color == classItem.getColor() && Objects.equals(name, classItem.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, color);
    }

    @Override
    public String toString() {
        return ("name: " + name + ", color: " + color);
    }
}
