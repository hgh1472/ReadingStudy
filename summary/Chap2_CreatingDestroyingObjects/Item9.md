# Item 9. try-finally 보다는 try-with-resouces를 사용하라

## try-finally
- 전통적으로 쓰였었음
```
static String firstLineOfFiles(String path) throws IOException{
    BufferedReader br=new BufferedReader(new FileReader(path));
    try{
        return br.readLine();
    }finally{
        br.close();
    }
}
```
- 자원을 하나만 사용할 때의 코드. 나쁘지 않다.<br><br>
```
static String firstLineOfFiles(String src, String dst) throws IOException{
    InputStream in=new FileInputStream(src);
    try{
        OutputStream out-new FileOutputStream(dst);
        try{
            byre[] buf=new Byte[BUFFER_SIZE];
            int n;
            while((n=in.read(buf))>=0)
                out.write(buf,0,n);
        }finally{
            out.close();
        }
    }finally{
        in.close();
    }
}
```
- 자원을 두 개 이상 사용할 때의 코드. 지저분하다.
<br><br>
- 예외는 try 블록과 finally 블록 모두에서 발생할 수 있다.
    - 기기에 물리적인 문제 발생시 readLine 메서드가 예외를 던지고, 같은 이유로 close도 실패할 것.
    - 두 번째 예외가 첫 번째 예외를 완전히 집어삼켜 버린다.
    - 스택 추적 내역에 첫 번째 예외에 관한 정보는 남지 않게 되어 디버깅을 어렵게 만든다.


## try-with-resources
- 이 구조를 사용하려면 해당 자원이 AutoCloseable 인터페이스를 구현해야 한다.
    - 단순히 void를 반환하는 close 메서드 하나만 정의한 인터페이스이다.

```
static String firstLineOfFiles(String path) throws IOException{
    try(BufferedReader br=new BufferedReader(
        new FileReader(path)
    )){
        return br.readLine();
    }
}
```
```
static void copy(String src, String dst) throws IOException{
    try(InputStream in=new FileInputStream(src);
        OutputStream out=new OutputStream(dst)){
            byte[] buf=new byte[BUFFER_SIZE];
            int n;
            while((n=in.read(buf))>=0)
                out.write(buf,0,n);
        }
}
```
- try-with-resources 구조를 사용해 위 두개의 코드를 재작성.

- fileLineOfFile 메서드
    - readLine과 close 호출 양쪽에서 예외 발생시 close에서 발생한 예외는 숨겨지고 readLine에서 발생한 예외가 기록됨

- try-with-resources에서도 catch절 사용 가능
    - catch절 덕분에 try문 중첩 없이 다수의 예외 처리 가능
```
static String firstLineOfFiles(String path, String defaultVal){
    try(BufferedReader br=new BufferedReader(
            new FileReader(path)
    )){
        return br.readLine();
    }catch(IOException e){
        return defaultVal;
    }
}
```
- catch문을 이용해 예외를 던지는 대신 기본값을 반환하도록 한 코드.