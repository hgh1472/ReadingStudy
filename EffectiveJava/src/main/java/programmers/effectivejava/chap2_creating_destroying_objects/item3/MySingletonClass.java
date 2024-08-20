package programmers.effectivejava.chap2_creating_destroying_objects.item3;

public class MySingletonClass {
	private static MySingletonClass instance = new MySingletonClass();

	private MySingletonClass() {}

	public static MySingletonClass getInstance() {
		return instance;
	}
}
