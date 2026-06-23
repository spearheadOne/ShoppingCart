--liquibase formatted sql

--changeset abondar:003-create-orders-table

CREATE TABLE orders (
                        id UUID PRIMARY KEY,
                        cart_id UUID NOT NULL,
                        status VARCHAR(32) NOT NULL,
                        total_price DECIMAL(19, 2) NOT NULL,
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                        CONSTRAINT uk_orders_cart_id UNIQUE (cart_id),
                        CONSTRAINT chk_orders_total_price
                            CHECK (total_price >= 0)
);

--rollback DROP TABLE orders;
