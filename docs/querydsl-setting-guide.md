# Querydsl 설정 및 QClass 생성 가이드

## 1. 목적

본 문서는 Findex 프로젝트에서 Querydsl을 사용하기 위한 Gradle 설정과 QClass 생성 방법을 정리한 문서입니다.

이번 프로젝트에서는 지수 정보, 지수 데이터, 연동 이력, 자동 연동 설정 등에서 다양한 검색 조건이 필요합니다.
예를 들어 다음과 같은 조건들이 조합될 수 있습니다.

* 지수명 검색
* 지수 분류 검색
* 기준일자 범위 검색
* 동기화 상태 검색
* 정렬 조건
* 페이징 조건
* 커서 기반 조회 조건

이러한 동적 검색 조건을 문자열 기반 JPQL로 직접 조합하면 코드가 복잡해지고 런타임 오류 가능성이 높아집니다.
따라서 본 프로젝트에서는 복잡한 조회 기능 구현 시 Querydsl을 사용합니다.

Querydsl은 Java 코드 기반의 fluent API로 타입 안정성 있는 SQL 유사 쿼리를 작성할 수 있게 도와주는 프레임워크입니다.

---

## 2. Querydsl 사용 목적

Querydsl을 사용하는 이유는 다음과 같습니다.

| 목적       | 설명                                 |
| -------- | ---------------------------------- |
| 동적 쿼리 구현 | 검색 조건이 있을 때만 where 조건을 추가할 수 있습니다. |
| 타입 안정성   | 문자열 JPQL보다 필드명 오타를 줄일 수 있습니다.      |
| 가독성 향상   | 복잡한 조회 조건을 메서드 단위로 분리할 수 있습니다.     |
| 유지보수성 향상 | 조건 추가/변경 시 쿼리 구조를 파악하기 쉽습니다.       |
| JPA와 연동  | Entity 기반으로 조회 쿼리를 작성할 수 있습니다.     |

---

## 3. Gradle Querydsl 설정

현재 프로젝트의 `build.gradle`에는 Querydsl 사용을 위해 아래 설정이 추가되어 있습니다.

```gradle
// Querydsl
implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
```

각 의존성의 역할은 다음과 같습니다.

| 설정                        | 역할                                                 |
| ------------------------- | -------------------------------------------------- |
| `querydsl-jpa`            | JPA 환경에서 Querydsl을 사용하기 위한 라이브러리입니다.               |
| `querydsl-apt`            | Entity를 분석해서 QClass를 생성하는 Annotation Processor입니다. |
| `jakarta.annotation-api`  | Jakarta annotation 관련 타입을 처리하기 위해 필요합니다.           |
| `jakarta.persistence-api` | JPA Entity annotation을 인식하기 위해 필요합니다.              |

Spring Boot 3.x는 `javax.persistence`가 아니라 `jakarta.persistence` 기반이므로 Querydsl 의존성에도 `:jakarta` classifier를 사용합니다.

---

## 4. 전체 Querydsl 설정 코드

현재 프로젝트의 Querydsl 관련 설정은 다음과 같습니다.

```gradle
// Querydsl
implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
annotationProcessor 'jakarta.persistence:jakarta.persistence-api'

// Querydsl Q클래스 생성 위치
def generated = layout.buildDirectory.dir("generated/sources/annotationProcessor/java/main")

sourceSets {
    main {
        java {
            srcDir generated
        }
    }
}

tasks.withType(JavaCompile).configureEach {
    options.annotationProcessorGeneratedSourcesDirectory = generated.get().asFile
}
```

---

## 5. QClass란?

QClass는 Querydsl이 Entity를 기반으로 자동 생성하는 메타 모델 클래스입니다.

예를 들어 아래와 같은 Entity가 있다고 가정합니다.

```java
@Entity
public class IndexInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String indexName;
    private String indexClassification;
}
```

Querydsl QClass 생성 작업을 실행하면 다음과 같은 클래스가 자동 생성됩니다.

```text
QIndexInfo.java
```

Querydsl에서는 이 QClass를 사용하여 타입 안정성 있는 쿼리를 작성합니다.

```java
QIndexInfo indexInfo = QIndexInfo.indexInfo;

List<IndexInfo> result = queryFactory
        .selectFrom(indexInfo)
        .where(indexInfo.indexName.contains("KOSPI"))
        .fetch();
```

