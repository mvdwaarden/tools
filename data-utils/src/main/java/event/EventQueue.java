package event;

public interface EventQueue {
	void put(Event ev);

	void get(Event ev);

	void size();

	Event peek();
}
