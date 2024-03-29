# UOS Java Class Viewer

|    학번    |     학과     |  이름  |       학기       |           강의           |
| :--------: | :----------: | :----: | :--------------: | :----------------------: |
| 2019920037 | 컴퓨터과학부 | 이성호 | 2020학년도 1학기 | 객체지향프로그래밍및실습 |

[TOC]

## 개발 개요

![image-20200615065304894](./img/image-20200615065304894.png)

* Java로 간단한 Java Class Viewer를 개발함
* 원래는 아래의 `MyStack.java` 파일만 파싱할 수 있으면 되지만, 다른 class 파일도 파싱할 수 있는 방향으로 개발함

```java
class MyStack {
    private int size;
    private int top;
    private int[] data;
    
    public MyStack(int size) {
        data = new int[size];
        this.size = size;
        top = 0;
    }
    
    public void push(int v) {
        if(!isFull()) data[top++] = v;
        else
            System.out.println("Stack is full");
    }
    
    public int pop() {
        if(!isEmpty()) return data[--top];
        else
            return -1;
    }
    
    public boolean isFull() {
        if(top >= size) return true;
        else
            return false;
    }
    
    public boolean isEmpty() {
        if(top == 0) return true;
        else
            return false;
    }
}
```



## 요구 사항

1. Java 소스 파일(.java)을 파싱
2. 화면 왼쪽에 Tree 그리고 오른쪽에 텍스트 영역과 Table을 배치
3.  "File" 메뉴에 "Open", "Exit" 메뉴 아이템 추가하고 "Exit"를 선택하면 종료
4. Tree에는 Class, Method, Field가 계층적으로 위치: Tree Model 사용
5. Class 이름을 더블 클릭하면 Method와 Field를 Tree에 표시하고, 모든 Member(Method, Field)들을 Table에 표시
   - 테이블에는 Name, Type, Access 열이 위치: Table Model 사용

6. Method를 클릭하면 소스 코드를 텍스트 영역에 표시
   * 왼쪽 아래 영역에 이 Method가 사용하는 Field들을 표시(텍스트 또는 table model 사용)

7. Field를 클릭하면 Field를 사용하는 Method들을 표시: Table Model 사용



## 개발 방법

### class 파일 파싱

class 파일을 파싱하기 위해 총 4단계의 과정으로 나누었다. **첫 번째**는 class 파일의 전체적인 구조를 분석하는 과정이다. class는 이름, 메서드(method), 자료(field) 이 3가지의 정보를 나타낸다. 따라서 어느 부분이 class의 이름인지, 어디서 부터 어디까지 메서드 구간이고 자료 구간인지 파악하고 이들을 분리하는 과정을 수행한다. **두 번째**는 각각 분리된 메서드들을 파싱하여 메서드의 이름이 무엇인지, 메서드의 매개 변수의 타입(type)은 무엇인지 파악하는 과정을 수행한다. **세 번째**는 마찬가지로 각각 분리된 자료들을 파싱하여 필요한 정보를 추출하는 과정이다. **네 번째**는 파악된 class의 메서드와 자료 간의 참조 관계를 파악하는 과정이다.



#### 첫 번째 단계: Class의 전체적인 구조 분석

간단한 Java Class는 다음의 구조로 나타낼 수 있다.

```
(Access Modifier) (Class Name) {
    (Access Modifier) (Type) (Field Name) = ...;
	(Type) (Field Name);
    ...
    
    (Access Modifier) (Type) (Constructor Name) (Arguments) {
    	...
    }
    
    (Access Modifier) (Type) (Method Name)() {
    	...
    }
    
    (Type) (Method Name) (Arguments) {
    	{
    		...
    	}
    }
}
```

* 여기서 Class나 Method 그리고 Field의 구간을 Block이라고 하자. 첫 번째 단계에서는 Block을 파악하기만 하고 Block 안에 있는 내용은 나머지 단계에서 처리하도록 한다.

* Block의 시작과 끝을 저장할 수 있는 Stack을 만들어 Block을 파악할 수 있도록 한다. 그러면 Block의 시작 기호가 나타났을 때(예를 들면 `{`) Stack에 시작 위치를 push하고 Block의 끝 기호가 나타났을 때(예를 들면 `}`) Stack에서 시작 위치를 pop하여 Block을 구할 수 있게 된다.
  - Block을 파악하는 이유는 정확한 Member 위치를 파악하기 위해서이다. 또한 `StringTokenizer` 클래스를 통해 생성한 토큰으로만 Member를 파악하기에 한계가 있었다(Field Member에서 Field의 이름과 `;`이 붙어있는 경우가 있었기에, `;`가 token으로 분리되지 않아 Field Member를 파악하는데 어려움이 있었다).
  - Method Block 안에 있는 Sub Block들은 우리의 관심사가 아니므로 무시한다. 다만 나중에 Method와 Field간에 참조 관계를 분석하기 위해 저장해둔다.
