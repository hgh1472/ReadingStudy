# Item 16. public 클래스에서는 public 필드가 아닌 접근자 메서드를 사용하라

## 📌 패키지 바깥에서 접근할 수 있는 클래스라면 접근자를 제공하자
```java

class Point {
    private double x;
    private double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}

```

- `Point`클래스(public 클래스)의 필드 x, y가 `public` 이라면
    - 외부에서 필드에 직접 접근할 수 있으니 `캡슐화`의 이점을 제공하지 못한다.
    - API를 수정하지 않고는 내부 표현을 바꿀 수 없다.
    - 불변식을 보장할 수 없다.
    - 외부에서 필드에 접근할 때 부수 작업을 수행할 수 없다.
> 위의 문제점을 해결하고 객체 지향 프로그래밍을 하기 위해 필드를 모두 private으로 바꾸고 public 접근자(getter)를 추가한다. 

### 이 규칙을 어긴 자바 플랫폼 라이브러리도 있다.
- 자바 플랫폼 라이브러리에도 `public`클래스의 필드를 직접 노출하지 말라는 규칙을 어기는 사례가 종종 있다.
- `java.awt.package` 패키지의 `Point`클래스와 `Dimension`클래스
- 내부를 노출한 클래스들의 심각한 성능 문제는 오늘날까지도 해결되지 못했다. 

### public 클래스의 필드가 불변이라면?
- API를 변경하지 않고는 표현 방식을 바꿀 수 없고, 필드를 읽을 때 부수 작업을 수행할 수 없다는 단점은 여전하다.
- 하지만 불변식은 보장할 수 있다. 

```java
public final class Time {
    private static final int HOURS_PER_DAY = 24;
    private static final int MINUTES_PER_HOUR = 60;

    public final int hour; //불변식 보장
    public final int minute; //불변식 보장

    public Time(int hour, int minute) {
        ...
        this.hour = hour;
        this.minute = minute;
    }
    ...
}
```

<br>

## 📌 package-private 클래스 혹은 private 중첩 클래스라면 데이터 필드를 노출해도 문제가 없다
- 클래서 선언 면에서나 이를 사용하는 클라이언트 코드 면에서나 접근자 방식보다 훨씬 깔끔하다.
- pacakge-private 클래스
    - 클라이언트 코드도 어차피 이 클래스를 포함하는 패키지 안에서만 동작하는 코드일 뿐이기 때문에 패키지 바깥 코드는 전혀 손대지 않아도 된다.
- private 중첩 클래스
    - 해당 클래스를 포함하는 외부 클래스까지로 제한된다.