즉, QClass는 Querydsl 쿼리 작성에 필요한 자동 생성 클래스입니다.

---

## 6. QClass 생성 위치

현재 설정 기준으로 QClass는 아래 위치에 생성됩니다.

```text
build/generated/sources/annotationProcessor/java/main
```

예시:

```text
build/generated/sources/annotationProcessor/java/main/com/sb13/findex/domain/indexinfo/entity/QIndexInfo.java
```

`build` 디렉터리는 Gradle 빌드 과정에서 생성되는 결과물이므로 Git에 커밋하지 않습니다.

보통 `.gitignore`에 아래 설정이 포함되어 있어야 합니다.

```gitignore
/build/
```

---

## 7. `sourceSets` 설정이 필요한 이유

아래 설정은 생성된 QClass 위치를 main source set에 포함시키기 위한 설정입니다.

```gradle
sourceSets {
    main {
        java {
            srcDir generated
        }
    }
}
```

이 설정이 없으면 QClass가 생성되더라도 IDE나 Gradle 빌드 과정에서 해당 경로를 소스 코드로 인식하지 못할 수 있습니다.

즉, 이 설정은 다음 문제를 방지하기 위한 것입니다.

```text
Cannot resolve symbol QIndexInfo
Cannot resolve symbol QIndexData
```

---

## 8. `JavaCompile` 설정이 필요한 이유

아래 설정은 Annotation Processor가 생성한 소스 파일을 지정한 위치에 생성하도록 설정합니다.

```gradle
tasks.withType(JavaCompile).configureEach {
    options.annotationProcessorGeneratedSourcesDirectory = generated.get().asFile
}
```

이 설정을 통해 Querydsl QClass가 아래 위치에 생성됩니다.

```text
build/generated/sources/annotationProcessor/java/main
```

---

## 9. QClass 생성 명령어

QClass는 Gradle의 Java compile 과정에서 생성됩니다.
따라서 별도의 Querydsl 전용 명령어를 실행하는 것이 아니라 `compileJava`, `build`, `test` 등의 Gradle task를 실행하면 Annotation Processor가 동작하면서 QClass를 생성합니다.

가장 기본적으로는 아래 명령어를 사용합니다.

```bash
./gradlew clean compileJava
```

Windows에서는 아래 명령어를 사용합니다.

```powershell
.\gradlew.bat clean compileJava
```

---

## 10. Mac / Linux / Git Bash 환경에서 QClass 생성

Mac, Linux, Git Bash 환경에서는 프로젝트 루트에서 아래 명령어를 실행합니다.

```bash
./gradlew clean compileJava
```

빌드까지 함께 확인하고 싶다면 아래 명령어를 사용합니다.

```bash
./gradlew clean build
```

테스트까지 함께 실행하려면 아래 명령어를 사용합니다.

```bash
./gradlew clean test
```

QClass 생성 여부는 아래 명령어로 확인할 수 있습니다.

```bash
ls build/generated/sources/annotationProcessor/java/main
```

패키지 경로까지 확인하려면 다음과 같이 이동합니다.

```bash
find build/generated/sources/annotationProcessor/java/main -name "Q*.java"
```

예상 결과 예시:

```text
build/generated/sources/annotationProcessor/java/main/com/sb13/findex/indexinfo/entity/QIndexInfo.java
build/generated/sources/annotationProcessor/java/main/com/sb13/findex/indexdata/entity/QIndexData.java
```

---

## 11. Windows PowerShell 환경에서 QClass 생성

Windows PowerShell에서는 프로젝트 루트에서 아래 명령어를 실행합니다.

```powershell
.\gradlew.bat clean compileJava
```

빌드까지 함께 확인하고 싶다면 아래 명령어를 사용합니다.

```powershell
.\gradlew.bat clean build
```

테스트까지 함께 실행하려면 아래 명령어를 사용합니다.

```powershell
.\gradlew.bat clean test
```

QClass 생성 여부는 아래 명령어로 확인할 수 있습니다.

```powershell
Get-ChildItem -Recurse build\generated\sources\annotationProcessor\java\main -Filter "Q*.java"
```

예상 결과 예시:

```text
QIndexInfo.java
QIndexData.java
QSyncJob.java
QAutoSyncConfig.java
```

---

## 12. Windows CMD 환경에서 QClass 생성

Windows CMD에서는 프로젝트 루트에서 아래 명령어를 실행합니다.