* 첫 `{`가 나타났을 때는 `{`이전의 문자열들은 클래스 정보를 나타내므로 클래스의 이름을 구한다.
* `ClassInfo` Class를 만들어 클래스의 이름과 어떤 Method와 Field를 가지고 있는지에 대한 정보를 저장한다.



#### 두 번째 단계: Method 내용 분석

주어진 Method Block은 `(Access Modifier) (Method Name) (Arguments) { ... }`의 형태를 가진다. 예를 들면 다음과 같다.

```java
    public void push(int v) {
        if(!isFull()) data[top++] = v;
        else
            System.out.println("Stack is full");
    }
```

먼저 Method의 Access Modifier, Method Name을 파악하기 위해 `(`가 시작되는 위치를 파악한 후 `(` 이전, 이후의 문자열로 나눈다. 그 다음 `(` 이전의 문자열을 `StringTokenizer`를 이용해 각 예약어들을 토큰화하고 Access Modifier을 찾는다. 만약 없다면 `default` Access Modifier를 부여한다. 그리고 `(` 바로 이전의 토큰은 Method의 이름이다. 한편, `(` 부터 `)` 까지의 문자열을 토큰화한 후 Argument들의 Type을 검사한다. 이때 Argument의 이름은 우리의 관심사가 아니기 때문에 파악하지 않는다. Method의 내용을 모두 분석했다면 Method 정보를 담는 Class에 분석한 내용을 저장한다(여기선 `MethodInfo` Class).



#### 세 번째 단계: Field 내용 분석

주어진 Field Block은 `(Access Modifier) (Type) (Field Name) = ...;`의 형태를 가진다. 위의 Method 내용 분석과 비슷하게 `=` 이전의 문자열을 추출하여 토큰화해서 Access Modifier, Type, 이름을 찾는다. 여기서 주의할 점이 `=`의 여부인데, `=`이 없는 Field Block을 잘 처리해야 한다. Field의 내용을 모두 분석했다면 Field 정보를 담는 Class에 분석한 내용을 저장한다(여기선 `FieldInfo` Class).



#### 네 번째 단계: Method와 Field 간의 참조 관계 분석

Method와 Field 정보를 저장하는 class에 참조 정보를 `ArrayList`로 보관한다. Method가 Field를 참조하기 때문에 Method 입장에서 Field 참조 정보를 파악한다. Method Block의 `{ ... }` 부분을 `StringTokenizer`를 이용해 토큰화한다. 그리고 각 토큰들의 문자열과 이 Class가 가지고 있는 Field의 이름을 비교해서 서로 같다면 서로의 정보를 `ArrayList`에 보관한다. 다시 말해, Method A가 Field B를 참조하고 있으면 A의 정보를 담는 Class에 B를 저장하고, B의 정보를 담는 Class에도 A의 정보를 저장한다. 이때 주의할 점이 `" ... "` 처럼 문자열 안에 Field 이름이 들어 있는 경우이다. 이는 참조 정보를 잘못 파악할 수 있기 때문에 Method Block 안에 있는 문자열 정보를 모두 삭제하는 과정을 참조 관계 분석 전에 수행한다.



### GUI 구현

#### 기본 Layout 구성

![image-20200615030014031](./img/image-20200615030014031.png)

* `JMenuBar`, `JMenu`, `JMenuItem`을 이용해서 File - Open, File - Exit 메뉴를 구현함.
* `JSplitPane`을 이용해서 왼쪽에 `JTree`와 `JTextArea`를 `JSplitPane`을 이용해서 가로 방향으로 배치하고 오른쪽에 `JTable`과 `JTextArea`를 번갈아 표시할 수 있도록 `JPanel`을 배치함.

![image-20200615030222464](./img/image-20200615030222464.png)

* File - Open 메뉴를 통해 class 파일을 열게 되면 왼쪽 `JTree`에 Class의 이름을 부모 노드로 하고 Member의 이름을 자식 노드로 하는 Tree를 생성함.
  * 파일을 열기 위한 Dialog로는 `FileDialog` Class를 이용함.
  * 파싱된 정보를 담고 있는 Class(여기선 `ClassInfo`)로부터  `TreeModel`을 구현함. 그리고 `TreeModel`의 모델을 `JTree`의 모델로 설정해서 Tree를 표시함.
