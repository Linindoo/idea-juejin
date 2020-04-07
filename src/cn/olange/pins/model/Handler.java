package cn.olange.pins.model;

@FunctionalInterface
public interface Handler<E> {
	void handle(E var1);
}
