# [ITEM 3] private 생성자나 열거 타입으로 싱글톤임을 보증하라

싱글톤이란 인스턴스를 오직 하나만 생성할 수 있는 클래스를 말한다.  그런데 클래스를 싱글톤으로 만들면 이를 사용하는 클라이언트가 테스트하기가 어려워질 수 있다. 싱글톤 인스턴스는 자원을 공유하기 때문에 격리된 환경에서 수행하기 위해서는 매번 인스턴스의 상태를 초기화시켜줘야 한다.

## public static final 필드

```java
public class Elvis {
		public static final Elvis INSTANCE = new Elvis();
		private Elvis() {}
}
```

private 생성자는 Elvis.INSTANCE를 초기화할 때 딱 한 번만 호출된다. public이나 protected 생성자가 없으므로 Elvis 클래스가 초기화될 때 만들어진 인스턴스가 전체 시스템에서 하나뿐임이 보장된다. 단 리플렉션 API를 활용하면 private 생성자를 호출할 수도 있다.

### 장점

- 해당 클래스가 싱글톤임이 API에 명백히 드러난다.
- 간결함

---
**Git Issue**

🪞 리플렉션 API?

> private 생성자로 싱글턴 패턴을 만들때 예외인 권한이 있는 클라이언트는 리플렉션 API인 AccessibleObject.setAccessible을 사용해 private 생성자를 호출할 수 있다는데 리플렉션 API가 뭔가요??
>

먼저 코드로 예시를 먼저 들어볼게요.

```
Elvis instance1 = Elvis.getInstance();
Class<?> class = Elvis.class;
Constructor<?> constructor = class.getDeclaredConstructor();
constructor.setAccessible(true);
Elvis instance2 = (Elvis) constructor.newInstance();
```

위 코드에서 instance1과 instance2는 서로 다르다고 합니다.

Reflection API는 런타임에서 특정 클래스, 메서드, 필드 등의 정보에 접근할 수 있게 하는 Java API입니다. 애플리케이션이 실행될 때 JVM 메모리 내에 .class 파일들이 static 영역에 올라오는데, 이 영역에 접근해 특정 클래스에 대한 정보를 얻어냅니다.

이를 통해 Elvis 클래스 타입을 얻었을 때 `getDeclaredConstructor()` 메서드를 통해 생성자에 접근할 수 있습니다. 이후 `Constructor` 객체의 `setAccessible()` 메서드를 통해 접근 가능하게 변경하면 private 임에도 불구하고 새로운 인스턴스를 생성할 수 있습니다. 하지만 실제로 저희가 코드를 작성할 때 Reflection API를 활용할 일은 거의 없고, 구체적인 클래스가 정해지지 않은 상황에서 코딩을 할 일은 거의 없습니다.

위 내용들을 보면 Reflection API는 저희가 작성한 모든 것들을 마법처럼 건너뛰는 것 같지만, 대표적으로 성능 오버헤드 단점이 있습니다. 컴파일 타임이 아닌, 런타임에 동적으로 타입을 분석하고 정보를 가져오기 때문에 JVM을 최적화할 수 없다고 합니다. 또한 private 필드, 메서드 등에 접근하기 때문에 추상화가 깨지게 됩니다.

결국 Reflection API는 애플리케이션 개발보다는 프레임워크나 라이브러리에 주로 사용됩니다. 프레임워크나 라이브러리는 사용자가 어떤 클래스를 만들지 예측할 수 없기 때문에 동적으로 해결하기 위해 Reflection API를 활용한다고 합니다. 실제로 Spring의 Spring Container의 BeanFactory에서 사용됩니다.

---

## 정적 팩토리 메서드

```java
public class Elvis {
		private static final Elvis INSTANCE = new Elvis();
		
		private Elvis() {}
		
		public static Elvis getInstance() {
				return INSTANCE;
		}
}
```

Elvis.getInstance는 항상 같은 객체의 참조를 반환하므로 제2의 Elvis 인스턴스는 결고 만들어지지 않는다. (리플렉션을 통한 예외는 똑같이 적용된다.)

### 장점

- API를 바꾸지 않고도 싱글톤이 아니게 변경 가능
- 정적 팩토리 를 제네릭 싱글톤 팩토리로 만들 수 있다.
- 정적 팩토리의 메서드 참조를 공급자로 사용할 수 있다.

```java
public class Concert {
    // 공급자 Supplier (FunctionalInterface)
    public void start(Supplier<Singer> singerSupplier) {
        Singer singer = singerSupplier.get();
        singer.sing();
    }

    public static void main(String[] args) {
        Concert concert = new Concert();
        // T get();
        // getInstance()를 전달가능 : 인스턴스를 리턴하기 때문에 인자로 전달 가능
        concert.start(Elvis::getInstance);
    }
}
```

## 열거 타입 선언

```java
public enum Elvis {
		INSTANCE;
}
```

public 필드 방식과 비슷하지만, 더 간결하고 추가 노력없이 직렬화할 수 있고, 아주 복잡한 직렬화 상황이나 리플렉션 공격에서도 제2의 인스턴스가 생기는 일을 완벽히 막아준다. 조금 부자연스러워 보일 수 있으나 대부분 상황에서는 원소가 하나뿐인 열거 타입이 싱글톤을 만드는 가장 좋은 방법이다. 단, 만들려는 싱글톤이 Enum 외의 클래스를 상속해야 한다면 이 방법은 사용할 수 없다.

실제로 실무에서 열거 타입으로 싱글톤을 사용할지 스터디원들고 이야기해봤다. 다들 이런 코드에 대해서 상당히 생소하기도 했고, 직접 봤을 때도
이 코드가 싱글톤이라는 것을 한번에 이해하기 쉽지 않을 것 같다는 의견이 있었다. 실무에서도 이 방법이 가장 좋은 방법일지는 잘 모르겠다.

## 싱글톤 클래스 직렬화

객체 직렬화는 자바가 객체를 바이트 스트림으로 인코딩하고, 객체 역직렬화는 바이트 스트림으로부터 다시 객체를 재구성하는 것이다.

만일 어떤 클래스를 직렬화하여 다른 컴퓨터에 전송하려는데, 이 클래스를 싱글톤으로 구성하려고 한다. 하지만 이 싱글톤 클래스는 송신자가 파일을 받고 역직렬화시 깨지게 되어 더이상 싱글톤이 아니게 된다.

그 이유는 역직렬화 자체가 보이지 않은 생성자로서 역할을 수행하기 때문에 인스턴스를 또 다시 만들어 직렬화에 사용한 인스턴스와는 전혀 다른 인스턴스가 되기 때문이다.

이에 대한 대응으로 직렬화 관련 메서드인 `readResolve()` 를 정의한다. `readResolve()` 메서드를 정의하게 되면, 역직렬화 과정에서 readObject를 통해 만들어진 인스턴스 대신 `readResolve()` 에서 반환되는 인스턴스를 내가 원하는 것으로 바꿀 수 있기 때문이다.
