# Flyway Migration Guide

## 1. 목적

본 문서는 Findex 프로젝트에서 Flyway를 사용하여 데이터베이스 스키마 변경 이력을 관리하는 방법을 정리한 문서입니다.

여러 명이 함께 개발하는 프로젝트에서는 각자 로컬 DB 상태가 달라질 수 있습니다.
따라서 테이블 생성, 컬럼 추가, 제약조건 추가와 같은 DDL 변경 사항을 코드처럼 관리할 필요가 있습니다.

본 프로젝트에서는 Flyway를 사용하여 DB 스키마 변경 이력을 SQL 파일로 관리합니다.

---

## 2. Flyway를 사용하는 이유

Flyway를 사용하는 이유는 다음과 같습니다.

| 목적                    | 설명                                                 |
| --------------------- | -------------------------------------------------- |
| DB 변경 이력 관리           | 어떤 DDL이 언제 추가되었는지 파일로 확인할 수 있습니다.                  |
| 팀원 간 DB 구조 통일         | 동일한 migration script를 실행하여 로컬 DB 구조 차이를 줄일 수 있습니다. |
| 수동 DDL 실행 최소화         | 팀원이 직접 SQL을 하나씩 실행하지 않아도 애플리케이션 실행 시 반영됩니다.        |
| JPA `ddl-auto` 의존도 감소 | 테이블 생성/변경을 Hibernate 자동 생성이 아니라 명시적인 SQL로 관리합니다.   |
| 운영 환경 대비              | DB 변경을 추적 가능한 형태로 관리할 수 있습니다.                      |

---

## 3. 현재 프로젝트 Flyway 설정

현재 프로젝트의 `application.yaml`에는 Flyway 설정이 아래와 같이 작성되어 있습니다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true
    locations: classpath:db/migration
```

### 설정 설명

| 설정                                                | 설명                                                     |
| ------------------------------------------------- | ------------------------------------------------------ |
| `spring.jpa.hibernate.ddl-auto: validate`         | Entity와 DB 테이블 구조가 맞는지 검증합니다. 테이블을 자동 생성하거나 수정하지 않습니다. |
| `spring.flyway.enabled: true`                     | Flyway migration을 활성화합니다.                              |
| `spring.flyway.locations: classpath:db/migration` | migration SQL 파일을 찾을 위치를 지정합니다.                        |

본 프로젝트에서는 Flyway가 테이블 생성과 변경을 담당하고, JPA는 `validate`를 통해 Entity와 DB 구조가 맞는지만 확인합니다.

---

## 4. Flyway 의존성

현재 프로젝트에서는 PostgreSQL 환경에서 Flyway를 사용하기 위해 아래 의존성을 추가합니다.

```gradle
// Flyway
implementation 'org.flywaydb:flyway-core'
implementation 'org.flywaydb:flyway-database-postgresql'
```

### 의존성 설명

| 의존성                          | 설명                                                |
| ---------------------------- | ------------------------------------------------- |
| `flyway-core`                | Flyway migration 기능을 사용하기 위한 핵심 라이브러리입니다.         |
| `flyway-database-postgresql` | PostgreSQL 데이터베이스에서 Flyway를 사용하기 위한 DB별 지원 모듈입니다. |

---

## 5. Migration Script 위치

Flyway migration SQL 파일은 아래 위치에 작성합니다.

```text
src/main/resources/db/migration
```

현재 프로젝트 기준 파일 위치는 다음과 같습니다.

```text
src/main/resources/db/migration/V1__init_tables.sql
```

---

## 6. Migration Script 파일명 규칙

Flyway의 versioned migration 파일은 아래 형식을 사용합니다.

```text
V{버전}__{설명}.sql
```

주의할 점은 `V1`과 설명 사이에 언더스코어 `_`가 **두 개** 들어간다는 점입니다.

```text
V1__init_tables.sql
```

### 파일명 예시

```text
V1__init_tables.sql
V2__add_sync_job_error_message.sql
V3__add_index_data_indexes.sql
V4__alter_index_info_source_type.sql
```

### 파일명 규칙

| 부분            | 설명                          |
| ------------- | --------------------------- |
| `V`           | Versioned migration을 의미합니다. |
| `1`, `2`, `3` | migration 버전입니다.            |
| `__`          | 버전과 설명을 구분하는 구분자입니다.        |
| `init_tables` | migration 설명입니다.            |
| `.sql`        | SQL migration 파일 확장자입니다.    |

---

## 7. 현재 작성된 V1 Migration Script

현재 작성된 `V1__init_tables.sql`은 초기 테이블 생성을 담당합니다.

생성되는 주요 테이블은 다음과 같습니다.

| 테이블                | 설명          |
| ------------------ | ----------- |
| `index_info`       | 지수 기본 정보    |
| `index_data`       | 지수별 일자별 데이터 |
| `sync_job`         | 연동 작업 이력    |
| `auto_sync_config` | 자동 연동 설정    |

---

## 8. V1 Script 주요 내용

### 8-1. `index_info`

`index_info` 테이블은 지수의 기본 정보를 저장합니다.

주요 컬럼:

| 컬럼                     | 설명       |
| ---------------------- | -------- |
| `id`                   | 지수 정보 ID |
| `index_classification` | 지수 분류    |
| `index_name`           | 지수명      |
| `employed_items_count` | 채용 종목 수  |
| `base_point_in_time`   | 기준 시점    |
| `base_index`           | 기준 지수    |
| `source_type`          | 데이터 출처   |
| `favorite`             | 즐겨찾기 여부  |

제약조건:

```sql
ALTER TABLE "index_info"
    ADD CONSTRAINT uq_index_info_classification_name
        UNIQUE (index_classification, index_name);
