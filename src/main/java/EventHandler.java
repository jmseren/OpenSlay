import java.util.LinkedList;

public class EventHandler {
    private LinkedList<Event> eventQueue;
    

    public EventHandler(){
        this.eventQueue = new LinkedList<Event>();
    }
    public void pushEvent(Event e){
        eventQueue.add(e);

    }
    public void pushEvent(Events e){
        pushEvent(new Event(e));
    }
    public void pushEvent(Events e, Object o){
        pushEvent(new Event(e, o));
    }
    public Event nextEvent(){
        if(eventQueue.size() == 0) return new Event(Events.NO_EVENT, null);
        return eventQueue.removeFirst();
    }
    public void clearQueue(){
        eventQueue.clear();
    }
    public int queueSize(){
        return eventQueue.size();
    }
}
