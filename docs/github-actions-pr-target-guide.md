# GitHub Actions로 PR 대상 브랜치 검증하기

## 1. 목적

본 문서는 `main` 브랜치로 들어오는 Pull Request의 출발 브랜치를 검증하기 위한 GitHub Actions 설정 방법을 정리한 문서입니다.

본 프로젝트의 브랜치 전략은 다음과 같습니다.

```text
feature/* → dev → main
```

따라서 `main` 브랜치로는 반드시 `dev` 브랜치에서만 Pull Request를 생성해야 합니다.

아래와 같은 PR은 허용합니다.

```text
dev → main
```

아래와 같은 PR은 허용하지 않습니다.

```text
feature/index-info → main
fix/bug → main
docs/readme → main
```

이를 팀 규칙으로만 관리할 수도 있지만, 실수를 줄이기 위해 GitHub Actions를 사용하여 자동으로 검증합니다.

---

## 2. 현재 브랜치 전략

본 프로젝트는 개인 저장소에 팀원들을 collaborator로 초대한 방식으로 진행합니다.
따라서 fork 방식이 아니라 원본 저장소를 clone 받아 feature branch를 생성하여 작업합니다.

### Branch

| 브랜치         | 역할            |
| ----------- | ------------- |
| `main`      | 최종 발표 및 안정 버전 |
| `dev`       | 개발 통합 브랜치     |
| `feature/*` | 기능 개발 브랜치     |
| `fix/*`     | 버그 수정 브랜치     |
| `docs/*`    | 문서 수정 브랜치     |

### Merge Flow

```text
feature/* → dev
fix/* → dev
docs/* → dev
dev → main
feature/* → main 금지
```

### Rules

* `main`과 `dev`에는 직접 push하지 않습니다.
* 모든 작업은 PR을 통해 merge합니다.
* `feature/*` 브랜치는 항상 `dev`에서 생성합니다.
* PR 생성 전 로컬에서 서버 실행 또는 빌드 확인을 합니다.
* PR은 최소 1명 이상의 리뷰 후 merge합니다.
* `main`으로 가는 PR은 `dev` 브랜치에서만 생성합니다.

---

## 3. GitHub Actions 적용 이유

GitHub의 Branch Protection 설정으로 `main` 브랜치 직접 push, force push, 삭제, 리뷰 없이 merge 등을 막을 수 있습니다.

하지만 일반적인 Branch Protection만으로는 아래 규칙을 완전히 강제하기 어렵습니다.

```text
main 브랜치로는 dev 브랜치에서만 PR을 보낼 수 있다.
```

따라서 GitHub Actions를 사용하여 `main` 대상 PR의 source branch를 검사합니다.

---

## 4. 동작 방식

이번에 추가할 workflow는 `main` 브랜치로 들어오는 PR에서만 실행됩니다.

```yaml
on:
  pull_request:
    branches:
      - main
```

여기서 `branches: main`은 PR의 대상 브랜치, 즉 base branch가 `main`일 때만 실행된다는 의미입니다.

따라서 아래 PR에서는 workflow가 실행됩니다.

```text
dev → main
feature/index-info → main
fix/bug → main
docs/readme → main
```

반대로 아래 PR에서는 실행되지 않을 수 있습니다.

```text
feature/index-info → dev
fix/bug → dev
docs/readme → dev
```

이것은 정상 동작입니다.
이 workflow의 목적은 `main` 브랜치로 잘못 들어오는 PR을 막는 것이기 때문입니다.

---

## 5. workflow 파일 위치

아래 경로에 workflow 파일을 추가합니다.

```text
.github/workflows/validate-pr-target.yml
```

디렉터리가 없다면 직접 생성합니다.

```bash
mkdir -p .github/workflows
```

---

## 6. workflow 파일 내용

`.github/workflows/validate-pr-target.yml` 파일에 아래 내용을 작성합니다.

```yaml
name: Validate PR Target

on:
  pull_request:
    branches:
      - main

jobs:
  validate-pr-source:
    name: Validate PR Source Branch
    runs-on: ubuntu-latest

    steps:
      - name: Check PR source branch
        run: |
          echo "base branch: ${{ github.base_ref }}"
          echo "head branch: ${{ github.head_ref }}"

          if [ "${{ github.base_ref }}" = "main" ] && [ "${{ github.head_ref }}" != "dev" ]; then
            echo "main 브랜치로는 dev 브랜치에서만 PR을 보낼 수 있습니다."
            exit 1
          fi
```

---

## 7. workflow 코드 설명

### `name`

```yaml
name: Validate PR Target
```

GitHub Actions 화면에 표시될 workflow 이름입니다.

---

### `on.pull_request.branches`

```yaml
on:
  pull_request:
    branches:
      - main
```

PR의 대상 브랜치가 `main`일 때만 workflow를 실행합니다.

즉, 아래 PR에서는 실행됩니다.