```

같은 지수 분류와 지수명이 중복 저장되지 않도록 unique 제약조건을 설정합니다.

---

### 8-2. `index_data`

`index_data` 테이블은 지수별 일자별 시장 데이터를 저장합니다.

주요 컬럼:

| 컬럼                    | 설명        |
| --------------------- | --------- |
| `id`                  | 지수 데이터 ID |
| `index_info_id`       | 지수 정보 ID  |
| `base_date`           | 기준일자      |
| `index_type`          | 지수 타입     |
| `market_price`        | 시가        |
| `closing_price`       | 종가        |
| `high_price`          | 고가        |
| `low_price`           | 저가        |
| `versus`              | 대비        |
| `fluctuation_rate`    | 등락률       |
| `trading_quantity`    | 거래량       |
| `trading_price`       | 거래대금      |
| `market_total_amount` | 상장 시가총액   |

제약조건:

```sql
ALTER TABLE "index_data"
    ADD CONSTRAINT uq_index_data_info_base_date
        UNIQUE (index_info_id, base_date);
```

같은 지수에 대해 같은 기준일자의 데이터가 중복 저장되지 않도록 unique 제약조건을 설정합니다.

---

### 8-3. `sync_job`

`sync_job` 테이블은 공공데이터 API 연동 작업 이력을 저장합니다.

주요 컬럼:

| 컬럼              | 설명       |
| --------------- | -------- |
| `id`            | 연동 작업 ID |
| `index_info_id` | 지수 정보 ID |
| `job_type`      | 작업 유형    |
| `target_date`   | 연동 대상 날짜 |
| `worker`        | 작업자      |
| `job_time`      | 작업 시각    |
| `result`        | 작업 결과    |

`sync_job`은 어떤 지수에 대해 언제, 누가, 어떤 결과로 연동을 수행했는지 기록하기 위한 테이블입니다.

---

### 8-4. `auto_sync_config`

`auto_sync_config` 테이블은 지수별 자동 연동 설정을 저장합니다.

주요 컬럼:

| 컬럼              | 설명           |
| --------------- | ------------ |
| `id`            | 자동 연동 설정 ID  |
| `index_info_id` | 지수 정보 ID     |
| `enabled`       | 자동 연동 활성화 여부 |

제약조건:

```sql
ALTER TABLE "auto_sync_config"
    ADD CONSTRAINT uq_auto_sync_config_index_info
        UNIQUE (index_info_id);
