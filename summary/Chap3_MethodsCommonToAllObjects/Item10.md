# Item 10. equals는 일반 구약을 지켜 재정의하라

## 재정의 하지 않는 것이 좋은 상황
- 각 인스턴스가 본질적으로 고유하다
    - 값을 표현하는게 아닌 동작하는 개체를 표현하는 클래스가 해당
    - e.g., Thread
- 인스턴스의 '논리적 동치성(logical equality)'를 검사 할 일이 없다.
    - e.g., java.util.regex.Pattern은 두 패턴의 인스턴스가 같은 정규표현식을 나타내는지 검사하는 = 논리적 동치성을 검사하는 방법
    - 이 방식을 원하지 않거나 애초에 필요하지 않다고 판단할 수도 있음 = 기본 equals만으로 해결
- 상위 클래스에서 재정의한 하위 equals가 하위 클래스에도 딱 들어맞는다.
    - e.g., 대부분의 Set구현체는 AbstractSet이 구현한 equals를 상속받아 씀
    - e.g., List -> AbstractList, Map -> AbstractMap으로부터 상속받아 사용
- 클래스가 private 이거나 package-private이고 equals 메서드를 호출할 일이 없다
    - equals가 실수로라도 호출되는 걸 막고싶을 경우 아래 코드처럼 막아두기
    ```
    @Override public boolean equals(Object o){
        throw new AssertionError(); //호출 금지
    }
    ```

## 재정의 하는 것이 좋은 상황
- 논리적 동치성을 확인해야 하는데 상의 클래스의 equals가 논리적 동치성을 비교하도록 재정의 되지 않았을 경우
    - 주로 값 클래스(Integer, String처럼 값을 표현하는 클래스)들이 해당됨.
    - 객체가 아니라 값이 같은지를 알고싶어 함 = 논리적 동치성을 확인하도록 재정의
    - 값 클래스라 하더라도 값이 같은 인스턴스가 둘 이상 만들어지지 않음을 보장 -> equals 재정의 할 필요 없음
        - e.g., Enum
        - 논리적으로 같은 인스턴스가 두 개 이상 만들어지지 않으니 논리적 동치성와 객체 식별성이 사실상 같은 의미

## equals 메서드 재정의 할 때 따라야 할 일반 규약(Object 명세에 적혀있음)
- equals 메서드는 동치관계(equivalence relation)을 구현하며, 다음을 만족한다<br><br>
- 반사성(reflexivity) : null이 아닌 모든 참조 값 x에 대해, x.equals(x)는 true다.
- 대칭성(symmetry) : null이 아닌 모든 참조 값 x, y에 대해, x.equals(y)가 true면 y.equals(x)도 true다.
- 추이성(transitivity) : null이 아닌 모든 참조값 x, y, z에 대해, x.equals(y)가 true이고, y.equals(z)도 true면 x.equals(z)도 true다.
- 일관성(consistency) : null이 아닌 모든 참조값 x, y에 대해 x.equals(y)를 반복해서 호출하면 항상 true를 반환하거나 항상 false를 반환한다.
- null-아님 : null이 아닌 모든 참조 값 x에 대해 x.equals(null)은 false이다.

### 동치 관계?
- 집합을 서로 같은 원소들로 이뤄진 부분집합으로 나누는 연산
- 이 부분집합을 동치류(equivalence class; 동치 클래스)라 한다
- equals 메서드가 쓸모있으려면 모든 원소가 같은 동치류에 속한 어떤 원소와도 서로 교환 할 수 있어야 한다<br><br>

----

## 반사성
- 객체는 자기 자신과 같아야 한다는 뜻
- 요건을 어긴 클래스의 인스턴스를 컬렉션에 넣은 다음 contains 메서드 호출시 방금 넣은 인스턴스가 없다고 답할 것

## 대칭성
- 두 객체는 서로에 대한 동치 여부에 똑같이 답해야 한다는 뜻
```
public final class CaseInsensitiveString{
    private final String s;

    public CaseInsensitiveString(String s){
        this.s=Objects.requireNonNull(s);
    }
}

//대칭성 위배
@Override public boolean equals(Object o){
    if(o instanceof CaseInsensitiveString){
        return s.equalsIgnoreCase(
            ((CaseInsensitiveString) o).s);
    }
    if(o instanceof String){ // 한 방향으로만 작동한다
        return s.equalsIgnoreCase((String) o);
    }
    return false;
}
```
- 대칭성을 위배한 잘못된 코드이다
- toString 메서드는 원본 문자열의 대소문자를 그대로 돌려주지만 equals에서는 대소문자를 무시한다
```
CaseInsensitiveString cis=new CaseInsensitiveString("Polish");
String s="polish";
```
- 위 코드 실행시 cis.equals(s)는 true를 반환함
- CaseInsensitiveString의 equals는 일반 String을 알고 있지만 String의 equals는 CaseInsensitiveString의 존재를 모름<br>
    -> s.equals(cis)는 false를 반환 = 대칭성 위반!

