import java.util.ArrayList;
import java.util.Collections;

// A ShuffleBag is used to create a more fair distribution of tiles.

public class ShuffleBag<T> {

    private ArrayList<T> bag;
    private int curr = 0;

    public ShuffleBag(){
        bag = new ArrayList<T>();
    }

    public void add(T item){
        bag.add(item);
        reshuffle();
    }
    public void add(T item, int amt){
        for(int i = 0; i < amt; i++) bag.add(item);
        reshuffle();
    }

    public T next(){
        T item = bag.get(curr-1);
        curr--;
        if(curr < 1){
            reshuffle();
        }
        return item;
    }

    private void reshuffle(){
        curr = bag.size();
        Collections.shuffle(bag);
    }
}