```

하나의 지수 정보에는 하나의 자동 연동 설정만 연결되도록 unique 제약조건을 설정합니다.

---

## 9. 테이블 관계

현재 V1 script 기준 테이블 관계는 다음과 같습니다.

```text
index_info 1 ── N index_data
index_info 1 ── N sync_job
index_info 1 ── 1 auto_sync_config
```

관계 설명:

| 관계                                | 설명                                   |
| --------------------------------- | ------------------------------------ |
| `index_info` → `index_data`       | 하나의 지수 정보는 여러 일자별 지수 데이터를 가질 수 있습니다. |
| `index_info` → `sync_job`         | 하나의 지수 정보는 여러 연동 이력을 가질 수 있습니다.      |
| `index_info` → `auto_sync_config` | 하나의 지수 정보는 하나의 자동 연동 설정을 가질 수 있습니다.  |

---

## 10. Foreign Key 제약조건

현재 script에서는 `index_data`, `sync_job`, `auto_sync_config`가 모두 `index_info`를 참조합니다.

```sql
ALTER TABLE "index_data"
    ADD CONSTRAINT "FK_index_info_TO_index_data_1" FOREIGN KEY ("index_info_id")
        REFERENCES "index_info" ("id");
```

```sql
ALTER TABLE "sync_job"
    ADD CONSTRAINT "FK_index_info_TO_sync_job_1" FOREIGN KEY ("index_info_id")
        REFERENCES "index_info" ("id");
```

```sql
ALTER TABLE "auto_sync_config"
    ADD CONSTRAINT "FK_index_info_TO_auto_sync_config_1" FOREIGN KEY ("index_info_id")
        REFERENCES "index_info" ("id");
```

현재 FK에는 `ON DELETE CASCADE`가 설정되어 있지 않습니다.

따라서 `index_info`에 연결된 `index_data`, `sync_job`, `auto_sync_config`가 있으면 해당 `index_info` 삭제가 제한될 수 있습니다.

이 정책은 다음 의미를 가집니다.

```text
연결된 데이터가 있는 지수 정보는 함부로 삭제되지 않는다.
```

만약 지수 정보 삭제 시 관련 데이터도 함께 삭제해야 한다면, 별도의 migration script에서 `ON DELETE CASCADE` 적용 여부를 논의해야 합니다.

---

## 11. Flyway 실행 흐름

Spring Boot 애플리케이션 실행 시 Flyway는 지정된 migration 위치를 확인합니다.

현재 설정 기준 위치:

```text
classpath:db/migration
```

실행 흐름은 다음과 같습니다.

```text
1. 애플리케이션 실행
2. DataSource 연결
3. Flyway가 db/migration 아래 migration script 확인
4. 아직 적용되지 않은 versioned migration 실행
5. migration 성공 시 schema history table에 이력 저장
6. JPA ddl-auto validate로 Entity와 DB 구조 검증
7. 애플리케이션 정상 실행
```

---

## 12. Flyway Schema History Table

Flyway는 migration 실행 이력을 관리하기 위해 schema history table을 생성합니다.

기본적으로 이 테이블에는 다음 정보가 기록됩니다.

```text
적용된 migration version
migration description
script name
checksum
실행 시간
성공 여부
```

이를 통해 Flyway는 어떤 migration이 이미 적용되었는지 확인하고, 아직 적용되지 않은 migration만 실행합니다.

---

## 13. Migration Script 작성 규칙

### 13-1. 이미 적용된 migration은 수정하지 않는다

한 번 DB에 적용된 migration 파일은 수정하지 않습니다.

예를 들어 `V1__init_tables.sql`이 이미 실행된 상태에서 해당 파일 내용을 수정하면 checksum 불일치 문제가 발생할 수 있습니다.

잘못된 예:

```text
V1__init_tables.sql 실행 완료
→ V1__init_tables.sql 내용 수정
→ 다음 실행 시 checksum 불일치 발생 가능
```

수정이 필요한 경우에는 새로운 migration 파일을 추가합니다.

```text
V2__alter_index_info_columns.sql
```

---

### 13-2. 기존 테이블 변경은 새로운 버전으로 작성한다

이미 적용된 테이블에 컬럼을 추가해야 한다면 `V1`을 수정하지 않고 `V2`를 작성합니다.

예시:

```sql
ALTER TABLE "sync_job"
    ADD COLUMN "error_message" TEXT NULL;