`- equals 규약을 어기면 그 객체를 사용하는 다른 객체들이 어떻게 반응할 지 모른다`
```
@Overried public boolean equals(Object o){
    return o instanceof CaseInsensitiveString &&
        ((CaseInsensitiveString) o).s.equalsIgnoreCase(s);
}

```
- 대칭성 문제를 해결한 equals 코드

## 추이성
- 첫 번째 객체와 두 번째 객체가 같고, 두 번째 객체와 세 번째 객체가 같다면, 첫 번째 객체와 세 번째 객체도 같아야 한다는 뜻

- 상위 클래스에는 없는 새로운 필드를 하위 클래스에 추가하는 상황을 생각해보자
    - equals 비교에 영향을 주는 정보를 추가하는 것

```
public class Point{
    private final int x;
    private final int y;

    public Point(int x, int y){
        this.x=x;
        this.y=y;
    }


    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Point))
            return false;
        Point p=(Point) o;
        return p.x==x && p.y==y;
    }
    //나머지 코드 생략
}
```
- 2차원 방향에서의 점을 표현하는 클래스
```
public class ColorPoint extends Point{
    private final Color color;

    public ColorPoint(int x, int y, Color color){
        super(x, y);
        this.color=color;
    }
    //나머지 코드 생략
}
```
- 위 클래스에 색상 정보를 추가 한 클래스

-  equals 메서드를 그대로 두면 Point의 구현이 상속되어 색상 정보는 무시한 채 비교를 수행한다<br><br>
---
- 위치와 색상이 같을 때만 true를 반환하는 equals로 바꾸고자 함
```
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Point))
            return false;
        return super.equals(o) && ((ColorPoint) o).color==color;
    }
```
- 일반 Point를 colorPoint에 비교한 결과와 그 둘을 바꿔 비교한 결과가 다를 수 있다.
- Point의 equals는 색상을 무시, ColorPoint의 equals는 입력 매개변수의 클래스 종류가 다르기에 매번 false를 반환
```
Point p=new Point(1, 2);
ColorPoint cp=new ColorPoint(1, 2, Color.RED);
```
- 위 코드 실행시 p.equals(cp)는 true, cp.equals(p)는 false를 반환
---
```
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Point))
            return false;
        
        //o가 일반 Point면 색상을 무시하고 비교
        if(!(o instanceof ColorPoint))
            return o.equals(this);
        
        //ColorPoint면 색상까지 비교
        return super.equals(o)&&((ColorPoint)o).color=color;
    }
```    
- 대칭성은 만족하지만 추이성 위배
```
ColorPoint p1=new ColorPoint(1,2,Color.RED);
Point p2=new Point(1,2);
ColorPoint p3=new ColorPoint(1,2,Color.BLUE);
```
- 위 코드 실행시 p1.equals(p2)외 p2.equals(p3)는 true지만 p1.equals(p3)는 false
- p1-p2, p2-p3 비교시에는 색상을 무시했지만, p1-p3 비교는 색상까지 고려했기에
- `구체클래스르 확장해 새로운 값을 추가하면서 equals 규약을 만족 시킬 방법은 존재하지 않는다.`
    - equals 안의 instanceof 검사를 getClass 검사로 바꾸면 가능한가?
    ```
    @Override
    public boolean equals(Object o) {
        if(o==null || o.getClass() != getClass())
            return false;
        Point p=(Point)o;
        return p.x==x&&p.y==y;
    }
    ```
    - 위 코드는 같은 구현 클래스의 객체와 비교할 때만 true를 반환한다.
    - 실제로 활용은 불가능. Point의 하위 클래스는 Point로 활용되어야 하는데 이 방식에서는 불가능하다. = 리스코프 치환 원칙 위반
    ```
    public class CounterPoint extends Point{
        private static final AtomicInteger counter=new AtomicInteger();

        public CounterPoint(int x, int y){
            super(x,y);
            counter.incrementAndGet();
        }
        public static int numberCreated(){
            return counter.get();
        }
    }
    ```
    - CounterPoint의 인스턴스는 어떤 Point와도 같을 수 없다.
    - Point의 equals를 instanceof 기반으로 구현했다면 제대로 동작할 것
- 하지만 상속 대신 컴포지션을 사용하라(아이템 18)을 따르면 된다.
    - Point를 상속하는 대신 Point를 ColorPoint의 private 메서드로 두고, ColorPoint와 같은 위치의 일반 Point를 반환하는 뷰(view) 메서드(아이템 6)를 public으로 추가하는 식
    ```
    public class ColorPoint{
        private final Point point;
        private final Color color;

        public ColorPoint(int x, int y, Color color){
            point=new Point(x, y);
            this.color=Object.requireNonNull(color);
        }
        //나머지 코드 생략

        public Point asPonit(){ //ColorPoint의 point 뷰를 반환
            return point;
        }

        @Override
        public boolean equals(Object o) {
            if(!(o instanceof ColorPoint))
                return false;
            ColorPoint cp=(ColorPoint) o;
            return cp.point.equals(point)&&cp.color.equals(color);
        }
    }
    ```