```text
dev → main
feature/index-info → main
```

하지만 아래 PR에서는 실행되지 않을 수 있습니다.

```text
feature/index-info → dev
```

---

### `jobs.validate-pr-source.name`

```yaml
jobs:
  validate-pr-source:
    name: Validate PR Source Branch
```

실제로 status check 목록에 표시될 수 있는 job 이름입니다.
나중에 Branch Protection의 Required Status Check로 등록할 때 이 이름을 확인해야 합니다.

---

### `github.base_ref`

```yaml
${{ github.base_ref }}
```

PR의 대상 브랜치를 의미합니다.

예시:

```text
base branch: main
```

---

### `github.head_ref`

```yaml
${{ github.head_ref }}
```

PR의 출발 브랜치를 의미합니다.

예시:

```text
head branch: dev
head branch: feature/index-info
```

---

### 검증 조건

```bash
if [ "${{ github.base_ref }}" = "main" ] && [ "${{ github.head_ref }}" != "dev" ]; then
  echo "main 브랜치로는 dev 브랜치에서만 PR을 보낼 수 있습니다."
  exit 1
fi
```

조건은 다음과 같습니다.

```text
대상 브랜치가 main이고
출발 브랜치가 dev가 아니라면
workflow 실패 처리
```

따라서 아래 PR은 성공합니다.

```text
dev → main
```

아래 PR은 실패합니다.

```text
feature/index-info → main
fix/bug → main
docs/readme → main
```

---

## 8. 적용 순서

### 1. `dev` 브랜치에서 feature 브랜치 생성

```bash
git checkout dev
git pull origin dev

git checkout -b feature/validate-pr-target
```

---

### 2. workflow 파일 추가

```bash
mkdir -p .github/workflows
touch .github/workflows/validate-pr-target.yml
```

파일에 아래 내용을 작성합니다.

```yaml
name: Validate PR Target

on:
  pull_request:
    branches:
      - main

jobs:
  validate-pr-source:
    name: Validate PR Source Branch
    runs-on: ubuntu-latest

    steps:
      - name: Check PR source branch
        run: |
          echo "base branch: ${{ github.base_ref }}"
          echo "head branch: ${{ github.head_ref }}"

          if [ "${{ github.base_ref }}" = "main" ] && [ "${{ github.head_ref }}" != "dev" ]; then
            echo "main 브랜치로는 dev 브랜치에서만 PR을 보낼 수 있습니다."
            exit 1
          fi
```

---

### 3. 커밋 후 push

```bash
git add .github/workflows/validate-pr-target.yml
git commit
git push origin feature/validate-pr-target
```

커밋 메시지 예시:

```text
Add: PR 대상 브랜치 검증 워크플로우 추가
```

---

### 4. PR 생성

GitHub에서 PR을 생성합니다.

```text
base: dev
compare: feature/validate-pr-target
```

이 PR에서는 workflow가 실행되지 않을 수 있습니다.
정상입니다.

이 workflow는 `main`으로 들어가는 PR만 검사하기 때문입니다.

---

### 5. `feature/validate-pr-target → dev` merge

리뷰 후 `dev` 브랜치에 merge합니다.

---

### 6. `dev → main` PR 생성

workflow 파일을 `main` 브랜치에도 반영하기 위해 PR을 생성합니다.

```text
base: main
compare: dev
```

이 PR에서는 workflow가 실행됩니다.

출발 브랜치가 `dev`이므로 workflow는 통과해야 합니다.

---

### 7. `dev → main` merge

`dev → main` PR을 merge하면 `main` 브랜치에도 workflow 파일이 반영됩니다.

이후부터는 `main` 대상 PR에서 source branch 검증이 동작합니다.

---

## 9. main Branch Protection에 Required Status Check 등록

workflow를 실제 merge 차단 조건으로 사용하려면 `main` 브랜치 보호 설정에 required status check로 등록해야 합니다.

### 설정 경로

```text
Repository → Settings → Branches → Branch protection rules → main
```

또는 branch protection rule이 없다면 새로 생성합니다.

```text
Repository → Settings → Branches → Add branch protection rule
```

Branch name pattern:

```text
main
```

---

### 설정 항목

아래 항목을 활성화합니다.

```text
Require status checks to pass before merging
```

그리고 status check 목록에서 아래 이름을 선택합니다.

```text
Validate PR Source Branch
```

GitHub 화면에 따라 아래처럼 보일 수도 있습니다.

```text
Validate PR Target / Validate PR Source Branch
```

---

## 10. main 브랜치 권장 보호 설정

`main` 브랜치에는 아래 설정을 권장합니다.

```text
Require a pull request before merging
Require approvals: 1
Dismiss stale pull request approvals when new commits are pushed
Require status checks to pass before merging
Require conversation resolution before merging
Do not allow bypassing the above settings
Block force pushes
Block deletions
```

