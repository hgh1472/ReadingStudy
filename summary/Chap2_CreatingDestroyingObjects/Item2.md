# [ITEM 2] 생성자에 매개변수가 많다면 빌더를 고려하라

정적 팩터리와 생성자는 선택적 매개변수가 많을 때 적절히 대응하기 어렵다. 만약 필수 필드와 선택적 필드가 같이 존재하는 경우는 어떨까?

## 점층적 생성자 패턴

필수 매개변수만 받는 생성자, 필수 매개변수와 선택 매개변수 1개를 받는 생성자, 선택 매개변수 2개까지 받는 생성자 등등 선택 매개변수를 전부 다 받는 생성자까지 늘려가는 방식이다.

```java
public class NutritionFacts {
		private final int servingSize; // 필수
		private final int servings; // 필수
		private final int calories; // 선택
		private final int fat; // 선택
		private final int sodium; // 선택
		private final int carbohydrate; // 선택
		
		public NutritionFacts(int servingSize, int servings) {
				this(servingSize, servings, 0);
		}
		
		public NutritionFacts(int servingSize, int servings, int calories) {
				this(servingSize, servings, calories, 0);
		}
		
		public NutritionFacts(int servingSize, int servings, int calories, int fat) {
				this(servingSize, servings, calories, fat, 0);
		}
		
		public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium) {
				this(servingSize, servings, calories, fat, sodium, 0);
		}
		
		public NutritionFacts(int servingSize, int servings, int calories, int fat, int sodium, int carbohydarte) {
				this.servingSize = servingSize;
				this.servings = servings;
				this.calories = calories;
				this.fat = fat;
				this.sodium = sodium;
				this.carbohydrate = carbohydrate;
		}
}
```

이 클래스의 인스턴스를 만들려면 원하는 매개변수를 모두 포함한 생성자 중 가장 짧은 것을 골라 호출하면 된다. **그런데 매개변수 개수가 많아지면 클라이언트 코드를 작성하거나 읽기 어렵다.**

## 자바빈즈 패턴

매개변수가 없는 생성자로 객체를 만든 후, 세터 메서드를 호출해 원하는 매개변수의 값을 설정하는 방식이다.

```java
public class NutritionFacts {
		// 매개변수들은 기본값으로 초기화
		private int servingSize = -1;
		private int servings = -1;
		private int calories = 0;
		private int fat = 0;
		private int sodium = 0;
		private int carbohydrate = 0;
		
		public NutritionFacts() {}
		public void setServingSize(int val) { servingSize = val; }
		public void setServings(int val) { servings = val; }
		public void setCalories(int val) { calories = val; }
		public void setFat(int val) { fat = val; }
		public void setSodium(int val) { sodium = val; }
		public void setCarbohydrate(int val) { carbohydrate = val; }
}

/**
*  NutritionFacts cocaCola = new NutritionFacts();
*  cocaCola.setServingSize(240);
*  ...
*/
```

자비빈즈는 단점이 있는데 **자비빈즈 패턴에서는 객체 하나를 만들려면 메서드 여러 개를 호출해야 하고, 객체가 완전히 생성되기 전까지는 일관성이 무너진 상태에 놓이게 된다.** 점층적 생성자 패턴에서는 매개변수들이 유효한지를 생성자에서만 확인하면 일관성을 유지할 수 있는데, 그 장치가 사라진 것이다. 결국 자바빈즈 패턴에서는 클래스를 불변으로 만들 수 없다.

## 빌더 패턴

점층적 생성자 패턴의 안정성과 자바빈즈 패턴의 가독성을 겸비한 패턴이다. 클라이언트는 필요한 객체를 직접 만드는 대신, 필수 매개변수만으로 생성자를 호출해 빌더 객체를 얻는다. 그런 다음 빌더 객체가 제공하는 일종의 세터 메서드들로 원하는 선택 매개변수들을 설정한다. 마지막으로 매개변수가 없는 build 메서드를 호출해 필요한 객체를 얻는다.

```java
public class NutritionFacts { 
    private final int servingSize; // 필수
    private final int servings; // 필수
    private final int calories; // 선택
    private final int fat; // 선택
    private final int sodium; // 선택
    private final int carbohydrate; // 선택
    
    public static class Builder {
        //필수 매개 변수
        private final int servingSize;
        private final int servings;
        
        // 선택 매개변수 : 기본값으로 초기화
        private int calories = 0;
        private int fat = 0;
        private sodium = 0;
        private int carbohydrate = 0;
        
        public Builder(int servingSize, int servings) {
            this.servingSize = servingSize;
            this.servings = servings;
        }
        
        public Builder calories(int val) {
            calories = val;
            return this;
        }
        
        public Builder fat(int val) {
            fat = val;
            return this;
        }
        
        public Builder sodium(int val) {
            sodium = val;
            return this;
        }
        
        public Builder carbohydrate(int val) {
            carbohydrate = val;
            return this;
        }
        
        public NutritionFacts build() {
           return new NutritrionFacts(this);
        }
    }
    
    private NutritionFacts(Builder builder) {
        servingSize = builder.servingSize;
        servings = builder.servings;
        calories = builder.caloires;
        fat = builder.fat;
        sodium = builder.sodium;
        carbohydrate = builder.carbohydrate;
    }
}
```

`NutritionFacts` 클래스는 불변이고, 모든 매개변수의 기본값을 한 곳에 모아뒀다. 빌더의 세터 메서드들은 빌더 자신을 반환하기 때문에 연쇄적으로 호출할 수 있다.

```java
NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
                                .calories(100)
                                .sodium(35)
                                .carbohydrate(27)
                                .build();
```

이 클라이언트 코드는 쓰기 쉽고, 무엇보다 읽기 쉽다.

