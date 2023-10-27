package issue1;

public interface InterfaceC extends InterfaceA, InterfaceB {
    default void methodInterfaceC() {
        methodInterfaceB();
    }
}
