public class NumberElement extends GUI {
    public int value;
    public int max;
    public int min;

    public NumberElement(int value, int min, int max, int x, int y){
        super("NumberElement", null, x, y, 0, 0);
        this.value = value;
        this.min = min;
        this.max = max;
    }
    
    public boolean click(int x, int y){
        return false;
    }
    public Event onClick() {
        return null;
    }
    public void draw(OpenSlay os) {
        os.text(value, x, y);
    }
    public void increment(){
        value++;
        if(value > max) value = max;
    }
    public void decrement(){
        value--;
        if(value < min) value = min;
    }
}