* 왼쪽 `JTree`에서 부모 노드인 Class 이름을 클릭하게 되면 오른쪽 `JPanel`에 불러온 Class가 가지고 있는 Member들을 표시하는 `JTable`을 배치함.
  
  * 파싱된 정보를 담고 있는 Class(여기선 `ClassInfo`)로부터 `AbstractTableModel` 상속하여 모델 Class를 구현함. 그리고 `AbstractTableModel`의 모델을 `JTable`의 모델로 설정해서 Table을 표시함.

![image-20200615030245096](./img/image-20200615030245096.png)

* 왼쪽 `JTree`에서 자식 노드인 Field 이름을 클릭하게 되면 오른쪽 `JPanel`에 이 Field를 참조하는 Method들을 나열한 `JTable`이 표시됨.
  * 해당 Field 정보를 담고 있는 Instance(여기선 `FieldInfo`의 Instance)로부터 모든 참조 관계를 불러와 `AbstractTableModel`을 상속한 Class에 저장함. 그리고 이 `AbstractTableModel`의 모델을 `JTable`의 모델로 설정해서 Table을 표시함.

![image-20200615030421461](./img/image-20200615030421461.png)

* 왼쪽 `JTree`에서 자식 노드인 Method 이름을 클릭하게 되면 오른쪽 `JPanel`에 이 Method의 코드를 표시하는 `JTextArea`가 표시됨.
* 그리고 왼쪽 아래의 `JTextArea`에 이 Method가 사용하는 Field들을 모두 나열함.



### 한계점 및 개선 사항

정규 표현식(Regular Expression)을 사용하면 정확한 파싱 결과를 보장할 수 있지만, `StringTokenizer`을 기본으로 사용하여 구현하려고 했기 때문에 어떤 부분이 Class이고 Method 인지 등등을 파악하기 힘들었다. 최대한 다양한 Java의 Class를 파싱하려고 시도했지만, 다음과 같은 한계가 있었다.

1. 가장 기본적인 형태의 Member인 Field와 Method만 파싱할 수 있다. 즉, `enum`이나 `inner class` 등이 들어 있는 Class는 파싱이 제대로 되지 않는다.
2. 오직 하나의 Class만 파싱 가능하다. 파일 안에 여러 Class가 있어도 파싱은 되지만, Class 정보를 저장하는 Instance는 하나 밖에 존재하지 않고, Tree View에는 하나의 Class만 표시되도록 만들었다.
   - Class 정보를 담은 Instance를 여러 개 저장할 수 있는`ArrayList`를 만들어 해결할 수 있다.
   - Tree Model의 자식 계층 구조를 더욱 세분화하여 해결할 수 있다.
3. 가장 기본적인 형태의 Access Modifier(접근 제한자)만 파싱 가능하다. 즉, 사전에 정의된 Modifier만 파싱 가능하다.
 4. 가장 기본적인 형태의 Type(자료형)만 파싱 가능하다. 즉, 사전에 정의된 Type만 파싱 가능하다.
 5. `{`, `}`, `;`, `(`, `)`와 같이 Block을 구별할 수 있는 문자들로 파싱을 하는게 이 프로그램의 핵심이기 때문에 문자열이나 주석에 저러한 문자들이 있으면 파싱이 제대로 되지 않는다.
    * 파싱하기 전에 모든 주석을 제거하여 해결할 수 있다.
    * 파싱하기 전에 모든 문자열을 제거하여 해결할 수 있다.
6. 오른쪽 Method 코드 편집기에서 코드 수정은 가능하지만 저장은 불가능하다.
   * `FileWriter` Class를 이용해서 해결할 수 있다.
7. Type이나 Access Modifier를 따로 Class로 관리하지 않은 점이 아쉽다.



## 클래스 설명

총 5가지의 클래스 종류로 나눌 수 있다.

* 첫 번째로, **View를 담당**하는 Class로 JavaClassViewer가 있다.
* 두 번째로, **Model을 담당**하는 Class로 ClassInfoTableModel, FieldReferenceTreeModel, ClassInfoTreeModel이 있다.
* 세 번째로, **Parsing된 정보를 담는 Class**로 ClassInfo, MemberInfo, MethodInfo, FieldInfo가 있다.
* 네 번째로, **Parsing을 담당**하는 Class로 ClassParser가 있다.
* 다섯 번째로, **Exception Class**인 ClassParsingException, EmptyClassInfoException이 있다.



### View를 담당하는 Class

#### JavaClassViewer