```cmd
gradlew.bat clean compileJava
```

빌드까지 함께 확인하고 싶다면 아래 명령어를 사용합니다.

```cmd
gradlew.bat clean build
```

QClass 생성 여부는 아래 명령어로 확인할 수 있습니다.

```cmd
dir /s /b build\generated\sources\annotationProcessor\java\main\Q*.java
```

---

## 13. IntelliJ에서 QClass 인식 방법

Gradle 명령어로 QClass를 생성했는데 IntelliJ에서 `QIndexInfo` 같은 클래스를 인식하지 못할 수 있습니다.

이 경우 아래 순서로 확인합니다.

### 1. Gradle Refresh

IntelliJ 우측 Gradle 탭에서 새로고침 버튼을 클릭합니다.

```text
Gradle 탭 → Reload All Gradle Projects
```

### 2. QClass 생성 명령어 실행

터미널에서 아래 명령어를 실행합니다.

```bash
./gradlew clean compileJava
```

Windows PowerShell에서는 아래 명령어를 실행합니다.

```powershell
.\gradlew.bat clean compileJava
```

### 3. Generated Sources Root 확인

아래 경로가 생성되었는지 확인합니다.

```text
build/generated/sources/annotationProcessor/java/main
```

IntelliJ에서 자동으로 인식되지 않으면 해당 디렉터리를 우클릭 후 아래 메뉴를 선택합니다.

```text
Mark Directory as → Generated Sources Root
```

---

## 14. Querydsl 기본 사용 예시

Querydsl을 사용하려면 `JPAQueryFactory` Bean을 등록하는 것이 일반적입니다.

예시:

```java
@Configuration
public class QuerydslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
```

Repository 구현체에서는 다음과 같이 사용합니다.

```java
@Repository
@RequiredArgsConstructor
public class IndexInfoRepositoryCustomImpl implements IndexInfoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<IndexInfo> search(String keyword) {
        QIndexInfo indexInfo = QIndexInfo.indexInfo;

        return queryFactory
                .selectFrom(indexInfo)
                .where(indexInfo.indexName.containsIgnoreCase(keyword))
                .fetch();
    }
}
```

---

## 15. 동적 조건 분리 예시

Querydsl에서는 조건을 메서드로 분리하면 검색 조건을 깔끔하게 관리할 수 있습니다.

```java
private BooleanExpression indexNameContains(String keyword) {
    if (keyword == null || keyword.isBlank()) {
        return null;
    }

    return QIndexInfo.indexInfo.indexName.containsIgnoreCase(keyword);
}
```

사용 예시:

```java
return queryFactory
        .selectFrom(indexInfo)
        .where(indexNameContains(keyword))
        .fetch();
```

Querydsl의 `where` 조건에 `null`이 들어가면 해당 조건은 무시됩니다.
이 특성을 이용하면 검색 조건이 있을 때만 동적으로 조건을 추가할 수 있습니다.

---

## 16. Repository 구조 예시

Querydsl을 사용할 때는 Spring Data JPA Repository와 Custom Repository를 함께 사용하는 방식이 일반적입니다.

```text
indexinfo/
 ├── entity/
 │    └── IndexInfo.java
 ├── repository/
 │    ├── IndexInfoRepository.java
 │    ├── IndexInfoRepositoryCustom.java
 │    └── IndexInfoRepositoryCustomImpl.java
```

### `IndexInfoRepository`

```java
public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long>, IndexInfoRepositoryCustom {
}
```

### `IndexInfoRepositoryCustom`

```java
public interface IndexInfoRepositoryCustom {
    List<IndexInfo> search(String keyword);
}
```

### `IndexInfoRepositoryCustomImpl`

```java
@Repository
@RequiredArgsConstructor
public class IndexInfoRepositoryCustomImpl implements IndexInfoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<IndexInfo> search(String keyword) {
        QIndexInfo indexInfo = QIndexInfo.indexInfo;

        return queryFactory
                .selectFrom(indexInfo)
                .where(indexNameContains(keyword))
                .fetch();
    }

    private BooleanExpression indexNameContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }

        return QIndexInfo.indexInfo.indexName.containsIgnoreCase(keyword);
    }
}
```

---

## 17. 주의사항

### 1. QClass는 Git에 커밋하지 않습니다.

QClass는 빌드 과정에서 자동 생성되는 파일입니다.
따라서 `build/generated/...` 아래 생성된 QClass는 Git에 커밋하지 않습니다.

