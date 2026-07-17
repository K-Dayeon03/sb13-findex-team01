## Railway 배포 환경 설정

Railway 배포에 필요한 환경 변수는 애플리케이션 서비스의 `Variables`에 등록해야 합니다.

저장소의 `.env.example` 파일은 필요한 환경 변수 목록을 공유하기 위한 템플릿입니다.  
파일을 저장소에 추가하는 것만으로는 Railway 실행 환경에 자동 적용되지 않습니다.

### 환경 변수 등록

Railway 프로젝트에서 애플리케이션 서비스를 선택한 뒤 다음 위치에서 환경 변수를 등록합니다.

```text
Application Service > Variables > Raw Editor
```

`.env.example`의 내용을 복사하여 Raw Editor에 붙여 넣습니다.

```dotenv
SPRING_PROFILES_ACTIVE=prod

DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=${{Postgres.PGDATABASE}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}

RAILPACK_JDK_VERSION=17

JAVA_TOOL_OPTIONS="-Duser.timezone=Asia/Seoul -Xms128m -Xmx384m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/heapdump-%p.hprof -XX:+ExitOnOutOfMemoryError -Xlog:gc=warning:stdout:time,level,tags"

AUTO_SYNC_CRON="0 0 1 * * *"
AUTO_SYNC_ZONE=Asia/Seoul
```

환경 변수를 등록하거나 수정한 뒤 애플리케이션을 다시 배포해야 합니다.

비밀번호, API Key와 같은 실제 민감정보는 저장소에 커밋하지 않고 Railway Variables에서 별도로 관리합니다.

---

## Railway Volume 설정

애플리케이션은 JVM OutOfMemoryError 발생 시 다음 경로에 Heap Dump를 생성하도록 설정되어 있습니다.

```text
/data/heapdump-{process-id}.hprof
```

Railway의 기본 배포 파일시스템은 배포 교체 또는 컨테이너 재시작 후 파일이 보존되지 않을 수 있습니다.

Heap Dump를 영구적으로 보관하려면 애플리케이션 서비스에 Railway Volume을 연결해야 합니다.

### Volume 연결 방법

1. Railway 프로젝트에서 애플리케이션 서비스를 선택합니다.
2. 서비스에 새로운 Volume을 추가합니다.
3. Volume의 Mount Path를 `/data`로 설정합니다.
4. 변경 사항을 저장하고 애플리케이션을 다시 배포합니다.

```text
Mount Path: /data
```

Volume이 `/data`에 정상적으로 연결되면 OOM 발생 시 생성되는 Heap Dump가 다음과 같이 저장됩니다.

```text
/data/heapdump-1.hprof
```

Volume을 연결하지 않으면 Heap Dump가 생성되더라도 배포 교체 또는 컨테이너 재생성 이후 파일이 사라질 수 있습니다.

### Heap Dump를 사용하지 않는 경우

Heap Dump를 보관하거나 분석하지 않을 경우 `JAVA_TOOL_OPTIONS`에서 다음 옵션을 제거할 수 있습니다.

```text
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/data/heapdump-%p.hprof
```