`JFrame`을 상속하며 화면에 GUI로 Java Class 정보를 보여주도록 구현된 Class이다. 기본적으로 `openClassFile()` 메서드를 호출하면 class 파일을 불러오는 것 부터 해서 Tree View에 Class 정보를 띄워주는 것 까지 모두 수행한다.

| Field               | Data Type (Access Modifier) | Description                                                  |
| ------------------- | --------------------------- | ------------------------------------------------------------ |
| `rawCode`           | `String `(private)          | `loadClassFile() ` Method를 통해 불러온 원본 Class 소스코드  |
| `classParser`       | `ClassParser `(private)     | Java Class를 파싱하는 클래스                                 |
| `viewerWidth`       | `int` (private)             | 창의 가로 길이 (`JSplitPane`의 구간 크기 조절에 사용)        |
| `viewerHeight`      | `int` (private)             | 창의 세로 길이 (`JSplitPane`의 구간 크기 조절에 사용)        |
| `mainPanel`         | `JSplitPane` (private)      | 화면을 좌우로 나누는 Main Panel                              |
| `leftPanel`         | `JSplitPane` (private)      | 왼쪽 화면을 상하로 나누는 Left Panel (위에는 `classInfoTree`가 밑에는 `usageDisplay`가 위치함) |
| `rightPanel`        | `JPanel` (private)          | 오른쪽 화면에 해당하는 Panel                                 |
| `classInfoTree`     | `JTree` (private)           | Class의 Member들의 이름을 표시하는 Tree View                 |
| `infoTable`         | `JTable` (private)          | Class의 Member들의 상세 정보를 표시하거나 특정 Field가 사용되는 Method들을 표시하는데 사용하는 Table View |
| `usageDispaly`      | `JTextArea` (private)       | Method가 사용하는 Field들을 표시하는 Text Area               |
| `sourceCodeDisplay` | `JTextArea` (private)       | Method의 소스 코드를 표시하는 Text Area                      |

| Constructor                 | Access Modifier | Description                                                  |
| --------------------------- | --------------- | ------------------------------------------------------------ |
| `JavaClassViewer(int, int)` | public          | (창의 가로 길이, 창의 세로 길이)를 인자로 입력받아 `JavaClassViewer`를 생성하고 표시한다. 각종 Component들을 초기화 한다. |

| Method                               | Return Type(Access Modifier) | Description                                                  |
| ------------------------------------ | ---------------------------- | ------------------------------------------------------------ |
| `openClassFile()`                    | `void` (public)              | File - Open 메뉴를 클릭하면 실행되는 Method. Class 파일을 열 수 있는 Dialog 창이 표시되고 Class 파일을 Parsing 한다. 그리고 왼쪽의 Tree View에 Class Member의 이름을 표시한다. |
| `main(String[])`                     | `void` (public)              | 프로그램 진입점이다.                                         |
| `initMenuBar()`                      | `void` (private)             | 화면 상단의 MenuBar를 생성한다.                              |
| `initComponents()`                   | `void` (private)             | 화면 표시를 위한 각종 Component들을 생성한다. 생성자가 호출될 때 같이 호출된다. |
| `initEventListener()`                | `void` (private)             | 필요한 Event Listener를 등록한다. 생성자가 호출될 때 같이 호출된다. |
| `clearDisplays()`                    | `void` (private)             | 화면에 다른 정보를 표시할 때 먼저 화면에 표시된 정보를 지울 때 사용하는 메서드이다. 특히 `infoTable`, `usageDisplay`, `sourceCodeDispaly`의 내용이 초기화된다. |
| `classSelectionHandler()`            | `void` (private)             | TreeView에서 부모 노드인 Class를 선택했을 때의 Event Handler이다. 오른쪽 화면에 Class의 Member들을 나타내는 Table View를 표시한다. |
| `methodSelectionHandler(MethodInfo)` | `void` (private)             | TreeView에서 자식 노드인 Method를 선택했 때의 Event Handler이다. 인자로 받은 `MethodInfo` Instance를 통해 오른쪽 화면에 Method의 소스 코드를 표시하고, 왼쪽 하단에 이 Method가 참조하는 Field들을 텍스트로 나열한다. |
| `fieldSelectionHandler(FieldInfo)`   | `void` (private)             | TreeView에서 자식 노드인 Field를 선택했을 때의 Event Handler이다. 인자로 받은 `FieldInfo` Instance를 통해 오른쪽 화면에 이 Field를 사용하는 Method들을 Table View로 표시한다. |
| `loadClassFile()`                    | `void` (private)             | 파일을 열 수 있는 Dialog 창이 표시되고, 파일의 내용을 `rawCode`에 적재한다. |