```gitignore
/build/
```

### 2. Entity 변경 후에는 QClass를 다시 생성해야 합니다.

Entity에 필드를 추가하거나 이름을 변경했다면 QClass도 다시 생성해야 합니다.

```bash
./gradlew clean compileJava
```

Windows:

```powershell
.\gradlew.bat clean compileJava
```

### 3. QClass가 없다는 오류가 발생하면 compileJava를 실행합니다.

오류 예시:

```text
Cannot resolve symbol QIndexInfo
Cannot resolve symbol QIndexData
```

해결:

```bash
./gradlew clean compileJava
```

### 4. `jakarta` classifier를 사용해야 합니다.

Spring Boot 3.x 환경에서는 Jakarta Persistence를 사용하므로 Querydsl 설정도 `:jakarta` classifier를 사용합니다.

```gradle
implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
```

### 5. `build` 폴더 삭제는 문제되지 않습니다.

`build` 폴더를 삭제해도 QClass는 다시 생성할 수 있습니다.

```bash
./gradlew clean compileJava
```

---

## 18. 자주 발생하는 문제

### 문제 1. `QIndexInfo`를 찾을 수 없음

```text
Cannot resolve symbol QIndexInfo
```

해결:

```bash
./gradlew clean compileJava
```

이후 IntelliJ에서 Gradle Refresh를 실행합니다.

---

### 문제 2. QClass가 생성되지 않음

확인할 것:

```text
1. Entity에 @Entity가 붙어 있는지 확인
2. Querydsl annotationProcessor 설정이 있는지 확인
3. jakarta.persistence-api annotationProcessor 설정이 있는지 확인
4. Gradle Refresh를 했는지 확인
5. clean compileJava를 실행했는지 확인
```

---

### 문제 3. Windows에서 `./gradlew`가 실행되지 않음

Windows PowerShell에서는 아래 명령어를 사용합니다.

```powershell
.\gradlew.bat clean compileJava
```

CMD에서는 아래 명령어를 사용합니다.

```cmd
gradlew.bat clean compileJava
```

Git Bash를 사용하는 경우에는 아래 명령어도 가능합니다.

```bash
./gradlew clean compileJava
```

---

### 문제 4. IntelliJ에서는 오류가 보이지만 Gradle build는 성공함

이 경우 IntelliJ가 generated source 경로를 아직 인식하지 못한 경우일 수 있습니다.

해결 방법:

```text
1. Gradle Reload 실행
2. ./gradlew clean compileJava 실행
3. build/generated/sources/annotationProcessor/java/main 경로 확인
4. 필요 시 Mark Directory as → Generated Sources Root 설정
```

---

## 19. 팀원 사용 규칙

팀원들은 Entity 추가 또는 변경 후 Querydsl을 사용해야 한다면 아래 명령어를 실행합니다.

Mac / Linux / Git Bash:

```bash
./gradlew clean compileJava
```

Windows PowerShell:

```powershell
.\gradlew.bat clean compileJava
```

Windows CMD:

```cmd
gradlew.bat clean compileJava
```

그리고 QClass는 직접 수정하지 않습니다.

```text
QClass는 자동 생성 파일이므로 직접 수정하지 않는다.
Entity를 수정한 뒤 compileJava를 다시 실행해 재생성한다.
```

---

## 20. 최종 정리

이번 프로젝트의 Querydsl 설정 핵심은 다음과 같습니다.

```gradle
implementation 'com.querydsl:querydsl-jpa:5.1.0:jakarta'
annotationProcessor 'com.querydsl:querydsl-apt:5.1.0:jakarta'
annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
```

QClass 생성 위치는 다음과 같습니다.

```text
build/generated/sources/annotationProcessor/java/main
```

QClass 생성 명령어는 다음과 같습니다.

Mac / Linux / Git Bash:

```bash
./gradlew clean compileJava
```

Windows PowerShell:

```powershell
.\gradlew.bat clean compileJava
```

Windows CMD:

```cmd
gradlew.bat clean compileJava
```

Querydsl 사용 흐름은 다음과 같습니다.

```text
1. Entity 작성
2. ./gradlew clean compileJava 실행
3. QClass 생성 확인
4. JPAQueryFactory 설정
5. Custom Repository에서 Querydsl 사용
```

QClass는 자동 생성 파일이므로 Git에 커밋하지 않습니다.
