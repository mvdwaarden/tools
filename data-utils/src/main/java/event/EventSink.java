package event;

public interface EventSink {
	boolean accept(Event ev);
}
