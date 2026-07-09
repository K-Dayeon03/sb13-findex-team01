CREATE TABLE "sync_job"
(
    "id"            BIGSERIAL    NOT NULL,
    "index_info_id" BIGINT       NOT NULL,
    "job_type"      VARCHAR(30)  NOT NULL,
    "target_date"   DATE NULL,
    "worker"        VARCHAR(100) NOT NULL,
    "job_time"      TIMESTAMP    NOT NULL,
    "result"        VARCHAR(30)  NOT NULL
);

CREATE TABLE "index_data"
(
    "id"                  BIGSERIAL   NOT NULL,
    "index_info_id"       BIGINT      NOT NULL,
    "base_date"           DATE        NOT NULL,
    "index_type"          VARCHAR(30) NOT NULL,
    "market_price"        DECIMAL     NOT NULL,
    "closing_price"       DECIMAL     NOT NULL,
    "high_price"          DECIMAL     NOT NULL,
    "low_price"           DECIMAL     NOT NULL,
    "versus"              DECIMAL     NOT NULL,
    "fluctuation_rate"    DECIMAL     NOT NULL,
    "trading_quantity"    BIGINT      NOT NULL,
    "trading_price"       BIGINT      NOT NULL,
    "market_total_amount" BIGINT      NOT NULL
);

CREATE TABLE "auto_sync_config"
(
    "id"            BIGSERIAL NOT NULL,
    "index_info_id" BIGINT    NOT NULL,
    "enabled"       BOOLEAN   NOT NULL
);

CREATE TABLE "index_info"
(
    "id"                   BIGSERIAL    NOT NULL,
    "index_classification" VARCHAR(50)  NOT NULL,
    "index_name"           VARCHAR(100) NOT NULL,
    "employed_items_count" INT          NOT NULL,
    "base_point_in_time"   DATE         NOT NULL,
    "base_index"           DECIMAL      NOT NULL,
    "source_type"          VARCHAR(30)  NOT NULL,
    "favorite"             BOOLEAN      NOT NULL
);

ALTER TABLE "sync_job"
    ADD CONSTRAINT "PK_SYNC_JOB" PRIMARY KEY ("id");

ALTER TABLE "index_data"
    ADD CONSTRAINT "PK_INDEX_DATA" PRIMARY KEY ("id");

ALTER TABLE "auto_sync_config"
    ADD CONSTRAINT "PK_AUTO_SYNC_CONFIG" PRIMARY KEY ("id");

ALTER TABLE "index_info"
    ADD CONSTRAINT "PK_INDEX_INFO" PRIMARY KEY ("id");

ALTER TABLE "sync_job"
    ADD CONSTRAINT "FK_index_info_TO_sync_job_1" FOREIGN KEY ("index_info_id")
        REFERENCES "index_info" ("id");

ALTER TABLE "index_data"
    ADD CONSTRAINT "FK_index_info_TO_index_data_1" FOREIGN KEY ("index_info_id")
        REFERENCES "index_info" ("id");

ALTER TABLE "auto_sync_config"
    ADD CONSTRAINT "FK_index_info_TO_auto_sync_config_1" FOREIGN KEY ("index_info_id")
        REFERENCES "index_info" ("id");

ALTER TABLE "auto_sync_config"
    ADD CONSTRAINT uq_auto_sync_config_index_info
        UNIQUE (index_info_id);

ALTER TABLE "index_data"
    ADD CONSTRAINT uq_index_data_info_base_date
        UNIQUE (index_info_id, base_date);

ALTER TABLE "index_info"
    ADD CONSTRAINT uq_index_info_classification_name
        UNIQUE (index_classification, index_name);