### Model을 담당하는 Class

#### ClassInfoTreeModel

`TreeModel`을 implements한다. 즉, `ClassInfo` Instance를 Tree View에 표시할 수 있도록 변환해주는 Class이다. 이때 표시하는 정보는 Member의 이름이다.

| Field     | Data Type (Access Modifier) | Description              |
| --------- | --------------------------- | ------------------------ |
| classInfo | ClassInfo (private)         | Tree에 표시할 Class 정보 |

| Constructor                   | Access Modifier | Description                                                  |
| ----------------------------- | --------------- | ------------------------------------------------------------ |
| ClassInfoTreeModel(ClassInfo) | public          | 인자로 전달받은 ClassInfo Instance를 통해 Tree View에 표시할 수 있는 정보(모델)로 바꾼다. |

| Method                          | Return Type (Access Modifier) | Description                                                  |
| ------------------------------- | ----------------------------- | ------------------------------------------------------------ |
| getRoot()                       | Object (public)               | 이 Model의 ClassInfo를 반환한다.                             |
| getChild(Object, int)           | Object (public)               | 이 Model의 ClassInfo에 있는 MemberInfo들을 index를 통해 가져온다. |
| getChildCount(Object)           | int (public)                  | 이 Model의 ClassInfo의 총 Member 수를 반환한다.              |
| getIndexOfChild(Object, Object) | int (public)                  | 인자로 받은 Member 정보에 대해 Index를 반환한다.             |
| isLeaf(Object)                  | boolean (public)              | 인자로 받은 Object가 Member이면 true를 반환한다. 그 외에는 false. |



#### ClassInfoTableModel

`AbstractTableModel`을 상속한다. 즉, `ClassInfo` Instance를 Table View에 표시할 수 있도록 변환해주는 Class이다. 이때 표시하는 정보는 Member의 이름, Type, Access Modifier이다.

| Field        | Data Type (Access Modifier) | Description                                                  |
| ------------ | --------------------------- | ------------------------------------------------------------ |
| `classInfo`  | `ClassInfo` (private)       | Table에 표시할 Class 정보                                    |
| `columnName` | `String[]` (private)        | 첫 번째 column: Name, 두 번째 column: Type, 세 번째 column: Access |
| `data`       | `Object[][]` (private)      | Table에 표시할 데이터                                        |

| Constructor                      | Access Modifier | Description                                                  |
| -------------------------------- | --------------- | ------------------------------------------------------------ |
| `ClassInfoTableModel(ClassInfo)` | public          | 인자로 전달받은 `ClassInfo` Instance를 통해 Table로 표시할 수 있는 정보로 변환한다. 다시 말해, Class의 Member들의 이름, Type, Access Modifier 정보를 table에 표시할 수 있게끔 한다. |

| Method                 | Return Type (Access Modifier) | Description                                                  |
| ---------------------- | ----------------------------- | ------------------------------------------------------------ |
| `initData()`           | `void` (private)              | 생성자에서 호출되는 Method로 정보를 Table View로 표시할 수 있도록 가공한다. |
| `getRowCount()`        | `int` (public)                | 정보의 갯수를 반환한다.                                      |
| `getColumnCount()`     | `int` (public)                | Table의 열의 갯수를 반환한다(여기선 3개).                    |
| `getColumnName(int)`   | `String` (public)             | 전달받은 인자를 통해 Table의 열의 이름을 반환한다.           |
| `getValueAt(int, int)` | `Object` (public)             | 전달받은 인자(row, column)를 통해 Table에 표시될 정보를 반환한다. |



#### FieldReferenceTableModel

`AbstractTableModel`을 상속한다. 즉, `FieldInfo` Instance를 Table View에 표시할 수 있도록 변환해주는 Class이다. 이때 표시하는 정보는 이 Field를 사용하는 Method들의 이름이다.

| Field        | Data Type (Access Modifier) | Description                                                  |
| ------------ | --------------------------- | ------------------------------------------------------------ |
| `fieldInfo`  | `FieldInfo` (private)       | Field 정보(이를 통해 어떤 Method가 이 Field를 사용하는지 알 수 있음) |
| `columnName` | `String[]` (private)        | 첫 번째 column: Name, 두 번째 column: Method                 |
| `data`       | `Object[][]` (private)      | Table에 표시할 데이터                                        |