```

파일명:

```text
V2__add_sync_job_error_message.sql
```

---

### 13-3. 데이터 보존을 고려한다

운영 중인 테이블의 컬럼을 삭제하거나 타입을 변경할 때는 데이터 손실 가능성을 고려해야 합니다.

주의가 필요한 작업:

```text
DROP TABLE
DROP COLUMN
ALTER COLUMN TYPE
NOT NULL 컬럼 추가
기존 데이터와 충돌하는 UNIQUE 제약조건 추가
```

---

### 13-4. 제약조건 이름은 명확하게 작성한다

제약조건 이름은 역할이 드러나도록 작성합니다.

예시:

```sql
CONSTRAINT uq_index_data_info_base_date
CONSTRAINT uq_auto_sync_config_index_info
CONSTRAINT FK_index_info_TO_index_data_1
```

---

## 14. 새 Migration 추가 예시

### 예시 1. 컬럼 추가

파일명:

```text
V2__add_sync_job_error_message.sql
```

내용:

```sql
ALTER TABLE "sync_job"
    ADD COLUMN "error_message" TEXT NULL;
```

---

### 예시 2. 인덱스 추가

파일명:

```text
V3__add_index_data_search_indexes.sql
```

내용:

```sql
CREATE INDEX idx_index_data_base_date
    ON "index_data" ("base_date");

CREATE INDEX idx_index_data_index_info_base_date
    ON "index_data" ("index_info_id", "base_date");
```

---

### 예시 3. 컬럼 타입 변경

파일명:

```text
V4__alter_index_info_base_index_precision.sql
```

내용:

```sql
ALTER TABLE "index_info"
    ALTER COLUMN "base_index" TYPE DECIMAL(19, 4);
```

---

## 15. 로컬에서 Flyway 실행 확인하기

본 프로젝트는 IntelliJ 실행 버튼을 기준으로 실행할 수 있습니다.

### IntelliJ 실행 기준

IntelliJ Run Configuration에서 아래 설정을 확인합니다.

```text
Active profiles: local
```

그리고 애플리케이션을 실행합니다.

애플리케이션이 정상 실행되면 Flyway가 자동으로 migration을 수행합니다.

---

### CLI 실행 기준

Mac / Linux / Git Bash:

```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

Windows PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\gradlew.bat bootRun
```

---

## 16. 실행 후 확인 방법

PostgreSQL에서 아래 쿼리로 테이블 생성 여부를 확인할 수 있습니다.

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

예상 테이블:

```text
auto_sync_config
flyway_schema_history
index_data
index_info
sync_job
```

Flyway migration 이력은 아래 쿼리로 확인할 수 있습니다.

```sql
SELECT installed_rank,
       version,
       description,
       script,
       success,
       installed_on
