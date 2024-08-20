package programmers.effectivejava.chap2_creating_destroying_objects.item3;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class MySingletonClassTest {
	@Test
	public void ReflectionTest() throws
		NoSuchMethodException,
		InvocationTargetException,
		InstantiationException,
		IllegalAccessException {
		MySingletonClass instance1 = MySingletonClass.getInstance();

		Class<MySingletonClass> myClass = MySingletonClass.class;
		Constructor<MySingletonClass> declaredConstructor = myClass.getDeclaredConstructor();
		declaredConstructor.setAccessible(true);

		MySingletonClass instance2 = declaredConstructor.newInstance();
		System.out.println(instance1);
		System.out.println(instance2);
		Assertions.assertThat(instance1).isNotEqualTo(instance2);
	}
}
