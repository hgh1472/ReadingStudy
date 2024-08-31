# Item 8. finalizer와 cleaner 사용을 피하라

- 자바는 두 가지의 객체 소멸자를 제공. 그 중 `finalizer`는 `일반적으로 불필요`
    - 예측할 수 없고, 상황에 따라 위험할 수 있기때문
    - 오동작, 낮은 성능, 이식성 문제의 원인이 되기도 함
    - 자바 9에서는 finalizer를 사용 자제(deprecated) API로 지정하고 cleaner를 그 대안으로 소개
        - 하지만 `cleaner`도 finalizer보다는 덜 위험하지만 여전히 예측할 수 없고, 느리고, `일반적으로 불필요`
<br><br>
## vs C++의 파괴자(destructor)
- 특정 객체와 관련된 자원을 회수하는 보편적인 방법
    - 자바에서는 접근할 수 없게 된 객체를 회수하는 역할을 가비지컬렉터가 담당

- 비메모리 자원을 회수하는 용도로도 쓰임
    - 자바에서는 try-with-resources와 try-finally를 사용해 해결

<br><br>
## 즉시수행된다는 보장이 없다
- 실행되기가지 얼마나 걸릴지 알 수 없다
    - 즉 `finalizer와 cleaner로는 제때 실행되어야 하는 작업은 절대 할 수 없다`

- e.g., 파일 닫기는 시스템이 동시에 열 수 있는 파일 개수에 한계가 있기에 finalizer와 cleaner 실행을 게을리해 파일을 계속 열어두면 새로운 파일을 열지 못해 프로그램이 실패할 수 있다.
- 가비지 컬렉터 구현마다 finalizer와 cleaner를 얼마나 신속하게 수행할지가 다름

- 클래스에 finlaizer를 달아두면 그 인스턴스의 자원 회수가 제멋대로 지연될 수 있다.
- finalizer 스레드는 다른 애플리케이션 스레드보다 우선순위가 낮아서 실행 될 기회를 제대로 얻지 못한다.
- 이 문제를 해결하는 방법은 자바 언어 명세는 어떤 스레드가 finalizer를 수행할지 명시하지 않으니 finalizer를 사용하지 않는 방법뿐이다.
<br><br>

### cleaner
- 자신을 수행할 스레드를 제어할 수 있다는 면에서 조금 나음
- 하지만 여전히 백그라운드에서 수행되며 가비지 컬렉터의 통제하에 있으니 즉각 수행되리라는 보장은 없다.
<br><br>

## 수행 여부
- 수행 시점뿐만 아니라 수행 여부조차 보장하지 않는다
    - 종료 작업을 수행하지 못한채 프로그램을 중단시킬수도 있다.
- 프로그램 생애 주기와 상관 없는, 상태를 영구적으로 수정하는 작업에서는 절대 finalize나 cleaner에 의존해서는 안된다.
<br>

## System.gc, System.runFinalization 메서드
- finalizer와 cleaner가 실행될 가능성을 높여줄 수는 있으나, 보장해주지는 않는다.
- System.runFinalizersOnExi와 Runtime.runFinalizaersOnExit는 보장해주지만 심각한 결함이 있어 수십 년간 지탄받아왔다.(Thread Stop)
<br><br>

## 예외
- 발생한 예외는 무시되며, 처리 할 작업이 남았더라도 그 순간 종료된다
- 잡지 못한 예외때문에 해당 객체는 자칫 마무리가 덜 된 상태로 남을 수 있다
- 다른 스레드가 이처럼 훼손된 객체를 사용하려 한다면 어떻게 동작할지 예측 불가
- 보통의 경우는 잡지 못한 예외가 스레드 중단, 스택 추적 내역을 출력
    - finalizer는 경고조차 출력하지 않음
    - cleaner를 사용하는 라이브러리는 자신의 스레드를 통제하기 때문에 이러한 문제가 발생하지는 않음

## 성능
- 간단한 AutoCloseable 객체를 생성하고 가비지 컬렉터가 수거 : `12ns` 걸림
    - try-with-resourses로 자신을 닫도록 함
- finalizer를 사용 : `550ns` 걸림
- 즉, finalizer를 사용한 객체를 생성하고 파괴하니 50배의 성능 저하
    - finalizer가 가비지 컬렉터의 효율을 떨어뜨리기 때문
    - cleaner도 클래스의 모든 이느턴스를 수거하는 형태로 사용하면 성능은 finalizer와 비슷 : 인스턴스당 500ns정도
        - 안전망 형태로만 사용하면 훨씬 빨라짐 : 객체 하나를 생성, 정리, 파괴하는 데 66ns가 걸림 = 안전망을 설치하는 대가로 성능이 약 5배 정도 느려짐