## 일관성
- 두 객체가 같다면(어느 하나 혹은 두 객체 모두가 수정되지 않는 한) 앞으로도 영원히 같아야 한다는 뜻
- 가변 객체는 비교 시점에 따라 서로 다를 수도, 같은 수도 있는 반면 불변 객체는 한 번 다르면 끝까지 달라야 한다
    - 클래스를 불변 클래스로 만들기로 했다면 equals가 한 번 같다고 한 객체는 영원히 같다고 해야 한다
- 클래스가 불변이든 가변이든 equals의 판단에 신뢰할 수 없는 자원이 끼어들어서는 안 된다.
- equals는 항시 `메모리`에 존재하는 객체만을 사용한 `결정적(deterministic)`계산만 수행해야 한다.

## null-아님
- 모든 객체가 null과 같지 않아야 한다는 뜻
- 동치성을 검사하면 equals는 건네받은 객체를 적절히 형변환한 후 필수 필드의 값을 알아내야 한다. 그러려면 형 변환 전 instanceof 연산자로 입력 매개변수가 올바를 타입인지 검사해야 한다.
```
    //명시적 null 검사 - 필요없다
    @Override
    public boolean equals(Object o) {
        if(o==null) return false;
    }

    //묵시적 null 검사 - 이쪽이 좋음
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof MyType)) return false;
        MyType my=(MyType)o;
    }
```
- equals가 타입 확인하지 않으면
    - 잘못된 타입이 인수로 주어질 때 ClassCastException을 던져 일반 규약을 위배
- instanceof는 첫 번째 피연산자가 null이면 false를 반환 = 입력이 null이면 타입 확인 단계에서 false를 반환 = null 검사를 명시적으로 하지 않아도 된다.
<br><br><br>
## 양질의 equals 메서드 구현 방법 : 단계별
1. == 연산자를 사용해 입력이 자기 자신의 참조인지 확인
    - 자기 자신이면 true를 반환. 단순한 성능 최적화 용도
2. instanceof 연산자로 입력이 올바른 타입인지 확인
    - 올바른 타입은 equals가 정의된 클래스인 것이 보통이지만 가끔은 그 클래스가 구현한 특정 인터페이스가 될 수도 있음
    - 자신을 구현한 (서로 다른)클래스 끼리도 비교할 수 있도록 equals 규약을 수정하기도 함
        - 이런 인터페이스를 구현한 클래스 = 해당 인터페이스를 사용해야 함
        - e.g., Set, List, Map, Map.Entry
3. 입력을 올바른 타입으로 현변환
    - 2번에서 instanceof 검사를 했기에 이 단계는 100% 성공
4. 입력 객체와 자기 자신의 대응되는 핵심 필드들이 모두 일치하는지 하나씩 검사
    - 모든 필드가 일치하면 true, 하나라도 다르다면 false를 반환
    - 2단계에서 인터페이스 사용시 입력의 필드 값을 가져올 때도 그 인터페이스의 메서드를 사용해야 함
    - 타입이 클래스라면 해당 필드에 직접 접근할 수도 있음
<br><br>
---
<br>
- 기본 타입 필드 : == 연산자로 비교
- 참조 타입 필드 : 각각의 equals 메서드로 비교
- float, double : 각각 정적 메서드인 Float.compare(float, float)와 Double.compare(double, double)로 비교
    - 부동소수 값을 다뤄야 하기 때문에 기본 타입과 별도로 취급
    - Float.equals와 Double.equals는 오토 박싱을 수반할 수 있으니 성능상 좋지 않음
- 배열 : 원소 각각을 앞선 지침으로 비교
    - 배열의 모든 원소가 핵심 필드 : Arrays.equals 메서드들 중 하나를 사용
- null도 정상 값으로 취급하는 참조 타입 필드 : Objects.equals(Object, Object)로 비교해 NullPointerException 발생을 예방
- 비교하기가 복잡한 플드를 가진 클래스 : 필드의 표준형(canonical form)을 저장해둔 후 표준형끼리 비교
    - 불변 클래스에 제격. 가변 객체라면 값이 바뀔 때 마다 표준형을 갱신해야 함
<br><br>

---
<br>

- 다를 가능성이 더 크거나 비교하는 비용이 싼 필드를 번저 비교 하는 것이 성능에 좋음
- 객체의 논리적 상태와 관련 없는 필드는 비교하면 안 된다.
- 파생 필드를 비교 할 필요는 없지만, 비교하는 쪽이 더 빠를 때도 있다.
    - 파생 필드가 객체 전체의 상태를 대표하는 상황의 경우

## 주의사항
- equals를 재정의 할 땐 hashCode도 반드시 재정의하자(아이템 11)
- 너무 복잡하게 해결하려 들지 말자
    - 필드들의 동치성만 검사해도 equals 규약을 어렵지 않게 지킬 수 있음
    - 별칭(alias)은 비교하지 않는 게 좋음
- Object 외의 타입을 매개변수로 받는 equals 메서드는 선언하지 말자
    - 입력 타입이 Object가 아니면 재정의가 아니라 다중정의 한 게 된다. 
    - @Override 애노테이션을 사용하면 이러한 실수를 예방할 수 있다.

- 구글의 AutoValue 프레임워크를 사용하면 equals, hashCode 등을 대신 작성해준다