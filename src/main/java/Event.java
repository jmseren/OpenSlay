public class Event {
    private Events eventType;
    private Object eventData;

    public Event(Events event){
        this.eventType = event;
        this.eventData = null;
    }
    public Event(Events event, Object data){
        this.eventType = event;
        this.eventData = data;
        
    }
    public Object getEventData() {
        return eventData;
    }
    public Events getEventType() {
        return eventType;
    }

}