## finalizer공격
- finalizer를 사용한 클래스는 fianlizer 공격에 노출되어 심각한 보안 문제를 일으킬 수도 있다
- 공격 원리 : 생성자나 직렬화 과정에서 예외 발생시, 이 생성되다 만 객체에서의 악의적인 하위 클래스의 fianlizer가 수행될 수 있게 된다. 
    - 이 finalizer는 정적 필드에 자신의 참조를 할당해 가비지 컬렉터가 수집하지 못하게 막을 수 있다.
    - 이렇게 일그러진 객체가 만들어지면 이 객체의 메서드를 호출해 애초에는 허용되지 않았을 작업을 수행하는것은 쉽다.
- 객체 생성을 막으려면 생성자에서 예외를 던지는 것만으로 충분하지만, finalizer가 있다면 그렇지도 않다.
    - final 클래스들은 하위 클래스를 만들 수 없으니 이 공격에서 안전하다 => 아무 일도 하지 않는 finalize 메서드를 만들고 final로 선언


## AutoCloseable
- 종료해야 할 자원을 담고 있는 객체의 클래스에서 finalizer나 cleaner를 대신해줄 묘안
- AutoCloseable을 구현하고 인스턴스를 다 쓰고 나면 close 메서드를 호출하면 된다.
    - try-with-resources를 사용해 예외가 발생해도 제대로 종료되도록 하자
- 각 인스턴스는 자신이 닫혔는지를 추적하는 것이 좋다
    - close 메서드에서 이 객체는 더 이상 유효하지 않음을 필드에 기록하고, 다른 메서드는 이 필드를 검사해 객체가 닫힌 후에 불렸다면 IllegalStateException을 던지는 것

## cleaner와 finalizer의 쓰임새
1. 자원의 소유자가 close 메서드를 호출하지 않는 것에 대비한 `안전망` 역할
    - 즉시 호출되리란 보장은 없지만, 하지 않는 것보다는 늦게라도 해주는 것이 낫다.
    - 자바 라이브러리의 일부 클래스는 안전망 역할의 finalizer를 제공 : FileInputStream, FileOutputStream, ThreadPoolExecutor
2. 네이티브 피어와 연결된 객체에서 활용
    - 네이티브 피어 : 일반 자바 객체가 네이티브 메서드를 통해 기능을 위임한 네이티브 객체.
        - 자바 객체가 아니니 가비지 컬렉터는 그 존재를 알지 못함
        - 자바 피어를 회수할 때 네이티브 객체까지 회수하지 못함
    - cleaner나 finalizer가 나서서 처리하기 적당한 작업
    - 단, 성능 저하를 감당할 수 있고 네이티브 피어가 심각한 자원을 가지고 있지 않을 때에만 해당됨 -> 아니라면 close 메서드 사용
    <br><br>

```
public class implements Autocloseable{
    private static final Cleaner cleaner=Cleaner.create();

    private static class State implements Runnable{
        int numJunkPiles;

        State(int numJunkPiles){
            this.numJunkPiles=numJunkPiles;
        }

        @Override public void run(){
            System.out.println("방 청소");
            numJumkPiles=0;
        }
    }

    private final State state;
    private final Cleaner.Cleanable cleanable;

    public Room(int numJunkPiles){
        state=new State(numJunkPiles);
        cleanable=cleaner.register(this,state);
    }

    @Override public void close(){
        cleanalbe.clean();
    }
}
```
- State는 cleaner가 방을 청소할 때 수거할 자원들을 담고 있다. = numJunkPiles vlfem
- State는 Runnable를 구현하고, 그 안의 run 메서드는 cleanable에 의해 딱 한 번만 호출됨
- run 메서드가 호출되는 상황
    - Room의 close 메서드 호출할 때
    - 가비지 컬렉터가 Room을 회수할 때 까지 클라이언트가 close를 호출하지 않으면 cleaner가 State의 run 메서드를 호출해줄 것.
- State 메서드는 절대로 Room 인스턴스를 참조하면 안된다
    - 순환참조가 생겨 가비지 컬렉터가 Room 인스턴스를 회수해갈 기회가 오지 않음
    - State가 정적 중첩 클래스인 이유 : 정적이 아닌 중첩 클래스는 자동으로 바깥 객체의 참조를 갖게 되기 때문

- 이 코드에서 Room의 cleaner는 단지 안전망으로만 쓰임.
---

```
public class Adult{
    public static void main(String[] args){
        try(Room room=new Room(7)){
            System.out.println("잘 짜여진 코드");
        }
    }
}
```
- 잘 짜여진 코드의 예이다. Room 생성을 try-with-resources 블럭으로 감싸 자동 청소가 필요하지 않다.
- 잘 짜여진 코드를 출력 후 방 청소를 출력한다. <br><br>

```
public class Teenager{
    public static void main(String[] args){
        new Room(99);
        System.out.println("잘 짜지 않은 코드");
    }
}
```
- 방 청소는 한 번도 출력되지 않는다.
- Teenager의 main 메서드에 System.gc()를 추가해서 "방 청소"를 출력할 수도 있지만 출력하지 못할 수도 있다.

<br><br><br>
## 정리
- cleaner(finalizer)는 안전망 역할이나 중요하지 않은 네이티브 자원 회수용으로만 사용하자. 물론 이런 경우라도 불확실성과 성능 저하에 주의해야 한다.