
--changeset abondar:001-create-products-table
CREATE TABLE products (
                          id UUID PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          img_url TEXT NOT NULL,
                          price NUMERIC(12, 2) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_created_at ON products (created_at);

--rollback DROP TABLE products;
