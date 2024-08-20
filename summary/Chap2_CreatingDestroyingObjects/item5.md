# [ITEM 5] 자원을 직접 명시하지 말고 의존 객체 주입을 사용하라

많은 클래스가 하나 이상의 자원에 의존한다. 예를 들어 `Service` 객체는 `Repository` 에 의존한다.

```java
class MyService {
    private final MyRepository repository = new MyRepositoryImpl();
    private MyService() {}
    public static MyService INSTANCE = new MyService();
}
```

이 방식은 `Repository` 를 단 하나만 사용할 수 있다. 실제로 다른 Repository를 사용해야하는 상황이 있을 수 있지만 위 코드는 MyRepositoryImpl을 고정적으로 사용한다.

MyService가 여러 MyRepository 구현체에 대응할 수 있도록 해보자. 단순히 repository의 final을 없애고 다른 사전으로 교체하는 메서드를 추가할 수 있다. 하지만 이 방식은 오류를 내기 쉽고 멀티스레드 환경에서는 쓸 수 없다. **사용하는 자원에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글톤 방식이 적합하지 않다.**

---
❓ 생성자 주입은 싱글톤 방식을 사용하면 안되는걸까?

책에서 말한 ‘**사용하는 자원에 따라 동작이 달라지는 클래스에는 정적 유틸리티 클래스나 싱글톤 방식이 적합하지 않다.’** 이 내용은 자원에 따라 여러가지를 생성해놓고 사용하는 경우를 말하는 것으로 보인다.

위의 코드 예시는 Service와 Repository를 예시로 들어서 헷갈릴 수 있지만 책에서는 `맞춤법 검사기 - 사전` 을 예시로 들었다. 맞춤법 검사기는 여러 곳에서 다양한 언어로 사용될 수 있으므로 싱글톤 방식이 적합하지 않다는 뜻으로 이해된다.

---

대신 클래스가 여러 자원 인스턴스를 지원해야 하며, 클라이언트가 원하는 자원을 사용해야 한다. 이를 위해 인스턴스를 생성할 때 생성자에 필요한 자원을 넘겨주는 방식이다. 이는 의존 객체 주입의 한 형태이다.

```java
class MyService {
    private final MyRepository repository;
    
    public MyService(MyRepository myRepository) {
        this.repository = myRepository;
    }
}
```

의존 객체 주입 패턴은 단순하여 사용하기 쉽다. 또한 불변을 보장하여 여러 클라이언트가 의존 객체들을 안심하고 공유할 수 있기도 하다.

또한 생성자에 자원 팩토리를 넘겨주는 방식도 가능하다.

의존 객체 주입이 유연성과 테스트 용이성을 개선해주지만, 의존성이 많아지는 경우 코드가 어지럽게 될 수도 있다. 하지만 스프링 같은 프레임워크를 사용하면 비교적 편하게 사용할 수 있다.
