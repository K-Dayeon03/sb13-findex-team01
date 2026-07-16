ALTER TABLE index_info
    ALTER COLUMN "base_point_in_time" DROP NOT NULL,
    ALTER COLUMN "base_index" DROP NOT NULL,
    ALTER COLUMN "index_classification" TYPE VARCHAR(30);
