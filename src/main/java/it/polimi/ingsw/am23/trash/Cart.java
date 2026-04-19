package it.polimi.ingsw.am23.trash;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private List<Double> storage;
    public Cart() {
        storage = new ArrayList<Double>();
    }

    public void addElement(String element, double price) throws IllegalArgumentException {
        if(price < 0){
            throw new IllegalArgumentException();
        }
        storage.add(price);
    }
    public double getTotal(){
        var total = storage
                .stream()
                .reduce(0.0, Double::sum);
        if(storage.size() > 3){
            total = total * 0.9;
        }
        return total;
    }

}
