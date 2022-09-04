public class TextElement extends GUI {
    public String text;

    public TextElement(String text, int x, int y){
        super(text, null, x, y, 0, 0);
        this.text = text;
    }
    public boolean click(int x, int y){
        return false;
    }
    public Event onClick() {
        return null;
    }
    public void draw(OpenSlay os) {
        os.text(text, x, y);
    }
}