| Constructor                           | Access Modifier | Description                                                  |
| ------------------------------------- | --------------- | ------------------------------------------------------------ |
| `FieldReferenceTableModel(FieldInfo)` | public          | 인자로 전달받은 `FieldInfo` Instance를 통해 Table로 표시할 수 있는 정보로 변환한다. 다시 말해, Field를 사용하는 Method들의 정보를 Table에 표시할 수 있게끔 한다. |

| Method                 | Return Type (Access Modifier) | Description                                                  |
| ---------------------- | ----------------------------- | ------------------------------------------------------------ |
| `initData()`           | `void` (private)              | 생성자에서 호출되는 Method로 정보를 Table View로 표시할 수 있도록 가공한다. |
| `getRowCount()`        | `int` (public)                | 정보의 갯수를 반환한다.                                      |
| `getColumnCount()`     | `int` (public)                | Table의 열의 갯수를 반환한다(여기선 2개).                    |
| `getColumnName(int)`   | `String` (public)             | 전달받은 인자를 통해 Table의 열의 이름을 반환한다.           |
| `getValueAt(int, int)` | `Object` (public)             | 전달받은 인자(row, column)를 통해 Table에 표시될 정보를 반환한다. |



### Parsing된 정보를 담는 Class

#### ClassInfo

파싱된 Class의 모든 정보를 담는 Class이다. 이 Class의 이름과 어떤 Member들을 가지고 있는지에 대한 정보를 담고 있다.

| Field        | Data Type (Access Modifier) | Description                                         |
| ------------ | --------------------------- | --------------------------------------------------- |
| `name`       | `String` (private)          | Class의 이름을 저장하는 문자열                      |
| `memberInfo` | `ArrayList<MemberInfo>`     | Class가 가지고 있는 Member들을 저장하는 `ArrayList` |

| Constructor   | Access Modifier | Description               |
| ------------- | --------------- | ------------------------- |
| `ClassInfo()` | public          | `ArrayList`를 초기화한다. |

| Method                           | Return Type (Access Modifier) | Description                                                  |
| -------------------------------- | ----------------------------- | ------------------------------------------------------------ |
| `toString()`                     | `String` (public)             | Class의 이름을 반환한다.                                     |
| `addMethodInfo(MethodInfo)`      | `void` (public)               | `MethodInfo` Instance를 `ArrayList`에 등록한다.              |
| `addFieldInfo(FieldInfo)`        | `void` (public)               | `FieldInfo` Instance를 `ArrayList`에 등록한다.               |
| `setName(String)`                | `void` (public)               | Class의 이름을 설정한다.                                     |
| `getName()`                      | `String` (public)             | Class의 이름을 반환한다.                                     |
| `getMemberInfo(int)`             | `MemberInfo `(public)         | 인자로 전달받은 index에 해당하는 `MemberInfo` Instance를 `ArrayList`에서 가져와 반환한다. |
| `getMemberInfoSize()`            | `int `(public)                | 등록된 `MemberInfo`의 갯수를 반환한다.                       |
| `getMemberInfoIndex(MemberInfo)` | `int `(public)                | 인자로 전달받은 `MemberInfo` Instance에 해당하는 `ArrayList`의 index를 반환한다. |



#### MemberInfo

`MethodInfo`와 `FieldInfo`의 부모 Class이다. Member의 이름, Access Modifier, Type 정보를 담고 있으며 추가적으로 참조 관계 `ArrayList`형태로 담고 있다. 예를 들어 Method A가 Field B, C를 참조하고 있으면 Method A의 참조 관계 `ArrayList`에는 Field B와 C의 Instance가 들어있다.

| Field            | Data Type (Access Modifier)       | Description                           |
| ---------------- | --------------------------------- | ------------------------------------- |
| `name`           | `String` (private)                | Member의 이름                         |
| `accessModifier` | `String` (private)                | Member의 Access Modifier(접근 제한자) |
| `type`           | `String` (private)                | Member의 Type(자료형)                 |
| `refList`        | `ArrayList<MemberInfo>` (private) | Member의 참조 관계                    |

| Constructor                        | Access Modifier | Description                                                  |
| ---------------------------------- | --------------- | ------------------------------------------------------------ |
| MemberInfo(String, String, String) | public          | (이름, Access Modifier, Type)을 인자로 받아 `MemberInfo ` Instance를 생성한다. |