빌더 패턴은 계층적으로 설계된 클래스와 함께 쓰기에 좋다. 각 계층의 클래스에 관련 빌더를 멤버로 정의하고 추상 클래스는 추상 빌더를, 구체 클래스는 구체 빌더를 갖게 한다.

```java
public abstract class Pizza {
    public enum Topping {
        HAM, MUSHROOM, ONION, PEPPER, SAUSAGE
    }
    final Set<Topping> toppings;
    
    abstract static class Builder<T extends Builder<T>> {
        EnumSet<Topping> toppings = EnumSet.noneOf(Topping.class);
        public T addTopping(Topping topping) {
            toppings.add(Objects.requireNonNull(topping));
            return self();
        }
    
        abstract Pizza build();
    
        protected abstract T self();
    }
    Pizza(Builder<?> builder) {
        toppings = builder.toppings.clone();
    }
}

public class NyPizza extends Pizza {
    @Override
    public String toString() {
        return "NyPizza{" +
            "size=" + size +
            ", toppings=" + toppings +
            '}';
    }
    
    public enum Size {
        SMALL, MEDIUM, LARGE
    }
    private final Size size;
    
    public static class Builder extends Pizza.Builder<Builder> {
        private final Size size;
    
        public Builder(Size size) {
            this.size = Objects.requireNonNull(size);
        }
    
        @Override
        Pizza build() {
            return new NyPizza(this);
        }
    
        @Override
        protected Builder self() {
            return this;
        }
    }
    private NyPizza(Builder builder) {
        super(builder);
        size = builder.size;
    }
}

class PizzaTest {
    @Test
    public void makePizzaTest() {
        Pizza pizza = new NyPizza.Builder(SMALL)
            .addTopping(Pizza.Topping.SAUSAGE)
            .addTopping(Pizza.Topping.ONION)
            .build();
        System.out.println(pizza);
    }
}
```

`Pizza.Builder` 클래스는 추상 메서드 self를 통해 하위 클래스에서 형 변환을 하지 않고도 메서드 연쇄가 가능하다. Pizza의 하위 클래스 `NyPizza`는 크기 매개변수를 필수로 받는다.

`NyPizza` 의 build 메서드는 구체 하위 클래스를 반환한다. 상위 클래스의 메서드가 정의한 반환 타입이 아닌, 하위 타입을 반환함으로써 클라이언트가 형 변환에 신경쓰지 않고 빌더를 이용할 수 있다.

빌더 패턴을 만들려면 우선 빌더부터 만들어야 한다. 빌더 생성 비용이 크진 않지만 성능에 민감한 상황에서는 문제가 될 수 있고 코드가 장황해서 매개변수 4개 이상은 되어야 값어치를 한다. 하지만 API는 시간이 지날수록 확장되는 경향이 있으므로 주의해야 한다.

---
**Git Issue**

🔨 빌더와 다른 패턴과의 차이점

> 빌더의 형식이 데코레이터 패턴과 유사한데 이 둘의 차이점과 각각 어떤 상황에 사용을 해야 하나
>

기존에 데코레이터 패턴에 대해서 잘 몰라서 추가적으로 한 번 찾아봤습니다!

제가 찾아보며 생각했을 때 우선 데코레이터 패턴은 기존 객체에 대해 기능의 확장이나 변경이 필요할 때 상속하는 하위 클래스 대신 사용할 수 있는 패턴이라고 생각합니다. 예를 들어 하나의 객체 A가 존재한다고 할 때, 이 객체의 기능이 확장될 수 있습니다. 이 때 확장된 기능이 X, Y, Z라고 해보겠습니다. 만약 확장된 기능에 따른 하위 클래스를 만든다고 한다면, 우리는 'A+X', 'A+Y', 'A+Z', A+X+Y' ... 와 같이 만들어야 하는 상황이 올 수 있습니다.

이 때 X, Y, Z만 구현하고, 원본 클래스에 단순히 넣어주는 방식으로 사용합니다.

```
new Z(new X(new A()));
```

위처럼 원본 객체 생성자(`new A()`)를 장식자 생성자(`new Z()`, `new X()`)가 래핑하는 형태로 진행됩니다.

반면 빌더의 경우 생성자에서 매개변수가 많을 때 이점을 얻기 위해 사용하는 패턴입니다. 즉, 기능의 확장이 아닌 생성자에서 이점을 얻기 위해 사용하는 패턴이라고 생각됩니다.

> 마찬가지로 빌더와 팩토리 패턴의 차이점, 그리고 핵심 정리 부분에서 말하는 '매개변수가 많다면'이 통상적으로 어느 정도 인가요?
>

다음으로 팩토리 패턴과 빌더 패턴은 결국 둘 다 객체의 생성과 관련한 것이지만, 팩토리 패턴은 싱글톤이나 매개변수에 자유롭게 생성할 수 있다는 것에 장점이 있습니다. 예를 들어 조회한 Entity를 DTO로 변환할 경우, 매개변수를 Entity 그대로 넣어 DTO로 변환할 수 있습니다.

대신 빌더 패턴은 매개변수에서 비록 자유롭지 못하지만, 인자가 여러 개가 되더라도 작성하고 읽기에 매우 편리하다는 것에 장점이 있는 것 같습니다.

빌더 패턴을 사용하면 코드가 장황해서 약 매개변수가 4개 이상은 되어야 이점이 있다고 합니다. 하지만 API는 갈수록 확장되는 경향이 있기 때문에 무조건적으로 4개 이상부터 사용할 필요는 없을 것 같습니다. 그리고 Spring에서 Lombok을 사용할 경우, 손쉽게 Builder 패턴을 적용할 수 있기 때문에 Builder 패턴은 사용하는 것도 좋을 것 같습니다!

---