### 설정 의미

| 설정                                    | 의미                            |
| ------------------------------------- | ----------------------------- |
| Require a pull request before merging | main에 직접 push하지 않고 PR로만 merge |
| Require approvals: 1                  | 최소 1명 이상의 리뷰 승인 필요            |
| Dismiss stale approvals               | 승인 후 코드가 바뀌면 다시 리뷰 필요         |
| Require status checks                 | 필수 GitHub Actions 통과 필요       |
| Require conversation resolution       | 리뷰 코멘트 미해결 상태에서 merge 방지      |
| Do not allow bypassing                | 레포 주인도 규칙 우회 방지               |
| Block force pushes                    | force push 방지                 |
| Block deletions                       | 브랜치 삭제 방지                     |

---

## 11. 테스트 시나리오

workflow가 정상 동작하는지 확인하려면 아래 시나리오를 테스트합니다.

### 성공해야 하는 PR

```text
dev → main
```

예상 결과:

```text
Validate PR Source Branch 성공
```

---

### 실패해야 하는 PR

```text
feature/test-main-pr → main
```

예상 결과:

```text
Validate PR Source Branch 실패
```

실패 메시지 예시:

```text
main 브랜치로는 dev 브랜치에서만 PR을 보낼 수 있습니다.
```

---

## 12. 주의사항

### 1. workflow 파일은 main 브랜치에 있어야 안정적으로 동작합니다.

처음에는 `feature → dev` PR로 workflow 파일을 추가하고, 이후 `dev → main` PR을 통해 `main` 브랜치에 반영해야 합니다.

---

### 2. `feature → dev` PR에서는 실행되지 않을 수 있습니다.

현재 설정은 아래와 같습니다.

```yaml
on:
  pull_request:
    branches:
      - main
```

따라서 이 workflow는 `main` 대상 PR에서만 실행됩니다.

---

### 3. required status check는 workflow가 한 번 실행된 뒤 선택 가능할 수 있습니다.

GitHub 설정 화면에서 required status check 목록에 `Validate PR Source Branch`가 바로 보이지 않을 수 있습니다.

이 경우 먼저 `dev → main` PR을 한 번 생성하여 workflow를 실행한 뒤 다시 확인합니다.

---

### 4. workflow만 추가해도 PR이 실패하긴 하지만, merge 차단은 branch protection 설정이 필요합니다.

GitHub Actions가 실패하더라도 Branch Protection에서 required status check로 등록하지 않으면 merge 버튼이 완전히 막히지 않을 수 있습니다.

따라서 반드시 `main` Branch Protection에 required status check로 등록해야 합니다.

---

### 5. 브랜치 이름이 정확히 `dev`여야 합니다.

workflow 조건은 source branch가 정확히 `dev`인지 검사합니다.

```bash
if [ "${{ github.base_ref }}" = "main" ] && [ "${{ github.head_ref }}" != "dev" ]; then
```

따라서 아래 이름은 통과하지 못합니다.

```text
develop
development
dev/main
```

본 프로젝트에서는 통합 브랜치명을 `dev`로 통일합니다.

---

## 13. 팀원 안내 문구

팀원에게는 아래와 같이 안내합니다.

```text
main 브랜치로는 dev 브랜치에서만 PR을 보낼 수 있습니다.

기능 개발은 항상 dev에서 feature 브랜치를 생성한 뒤 작업합니다.

작업 흐름:
1. dev 브랜치 최신화
2. feature/* 브랜치 생성
3. 작업 후 push
4. PR 생성
   - base: dev
   - compare: feature/작업명
5. 리뷰 후 dev로 merge

최종 발표 전에는 dev에서 main으로 PR을 생성합니다.

feature/* → main PR은 GitHub Actions에서 실패합니다.
```

---

## 14. 전체 흐름 정리

```text
1. dev에서 feature/validate-pr-target 생성
2. .github/workflows/validate-pr-target.yml 추가
3. feature/validate-pr-target → dev PR 생성
4. 리뷰 후 dev에 merge
5. dev → main PR 생성
6. workflow 실행 확인
7. dev → main merge
8. main branch protection에서 required status check 등록
9. feature/* → main PR 실패 여부 테스트
```

---

## 15. 최종 정리

이번 설정의 목적은 아래 규칙을 자동으로 강제하는 것입니다.

```text
main 브랜치로는 dev 브랜치에서만 PR을 보낼 수 있다.
```

허용되는 PR:

```text
dev → main
```

허용되지 않는 PR:

```text
feature/* → main
fix/* → main
docs/* → main
```

workflow 파일 위치:

```text
.github/workflows/validate-pr-target.yml
```

workflow 이름:

```text
Validate PR Target
```

status check 이름:

```text
Validate PR Source Branch
```

커밋 메시지 예시:

```text
Add: PR 대상 브랜치 검증 워크플로우 추가
```