| Method                      | Return Type (Access Modifier) | Description                                                  |
| --------------------------- | ----------------------------- | ------------------------------------------------------------ |
| `setName(String)`           | `void` (public)               | Member의 이름을 설정한다.                                    |
| `getName()`                 | `String` (public)             | Member의 이름을 반환한다.                                    |
| `setAccessModifier(String)` | `void` (public)               | Member의 Access Modifier를 설정한다.                         |
| `getAccessModifier()`       | `String` (public)             | Member의 Access Modifier를 반환한다.                         |
| `setType(String)`           | `void` (public)               | Member의 Type을 설정한다.                                    |
| `getType()`                 | `String` (public)             | Member의 Type을 반환한다.                                    |
| `addReference(MemberInfo)`  | `void` (public)               | Member의 참조 관계를 추가한다.                               |
| `getReferenceSize()`        | `int` (public)                | Member의 참조 관계의 크기를 반환한다.                        |
| `getReference(int)`         | `MemberInfo` (public)         | 전달 받은 index 인자를 통해 참조 관계에 있는 `MemberInfo`를 반환한다. |



#### MethodInfo

`MemberInfo`를 상속하며 Method 정보를 담는 클래스이다.

| Field       | Data Type (Access Modifier)   | Description                           |
| ----------- | ----------------------------- | ------------------------------------- |
| `innerCode` | `String` (private)            | Method의 소스 코드                    |
| `arguments` | `ArrayList<String>` (private) | Method의 매개 변수의 Type을 담은 List |

| Constructor                                  | Access Modifier | Description                                                  |
| -------------------------------------------- | --------------- | ------------------------------------------------------------ |
| `MethodInfo(String, String, String, String)` | public          | (Access Modifier, Return Type, 이름, 소스 코드)를 인자로 받아 `MethodInfo` Instance를 생성한다. |

| Method                    | Return Type (Access Modifier) | Description                                                  |
| ------------------------- | ----------------------------- | ------------------------------------------------------------ |
| `addArgumentType(String)` | `void` (public)               | Method의 매개 변수의 Type을 추가한다.                        |
| `getSimpleInfo()`         | `String` (public)             | Method의 정보를 간단한 형태로 출력한다. (Example: `getSimpleInfo(): String`) |
| `getName()`               | `String `(public)             | Method의 이름을 반환한다.                                    |
| `getInnerCode()`          | `String` (public)             | Method의 소스 코드를 반환한다.                               |
| `setInnerCode(String)`    | `void` (public)               | Method의 소스 코드를 설정한다. Method의 소스 코드를 설정하더라도 기본 정보(이름, Access Modifier 등)은 바뀌지 않는다. |
| `toString()`              | `String` (public)             | `getSimpleInfo()`를 반환한다.                                |



#### FieldInfo

`MemberInfo`를 상속하며 Field 정보를 담는 클래스이다.

| Constructor                         | Access Modifier | Description                                                  |
| ----------------------------------- | --------------- | ------------------------------------------------------------ |
| `FieldInfo(String, String, String)` | public          | (Access Modifier, Type, 이름)을 인자로 받아 `FieldInfo` Instance를 생성한다. |

| Method            | Return Type (Access Modifier) | Description                                                  |
| ----------------- | ----------------------------- | ------------------------------------------------------------ |
| `getSimpleInfo()` | `String` (public)             | Field의 정보를 간단한 형태로 출력한다. (Example: `name: String`) |
| `toString()`      | `String` (public)             | `getSimpleInfo()`를 반환한다.                                |



### Parsing을 담당하는 Class

#### ClassParser

Java Class를 파싱하는 핵심적인 Class이다. 이 Class를 사용하기 위해서는 먼저 생성자로 소스 코드를 `ClassParser`에 적재(load)해야 한다. 그리고 파싱된 Class 정보를 담은 `ClassInfo`를 얻기 위해서, `parse()` Method를 실행한다. 그 후에 `getClassInfo()` Method를 사용하면 파싱된 정보를 담은 `ClassInfo` Instance가 반환된다. 또한 Member 간의 참조 관계를 구하고 싶을 땐 `findReferenceRelation()` Method를 호출하면 된다.

| Field       | Data Type (Access Modifier) | Description                                                  |
| ----------- | --------------------------- | ------------------------------------------------------------ |
| `delimiter` | `String` (private)          | 파싱하기 위한 기본적인 구분자를 정의함                       |
| `rawCode`   | `String` (private)          | 적재된 Java Class 소스 코드                                  |
| `classInfo` | `ClassInfo` (private)       | 파싱된 정보를 담은 `ClassInfo` Instance (`parse()` 함수를 호출해야만 의미있는 값이 담긴다) |

| Constructor           | Access Modifier | Description                                  |
| --------------------- | --------------- | -------------------------------------------- |
| `ClassParser(String)` | public          | 인자로 받은 Java Class 소스 코드를 적재한다. |