FROM flyway_schema_history
ORDER BY installed_rank;
```

예상 결과 예시:

```text
version: 1
description: init tables
script: V1__init_tables.sql
success: true
```

---

## 17. 자주 발생하는 문제

### 문제 1. 이미 적용된 V1 script를 수정한 경우

증상:

```text
Validate failed: Migration checksum mismatch
```

원인:

```text
이미 DB에 적용된 migration 파일 내용을 수정했기 때문입니다.
```

해결 방법:

```text
1. 이미 공유된 migration 파일은 수정하지 않습니다.
2. 변경사항은 V2, V3처럼 새 migration으로 작성합니다.
3. 로컬에서만 테스트 중이고 DB를 초기화해도 되는 경우에는 DB를 재생성할 수 있습니다.
```

---

### 문제 2. 테이블이 이미 존재하는 경우

증상:

```text
relation "index_info" already exists
```

원인:

```text
기존에 수동으로 테이블을 만들었거나, 이전 migration 결과가 남아있을 수 있습니다.
```

해결 방법:

```text
1. 로컬 개발 DB라면 DB를 초기화한 뒤 다시 실행합니다.
2. 기존 DB를 유지해야 한다면 baseline 전략을 별도로 검토합니다.
3. 팀 프로젝트 초기 단계에서는 가능하면 DB를 새로 만들고 V1부터 적용합니다.
```

---

### 문제 3. JPA validate 실패

증상:

```text
Schema-validation: missing table
Schema-validation: wrong column type
```

원인:

```text
Entity와 Flyway로 생성된 DB 테이블 구조가 일치하지 않는 경우입니다.
```

해결 방법:

```text
1. Entity의 @Table, @Column 이름이 DB 컬럼명과 일치하는지 확인합니다.
2. Enum 저장 방식이 DB 컬럼 타입과 맞는지 확인합니다.
3. 숫자 타입 BigDecimal, Long, Integer와 DB 타입 DECIMAL, BIGINT, INT가 맞는지 확인합니다.
4. 필요하면 새로운 migration script로 DB 구조를 수정합니다.
```

---

### 문제 4. Flyway script 위치를 찾지 못하는 경우

확인할 것:

```text
1. 파일 위치가 src/main/resources/db/migration 아래인지 확인합니다.
2. 파일명이 V1__init_tables.sql 형식인지 확인합니다.
3. 언더스코어가 한 개가 아니라 두 개인지 확인합니다.
4. application.yaml의 spring.flyway.locations가 classpath:db/migration인지 확인합니다.
```

---

## 18. 팀원 작업 규칙

팀원들은 DB 구조 변경이 필요할 때 아래 규칙을 따릅니다.

```text
1. 이미 적용된 migration 파일은 수정하지 않는다.
2. DB 변경이 필요하면 새 version migration 파일을 추가한다.
3. 파일명은 V{번호}__{설명}.sql 형식을 따른다.
4. migration script 작성 후 로컬에서 애플리케이션을 실행해 적용 여부를 확인한다.
5. Entity 변경과 migration script 변경을 함께 PR에 포함한다.
6. PR 설명에 어떤 테이블/컬럼/제약조건이 변경되었는지 작성한다.
```

---

## 19. PR 작성 시 체크리스트

DB 변경이 포함된 PR은 아래 내용을 확인합니다.

```text
1. migration 파일명이 올바른가?
2. 기존 migration 파일을 수정하지 않았는가?
3. Entity와 DB 컬럼명이 일치하는가?
4. NOT NULL 컬럼 추가 시 기존 데이터에 대한 처리가 있는가?
5. UNIQUE 제약조건 추가 시 기존 데이터와 충돌하지 않는가?
6. FK 제약조건 추가 시 참조 데이터가 존재하는가?
7. 로컬에서 애플리케이션 실행 시 Flyway가 정상 적용되는가?
8. flyway_schema_history에 success=true로 기록되는가?
```

---

## 20. 최종 정리

본 프로젝트에서는 Flyway를 사용하여 DB 스키마 변경 이력을 관리합니다.

현재 초기 migration 파일은 다음과 같습니다.

```text
src/main/resources/db/migration/V1__init_tables.sql
```

현재 V1 migration에서 생성하는 테이블은 다음과 같습니다.

```text
index_info
index_data
sync_job
auto_sync_config
```

현재 프로젝트의 Flyway 설정은 다음과 같습니다.

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
```

JPA는 DB 구조를 자동 생성하지 않고 검증만 수행합니다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
```

DB 구조 변경이 필요하면 기존 migration 파일을 수정하지 않고 새로운 migration 파일을 추가합니다.

```text
V2__add_sync_job_error_message.sql
V3__add_index_data_search_indexes.sql
```

Flyway script는 DB 변경 이력을 팀원 모두가 동일하게 공유하기 위한 기준 파일입니다.