| Method                      | Return Type (Access Modifier) | Description |
| --------------------------- | ----------------------------- | ----------- |
| `parse()`                 | `void` (public) | Field Block과 Method Block을 파악한다. 그리고 나서 Field Block의 내용을 `parseField()` Method로 파싱하고 `classInfo`에 파싱된 `FieldInfo`를 추가한다. 마찬가지로 Method Block의 내용을 `parseMethod()` Method로 파싱하고 `classInfo`에 파싱된 `MethodInfo`를 추가한다. 파싱 중 오류가 발생했을 경우 `ClassParsingException`이 발생된다. |
| `getClassInfo()`            | `ClassInfo` (public) | `parse()`Method를 호출하지 않고 이 Method를 호출하게 되면 `EmptyClassInfoException`이 발생된다. 파싱된 `ClassInfo` Insatance가 반환된다. |
| `findReferenceRelation()`   | `void` (public) | `parse()` Method를 호출하지 않고 이 Method를 호출하게 되면 `EmptyClassInfoException`이 발생된다. 파싱된 정보를 바탕으로 Class의 Member간 참조 관계를 파악하여 각 Member에 참조 관계를 등록한다. |
| `removeStringBlock(String)` | `String` (private) | 파싱을 정상적으로 하기 위해 문자열을 모두 제거한다. 문자를 표현한 문장도 제거한다. |
| `parseClassName(String)`    | `String` (private) | `public class A extends B`와 같은 형태의 문자열이 들어왔을 때 Class의 이름을 성공적으로 파싱하고 Class의 이름을 반환한다. 인자로 `{`의 이전 문자열을 받는다. |
| `parseMethod(String)`       | `MethodInfo` (public) | `public void push(int v) { }`와 같은 형태의 문자열이 들어왔을 때 Method의 이름과 Access Modifier, Return Type 그리고 Type of Argument의 정보를 파싱하고 `MethodInfo` Class에 정보를 담는다. 그리고 나서 `MethodInfo`의 Instance를 반환한다. |
| `parseField(String)`        | `FieldInfo` (public) | `private int[] data`와 같은 형태의 문자열이 들어왔을 때 Field의 이름과 Access Modifier, Type을 파싱하고 `FieldInfo` Class에 정보를 담는다. 그리고 나서 `FieldInfo`의 Instance를 반환한다. |
| `isAccessModifier(String)`  | `boolean` (private) | 파싱을 할 때 주어진 token이 Access Modifier인지 파악하는데 사용되는 Method이다. |
| `isTypeKeyword(String)`     | `boolean` (private) | 파싱을 할 때 주어진 token이 Type Keyword인지 파악하는데 사용되는 Method이다. |



### Exception Class

#### ClassParsingException

파싱 중 오류가 발생했을 경우를 나타내는 Exception Class이다. Stack이 비어 있는데 `pop()` 작업을 수행한다던지, index가 범위를 초과했다던지 등의 경우에 `ClassParsingException`을 발생시킨다.

| Constructor                     | Access Modifier | Description                                                  |
| ------------------------------- | --------------- | ------------------------------------------------------------ |
| `ClassParsingException(String)` | public          | 파싱 중 오류가 발생한 소스 코드를 인자로 받아 Exception을 발생시킨다. |



#### EmptyClassInfoException

`parse()` 함수를 호출하지 않아 `ClassInfo` Instance가 `null`일 경우를 나타내는 Exception Class이다.

| Constructor                 | Access Modifier | Description |
| --------------------------- | --------------- | ----------- |
| `EmptyClassInfoException()` | public          |             |



## 프로그램 소스

`JavaClassViewer10.zip`에 프로그램의 소스 코드가 들어있습니다.



## 실행 방법

1. 이클립스로 `JavaClassViewer10.zip` 소스 코드를 열어 컴파일 후 프로그램을 실행합니다 (Java SE8).
   * 함께 첨부한 소스 코드(`JavaClassViewer10.zip`)를 압축 해제합니다.
   * Eclipse를 실행해서 File - Import 메뉴를 이용해 소스 코드를 불러옵니다(Import 메뉴 내에 General > Project from Folder or Archive 항목 이용)
   * `default package` 의 `JavaClassViewer.java`를 실행합니다.
2. 프로그램 내의 File - Open을 클릭하여 보기를 원하는 Java class 파일을 엽니다.

![image-20200615062656139](./img/image-20200615062656139.png)

3. 왼쪽의 Tree View를 통해 불러온 class 파일의 Member들을 확인할 수 있습니다.

![image-20200615062808190](./img/image-20200615062808190.png)
